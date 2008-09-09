/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.coverage.web;

import java.awt.Color;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.SQLException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.DataBuffer;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.awt.image.RenderedImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.media.jai.operator.DivideByConstDescriptor;
import javax.media.jai.Interpolation;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.opengis.coverage.Coverage;
import org.opengis.coverage.grid.GridEnvelope;
import org.opengis.coverage.grid.GridGeometry;
import org.opengis.coverage.CannotEvaluateException;
import org.opengis.coverage.PointOutsideCoverageException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.geometry.DirectPosition;

import org.geotools.coverage.processing.ColorMap;
import org.geotools.coverage.processing.Operations;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.ViewType;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GeneralGridEnvelope;
import org.geotools.coverage.grid.GeneralGridGeometry;
import org.geotools.coverage.SpatioTemporalCoverage3D;
import org.geotools.referencing.CRS;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.util.NumberRange;
import org.geotools.util.MeasurementRange;
import org.geotools.util.LRULinkedHashMap;
import org.geotools.util.logging.Logging;
import org.geotools.resources.XArray;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.VocabularyKeys;
import org.geotools.resources.image.ImageUtilities;
import org.geotools.resources.coverage.CoverageUtilities;

import org.constellation.catalog.Database;
import org.constellation.catalog.CatalogException;
import org.constellation.catalog.NoSuchRecordException;
import org.constellation.coverage.catalog.CoverageReference;
import org.constellation.coverage.catalog.Layer;
import org.constellation.coverage.catalog.LayerTable;
import org.constellation.resources.i18n.ResourceKeys;
import org.constellation.resources.i18n.Resources;
import static org.constellation.coverage.wms.WMSExceptionCode.*;


/**
 * Produces {@linkplain RenderedImage rendered images} from parameters. The parameters are the
 * protected fields, which must be assigned by subclasses.
 * <p>
 * <strong>This class is not thread-safe</strong>. Multi-threads application shall
 * use one instance per thread. The first instance shall be created using the
 * {@linkplain #ImageProducer(Database) constructor expecting a database connection},
 * and every additional instance connected to the same database shall be created using
 * the {@linkplain #ImageProducer(ImageProducer) copy constructor}. This approach
 * enables sharing of some common structures for efficienty.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Guilhem Legal
 * @author Sam Hiatt
 *
 * @todo Some table-related fields in this class, together with some caches, should move in a
 *       more global class and be shared for every instances connected to the same database.
 */
public abstract class ImageProducer {
    /**
     * Enable or disable some JAI codecs.
     * The codecs provided in standard Java are sometime more reliable.
     */
    static {
        if (false) {
            Logging.ALL.forceMonolineConsoleOutput(Level.CONFIG);
        }
        ImageUtilities.allowNativeCodec("png", ImageReaderSpi.class, false);
        ImageUtilities.allowNativeCodec("png", ImageWriterSpi.class, false);
    }

    /**
     * A logger for every {@link ImageProducer} instances.
     */
    static final Logger LOGGER = Logger.getLogger("org.constellation.coverage.web");

    /**
     * The default format, or {@code null} if we should not provides any default.
     */
    private static final String DEFAULT_FORMAT = "image/png";

    /**
     * The default file suffix when none can be inferred from the format.
     */
    private static final String DEFAULT_SUFFIX = ".raw";

    /**
     * The default interpolation to use.
     */
    static final int DEFAULT_INTERPOLATION = Interpolation.INTERP_NEAREST;

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
     * The web service version.
     */
    protected ServiceVersion version;

    /**
     * The layer name.
     */
    protected String layer;

    /**
     * The grid geometry. This field is computed automatically from other user-supplied values
     * like {@link #gridCRS}, {@link #gridRange} and {@link #envelope}.
     *
     * @see #getGridGeometry
     */
    private transient GeneralGridGeometry gridGeometry;

    /**
     * The envelope of current layer, including its CRS.
     */
    protected GeneralEnvelope envelope;

    /**
     * The CRS of the query, or {@code null} if not specified. In theory it should be the same one
     * than the {@linkplain #envelope} CRS. In practice they sometime have different axis order and
     * {@code TOWGS84} element because of difference between the EPSG database and the PostGIS
     * {@code "spatial_ref_sys"} table. The envelope CRS should be the one specified in the EPSG
     * database, while {@code queryCRS} is the same CRS as specified in the PostGIS table.
     * <p>
     * Be aware that {@code queryCRS} may have wrong axis order. The envelope CRS is the
     * authoritative one. This {@code queryCRS} may be understood as the CRS to use for
     * submitting the query to the PostGrid database.
     */
    protected CoordinateReferenceSystem queryCRS;

    /**
     * The CRS of the response, or {@code null} if not specified.
     */
    protected CoordinateReferenceSystem responseCRS;

    /**
     * The dimension of target image.
     */
    protected GridEnvelope gridRange;

    /**
     * The <cite>grid to CRS</cite> transform specified by the user.
     */
    protected MathTransform gridToCRS;

    /**
     * The range on value on which to apply a color ramp.
     */
    protected NumberRange<Double> colormapRange;

    /**
     * The requested times. Typically contains a singleton, but could also contains more
     * values for WCS requests that span the time dimension.
     */
    protected final List<Date> times = new ArrayList<Date>();

    /**
     * The requested elevation.
     */
    protected Number elevation;

    /**
     * The interpolation to use for resampling.
     */
    protected Interpolation interpolation = Interpolation.getInstance(DEFAULT_INTERPOLATION);

    /**
     * The exceptions format as a MIME type.
     */
    protected String exceptionFormat;

    /**
     * The output format as a MIME type.
     */
    protected String format;

    /**
     * The background color of the current image (default {@code 0xFFFFFF}).
     */
    protected Color background = Color.WHITE;

    /**
     * A flag specifying if the image have to handle transparency.
     */
    protected boolean transparent;

    /**
     * {@code true} if {@linkplain IndexColorModel index color model} are allowed to
     * store sample value with more than 8 bits. The default value is {@code false}.
     */
    boolean indexedShortAllowed;

    /**
     * The temporary file in which an image is written. Will be created when first needed,
     * overwritten everytime a new request is performed and deleted on JVM exit. Different
     * files are used for different suffix.
     */
    private transient ImageRequestMap files;

    /**
     * The directory for temporary files. Will be computed when first needed.
     */
    private transient File temporaryDirectory;

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
     * The global layer table, <strong>without</strong> any bounding box or time range setting.
     * Will be created when first needed. Will be shared by every workers using the same
     * {@linkplain #database} instance.
     */
    private transient LayerTable globalLayerTable;

    /**
     * The layer names in an unmodifiable set. Will be created when first needed.
     */
    private transient Set<String> layerNames;

    /**
     * The most recently used layers. Different {@code WebServiceWorker} may share the same
     * instance, so every request to this map must be synchronized.
     */
    private transient final Map<LayerRequest,Layer> layers;

    /**
     * The mime types for each format name. Created when first needed.
     */
    private transient Map<String,String> mimeTypes;

    /**
     * The current coordinate of the point requested by a {@code getFeatureInfo} request.
     */
    private DirectPosition coordinate;

    /**
     * A manager allowing to make some operation remotly with JConsole.
     */
    private final WebServiceManager manager;

    /**
     * Creates a new image producer connected to the specified database.
     *
     * @param database The connection to the database.
     * @param jmx {@code true} for enabling JMX management, or {@code false} otherwise.
     */
    public ImageProducer(final Database database, final boolean jmx) {
        this.database = database;
        this.layers   = LRULinkedHashMap.createForRecentAccess(12);
        this.manager  = new WebServiceManager(jmx);
        manager.addWorker(this);
    }

    /**
     * Creates a new image producer connected to the same database than the specified worker.
     * This constructor is used for creating many worker instance to be used in multi-threads
     * application.
     *
     * @param worker The worker to use as a template.
     */
    public ImageProducer(final ImageProducer worker) {
        database = worker.database;
        layers   = worker.layers;
        manager  = worker.manager;
        manager.addWorker(this);
    }

    /**
     * Determines if the request is for a time-series by checking for the size of {@link #times}.
     *
     * @return {@code true} if {@code times.size()} is > 1, else return {@code false}.
     */
    public boolean isTimeseriesRequest() {
        return times.size() > 1;
    }

    /**
     * Executes a request for a time-series
     * Currently evaluates just one point at a corner of the envelope.
     *
     * @return An array containing the extracted time-series.
     * @throws WebServiceException if an error occured while querying the database.
     *
     * @todo Return a time-series of the average value in the envelope?
     */
    public double[] getTimeseries() throws WebServiceException {
        final Layer layer = getLayer();
        final Coverage coverage;
        try {
            coverage = layer.getCoverage();
        } catch (CatalogException exception) {
            throw new WMSWebServiceException(exception, LAYER_NOT_QUERYABLE, version);
        }
        SpatioTemporalCoverage3D stCoverage = new SpatioTemporalCoverage3D(layer.getName(), coverage);
        Point2D point = new Point2D.Double(envelope.getMinimum(0), envelope.getMinimum(1));
        double[] res = null;
        final double[] values = new double[times.size()];
        for (int i=0; i<values.length; i++) {
            final Date t = times.get(i);
            try {
                res = stCoverage.evaluate(point, t, res);
            } catch (CannotEvaluateException e) {
                res[0] = Float.NaN;
                System.out.println("Error evaluating pixel.  Returning NaN for this pixel.  "+e.getLocalizedMessage());
            } catch (RuntimeException e) {
                res[0] = Float.NaN;
                System.out.println("Error reading file.  Returning NaN for this pixel.  "+e.getLocalizedMessage());
            }
            values[i] = res[0];
        }
        return values;
    }

    /**
     * Dumps the resulting timeseries to an XML file.
     *
     * @return XML file containing generated time-series.
     * @throws WebServiceException if an error occured while querying the database.
     *
     * @todo Write to a standard schem for representing time-series.
     *
     * @todo Current implementation creates a new temporary files everytime it is invoked.
     *       We should try to use some pool instead, like what we do for images.
     *
     * @todo The XML formatting part should be delagated to an other class, something
     *       like a {@code XMLWriter}.
     */
    public File getTimeseriesAsXML() throws WebServiceException {
        final double[] ts = getTimeseries();
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            final DocumentBuilder builder = dbf.newDocumentBuilder();
            final Document doc = builder.newDocument();
            final Element element = doc.createElement("timeseries");
            doc.appendChild(element);
            for (double val : ts) {
                Element e = doc.createElement(layer);
                e.appendChild(doc.createTextNode(Double.toString(val)));
                element.appendChild(e);
            }
            final TransformerFactory tf = TransformerFactory.newInstance();
            final Transformer transformer = tf.newTransformer();
            final DOMSource source = new DOMSource(doc);
            final String directory = System.getProperty("java.io.tmpdir");
            if (directory != null) {
                temporaryDirectory = new File(directory, "constellation");
                if (!temporaryDirectory.isDirectory() && !temporaryDirectory.mkdir()) {
                    temporaryDirectory = null; // Fallback on system default.
                }
            }
            File file = File.createTempFile("testout", "xml", temporaryDirectory);
            file.deleteOnExit();
            StreamResult result = new StreamResult(file);
            transformer.transform(source, result);
            return file;
        } catch (TransformerException exception) {
            throw new WMSWebServiceException(exception, LAYER_NOT_QUERYABLE, version);
        } catch (IOException exception) {
            throw new WMSWebServiceException(exception, LAYER_NOT_QUERYABLE, version);
        } catch (ParserConfigurationException exception) {
            throw new WMSWebServiceException(exception, LAYER_NOT_QUERYABLE, version);
        }
    }

    /**
     * Returns the output MIME type for exceptions.
     *
     * @return The output MIMI type for exceptions.
     */
    public String getExceptionFormat() {
        if (exceptionFormat == null) {
            return "application/vnd.ogc.se_xml";
        } else {
            return exceptionFormat;
        }
    }

    /**
     * Returns the layer table.
     *
     * @param  global {@code true} for the global (<strong>unmodifiable</strong>) layer
     *         table, or {@code false} for the specific table that we can modify.
     * @return The layer table.
     * @throws WebServiceException if the layer table can not be created.
     */
    protected LayerTable getLayerTable(final boolean global) throws WebServiceException {
        if (globalLayerTable == null) try {
            globalLayerTable = database.getTable(LayerTable.class);
        } catch (CatalogException exception) {
            throw new WMSWebServiceException(exception, NO_APPLICABLE_CODE, version);
        }
        if (global) {
            return globalLayerTable;
        }
        if (layerTable == null) {
            layerTable = new LayerTable(globalLayerTable);
        }
        Service service = (version != null) ? version.getService() : Service.WCS;
        layerTable.setService(service);
        return layerTable;
    }

    /**
     * Returns only the name of all available layers. This method is much cheaper than
     * {@link #getLayers} when only the names are wanted.
     *
     * @return The layer names, or an empty set if none.
     * @throws WebServiceException if an error occured while fetching the table.
     */
    public Set<String> getLayerNames() throws WebServiceException {
        if (layerNames == null) try {
            final LayerTable table = getLayerTable(true);
            layerNames = Collections.unmodifiableSet(table.getIdentifiers());
        } catch (CatalogException exception) {
            throw new WMSWebServiceException(exception, NO_APPLICABLE_CODE, version);
        } catch (SQLException exception) {
            throw new WMSWebServiceException(exception, NO_APPLICABLE_CODE, version);
        }
        return layerNames;
    }

    /**
     * Parses a list of layer names and return the specified layers. The given string shall
     * be a comma or semi-colon separated list of name returned by {@link #getLayerNames}.
     *
     * @param  layerNames a list of layer names separated by comma or semi-colon.
     * @return The layers, or an empty list if none.
     * @throws WebServiceException if an error occured while fetching the table.
     */
    public List<Layer> getLayers(final String layerNames) throws WebServiceException {
        final LayerTable table = getLayerTable(true);
        final List<Layer> layers = new ArrayList<Layer>();
        final StringTokenizer tokens = new StringTokenizer(layerNames, ",;");
        while (tokens.hasMoreTokens()) {
            final String token = tokens.nextToken().trim();
            final Layer layer;
            try {
                layer = table.getEntry(token);
            } catch (NoSuchRecordException exception) {
                throw new WMSWebServiceException(exception, LAYER_NOT_DEFINED, version);
            } catch (CatalogException exception) {
                throw new WMSWebServiceException(exception, LAYER_NOT_QUERYABLE, version);
            } catch (SQLException exception) {
                throw new WMSWebServiceException(exception, LAYER_NOT_QUERYABLE, version);
            }
            if (layer.getSeries().isEmpty() || !layer.isQueryable(version.getService())) {
                throw new WMSWebServiceException(Resources.format(ResourceKeys.NO_DATA_TO_DISPLAY),
                        LAYER_NOT_DEFINED, version);
            }
            layers.add(layer);
        }
        return layers;
    }

    /**
     * Returns the specified layers.
     *
     * @param  layerNames a list of layer names.
     * @return The layers, or an empty list if none.
     * @throws WebServiceException if an error occured while fetching the table.
     */
    public List<Layer> getLayers(final Collection<String> layerNames) throws WebServiceException {
        final LayerTable table = getLayerTable(true);
        final List<Layer> layers = new ArrayList<Layer>(layerNames.size());
        for (final String layerName : layerNames) {
            final Layer layer;
            try {
                layer = table.getEntry(layerName);
            } catch (NoSuchRecordException exception) {
                throw new WMSWebServiceException(exception, LAYER_NOT_DEFINED, version);
            } catch (CatalogException exception) {
                throw new WMSWebServiceException(exception, LAYER_NOT_QUERYABLE, version);
            } catch (SQLException exception) {
                throw new WMSWebServiceException(exception, LAYER_NOT_QUERYABLE, version);
            }
            if (layer == null || layer.getSeries().isEmpty() ||
                    (version != null && !layer.isQueryable(version.getService())))
            {
                throw new WMSWebServiceException(Resources.format(ResourceKeys.NO_DATA_TO_DISPLAY),
                        LAYER_NOT_DEFINED, version);
            }
            layers.add(layer);
        }
        return layers;
    }

    /**
     * Returns all available layers.
     *
     * @return The layers, or an empty list if none.
     * @throws WebServiceException if an error occured while fetching the table.
     */
    public Set<Layer> getLayers() throws WebServiceException {
        try {
            final LayerTable table = getLayerTable(true);
            return table.getEntries();
        } catch (CatalogException exception) {
            throw new WMSWebServiceException(exception, NO_APPLICABLE_CODE, version);
        } catch (SQLException exception) {
            throw new WMSWebServiceException(exception, NO_APPLICABLE_CODE, version);
        }
    }

    /**
     * Returns the layer for the current configuration.
     *
     * @return The current layer.
     * @throws WebServiceException if an error occured while fetching the table.
     */
    public Layer getLayer() throws WebServiceException {
        if (layer == null) {
            throw new WMSWebServiceException(Errors.format(ErrorKeys.MISSING_PARAMETER_VALUE_$1, "layer"),
                    LAYER_NOT_DEFINED, version);
        }
        Layer candidate;
        boolean change = false;
        try {
            LayerTable table = getLayerTable(true);
            final Layer entry = table.getEntry(layer);
            if (entry.getSeries().isEmpty() || !entry.isQueryable(version.getService())) {
                throw new WMSWebServiceException(Resources.format(ResourceKeys.NO_DATA_TO_DISPLAY),
                        LAYER_NOT_DEFINED, version);
            }
            GeneralEnvelope queryEnvelope = envelope;
            if (queryCRS != null) try {
                // Make sure we are using the same CRS than the one declared in the PostGIS database.
                // It may be slightly different in TOWGS84 elements and axis order.
                queryEnvelope = (GeneralEnvelope) CRS.transform(queryEnvelope, queryCRS);
                queryEnvelope.setCoordinateReferenceSystem(queryCRS);
            } catch (TransformException e) {
                // Keep the previous queryEnvelope. It will work, maybe with reduced accuracy.
                Logging.recoverableException(ImageProducer.class, "getLayer", e);
            }
            final LayerRequest request = new LayerRequest(entry,
                    table.getCoordinateReferenceSystem(), queryEnvelope, gridRange);
            synchronized (layers) {
                candidate = layers.get(request);
                if (candidate == null) {
                    table = getLayerTable(false);
                    change |= table.setGeographicBoundingBox(request.bbox);
                    change |= table.setPreferredResolution(request.resolution);
                    if (!times.isEmpty()) {
                        change |= table.setTimeRange(Collections.min(times),
                                                     Collections.max(times));
                    }
                    candidate = table.getEntry(layer);
                    layers.put(request, candidate);
                }
            }
        } catch (NoSuchRecordException exception) {
            throw new WMSWebServiceException(exception, LAYER_NOT_DEFINED, version);
        } catch (CatalogException exception) {
            throw new WMSWebServiceException(exception, LAYER_NOT_QUERYABLE, version);
        } catch (SQLException exception) {
            throw new WMSWebServiceException(exception, LAYER_NOT_QUERYABLE, version);
        }
        if (change) {
            LOGGER.fine("LayerTable configuration changed.");
        }
        return candidate;
    }

    /**
     * Returns a single time from the {@linkplain #times} list, or {@code null} if none.
     * If there is more than one time, select the last one on the basis that it is typically
     * the most recent one.
     */
    private Date getTime() {
        return times.isEmpty() ? null : times.get(times.size() - 1);
    }

    /**
     * Returns the CRS from the {@code "spatial_ref_sys"} table for the given code.
     * This method does <strong>not</strong> look in other CRS databases like what
     * {@link org.geotools.referencing.CRS#decode(String)} does.
     *
     * @param  code The CRS code to look for.
     * @return The coordinate reference system for the given code.
     * @throws WebServiceException if an error occured while querying the database.
     * @throws FactoryException if no CRS can be created for the given code.
     */
    public CoordinateReferenceSystem getSpatialReferenceSystem(final String code)
            throws WebServiceException, FactoryException
    {
        try {
            return getLayerTable(true).getSpatialReferenceSystem(code);
        } catch (CatalogException exception) {
            throw new WMSWebServiceException(exception, NO_APPLICABLE_CODE, version);
        } catch (SQLException exception) {
            throw new WMSWebServiceException(exception, NO_APPLICABLE_CODE, version);
        }
    }

    /**
     * Returns the coordinate reference system, or {@code null} if unknown.
     *
     * @return The current CRS for queries, or {@code null}.
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return (envelope != null) ? envelope.getCoordinateReferenceSystem() : null;
    }

    /**
     * Returns the response CRS, or {@code null} if unknown.
     *
     * @return The current CRS for responses, or {@code null}.
     */
    public CoordinateReferenceSystem getResponseCRS() {
        return (responseCRS != null) ? responseCRS : getCoordinateReferenceSystem();
    }

    /**
     * Computes the grid geometry. Returns {@code null} if the geometry can not be computed.
     * The geometry CRS is the one returned by {@link #getCoordinateReferenceSystem()},
     * not the {@linkplain #getResponseCRS response CRS}.
     *
     * @return The grid geometry, or {@code null}.
     * @throws WebServiceException if an error occured while querying the layer.
     */
    public GeneralGridGeometry getGridGeometry() throws WebServiceException {
        if (gridGeometry == null) {
            if (gridToCRS != null) {
                if (envelope == null || envelope.isInfinite()) {
                    final CoordinateReferenceSystem crs = getCoordinateReferenceSystem();
                    gridGeometry = new GeneralGridGeometry(gridRange, gridToCRS, crs);
                } else {
                    gridGeometry = new GeneralGridGeometry(PixelInCell.CELL_CENTER, gridToCRS, envelope);
                }
            } else {
                GeneralEnvelope envelope = this.envelope;
                GridEnvelope gridRange = this.gridRange;
                if (envelope == null || gridRange == null) {
                    final Layer layer = getLayer();
                    try {
                        if (gridRange == null) {
                            final Rectangle bounds = layer.getTypicalBounds();
                            if (bounds != null) {
                                gridRange = new GeneralGridEnvelope(bounds, 2);
                            }
                        }
                        if (envelope == null) {
                            final GeographicBoundingBox box = layer.getGeographicBoundingBox();
                            if (box != null) {
                                envelope = new GeneralEnvelope(box);
                            }
                        }
                    } catch (CatalogException exception) {
                        throw new WMSWebServiceException(exception, NO_APPLICABLE_CODE, version);
                    }
                }
                // We know that gridToCRS is null, but we try to select constructors that accept
                // null arguments. If we are wrong, an IllegalArgumentException will be thrown.
                if (envelope == null || envelope.isInfinite()) {
                    gridGeometry = new GeneralGridGeometry(gridRange, gridToCRS, getCoordinateReferenceSystem());
                } else if (gridRange != null) {
                    gridGeometry = new GeneralGridGeometry(gridRange, envelope);
                } else {
                    gridGeometry = new GeneralGridGeometry(PixelInCell.CELL_CENTER, gridToCRS, envelope);
                }
            }
        }
        return gridGeometry;
    }

    /**
     * Gets the coverage referred to by the web service request params
     *
     * This is used in WMS GetFeatureInfo to provide information about the original data source.
     *
     * @return The coverage reference for the requested time and elevation.
     * @throws WebServiceException if an error occured while querying the layer.
     *
     * @todo In current implementation, the TIME parameter is mandatory. This is inconsistent
     *       with the behavior of other methods like {@link #getGridCoverage2D}.
     */
    public CoverageReference getCoverageReference() throws WebServiceException {
        final Layer layer = getLayer();
        final CoverageReference ref;
        if (times.isEmpty()) {
            throw new WMSWebServiceException("Must specify TIME.",
                    INVALID_PARAMETER_VALUE, version);
        }
        try {
            ref = layer.getCoverageReference(getTime(), elevation);
        } catch (CatalogException exception) {
            throw new WMSWebServiceException(exception, LAYER_NOT_QUERYABLE, version);
        }
        if (ref == null) {
            // TODO: provides a better message.
            throw new WMSWebServiceException(Resources.format(ResourceKeys.NO_DATA_TO_DISPLAY),
                    INVALID_PARAMETER_VALUE, version);
        }
        return ref;
    }

    /**
     * Gets the grid coverage for the current layer, time, elevation, <cite>etc.</cite>
     *
     * @param  resample {@code true} for resampling the coverage to the specified envelope
     *         and dimension, or {@code false} for getting the coverage as in the database.
     * @return The coverage for the requested time and elevation.
     * @throws WebServiceException if an error occured while querying the coverage.
     */
    public GridCoverage2D getGridCoverage2D(final boolean resample) throws WebServiceException {
        final Layer layer = getLayer();
        final CoverageReference ref;
        if (times.isEmpty()) {
            /*
             * If the WMS request does not include a TIME parameter, then use the latest time available.
             *
             * NOTE! - this gets the time of the LAYER's latest entry, but not necessarily within
             *         the requested bounding box.
             * TODO: This fix should probably be incorporated as part of the CoverageComparator,
             *       the but this quick hack keeps the Comparator from looking at ALL coverages
             *       in layer when no time parameter is given.
             */
            try {
                final SortedSet<Date> availableTimes = layer.getAvailableTimes();
                if (availableTimes != null && !availableTimes.isEmpty()) {
                    times.addAll(availableTimes);
                }
            } catch (CatalogException ex) {
                Logging.unexpectedException(LOGGER, ImageProducer.class, "getGridCoverage2D", ex);
                // 'time' still null, which is a legal value.
            }
        }
        try {
            ref = layer.getCoverageReference(getTime(), elevation);
        } catch (CatalogException exception) {
            throw new WMSWebServiceException(exception, LAYER_NOT_QUERYABLE, version);
        }
        if (ref == null) {
            // TODO: provides a better message.
            throw new WMSWebServiceException(Resources.format(ResourceKeys.NO_DATA_TO_DISPLAY),
                    INVALID_PARAMETER_VALUE, version);
        }
        GridCoverage2D coverage;
        try {
            coverage = ref.getCoverage(null);
        } catch (IOException exception) {
            Object file = ref.getFile();
            if (file == null) {
                file = ref.getName();
            }
            throw new WMSWebServiceException(Errors.format(ErrorKeys.CANT_READ_$1, file),
                    exception, LAYER_NOT_QUERYABLE, version);
        }
        if (resample) {
            final GridGeometry gridGeometry = getGridGeometry();
            if (gridGeometry != null) {
                final Operations op = Operations.DEFAULT;
                final CoordinateReferenceSystem targetCRS = getResponseCRS();
                coverage = (GridCoverage2D) op.resample(coverage, targetCRS, gridGeometry, interpolation);
            }
        }
        return coverage;
    }

    /**
     * Gets the image for the {@linkplain #getGridCoverage2D current coverage}.
     * The image is resized to the requested dimension and CRS.
     *
     * @return The rendered image for the requested time and elevation.
     * @throws WebServiceException if an error occured while querying the coverage.
     */
    public RenderedImage getRenderedImage() throws WebServiceException {
        GridCoverage2D coverage = getGridCoverage2D(true);
        final Service service = version.getService();
        if (service != null) {
            switch (service) {
                case WMS: coverage = coverage.view(ViewType.RENDERED);   break;
                case WCS: coverage = coverage.view(ViewType.GEOPHYSICS); break;
            }
        }
        if (colormapRange != null) {
            final ColorMap colorMap = new ColorMap();
            colorMap.setGeophysicsRange(ColorMap.ANY_QUANTITATIVE_CATEGORY, new MeasurementRange<Double>(colormapRange, null));
            coverage = (GridCoverage2D) Operations.DEFAULT.recolor(coverage, new ColorMap[] {colorMap});
        }
        RenderedImage image = coverage.getRenderedImage();
        if (LOGGER.isLoggable(Level.FINE)) {
            final Vocabulary resources = Vocabulary.getResources(null);
            LOGGER.fine(resources.getLabel (VocabularyKeys.IMAGE_SIZE) +
                        resources.getString(VocabularyKeys.IMAGE_SIZE_$3,
                        image.getWidth(), image.getHeight(), image.getSampleModel().getNumBands()));
        }
        if (indexedShortAllowed) {
            return image;
        }
        /*
         * If the image is not geophysics and is indexed on 16 bits, then rescale the image to 8
         * bits while preserving the colors. TODO: this algorithm is simplist and doesn't consider
         * the "no data" values, which is a risk of wrong output. We need to find some better way.
         */
        ColorModel model = image.getColorModel();
        if (model instanceof IndexColorModel) {
            final IndexColorModel icm = (IndexColorModel) model;
            final int sourceMapSize = icm.getMapSize();
            final int targetMapSize = 256;
            if (sourceMapSize > targetMapSize) {
                final GridSampleDimension dimension = coverage.getSampleDimension(
                        CoverageUtilities.getVisibleBand(image));
                if (dimension.geophysics(false) == dimension) {
                    final NumberRange range = dimension.getRange();
                    final double scale = (range != null ? range.getMaximum(false) :
                            dimension.getMaximumValue()) / targetMapSize;
                    if (scale > 1 && scale < Double.POSITIVE_INFINITY) {
                        final int[] ARGB = new int[targetMapSize];
                        for (int i=0; i<targetMapSize; i++) {
                            final int index = Math.min(sourceMapSize-1, (int) Math.round(i * scale));
                            ARGB[i] = icm.getRGB(index);
                        }
                        int transparent = (int) Math.round(icm.getTransparentPixel() / scale);
                        if (transparent != (int) Math.round(transparent * scale)) {
                            transparent = -1;
                        }
                        model = new IndexColorModel(8, targetMapSize, ARGB, 0, icm.hasAlpha(), transparent, DataBuffer.TYPE_BYTE);
                        final ImageLayout layout = new ImageLayout(image);
                        layout.setColorModel(model);
                        layout.setSampleModel(model.createCompatibleSampleModel(image.getWidth(), image.getHeight()));
                        final RenderingHints hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);
                        hints.put(JAI.KEY_REPLACE_INDEX_COLOR_MODEL, Boolean.FALSE);
                        hints.put(JAI.KEY_TRANSFORM_ON_COLORMAP, Boolean.FALSE);
                        image = DivideByConstDescriptor.create(image, new double[] {scale}, hints);
                        assert image.getColorModel() == model;
                    }
                }
            }
        }
        return image;
    }

    /**
     * Returns the format as a mime type.
     *
     * @return The format MIME type.
     * @throws WebServiceException if the format is not defined.
     */
    public String getMimeType() throws WebServiceException {
        if (format != null) {
            if (mimeTypes != null) {
                final String mime = mimeTypes.get(format);
                if (mime != null) {
                    return mime;
                }
            }
            return format;
        } else if (DEFAULT_FORMAT != null) {
            return DEFAULT_FORMAT;
        } else {
            throw new WMSWebServiceException(Errors.format(ErrorKeys.MISSING_PARAMETER_$1, "format"),
                    MISSING_PARAMETER_VALUE, version);
        }
    }

    /**
     * Returns a snapshot of current configuration as an {@link ImageRequest} instance.
     */
    private ImageRequest getImageRequest(final ImageType type) throws WebServiceException {
        final GridGeometry geometry = getGridGeometry();
        return new ImageRequest(type, layer, geometry, responseCRS, colormapRange,
                getTime(), elevation, interpolation, format, background, transparent);
    }

    /**
     * Returns the legend as an image. The {@link #setDimension dimension} and {@link #setFormat
     * format} are honored.
     *
     * @return The legend image as a file.
     * @throws WebServiceException if an error occured while processing the legend.
     */
    public File getLegendFile() throws WebServiceException {
        final ImageRequest request = getImageRequest(ImageType.LEGEND);
        if (files != null) {
            final File file = files.get(request);
            if (file != null) {
                return file;
            }
        }
        final Dimension dimension;
        if (gridRange != null) {
            dimension = new Dimension(gridRange.getHigh(0), gridRange.getHigh(1));
        } else {
            dimension = new Dimension(200, 40);
        }
        return getImageFile(request, getLayer().getLegend(dimension));
    }

    /**
     * Returns the {@linkplain #getRenderedImage current rendered image} as a file in the
     * {@linkplain #getMimeType current format}. The file is created in the temporary
     * directory, may be overwritten during the next invocation of this method and will
     * be deleted at JVM exit.
     *
     * @return The image as a file.
     * @throws WebServiceException if an error occured while processing the image.
     */
    public File getImageFile() throws WebServiceException {
        ImageType type = ImageType.COVERAGE; // Default value.
        final Service service = version.getService();
        if (service != null) {
            switch (service) {
                case WMS: type = ImageType.IMAGE;    break;
                case WCS: type = ImageType.COVERAGE; break;
            }
        }
        final ImageRequest request = getImageRequest(type);
        if (files != null) {
            final File file = files.get(request);
            if (file != null) {
                return file;
            }
        }
        File f = getImageFile(request, getRenderedImage());
        return f;
    }

    /**
     * Returns the given image as a file in the {@linkplain #getMimeType current format}.
     * The file is created in the temporary directory, may be overwritten during the next
     * invocation of this method and will be deleted at JVM exit.
     *
     * @param  request A description of the request.
     * @throws WebServiceException if an error occured while processing the image.
     */
    private File getImageFile(final ImageRequest request, final RenderedImage image) throws WebServiceException {
        RenderedImage formated = image;
        try {
            /*
             * Try the last used writer, if there is any. If this writer needs to reformat the
             * data in order to processed (for example PNG needs to convert 16 bits indexed to
             * RGB or 8 bits indexed), then we will reformat before to try an other writer.
             */
            if (writer != null) {
                File file = write(request, image);
                if (file != null) {
                    return file;
                }
                if (writerNeedsReformat) {
                    formated = reformat(image);
                    if (formated != image) {
                        file = write(request, formated);
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
            File file = write(request, image, format);
            if (file != null) {
                return file;
            }
            if (formated != image) {
                file = write(request, formated, format);
                if (file != null) {
                    writerNeedsReformat = true;
                    return file;
                }
            } else {
                final Iterator<ImageWriter> it = getImageWriter(format);
                while (it.hasNext()) {
                    writer = it.next();
                    formated = reformat(image);
                    if (formated != image) {
                        file = write(request, formated);
                        if (file != null) {
                            writerNeedsReformat = true;
                            return file;
                        }
                        break;
                    }
                }
            }
        } catch (IOException exception) {
            disposeWriter();
            throw new WMSWebServiceException(exception, LAYER_NOT_QUERYABLE, version);
        }
        disposeWriter();
        throw new WMSWebServiceException(Errors.format(ErrorKeys.NO_IMAGE_WRITER), LAYER_NOT_QUERYABLE, version);
    }

    /**
     * Returns {@code true} if the given color model is opaque (i.e. has no transparent pixels).
     */
    private static boolean isOpaque(final ColorModel model) {
        return model.getTransparency() == Transparency.OPAQUE;
    }

    /**
     * Reformats the given image to a type appropriate for the current {@linkplain #writer}.
     * If this method can't reformat, then the image is returned unchanged.
     */
    private RenderedImage reformat(final RenderedImage image) {
        final ImageWriterSpi spi = writer.getOriginatingProvider();
        if (spi != null) {
            final boolean opaque = isOpaque(image.getColorModel());
            for (int i=0; i<BUFFERED_TYPES.length; i++) {
                final ImageTypeSpecifier type =
                        ImageTypeSpecifier.createFromBufferedImageType(BUFFERED_TYPES[i]);
                if (opaque && isOpaque(type.getColorModel())) {
                    // In order to reduce the file size, discards
                    // types with transparency if we don't need it.
                    continue;
                }
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
     * Returns the image writer for the given format. We usually expect a MIME type,
     * but we make this method tolerant to simple format names.
     */
    private static Iterator<ImageWriter> getImageWriter(final String format) {
        if (format.indexOf('/') >= 0) {
            return ImageIO.getImageWritersByMIMEType(format);
        } else if (format.startsWith(".")) {
            return ImageIO.getImageWritersBySuffix(format.substring(1));
        } else {
            return ImageIO.getImageWritersByFormatName(format);
        }
    }

    /**
     * Attempts to write the given image using a writer of the given format.
     * The current {@linkplain #writer}, if any, is disposed.
     *
     * @param  request A description of the request.
     * @param  image  The image to write.
     * @param  format The format for the writer to use.
     * @return The file, or {@code null} if no writer is suitable.
     * @throws IOException if an error occured while processing the image.
     */
    private File write(final ImageRequest request, final RenderedImage image, final String format) throws IOException, WebServiceException {
        final Iterator<ImageWriter> it = getImageWriter(format);
        while (it.hasNext()) {
            disposeWriter();
            writer = it.next();
            final File file = write(request, image);
            if (file != null) {
                return file;
            }
        }
        return null;
    }

    /**
     * Attempts to write the given image using the current {@linkplain #writer writer}.
     *
     * @param  request A description of the request.
     * @param  image The image to write.
     * @return The file, or {@code null} if the current writer is not suitable.
     * @throws IOException if an error occured while processing the image.
     */
    private File write(final ImageRequest request, final RenderedImage image) throws IOException, WebServiceException {
        final ImageWriterSpi spi = writer.getOriginatingProvider();
        if (spi != null && !spi.canEncodeImage(image)) {
            return null; // Can not encode the image.
        }
        // Additional check for format that doesn't support 16 bits palettes.
        final ColorModel model = image.getColorModel();
        if (model.getPixelSize() > 8 && model instanceof IndexColorModel) {
            final String name = writer.getClass().getName();
            if (name.indexOf("PNG", name.lastIndexOf('.') + 1) >= 0) {
                return null;
            }
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
                    String candidate = candidates[0];
                    if (!candidate.startsWith(".")) {
                        candidate = '.' + candidate;
                    }
                    writerSuffix = candidate;
                } else {
                    writerSuffix = DEFAULT_SUFFIX;
                }
                writerAcceptsFile = XArray.contains(spi.getOutputTypes(), File.class);
            }
        }
        /*
         * Gets a temporary file, different for every WebServiceWorker instance (so
         * different for every thread if the rule given in the javadoc is respected).
         * This file may be overwritten at any invocation of this method, in order to
         * avoid a multiplication of temporary file. It will be deleted on JVM exit.
         */
        if (files == null) {
            files = new ImageRequestMap();
            final String directory = System.getProperty("java.io.tmpdir");
            if (directory != null) {
                temporaryDirectory = new File(directory, "constellation");
                if (!temporaryDirectory.isDirectory() && !temporaryDirectory.mkdir()) {
                    temporaryDirectory = null; // Fallback on system default.
                }
            }
        }
        File file = files.get(request);
        if (file != null) {
            return file;
        }
        file = File.createTempFile(request.type.name(), writerSuffix, temporaryDirectory);
        file.deleteOnExit();
        files.put(request, file);
        /*
         * If the image writer accepts directly file output, uses it. Otherwise we
         * will create an image output stream, which should be accepted by all writers
         * according Image I/O specification.
         */
        try {
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
        } catch (RasterFormatException ex) {
            throw new WMSWebServiceException(ex, LAYER_NOT_QUERYABLE, version);
        }
        /*
         * Retains the MIME type used, if it was not already a MIME type.
         */
        if ((format == null || format.indexOf('/') < 0) && spi != null) {
            final String[] mimes = spi.getMIMETypes();
            if (mimes != null && mimes.length != 0) {
                if (mimeTypes == null) {
                    mimeTypes = new HashMap<String,String>();
                }
                mimeTypes.put(format, mimes[0]);
            }
        }
        return file;
    }

    /**
     * Evaluates the {@linkplain #getGridCoverage2D current coverage} at the given position,
     * <strong>in pixels coordinates</strong>. If the coverage has more than one band, only
     * the value in the first band is returned. This methods returns the <cite>geophysics</cite>
     * value, if possible.
     *
     * @param  x The first coordinate, typically longitude.
     * @param  y The second coordinate, typically latitude.
     * @return The geophysics value at the given position.
     * @throws WebServiceException if an error occured while processing the date.
     */
    public double evaluatePixel(final double x, final double y) throws WebServiceException {
        final GridGeometry gridGeometry = getGridGeometry();
        if (gridGeometry != null) {
            final MathTransform gridToCRS = gridGeometry.getGridToCRS();
            if (gridToCRS != null) {
                this.coordinate = new DirectPosition2D(getCoordinateReferenceSystem(), x, y);
                try {
                    coordinate = gridToCRS.transform(coordinate, coordinate);
                } catch (TransformException exception) {
                    throw new WMSWebServiceException(exception, INVALID_POINT, version);
                }
                double[] values = null;
                try {
                    values = getGridCoverage2D(false).evaluate(coordinate, values);
                } catch (PointOutsideCoverageException exception) {
                    throw new WMSWebServiceException(exception, INVALID_POINT, version);
                }
                if (values.length != 0) {
                    return values[0];
                }
            }
        }
        return Double.NaN;
    }

    /**
     * Return the geographic coordinate of a point.
     * This method can be invoked after evaluatePixel(...) in a getFeatureInfo request.
     */
    @Deprecated
    public DirectPosition getCoordinates() {
        return this.coordinate;
    }

    /**
     * Invoked after a change in envelope, resolution, image size, <cite>grid to CRS</cite>
     * transform or response CRS.
     *
     * @see #getGridGeometry
     */
    protected void clearGridGeometry() {
        gridGeometry = null;
    }

    /**
     * Clears the cache. This method should be invoked when the database content changed.
     * This {@code WebServiceWorker} instance can still be used, but the first next invocation
     * may be a little bit slower until the cache is rebuild.
     *
     * @throws WebServiceException if an error occured while clearing the cache.
     */
    public void flush() throws WebServiceException {
        LOGGER.info("Web Service Worker is flushing");
        disposeWriter();
        if (files != null) {
            files.run();
        }
        layers.clear();
        layerNames       = null;
        layerTable       = null;
        globalLayerTable = null;
        clearGridGeometry();
        try {
            database.flush();
        } catch (CatalogException exception) {
            throw new WMSWebServiceException(exception, NO_APPLICABLE_CODE, version);
        } catch (SQLException exception) {
            throw new WMSWebServiceException(exception, NO_APPLICABLE_CODE, version);
        }
    }

    /**
     * Disposes the {@linkplain #writer} and information derived from the writer.
     * Other fields must be left untouched.
     */
    final void disposeWriter() {
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
     * when waiting for the garbage collector would be over-conservative. This
     * {@code WebServiceWorker} instance should not be used anymore after disposal.
     *
     * @throws WebServiceException if an error occured while disposing the resources.
     */
    public void dispose() throws WebServiceException {
        flush();
        // we delete the worker of the management MBean
        if (manager != null){
            manager.removeWorker(this);
        }
        if (files != null) {
            files.dispose();
            files = null;
        }
        // Do not close the database connection, since it may be shared by other instances.
    }

    public Database getDatabase(){
        return database;
    }
}
