/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2007, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package net.seagis.coverage.web;

import java.io.*;
import java.util.*;
import java.sql.SQLException;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.media.jai.Interpolation;

import org.opengis.coverage.grid.GridRange;
import org.opengis.coverage.grid.GridGeometry;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.coverage.processing.Operations;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GeneralGridGeometry;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.util.LRULinkedHashMap;
import org.geotools.util.Version;
import org.geotools.resources.XArray;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;

import net.seagis.catalog.Database;
import net.seagis.catalog.CatalogException;
import net.seagis.catalog.NoSuchRecordException;
import net.seagis.coverage.catalog.CoverageReference;
import net.seagis.coverage.catalog.Layer;
import net.seagis.coverage.catalog.LayerTable;
import static net.seagis.coverage.wms.WMSExceptionCode.*;


/**
 * Produces {@linkplain RenderedImage rendered images} from Web Service parameters.
 * <p>
 * <strong>This class is not thread-safe</strong>. Multi-threads application shall
 * use one instance per thread. The first instance shall be created using the
 * {@linkplain #WebServiceWorker(Database) constructor expecting a database connection},
 * and every additional instance connected to the same database shall be created using
 * the {@linkplain #WebServiceWorker(WebServiceWorker) copy constructor}. This approach
 * enables sharing of some common structures for efficienty.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Guilhem Legal
 */
public class WebServiceWorker {
    /**
     * WMS before this version needs longitude before latitude. WMS after this version don't
     * perform axis switch. WMS at this exact version switch axis only for EPSG:4326.
     */
    private static final Version AXIS_SWITCH_THRESHOLD = new Version("1.1");

    /**
     * The EPSG code for the CRS for which to switch axis in the version
     * given by {@link #AXIS_SWITCH_THRESHOLD}.
     */
    private static final Integer AXIS_SWITCH_EXCEPTION = 4326;

    /**
     * The default format, or {@code null} if we should not provides any default.
     */
    private static final String DEFAULT_FORMAT = "image/png";

    /**
     * The default file suffix when none can be inferred from the format.
     */
    private static final String DEFAULT_SUFFIX = ".raw";

    /**
     * The buffered image types to try if the native image sample model doesn't fit the current
     * {@linkplain #writer}. Those types will be tried in the order they are declared.
     */
    private static final int[] BUFFERED_TYPES = {
        BufferedImage.TYPE_INT_ARGB,
        BufferedImage.TYPE_INT_ARGB_PRE,
        BufferedImage.TYPE_INT_RGB,
        BufferedImage.TYPE_INT_BGR,
        BufferedImage.TYPE_4BYTE_ABGR,
        BufferedImage.TYPE_4BYTE_ABGR_PRE,
        BufferedImage.TYPE_3BYTE_BGR,
        BufferedImage.TYPE_USHORT_565_RGB,
        BufferedImage.TYPE_USHORT_555_RGB,
        BufferedImage.TYPE_USHORT_GRAY
    };

    /**
     * The connection to the database.
     */
    private final Database database;

    /**
     * The kind of service.
     */
    private Service service;

    /**
     * The web service version. This version number need to be interpreted together
     * with {@link #service}.
     */
    private Version version;

    /**
     * The layer name.
     */
    private String layer;

    /**
     * The envelope of current layer, including its CRS.
     */
    private GeneralEnvelope envelope;

    /**
     * The dimension of target image.
     */
    private GridRange gridRange;

    /**
     * The requested time.
     */
    private Date time;

    /**
     * The requested elevation.
     */
    private Number elevation;

    /**
     * The interpolation to use for resampling.
     */
    private Interpolation interpolation = Interpolation.getInstance(Interpolation.INTERP_BILINEAR);

    /**
     * The output format as a MIME type.
     */
    private String format;

    /**
     * List of valid formats. Will be created only when first needed.
     */
    private transient Set<String> formats;

    /**
     * The temporary file in which a file is written. Will be created when first needed,
     * overwritten everytime a new request is performed and deleted on JVM exit. Different
     * files are used for different suffix.
     */
    private transient Map<String,File> files;

    /**
     * The writer used during the last write operation, cached for more efficient reuse.
     * May be {@code null} if not yet determined or {@linkplain ImageWriter#dispose disposed}.
     * The value of this field depends on:
     * <p>
     * <ul>
     *   <li>The output format (MIME type)</li>
     *   <li>The sample and color model of the last image encoded.</li>
     * </ul>
     * <p>
     * We assume that in typical applications, every images in a given layer have the same sample
     * and color model (it doesn't need to be always true - this is just a common case).  If this
     * assumption hold, then it is safe to avoid searching for a new image writer for every write
     * operation as long as the output format and the layer do not change.
     * <p>
     * {@code ImageServiceWorker} still reasonably safe even if the above assumption do not holds,
     * because the writer is verified before every write operation  and a new one will be fetched
     * if needed. However because many writers may be able to encode the same image, the value of
     * this field depends on the history (e.g. the sample model of the last encoded image).  When
     * we have some reason to believe that the image sample model may change  (e.g. when changing
     * the layer), we will clear this field as a safety - even if not strictly necessary - in
     * order to get a more determinist behavior, i.e. reduce the dependency to history.
     */
    private transient ImageWriter writer;

    /**
     * File suffix for the current {@linkplain #writer}.
     */
    private transient String writerSuffix;

    /**
     * {@code true} if current {@linkplain #writer} accepts {@link File} outputs.
     */
    private transient boolean writerAcceptsFile;

    /**
     * {@code true} during the last image write, the current {@linkplain #writer} was unable
     * to write directly the provided image. We assume that the same apply to next images to
     * be written, as long as we don't change the format or the layer. This assumption is used
     * only in order to avoid scanning over every image writer for each write operation.
     */
    private transient boolean writerNeedsReformat;

    /**
     * The layer table. Will be created when first needed.
     */
    private transient LayerTable layerTable;

    /**
     * The most recently used layers. Different {@code WebServiceWorker} may share the same
     * instance, so every request to this map must be synchronized.
     */
    private transient final Map<LayerRequest,Layer> layers;

    /**
     * Creates a new image producer connected to the specified database.
     *
     * @param database The connection to the database.
     */
    public WebServiceWorker(final Database database) {
        this.database = database;
        layers = LRULinkedHashMap.createForRecentAccess(12);
    }

    /**
     * Creates a new image producer connected to the same database than the specified worker.
     * This constructor is used for creating many worker instance to be used in multi-threads
     * application.
     */
    public WebServiceWorker(final WebServiceWorker worker) {
        database = worker.database;
        layers   = worker.layers;
    }

    /**
     * Sets the version of the web service. This method should be invoked before any other
     * setters in this class, since it may affect the parsing of strings.
     *
     * @param  service The kind of service.
     * @param  version The WMS version, or {@code null} if unknown.
     *         If null, latest version is assumed.
     * @throws WebServiceException if the version string can't be parsed
     */
    public void setService(final String service, final String version) throws WebServiceException {
        this.service = (service != null) ? Service.valueOf(service.trim().toUpperCase()) : null;
        this.version = (version != null) ? new Version(version) : null;
    }

    /**
     * Sets the layer of interest.
     *
     * @param  layer The layer, or {@code null} if unknown.
     * @throws WebServiceException if the layer is not recognize.
     */
    public void setLayer(String layer) throws WebServiceException {
        if (layer != null) {
            this.layer = null;
        } else {
            layer = layer.trim();
            if (!layer.equals(this.layer)) {
                this.layer = layer;
                /*
                 * Changing the layer do not change the format, so it should not be strictly
                 * necessary to dispose the previous image writer (if any). However we clear
                 * the writer as a safety in order to get more determinist behavior. See the
                 * writer field javadoc for details.
                 */
                disposeWriter();
            }
        }
    }

    /**
     * Sets the coordinate reference system from a code. Invoking this method will erase
     * any bounding box that may have been previously set.
     *
     * @param  code The coordinate reference system code, or {@code null} if unknown.
     * @throws WebServiceException if no CRS object can be built from the given code.
     */
    public void setCoordinateReferenceSystem(final String code) throws WebServiceException {
        if (code == null) {
            envelope = null;
            return;
        }
        final int versionThreshold;
        if (Service.WMS.equals(service) && version != null) {
            versionThreshold = version.compareTo(AXIS_SWITCH_THRESHOLD);
        } else {
            versionThreshold = 1;
        }
        CoordinateReferenceSystem crs;
        try {
            crs = CRS.decode(code, versionThreshold < 0);
            if (versionThreshold == 0 && AXIS_SWITCH_EXCEPTION.equals(CRS.lookupEpsgCode(crs, false))) {
                crs = DefaultGeographicCRS.WGS84;
            }
        } catch (FactoryException exception) {
            throw new WebServiceException(Errors.format(ErrorKeys.ILLEGAL_COORDINATE_REFERENCE_SYSTEM),
                    exception, INVALID_CRS, version);
        }
        envelope = new GeneralEnvelope(crs);
        envelope.setToInfinite();
    }

    /**
     * Sets the bounding box.
     *
     * @param  The bounding box, or {@code null} if unknown.
     * @throws WebServiceException if the given bounding box can't be parsed.
     */
    public void setBoundingBox(final String bbox) throws WebServiceException {
        if (bbox == null) {
            if (envelope != null) {
                envelope.setToInfinite();
            }
            return;
        }
        final StringTokenizer tokens = new StringTokenizer(bbox, ",;");
        if (envelope == null) {
            envelope = new GeneralEnvelope((tokens.countTokens() + 1) >> 1);
        }
        int dimension = 0;
        while (tokens.hasMoreTokens()) {
            final double minimum, maximum;
            String token = tokens.nextToken().trim();
            try {
                minimum = Double.parseDouble(token);
                if (tokens.hasMoreTokens()) {
                    token = tokens.nextToken().trim();
                    maximum = Double.parseDouble(token);
                } else {
                    maximum = minimum;
                }
            } catch (NumberFormatException exception) {
                throw new WebServiceException(Errors.format(ErrorKeys.NOT_A_NUMBER_$1, token),
                        exception, INVALID_PARAMETER_VALUE, version);
            }
            try {
                envelope.setRange(dimension++, minimum, maximum);
            } catch (IndexOutOfBoundsException exception) {
                throw new WebServiceException(Errors.format(ErrorKeys.MISMATCHED_DIMENSION_$3, "envelope",
                        dimension + ((tokens.countTokens() + 1) >> 1), envelope.getDimension()),
                        exception, INVALID_DIMENSION_VALUE, version);
            }
        }
    }

    /**
     * Sets the dimension, or {@code null} if unknown. If a value is null, the other
     * one must be null as well otherwise a {@link WebServiceException} is thrown.
     *
     * @param  width  The image width.
     * @param  height The image height.
     * @throws WebServiceException if the dimension can't be parsed from the given strings.
     */
    public void setDimension(final String width, final String height) throws WebServiceException {
        if (width == null && height == null) {
            gridRange = null;
            return;
        }
        final int[] upper = new int[2];
        String name=null, value=null;
        try {
            name = "width";  upper[0] = Integer.parseInt(value = width .trim());
            name = "height"; upper[1] = Integer.parseInt(value = height.trim());
        } catch (NullPointerException exception) {
            throw new WebServiceException(Errors.format(ErrorKeys.MISSING_PARAMETER_VALUE_$1, name),
                    exception, MISSING_PARAMETER_VALUE, version);
        } catch (NumberFormatException exception) {
            throw new WebServiceException(Errors.format(ErrorKeys.NOT_AN_INTEGER_$1, value),
                    exception, INVALID_PARAMETER_VALUE, version);
        }
        gridRange = new GeneralGridRange(new int[upper.length], upper);
    }
    
    /**
     * Sets the time, or {@code null} if unknown.
     *
     * @param  elevation The elevation.
     * @throws WebServiceException if the elevation can't be parsed from the given string.
     */
    public void setTime(String date) throws WebServiceException {
        if (date == null) {
            time = null;
            return;
        }
        date = date.trim();
        
    }

    /**
     * Sets the elevation, or {@code null} if unknown.
     *
     * @param  elevation The elevation.
     * @throws WebServiceException if the elevation can't be parsed from the given string.
     */
    public void setElevation(String elevation) throws WebServiceException {
        if (elevation == null) {
            this.elevation = null;
        } else try {
            elevation = elevation.trim();
            this.elevation = Double.parseDouble(elevation);
        } catch (NumberFormatException exception) {
            throw new WebServiceException(Errors.format(ErrorKeys.NOT_A_NUMBER_$1, elevation),
                    exception, INVALID_PARAMETER_VALUE, version);
        }
    }

    /**
     * Sets the output format as a MIME type.
     *
     * @param  format The output format.
     * @throws WebServiceException if the format is invalid.
     */
    public void setFormat(String format) throws WebServiceException {
        if (format == null) {
            this.format = null;
        } else {
            format = format.trim();
            if (!format.equals(this.format)) {
                if (formats == null) {
                    formats = new HashSet<String>(Arrays.asList(ImageIO.getWriterMIMETypes()));
                }
                if (!formats.contains(format)) {
                    throw new WebServiceException(Errors.format(ErrorKeys.ILLEGAL_ARGUMENT_$2,
                            "format", format), LAYER_NOT_QUERYABLE, version);
                }
                this.format = format;
                /*
                 * Changing the format will change the image writer, so
                 * we need to dispose the previous one if there is any.
                 */
                disposeWriter();
            }
        }
    }

    /**
     * Returns the layer for the current configuration.
     *
     * @throws WebServiceException if an error occured while fetching the table.
     */
    private Layer getLayer() throws WebServiceException {
        if (layer == null) {
            throw new WebServiceException(Errors.format(ErrorKeys.MISSING_PARAMETER_VALUE_$1, "layer"),
                    LAYER_NOT_DEFINED, version);
        }
        final LayerRequest request = new LayerRequest(layer, envelope, null);
        Layer candidate;
        synchronized (layers) {
            candidate = layers.get(request);
            if (candidate == null) {
                try {
                    if (layerTable == null) {
                        layerTable = new LayerTable(database.getTable(LayerTable.class));
                    }
                    candidate = layerTable.getEntry(layer);
                } catch (NoSuchRecordException exception) {
                    throw new WebServiceException(exception, LAYER_NOT_DEFINED, version);
                } catch (CatalogException exception) {
                    throw new WebServiceException(exception, LAYER_NOT_QUERYABLE, version);
                } catch (SQLException exception) {
                    throw new WebServiceException(exception, LAYER_NOT_QUERYABLE, version);
                }
                layers.put(request, candidate);
            }
        }
        return candidate;
    }

    /**
     * Gets the grid coverage for the current layer, time, elevation, <cite>etc.</cite>
     *
     * @param  resample {@code true} for resampling the coverage to the specified envelope
     *         and dimension, or {@code false} for getting the coverage as in the database.
     * @throws WebServiceException if an error occured while querying the coverage.
     */
    public GridCoverage2D getGridCoverage2D(final boolean resample) throws WebServiceException {
        final Layer layer = getLayer();
        final CoverageReference ref;
        try {
            time = new Date(86,0,1);
            ref = layer.getCoverageReference(time, elevation);
        } catch (CatalogException exception) {
            throw new WebServiceException(exception, LAYER_NOT_QUERYABLE, version);
        }
        GridCoverage2D coverage;
        try {
            coverage = ref.getCoverage(null);
        } catch (IOException exception) {
            throw new WebServiceException(Errors.format(ErrorKeys.CANT_READ_$1, ref.getFile()),
                    exception, LAYER_NOT_QUERYABLE, version);
        }
        if (resample && envelope != null) {
            final Operations op = Operations.DEFAULT;
            if (gridRange != null) {
                final GridGeometry gridGeometry;
                final CoordinateReferenceSystem crs = envelope.getCoordinateReferenceSystem();
                if (envelope.isInfinite()) {
                    gridGeometry = new GeneralGridGeometry(gridRange, null, crs);
                } else {
                    gridGeometry = new GeneralGridGeometry(gridRange, envelope);
                }
                coverage = (GridCoverage2D) op.resample(coverage, crs, gridGeometry, interpolation);
            } else {
                coverage = (GridCoverage2D) op.resample(coverage, envelope, interpolation);
            }
        }
        return coverage;
    }

    /**
     * Gets the image for the {@linkplain #getGridCoverage2D current coverage}.
     * The image is resized to the requested dimension and CRS.
     *
     * @throws WebServiceException if an error occured while querying the coverage.
     */
    public RenderedImage getRenderedImage() throws WebServiceException {
        return getGridCoverage2D(true).geophysics(false).getRenderedImage();
    }

    /**
     * Returns the format as a mime type.
     *
     * @throws WebServiceException if the format is not defined.
     */
    public String getMimeType() throws WebServiceException {
        if (format != null) {
            return format;
        } else if (DEFAULT_FORMAT != null) {
            return DEFAULT_FORMAT;
        } else {
            throw new WebServiceException(Errors.format(ErrorKeys.MISSING_PARAMETER_$1, "format"),
                    MISSING_PARAMETER_VALUE, version);
        }
    }

    /**
     * Returns the {@linkplain #getRenderedImage current rendered image} as a file in the
     * {@linkplain #getMimeType current format}. The file is created in the temporary
     * directory, may be overwritten during the next invocation of this method and will
     * be deleted at JVM exit.
     *
     * @throws WebServiceException if an error occured while processing the image.
     *
     * @todo As an optimization, returns the current file if it still valid.
     */
    public File getImageFile() throws WebServiceException {
        final RenderedImage image = getRenderedImage();
        RenderedImage formated = image;
        try {
            /*
             * Try the last used writer, if there is any. If this writer needs to reformat the
             * data in order to processed (for example PNG needs to convert 16 bits indexed to
             * RGB or 8 bits indexed), then we will reformat before to try an other writer.
             */
            if (writer != null) {
                File file = write(image);
                if (file != null) {
                    return file;
                }
                if (writerNeedsReformat) {
                    formated = reformat(image);
                    if (formated != image) {
                        file = write(formated);
                        if (file != null) {
                            return file;
                        }
                    }
                }
            }
            /*
             * The last used writer was not suitable or not available anymore. Search a new one.
             * If it doesn't work, reformat the image and try again. If it then work, we will
             * remember that we need to reformat the image in order to get that writer to work.
             */
            final String format = getMimeType();
            File file = write(image, format);
            if (file != null) {
                return file;
            }
            if (formated == image) {
                formated = reformat(image);
                // May still the same image, so we need to compare again.
            }
            if (formated != image) {
                file = write(formated, format);
                if (file != null) {
                    writerNeedsReformat = true;
                    return file;
                }
            }
        } catch (IOException exception) {
            disposeWriter();
            throw new WebServiceException(exception, LAYER_NOT_QUERYABLE, version);
        }
        disposeWriter();
        throw new WebServiceException(Errors.format(ErrorKeys.NO_IMAGE_WRITER), LAYER_NOT_QUERYABLE, version);
    }

    /**
     * Reformats the given image to a type appropriate for the current {@linkplain #writer}.
     * If this method can't reformat, then the image is returned unchanged.
     */
    private RenderedImage reformat(final RenderedImage image) {
        final ImageWriterSpi spi = writer.getOriginatingProvider();
        if (spi != null) {
            for (int i=0; i<BUFFERED_TYPES.length; i++) {
                final ImageTypeSpecifier type =
                        ImageTypeSpecifier.createFromBufferedImageType(BUFFERED_TYPES[i]);
                if (spi.canEncodeImage(type)) {
                    final BufferedImage buffered = type.createBufferedImage(image.getWidth(), image.getHeight());
                    final Graphics2D graphics = buffered.createGraphics();
                    graphics.drawRenderedImage(image, new AffineTransform());
                    graphics.dispose();
                    return buffered;
                }
            }
        }
        return image;
    }

    /**
     * Attempts to write the given image using a writer of the given format.
     * The current {@linkplain #writer}, if any, is disposed.
     *
     * @param  image  The image to write.
     * @param  format The format for the writer to use.
     * @return The file, or {@code null} if no writer is suitable.
     * @throws IOException if an error occured while processing the image.
     */
    private File write(final RenderedImage image, final String format) throws IOException {
        for (final Iterator<ImageWriter> it=ImageIO.getImageWritersByMIMEType(format); it.hasNext();) {
            disposeWriter();
            writer = it.next();
            final File file = write(image);
            if (file != null) {
                return file;
            }
        }
        return null;
    }

    /**
     * Attempts to write the given image using the current {@linkplain #writer writer}.
     *
     * @param  image The image to write.
     * @return The file, or {@code null} if the current writer is not suitable.
     * @throws IOException if an error occured while processing the image.
     */
    private File write(final RenderedImage image) throws IOException {
        final ImageWriterSpi spi = writer.getOriginatingProvider();
        if (spi != null && !spi.canEncodeImage(image)) {
            return null; // Can not encode the image.
        }
        /*
         * If the suffix and output types are not yet determined for the current writer,
         * get them now.
         */
        if (writerSuffix == null) {
            if (spi == null) {
                writerSuffix = DEFAULT_SUFFIX;
                writerAcceptsFile = false;
            } else {
                final String[] candidates = spi.getFileSuffixes();
                if (candidates != null && candidates.length != 0) {
                    writerSuffix = candidates[0];
                } else {
                    writerSuffix = DEFAULT_SUFFIX;
                }
                writerAcceptsFile = XArray.contains(spi.getOutputTypes(), File.class);
            }
        }
        /*
         * Gets a temporary file, different for every WebServiceWorker instance (so
         * different for every thread if the rule given in the javadoc is respected).
         * This file will be overwritten for every invocation of this method, in order
         * to avoid a multiplication of temporary file. It will be deleted on JVM exit.
         */
        if (files == null) {
            files = new HashMap<String,File>();
        }
        File file = files.get(writerSuffix);
        if (file == null) {
            file = File.createTempFile("WCS", writerSuffix);
            file.deleteOnExit();
            files.put(writerSuffix, file);
        }
        /*
         * If the image writer accepts directly file output, uses it. Otherwise we
         * will create an image output stream, which should be accepted by all writers
         * according Image I/O specification.
         */
        if (writerAcceptsFile) {
            writer.setOutput(file);
            writer.write(image);
        } else {
            final ImageOutputStream stream = new MemoryCacheImageOutputStream(new FileOutputStream(file));
            try {
                writer.setOutput(stream);
                writer.write(image);
            } finally {
                stream.close();
            }
        }
        return file;
    }

    /**
     * Disposes the {@linkplain #writer} and information derived from the writer.
     * Other fields must be left untouched.
     */
    private void disposeWriter() {
        writerSuffix        = null;
        writerAcceptsFile   = false;
        writerNeedsReformat = false;
        if (writer != null) {
            writer.dispose();
            writer = null;
        }
    }

    /**
     * Disposes any resources held by this worker. This method should be invoked
     * when waiting for the garbage collector would be over-conservative.
     */
    public void dispose() {
        disposeWriter();
    }
}
