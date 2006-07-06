/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2006, Institut de Recherche pour le Développement
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
 * Une nouvelle image en cours de création. Cette image doit correspondre à une entrée de la base
 * de données, mais pour laquelle l'image n'existait pas encore ou sera à remplacer.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class CoverageBuilder {
    /**
     * Fabrique à utiliser pour la création des objets {@link GridCoverage2D}.
     */
    public static GridCoverageFactory FACTORY = FactoryFinder.getGridCoverageFactory(null);

    /**
     * Extension (et format) à donner par défaut aux fichiers lorsqu'aucune extension
     * n'apparait dans le nom.
     */
    private static final String DEFAULT_SUFFIX = "png";

    /**
     * Répertoire dans lequel écrire les images contrastées, si demandées.
     */
    private static final String CONTRASTED_DIRECTORY = "Contrastées";

    /**
     * Répertoire dans lequel écrire les descripteurs entrant dans la composition d'une image.
     */
    private static final String DESCRIPTORS_DIRECTORY = "Descripteurs";

    /**
     * Référence vers l'entrée de la base de données qui correspond à l'image à construire.
     */
    private final CoverageReference entry;

    /**
     * L'image en cours de création. Cette image utilise un {@link BufferedImage} recevant
     * des données de type {@link DataBuffer#TYPE_FLOAT}.
     */
    private final GridCoverage2D coverage;

    /**
     * Les données de {@link #coverage}.
     */
    private final WritableRaster raster;

    /**
     * Transformation des coordonnées de {@link #raster} vers les coordonnées de {@link #coverage}.
     */
    private final MathTransform gridToCRS;

    /**
     * Les dimensions <var>x</var> et <var>y</var> de la grille. Habituellement 0 et 1 respectivement,
     * mais peuvent parfois être différents avec certains systèmes de coordonnées à plus de 2 dimensions.
     */
    private final int gridDimensionX, gridDimensionY;

    /**
     * Un objet optionel à informer des progrès, ou {@code null} si aucun.
     */
    private ProgressListener listener;

    /**
     * Fichier dans lequel enregistrer l'image, ou {@code null} s'il n'a pas encore été déterminé.
     */
    private File file;

    /**
     * {@code true} pour enregistrer une image représentant les descripteurs utilisés.
     */
    private boolean saveDescriptorImages;

    /**
     * {@code true} pour enregistrer une version contrastée de l'image en plus de la version habituelle.
     */
    private boolean saveContrasted;

    /**
     * Construit une nouvelle image pour la référence spécifiée. La nouvelle image
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
     * Spécifie un objet à informer des progrès. La valeur {@code null} retire tous objets
     * qui aurait été déclaré lors d'un appel précédent. 
     */
    public void setProgressListener(final ProgressListener listener) {
        this.listener = listener;
    }

    /**
     * Affecte à tous les pixels de cette image le résultat du modèle spécifié.
     *
     * @param  model Le modèle à appliquer.
     * @return Des statistiques sur les valeurs calculées.
     * @throws CatalogException si une image ne peut pas être construite à partir du modèle.
     * @throws CannotEvaluateException si une erreur est survenue lors d'un calcul d'un des points
     *         de l'image. La cause la plus courante est une date en dehors de la plage de temps
     *         des données disponibles.
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
                    // Laisse le pixel à 0 (habituellement NaN).
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
     * Enregistre une image représentant un des termes entrant dans la composition d'un modèle
     * linéaire. Cette méthode est utilisée essentiellement à des fins de vérifications. Pour
     * cette raison, les éventuelles erreurs sont attrapées et écrite dans le fichier de destination
     * plutôt que propagées.
     */
    private void saveDescriptorImage(final DynamicCoverage coverage) throws CatalogException {
        if (coverage == null) {
            return;
        }
        /*
         * Obtention du répertoire dans lequel écrire les images des descripteurs.
         * Ce répertoire sera créé si nécessaire.
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
         * En cas d'erreur ou d'avertissement, écriture d'un fichier de commentaires.
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
     * Retourne le nom de fichier dans lequel sera {@linkplain #save enregistrée} l'image, ainsi que
     * son répertoire de destination. Si un fichier a été spécifié explicitement par un appel à la
     * méthode {@link #setFile setFile}, alors ce fichier est retourné. Sinon, le fichier est déterminé
     * à partir de l'entrée spécifiée au {@linkplain #CoverageBuilder(CoverageReference) constructeur}
     * en testant d'abord la valeur retournée par {@link CoverageReference#getFile()}. Si cette dernière
     * est nulle, alors le nom de fichier sera déterminé à partir de {@link CoverageReference#getURL()}
     * et le répertoire de destination sera le répertoire courant.
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
     * Définit le nom de fichier dans lequel sera {@linkplain #save enregistrée} l'image, ainsi que
     * son répertoire de destination. L'appel de cette méthode remplace toute valeur précédemment
     * calculée par {@link #getFile}. Un argument {@code null} rétablit la valeur par défaut.
     */
    public void setFile(final File file) {
        this.file = file;
    }

    /**
     * Enregistre l'image. Le nom de fichier ainsi que le répertoire de destination seront obtenus
     * par {@link #getFile}. Le format de l'image sera déterminé à partir de l'extension du nom de
     * fichier.
     *
     * @throws IOException si l'enregistrement de l'image a échouée.
     */
    public void save() throws IOException {
        final File   file     = getFile();
        final String filename = file.getName();
        if (listener != null) {
            listener.setDescription("Enregistrement de l'image \"" + filename + '"');
            listener.started();
        }
        /*
         * Obtient un encodeur pour l'image. L'encodeur est déduit à partir
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
         * Spécifie la sortie, en donnant directement l'objet File à l'encodeur s'il l'accepte.
         * Sinon, on créera le flot de sortie nous-même, sans oublier de le fermer à la fin.
         */
        final ImageOutputStream out;
        if (contains(writer.getOriginatingProvider().getOutputTypes(), File.class)) {
            writer.setOutput(file);
            out = null;
        } else {
            out = ImageIO.createImageOutputStream(file);
        }
        /*
         * Procède à l'enregistrement de l'image, puis libère les ressources.
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
         * Enregistre une version constrastée de l'image.
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
                Logger.getLogger("net.sicade.observation.coverage").warning("Modèle de couleurs incompatible.");
            }
        }
    }

    /**
     * Indique si la liste de types spécifiée contient le candidat spécifié.
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
     * Ajoute une nouvelle entrée dans la base de données, qui correspondra à l'image juste
     * après {@code lastSuccessful}.
     *
     * @param  lastSuccessful Dernière image déclarée dans la base de données.
     * @param  datePattern    Nomenclature du nom de fichier, avec la date en heure locale.
     *                        Notez que l'heure "locale" peut avoir été définie comme étant
     *                        l'heure "UTC" par la methode {@link #main}.
     * @return Un singleton contenant l'image ajoutée, ou {@code null}.
     * @throws CatalogException si une erreur est survenue lors de l'accès à la base de données.
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

        // Etendue géographique et taille de l'image
        final GeographicBoundingBox bbox = lastSuccessful.getGeographicBoundingBox();
        final Dimension size = lastSuccessful.getGridGeometry().getGridRange2D().getSize();

        // Ajout de l'entrée
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
     * Appelée lorsqu'une exception non-fatale est survenue.
     */
    private static void unexpectedException(final String method, final Exception exception) {
        Utilities.unexpectedException("net.sicade.observation.coverage", "CoverageBuilder", method, exception);
    }

    /**
     * Procède à la création de toutes les nouvelles images des séries spécifiées. Les images qui
     * existent déjà seront sautées. Les progrès sont affichés sur le périphérique de sortie standard.
     * Cette méthode peut être appelée à partir de la ligne de commande. Les arguments acceptées sont
     * énumérés ci-dessous. Tous ces arguments sont optionels. Les arguments {@code -xmin}, {@code -xmax},
     * {@code -ymin} et {@code -ymax} servent à limiter la consommation de mémoire en évitant de charger
     * la totalité des images lorsque seule une sous-région nous intéresse. Si un de ces paramètres est
     * spécifié, alors ils doivent l'être tous. L'argument {@code -limit} sert essentiellement à tester
     * un modèle linéaire sur quelques images seulement.
     * <p>
     * <table>
     *   <tr>
     *     <td nowrap>{@code -xmin} <var>x</var></td>
     *     <td>Limite ouest (en degrés de longitude) des données à charger en mémoire.</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>{@code -xmax} <var>x</var></td>
     *     <td>Limite est (en degrés de longitude) des données à charger en mémoire.</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>{@code -ymin} <var>y</var></td>
     *     <td>Limite sud (en degrés de latitude) des données à charger en mémoire.</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>{@code -ymax} <var>y</var></td>
     *     <td>Limite nord (en degrés de latitude) des données à charger en mémoire.</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>{@code -limit} <var>n</var></td>
     *     <td>Nombre maximal d'images à générer.</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>{@code -overwrite-last} <var>n</var></td>
     *     <td>Si des images existent déjà, nombre d'images à écraser parmis les plus récentes.
     *         Par exemple la valeur 5 recalculera inconditionnellement les 5 dernières images
     *         même si elles existent déjà.</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>{@code -cache} <var>n</var></td>
     *     <td>Taille (en mega octets) à alouer à la cache des tuiles de JAI.</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>{@code -save-descriptors}</td>
     *     <td>Enregistre une image des descripteurs servant au calcul du modèle.</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>{@code -save-contrasted}</td>
     *     <td>Enregistre une version contrastée de l'image en plus de la version habituelle.</td>
     *   </tr>
     *   <tr>
     *     <td nowrap>{@code -date-pattern}</td>
     *     <td>Nomenclature du nom de l'image (date incluse, en heure UTC). Si l'utilisateur ne fournit pas de 
     *      {@code -date-pattern}, alors on ne rajoute pas d'entrée dans la table "{@code GridCoverages}".</td>
     *   </tr>
     * </table>
     *
     * @param  args Noms des séries pour lesquelles on veut créer des images. Exemple:
     *              {@code "Potentiel de pêche ALB-optimal (Calédonie)"}.
     * @throws CatalogException si une exception est survenue lors de l'interrogation de la base de données.
     * @throws IOException si une exception est survenue lors de l'écriture de l'image.
     */
    public static void main(String[] args) throws CatalogException, IOException {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        /*
         * Extraction des arguments et configuration globale (par exemple quantité
         * de mémoire allouée à la cache des tuiles) en fonction de ces arguments.
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
         * Calcul des modèles linéaires pour chacune des séries déclarées sur la ligne de commande.
         * Les séries non-trouvées où les modèles linéaires non-définis provoqueront l'arrêt du
         * programme après affichage d'un message d'erreur à peu-près propre.
         */
        nextSeries: for (int i=0; i<args.length; i++) {
            out.print("Traitement de la série \"");
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
                out.println("Aucun modèle numérique n'est défini pour cette série.");
                return;
            }
            int remaining = (limit!=null) ? limit : Integer.MAX_VALUE;
            CoverageReference lastSuccessful = null; // La dernière référence traitée avec succès.
            Set<CoverageReference> references = series.getCoverageReferences();
            int skipIfExist = (overwrite != null) ? references.size() - overwrite : Integer.MAX_VALUE;
            /*
             * Pour chaque reférences déclarées dans la base de données pour la série courante,
             * vérifie si le fichier correspondant à l'image existe. Les images déjà existantes
             * seront ignorées silencieusement.
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
                     * Erreur d'accès au catalogue. C'est peut-être sérieux, alors on laisse l'exception
                     * se propager après avoir sauté la ligne sur laquelle on écrivait les progrès (afin
                     * que le nom de fichier ne soit pas écrasé).
                     */
                    out.println();
                    throw exception;
                } catch (CannotEvaluateException exception) {
                    /*
                     * Donnée manquante. C'est une erreur très courante, alors on écrit un message
                     * pour l'utilisateur sans le "stack trace qui fait peur", et on passe gentiment
                     * à la série suivante (on n'arrête pas complètement le programme parce qu'il peut
                     * être normal que l'image la plus récente n'aie pas encore toutes les données, et
                     * on ne pas que cette "erreur" empêche le traitement des séries suivantes).
                     */
                    out.println();
                    out.println(exception.getLocalizedMessage());
                    if (exception instanceof OrdinateOutsideCoverageException) {
                        final Envelope envelope = ((OrdinateOutsideCoverageException) exception).getCoverageEnvelope();
                        if (envelope != null) {
                            out.print("L'enveloppe des données source est ");
                            out.println(envelope);
                        }
                    }
                    out.print("L'enveloppe de la destination est  ");
                    out.println(builder.coverage.getEnvelope());
                    continue nextSeries;
                } catch (RuntimeException exception) {
                    // Tout autre type d'erreur. Même traitement que CatalogException.
                    out.println();
                    throw exception;
                }
                /*
                 * Affiche des statistiques sur l'image, ainsi que sur les ressources utilisées.
                 * Ces dernières informations servent notamment à ajuster la valeur du paramètre
                 * -Xmx spécifié au démarrage du Java. Puis procède à l'enregistrement de l'image.
                 */
                final MemoryUsage mem = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
                out.println(stats);
                out.print("Mémoire utilisée: ");
                out.print(mem.getUsed() / (1024*1024));
                out.print(" sur ");
                out.print(mem.getCommitted() / (1024*1024));
                out.println(" Mb réservé.");
                out.println(separator);
                out.println();
                builder.save();
                if (--remaining <= 0) {
                    return;
                }
            } while ((references = addNextEntry(lastSuccessful, datePattern)) != null);
            /*
             * La ligne précédente ajoute dans la base de données une entrée pour la prochaine image.
             * Cet ajout ne sera effectué que si une image existe ou a été créée pour toutes les entrées
             * existantes, de sorte que si on n'ajoutait pas d'images, il n'y aurait plus de prochaine
             * exécution du calcul du modèle linéaire.
             */
        }
    }
}
