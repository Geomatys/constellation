/*
 * Sicade - Syst�mes int�gr�s de connaissances pour l'aide � la d�cision en environnement
 * (C) 2005, Institut de Recherche pour le D�veloppement
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sicade.observation.coverage.sql;

// Images
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;

// Image I/O
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.IIOException;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.event.IIOReadWarningListener;
import javax.imageio.event.IIOReadProgressListener;

// Generic I/O
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URI;

// Other J2SE dependencies
import java.util.Map;
import java.util.List;
import java.util.Locale;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Collections;
import java.util.EventListener;
import java.util.IdentityHashMap;
import java.util.logging.LogRecord;
import java.util.logging.Level;
import java.awt.Dimension;

// Java Advanced Imaging
import javax.media.jai.JAI;
import javax.media.jai.util.Range;
import com.sun.media.imageio.stream.RawImageInputStream;

// Geotools dependencies
import org.geotools.coverage.Category;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.gui.swing.tree.MutableTreeNode;
import org.geotools.gui.swing.tree.DefaultMutableTreeNode;
import org.geotools.image.io.RawBinaryImageReadParam;
import org.geotools.image.io.IIOListeners;
import org.geotools.resources.Utilities;
import org.geotools.resources.XArray;

// Sicade dependencies
import net.sicade.observation.sql.Entry;
import net.sicade.observation.sql.Database;
import net.sicade.observation.coverage.Format;
import net.sicade.observation.coverage.CoverageReference;
import net.sicade.resources.seagis.Resources;
import net.sicade.resources.seagis.ResourceKeys;


/**
 * Impl�mentation d'une entr�e repr�sentant un {@linkplain FormatEntry format d'image}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class FormatEntry extends Entry implements Format {
    /**
     * Pour compatibilit�s entre les enregistrements binaires de diff�rentes versions.
     */
    private static final long serialVersionUID = -8790032968708208057L;

    /**
     * {@code true} pour utiliser l'op�ration {@code "ImageRead"} de JAI, ou {@code false}
     * pour utiliser directement l'objet {@link ImageReader}.
     */
    private static final boolean USE_IMAGE_READ_OPERATION = false;

    /**
     * Images en cours de lecture. Les cl�s sont les objets {@link CoverageReference} en attente
     * d'�tre lues, tandis que les valeurs sont {@link Boolean#TRUE} si la lecture est en cours,
     * ou {@link Boolean#FALSE} si elle est en attente.
     */
    private final transient Map<CoverageReference,Boolean> enqueued =
            Collections.synchronizedMap(new IdentityHashMap<CoverageReference,Boolean>());

    /**
     * Nom MIME du format lisant les images.
     */
    private final String mimeType;

    /**
     * Extension (sans le point) des noms de fichier des images � lire.
     */
    final String extension;

    /**
     * Liste des bandes appartenant � ce format. Les �l�ments de ce tableau doivent
     * correspondre dans l'ordre aux bandes {@code [0,1,2...]} de l'image.
     */
    private final GridSampleDimension[] bands;

    /**
     * {@code true} si les donn�es lues repr�senteront d�j� les valeurs du param�tre g�ophysique.
     */
    private final boolean geophysics;

    /**
     * Objet � utiliser pour lire des images de ce format. Cet objet ne sera cr�� que lors
     * du premier appel de {@link #read}, puis r�utilis� pour tous les appels subs�quents.
     */
    private transient ImageReader reader;

    /**
     * Construit une entr�e repr�sentant un format.
     *
     * @param name       Nom du format.
     * @param mimeType   Nom MIME du format (par exemple "image/png").
     * @param extension  Extension (sans le point) des noms de fichier (par exemple "png").
     * @param geophysics {@code true} si les donn�es lues repr�senteront d�j� les valeurs du param�tre g�ophysique.
     * @param bands      Listes des bandes apparaissant dans ce format.
     */
    protected FormatEntry(final String  name,
                          final String  mimeType,
                          final String  extension,
                          final boolean geophysics,
                          final GridSampleDimension[] bands)
    {
        super(name);
        this.mimeType   = mimeType.trim().intern();
        this.extension  = extension.trim().intern();
        this.geophysics = geophysics;
        this.bands      = bands;
        for (int i=0; i<bands.length; i++) {
            bands[i] = bands[i].geophysics(geophysics);
        }
    }

    /**
     * La langue � utiliser pour le d�codeur d'image, ou {@code null} pour la langue par d�faut.
     */
    private static Locale getLocale() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public final GridSampleDimension[] getSampleDimensions() {
        return getSampleDimensions(null);
    }

    /**
     * Retourne les bandes {@link GridSampleDimension} qui permettent de d�coder les valeurs des
     * param�tres g�ophysiques des images lues par cet objet. Cette m�thode peut retourner plusieurs
     * objets {@link GridSampleDimension}, un par bande. De fa�on optionnelle, on peut sp�cifier �
     * cette m�thode les param�tres {@link ImageReadParam} qui ont servit � lire une image
     * (c'est-�-dire les m�mes param�tres que ceux qui avaient �t� donn�s � {@link #read}). Cette
     * m�thode ne retournera alors que les listes de cat�gories pertinents pour les bandes lues.
     *
     * @param param Param�tres qui ont servit � lire l'image, ou {@code null} pour les param�tres par d�faut.
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
         * Recherche les objets 'GridSampleDimension' qui correspondent aux bandes sources
         * demand�es. Ces objets seront plac�s aux index des bandes de destination sp�cifi�es.
         */
        for (int j=0; j<bandCount; j++) {
            final int srcBand = (srcBands!=null) ? srcBands[j] : j;
            final int dstBand = (dstBands!=null) ? dstBands[j] : j;
            selectedBands[dstBand] = bands[srcBand];
        }
        return selectedBands;
    }

    /**
     * Retourne l'objet � utiliser pour lire des images. Le lecteur retourn� ne lira
     * que des images du format MIME sp�cifi� au constructeur. Les m�thodes qui appelent
     * {@code getImageReader} <u>doivent</u> appeler cette m�thode et utiliser l'objet
     * {@link ImageReader} retourn� � l'int�rieur d'un bloc synchronis� sur cet objet
     * {@code FormatEntry} (c'est-�-dire {@code this}).
     *
     * @return Le lecteur � utiliser pour lire les images de ce format.
     *         Cette m�thode ne retourne jamais {@code null}.
     * @throws IIOException s'il n'y a pas d'objet {@link ImageReader} pour ce format.
     */
    private ImageReader getImageReader() throws IIOException {
        assert Thread.holdsLock(this);
        if (reader != null) {
            return reader;
        }
        Iterator<ImageReader> readers;
        if (mimeType.length() != 0) {
            readers = ImageIO.getImageReadersByMIMEType(mimeType);
            if (readers.hasNext()) {
                return reader = readers.next();
            }
        }
        readers = ImageIO.getImageReadersByFormatName(extension);
        if (readers.hasNext()) {
            return reader = readers.next();
        }
        throw new IIOException(Resources.format(ResourceKeys.ERROR_NO_IMAGE_DECODER_$1, mimeType));
    }

    /**
     * Retourne un bloc de param�tres par d�faut pour le format courant. Cette m�thode n'est
     * appel�e que par {@link GridCoverageEntry#getCoverage}. Note: cette m�thode
     * <strong>doit</strong> �tre appel�e � partir d'un bloc synchronis� sur {@code this}.
     *
     * @return Un bloc de param�tres par d�faut. Cette m�thode ne retourne jamais {@code null}.
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
    private static <T> boolean contains(final Class<Object>[] array, final Class<T> item) {
        for (final Class<Object> c : array) {
            if (c.isAssignableFrom(item)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Convertit l'objet {@code input} sp�cifi� en un des types sp�cifi�s dans le
     * tableau {@code inputTypes}. Si la conversion ne peut pas �tre effectu�e,
     * alors cette m�thode retourne {@code null}.
     */
    private static Object getInput(final Object file, final Class<Object>[] inputTypes) {
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
                return ((File) file).toURL();
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
     * Proc�de � la lecture d'une image. Il est possible que l'image soit lue non pas
     * localement, mais plut�t � travers un r�seau. Cette m�thode n'est appel�e que par
     * {@link GridCoverageEntry#getCoverage}.
     * <p>
     * Note 1: cette m�thode <strong>doit</strong> �tre appel�e � partir d'un bloc
     * synchronis� sur {@code this}.
     * <p>
     * Note 2: La m�thode {@link #setReading} <strong>doit</strong> �tre appel�e
     *         avant et apr�s cette m�thode dans un bloc {@code try...finally}.
     *
     *
     * @param  file Fichier � lire. Habituellement un objet {@link File}, {@link URL} ou {@link URI}.
     * @param  imageIndex Index (� partir de 0) de l'image � lire.
     * @param  param Bloc de param�tre � utiliser pour la lecture.
     * @param  listeners Objets � informer des progr�s de la lecture ainsi que des �ventuels
     *         avertissements, ou {@code null} s'il n'y en a pas. Les objets qui ne sont
     *         pas de la classe {@link IIOReadWarningListener} ou {@link IIOReadProgressListener}
     *         ne seront pas pris en compte.
     * @param  expected Dimension pr�vue de l'image.
     * @param  source Objet {@link CoverageReference} qui a demand� la lecture de l'image.
     *         Cette information sera utilis�e par {@link #abort} pour v�rifier si
     *         un l'objet {@link CoverageReference} qui demande l'annulation est celui qui
     *         est en train de lire l'image.
     * @return Image lue, ou {@code null} si la lecture de l'image a �t� annul�e.
     * @throws IOException si une erreur est survenue lors de la lecture.
     */
    @SuppressWarnings("unchecked")
    final RenderedImage read(final Object         file,
                             final int            imageIndex,
                             final ImageReadParam param,
                             final IIOListeners   listeners,
                             final Dimension      expected,
                             final CoverageReference  source) throws IOException
    {
        assert Thread.holdsLock(this);
        RenderedImage    image       = null;
        ImageInputStream inputStream = null;
        Object           inputObject;
        /*
         * Obtient l'objet � utiliser comme source. Autant que possible,  on
         * essaira de donner un objet de type 'File' ou 'URL', ce qui permet
         * au d�codeur d'utiliser la connection la plus appropri�e pour eux.
         */
        final ImageReader reader = getImageReader();
        final ImageReaderSpi spi = reader.getOriginatingProvider();
        final Class<Object>[] inputTypes = (spi!=null) ? spi.getInputTypes() : ImageReaderSpi.STANDARD_INPUT_TYPE;
        inputObject = getInput(file, inputTypes);
        if (inputObject == null) {
            inputObject = inputStream = ImageIO.createImageInputStream(file);
            if (inputObject == null) {
                throw new FileNotFoundException(Resources.format(
                        ResourceKeys.ERROR_FILE_NOT_FOUND_$1, getPath(file)));
            }
        }
        /*
         * Si l'image � lire est au format "RAW", d�finit la taille de l'image.  C'est
         * n�cessaire puisque le format binaire RAW ne contient aucune information sur
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
        // Patch temporaire, en attendant que les d�codeurs sp�ciaux (e.g. "image/raw-msla")
        // soient adapt�s � l'architecture du d�codeur RAW de Sun.
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
         * Configure maintenant le d�codeur et lance la lecture de l'image.
         * Cette �tape existe en deux versions: avec utilisation de l'op�ration
         * "ImageRead", ou lecture directe � partir du ImageReader.
         */
        if (USE_IMAGE_READ_OPERATION) {
            /*
             * Utilisation de l'op�ration "ImageRead": cette approche retarde la lecture des
             * tuiles � un moment ind�termin� apr�s l'appel de cette m�thode. Elle a l'avantage
             * de contr�ler la m�moire consomm�e gr�ce au TileCache de JAI, Mais elle rend plus
             * difficile la gestion des exceptions et l'annulation de la lecture avec 'abort()',
             * ce qui rend caduc la queue 'enqueued'.
             */
            image = JAI.create("ImageRead", new ParameterBlock()
                .add(inputObject)                  // Objet � utiliser en entr�
                .add(imageIndex)                   // Index de l'image � lire
                .add(Boolean.FALSE)                // Pas de lecture des m�ta-donn�es
                .add(Boolean.FALSE)                // Pas de lecture des "thumbnails"
                .add(Boolean.TRUE)                 // V�rifier la validit� de "input"
                .add(listeners.getReadListeners()) // Liste des "listener"
                .add(getLocale())                  // Langue du d�codeur
                .add(param)                        // Les param�tres
                .add(reader));                     // L'objet � utiliser pour la lecture.
            this.reader = null;                    // N'utilise qu'un ImageReader par op�ration.
        } else try {
            /*
             * Utilisation direct du 'ImageReader': cette approche lit l'image imm�diatement,
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
             * Read the file, close it in the "finally" block and returns the image.
             * The reading will not be performed if the user aborted it before we reach
             * this point.
             */
            if (enqueued.put(source, Boolean.TRUE) != null) try {
                image = reader.readAsRenderedImage(imageIndex, param);
            } catch (OutOfMemoryError e) {
                System.gc();
                System.runFinalization();
                System.gc();
                image = reader.readAsRenderedImage(imageIndex, param);
            }
        } finally {
            if (enqueued.remove(source) == null) {
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
            if (enqueued.put(source, Boolean.FALSE) != null) {
                throw new AssertionError();
            }
        } else {
            enqueued.remove(source);
        }
    }

    /**
     * Annule la lecture de l'image en appelant {@link ImageReader#abort}.
     * Cette m�thode peut �tre appel�e � partir de n'importe quel thread.
     *
     * @param source Objet qui appelle cette m�thode.
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
            Format.LOGGER.log(record);
        }
    }

    /**
     * V�rifie que la taille de l'image a bien la taille qui �tait d�clar�e
     * dans la base de donn�es. Cette v�rification sert uniquement � tenter
     * d'intercepter d'�ventuelles erreurs qui se serait gliss�es dans la
     * base de donn�es et/ou la copie d'images sur le disque.
     *
     * @param  imageWidth   Largeur de l'image.
     * @param  imageHeight  Hauteur de l'image.
     * @param  expected     Largeur et hauteur attendues.
     * @param  file         Nom du fichier de l'image � lire.
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
    public MutableTreeNode getTree(final Locale locale) {
        final DefaultMutableTreeNode root = new TreeNode(this);
        for (final GridSampleDimension band : bands) {
            final List             categories = band.getCategories();
            final int           categoryCount = categories.size();
            final DefaultMutableTreeNode node = new TreeNode(band, locale);
            for (int j=0; j<categoryCount; j++) {
                node.add(new TreeNode((Category)categories.get(j), locale));
            }
            root.add(node);
        }
        return root;
    }

    /**
     * Retourne une cha�ne de caract�res repr�sentant cette entr�e.
     */
    final StringBuilder toString(final StringBuilder buffer) {
        buffer.append(getName());
        buffer.append(" (");
        buffer.append(mimeType);
        buffer.append(')');
        return buffer;
    }

    /**
     * Retourne une cha�ne de caract�res repr�sentant cette entr�e.
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder(40);
        buffer.append(Utilities.getShortClassName(this));
        buffer.append('[');
        buffer = toString(buffer);
        buffer.append(']');
        return buffer.toString();
    }

    /**
     * Indique si cette entr�e est identique � l'entr�e sp�cifi�e.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final FormatEntry that = (FormatEntry) object;
            return Utilities.equals(this.mimeType,   that.mimeType )  &&
                   Utilities.equals(this.extension,  that.extension)  &&
                      Arrays.equals(this.bands,      that.bands    )  &&
                                    this.geophysics==that.geophysics;
        }
        return false;
    }

    /**
     * Noeud apparaissant dans l'arborescence des formats et de leurs bandes.
     * Ce noeud red�finit la m�thode {@link #toString} pour retourner une cha�ne
     * adapt�e plut�t que <code>{@link #getUserObject}.toString()</code>.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    private static final class TreeNode extends DefaultMutableTreeNode {
        /**
         * Le texte � retourner par {@link #toString}.
         */
        private final String text;

        /**
         * Construit un noeud pour l'entr�e sp�cifi�e.
         */
        public TreeNode(final FormatEntry entry) {
            super(entry);
            text = String.valueOf(entry.toString(new StringBuilder()));
        }

        /**
         * Construit un noeud pour la liste sp�cifi�e. Ce constructeur ne
         * balaie pas les cat�gories contenues dans la liste sp�cifi�e.
         */
        public TreeNode(final GridSampleDimension band, final Locale locale) {
            super(band);
            text = band.getDescription().toString(locale);
        }

        /**
         * Construit un noeud pour la cat�gorie sp�cifi�e.
         */
        public TreeNode(final Category category, final Locale locale) {
            super(category, false);
            final StringBuilder buffer = new StringBuilder();
            final Range range = category.geophysics(false).getRange();
            buffer.append('[');  append(buffer, range.getMinValue());
            buffer.append(".."); append(buffer, range.getMaxValue()); // Inclusive
            buffer.append("] ");
            buffer.append(category.getName());
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
