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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sicade.observation.coverage.sql;

// Entrés/sorties et bases de données
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

// Geométries
import java.awt.Point;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Dimension2D;

// Références faibles
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
 * Implémentation d'une entrée représentant une {@linkplain CoverageReference référence vers une
 * image}. Un objet {@code GridCoverageEntry} correspond à un enregistrement de la base de données
 * d'images. Chaque instance est imutable et sécuritaire dans un environnement multi-threads.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class GridCoverageEntry extends Entry implements CoverageReference, CoverageLoader {
    /**
     * Compare deux entrées selon le même critère que celui qui apparait dans l'instruction
     * {@code "ORDER BY"} de la réquête SQL de {@link GridCoverageTable}). Les entrés sans
     * dates sont une exception: elles sont considérées comme non-ordonnées.
     */
    final boolean compare(final GridCoverageEntry other) {
        if (startTime==Long.MIN_VALUE && endTime==Long.MAX_VALUE) {
            return false;
        }
        return endTime == other.endTime;
    }

    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = -5725249398707248625L;

    /**
     * Ensemble des entrées qui ont déjà été retournées par {@link #canonicalize()} et qui n'ont pas
     * encore été réclamées par le ramasse-miettes. La classe {@link GridCoverageTable} tentera autant
     * que possible de retourner des entrées qui existent déjà en mémoire afin de leur donner une chance
     * de faire un meilleur travail de cache sur les images.
     */
    private static final WeakHashSet POOL = new WeakHashSet();

    /**
     * Liste des derniers {@link GridCoverageEntry} pour lesquels la méthode {@link #getCoverage}
     * a été appelée. Lorsqu'une nouvelle image est lue, les références molles les plus anciennes
     * sont changées en références faibles afin d'augmenter les chances que le ramasse-miette se
     * débarasse des images les plus anciennes avant que la mémoire ne sature.
     */
    private static final LinkedList<GridCoverageEntry> LAST_INVOKED = new LinkedList<GridCoverageEntry>();

    /**
     * Quantité maximale de mémoire (en octets) que l'on autorise pour l'ensemble des images
     * énumérées dans {@link #LAST_INVOKED}.  Si cette quantité de mémoire est dépassée, les
     * images les plus anciennes seront retirées de la liste {@link #LAST_INVOKED} jusqu'à ce
     * qu'elle soit ramenée en dessous de cette limite.
     */
    private static final long MAX_MEMORY_USAGE = 128L * 1024 * 1024;

    /**
     * Somme de {@link #memoryUsage} pour toutes les images de la liste {@link #LAST_INVOKED}.
     */
    private static long lastInvokedMemoryUsage;

    /**
     * Petite valeur utilisée pour contourner les erreurs d'arrondissement.
     */
    private static final double EPS = 1E-6;

    /**
     * Largeur et hauteur minimale des images, en pixels. Si l'utilisateur demande une région plus
     * petite, la région demandée sera agrandie pour que l'image fasse cette taille.
     */
    private static final int MIN_SIZE = 64;

    /** Nom du fichier.                  */ private final String      filename;
    /** Date du début de l'acquisition.  */ private final long        startTime;
    /** Date de la fin de l'acquisition. */ private final long        endTime;
    /** Envelope géographique.           */ private final Rectangle2D boundingBox;
    /** Nombre de pixels en largeur.     */ private final short       width;
    /** Nombre de pixels en hauteur.     */ private final short       height;

    /**
     * Bloc de paramètres de la table d'images. On retient ce bloc de paramètres plutôt qu'une
     * référence directe vers {@link GridCoverageTable} afin de ne pas empêcher le ramasse-miettes
     * de détruire la table et ses connections vers la base de données.
     */
    private final Parameters parameters;

    /**
     * Un décodeur sur lequel déléguer le chargement des images, ou {@code null} pour le lire
     * directement avec cette entrée. Dans ce dernier cas, l'image sera typiquement rapatriée
     * par FTP.
     */
    private CoverageLoader loader;

    /**
     * Référence molle vers l'image {@link GridCoverage2D} qui a été retournée lors du dernier appel
     * de {@link #getCoverage}. Cette référence est retenue afin d'éviter de charger inutilement une
     * autre fois l'image si elle est déjà en mémoire.
     */
    private transient Reference<GridCoverage2D> gridCoverage;

    /**
     * Référence molle vers l'image {@link RenderedImage} qui a été retournée lors du dernier appel
     * de {@link #getCoverage}. Cette référence est retenue afin d'éviter de charger inutilement une
     * autre fois l'image si elle est déjà en mémoire.
     */
    private transient Reference<RenderedImage> renderedImage;

    /**
     * Quantité de mémoire utilisée par les pixels de {@link #renderedImage}. Seuls les pixels
     * sont pris en compte; l'espace occupé par l'objet lui-même n'est pas mesuré. La valeur 0
     * indique que {@link #renderedImage} n'a pas encore été lue.
     */
    private transient int memoryUsage;

    /**
     * Construit une entré contenant des informations sur une image. Un {@linkplain #getName nom unique}
     * sera construit à partir de la sous-série et du nom de fichiers (les colonnes {@code subseries} et
     * {@code filename}, qui constituent habituellement la clé primaire de la table).
     * <p>
     * <strong>NOTE:</strong> Les coordonnées {@code xmin}, {@code xmax}, {@code ymin} et {@code ymax}
     * ne sont <u>pas</u> exprimées selon le système de coordonnées de l'image, mais plutôt selon le
     * système de coordonnées de la table d'images ({@code table}). La transformation sera effectuée
     * par {@link #getEnvelope()} à la volé.
     *
     * @param  table Table d'où proviennent les enregistrements.
     * @throws CatalogException si des arguments sont invalides.
     * @throws SQLException si une erreur est survenue lors de l'accès à la base de données.
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
     * Retourne un exemplaire unique de cette entrée. Une banque d'entrées, initialement vide, est
     * maintenue de façon interne par la classe {@code GridCoverageEntry}. Lorsque la méthode
     * {@code canonicalize} est appelée, elle recherchera une entrée égale à {@code this} au
     * sens de la méthode {@link #equals}. Si une telle entrée est trouvée, elle sera retournée.
     * Sinon, l'entrée {@code this} sera ajoutée à la banque de données en utilisant une
     * {@linkplain WeakReference référence faible} et cette méthode retournera {@code this}.
     * <p>
     * De cette méthode il s'ensuit que pour deux entrées <var>u</var> et <var>v</var>,
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
     * Transforme un chemin en URL. Si {@code encoding} est non-nul, alors le chemin est encodé.
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
     * Note: Cette bibliothèque utilise une instance spéciale de {@link DefaultTemporalCRS}
     *       (définie dans {@link net.sicade.observation.sql.CRS}) qui sait représenter les
     *       plages de temps illimitées par {@link Double#POSITIVE_INFINITY} ou
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
     * @todo L'implémentation actuelle suppose que le CRS de la table est toujours WGS84.
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
     * @todo L'implémentation actuelle suppose que le CRS de la table a toujours des axes dans
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
     * Calcule les limites des pixels à lire, en coordonnées logiques et en coordonnées pixels.
     * Cette méthode est appelée avant la lecture d'une image, mais peut aussi être appelée par
     * des methodes telles que {@link #getEnvelope} et {@link #getGridGeometry}. Tous les arguments
     * de cette méthode sont des arguments de sortie (en écriture seule).
     *
     * @param  clipPixel Rectangle dans lequel écrire les coordonnées pixels de la région à lire.
     *         Ce rectangle restera inchangé si tous les pixels sont à lire.
     * @param  subsampling Objet dans lequel écrire les pas de sous-échantillonage, ou {@code null}
     *         si cette information n'est pas désirée.
     * @param  envelope Envelope dans lequel écrire les coordonnées logiques de la région à lire.
     * @return Les coordonnées logiques de l'image à lire, où {@code null} si l'image ne doit pas
     *         être lue (par exemple parce que l'envelope est vide).
     */
    private GeneralEnvelope computeBounds(final Rectangle clipPixel, final Point subsampling)
            throws TransformException
    {
        /*
         * Obtient les coordonnées géographiques et la résolution désirées. Notez que ces
         * rectangles ne sont pas encore exprimées dans le système de coordonnées de l'image.
         * Cette projection sera effectuée par 'tableToCoverageCRS(...)' seulement après avoir
         * pris en compte le clip. Ca nous évite d'avoir à projeter le clip, ce qui aurait été
         * problématique avec les projections qui n'ont pas un domaine de validité suffisament
         * grand (par exemple jusqu'aux pôles).
         */
        final Rectangle2D clipArea   = parameters.geographicArea;
        final Dimension2D resolution = parameters.resolution;
        final int xSubsampling;
        final int ySubsampling;
        if (resolution != null) {
            /*
             * Conversion [résolution logique désirée] --> [fréquence d'échantillonage des pixels].
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
            // Ne PAS modifier ce clipLogical; 'boundingBox' n'a peut-être pas été cloné!
        } else {
            /*
             * Vérifie si le rectangle demandé (clipArea) intercepte la région géographique
             * couverte par l'image. On utilise un code spécial plutôt que de faire appel à
             * Rectangle2D.intersects(..) parce qu'on veut accepter les cas où le rectangle
             * demandé se résume à une ligne ou un point.
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
             * Conversion [coordonnées logiques] --> [coordonnées pixels].
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
             * Vérifie que les coordonnées obtenues sont bien
             * dans les limites de la dimension de l'image.
             */
            final int clipX2 = min(this.width,  clipPixel.width  + clipPixel.x);
            final int clipY2 = min(this.height, clipPixel.height + clipPixel.y);
            if (clipPixel.x < 0) clipPixel.x = 0;
            if (clipPixel.y < 0) clipPixel.y = 0;
            clipPixel.width  = clipX2-clipPixel.x;
            clipPixel.height = clipY2-clipPixel.y;
            /*
             * Vérifie que la largeur du rectangle est un
             * multiple entier de la fréquence d'échantillonage.
             */
            clipPixel.width  = (clipPixel.width /xSubsampling) * xSubsampling;
            clipPixel.height = (clipPixel.height/ySubsampling) * ySubsampling;
            if (clipPixel.isEmpty()) {
                return null;
            }
            /*
             * Conversion [coordonnées pixels] --> [coordonnées logiques].
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
     * Procède à la lecture d'une image à l'index spécifié.
     *
     * @param imageIndex Index de l'image à lire.
     *        NOTE: si on permet d'obtenir des images à différents index, il faudra en
     *              tenir compte dans {@link #gridCoverage} et {@link #renderedImage}.
     * @param listeners Liste des objets à informer des progrès de la lecture.
     */
    private synchronized GridCoverage2D getCoverage(final int          imageIndex,
                                                    final IIOListeners listeners)
            throws IOException, TransformException
    {
        /*
         * NOTE SUR LES SYNCHRONISATIONS: Cette méthode est synchronisée à plusieurs niveau:
         *
         *  1) Toute la méthode sur 'this',  afin d'éviter qu'une image ne soit lue deux fois
         *     si un thread tente d'accéder à la cache alors que l'autre thread n'a pas eu le
         *     temps de placer le résultat de la lecture dans cette cache.   Synchroniser sur
         *     'this' ne devrait pas avoir d'impact significatif sur la performance,    étant
         *     donné que l'opération vraiment longue (la lecture de l'image) est synchronisée
         *     sur 'format' de toute façon (voir prochain item).
         *
         *  2) La lecture de l'image sur 'format'. On ne synchronise pas toute la méthode sur
         *     'format' afin de ne pas bloquer l'accès à la cache pour un objet 'CoverageReference'
         *     donné pendant qu'une lecture est en cours sur un autre objet 'CoverageReference' qui
         *     utiliserait le même format.
         *
         *  3) Les demandes d'annulation de lecture (abort) sur FormatEntry.enqueued, afin de
         *     pouvoir être faite pendant qu'une lecture est en cours. Cette synchronisation
         *     est gérée en interne par FormatEntry.
         */

        /*
         * Vérifie d'abord si l'image demandée se trouve déjà en mémoire. Si
         * oui, elle sera retournée et la méthode se termine immédiatement.
         */
        if (gridCoverage != null) {
            final GridCoverage2D coverage = gridCoverage.get();
            if (coverage != null) {
                return coverage;
            }
            gridCoverage = null;
        }
        /*
         * Obtient les coordonnées pixels et les coordonnées logiques de la région à extraire.
         */
        final Rectangle       clipPixel   = new Rectangle();
        final Point           subsampling = new Point();
        final GeneralEnvelope envelope    = computeBounds(clipPixel, subsampling);
        if (envelope == null) {
            return null;
        }
        /*
         * Avant d'effectuer la lecture, vérifie si l'image est déjà en mémoire. Une image
         * {@link RenderedGridCoverage} peut être en mémoire même si {@link GridCoverage2D}
         * ne l'est plus si, par exemple, l'image est entrée dans une chaîne d'opérations de JAI.
         */
        RenderedImage image = null;
        if (renderedImage != null) {
            image = renderedImage.get();
            if (image == null) {
                renderedImage = null;
                LOGGER.fine("Charge une nouvelle fois les données de \"" + getName() + "\".");
            }
        }
        /*
         * Si la lecture de l'image doit être effectuée par un serveur distant, délègue cette lecture.
         * Le serveur effectuera toutes les traitements jusqu'à l'application de l'opération, inclusivement.
         */
        GridCoverage2D coverage;
        if (image==null && loader instanceof RemoteStub) {
            coverage = loader.getCoverage();
            image    = coverage.getRenderedImage();
            coverage = coverage.geophysics(true);
        } else {
            /*
             * A ce stade, nous savons que nous devrons effectuer la lecture nous-mêmes et nous
             * disposons des coordonnées en pixels de la région à charger. Procède maintenant à
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
             * La lecture est terminée et n'a pas été annulée. On construit maintenant l'objet
             * GridCoverage2D, on le conserve dans une cache interne puis on le retourne. Note:
             * la source n'est pas conservée si cet objet est susceptible d'être utilisé comme
             * serveur, afin d'éviter de transmettre une copie de GridCoverageEntry via le réseau.
             */
            final Map properties = (loader==null) ? Collections.singletonMap(SOURCE_KEY, this) : null;
            coverage = FACTORY.create(filename, image, envelope, bands, null, properties);
            /*
             * Retourne toujours la version "géophysique" de l'image.
             */
            coverage = coverage.geophysics(true);
            /*
             * Si l'utilisateur a spécifié une operation à appliquer
             * sur les images, applique cette opération maintenant.
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
         * Calcule la quantité de mémoire utilisée par l'image. Si la quantité totale utilisée par
         * les dernières images dépasse un seuil maximal, alors les images les plus anciennes veront
         * leurs références molles transformées en références faibles.
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
     * Retourne l'image correspondant à cette entrée. Si l'image avait déjà été lue précédemment et
     * qu'elle n'a pas encore été réclamée par le ramasse-miette, alors l'image existante sera
     * retournée sans qu'une nouvelle lecture du fichier ne soit nécessaire. Si au contraire l'image
     * n'était pas déjà en mémoire, alors un décodage du fichier sera nécessaire.
     * <p>
     * Cette méthode ne décodera pas nécessairement l'ensemble de l'image. La partie décodée dépend de la
     * {@linkplain GridCoverageTable#setGeographicBoundingBox région géographique} et de la {@linkplain
     * GridCoverageTable#setPreferredResolution résolution} qui étaient actifs au moment où
     * {@link GridCoverageTable#getEntries} a été appelée (les changement subséquents des paramètres
     * de {@link GridCoverageTable} n'ont pas d'effets sur les {@code GridCoverageEntry} déjà créés).
     */
    public GridCoverage2D getCoverage(final IIOListeners listeners) throws IOException {
        try {
            return getCoverage(0, listeners);
        } catch (TransformException exception) {
            throw new IIOException(exception.getLocalizedMessage(), exception);
        }
    }

    /**
     * Retourne l'image correspondant à cette entrée. Cette méthode délègue son travail à
     * <code>{@linkplain #getCoverage(IIOListeners) getCoverage}(null)</code>.
     */
    public final GridCoverage2D getCoverage() throws IOException {
        return getCoverage(null);
    }

    /**
     * Remplace la référence molle de {@link #gridCoverage} par une référence faible.
     * Cette méthode est appelée par quand on a déterminé que la mémoire allouée par
     * un {@link GridCoverage2D} devrait être libérée.
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
     * Indique si cette image a au moins la résolution spécifiée.
     *
     * @param  resolution Résolution désirée, exprimée selon le CRS de la table d'images.
     * @return {@code true} si la résolution de cette image est égale ou supérieure à la résolution
     *         demandée. Cette méthode retourne {@code false} si {@code resolution} était nul.
     */
    final boolean hasEnoughResolution(final Dimension2D resolution) {
        return (resolution != null) &&
               (1+EPS)*resolution.getWidth()  >= boundingBox.getWidth() /width &&
               (1+EPS)*resolution.getHeight() >= boundingBox.getHeight()/height;
    }

    /**
     * Si les deux images couvrent les mêmes coordonnées spatio-temporelles, retourne celle qui a
     * la plus basse résolution. Si les deux images ne couvrent pas les mêmes coordonnées ou si
     * leurs résolutions sont incompatibles, alors cette méthode retourne {@code null}.
     */
    final GridCoverageEntry getLowestResolution(final GridCoverageEntry that) {
        if (Utilities.equals(this.parameters.series, that.parameters.series) && sameEnvelope(that)) {
            if (this.width<=that.width && this.height<=that.height) return this;
            if (this.width>=that.width && this.height>=that.height) return that;
        }
        return null;
    }

    /**
     * Indique si l'image de cette entrée couvre la même région géographique et la même plage
     * de temps que celles de l'entré spécifiée. Les deux entrés peuvent toutefois appartenir
     * à des séries différentes.
     */
    private boolean sameEnvelope(final GridCoverageEntry that) {
        return this.startTime == that.startTime &&
               this.endTime   == that.endTime   &&
               Utilities.equals(this.boundingBox, that.boundingBox) &&
               CRSUtilities.equalsIgnoreMetadata(parameters.tableCRS, that.parameters.tableCRS);
    }

    /**
     * Indique si cette entrée est identique à l'entrée spécifiée. Cette méthode vérifie
     * tous les paramètres de {@code GridCoverageEntry}, incluant le chemin de l'image et
     * les coordonnées géographiques de la région qui a été demandée. Si vous souhaitez
     * seulement vérifier si deux objets {@code GridCoverageEntry} décrivent bien la même
     * image (même si les coordonnées de la région demandée sont différentes), comparez plutôt
     * leur identifiant {@link #getName}. Notez que cette dernière solution n'est valide que si
     * les deux objets {@code GridCoverageEntry} proviennent de la même base de données.
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
     * Retourne une chaîne de caractères représentant cette entrée.
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
     * Après la lecture binaire, remplace l'entrée lue par une entrée qui se trouvaient
     * déjà en mémoire, si une telle entrée existe. Ce remplacement augmente les chances
     * que la méthode {@code #getCoverage} retourne une image qui se trouvait déjà en mémoire.
     *
     * @see #canonicalize
     */
    protected final Object readResolve() throws ObjectStreamException {
        return canonicalize();
    }

    /**
     * Exporte cette entrée comme service RMI ((<code>Remote Method Invocation</cite>). Lorsque
     * cette entrée est envoyée vers un client via le réseau (typiquement comme objet retourné
     * par une autre fonction exécutée sur un serveur distant), une connexion vers le serveur
     * d'origine sera conservée. La plupart des méthodes que le client appellera s'exécuteront
     * localement, excepté {@link #getCoverage()} et ses variantes qui liront et traiteront
     * l'image sur un serveur distant avant de l'envoyer sur le réseau.
     * <p>
     * Il est innofensif d'appeller cette méthode plusieurs fois, mais seul le premier appel aura un
     * effet. Cette méthode est utilisée par {@link net.sicade.observation.coverage.rmi.GridCoverageServer}
     * et n'a habituellement pas besoin d'être appelée directement.
     *
     * @throws RemoteException si l'exportation du service RMI a échouée.
     *
     * @see RemoteLoader
     */
    public final synchronized void export() throws RemoteException {
        if (loader == null) {
            loader = new RemoteLoader(this);
        }
    }
}
