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

// Utilitaires
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.LogRecord;

// Géométries et positions géographiques
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import org.geotools.resources.geometry.XRectangle2D;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;

// Base de données en entrés/sorties
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.text.DateFormat;
import java.io.IOException;

// Geotools et GeoAPI
import org.geotools.measure.Latitude;
import org.geotools.measure.Longitude;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.coverage.CoverageStack;
import org.geotools.resources.Utilities;
import org.opengis.coverage.Coverage;

// Sicade
import net.sicade.observation.ConfigurationKey;
import net.sicade.observation.CatalogException;
import net.sicade.observation.coverage.Series;
import net.sicade.observation.coverage.Operation;
import net.sicade.observation.coverage.CoverageReference;
import net.sicade.observation.coverage.DataAvailability;
import net.sicade.observation.coverage.CoverageComparator;
import net.sicade.observation.coverage.rmi.DataConnection;
import net.sicade.observation.sql.BoundedSingletonTable;
import net.sicade.observation.sql.Role;
import net.sicade.observation.sql.Use;
import net.sicade.observation.sql.UsedBy;
import net.sicade.observation.sql.Database;
import net.sicade.observation.sql.QueryType;
import net.sicade.resources.i18n.Resources;
import net.sicade.resources.i18n.ResourceKeys;


/**
 * Connexion vers une table d'images. Cette table contient des références vers des images sous
 * forme d'objets {@link CoverageReference}. Une table {@code GridCoverageTable} est capable
 * de fournir la liste des images qui interceptent une certaines région géographique et une
 * certaine plage de dates.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
@Use({FormatTable.class, CoordinateReferenceSystemTable.class})
@UsedBy(SeriesTable.class)
public class GridCoverageTable extends BoundedSingletonTable<CoverageReference> implements DataConnection {
    /**
     * Requête SQL utilisée pour obtenir l'enveloppe spatio-temporelle couverte
     * par toutes les images d'une série (ou de l'ensemble des séries).
     */
     static final ConfigurationKey BOUNDING_BOX = new ConfigurationKey("GridCoverages:BOX",
            "SELECT MIN(\"startTime\") "           + "AS \"tmin\", "  +
                   "MAX(\"endTime\") "             + "AS \"tmax\", "  +
                   "\"spatialExtent\"\n"           +
             "  FROM \"GridCoverages\"\n"          +
             "  JOIN \"GridGeometries\""           + " ON extent=\"GridGeometries\".id\n" +
             "  JOIN \"Series\""                   + " ON layer=\"Series\".identifier\n"  +
             " WHERE (  \"endTime\" IS NULL OR   \"endTime\" >= ?)\n"                     +
             "   AND (\"startTime\" IS NULL OR \"startTime\" <= ?)\n"                     +
             "   AND (\"spatialExtent\" && ?)\n"                                          +
             "   AND (series LIKE ?) AND visible=TRUE\n");
     
     /**
      * Requête SQL utilisée pour obtenir l'enveloppe spatio-temporelle couverte
      * par toutes les images d'une série (ou de l'ensemble des séries). Requête 
      * utilisée avec des bases JavaDB.
      */
     static final ConfigurationKey BOUNDING_BOX_JAVADB = new ConfigurationKey("GridCoverages:BOX",
            "SELECT MIN(\"startTime\") "           + "AS \"tmin\", "  +
                   "MAX(\"endTime\") "             + "AS \"tmax\", "  +
                   "MIN(\"westBoundLongitude\") "  + "AS \"xmin\", "  +
                   "MAX(\"eastBoundLongitude\") "  + "AS \"xmax\", "  +
                   "MIN(\"southBoundLatitude\") "  + "AS \"ymin\", "  +
                   "MAX(\"northBoundLatitude\") "  + "AS \"ymax\"\n"  +
                   "MIN(\"altitudeMin\") "         + "AS \"zmin\"\n"  +
                   "MAX(\"altitudeMax\") "         + "AS \"zmax\"\n"  +
             "  FROM \"GridCoverages\"\n"          +
             "  JOIN \"GridGeometries\""           + " ON extent=\"GridGeometries\".id\n" +
             "  JOIN \"Series\""                   + " ON layer=\"Series\".identifier\n"  +
             " WHERE (  \"endTime\" IS NULL OR   \"endTime\" >= ?)\n"                     +
             "   AND (\"startTime\" IS NULL OR \"startTime\" <= ?)\n"                     +
             "   AND (\"eastBoundLongitude\">=? AND \"westBoundLongitude\"<=?)\n"         +
             "   AND (\"northBoundLatitude\">=? AND \"southBoundLatitude\"<=?)\n"         +
             "   AND (series LIKE ?) AND visible=TRUE\n");

    /**
     * Requête SQL utilisée par cette classe pour obtenir la liste des images.
     * L'ordre des colonnes est essentiel. Ces colonnes sont référencées par
     * les constantes {@link #SERIES}, {@link #FILENAME} et compagnie.
     */
    private static final ConfigurationKey LIST = new ConfigurationKey("GridCoverages:LIST",
            "SELECT " + "series, "                +  // [01] SERIES
                        "subseries, "             +  // [02] SUB_SERIES
                        "pathname, "              +  // [03] PATHNAME
                        "filename, "              +  // [04] FILENAME
                      "\"startTime\", "           +  // [05] START_TIME
                      "\"endTime\", "             +  // [06] END_TIME
                      "\"spatialExtent\", "       +  // [07] SPATIAL_EXTENT
                        "width, "                 +  // [08] WIDTH
                        "height, "                +  // [09] HEIGHT
                        "depth, "                 +  // [10] DEPTH
                        "\"CRS\", "               +  // [11] CRS
                        "format, "                +  // [12] FORMAT
                        "NULL AS remarks\n"       +  // [13] REMARKS
             "  FROM \"GridCoverages\"\n"                                                +
             "  JOIN \"GridGeometries\""          + " ON extent=\"GridGeometries\".id\n" +
             "  JOIN \"Series\""                  + " ON layer=\"Series\".identifier\n"  +
             " WHERE (  \"endTime\" IS NULL OR   \"endTime\" >= ?)\n"                    +
             "   AND (\"startTime\" IS NULL OR \"startTime\" <= ?)\n"                    +
             "   AND (\"spatialExtent\" && ?)\n"                                         +
             "   AND (series LIKE ?) AND (filename LIKE ?) AND visible=TRUE\n"           +
             " ORDER BY \"endTime\", series"); // DOIT être en ordre chronologique.
                                                  // Voir {@link GridCoverageEntry#compare}.
    
    /**
     * Requête SQL utilisée par cette classe pour obtenir la liste des images.
     * L'ordre des colonnes est essentiel. Ces colonnes sont référencées par
     * les constantes {@link #SERIES}, {@link #FILENAME} et compagnie. 
     * Requête utilisée avec des bases JavaDB.
     */
    private static final ConfigurationKey LIST_JAVADB = new ConfigurationKey("GridCoverages:LIST",
            "SELECT " + "series, "                +  // [01] SERIES
                        "subseries, "             +  // [02] SUB_SERIES
                        "pathname, "              +  // [03] PATHNAME
                        "filename, "              +  // [04] FILENAME
                      "\"startTime\", "           +  // [05] START_TIME
                      "\"endTime\", "             +  // [06] END_TIME
                      "\"westBoundLongitude\", "  +  // [07] XMIN
                      "\"eastBoundLongitude\", "  +  // [08] XMAX
                      "\"southBoundLatitude\", "  +  // [09] YMIN
                      "\"northBoundLatitude\", "  +  // [10] YMAX
                      "\"altitudeMin\", "         +  // [11] ZMIN
                      "\"altitudeMax\", "         +  // [12] ZMAX
                        "width, "                 +  // [13] WIDTH
                        "height, "                +  // [14] HEIGHT
                        "depth, "                 +  // [15] DEPTH
                        "\"CRS\", "               +  // [16] CRS
                        "format, "                +  // [17] FORMAT
                        "NULL AS remarks\n"       +  // [18] REMARKS
             "  FROM \"GridCoverages\"\n"                                                +
             "  JOIN \"GridGeometries\""          + " ON extent=\"GridGeometries\".id\n" +
             "  JOIN \"Series\""                  + " ON layer=\"Series\".identifier\n"  +
             " WHERE (  \"endTime\" IS NULL OR   \"endTime\" >= ?)\n"                    +
             "   AND (\"startTime\" IS NULL OR \"startTime\" <= ?)\n"                    +
             "   AND (\"eastBoundLongitude\">=? AND \"westBoundLongitude\"<=?)\n"        +
             "   AND (\"northBoundLatitude\">=? AND \"southBoundLatitude\"<=?)\n"        +
             "   AND (series LIKE ?) AND (filename LIKE ?) AND visible=TRUE\n"           +
             " ORDER BY \"endTime\", series"); // DOIT être en ordre chronologique.
                                                  // Voir {@link GridCoverageEntry#compare}.


    /** Numéro d'argument. */         static final int ARGUMENT_SERIES   = 7;
    /** Numéro d'argument. */ private static final int ARGUMENT_FILENAME = 8;

    /** Numéro de colonne. */ private static final int SERIES     =  1;
    /** Numéro de colonne. */ private static final int SUB_SERIES =  2;
    /** Numéro de colonne. */ private static final int PATHNAME   =  3;
    /** Numéro de colonne. */ private static final int FILENAME   =  4;
    /** Numéro de colonne. */ private static final int START_TIME =  5;
    /** Numéro de colonne. */ private static final int END_TIME   =  6;
    /** Numéro de colonne. */ private static final int XMIN       =  7;
    /** Numéro de colonne. */ private static final int XMAX       =  8;
    /** Numéro de colonne. */ private static final int YMIN       =  9;
    /** Numéro de colonne. */ private static final int YMAX       = 10;
    /** Numéro de colonne. */ private static final int WIDTH      = 11;
    /** Numéro de colonne. */ private static final int HEIGHT     = 12;
    /** Numéro de colonne. */ private static final int CRS        = 13;
    /** Numéro de colonne. */ private static final int FORMAT     = 14;
    /** Numéro de colonne. */ private static final int REMARKS    = 15;

    /**
     * Le modèle à utiliser pour formatter des angles.
     */
    static final String ANGLE_PATTERN = "D°MM.m'";

    /**
     * Réference vers la série d'images.
     */
    private Series series;

    /**
     * L'opération à appliquer sur les images lue, ou {@code null} s'il n'y en a aucune.
     */
    private Operation operation;

    /**
     * Dimension logique (en degrés de longitude et de latitude) désirée des pixels
     * de l'images. Cette information n'est qu'approximative. Il n'est pas garantie
     * que les lectures produiront effectivement des images de cette résolution.
     * Une valeur nulle signifie que les lectures doivent se faire avec la meilleure
     * résolution possible.
     */
    private Dimension2D resolution;

    /**
     * Index des ordonnées dans une position géographique qui correspondent aux coordonnées
     * (<var>x</var>,<var>y</var>) dans une image.
     *
     * @todo Codés en dure pour l'instant. Peut avoir besoin d'être paramètrables dans une
     *       version future.
     */
    private static final int xDimension=0, yDimension=1;

    /**
     * Formatteur à utiliser pour écrire des dates pour le journal. Les caractères et les
     * conventions linguistiques dépendront de la langue de l'utilisateur. Toutefois, le
     * fuseau horaire devrait être celui de la région d'étude (ou GMT) plutôt que celui
     * du pays de l'utilisateur.
     */
    private final DateFormat dateFormat;

    /**
     * Table des systèmes de coordonnées. Ne sera construit que la première fois où elle
     * sera nécessaire.
     */
    private transient CoordinateReferenceSystemTable crsTable;

    /**
     * Table des formats. Cette table ne sera construite que la première fois
     * où elle sera nécessaire.
     */
    private transient FormatTable formatTable;

    /**
     * Le comparateur à utiliser pour choisir une image parmis un ensemble d'images interceptant
     * les coordonnées spatio-temporelles spécifiées. Ne sera construit que la première fois où
     * il sera nécessaire.
     */
    private transient CoverageComparator comparator;

    /**
     * Envelope spatio-temporelle couvertes par l'ensemble des images de cette table, ou
     * {@code null} si elle n'a pas encore été déterminée. Cette envelope est calculée par
     * {@link BoundedSingletonTable#getEnvelope} et cachée ici pour des raisons de performances.
     */
    private transient Envelope envelope;

    /**
     * Derniers paramètres à avoir été construit. Ces paramètres sont
     * retenus afin d'éviter d'avoir à les reconstruires trop souvent
     * si c'est évitable.
     */
    private transient Parameters parameters;

    /**
     * Une vue tri-dimensionnelle de toutes les données d'une série.
     * Ne sera construite que la première fois où elle sera nécessaire.
     */
    private transient CoverageStack coverage3D;

    /**
     * Une instance d'une coordonnées à utiliser avec {@link #evaluate}.
     */
    private transient GeneralDirectPosition position;

    /**
     * Un buffer pré-alloué à utiliser avec {@link #evaluate}.
     */
    private transient double[] samples;

    /**
     * Construit une table pour la connexion spécifiée.
     *
     * @param  database Connexion vers la base de données d'observations.
     */
    public GridCoverageTable(final Database database) {
        super(database, net.sicade.observation.sql.CRS.XYT);
        this.dateFormat = DateFormat.getDateInstance(DateFormat.LONG);
        this.dateFormat.setTimeZone(database.getTimeZone());
    }

    /**
     * Construit une nouvelle table avec la même configuration initiale que celle de la table
     * spécifiée.
     */
    public GridCoverageTable(final GridCoverageTable table) {
        super(table);
        series      = table.series;
        operation   = table.operation;
        resolution  = table.resolution;
        dateFormat  = table.dateFormat;
        crsTable    = table.crsTable;
        formatTable = table.formatTable;
        comparator  = table.comparator;
        parameters  = table.parameters;
        coverage3D  = table.coverage3D;
    }

    /**
     * {inheritDoc}
     */
    public GridCoverageTable newInstance(final Operation operation) {
        final GridCoverageTable view = new GridCoverageTable(this);
        view.setOperation(operation);
        return view;
    }

    /**
     * Retourne la référence vers la séries d'images.
     */
    public Series getSeries() {
        return series;
    }

    /**
     * Définit la série dont on veut les images.
     *
     * @param  series Réference vers la série d'images.
     */
    public synchronized void setSeries(final Series series) {
        if (!series.equals(this.series)) {
            clearCacheKeepEntries();
            this.series = series;
            fireStateChanged("Series");
            log("setSeries", Level.CONFIG, ResourceKeys.SET_SERIES_$1, series.getName());
        }
    }

    /**
     * Définit la période de temps d'intérêt (dans laquelle rechercher des images).
     */
    @Override
    public synchronized boolean setTimeRange(final Date startTime, final Date endTime) {
        final boolean change = super.setTimeRange(startTime, endTime);
        if (change) {
            clearCacheKeepEntries();
            final String startText, endText;
            synchronized (dateFormat) {
                startText = dateFormat.format(startTime);
                endText   = dateFormat.format(  endTime);
            }
            log("setTimeRange", Level.CONFIG, ResourceKeys.SET_TIME_RANGE_$3,
                                new String[]{startText, endText, series.getName()});
        }
        return change;
    }

    /**
     * Définit la région géographique d'intérêt dans laquelle rechercher des images.
     */
    @Override
    public synchronized boolean setGeographicBoundingBox(final GeographicBoundingBox area) {
        final boolean change = super.setGeographicBoundingBox(area);
        if (change) {
            clearCache();
            log("setGeographicArea", Level.CONFIG, ResourceKeys.SET_GEOGRAPHIC_AREA_$2, new String[] {
                GeographicBoundingBoxImpl.toString(area, ANGLE_PATTERN, database.getLocale()),
                series.getName()
            });
        }
        return change;
    }

    /**
     * Retourne la dimension désirée des pixels de l'images.
     *
     * @return Résolution préférée, ou {@code null} si la lecture doit se faire avec
     *         la meilleure résolution disponible.
     */
    public synchronized Dimension2D getPreferredResolution() {
        return (resolution!=null) ? (Dimension2D)resolution.clone() : null;
    }

    /**
     * Définit la dimension désirée des pixels de l'images.  Cette information n'est
     * qu'approximative. Il n'est pas garantie que la lecture produira effectivement
     * des images de cette résolution. Une valeur nulle signifie que la lecture doit
     * se faire avec la meilleure résolution disponible.
     *
     * @param  pixelSize Taille préférée des pixels, en degrés de longitude et de latitude.
     */
    public synchronized void setPreferredResolution(final Dimension2D pixelSize) {
        if (!Utilities.equals(resolution, pixelSize)) {
            clearCache();
            final int clé;
            final Object param;
            if (pixelSize != null) {
                resolution = (Dimension2D)pixelSize.clone();
                clé = ResourceKeys.SET_RESOLUTION_$3;
                param = new Object[] {
                    new Double(resolution.getWidth()),
                    new Double(resolution.getHeight()),
                    series.getName()
                };
            } else {
                resolution = null;
                clé = ResourceKeys.UNSET_RESOLUTION_$1;
                param = series.getName();
            }
            fireStateChanged("PreferredResolution");
            log("setPreferredResolution", Level.CONFIG, clé, param);
        }
    }

    /**
     * Retourne l'opération appliquée sur les images lues. L'opération retournée
     * peut représenter par exemple un gradient. Si aucune opération n'est appliquée
     * (c'est-à-dire si les images retournées représentent les données originales),
     * alors cette méthode retourne {@code null}.
     */
    public Operation getOperation() {
        return operation;
    }

    /**
     * Définit l'opération à appliquer sur les images lues.
     *
     * @param  operation L'opération à appliquer sur les images, ou {@code null} pour
     *         n'appliquer aucune opération.
     */
    public synchronized void setOperation(final Operation operation) {
        if (!Utilities.equals(operation, this.operation)) {
            clearCache();
            this.operation = operation;
            final int clé;
            final Object param;
            if (operation != null) {
                param = new String[] {operation.getName(), series.getName()};
                clé   = ResourceKeys.SET_OPERATION_$2;
            } else {
                param = series.getName();
                clé   = ResourceKeys.UNSET_OPERATION_$1;
            }
            fireStateChanged("Operation");
            log("setOperation", Level.CONFIG, clé, param);
        }
    }

    /**
     * Retourne la liste des images disponibles dans la plage de coordonnées spatio-temporelles
     * préalablement sélectionnées. Ces plages auront été spécifiées à l'aide des différentes
     * méthodes {@code set...} de cette classe.
     *
     * @return Liste d'images qui interceptent la plage de temps et la région géographique d'intérêt.
     * @throws CatalogException si un enregistrement est invalide.
     * @throws SQLException si la base de données n'a pas pu être interrogée pour une autre raison.
     */
    @Override
    public Set<CoverageReference> getEntries() throws CatalogException, SQLException {
        if (envelope == null) {
            /*
             * getEnvelope() doit être appelée au moins une fois (sauf si l'enveloppe n'a
             * pas changé) avant super.getEntries() afin d'éviter que le java.sql.Statement
             * de QueryType.LIST ne soit fermé en pleine itération pour exécuter le Statement
             * de QueryType.BOUNDING_BOX.
             */
            envelope = getEnvelope();
        }
        final  Set<CoverageReference> entries  = super.getEntries();
        final List<CoverageReference> filtered = new ArrayList<CoverageReference>(entries.size());
loop:   for (final CoverageReference newReference : entries) {
            if (newReference instanceof GridCoverageEntry) {
                final GridCoverageEntry newEntry = (GridCoverageEntry) newReference;
                /*
                 * Vérifie si une entrée existait déjà précédemment pour les mêmes coordonnées
                 * spatio-temporelle mais une autre résolution. Si c'était le cas, alors l'entrée
                 * avec une résolution proche de la résolution demandée sera retenue et les autres
                 * retirées de la liste.
                 */
                for (int i=filtered.size(); --i>=0;) {
                    final CoverageReference oldReference = filtered.get(i);
                    if (oldReference instanceof GridCoverageEntry) {
                        final GridCoverageEntry oldEntry = (GridCoverageEntry) oldReference;
                        if (!oldEntry.compare(newEntry)) {
                            // Entries not equals according the "ORDER BY" clause.
                            break;
                        }
                        final GridCoverageEntry lowestResolution = oldEntry.getLowestResolution(newEntry);
                        if (lowestResolution != null) {
                            // Two entries has the same spatio-temporal coordinates.
                            if (lowestResolution.hasEnoughResolution(resolution)) {
                                // The entry with the lowest resolution is enough.
                                filtered.set(i, lowestResolution);
                            } else if (lowestResolution == oldEntry) {
                                // No entry has enough resolution;
                                // keep the one with the finest resolution.
                                filtered.set(i, newEntry);
                            }
                            continue loop;
                        }
                    }
                }
            }
            filtered.add(newReference);
        }
        entries.retainAll(filtered);
        log("getEntries", Level.FINE, ResourceKeys.FOUND_COVERAGES_$1, entries.size());
        return entries;
    }

    /**
     * Retourne une des images disponibles dans la plage de coordonnées spatio-temporelles
     * préalablement sélectionnées. Si plusieurs images interceptent la région et la plage
     * de temps (c'est-à-dire si {@link #getEntries} retourne un ensemble d'au moins deux
     * entrées), alors le choix de l'image se fera en utilisant un objet
     * {@link CoverageComparator} par défaut.
     *
     * @return Une image choisie arbitrairement dans la région et la plage de date
     *         sélectionnées, ou {@code null} s'il n'y a pas d'image dans ces plages.
     * @throws CatalogException si un enregistrement est invalide.
     * @throws SQLException si la base de données n'a pas pu être interrogée pour une autre raison.
     */
    public synchronized CoverageReference getEntry() throws CatalogException, SQLException {
        /*
         * Obtient la liste des entrées avant toute opération impliquant l'envelope,
         * puisque cette envelope peut avoir été calculée par 'getEntries()'.
         */
        final Set<CoverageReference> entries = getEntries();
        assert getEnvelope().equals(envelope) : envelope; // Vérifie que l'enveloppe n'a pas changée.
        CoverageReference best = null;
        if (comparator == null) {
            comparator = new CoverageComparator(getCoordinateReferenceSystem(), envelope);
        }
        for (final CoverageReference entry : entries) {
            if (best==null || comparator.compare(entry, best) <= -1) {
                best = entry;
            }
        }
        return best;
    }

    /**
     * Retourne l'entrée pour le nom de fichier spécifié. Ces noms sont habituellement unique pour
     * une série donnée (mais pas obligatoirement). En cas de doublon, une exception sera lancée.
     *
     * @param  name Le nom du fichier.
     * @return L'entrée demandée, ou {@code null} si {@code name} était nul.
     * @throws CatalogException si aucun enregistrement ne correspond au nom demandé,
     *         ou si un enregistrement est invalide.
     * @throws SQLException si l'interrogation de la base de données a échoué pour une autre raison.
     */
    @Override
    public synchronized CoverageReference getEntry(final String name) throws CatalogException, SQLException {
        if (name == null) {
            return null;
        }
        if (envelope == null) {
            envelope = getEnvelope();
            // Voir le commentaire du code équivalent de 'getEntries()'
        }
        return super.getEntry(escapeSearch(name));
    }

    /**
     * Obtient les plages de temps et de coordonnées des images. L'objet retourné ne contiendra que
     * les informations demandées. Par exemple si {@link DataAvailability#t} est {@code null}, alors
     * la plage de temps ne sera pas examinée.
     *
     * @param  ranges L'objet dans lequel ajouter les plages de cette séries. Pour chaque champs
     *         nul dans cet objet, les informations correspondantes ne seront pas interrogées.
     * @return Un objet contenant les plages demandées. Il ne s'agira pas nécessairement du même
     *         objet que celui qui a été spécifié en argument; ça dépendra si cette méthode est
     *         appelée localement ou sur une machine distante.
     * @throws SQLException si la base de données n'a pas pu être interrogée.
     */
    public synchronized DataAvailability getRanges(final DataAvailability ranges) throws SQLException {
        long  lastEndTime       = Long.MIN_VALUE;
        final Calendar calendar = getCalendar();
        final ResultSet  result = getStatement(QueryType.SELECT).executeQuery();
        while (result.next()) {
            if (ranges.t != null) {
                final long timeInterval = Math.round(series.getTimeInterval() * LocationOffsetEntry.DAY);
                final Date    startTime = result.getTimestamp(START_TIME, calendar);
                final Date      endTime = result.getTimestamp(  END_TIME, calendar);
                if (startTime!=null && endTime!=null) {
                    final long lgEndTime = endTime.getTime();
                    final long checkTime = lgEndTime - timeInterval;
                    if (checkTime <= lastEndTime  &&  checkTime < startTime.getTime()) {
                        // Il arrive parfois que des images soient prises à toutes les 24 heures,
                        // mais pendant 12 heures seulement. On veut éviter que de telles images
                        // apparaissent tout le temps entrecoupées d'images manquantes.
                        startTime.setTime(checkTime);
                    }
                    lastEndTime = lgEndTime;
                    ranges.t.add(startTime, endTime);
                }
            }
            if (ranges.x != null) {
                final double xmin = result.getDouble(XMIN);
                final double xmax = result.getDouble(XMAX);
                ranges.x.add(new Longitude(xmin), new Longitude(xmax));
            }
            if (ranges.y != null) {
                final double ymin = result.getDouble(YMIN);
                final double ymax = result.getDouble(YMAX);
                ranges.y.add(new Latitude(ymin), new Latitude(ymax));
            }
        }
        result.close();
        return ranges;
    }

    /**
     * Retourne la requête SQL à utiliser pour obtenir des références vers des images.
     */
    @Override
    protected String getQuery(final QueryType type) throws SQLException {
        switch (type) {
            case SELECT:       // Fall through
            case LIST:         return getProperty(LIST);
            case BOUNDING_BOX: return getProperty(BOUNDING_BOX);
            default:           return super.getQuery(type);
        }
    }

    /**
     * Retourne l'index de l'argument pour le rôle spécifié. Cette méthode est résérvée à un usage
     * interne (indirectement) pour {@link #getEntry(String)}.
     */
    @Override
    protected int getArgumentIndex(final Role role) {
        switch (role) {
            case IDENTIFIER: return ARGUMENT_FILENAME;
            default: return super.getArgumentIndex(role);
        }
    }

    /**
     * Configure la requête spécifiée. Cette méthode est appelée automatiquement lorsque la table
     * a {@linkplain #fireStateChanged changé d'état}.
     */
    @Override
    @SuppressWarnings("fallthrough")
    protected void configure(final QueryType type, final PreparedStatement statement) throws SQLException {
        super.configure(type, statement);
        switch (type) {
            case SELECT: {
                super.configure(QueryType.BOUNDING_BOX, statement);
                // Fall through
            }
            case LIST: {
                statement.setString(ARGUMENT_FILENAME, "%");
                // Fall through
            }
            case BOUNDING_BOX: {
                final String name = series!=null ? series.getName() : null;
                statement.setString(ARGUMENT_SERIES, escapeSearch(name));
                break;
            }
        }
    }

    /**
     * Retourne l'image correspondant à l'enregistrement courant. Les classes dérivées peuvent
     * redéfinir cette méthode si elle souhaite contruire autrement la référence vers l'image.
     */
    @Override
    protected CoverageReference createEntry(final ResultSet result) throws CatalogException, SQLException {
        assert Thread.holdsLock(this);
        final Calendar calendar = getCalendar();
        return new GridCoverageEntry(this, result.getString    (SERIES),
                                           result.getString    (SUB_SERIES),
                                           result.getString    (PATHNAME),
                                           result.getString    (FILENAME),
                                           result.getTimestamp (START_TIME, calendar),
                                           result.getTimestamp (END_TIME,   calendar),
                                           result.getDouble    (XMIN),
                                           result.getDouble    (XMAX),
                                           result.getDouble    (YMIN),
                                           result.getDouble    (YMAX),
                                           result.getShort     (WIDTH),
                                           result.getShort     (HEIGHT),
                                           result.getString    (CRS),
                                           result.getString    (FORMAT),
                                           result.getString    (REMARKS)).canonicalize();
    }

    /**
     * Retourne les paramètres de cette table. Pour des raisons d'économie de mémoire (de très
     * nombreux objets {@code Parameters} pouvant être créés), cette méthode retourne un exemplaire
     * unique autant que possible. L'objet retourné ne doit donc pas être modifié!
     * <p>
     * Cette méthode est appelée par le constructeur de {@link GridCoverageEntry}.
     *
     * @param  seriesID Nom ID de la série, pour fin de vérification. Ce nom doit correspondre
     *                  à celui de la série examinée par cette table.
     * @param  formatID Nom ID du format des images.
     * @param  crsID    Nom ID du système de référence des coordonnées.
     * @param  pathname Chemin relatif des images.
     *
     * @return Un objet incluant les paramètres demandées ainsi que ceux de la table.
     * @throws CatalogException si les paramètres n'ont pas pu être obtenus.
     * @throws SQLException si une erreur est survenue lors de l'accès à la base de données.
     *
     * @todo L'implémentation actuelle n'accepte pas d'autres impléméntations de Format que FormatEntry.
     */
    final synchronized Parameters getParameters(final String seriesID,
                                                final String formatID,
                                                final String crsID,
                                                final String pathname)
            throws CatalogException, SQLException
    {
        final String seriesName = series.getName();
        if (!Utilities.equals(seriesID, seriesName)) {
            throw new CatalogException(Resources.format(ResourceKeys.ERROR_WRONG_SERIES_$1, seriesName));
        }
        /*
         * Vérifie que l'enveloppe n'a pas changé. Note: getEnvelope() doit avoir été appelée au
         * moins une fois (sauf si elle n'a pas changée) juste avant super.getEntries(), afin
         * d'éviter que le java.sql.Statement de QueryType.LIST n'aie été fermé pour exécuter
         * le Statement de QueryType.BOUNDING_BOX.
         */
        assert getEnvelope().equals(envelope) : envelope;
        /*
         * Si les paramètres spécifiés sont identiques à ceux qui avaient été
         * spécifiés la dernière fois, retourne le dernier bloc de paramètres.
         */
        if (parameters != null &&
            Utilities.equals(parameters.format     .getName(), formatID) &&
            Utilities.equals(parameters.coverageCRS.getName(), crsID)    &&
            Utilities.equals(parameters.pathname,              pathname))
        {
            return parameters;
        }
        /*
         * Construit un nouveau bloc de paramètres et projète les
         * coordonnées vers le système de coordonnées de l'image.
         */
        final Rectangle2D geographicArea = XRectangle2D.createFromExtremums(
                            envelope.getMinimum(xDimension), envelope.getMinimum(yDimension),
                            envelope.getMaximum(xDimension), envelope.getMaximum(yDimension));
        if (formatTable == null) {
            formatTable = database.getTable(FormatTable.class);
        }
        if (crsTable == null) {
            crsTable = database.getTable(CoordinateReferenceSystemTable.class);
        }
        parameters = new Parameters(series,
                                    (FormatEntry) formatTable.getEntry(formatID),
                                    pathname.intern(),
                                    operation,
                                    getCoordinateReferenceSystem(),
                                    crsTable.getEntry(crsID),
                                    geographicArea,
                                    resolution,
                                    dateFormat,
                                    getProperty(CoverageReference.ROOT_DIRECTORY),
                                    getProperty(CoverageReference.ROOT_URL),
                                    getProperty(CoverageReference.URL_ENCODING));
        return parameters;
    }

    /**
     * Prépare l'évaluation d'un point.
     */
    @SuppressWarnings("fallthrough")
    private void prepare(final double x, final double y, final double t)
            throws CatalogException, SQLException, IOException
    {
        assert Thread.holdsLock(this);
        if (coverage3D == null) {
            coverage3D = new CoverageStack(getSeries().getName(), getCoordinateReferenceSystem(), getEntries());
            position   = new GeneralDirectPosition(getCoordinateReferenceSystem());
        }
        switch (position.ordinates.length) {
            default: // Fall through in all cases.
            case 3:  position.ordinates[2] = t;
            case 2:  position.ordinates[1] = y;
            case 1:  position.ordinates[0] = x;
            case 0:  break;
        }
    }

    /**
     * {@inheritDoc}
     */
    public synchronized double evaluate(final double x, final double y, final double t, final short band)
            throws CatalogException, SQLException, IOException
    {
        prepare(x, y, t);
        samples = coverage3D.evaluate(position, samples);
        return samples[band];
    }

    /**
     * {@inheritDoc}
     */
    public synchronized double[] snap(final double x, final double y, final double t)
            throws CatalogException, SQLException, IOException
    {
        prepare(x, y, t);
        coverage3D.snap(position);
        return (double[]) position.ordinates.clone();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public synchronized List<Coverage> coveragesAt(final double t)
            throws CatalogException, SQLException, IOException
    {
        prepare(Double.NaN, Double.NaN, t);
        return coverage3D.coveragesAt(t);
    }

    /**
     * Vide la cache de toutes les références vers les entrées précédemment créées.
     */
    @Override
    protected void clearCache() {
        super.clearCache();
        clearCacheKeepEntries();
    }

    /**
     * Réinitialise les caches, mais en gardant les références vers les entrées déjà créées.
     * Cette méthode devrait être appellée à la place de {@link #clearCache} lorsque l'état
     * de la table a changé, mais que cet état n'affecte pas les prochaines entrées à créer.
     */
    private void clearCacheKeepEntries() {
        coverage3D = null;
        parameters = null;
        comparator = null;
        envelope   = null;
    }

    /**
     * Enregistre un évènement dans le journal.
     */
    private void log(final String method, final Level level, final int clé, final Object param) {
        final Resources resources = Resources.getResources(database.getLocale());
        final LogRecord record = resources.getLogRecord(level, clé, param);
        record.setSourceClassName("CoverageTable");
        record.setSourceMethodName(method);
        CoverageReference.LOGGER.log(record);
    }

    /**
     * Retourne une chaîne de caractères décrivant cette table.
     */
    @Override
    public final String toString() {
        String area;
        try {
            area = GeographicBoundingBoxImpl.toString(getGeographicBoundingBox(),
                                                      ANGLE_PATTERN, database.getLocale());
        } catch (CatalogException e) {
            area = e.getLocalizedMessage();
        }
        final StringBuilder buffer = new StringBuilder(Utilities.getShortClassName(this));
        buffer.append("[\"");
        buffer.append(String.valueOf(series));
        buffer.append("\": ");
        buffer.append(area);
        buffer.append(']');
        return buffer.toString();
    }
}
