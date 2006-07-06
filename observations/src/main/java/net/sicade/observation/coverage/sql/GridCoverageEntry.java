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

// Entr�s/sorties et bases de donn�es
import java.io.File;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import javax.imageio.IIOException;
import javax.imageio.ImageReadParam;
import java.rmi.server.RemoteStub;
import java.rmi.RemoteException;
import java.sql.SQLException;

// Geom�tries
import java.awt.Point;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Dimension2D;

// R�f�rences faibles
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.ref.SoftReference;

// Divers
import java.util.Map;
import java.util.Date;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Collections;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import static java.lang.Math.*;

// OpenGIS
import org.opengis.coverage.SampleDimension;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.opengis.metadata.extent.GeographicBoundingBox;

// Geotools
import org.geotools.image.io.IIOListeners;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.referencing.crs.DefaultTemporalCRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;

// Geotools (resources)
import org.geotools.util.NumberRange;
import org.geotools.util.WeakHashSet;
import org.geotools.resources.Utilities;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.geometry.XRectangle2D;

// Seagis
import net.sicade.util.DateRange;
import net.sicade.observation.sql.Entry;
import net.sicade.observation.coverage.Series;
import net.sicade.observation.coverage.Format;
import net.sicade.observation.coverage.Operation;
import net.sicade.observation.coverage.CoverageReference;
import net.sicade.observation.coverage.rmi.RemoteLoader;
import net.sicade.observation.coverage.rmi.CoverageLoader;
import net.sicade.observation.IllegalRecordException;
import net.sicade.observation.CatalogException;
import static net.sicade.observation.coverage.CoverageBuilder.FACTORY;


/**
 * Impl�mentation d'une entr�e repr�sentant une {@linkplain CoverageReference r�f�rence vers une
 * image}. Un objet {@code GridCoverageEntry} correspond � un enregistrement de la base de donn�es
 * d'images. Chaque instance est imutable et s�curitaire dans un environnement multi-threads.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class GridCoverageEntry extends Entry implements CoverageReference, CoverageLoader {
    /**
     * Compare deux entr�es selon le m�me crit�re que celui qui apparait dans l'instruction
     * {@code "ORDER BY"} de la r�qu�te SQL de {@link GridCoverageTable}). Les entr�s sans
     * dates sont une exception: elles sont consid�r�es comme non-ordonn�es.
     */
    final boolean compare(final GridCoverageEntry other) {
        if (startTime==Long.MIN_VALUE && endTime==Long.MAX_VALUE) {
            return false;
        }
        return endTime == other.endTime;
    }

    /**
     * Pour compatibilit�s entre les enregistrements binaires de diff�rentes versions.
     */
    private static final long serialVersionUID = -5725249398707248625L;

    /**
     * Ensemble des entr�es qui ont d�j� �t� retourn�es par {@link #canonicalize()} et qui n'ont pas
     * encore �t� r�clam�es par le ramasse-miettes. La classe {@link GridCoverageTable} tentera autant
     * que possible de retourner des entr�es qui existent d�j� en m�moire afin de leur donner une chance
     * de faire un meilleur travail de cache sur les images.
     */
    private static final WeakHashSet POOL = new WeakHashSet();

    /**
     * Liste des derniers {@link GridCoverageEntry} pour lesquels la m�thode {@link #getCoverage}
     * a �t� appel�e. Lorsqu'une nouvelle image est lue, les r�f�rences molles les plus anciennes
     * sont chang�es en r�f�rences faibles afin d'augmenter les chances que le ramasse-miette se
     * d�barasse des images les plus anciennes avant que la m�moire ne sature.
     */
    private static final LinkedList<GridCoverageEntry> LAST_INVOKED = new LinkedList<GridCoverageEntry>();

    /**
     * Quantit� maximale de m�moire (en octets) que l'on autorise pour l'ensemble des images
     * �num�r�es dans {@link #LAST_INVOKED}.  Si cette quantit� de m�moire est d�pass�e, les
     * images les plus anciennes seront retir�es de la liste {@link #LAST_INVOKED} jusqu'� ce
     * qu'elle soit ramen�e en dessous de cette limite.
     */
    private static final long MAX_MEMORY_USAGE = 128L * 1024 * 1024;

    /**
     * Somme de {@link #memoryUsage} pour toutes les images de la liste {@link #LAST_INVOKED}.
     */
    private static long lastInvokedMemoryUsage;

    /**
     * Petite valeur utilis�e pour contourner les erreurs d'arrondissement.
     */
    private static final double EPS = 1E-6;

    /**
     * Largeur et hauteur minimale des images, en pixels. Si l'utilisateur demande une r�gion plus
     * petite, la r�gion demand�e sera agrandie pour que l'image fasse cette taille.
     */
    private static final int MIN_SIZE = 64;

    /** Nom du fichier.                  */ private final String      filename;
    /** Date du d�but de l'acquisition.  */ private final long        startTime;
    /** Date de la fin de l'acquisition. */ private final long        endTime;
    /** Envelope g�ographique.           */ private final Rectangle2D boundingBox;
    /** Nombre de pixels en largeur.     */ private final short       width;
    /** Nombre de pixels en hauteur.     */ private final short       height;

    /**
     * Bloc de param�tres de la table d'images. On retient ce bloc de param�tres plut�t qu'une
     * r�f�rence directe vers {@link GridCoverageTable} afin de ne pas emp�cher le ramasse-miettes
     * de d�truire la table et ses connections vers la base de donn�es.
     */
    private final Parameters parameters;

    /**
     * Un d�codeur sur lequel d�l�guer le chargement des images, ou {@code null} pour le lire
     * directement avec cette entr�e. Dans ce dernier cas, l'image sera typiquement rapatri�e
     * par FTP.
     */
    private CoverageLoader loader;

    /**
     * R�f�rence molle vers l'image {@link GridCoverage2D} qui a �t� retourn�e lors du dernier appel
     * de {@link #getCoverage}. Cette r�f�rence est retenue afin d'�viter de charger inutilement une
     * autre fois l'image si elle est d�j� en m�moire.
     */
    private transient Reference<GridCoverage2D> gridCoverage;

    /**
     * R�f�rence molle vers l'image {@link RenderedImage} qui a �t� retourn�e lors du dernier appel
     * de {@link #getCoverage}. Cette r�f�rence est retenue afin d'�viter de charger inutilement une
     * autre fois l'image si elle est d�j� en m�moire.
     */
    private transient Reference<RenderedImage> renderedImage;

    /**
     * Quantit� de m�moire utilis�e par les pixels de {@link #renderedImage}. Seuls les pixels
     * sont pris en compte; l'espace occup� par l'objet lui-m�me n'est pas mesur�. La valeur 0
     * indique que {@link #renderedImage} n'a pas encore �t� lue.
     */
    private transient int memoryUsage;

    /**
     * Construit une entr� contenant des informations sur une image. Un {@linkplain #getName nom unique}
     * sera construit � partir de la sous-s�rie et du nom de fichiers (les colonnes {@code subseries} et
     * {@code filename}, qui constituent habituellement la cl� primaire de la table).
     * <p>
     * <strong>NOTE:</strong> Les coordonn�es {@code xmin}, {@code xmax}, {@code ymin} et {@code ymax}
     * ne sont <u>pas</u> exprim�es selon le syst�me de coordonn�es de l'image, mais plut�t selon le
     * syst�me de coordonn�es de la table d'images ({@code table}). La transformation sera effectu�e
     * par {@link #getEnvelope()} � la vol�.
     *
     * @param  table Table d'o� proviennent les enregistrements.
     * @throws CatalogException si des arguments sont invalides.
     * @throws SQLException si une erreur est survenue lors de l'acc�s � la base de donn�es.
     */
    protected GridCoverageEntry(final GridCoverageTable table,
                                final String            series,
                                final String            subseries,
                                final String            pathname,
                                final String            filename,
                                final Date              startTime,
                                final Date              endTime,
                                final double            xmin,
                                final double            xmax,
                                final double            ymin,
                                final double            ymax,
                                final short             width,
                                final short             height,
                                final String            crs,
                                final String            format,
                                final String            remarks)
            throws CatalogException, SQLException
    {
        super(subseries.trim() + ':' + filename, remarks);
        this.filename   = filename;
        this.width      = width;
        this.height     = height;
        this.parameters = table.getParameters(series, format, crs, pathname);
        this.startTime  = (startTime!=null) ? startTime.getTime() : Long.MIN_VALUE;
        this.  endTime  = (  endTime!=null) ?   endTime.getTime() : Long.MAX_VALUE;
        final XRectangle2D box = XRectangle2D.createFromExtremums(xmin, ymin, xmax, ymax);
        if (box.isEmpty() || this.startTime >= this.endTime) {
            // TODO: localize
            throw new IllegalRecordException(null, "L'enveloppe spatio-temporelle est vide.");
        }
        boundingBox = (Rectangle2D) POOL.canonicalize(box);
    }

    /**
     * Retourne un exemplaire unique de cette entr�e. Une banque d'entr�es, initialement vide, est
     * maintenue de fa�on interne par la classe {@code GridCoverageEntry}. Lorsque la m�thode
     * {@code canonicalize} est appel�e, elle recherchera une entr�e �gale � {@code this} au
     * sens de la m�thode {@link #equals}. Si une telle entr�e est trouv�e, elle sera retourn�e.
     * Sinon, l'entr�e {@code this} sera ajout�e � la banque de donn�es en utilisant une
     * {@linkplain WeakReference r�f�rence faible} et cette m�thode retournera {@code this}.
     * <p>
     * De cette m�thode il s'ensuit que pour deux entr�es <var>u</var> et <var>v</var>,
     * la condition {@code u.canonicalize()==v.canonicalize()} sera vrai si et seulement
     * si {@code u.equals(v)} est vrai.
     */
    public GridCoverageEntry canonicalize() {
        return (GridCoverageEntry) POOL.canonicalize(this);
    }

    /**
     * {@inheritDoc}
     */
    public Series getSeries() {
        return parameters.series;
    }

    /**
     * {@inheritDoc}
     */
    public Format getFormat() {
        return parameters.format;
    }

    /**
     * {@inheritDoc}
     */
    public File getFile() {
        try {
            final Object input = getInput(true);
            if (input instanceof File) {
                return (File) input;
            }
        } catch (IOException exception) {
            Utilities.unexpectedException(LOGGER.getName(), "GridCoverageEntry", "getFile", exception);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public URL getURL() {
        try {
            final Object input = getInput(false);
            if (input instanceof URL) {
                return (URL) input;
            }
        } catch (IOException exception) {
            Utilities.unexpectedException(LOGGER.getName(), "GridCoverageEntry", "getURL", exception);
        }
        return null;
    }

    /**
     * Returns the source as a {@link File} or an {@link URL}, in this preference order.
     *
     * @param  local {@code true} if the file are going to be read from a local machine,
     *         or {@code false} if it is going to be read through internet.
     * @return The input, usually a {@link File} object if {@code local} was {@code true}
     *         and an {@link URL} object if {@code local} was {@code false}.
     */
    private Object getInput(final boolean local) throws IOException {
        final File file = new File(parameters.pathname, filename+'.'+parameters.format.extension);
        if (!file.isAbsolute()) {
            if (local) {
                if (parameters.rootDirectory != null) {
                    if (!(loader instanceof RemoteStub)) {
                        return new File(parameters.rootDirectory, file.getPath());
                    }
                    // File loading delegated to a remote server.
                }
                // No root directory: means that the file is not accessible localy.
            }
            if (parameters.rootURL != null) {
                final StringBuilder buffer = new StringBuilder(parameters.rootURL);
                final int last = buffer.length()-1;
                if (last >= 0) {
                    if (buffer.charAt(last) == '/') {
                        buffer.setLength(last);
                    }
                }
                encodeURL(file, buffer, parameters.encoding);
                return new URL(buffer.toString());
            }
        }
        return (local) ? file : file.toURL();
    }

    /**
     * Transforme un chemin en URL. Si {@code encoding} est non-nul, alors le chemin est encod�.
     */
    private static void encodeURL(final File path, final StringBuilder buffer, final String encoding)
            throws UnsupportedEncodingException
    {
        final File parent = path.getParentFile();
        if (parent != null) {
            encodeURL(parent, buffer, encoding);
        }
        buffer.append('/');
        String name = path.getName();
        if (encoding != null) {
            name = URLEncoder.encode(name, encoding);
        }
        buffer.append(name);
    }

    /**
     * {@inheritDoc}
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return parameters.coverageCRS;
    }

    /**
     * {@inheritDoc}
     */
    public Envelope getEnvelope() {
        final Rectangle clipPixels = new Rectangle();
        try {
            return computeBounds(clipPixels, null);
        } catch (TransformException exception) {
            // Should not happen if the coordinate in the database are valids.
            throw new IllegalStateException(exception.getLocalizedMessage(), exception);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Note: Cette biblioth�que utilise une instance sp�ciale de {@link DefaultTemporalCRS}
     *       (d�finie dans {@link net.sicade.observation.sql.CRS}) qui sait repr�senter les
     *       plages de temps illimit�es par {@link Double#POSITIVE_INFINITY} ou
     *       {@link Double#NEGATIVE_INFINITY}.
     */
    public NumberRange getZRange() {
        final DefaultTemporalCRS temporalCRS = parameters.getTemporalCRS();
        return new NumberRange(temporalCRS.toValue(new Date(startTime)),
                               temporalCRS.toValue(new Date(  endTime)));
    }

    /**
     * {@inheritDoc}
     */
    public DateRange getTimeRange() {
        return new DateRange((startTime!=Long.MIN_VALUE) ? new Date(startTime) : null, true,
                               (endTime!=Long.MAX_VALUE) ? new Date(  endTime) : null, false);
    }

    /**
     * {@inheritDoc}
     *
     * @todo L'impl�mentation actuelle suppose que le CRS de la table est toujours WGS84.
     */
    public GeographicBoundingBox getGeographicBoundingBox() {
        try {
            assert CRSUtilities.equalsIgnoreMetadata(DefaultGeographicCRS.WGS84,
                   CRSUtilities.getCRS2D(getCoordinateReferenceSystem()));
        } catch (TransformException e) {
            throw new AssertionError(e);
        }
        return new GeographicBoundingBoxImpl(boundingBox);
    }

    /**
     * {@inheritDoc}
     *
     * @todo L'impl�mentation actuelle suppose que le CRS de la table a toujours des axes dans
     *       l'ordre (x,y).
     */
    public GridGeometry2D getGridGeometry() {
        final Rectangle clipPixels = new Rectangle();
        final Envelope envelope;
        try {
            envelope = computeBounds(clipPixels, null);
        } catch (TransformException exception) {
            // Should not happen if the coordinate in the database are valids.
            throw new IllegalStateException(exception.getLocalizedMessage(), exception);
        }
        if (envelope == null) {
            return null;
        }
        final int dimension = envelope.getDimension();
        final int[]   lower = new int[dimension];
        final int[]   upper = new int[dimension];
        switch (dimension) {
            // Fall through on every cases.
            default: Arrays.fill(upper, 2, dimension, 1);
            case 2:  upper[1] = clipPixels.height;
            case 1:  upper[0] = clipPixels.width;
            case 0:  break;
        }
        final GeneralGridRange gridRange = new GeneralGridRange(lower, upper);
        return new GridGeometry2D(gridRange, envelope, new boolean[]{false,true,false}, false);
    }

    /**
     * {@inheritDoc}
     */
    public SampleDimension[] getSampleDimensions() {
        final GridSampleDimension[] bands = parameters.format.getSampleDimensions();
        for (int i=0; i<bands.length; i++) {
            bands[i] = bands[i].geophysics(true);
        }
        return bands;
    }

    /**
     * Calcule les limites des pixels � lire, en coordonn�es logiques et en coordonn�es pixels.
     * Cette m�thode est appel�e avant la lecture d'une image, mais peut aussi �tre appel�e par
     * des methodes telles que {@link #getEnvelope} et {@link #getGridGeometry}. Tous les arguments
     * de cette m�thode sont des arguments de sortie (en �criture seule).
     *
     * @param  clipPixel Rectangle dans lequel �crire les coordonn�es pixels de la r�gion � lire.
     *         Ce rectangle restera inchang� si tous les pixels sont � lire.
     * @param  subsampling Objet dans lequel �crire les pas de sous-�chantillonage, ou {@code null}
     *         si cette information n'est pas d�sir�e.
     * @param  envelope Envelope dans lequel �crire les coordonn�es logiques de la r�gion � lire.
     * @return Les coordonn�es logiques de l'image � lire, o� {@code null} si l'image ne doit pas
     *         �tre lue (par exemple parce que l'envelope est vide).
     */
    private GeneralEnvelope computeBounds(final Rectangle clipPixel, final Point subsampling)
            throws TransformException
    {
        /*
         * Obtient les coordonn�es g�ographiques et la r�solution d�sir�es. Notez que ces
         * rectangles ne sont pas encore exprim�es dans le syst�me de coordonn�es de l'image.
         * Cette projection sera effectu�e par 'tableToCoverageCRS(...)' seulement apr�s avoir
         * pris en compte le clip. Ca nous �vite d'avoir � projeter le clip, ce qui aurait �t�
         * probl�matique avec les projections qui n'ont pas un domaine de validit� suffisament
         * grand (par exemple jusqu'aux p�les).
         */
        final Rectangle2D clipArea   = parameters.geographicArea;
        final Dimension2D resolution = parameters.resolution;
        final int xSubsampling;
        final int ySubsampling;
        if (resolution != null) {
            /*
             * Conversion [r�solution logique d�sir�e] --> [fr�quence d'�chantillonage des pixels].
             */
            xSubsampling = max(1, min(width /MIN_SIZE, (int)round(width  * (resolution.getWidth () / boundingBox.getWidth ()))));
            ySubsampling = max(1, min(height/MIN_SIZE, (int)round(height * (resolution.getHeight() / boundingBox.getHeight()))));
        } else {
            xSubsampling = 1;
            ySubsampling = 1;
        }
        if (subsampling != null) {
            subsampling.x = xSubsampling;
            subsampling.y = ySubsampling;
        }
        Rectangle2D clipLogical;
        if (clipArea == null) {
            clipLogical = parameters.tableToCoverageCRS(boundingBox, null);
            // Ne PAS modifier ce clipLogical; 'boundingBox' n'a peut-�tre pas �t� clon�!
        } else {
            /*
             * V�rifie si le rectangle demand� (clipArea) intercepte la r�gion g�ographique
             * couverte par l'image. On utilise un code sp�cial plut�t que de faire appel �
             * Rectangle2D.intersects(..) parce qu'on veut accepter les cas o� le rectangle
             * demand� se r�sume � une ligne ou un point.
             */
            if (clipArea.getWidth()<0 || clipArea.getHeight()<0 || boundingBox.isEmpty()) {
                return null;
            }
            if (clipArea.getMaxX() < boundingBox.getMinX() ||
                clipArea.getMinX() > boundingBox.getMaxX() ||
                clipArea.getMaxY() < boundingBox.getMinY() ||
                clipArea.getMinY() > boundingBox.getMaxY())
            {
                return null;
            }
            final Rectangle2D fullArea = parameters.tableToCoverageCRS(boundingBox, null);
            Rectangle2D.intersect(boundingBox, clipArea, clipLogical=new Rectangle2D.Double());
            clipLogical = parameters.tableToCoverageCRS(clipLogical, clipLogical);
            /*
             * Conversion [coordonn�es logiques] --> [coordonn�es pixels].
             */
            final double scaleX =  width/fullArea.getWidth();
            final double scaleY = height/fullArea.getHeight();
            clipPixel.x      = (int)floor(scaleX*(clipLogical.getMinX()-fullArea.getMinX()) + EPS);
            clipPixel.y      = (int)floor(scaleY*(fullArea.getMaxY()-clipLogical.getMaxY()) + EPS);
            clipPixel.width  = (int)ceil (scaleX*clipLogical.getWidth()                     - EPS);
            clipPixel.height = (int)ceil (scaleY*clipLogical.getHeight()                    - EPS);
            if (clipPixel.width <  MIN_SIZE) {
                clipPixel.x    -= (MIN_SIZE-clipPixel.width)/2;
                clipPixel.width =  MIN_SIZE;
            }
            if (clipPixel.height < MIN_SIZE) {
                clipPixel.y     -= (MIN_SIZE-clipPixel.height)/2;
                clipPixel.height = MIN_SIZE;
            }
            /*
             * V�rifie que les coordonn�es obtenues sont bien
             * dans les limites de la dimension de l'image.
             */
            final int clipX2 = min(this.width,  clipPixel.width  + clipPixel.x);
            final int clipY2 = min(this.height, clipPixel.height + clipPixel.y);
            if (clipPixel.x < 0) clipPixel.x = 0;
            if (clipPixel.y < 0) clipPixel.y = 0;
            clipPixel.width  = clipX2-clipPixel.x;
            clipPixel.height = clipY2-clipPixel.y;
            /*
             * V�rifie que la largeur du rectangle est un
             * multiple entier de la fr�quence d'�chantillonage.
             */
            clipPixel.width  = (clipPixel.width /xSubsampling) * xSubsampling;
            clipPixel.height = (clipPixel.height/ySubsampling) * ySubsampling;
            if (clipPixel.isEmpty()) {
                return null;
            }
            /*
             * Conversion [coordonn�es pixels] --> [coordonn�es logiques].
             *
             * 'clipLogical' ne devrait pas beaucoup changer (mais parfois un peu).
             */
            clipLogical.setRect(fullArea.getMinX() + clipPixel.getMinX()  /scaleX,
                                fullArea.getMaxY() - clipPixel.getMaxY()  /scaleY,
                                                     clipPixel.getWidth() /scaleX,
                                                     clipPixel.getHeight()/scaleY);
        }
        CoordinateReferenceSystem coverageCRS = parameters.coverageCRS;
        final DefaultTemporalCRS  temporalCRS = parameters.getTemporalCRS();
        final double tmin = temporalCRS.toValue(new Date(startTime));
        final double tmax = temporalCRS.toValue(new Date(  endTime));
        if (Double.isInfinite(tmin) && Double.isInfinite(tmax)) {
            // TODO : Attention getCRS2D ne tient pas compte des dimensions des GridCoverages
            coverageCRS = CRSUtilities.getCRS2D(coverageCRS);
        }
        final GeneralEnvelope envelope = new GeneralEnvelope(coverageCRS);
        switch (coverageCRS.getCoordinateSystem().getDimension()) {
            default: // Fall through (apply also for all cases below)
            case  3: envelope.setRange(2, tmin, tmax);
            case  2: envelope.setRange(1, clipLogical.getMinY(), clipLogical.getMaxY());
            case  1: envelope.setRange(0, clipLogical.getMinX(), clipLogical.getMaxX());
            case  0: break;
        }
        return envelope;
    }

    /**
     * Proc�de � la lecture d'une image � l'index sp�cifi�.
     *
     * @param imageIndex Index de l'image � lire.
     *        NOTE: si on permet d'obtenir des images � diff�rents index, il faudra en
     *              tenir compte dans {@link #gridCoverage} et {@link #renderedImage}.
     * @param listeners Liste des objets � informer des progr�s de la lecture.
     */
    private synchronized GridCoverage2D getCoverage(final int          imageIndex,
                                                    final IIOListeners listeners)
            throws IOException, TransformException
    {
        /*
         * NOTE SUR LES SYNCHRONISATIONS: Cette m�thode est synchronis�e � plusieurs niveau:
         *
         *  1) Toute la m�thode sur 'this',  afin d'�viter qu'une image ne soit lue deux fois
         *     si un thread tente d'acc�der � la cache alors que l'autre thread n'a pas eu le
         *     temps de placer le r�sultat de la lecture dans cette cache.   Synchroniser sur
         *     'this' ne devrait pas avoir d'impact significatif sur la performance,    �tant
         *     donn� que l'op�ration vraiment longue (la lecture de l'image) est synchronis�e
         *     sur 'format' de toute fa�on (voir prochain item).
         *
         *  2) La lecture de l'image sur 'format'. On ne synchronise pas toute la m�thode sur
         *     'format' afin de ne pas bloquer l'acc�s � la cache pour un objet 'CoverageReference'
         *     donn� pendant qu'une lecture est en cours sur un autre objet 'CoverageReference' qui
         *     utiliserait le m�me format.
         *
         *  3) Les demandes d'annulation de lecture (abort) sur FormatEntry.enqueued, afin de
         *     pouvoir �tre faite pendant qu'une lecture est en cours. Cette synchronisation
         *     est g�r�e en interne par FormatEntry.
         */

        /*
         * V�rifie d'abord si l'image demand�e se trouve d�j� en m�moire. Si
         * oui, elle sera retourn�e et la m�thode se termine imm�diatement.
         */
        if (gridCoverage != null) {
            final GridCoverage2D coverage = gridCoverage.get();
            if (coverage != null) {
                return coverage;
            }
            gridCoverage = null;
        }
        /*
         * Obtient les coordonn�es pixels et les coordonn�es logiques de la r�gion � extraire.
         */
        final Rectangle       clipPixel   = new Rectangle();
        final Point           subsampling = new Point();
        final GeneralEnvelope envelope    = computeBounds(clipPixel, subsampling);
        if (envelope == null) {
            return null;
        }
        /*
         * Avant d'effectuer la lecture, v�rifie si l'image est d�j� en m�moire. Une image
         * {@link RenderedGridCoverage} peut �tre en m�moire m�me si {@link GridCoverage2D}
         * ne l'est plus si, par exemple, l'image est entr�e dans une cha�ne d'op�rations de JAI.
         */
        RenderedImage image = null;
        if (renderedImage != null) {
            image = renderedImage.get();
            if (image == null) {
                renderedImage = null;
                LOGGER.fine("Charge une nouvelle fois les donn�es de \"" + getName() + "\".");
            }
        }
        /*
         * Si la lecture de l'image doit �tre effectu�e par un serveur distant, d�l�gue cette lecture.
         * Le serveur effectuera toutes les traitements jusqu'� l'application de l'op�ration, inclusivement.
         */
        GridCoverage2D coverage;
        if (image==null && loader instanceof RemoteStub) {
            coverage = loader.getCoverage();
            image    = coverage.getRenderedImage();
            coverage = coverage.geophysics(true);
        } else {
            /*
             * A ce stade, nous savons que nous devrons effectuer la lecture nous-m�mes et nous
             * disposons des coordonn�es en pixels de la r�gion � charger. Proc�de maintenant �
             * la lecture.
             */
            final FormatEntry format = parameters.format;
            final GridSampleDimension[] bands;
            try {
                format.setReading(this, true);
                synchronized (format) {
                    final ImageReadParam param = format.getDefaultReadParam();
                    if (!clipPixel.isEmpty()) {
                        param.setSourceRegion(clipPixel);
                    }
                    param.setSourceSubsampling(subsampling.x,   subsampling.y,
                                               subsampling.x/2, subsampling.y/2);
                    if (image == null) {
                        image = format.read(getInput(true), imageIndex, param, listeners,
                                            new Dimension(width, height), this);
                        if (image == null) {
                            return null;
                        }
                    }
                    bands = format.getSampleDimensions(param);
                }
            } finally {
                format.setReading(this, false);
            }
            /*
             * La lecture est termin�e et n'a pas �t� annul�e. On construit maintenant l'objet
             * GridCoverage2D, on le conserve dans une cache interne puis on le retourne. Note:
             * la source n'est pas conserv�e si cet objet est susceptible d'�tre utilis� comme
             * serveur, afin d'�viter de transmettre une copie de GridCoverageEntry via le r�seau.
             */
            final Map properties = (loader==null) ? Collections.singletonMap(SOURCE_KEY, this) : null;
            coverage = FACTORY.create(filename, image, envelope, bands, null, properties);
            /*
             * Retourne toujours la version "g�ophysique" de l'image.
             */
            coverage = coverage.geophysics(true);
            /*
             * Si l'utilisateur a sp�cifi� une operation � appliquer
             * sur les images, applique cette op�ration maintenant.
             */
            Operation operation = parameters.operation;
            if (operation == null) {
                operation = OperationEntry.DEFAULT;
            }
            coverage = (GridCoverage2D) operation.doOperation(coverage);
        }
        renderedImage = new WeakReference<RenderedImage>(image);
        gridCoverage  = new SoftReference<GridCoverage2D>(coverage);
        /*
         * Calcule la quantit� de m�moire utilis�e par l'image. Si la quantit� totale utilis�e par
         * les derni�res images d�passe un seuil maximal, alors les images les plus anciennes veront
         * leurs r�f�rences molles transform�es en r�f�rences faibles.
         */
        memoryUsage = (DataBuffer.getDataTypeSize(image.getSampleModel().getDataType()) / Byte.SIZE)
                    * image.getWidth() * image.getHeight();
        synchronized (LAST_INVOKED) {
            lastInvokedMemoryUsage += memoryUsage;
            for (final Iterator<GridCoverageEntry> it=LAST_INVOKED.iterator(); it.hasNext();) {
                final GridCoverageEntry previous = it.next();
                if (previous != this) {
                    if (lastInvokedMemoryUsage <= MAX_MEMORY_USAGE) {
                        continue;
                    }
                    previous.clearSoftReference();
                }
                it.remove();
                lastInvokedMemoryUsage -= previous.memoryUsage;
            }
            LAST_INVOKED.addLast(this);
        }
        return coverage;
    }

    /**
     * Retourne l'image correspondant � cette entr�e. Si l'image avait d�j� �t� lue pr�c�demment et
     * qu'elle n'a pas encore �t� r�clam�e par le ramasse-miette, alors l'image existante sera
     * retourn�e sans qu'une nouvelle lecture du fichier ne soit n�cessaire. Si au contraire l'image
     * n'�tait pas d�j� en m�moire, alors un d�codage du fichier sera n�cessaire.
     * <p>
     * Cette m�thode ne d�codera pas n�cessairement l'ensemble de l'image. La partie d�cod�e d�pend de la
     * {@linkplain GridCoverageTable#setGeographicBoundingBox r�gion g�ographique} et de la {@linkplain
     * GridCoverageTable#setPreferredResolution r�solution} qui �taient actifs au moment o�
     * {@link GridCoverageTable#getEntries} a �t� appel�e (les changement subs�quents des param�tres
     * de {@link GridCoverageTable} n'ont pas d'effets sur les {@code GridCoverageEntry} d�j� cr��s).
     */
    public GridCoverage2D getCoverage(final IIOListeners listeners) throws IOException {
        try {
            return getCoverage(0, listeners);
        } catch (TransformException exception) {
            throw new IIOException(exception.getLocalizedMessage(), exception);
        }
    }

    /**
     * Retourne l'image correspondant � cette entr�e. Cette m�thode d�l�gue son travail �
     * <code>{@linkplain #getCoverage(IIOListeners) getCoverage}(null)</code>.
     */
    public final GridCoverage2D getCoverage() throws IOException {
        return getCoverage(null);
    }

    /**
     * Remplace la r�f�rence molle de {@link #gridCoverage} par une r�f�rence faible.
     * Cette m�thode est appel�e par quand on a d�termin� que la m�moire allou�e par
     * un {@link GridCoverage2D} devrait �tre lib�r�e.
     */
    private synchronized void clearSoftReference() {
        if (gridCoverage instanceof SoftReference) {
            final GridCoverage2D coverage = gridCoverage.get();
            gridCoverage = (coverage!=null) ? new WeakReference<GridCoverage2D>(coverage) : null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void abort() {
        parameters.format.abort(this);
    }

    /**
     * Indique si cette image a au moins la r�solution sp�cifi�e.
     *
     * @param  resolution R�solution d�sir�e, exprim�e selon le CRS de la table d'images.
     * @return {@code true} si la r�solution de cette image est �gale ou sup�rieure � la r�solution
     *         demand�e. Cette m�thode retourne {@code false} si {@code resolution} �tait nul.
     */
    final boolean hasEnoughResolution(final Dimension2D resolution) {
        return (resolution != null) &&
               (1+EPS)*resolution.getWidth()  >= boundingBox.getWidth() /width &&
               (1+EPS)*resolution.getHeight() >= boundingBox.getHeight()/height;
    }

    /**
     * Si les deux images couvrent les m�mes coordonn�es spatio-temporelles, retourne celle qui a
     * la plus basse r�solution. Si les deux images ne couvrent pas les m�mes coordonn�es ou si
     * leurs r�solutions sont incompatibles, alors cette m�thode retourne {@code null}.
     */
    final GridCoverageEntry getLowestResolution(final GridCoverageEntry that) {
        if (Utilities.equals(this.parameters.series, that.parameters.series) && sameEnvelope(that)) {
            if (this.width<=that.width && this.height<=that.height) return this;
            if (this.width>=that.width && this.height>=that.height) return that;
        }
        return null;
    }

    /**
     * Indique si l'image de cette entr�e couvre la m�me r�gion g�ographique et la m�me plage
     * de temps que celles de l'entr� sp�cifi�e. Les deux entr�s peuvent toutefois appartenir
     * � des s�ries diff�rentes.
     */
    private boolean sameEnvelope(final GridCoverageEntry that) {
        return this.startTime == that.startTime &&
               this.endTime   == that.endTime   &&
               Utilities.equals(this.boundingBox, that.boundingBox) &&
               CRSUtilities.equalsIgnoreMetadata(parameters.tableCRS, that.parameters.tableCRS);
    }

    /**
     * Indique si cette entr�e est identique � l'entr�e sp�cifi�e. Cette m�thode v�rifie
     * tous les param�tres de {@code GridCoverageEntry}, incluant le chemin de l'image et
     * les coordonn�es g�ographiques de la r�gion qui a �t� demand�e. Si vous souhaitez
     * seulement v�rifier si deux objets {@code GridCoverageEntry} d�crivent bien la m�me
     * image (m�me si les coordonn�es de la r�gion demand�e sont diff�rentes), comparez plut�t
     * leur identifiant {@link #getName}. Notez que cette derni�re solution n'est valide que si
     * les deux objets {@code GridCoverageEntry} proviennent de la m�me base de donn�es.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final GridCoverageEntry that = (GridCoverageEntry) object;
            return Utilities.equals(this.filename,   that.filename  ) &&
                   Utilities.equals(this.parameters, that.parameters) &&
                                   (this.width    == that.width     ) &&
                                   (this.height   == that.height    ) &&
                                    sameEnvelope(that);
        }
        return false;
    }

    /**
     * Retourne une cha�ne de caract�res repr�sentant cette entr�e.
     */
    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder(40);
        buffer.append(Utilities.getShortClassName(this));
        buffer.append('[');
        buffer.append(getName());
        if (startTime!=Long.MIN_VALUE && endTime!=Long.MAX_VALUE) {
            buffer.append(" (");
            buffer.append(parameters.format(new Date((startTime+endTime)/2)));
            buffer.append(')');
        }
        buffer.append(' ');
        buffer.append(GeographicBoundingBoxImpl.toString(getGeographicBoundingBox(),
                      GridCoverageTable.ANGLE_PATTERN, null));
        buffer.append(']');
        return buffer.toString();
    }

    /**
     * Apr�s la lecture binaire, remplace l'entr�e lue par une entr�e qui se trouvaient
     * d�j� en m�moire, si une telle entr�e existe. Ce remplacement augmente les chances
     * que la m�thode {@code #getCoverage} retourne une image qui se trouvait d�j� en m�moire.
     *
     * @see #canonicalize
     */
    protected final Object readResolve() throws ObjectStreamException {
        return canonicalize();
    }

    /**
     * Exporte cette entr�e comme service RMI ((<code>Remote Method Invocation</cite>). Lorsque
     * cette entr�e est envoy�e vers un client via le r�seau (typiquement comme objet retourn�
     * par une autre fonction ex�cut�e sur un serveur distant), une connexion vers le serveur
     * d'origine sera conserv�e. La plupart des m�thodes que le client appellera s'ex�cuteront
     * localement, except� {@link #getCoverage()} et ses variantes qui liront et traiteront
     * l'image sur un serveur distant avant de l'envoyer sur le r�seau.
     * <p>
     * Il est innofensif d'appeller cette m�thode plusieurs fois, mais seul le premier appel aura un
     * effet. Cette m�thode est utilis�e par {@link net.sicade.observation.coverage.rmi.GridCoverageServer}
     * et n'a habituellement pas besoin d'�tre appel�e directement.
     *
     * @throws RemoteException si l'exportation du service RMI a �chou�e.
     *
     * @see RemoteLoader
     */
    public final synchronized void export() throws RemoteException {
        if (loader == null) {
            loader = new RemoteLoader(this);
        }
    }
}
