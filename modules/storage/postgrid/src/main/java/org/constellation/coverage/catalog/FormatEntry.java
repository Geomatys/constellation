/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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
package org.constellation.coverage.catalog;

import java.awt.Dimension;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.awt.image.RenderedImage;
import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URI;
import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.Locale;
import java.util.Arrays;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.IdentityHashMap;
import java.util.logging.LogRecord;
import java.util.logging.Level;
import java.nio.ByteOrder;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.IIOException;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.JAI;
import javax.media.jai.operator.BandMergeDescriptor;
import com.sun.media.imageio.stream.RawImageInputStream;

import org.geotoolkit.util.Utilities;
import org.geotoolkit.util.NumberRange;
import org.geotoolkit.util.MeasurementRange;
import org.geotoolkit.coverage.Category;
import org.geotoolkit.coverage.GridSampleDimension;
import org.geotoolkit.gui.swing.image.ColorRamp;
import org.geotoolkit.gui.swing.tree.MutableTreeNode;
import org.geotoolkit.gui.swing.tree.DefaultMutableTreeNode;
import org.geotoolkit.image.io.IIOListeners;
import org.geotoolkit.image.io.RawBinaryImageReadParam;
import org.geotoolkit.image.io.netcdf.NetcdfImageReader;
import org.geotoolkit.image.io.mosaic.MosaicImageReader;
import org.geotoolkit.image.io.mosaic.MosaicImageReadParam;
import org.geotoolkit.util.converter.Classes;
import org.geotoolkit.util.XArrays;

import org.constellation.catalog.Entry;
import org.constellation.resources.i18n.Resources;
import org.constellation.resources.i18n.ResourceKeys;


/**
 * Default {@link Format} implementation.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class FormatEntry extends Entry implements Format {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = -8790032968708208057L;

    /**
     * {@code true} for using JAI {@code "ImageRead"} operation,
     * or {@code false} for using {@link ImageReader} directly.
     */
    private static final boolean USE_IMAGE_READ_OPERATION = Boolean.getBoolean("org.constellation.usejai");

    /**
     * The input types for mosaic image reader.
     */
    private static final Class<?>[] MOSAIC_INPUT_TYPES = MosaicImageReader.Spi.DEFAULT.getInputTypes();

    /**
     * Image waiting or in process of being read. Keys are {@link CoverageReference} to be read.
     * Values are {@link Boolean#TRUE} if a read operation is in progress, or {@link Boolean#FALSE}
     * if a read operation is enqueueded for starting as soon as possible.
     */
    private final transient Map<CoverageReference,Boolean> enqueued =
            new IdentityHashMap<CoverageReference,Boolean>();

    /**
     * Format name (or, in legacy database, mime type) as declared in the database.
     */
    private final String formatName;

    /**
     * Sample dimensions for coverages encoded with this format.
     */
    private final GridSampleDimension[] bands;

    /**
     * {@code true} if coverage to be read are already geophysics values.
     */
    private final boolean geophysics;

    /**
     * The reader to use for reading pixel values. Will be created only when first needed,
     * then reused for all future use.
     */
    private transient ImageReader reader;

    /**
     * The {@linkplain #reader} provider. Will be created only when first needed.
     */
    private transient ImageReaderSpi provider;

    /**
     * Creates a new entry for this format.
     *
     * @param name       An identifier for this entry.
     * @param formatName Format name. May be MIME type in legacy database (example: {@code "image/png"}).
     * @param extension  Filename extension, excluding dot separator (example: {@code "png"}).
     * @param geophysics {@code true} if coverage to be read are already geophysics values.
     * @param bands      Sample dimensions for coverages encoded with this format.
     */
    protected FormatEntry(final String  name,
                          final String  formatName,
                          final boolean geophysics,
                          final GridSampleDimension[] bands)
    {
        super(name);
        this.formatName = formatName.trim().intern();
        this.geophysics = geophysics;
        this.bands      = bands;
        for (int i=0; i<bands.length; i++) {
            bands[i] = bands[i].geophysics(geophysics);
        }
    }

    /**
     * Returns the local to be given to the {@link ImageReader}, or {@code null} if none.
     */
    private static Locale getLocale() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getImageFormat() {
        return formatName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MeasurementRange<Double>[] getSampleValueRanges() {
        final GridSampleDimension[] bands = getSampleDimensions();
        @SuppressWarnings({"unchecked","rawtypes"})  // Generic array creation.
        final MeasurementRange<Double>[] ranges = new MeasurementRange[bands.length];
        for (int i=0; i<ranges.length; i++) {
            final GridSampleDimension band = bands[i].geophysics(true);
            ranges[i] = MeasurementRange.create(band.getMinimumValue(), band.getMaximumValue(), band.getUnits());
        }
        return ranges;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GridSampleDimension[] getSampleDimensions() {
        return getSampleDimensions(null);
    }

    /**
     * Returns the sample dimensions for coverages encoded with this format. If parameters are
     * supplied, this method returns only the sample dimensions for supplied source bands list
     * and returns them in the order inferred from the destination bands list.
     */
    GridSampleDimension[] getSampleDimensions(final ImageReadParam param) {
        int  bandCount = bands.length;
        int[] srcBands = null;
        int[] dstBands = null;
        if (param != null) {
            srcBands = param.getSourceBands();
            dstBands = param.getDestinationBands();
            if (srcBands!=null && srcBands.length<bandCount) bandCount = srcBands.length;
            if (dstBands!=null && dstBands.length<bandCount) bandCount = dstBands.length;
        }
        final GridSampleDimension[] selectedBands = new GridSampleDimension[bandCount];
        /*
         * Searchs for 'GridSampleDimension' from the given source band index and
         * stores their reference at the position given by destination band index.
         */
        for (int j=0; j<bandCount; j++) {
            final int srcBand = (srcBands!=null) ? srcBands[j] : j;
            final int dstBand = (dstBands!=null) ? dstBands[j] : j;
            selectedBands[dstBand] = bands[srcBand % bandCount];
        }
        return selectedBands;
    }

    /**
     * Returns the image reader provider.
     *
     * @throws IIOException if no suitable image reader provider can been found.
     */
    ImageReaderSpi getImageReaderSpi() throws IIOException {
        if (provider == null) {
            // We don't synchronize above this point because it is not a big
            // deal if two instances are created.
            synchronized (this) {
                provider = new ImageReaderSpiDecorator(getImageReader().getOriginatingProvider()) {
                    @Override
                    public ImageReader createReaderInstance() throws IOException {
                        final ImageReader reader = super.createReaderInstance();
                        handleSpecialCases(reader);
                        return reader;
                    }

                    @Override
                    public ImageReader createReaderInstance(final Object extension) throws IOException {
                        final ImageReader reader = super.createReaderInstance(extension);
                        handleSpecialCases(reader);
                        return reader;
                    }
                };
            }
        }
        return provider;
    }

    /**
     * Returns the single image reader. Callers <strong>must</strong> use the returned reader
     * inside a block synchronized on {@code this} lock.
     *
     * @return The single image reader to use (never {@code null}).
     * @throws IIOException if no suitable {@link ImageReader} has been found.
     */
    private ImageReader getImageReader() throws IIOException {
        assert Thread.holdsLock(this);
        if (reader == null) {
            final boolean isMIME = formatName.indexOf('/') >= 0;
            final Iterator<ImageReader> readers;
            if (isMIME) {
                readers = ImageIO.getImageReadersByMIMEType(formatName);
            } else {
                readers = ImageIO.getImageReadersByFormatName(formatName);
            }
            if (!readers.hasNext()) {
                /*
                 * No decoder found. Gets the list of decodeur. This list will be
                 * inserted in the error message as an attempt to help debugging.
                 * We will take only the first format name of each SPI since the
                 * other are only synonymous and we want to keep the message short.
                 */
                final Set<String> formats = new TreeSet<String>();
                final Iterator<ImageReaderSpi> spi = IIORegistry.getDefaultInstance()
                        .getServiceProviders(ImageReaderSpi.class, false);
                while (spi.hasNext()) {
                    final ImageReaderSpi p = spi.next();
                    final String[] f = isMIME ? p.getMIMETypes() : p.getFormatNames();
                    if (f != null && f.length != 0 && f[0].length() != 0) {
                        formats.add(f[0]);
                    }
                }
                final StringBuilder buffer = new StringBuilder(Resources.format(
                        ResourceKeys.ERROR_NO_IMAGE_DECODER_$1, formatName));
                String separator = " Available decoders are: "; // TODO: localize
                for (final String f : formats) {
                    buffer.append(separator).append(f);
                    separator = ", ";
                }
                throw new IIOException(buffer.toString());
            }
            reader = readers.next();
            if (false && readers.hasNext()) { // Check disabled for now.
                throw new IIOException(Resources.format(
                        ResourceKeys.ERROR_TOO_MANY_IMAGE_FORMATS_$1, formatName));
            }
            handleSpecialCases(reader);
        }
        return reader;
    }

    /**
     * Handles special cases for image reader configuration.
     *
     * @deprecated We need to figure out a better way to do this stuff.
     */
    private void handleSpecialCases(final ImageReader reader) {
        if (reader instanceof NetcdfImageReader) {
            final NetcdfImageReader r = (NetcdfImageReader) reader;
            final GridSampleDimension[] bandes = getSampleDimensions(null);
            final String[] names = new String[bandes.length];
            for (int i=0; i<names.length; i++) {
                String name = bandes[i].getDescription().toString().trim();
                /*
                 * Trim the name at the first invalid identifier character. This is a hack for
                 * allowing different NetCDF variables in the Categories table (e.g. "depth",
                 * "depth (Gascogne)", etc.)
                 */
                final int length = name.length();
                for (int j=0; j<length; j++) {
                    if (!Character.isJavaIdentifierPart(name.charAt(j))) {
                        name = name.substring(0, j);
                        break;
                    }
                }
                names[i] = name;
            }
            r.setVariables(names);
        }
    }

    /**
     * Returns a block of default parameters for the current format. This method is called by
     * {@link GridCoverageEntry#getCoverage}. Note: this method <strong>must</strong> be called
     * from block synchronized on {@code this}.
     *
     * @return Un bloc de paramètres par défaut. Cette méthode ne retourne jamais {@code null}.
     * @throws IIOException s'il n'y a pas d'objet {@link ImageReader} pour ce format.
     */
    ImageReadParam getDefaultReadParam(final Object input) throws IIOException {
        assert Thread.holdsLock(this);
        if (contains(MOSAIC_INPUT_TYPES, input.getClass())) {
            return new MosaicImageReadParam();
        } else {
            return getImageReader().getDefaultReadParam();
        }
    }

    /**
     * Indicates if the table {@code array} contains at least an
     * example of the class {@code item} or of a super-class.
     */
    private static <T> boolean contains(final Class<?>[] array, final Class<T> item) {
        for (final Class<?> c : array) {
            if (c.isAssignableFrom(item)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Converts the specified {@code input} object to one of the types specified in the
     * table {@code inputTypes}. If the conversion can not be executed,
     * then this method returns {@code null}.
     */
    private static Object getInput(final Object file, final Class<?>[] inputTypes) {
        if (contains(inputTypes, file.getClass())) {
            return file;
        }
        if (contains(inputTypes, File.class)) try {
            if (file instanceof URI) {
                return new File((URI) file);
            }
            if (file instanceof URL) {
                return new File(((URL) file).toURI());
            }
        } catch (Exception exception) {
            // Ignore... Following code is a raisonable fallback.
        }
        if (contains(inputTypes, URL.class)) try {
            if (file instanceof File) {
                return ((File) file).toURI().toURL();
            }
            if (file instanceof URI) {
                return ((URI) file).toURL();
            }
        } catch (MalformedURLException exception) {
            // Ignore... Following code is a raisonable fallback.
        }
        if (contains(inputTypes, URI.class)) try {
            if (file instanceof File) {
                return ((File) file).toURI();
            }
            if (file instanceof URL) {
                return ((URL) file).toURI();
            }
        } catch (URISyntaxException exception) {
            // Ignore... Following code is a raisonable fallback.
        }
        return null;
    }

    /**
     * Process to image reading. This method is invoked by {@link GridCoverageEntry#getCoverage}
     * only.
     * <p>
     * Note 1: cette méthode <strong>doit</strong> être appelée à partir d'un bloc
     * synchronisé sur {@code this}.
     * <p>
     * Note 2: La méthode {@link #setReading} <strong>doit</strong> être appelée
     *         avant et après cette méthode dans un bloc {@code try...finally}.
     *
     * @param  input Fichier à lire. Habituellement un objet {@link File}, {@link URL} ou {@link URI}.
     * @param  imageIndex Index (à partir de 0) de l'image à lire.
     * @param  numImages Number of consecutive images to read as different bands.
     *         WARNING: This is an ugly patch to remove when we can.
     * @param  param Bloc de paramètre à utiliser pour la lecture.
     * @param  listeners Objets à informer des progrès de la lecture ainsi que des éventuels
     *         avertissements, ou {@code null} s'il n'y en a pas. Les objets qui ne sont
     *         pas de la classe {@link IIOReadWarningListener} ou {@link IIOReadProgressListener}
     *         ne seront pas pris en compte.
     * @param  expected Dimension prévue de l'image.
     * @param  source Objet {@link CoverageReference} qui a demandé la lecture de l'image.
     *         Cette information sera utilisée par {@link #abort} pour vérifier si
     *         un l'objet {@link CoverageReference} qui demande l'annulation est celui qui
     *         est en train de lire l'image.
     * @return Image lue, ou {@code null} si la lecture de l'image a été annulée.
     * @throws IOException si une erreur est survenue lors de la lecture.
     */
    RenderedImage read(final Object         input,
                       final int            imageIndex,
                       final int            numImages,
                       final ImageReadParam param,
                       final IIOListeners   listeners,
                       final Dimension      expected,
                       final CoverageReference source) throws IOException
    {
        assert Thread.holdsLock(this);
        RenderedImage    image       = null;
        ImageInputStream inputStream = null;
        Object           inputObject;
        /*
         * Obtient l'objet à utiliser comme source. Autant que possible,  on
         * essaira de donner un objet de type 'File' ou 'URL', ce qui permet
         * au décodeur d'utiliser la connection la plus appropriée pour eux.
         */
        final ImageReader reader;
        final ImageReaderSpi spi;
        if (contains(MOSAIC_INPUT_TYPES, input.getClass())) {
            spi = MosaicImageReader.Spi.DEFAULT;
            reader = spi.createReaderInstance();
            inputObject = input;
        } else {
            reader = getImageReader();
            spi = reader.getOriginatingProvider();
            final Class<?>[] inputTypes = (spi!=null) ? spi.getInputTypes() : ImageReaderSpi.STANDARD_INPUT_TYPE;
            inputObject = getInput(input, inputTypes);
            if (inputObject == null) {
                inputObject = inputStream = ImageIO.createImageInputStream(input);
                if (inputObject == null) {
                    throw new FileNotFoundException(Resources.format(
                            ResourceKeys.ERROR_FILE_NOT_FOUND_$1, getPath(input)));
                }
            }
            /*
             * If the image is in "RAW" format, define the size of the image. This is necessary
             * because the "RAW" format does not contain any information about the size or data
             * type of the image.
             *
             * Note: We check the format name, not the RawImageInputStream input type, because
             * the later is a subclass of ImageInputStream which is supported by every readers.
             * Looking for it cause this code to be executed for every formats, not just RAW.
             */
            if (inputStream != null && XArrays.containsIgnoreCase(spi.getFormatNames(), "raw")) {
                // Patch contributed by Sam Hiatt
                // NOTE this fix requires a patched version of jai-imageio.jar
                // contact me if you have any questions about that.
                // Temporary hack for finding data type of raw format
                // TODO: set up a Format Parameters table or something in the database.

                final int type;
                final String name = getName();
                if (name.contains("flt32")) {
                    type = DataBuffer.TYPE_FLOAT;
                } else if (name.contains("flt64")) {
                    type = DataBuffer.TYPE_DOUBLE;
                } else {
                    type = DataBuffer.TYPE_BYTE;
                }
                final GridSampleDimension[] bandss = getSampleDimensions(param);
                if (bandss.length == 0) {
                    // We should instead make the code tolerant to images without SampleDimension.
                    throw new IIOException("No Sample Dimension found for format \"" + name + "\".");
                }
                final ColorModel  cm = bandss[0].getColorModel(0, bandss.length, type);
                final SampleModel sm = cm.createCompatibleSampleModel(expected.width, expected.height);
                inputObject = inputStream = new RawImageInputStream(inputStream,
                        new ImageTypeSpecifier(cm, sm), new long[] {0}, new Dimension[] {expected});
                inputStream.setByteOrder(ByteOrder.nativeOrder());
            }
        }
        // Patch temporaire, en attendant que les décodeurs spéciaux (e.g. "image/raw-msla")
        // soient adaptés à l'architecture du décodeur RAW de Sun.


        // Can this be trashed now?
        // MD answer: not yet, it still used.
        if (param instanceof RawBinaryImageReadParam) {
            final RawBinaryImageReadParam rawParam = (RawBinaryImageReadParam) param;
            if (rawParam.getStreamImageSize() == null) {
                rawParam.setStreamImageSize(expected);
            }
            if (rawParam.getDestinationType() == null) {
                // Use auto-detection for now as a patch for avoiding modification of
                // the database, but what we should really do is to add an explicit
                // column in the Format table.
                if (geophysics) {
                    final int dataType = rawParam.getStreamDataType();
                    if (dataType != DataBuffer.TYPE_FLOAT &&
                        dataType != DataBuffer.TYPE_DOUBLE)
                    {
                        rawParam.setDestinationType(DataBuffer.TYPE_FLOAT);
                    }
                } else if (bands != null && bands.length != 0) {
                    final double lower = bands[0].getMinimumValue();
                    final double upper = bands[0].getMaximumValue();
                    if (lower >= 0 && upper <= 0xFF) {
                        rawParam.setDestinationType(DataBuffer.TYPE_BYTE);
                    } else if (lower >= 0 && upper <= 0xFFFF) {
                        rawParam.setDestinationType(DataBuffer.TYPE_USHORT);
                    } else if (lower >= Short.MIN_VALUE && upper <= Short.MAX_VALUE) {
                        rawParam.setDestinationType(DataBuffer.TYPE_SHORT);
                    } else if (lower >= Integer.MIN_VALUE && upper <= Integer.MAX_VALUE) {
                        rawParam.setDestinationType(DataBuffer.TYPE_INT);
                    }
                }
            }
        }
        /*
         * Now configure the decoder and launch the image read.  This stage exists
         * in two versions: With the "ImageRead" operation, or direct reading
         * through the ImageReader.
         */
        handleSpecialCases(reader);
        if (USE_IMAGE_READ_OPERATION) {
            /*
             * Use of the "ImageRead" operation: This approach defers reading tiles until
             * an unspecified time after calling this method. It has the advantage of better
             * memory consumption, thanks to JAI's TileCache, but it makes it more difficult
             * to generate exceptions, or to abort an image read with 'abort()', which makes
             * 'enqueued' null.
             */
            image = JAI.create("ImageRead", new ParameterBlock()
                .add(inputObject)                  // Objet à utiliser en entré
                .add(imageIndex)                   // Index de l'image à lire
                .add(Boolean.FALSE)                // Pas de lecture des méta-données
                .add(Boolean.FALSE)                // Pas de lecture des "thumbnails"
                .add(Boolean.TRUE)                 // Vérifier la validité de "input"
                .add(listeners == null ? null : listeners.getReadListeners()) // Liste des "listener"
                .add(getLocale())                  // Langue du décodeur
                .add(param)                        // Les paramètres
                .add(reader));                     // L'objet à utiliser pour la lecture.
            this.reader = null;                    // N'utilise qu'un ImageReader par opération.

            if (inputStream != null && XArrays.containsIgnoreCase(spi.getFormatNames(), "raw")) {
            // workaround to mask out no-data values in the new image
            // TODO: add no-data specification in Formats table, or somewhere
                final double[] lower = { -9999 };  //TODO: get these from the database
                final double[] upper = { -999 };
                final double[] fill = { Float.NaN };
                final ParameterBlock pb = new ParameterBlock();
                pb.addSource(image).add(lower).add(upper).add(fill);
                image = JAI.create("threshold", pb, null);
            }
        } else try {
            /*
             * Direct use of 'ImageReader': This approach reads the image immediately,
             * which eases managing exceptions, aborting image reading with 'abort()',
             * and synchronizations.
             */
            if (listeners != null) {
                listeners.addListenersTo(reader);
            }
            reader.setLocale(getLocale());
            reader.setInput(inputObject, true, true);
            if (!(param instanceof RawBinaryImageReadParam)) {
                checkSize(reader.getWidth(imageIndex), reader.getHeight(imageIndex), expected, input);
            }
            /*
             * Reads the file, closes it in the "finally" block and returns the image.
             * The reading will not be performed if the user aborted it before we reach
             * this point.
             */
            final boolean waiting;
            synchronized (enqueued) {
                waiting = enqueued.put(source, Boolean.TRUE) != null;
            }
            if (waiting) try {
                image = reader.readAsRenderedImage(imageIndex, param);
            } catch (OutOfMemoryError e) {
                System.gc();
                System.runFinalization();
                System.gc();
                image = reader.readAsRenderedImage(imageIndex, param);
            }
            // WARNING: Ugly and unefficient patch below, to remove when we can (we
            // need a real GridCoverageReader in order to clean all this stuff).
            for (int i=1; i<numImages; i++) {
                final RenderedImage more = reader.readAsRenderedImage(imageIndex + i, param);
                image = BandMergeDescriptor.create(image, more, null);
            }
        } finally {
            final boolean aborted;
            synchronized (enqueued) {
                aborted = enqueued.remove(source) == null;
            }
            if (aborted) {
                // User aborted the reading while it was in process.
                image = null;
            }
            reader.reset(); // Comprend "removeIIOReadProgressListener" et "setInput(null)".
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return image;
    }

    /**
     * <strong>Must</strong> be invoked before and after {@link #read}. The thread must
     * <strong>not</strong> hold the lock on {@code this}. This method should be invoked
     * in a {@code try...finally} clause as below:
     *
     * <blockquote><pre>
     * try {
     *     format.setReading(source, true);
     *     synchronized (format) {
     *         format.read(...);
     *     }
     * } finally {
     *     format.setReading(source, false);
     * }
     * </pre></blockquote>
     */
    void setReading(final CoverageReference source, final boolean starting) {
        assert !Thread.holdsLock(this); // The thread must *not* hold the lock.
        if (starting) {
            final boolean waiting;
            synchronized (enqueued) {
                waiting = enqueued.put(source, Boolean.FALSE) != null;
            }
            if (waiting) {
                throw new AssertionError();
            }
        } else {
            synchronized (enqueued) {
                enqueued.remove(source);
            }
        }
    }

    /**
     * Cancel the image read by calling {@link ImageReader#abort}.
     * This method can me called from any thread.
     *
     * @param source Object that called this method.
     */
    void abort(final CoverageReference source) {
        assert !Thread.holdsLock(this); // The thread must *not* hold the lock.
        final Boolean active;
        synchronized (enqueued) {
            active = enqueued.remove(source);
            if (Boolean.TRUE.equals(active)) {
                if (reader != null) {
                    reader.abort();
                }
            }
        }
        if (active != null) {
            final String name = source.getName();
            final LogRecord record = Resources.getResources(null).getLogRecord(Level.FINE,
                        ResourceKeys.ABORT_IMAGE_READING_$2, name,
                        Integer.valueOf(active.booleanValue() ? 1 : 0));
            record.setSourceClassName("CoverageReference");
            record.setSourceMethodName("abort");
            LOGGER.log(record);
        }
    }

    /**
     * Check that the size of the image is the same as that declared in the
     * database.  This check is only used to catch possible errors
     * that would otherwise slip into the database and/or the copy of the
     * image on disk.
     *
     * @param  imageWidth   Width (in pixels)
     * @param  imageHeight  Height (in pixels)
     * @param  expected     Expected width and height
     * @param  file         Name of the image file to read
     * @throws IIOException If the image does not have the expected size
     */
    private static void checkSize(final int imageWidth, final int imageHeight,
                                  final Dimension expected, final Object file)
        throws IIOException
    {
        if (expected.width!=imageWidth || expected.height!=imageHeight) {
            throw new IIOException(Resources.format(ResourceKeys.ERROR_IMAGE_SIZE_MISMATCH_$5, getPath(file),
                                   Integer.valueOf(imageWidth), Integer.valueOf(imageHeight),
                                   Integer.valueOf(expected.width), Integer.valueOf(expected.height)));
        }
    }

    /**
     * Returns the path component of the specified input file.
     * The specified object is usually a {@link File}, {@link URL} or {@link URI} object.
     */
    private static String getPath(final Object file) {
        if (file instanceof File) {
            return ((File) file).getPath();
        } else if (file instanceof URL) {
            return ((URL) file).getPath();
        } else if (file instanceof URI) {
            return ((URI) file).getPath();
        } else {
            return file.toString();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BufferedImage getLegend(final Dimension dimension) {
        final GridSampleDimension[] bands = getSampleDimensions();
        if (bands.length == 0) {
            return null;
        }
        final ColorRamp legend = new ColorRamp();
        legend.setColors(bands[0]);
        legend.setSize(dimension);
        /* TODO: since Constellation WMS is not aware of the dim_range parameter for
         * a GetLegendGraphic request, the label graduation is disabled in order to not
         * write wrong numbers.
         */
        legend.setLabelVisibles(false);
        return legend.toImage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MutableTreeNode getTree(final Locale locale) {
        final DefaultMutableTreeNode root = new TreeNode(this);
        for (final GridSampleDimension band : bands) {
            final List<Category> categories = band.getCategories();
            final int categoryCount = categories.size();
            final DefaultMutableTreeNode node = new TreeNode(band, locale);
            for (int j=0; j<categoryCount; j++) {
                node.add(new TreeNode(categories.get(j), locale));
            }
            root.add(node);
        }
        return root;
    }

    /**
     * Returns a character string representing this entry.
     */
    StringBuilder toString(final StringBuilder buffer) {
        return buffer.append(getName()).append(" (").append(formatName).append(')');
    }

    /**
     * Returns a character string representing this entry.
     */
    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder(40);
        buffer.append(Classes.getShortClassName(this)).append('[');
        return toString(buffer).append(']').toString();
    }

    /**
     * Indicates if this entry is identical to the specified object.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final FormatEntry that = (FormatEntry) object;
            return Utilities.equals(this.formatName,   that.formatName) &&
                      Arrays.equals(this.bands,        that.bands     ) &&
                                    this.geophysics == that.geophysics;
        }
        return false;
    }

    /**
     * Node appearing as a tree structure of formats and their bands.
     * This node redefines the method {@link #toString} to return a string
     * formatted better than <code>{@link #getUserObject}.toString()</code>.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    private static final class TreeNode extends DefaultMutableTreeNode {
        /**
         * For cross-version compatibility.
         */
        private static final long serialVersionUID = 9030373781984474394L;

        /**
         * The text returned by {@link #toString}.
         */
        private final String text;

        /**
         * Construct a node for the specified entries.
         */
        public TreeNode(final FormatEntry entry) {
            super(entry);
            text = String.valueOf(entry.toString(new StringBuilder()));
        }

        /**
         * Construct a node for the specified list. The constructor does not
         * scan the categories containes in the specified list.
         */
        public TreeNode(final GridSampleDimension band, final Locale locale) {
            super(band);
            text = band.getDescription().toString(locale);
        }

        /**
         * Constructs a node for the specified category.
         */
        public TreeNode(final Category category, final Locale locale) {
            super(category, false);
            final StringBuilder buffer = new StringBuilder();
            final NumberRange<?> range = category.geophysics(false).getRange();
            buffer.append('[');  append(buffer, range.getMinValue());
            buffer.append(".."); append(buffer, range.getMaxValue()); // Inclusive
            buffer.append("] ").append(category.getName());
            text = buffer.toString();
        }

        /**
         * Add a whole number using at least 3 digits (for example 007).
         */
        private static void append(final StringBuilder buffer, final Comparable<?> value) {
            final String number = String.valueOf(value);
            for (int i=3-number.length(); --i>=0;) {
                buffer.append('0');
            }
            buffer.append(number);
        }

        /**
         * Returns the text of this node.
         */
        @Override
        public String toString() {
            return text;
        }
    }
}
