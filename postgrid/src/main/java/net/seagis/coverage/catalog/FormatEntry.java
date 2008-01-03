/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
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
package net.seagis.coverage.catalog;

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
import java.util.Map;
import java.util.List;
import java.util.Locale;
import java.util.Arrays;
import java.util.Iterator;
import java.util.IdentityHashMap;
import java.util.logging.LogRecord;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.IIOException;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.event.IIOReadWarningListener;
import javax.imageio.event.IIOReadProgressListener;
import javax.media.jai.JAI;
import javax.media.jai.util.Range;
import com.sun.media.imageio.stream.RawImageInputStream;

import org.geotools.util.MeasurementRange;
import org.geotools.coverage.Category;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.gui.swing.image.ColorRamp;
import org.geotools.gui.swing.tree.MutableTreeNode;
import org.geotools.gui.swing.tree.DefaultMutableTreeNode;
import org.geotools.image.io.IIOListeners;
import org.geotools.image.io.RawBinaryImageReadParam;
import org.geotools.image.io.netcdf.NetcdfImageReader;
import org.geotools.image.io.mosaic.MosaicImageReader;
import org.geotools.resources.Utilities;
import org.geotools.resources.Classes;

import net.seagis.catalog.Entry;
import net.seagis.resources.i18n.Resources;
import net.seagis.resources.i18n.ResourceKeys;


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
    private static final boolean USE_IMAGE_READ_OPERATION = false;

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
     * Format mime type as declared in the database.
     */
    private final String mimeType;

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
     * @param mimeType   Format MIME type (example: {@code "image/png"}).
     * @param extension  Filename extension, excluding dot separator (example: {@code "png"}).
     * @param geophysics {@code true} if coverage to be read are already geophysics values.
     * @param bands      Sample dimensions for coverages encoded with this format.
     */
    protected FormatEntry(final String  name,
                          final String  mimeType,
                          final boolean geophysics,
                          final GridSampleDimension[] bands)
    {
        super(name);
        this.mimeType   = mimeType.trim().intern();
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
    public String getMimeType() {
        return mimeType;
    }

    /**
     * {@inheritDoc}
     */
    public MeasurementRange[] getSampleValueRanges() {
        final GridSampleDimension[] bands = getSampleDimensions();
        final MeasurementRange[] ranges = new MeasurementRange[bands.length];
        for (int i=0; i<ranges.length; i++) {
            final GridSampleDimension band = bands[i].geophysics(true);
            ranges[i] = new MeasurementRange(band.getMinimumValue(), band.getMaximumValue(), band.getUnits());
        }
        return ranges;
    }

    /**
     * {@inheritDoc}
     */
    public final GridSampleDimension[] getSampleDimensions() {
        return getSampleDimensions(null);
    }

    /**
     * Returns the sample dimensions for coverages encoded with this format. If parameters are
     * supplied, this method returns only the sample dimensions for supplied source bands list
     * and returns them in the order inferred from the destination bands list.
     */
    final GridSampleDimension[] getSampleDimensions(final ImageReadParam param) {
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
    final ImageReaderSpi getImageReaderSpi() throws IIOException {
        if (provider == null) {
            // We don't synchronize above this point because it is not a big
            // deal if two instances are created.
            synchronized (this) {
                provider = new ImageReaderSpiDecorator(getImageReader().getOriginatingProvider()) {
                    @Override
                    public ImageReader createReaderInstance() throws IOException {
                        final ImageReader reader = super.createReaderInstance();
                        handleSpecialCases(reader, null);
                        return reader;
                    }

                    @Override
                    public ImageReader createReaderInstance(final Object extension) throws IOException {
                        final ImageReader reader = super.createReaderInstance(extension);
                        handleSpecialCases(reader, null);
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
            final Iterator<ImageReader> readers = ImageIO.getImageReadersByMIMEType(mimeType);
            if (!readers.hasNext()) {
                throw new IIOException(Resources.format(ResourceKeys.ERROR_NO_IMAGE_DECODER_$1, mimeType));
            }
            reader = readers.next();
            if (false && readers.hasNext()) { // Check disabled for now.
                throw new IIOException(Resources.format(
                        ResourceKeys.ERROR_TOO_MANY_IMAGE_FORMATS_$1, mimeType));
            }
            handleSpecialCases(reader, null);
        }
        return reader;
    }

    /**
     * Handles special cases for image reader configuration.
     */
    private void handleSpecialCases(final ImageReader reader, final ImageReadParam param) {
        if (reader instanceof NetcdfImageReader) {
            final NetcdfImageReader r = (NetcdfImageReader) reader;
            final GridSampleDimension[] bands = getSampleDimensions(param);
            final String[] names = new String[bands.length];
            for (int i=0; i<names.length; i++) {
                names[i] = bands[i].getDescription().toString();
            }
            r.setVariables(names);
        }
    }

    /**
     * Retourne un bloc de paramètres par défaut pour le format courant. Cette méthode n'est
     * appelée que par {@link GridCoverageEntry#getCoverage}. Note: cette méthode
     * <strong>doit</strong> être appelée à partir d'un bloc synchronisé sur {@code this}.
     *
     * @return Un bloc de paramètres par défaut. Cette méthode ne retourne jamais {@code null}.
     * @throws IIOException s'il n'y a pas d'objet {@link ImageReader} pour ce format.
     */
    final ImageReadParam getDefaultReadParam() throws IIOException {
        assert Thread.holdsLock(this);
        return getImageReader().getDefaultReadParam();
    }

    /**
     * Indique si le tableau {@code array} contient au moins un
     * exemplaire de la classe {@code item} ou d'une super-classe.
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
     * Convertit l'objet {@code input} spécifié en un des types spécifiés dans le
     * tableau {@code inputTypes}. Si la conversion ne peut pas être effectuée,
     * alors cette méthode retourne {@code null}.
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
            // Ignore... Le code suivant sera un "fallback" raisonable.
        }
        if (contains(inputTypes, URL.class)) try {
            if (file instanceof File) {
                return ((File) file).toURI().toURL();
            }
            if (file instanceof URI) {
                return ((URI) file).toURL();
            }
        } catch (MalformedURLException exception) {
            // Ignore... Le code suivant sera un "fallback" raisonable.
        }
        if (contains(inputTypes, URI.class)) try {
            if (file instanceof File) {
                return ((File) file).toURI();
            }
            if (file instanceof URL) {
                return ((URL) file).toURI();
            }
        } catch (URISyntaxException exception) {
            // Ignore... Le code suivant sera un "fallback" raisonable.
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
     * @param  file Fichier à lire. Habituellement un objet {@link File}, {@link URL} ou {@link URI}.
     * @param  imageIndex Index (à partir de 0) de l'image à lire.
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
    final RenderedImage read(final Object         file,
                             final int            imageIndex,
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
        if (contains(MOSAIC_INPUT_TYPES, file.getClass())) {
            spi = MosaicImageReader.Spi.DEFAULT;
            reader = spi.createReaderInstance();
            inputObject = file;
        } else {
            reader = getImageReader();
            spi = reader.getOriginatingProvider();
            final Class<?>[] inputTypes = (spi!=null) ? spi.getInputTypes() : ImageReaderSpi.STANDARD_INPUT_TYPE;
            inputObject = getInput(file, inputTypes);
            if (inputObject == null) {
                inputObject = inputStream = ImageIO.createImageInputStream(file);
                if (inputObject == null) {
                    throw new FileNotFoundException(Resources.format(
                            ResourceKeys.ERROR_FILE_NOT_FOUND_$1, getPath(file)));
                }
            }
            /*
             * Si l'image à lire est au format "RAW", définit la taille de l'image.  C'est
             * nécessaire puisque le format binaire RAW ne contient aucune information sur
             * la taille des images qu'elle contient.
             */
            if (inputStream!=null && contains(inputTypes, RawImageInputStream.class)) {
                final GridSampleDimension[] bands = getSampleDimensions(param);
                final ColorModel  cm = bands[0].getColorModel(0, bands.length);
                final SampleModel sm = cm.createCompatibleSampleModel(expected.width, expected.height);
                inputObject = inputStream = new RawImageInputStream(inputStream,
                                                                    new ImageTypeSpecifier(cm, sm),
                                                                    new long[]{0},
                                                                    new Dimension[]{expected});
            }
        }
        // Patch temporaire, en attendant que les décodeurs spéciaux (e.g. "image/raw-msla")
        // soient adaptés à l'architecture du décodeur RAW de Sun.
        if (param instanceof RawBinaryImageReadParam) {
            final RawBinaryImageReadParam rawParam = (RawBinaryImageReadParam) param;
            if (rawParam.getStreamImageSize() == null) {
                rawParam.setStreamImageSize(expected);
            }
            if (geophysics && rawParam.getDestinationType() == null) {
                final int dataType = rawParam.getStreamDataType();
                if (dataType != DataBuffer.TYPE_FLOAT &&
                    dataType != DataBuffer.TYPE_DOUBLE)
                {
                    rawParam.setDestinationType(DataBuffer.TYPE_FLOAT);
                }
            }
        }
        /*
         * Configure maintenant le décodeur et lance la lecture de l'image.
         * Cette étape existe en deux versions: avec utilisation de l'opération
         * "ImageRead", ou lecture directe à partir du ImageReader.
         */
        handleSpecialCases(reader, param);
        if (USE_IMAGE_READ_OPERATION) {
            /*
             * Utilisation de l'opération "ImageRead": cette approche retarde la lecture des
             * tuiles à un moment indéterminé après l'appel de cette méthode. Elle a l'avantage
             * de contrôler la mémoire consommée grâce au TileCache de JAI, Mais elle rend plus
             * difficile la gestion des exceptions et l'annulation de la lecture avec 'abort()',
             * ce qui rend caduc la queue 'enqueued'.
             */
            image = JAI.create("ImageRead", new ParameterBlock()
                .add(inputObject)                  // Objet à utiliser en entré
                .add(imageIndex)                   // Index de l'image à lire
                .add(Boolean.FALSE)                // Pas de lecture des méta-données
                .add(Boolean.FALSE)                // Pas de lecture des "thumbnails"
                .add(Boolean.TRUE)                 // Vérifier la validité de "input"
                .add(listeners.getReadListeners()) // Liste des "listener"
                .add(getLocale())                  // Langue du décodeur
                .add(param)                        // Les paramètres
                .add(reader));                     // L'objet à utiliser pour la lecture.
            this.reader = null;                    // N'utilise qu'un ImageReader par opération.
        } else try {
            /*
             * Utilisation direct du 'ImageReader': cette approche lit l'image immédiatement,
             * ce qui facilite la gestion des exceptions, de l'anulation de la lecture avec
             * 'abort()' et les synchronisations.
             */
            if (listeners != null) {
                listeners.addListenersTo(reader);
            }
            reader.setLocale(getLocale());
            reader.setInput(inputObject, true, true);
            if (!(param instanceof RawBinaryImageReadParam)) {
                checkSize(reader.getWidth(imageIndex), reader.getHeight(imageIndex), expected, file);
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
    final void setReading(final CoverageReference source, final boolean starting) {
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
     * Annule la lecture de l'image en appelant {@link ImageReader#abort}.
     * Cette méthode peut être appelée à partir de n'importe quel thread.
     *
     * @param source Objet qui appelle cette méthode.
     */
    final void abort(final CoverageReference source) {
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
            String name = source.getName();
            final LogRecord record = Resources.getResources(null).getLogRecord(Level.FINE,
                        ResourceKeys.ABORT_IMAGE_READING_$2, name,
                        new Integer(active.booleanValue() ? 1 : 0));
            record.setSourceClassName("CoverageReference");
            record.setSourceMethodName("abort");
            LOGGER.log(record);
        }
    }

    /**
     * Vérifie que la taille de l'image a bien la taille qui était déclarée
     * dans la base de données. Cette vérification sert uniquement à tenter
     * d'intercepter d'éventuelles erreurs qui se serait glissées dans la
     * base de données et/ou la copie d'images sur le disque.
     *
     * @param  imageWidth   Largeur de l'image.
     * @param  imageHeight  Hauteur de l'image.
     * @param  expected     Largeur et hauteur attendues.
     * @param  file         Nom du fichier de l'image à lire.
     * @throws IIOException si l'image n'a pas la largeur et hauteur attendue.
     */
    private static void checkSize(final int imageWidth, final int imageHeight,
                                  final Dimension expected, final Object file)
        throws IIOException
    {
        if (expected.width!=imageWidth || expected.height!=imageHeight) {
            throw new IIOException(Resources.format(ResourceKeys.ERROR_IMAGE_SIZE_MISMATCH_$5, getPath(file),
                                   new Integer(    imageWidth), new Integer(    imageHeight),
                                   new Integer(expected.width), new Integer(expected.height)));
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
    public BufferedImage getLegend(final Dimension dimension) {
        final GridSampleDimension band = getSampleDimensions()[0];
        final ColorRamp legend = new ColorRamp();
        legend.setColors(band);
        legend.setSize(dimension);
        return legend.toImage();
    }

    /**
     * {@inheritDoc}
     */
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
     * Retourne une chaîne de caractères représentant cette entrée.
     */
    final StringBuilder toString(final StringBuilder buffer) {
        return buffer.append(getName()).append(" (").append(mimeType).append(')');
    }

    /**
     * Retourne une chaîne de caractères représentant cette entrée.
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder(40);
        buffer.append(Classes.getShortClassName(this)).append('[');
        return toString(buffer).append(']').toString();
    }

    /**
     * Indique si cette entrée est identique à l'entrée spécifiée.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final FormatEntry that = (FormatEntry) object;
            return Utilities.equals(this.mimeType,     that.mimeType )  &&
                      Arrays.equals(this.bands,        that.bands    )  &&
                                    this.geophysics == that.geophysics;
        }
        return false;
    }

    /**
     * Noeud apparaissant dans l'arborescence des formats et de leurs bandes.
     * Ce noeud redéfinit la méthode {@link #toString} pour retourner une chaîne
     * adaptée plutôt que <code>{@link #getUserObject}.toString()</code>.
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
         * Le texte à retourner par {@link #toString}.
         */
        private final String text;

        /**
         * Construit un noeud pour l'entrée spécifiée.
         */
        public TreeNode(final FormatEntry entry) {
            super(entry);
            text = String.valueOf(entry.toString(new StringBuilder()));
        }

        /**
         * Construit un noeud pour la liste spécifiée. Ce constructeur ne
         * balaie pas les catégories contenues dans la liste spécifiée.
         */
        public TreeNode(final GridSampleDimension band, final Locale locale) {
            super(band);
            text = band.getDescription().toString(locale);
        }

        /**
         * Construit un noeud pour la catégorie spécifiée.
         */
        public TreeNode(final Category category, final Locale locale) {
            super(category, false);
            final StringBuilder buffer = new StringBuilder();
            final Range range = category.geophysics(false).getRange();
            buffer.append('[');  append(buffer, range.getMinValue());
            buffer.append(".."); append(buffer, range.getMaxValue()); // Inclusive
            buffer.append("] ").append(category.getName());
            text = buffer.toString();
        }

        /**
         * Ajoute un entier utilisant au moins 3 chiffres (par exemple 007).
         */
        private static void append(final StringBuilder buffer, final Comparable value) {
            final String number = String.valueOf(value);
            for (int i=3-number.length(); --i>=0;) {
                buffer.append('0');
            }
            buffer.append(number);
        }

        /**
         * Retourne le texte de ce noeud.
         */
        @Override
        public String toString() {
            return text;
        }
    }
}
