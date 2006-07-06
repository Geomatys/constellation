/*
 * Sicade - Syst�mes int�gr�s de connaissances pour l'aide � la d�cision en environnement
 * (C) 2006, Institut de Recherche pour le D�veloppement
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
package net.sicade.observation.coverage;

// J2SE and JAI dependencies
import java.awt.Dimension;
import java.util.Set;
import java.util.List;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Collections;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.awt.image.IndexColorModel;
import java.lang.management.MemoryUsage;
import java.lang.management.ManagementFactory;
import javax.media.jai.JAI;
import javax.media.jai.TileCache;
import javax.media.jai.RasterFactory;

// Input / Output
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Logger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.sql.SQLException;
import java.net.URL;
import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.IIOException;
import javax.imageio.stream.ImageOutputStream;

// OpenGIS dependencies
import org.opengis.coverage.Coverage;
import org.opengis.coverage.CannotEvaluateException;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.spatialschema.geometry.DirectPosition;
import org.opengis.metadata.extent.GeographicBoundingBox;

// Geotools dependencies
import org.geotools.coverage.FactoryFinder;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.OrdinateOutsideCoverageException;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.gui.headless.ProgressPrinter;
import org.geotools.util.ProgressListener;
import org.geotools.resources.Utilities;
import org.geotools.resources.Arguments;
import org.geotools.math.Statistics;

// Sicade dependencies
import net.sicade.util.DateRange;
import net.sicade.image.operation.Equalizer;
import net.sicade.observation.Observations;
import net.sicade.observation.ServerException;
import net.sicade.observation.CatalogException;
import net.sicade.observation.NoSuchRecordException;
import net.sicade.observation.coverage.sql.WritableGridCoverageTable;


/**
 * Une nouvelle image en cours de cr�ation. Cette image doit correspondre � une entr�e de la base
 * de donn�es, mais pour laquelle l'image n'existait pas encore ou sera � remplacer.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class CoverageBuilder {
    /**
     * Fabrique � utiliser pour la cr�ation des objets {@link GridCoverage2D}.
     */
    public static GridCoverageFactory FACTORY = FactoryFinder.getGridCoverageFactory(null);

    /**
     * Extension (et format) � donner par d�faut aux fichiers lorsqu'aucune extension
     * n'apparait dans le nom.
     */
    private static final String DEFAULT_SUFFIX = "png";

    /**
     * R�pertoire dans lequel �crire les images contrast�es, si demand�es.
     */
    private static final String CONTRASTED_DIRECTORY = "Contrast�es";

    /**
     * R�pertoire dans lequel �crire les descripteurs entrant dans la composition d'une image.
     */
    private static final String DESCRIPTORS_DIRECTORY = "Descripteurs";

    /**
     * R�f�rence vers l'entr�e de la base de donn�es qui correspond � l'image � construire.
     */
    private final CoverageReference entry;

    /**
     * L'image en cours de cr�ation. Cette image utilise un {@link BufferedImage} recevant
     * des donn�es de type {@link DataBuffer#TYPE_FLOAT}.
     */
    private final GridCoverage2D coverage;

    /**
     * Les donn�es de {@link #coverage}.
     */
    private final WritableRaster raster;

    /**
     * Transformation des coordonn�es de {@link #raster} vers les coordonn�es de {@link #coverage}.
     */
    private final MathTransform gridToCRS;

    /**
     * Les dimensions <var>x</var> et <var>y</var> de la grille. Habituellement 0 et 1 respectivement,
     * mais peuvent parfois �tre diff�rents avec certains syst�mes de coordonn�es � plus de 2 dimensions.
     */
    private final int gridDimensionX, gridDimensionY;

    /**
     * Un objet optionel � informer des progr�s, ou {@code null} si aucun.
     */
    private ProgressListener listener;

    /**
     * Fichier dans lequel enregistrer l'image, ou {@code null} s'il n'a pas encore �t� d�termin�.
     */
    private File file;

    /**
     * {@code true} pour enregistrer une image repr�sentant les descripteurs utilis�s.
     */
    private boolean saveDescriptorImages;

    /**
     * {@code true} pour enregistrer une version contrast�e de l'image en plus de la version habituelle.
     */
    private boolean saveContrasted;

    /**
     * Construit une nouvelle image pour la r�f�rence sp�cifi�e. La nouvelle image
     * aura une seule bande, et tous les pixels auront la valeur initiale 0.
     */
    public CoverageBuilder(final CoverageReference entry) {
        final GridSampleDimension band;
        final GridGeometry2D  geometry;
        final Rectangle          range;
        final BufferedImage      image;

        this.entry     = entry;
        band           = GridSampleDimension.wrap(entry.getSampleDimensions()[0]).geophysics(true);
        geometry       = entry.getGridGeometry();
        range          = geometry.getGridRange2D();
        gridDimensionX = geometry.gridDimensionX;
        gridDimensionY = geometry.gridDimensionY;
        gridToCRS      = geometry.getGridToCoordinateSystem();
        raster         = RasterFactory.createBandedRaster(DataBuffer.TYPE_FLOAT,
                         range.width, range.height, 1, new Point(range.x, range.y));
        image          = new BufferedImage(band.getColorModel(), raster, false, null);
        coverage       = FACTORY.create(entry.getName(), image, geometry.getCoordinateReferenceSystem(),
                                        gridToCRS, new GridSampleDimension[]{band}, null, null);
    }

    /**
     * Sp�cifie un objet � informer des progr�s. La valeur {@code null} retire tous objets
     * qui aurait �t� d�clar� lors d'un appel pr�c�dent. 
     */
    public void setProgressListener(final ProgressListener listener) {
        this.listener = listener;
    }

    /**
     * Affecte � tous les pixels de cette image le r�sultat du mod�le sp�cifi�.
     *
     * @param  model Le mod�le � appliquer.
     * @return Des statistiques sur les valeurs calcul�es.
     * @throws CatalogException si une image ne peut pas �tre construite � partir du mod�le.
     * @throws CannotEvaluateException si une erreur est survenue lors d'un calcul d'un des points
     *         de l'image. La cause la plus courante est une date en dehors de la plage de temps
     *         des donn�es disponibles.
     */
    public Statistics compute(final Model model) throws CatalogException, CannotEvaluateException {
        if (listener != null) {
            listener.setDescription("Calcul de l'image \"" + coverage.getName() + '"');
            listener.started();
        }
        final Coverage modelCoverage = model.asCoverage();
        if (saveDescriptorImages) {
            for (final Descriptor d : model.getDescriptors()) {
                saveDescriptorImage(d.getCoverage());
            }
        }
        double[] buffer = null;
        final Statistics        statistics = new Statistics();
        final GeneralDirectPosition source = new GeneralDirectPosition(gridToCRS.getSourceDimensions());
        final GeneralDirectPosition target = new GeneralDirectPosition(gridToCRS.getTargetDimensions());
        final int xmin = raster.getMinX();
        final int ymin = raster.getMinY();
        final int xmax = raster.getWidth()  + xmin;
        final int ymax = raster.getHeight() + ymin;
        for (int y=ymin; y<ymax; y++) {
            if (listener != null) {
                listener.progress(100f * (y-ymin) / (ymax-ymin));
            }
            source.ordinates[gridDimensionY] = y;
            for (int x=xmin; x<xmax; x++) {
                source.ordinates[gridDimensionX] = x;
                final DirectPosition position;
                try {
                    position = gridToCRS.transform(source, target);
                } catch (TransformException e) {
                    // Laisse le pixel � 0 (habituellement NaN).
                    unexpectedException("compute", e);
                    continue;
                }
                buffer = modelCoverage.evaluate(position, buffer);
                final double value = buffer[0];
                raster.setSample(x, y, 0, value);
                if (!Double.isInfinite(value)) {
                    statistics.add(value);
                }
            }
        }
        if (listener != null) {
            listener.complete();
        }
        return statistics;
    }

    /**
     * Retourne une image pour la couverture spatio-temporelle d'un descripteur, ou {@code null}
     * s'il n'y en a pas.
     */
    private GridCoverage2D getDescriptorImage(final DynamicCoverage coverage) throws CatalogException {
        GridCoverage2D coverage2D = null;
        final GeneralDirectPosition source = new GeneralDirectPosition(gridToCRS.getSourceDimensions());
        final GeneralDirectPosition target = new GeneralDirectPosition(gridToCRS.getTargetDimensions());
        source.ordinates[gridDimensionX]   = raster.getWidth()  / 2;
        source.ordinates[gridDimensionY]   = raster.getHeight() / 2;
        final DirectPosition position;
        try {
            position = gridToCRS.transform(source, target);
        } catch (TransformException e) {
            unexpectedException("getDescriptorImage", e);
            return coverage2D;
        }
        @SuppressWarnings("unchecked")
        final List<Coverage> coverages = coverage.coveragesAt(position.getOrdinate(position.getDimension()-1));
        if (!coverages.isEmpty()) {
            final Coverage candidate = coverages.get(coverages.size() / 2);
            if (candidate instanceof GridCoverage2D) {
                coverage2D = (GridCoverage2D) candidate;
            }
        }
        return coverage2D;
    }

    /**
     * Enregistre une image repr�sentant un des termes entrant dans la composition d'un mod�le
     * lin�aire. Cette m�thode est utilis�e essentiellement � des fins de v�rifications. Pour
     * cette raison, les �ventuelles erreurs sont attrap�es et �crite dans le fichier de destination
     * plut�t que propag�es.
     */
    private void saveDescriptorImage(final DynamicCoverage coverage) throws CatalogException {
        if (coverage == null) {
            return;
        }
        /*
         * Obtention du r�pertoire dans lequel �crire les images des descripteurs.
         * Ce r�pertoire sera cr�� si n�cessaire.
         */
        File directory;
        if (true) {
            final File file = getFile();
            directory = new File(file.getParentFile(), DESCRIPTORS_DIRECTORY);
            if (!(directory.exists() ? directory.isDirectory() : directory.mkdir())) {
                return;
            }
            final String filename = file.getName();
            final int ext = filename.lastIndexOf('.');
            directory = new File(directory, ext>0 ? filename.substring(0, ext) : filename);
            if (!(directory.exists() ? directory.isDirectory() : directory.mkdir())) {
                return;
            }
        }
        /*
         * Obtention et enregistrement de l'image.
         */
        String    message   = null;
        Exception exception = null;
        final String name = String.valueOf(coverage.getName());
        final GridCoverage2D coverage2D = getDescriptorImage(coverage);
        if (coverage2D == null) {
            message = "Type d'images non-affichable: " + Utilities.getShortClassName(coverage);
        } else {
            final File file = new File(directory, name + ".png");
            final RenderedImage image = coverage2D.geophysics(false).getRenderedImage();
            try {
                ImageIO.write(image, "png", file);
            } catch (IOException e) {
                exception = e;
            }
        }
        /*
         * En cas d'erreur ou d'avertissement, �criture d'un fichier de commentaires.
         */
        if (message!=null || exception!=null) {
            final File file = new File(directory, name + ".txt");
            final PrintWriter out;
            try {
                out = new PrintWriter(file);
            } catch (FileNotFoundException e) {
                unexpectedException("saveDescriptorImage", e);
                return;
            }
            if (message != null) {
                out.println(message);
            }
            if (exception != null) {
                exception.printStackTrace(out);
            }
            out.close();
        }
    }

    /**
     * Retourne le nom de fichier dans lequel sera {@linkplain #save enregistr�e} l'image, ainsi que
     * son r�pertoire de destination. Si un fichier a �t� sp�cifi� explicitement par un appel � la
     * m�thode {@link #setFile setFile}, alors ce fichier est retourn�. Sinon, le fichier est d�termin�
     * � partir de l'entr�e sp�cifi�e au {@linkplain #CoverageBuilder(CoverageReference) constructeur}
     * en testant d'abord la valeur retourn�e par {@link CoverageReference#getFile()}. Si cette derni�re
     * est nulle, alors le nom de fichier sera d�termin� � partir de {@link CoverageReference#getURL()}
     * et le r�pertoire de destination sera le r�pertoire courant.
     */
    public File getFile() {
        if (file == null) {
            file = entry.getFile();
            if (file == null) {
                final URL url = entry.getURL();
                if (url != null) {
                    String path = url.getPath();
                    if (path!=null && (path=path.trim()).length()!=0) {
                        file = new File(path);
                        if (!file.getParentFile().isDirectory()) {
                            file = new File(file.getName());
                        }
                        return file;
                    }
                }
                file = new File(entry.getName() + '.' + DEFAULT_SUFFIX);
            }
        }
        return file;
    }

    /**
     * D�finit le nom de fichier dans lequel sera {@linkplain #save enregistr�e} l'image, ainsi que
     * son r�pertoire de destination. L'appel de cette m�thode remplace toute valeur pr�c�demment
     * calcul�e par {@link #getFile}. Un argument {@code null} r�tablit la valeur par d�faut.
     */
    public void setFile(final File file) {
        this.file = file;
    }

    /**
     * Enregistre l'image. Le nom de fichier ainsi que le r�pertoire de destination seront obtenus
     * par {@link #getFile}. Le format de l'image sera d�termin� � partir de l'extension du nom de
     * fichier.
     *
     * @throws IOException si l'enregistrement de l'image a �chou�e.
     */
    public void save() throws IOException {
        final File   file     = getFile();
        final String filename = file.getName();
        if (listener != null) {
            listener.setDescription("Enregistrement de l'image \"" + filename + '"');
            listener.started();
        }
        /*
         * Obtient un encodeur pour l'image. L'encodeur est d�duit � partir
         * de l'extension du fichier de destination.
         */
        final int       ext = filename.lastIndexOf('.');
        final String suffix = (ext > 0) ? filename.substring(ext+1) : DEFAULT_SUFFIX;
        final Iterator   it = ImageIO.getImageWritersBySuffix(suffix);
        if (!it.hasNext()) {
            throw new IIOException("Aucun enregistreur pour le format \"" + suffix + "\".");
        }
        final ImageWriter writer = (ImageWriter) it.next();
        /*
         * Sp�cifie la sortie, en donnant directement l'objet File � l'encodeur s'il l'accepte.
         * Sinon, on cr�era le flot de sortie nous-m�me, sans oublier de le fermer � la fin.
         */
        final ImageOutputStream out;
        if (contains(writer.getOriginatingProvider().getOutputTypes(), File.class)) {
            writer.setOutput(file);
            out = null;
        } else {
            out = ImageIO.createImageOutputStream(file);
        }
        /*
         * Proc�de � l'enregistrement de l'image, puis lib�re les ressources.
         */
        final RenderedImage image = coverage.geophysics(false).getRenderedImage();
        writer.setOutput(out);
        writer.write(image);
        writer.dispose();
        if (out != null) {
            out.close();
        }
        if (listener != null) {
            listener.complete();
        }
        /*
         * Enregistre une version constrast�e de l'image.
         */
        if (saveContrasted) {
            final ColorModel cm = image.getColorModel();
            if (cm instanceof IndexColorModel && ((IndexColorModel) cm).getMapSize() == 256) {
                RenderedImage contraste = coverage.geophysics(true).getRenderedImage();
                contraste = new BufferedImage(cm, Equalizer.equalize(contraste), false, null);
                final File directory = new File(file.getParentFile(), CONTRASTED_DIRECTORY);
                if (directory.exists() ? directory.isDirectory() : directory.mkdir()) {
                    final String n = (ext > 0) ? filename.substring(0, ext) : filename;
                    final File f = new File(directory, n + '.' + DEFAULT_SUFFIX);
                    ImageIO.write(contraste, DEFAULT_SUFFIX, f);
                    // ATTENTION: On suppose ici que DEFAULT_SUFFIX correspond aussi au nom du format.
                }
            } else {
                Logger.getLogger("net.sicade.observation.coverage").warning("Mod�le de couleurs incompatible.");
            }
        }
    }

    /**
     * Indique si la liste de types sp�cifi�e contient le candidat sp�cifi�.
     */
    private static final boolean contains(final Class[] types, final Class candidate) {
        for (int i=0; i<types.length; i++) {
            if (types[i].equals(candidate)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Ajoute une nouvelle entr�e dans la base de donn�es, qui correspondra � l'image juste
     * apr�s {@code lastSuccessful}.
     *
     * @param  lastSuccessful Derni�re image d�clar�e dans la base de donn�es.
     * @param  datePattern    Nomenclature du nom de fichier, avec la date en heure locale.
     *                        Notez que l'heure "locale" peut avoir �t� d�finie comme �tant
     *                        l'heure "UTC" par la methode {@link #main}.
     * @return Un singleton contenant l'image ajout�e, ou {@code null}.
     * @throws CatalogException si une erreur est survenue lors de l'acc�s � la base de donn�es.
     */
    private static Set<CoverageReference> addNextEntry(final CoverageReference lastSuccessful,
                                                       final String            datePattern)
            throws CatalogException
    {
        if (lastSuccessful == null || datePattern == null) {
            return null;
        } 
        // Dates et nom du fichier
        final Series       series = lastSuccessful.getSeries();
        final long   timeInterval = Math.round(series.getTimeInterval() * (24*60*60*1000L));
        final DateRange timeRange = lastSuccessful.getTimeRange();
        final Date      startTime = new Date(timeRange.getMinValue().getTime() + timeInterval);
        final Date        endTime = new Date(timeRange.getMaxValue().getTime() + timeInterval);
        final DateFormat   format = new SimpleDateFormat(datePattern, Locale.FRANCE);
        final String     filename = format.format(endTime);

        // Etendue g�ographique et taille de l'image
        final GeographicBoundingBox bbox = lastSuccessful.getGeographicBoundingBox();
        final Dimension size = lastSuccessful.getGridGeometry().getGridRange2D().getSize();

        // Ajout de l'entr�e
        final Observations     observations = Observations.getDefault();
        final WritableGridCoverageTable wgt = observations.getDatabase().getTable(
                                                WritableGridCoverageTable.class);
        wgt.setSeries(series);
        final CoverageReference entry;
        try {
            wgt.addEntry(filename, startTime, endTime, bbox, size);
            entry = wgt.getEntry(filename);
        } catch (SQLException exception) {
            throw new ServerException(exception);
        }
        return Collections.singleton(entry);
    }

    /**
     * Appel�e lorsqu'une exception non-fatale est survenue.
     */
    private static void unexpectedException(final String method, final Exception exception) {
        Utilities.unexpectedException("net.sicade.observation.coverage", "CoverageBuilder", method, exception);
    }

    /**
     * Proc�de � la cr�ation de toutes les nouvelles images des s�ries sp�cifi�es. Les images qui
     * existent d�j� seront saut�es. Les progr�s sont affich�s sur le p�riph�rique de sortie standard.
     * Cette m�thode peut �tre appel�e � partir de la ligne de commande. Les arguments accept�es sont
     * �num�r�s ci-dessous. Tous ces arguments sont optionels. Les arguments {@code -xmin}, {@code -xmax},
     * {@code -ymin} et {@code -ymax} servent � limiter la consommation de m�moire en �vitant de charger
     * la totalit� des images lorsque seule une sous-r�gion nous int�resse. Si un de ces param�tres est
     * sp�cifi�, alors ils doivent l'�tre tous. L'argument {@code -limit} sert essentiellement � tester
     * un mod�le lin�aire sur quelques images seulement.
     * <p>
     * <table>
     *   <tr>
     *     <td nowrap>{@code -xmin} <var>x</var></td>
     *     <td>Limite ouest (en degr�s de longitude) des donn�es � charger en m�moire.</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>{@code -xmax} <var>x</var></td>
     *     <td>Limite est (en degr�s de longitude) des donn�es � charger en m�moire.</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>{@code -ymin} <var>y</var></td>
     *     <td>Limite sud (en degr�s de latitude) des donn�es � charger en m�moire.</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>{@code -ymax} <var>y</var></td>
     *     <td>Limite nord (en degr�s de latitude) des donn�es � charger en m�moire.</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>{@code -limit} <var>n</var></td>
     *     <td>Nombre maximal d'images � g�n�rer.</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>{@code -overwrite-last} <var>n</var></td>
     *     <td>Si des images existent d�j�, nombre d'images � �craser parmis les plus r�centes.
     *         Par exemple la valeur 5 recalculera inconditionnellement les 5 derni�res images
     *         m�me si elles existent d�j�.</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>{@code -cache} <var>n</var></td>
     *     <td>Taille (en mega octets) � alouer � la cache des tuiles de JAI.</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>{@code -save-descriptors}</td>
     *     <td>Enregistre une image des descripteurs servant au calcul du mod�le.</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>{@code -save-contrasted}</td>
     *     <td>Enregistre une version contrast�e de l'image en plus de la version habituelle.</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>{@code -date-pattern}</td>
     *     <td>Nomenclature du nom de l'image (date incluse, en heure UTC). Si l'utilisateur ne fournit pas de 
     *      {@code -date-pattern}, alors on ne rajoute pas d'entr�e dans la table "{@code GridCoverages}".</td>
     *   </tr>
     * </table>
     *
     * @param  args Noms des s�ries pour lesquelles on veut cr�er des images. Exemple:
     *              {@code "Potentiel de p�che ALB-optimal (Cal�donie)"}.
     * @throws CatalogException si une exception est survenue lors de l'interrogation de la base de donn�es.
     * @throws IOException si une exception est survenue lors de l'�criture de l'image.
     */
    public static void main(String[] args) throws CatalogException, IOException {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        /*
         * Extraction des arguments et configuration globale (par exemple quantit�
         * de m�moire allou�e � la cache des tuiles) en fonction de ces arguments.
         */
        final Arguments arguments = new Arguments(args);
        final Double        xmin  = arguments.getOptionalDouble ("-xmin" );
        final Double        xmax  = arguments.getOptionalDouble ("-xmax" );
        final Double        ymin  = arguments.getOptionalDouble ("-ymin" );
        final Double        ymax  = arguments.getOptionalDouble ("-ymax" );
        final Integer       limit = arguments.getOptionalInteger("-limit");
        final Integer   overwrite = arguments.getOptionalInteger("-overwrite-last");
        final Integer   cacheSize = arguments.getOptionalInteger("-cache");
        final boolean   saveSteps = arguments.getFlag("-save-descriptors");
        final boolean   saveContr = arguments.getFlag("-save-contrasted");
        final String  datePattern = arguments.getOptionalString("-date-pattern");
        args = arguments.getRemainingArguments(Integer.MAX_VALUE);
        if (cacheSize != null) {
            final long targetCapacity = cacheSize * (1024*1024L);
            final TileCache cache = JAI.getDefaultInstance().getTileCache();
            if (cache.getMemoryCapacity() < targetCapacity) {
                cache.setMemoryCapacity(targetCapacity);
            }
        }
        final GeographicBoundingBox area;
        if (xmin!=null && xmax!=null && ymin!=null && ymax!=null) {
            area = new GeographicBoundingBoxImpl(xmin, xmax, ymin, ymax);
        } else {
            area = null;
        }
        final PrintWriter           out = arguments.out;
        final Observations observations = Observations.getDefault();
        final ProgressListener listener = new ProgressPrinter(out);
        final char[]          separator = new char[72];
        Arrays.fill(separator, '_');
        /*
         * Calcul des mod�les lin�aires pour chacune des s�ries d�clar�es sur la ligne de commande.
         * Les s�ries non-trouv�es o� les mod�les lin�aires non-d�finis provoqueront l'arr�t du
         * programme apr�s affichage d'un message d'erreur � peu-pr�s propre.
         */
        nextSeries: for (int i=0; i<args.length; i++) {
            out.print("Traitement de la s�rie \"");
            out.print(args[i]);
            out.println('"');
            final Series series;
            try {
                series = observations.getSeries(area, null, args[i]);
            } catch (NoSuchRecordException e) {
                out.print(Utilities.getShortClassName(e));
                out.print(": ");
                out.println(e.getLocalizedMessage());
                return;
            }
            final Model model = series.getModel();
            if (model == null) {
                out.println("Aucun mod�le num�rique n'est d�fini pour cette s�rie.");
                return;
            }
            int remaining = (limit!=null) ? limit : Integer.MAX_VALUE;
            CoverageReference lastSuccessful = null; // La derni�re r�f�rence trait�e avec succ�s.
            Set<CoverageReference> references = series.getCoverageReferences();
            int skipIfExist = (overwrite != null) ? references.size() - overwrite : Integer.MAX_VALUE;
            /*
             * Pour chaque ref�rences d�clar�es dans la base de donn�es pour la s�rie courante,
             * v�rifie si le fichier correspondant � l'image existe. Les images d�j� existantes
             * seront ignor�es silencieusement.
             */
            do for (final CoverageReference reference : references) {
                final CoverageBuilder builder = new CoverageBuilder(reference);
                builder.saveDescriptorImages  = saveSteps;
                builder.saveContrasted        = saveContr;
                lastSuccessful                = reference;
                if (--skipIfExist >= 0 && builder.getFile().exists()) {
                    continue;
                }
                builder.setProgressListener(listener);
                final Statistics stats;
                try {
                    stats = builder.compute(model);
                } catch (CatalogException exception) {
                    /*
                     * Erreur d'acc�s au catalogue. C'est peut-�tre s�rieux, alors on laisse l'exception
                     * se propager apr�s avoir saut� la ligne sur laquelle on �crivait les progr�s (afin
                     * que le nom de fichier ne soit pas �cras�).
                     */
                    out.println();
                    throw exception;
                } catch (CannotEvaluateException exception) {
                    /*
                     * Donn�e manquante. C'est une erreur tr�s courante, alors on �crit un message
                     * pour l'utilisateur sans le "stack trace qui fait peur", et on passe gentiment
                     * � la s�rie suivante (on n'arr�te pas compl�tement le programme parce qu'il peut
                     * �tre normal que l'image la plus r�cente n'aie pas encore toutes les donn�es, et
                     * on ne pas que cette "erreur" emp�che le traitement des s�ries suivantes).
                     */
                    out.println();
                    out.println(exception.getLocalizedMessage());
                    if (exception instanceof OrdinateOutsideCoverageException) {
                        final Envelope envelope = ((OrdinateOutsideCoverageException) exception).getCoverageEnvelope();
                        if (envelope != null) {
                            out.print("L'enveloppe des donn�es source est ");
                            out.println(envelope);
                        }
                    }
                    out.print("L'enveloppe de la destination est  ");
                    out.println(builder.coverage.getEnvelope());
                    continue nextSeries;
                } catch (RuntimeException exception) {
                    // Tout autre type d'erreur. M�me traitement que CatalogException.
                    out.println();
                    throw exception;
                }
                /*
                 * Affiche des statistiques sur l'image, ainsi que sur les ressources utilis�es.
                 * Ces derni�res informations servent notamment � ajuster la valeur du param�tre
                 * -Xmx sp�cifi� au d�marrage du Java. Puis proc�de � l'enregistrement de l'image.
                 */
                final MemoryUsage mem = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
                out.println(stats);
                out.print("M�moire utilis�e: ");
                out.print(mem.getUsed() / (1024*1024));
                out.print(" sur ");
                out.print(mem.getCommitted() / (1024*1024));
                out.println(" Mb r�serv�.");
                out.println(separator);
                out.println();
                builder.save();
                if (--remaining <= 0) {
                    return;
                }
            } while ((references = addNextEntry(lastSuccessful, datePattern)) != null);
            /*
             * La ligne pr�c�dente ajoute dans la base de donn�es une entr�e pour la prochaine image.
             * Cet ajout ne sera effectu� que si une image existe ou a �t� cr��e pour toutes les entr�es
             * existantes, de sorte que si on n'ajoutait pas d'images, il n'y aurait plus de prochaine
             * ex�cution du calcul du mod�le lin�aire.
             */
        }
    }
}
