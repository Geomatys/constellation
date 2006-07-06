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

// Utilitaires
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.LogRecord;

// G�om�tries et positions g�ographiques
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import org.geotools.resources.geometry.XRectangle2D;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;

// Base de donn�es en entr�s/sorties
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
import net.sicade.resources.seagis.Resources;
import net.sicade.resources.seagis.ResourceKeys;


/**
 * Connexion vers une table d'images. Cette table contient des r�f�rences vers des images sous
 * forme d'objets {@link CoverageReference}. Une table {@code GridCoverageTable} est capable
 * de fournir la liste des images qui interceptent une certaines r�gion g�ographique et une
 * certaine plage de dates.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
@Use({FormatTable.class, CoordinateReferenceSystemTable.class})
@UsedBy(SeriesTable.class)
public class GridCoverageTable extends BoundedSingletonTable<CoverageReference> implements DataConnection {
    /**
     * Requ�te SQL utilis�e pour obtenir l'enveloppe spatio-temporelle couverte
     * par toutes les images d'une s�rie (ou de l'ensemble des s�ries).
     */
     static final ConfigurationKey BOUNDING_BOX = new ConfigurationKey("GridCoverages:BOX",
            "SELECT MIN(\"startTime\") "           + "AS \"tmin\", "  +
                   "MAX(\"endTime\") "             + "AS \"tmax\", "  +
                   "MIN(\"westBoundLongitude\") "  + "AS \"xmin\", "  +
                   "MAX(\"eastBoundLongitude\") "  + "AS \"xmax\", "  +
                   "MIN(\"southBoundLatitude\") "  + "AS \"ymin\", "  +
                   "MAX(\"northBoundLatitude\") "  + "AS \"ymax\"\n"  +
             "  FROM \"GridCoverages\"\n"          +
             "  JOIN \"GeographicBoundingBoxes\""  + " ON extent=\"GeographicBoundingBoxes\".id\n" +
             "  JOIN \"SubSeries\""                + " ON subseries=\"SubSeries\".identifier\n"    +
             " WHERE (  \"endTime\" IS NULL OR   \"endTime\" >= ?)\n"                              +
             "   AND (\"startTime\" IS NULL OR \"startTime\" <= ?)\n"                              +
             "   AND (\"eastBoundLongitude\">=? AND \"westBoundLongitude\"<=?)\n"                  +
             "   AND (\"northBoundLatitude\">=? AND \"southBoundLatitude\"<=?)\n"                  +
             "   AND (series LIKE ?) AND visible=TRUE\n");

    /**
     * Requ�te SQL utilis�e par cette classe pour obtenir la liste des images.
     * L'ordre des colonnes est essentiel. Ces colonnes sont r�f�renc�es par
     * les constantes {@link #SERIES}, {@link #FILENAME} et compagnie.
     */
    private static final ConfigurationKey LIST = new ConfigurationKey("GridCoverages:LIST",
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
                        "width, "                 +  // [11] WIDTH
                        "height, "                +  // [12] HEIGHT
                        "\"CRS\", "               +  // [13] CRS
                        "format, "                +  // [14] FORMAT
                        "NULL AS remarks\n"       +  // [15] REMARKS
             "  FROM \"GridCoverages\"\n"                                                         +
             "  JOIN \"GeographicBoundingBoxes\"" + " ON extent=\"GeographicBoundingBoxes\".id\n" +
             "  JOIN \"SubSeries\""               + " ON subseries=\"SubSeries\".identifier\n"    +
             " WHERE (  \"endTime\" IS NULL OR   \"endTime\" >= ?)\n"                             +
             "   AND (\"startTime\" IS NULL OR \"startTime\" <= ?)\n"                             +
             "   AND (\"eastBoundLongitude\">=? AND \"westBoundLongitude\"<=?)\n"                 +
             "   AND (\"northBoundLatitude\">=? AND \"southBoundLatitude\"<=?)\n"                 +
             "   AND (series LIKE ?) AND (filename LIKE ?) AND visible=TRUE\n"                    +
             " ORDER BY \"endTime\", subseries"); // DOIT �tre en ordre chronologique.
                                                  // Voir {@link GridCoverageEntry#compare}.


    /** Num�ro d'argument. */         static final int ARGUMENT_SERIES   = 7;
    /** Num�ro d'argument. */ private static final int ARGUMENT_FILENAME = 8;

    /** Num�ro de colonne. */ private static final int SERIES     =  1;
    /** Num�ro de colonne. */ private static final int SUB_SERIES =  2;
    /** Num�ro de colonne. */ private static final int PATHNAME   =  3;
    /** Num�ro de colonne. */ private static final int FILENAME   =  4;
    /** Num�ro de colonne. */ private static final int START_TIME =  5;
    /** Num�ro de colonne. */ private static final int END_TIME   =  6;
    /** Num�ro de colonne. */ private static final int XMIN       =  7;
    /** Num�ro de colonne. */ private static final int XMAX       =  8;
    /** Num�ro de colonne. */ private static final int YMIN       =  9;
    /** Num�ro de colonne. */ private static final int YMAX       = 10;
    /** Num�ro de colonne. */ private static final int WIDTH      = 11;
    /** Num�ro de colonne. */ private static final int HEIGHT     = 12;
    /** Num�ro de colonne. */ private static final int CRS        = 13;
    /** Num�ro de colonne. */ private static final int FORMAT     = 14;
    /** Num�ro de colonne. */ private static final int REMARKS    = 15;

    /**
     * Le mod�le � utiliser pour formatter des angles.
     */
    static final String ANGLE_PATTERN = "D�MM.m'";

    /**
     * R�ference vers la s�rie d'images.
     */
    private Series series;

    /**
     * L'op�ration � appliquer sur les images lue, ou {@code null} s'il n'y en a aucune.
     */
    private Operation operation;

    /**
     * Dimension logique (en degr�s de longitude et de latitude) d�sir�e des pixels
     * de l'images. Cette information n'est qu'approximative. Il n'est pas garantie
     * que les lectures produiront effectivement des images de cette r�solution.
     * Une valeur nulle signifie que les lectures doivent se faire avec la meilleure
     * r�solution possible.
     */
    private Dimension2D resolution;

    /**
     * Index des ordonn�es dans une position g�ographique qui correspondent aux coordonn�es
     * (<var>x</var>,<var>y</var>) dans une image.
     *
     * @todo Cod�s en dure pour l'instant. Peut avoir besoin d'�tre param�trables dans une
     *       version future.
     */
    private static final int xDimension=0, yDimension=1;

    /**
     * Formatteur � utiliser pour �crire des dates pour le journal. Les caract�res et les
     * conventions linguistiques d�pendront de la langue de l'utilisateur. Toutefois, le
     * fuseau horaire devrait �tre celui de la r�gion d'�tude (ou GMT) plut�t que celui
     * du pays de l'utilisateur.
     */
    private final DateFormat dateFormat;

    /**
     * Table des syst�mes de coordonn�es. Ne sera construit que la premi�re fois o� elle
     * sera n�cessaire.
     */
    private transient CoordinateReferenceSystemTable crsTable;

    /**
     * Table des formats. Cette table ne sera construite que la premi�re fois
     * o� elle sera n�cessaire.
     */
    private transient FormatTable formatTable;

    /**
     * Le comparateur � utiliser pour choisir une image parmis un ensemble d'images interceptant
     * les coordonn�es spatio-temporelles sp�cifi�es. Ne sera construit que la premi�re fois o�
     * il sera n�cessaire.
     */
    private transient CoverageComparator comparator;

    /**
     * Envelope spatio-temporelle couvertes par l'ensemble des images de cette table, ou
     * {@code null} si elle n'a pas encore �t� d�termin�e. Cette envelope est calcul�e par
     * {@link BoundedSingletonTable#getEnvelope} et cach�e ici pour des raisons de performances.
     */
    private transient Envelope envelope;

    /**
     * Derniers param�tres � avoir �t� construit. Ces param�tres sont
     * retenus afin d'�viter d'avoir � les reconstruires trop souvent
     * si c'est �vitable.
     */
    private transient Parameters parameters;

    /**
     * Une vue tri-dimensionnelle de toutes les donn�es d'une s�rie.
     * Ne sera construite que la premi�re fois o� elle sera n�cessaire.
     */
    private transient CoverageStack coverage3D;

    /**
     * Une instance d'une coordonn�es � utiliser avec {@link #evaluate}.
     */
    private transient GeneralDirectPosition position;

    /**
     * Un buffer pr�-allou� � utiliser avec {@link #evaluate}.
     */
    private transient double[] samples;

    /**
     * Construit une table pour la connexion sp�cifi�e.
     *
     * @param  database Connexion vers la base de donn�es d'observations.
     */
    public GridCoverageTable(final Database database) {
        super(database, net.sicade.observation.sql.CRS.XYT);
        this.dateFormat = DateFormat.getDateInstance(DateFormat.LONG);
        this.dateFormat.setTimeZone(database.getTimeZone());
    }

    /**
     * Construit une nouvelle table avec la m�me configuration initiale que celle de la table
     * sp�cifi�e.
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
     * Retourne la r�f�rence vers la s�ries d'images.
     */
    public Series getSeries() {
        return series;
    }

    /**
     * D�finit la s�rie dont on veut les images.
     *
     * @param  series R�ference vers la s�rie d'images.
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
     * D�finit la p�riode de temps d'int�r�t (dans laquelle rechercher des images).
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
     * D�finit la r�gion g�ographique d'int�r�t dans laquelle rechercher des images.
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
     * Retourne la dimension d�sir�e des pixels de l'images.
     *
     * @return R�solution pr�f�r�e, ou {@code null} si la lecture doit se faire avec
     *         la meilleure r�solution disponible.
     */
    public synchronized Dimension2D getPreferredResolution() {
        return (resolution!=null) ? (Dimension2D)resolution.clone() : null;
    }

    /**
     * D�finit la dimension d�sir�e des pixels de l'images.  Cette information n'est
     * qu'approximative. Il n'est pas garantie que la lecture produira effectivement
     * des images de cette r�solution. Une valeur nulle signifie que la lecture doit
     * se faire avec la meilleure r�solution disponible.
     *
     * @param  pixelSize Taille pr�f�r�e des pixels, en degr�s de longitude et de latitude.
     */
    public synchronized void setPreferredResolution(final Dimension2D pixelSize) {
        if (!Utilities.equals(resolution, pixelSize)) {
            clearCache();
            final int cl�;
            final Object param;
            if (pixelSize != null) {
                resolution = (Dimension2D)pixelSize.clone();
                cl� = ResourceKeys.SET_RESOLUTION_$3;
                param = new Object[] {
                    new Double(resolution.getWidth()),
                    new Double(resolution.getHeight()),
                    series.getName()
                };
            } else {
                resolution = null;
                cl� = ResourceKeys.UNSET_RESOLUTION_$1;
                param = series.getName();
            }
            fireStateChanged("PreferredResolution");
            log("setPreferredResolution", Level.CONFIG, cl�, param);
        }
    }

    /**
     * Retourne l'op�ration appliqu�e sur les images lues. L'op�ration retourn�e
     * peut repr�senter par exemple un gradient. Si aucune op�ration n'est appliqu�e
     * (c'est-�-dire si les images retourn�es repr�sentent les donn�es originales),
     * alors cette m�thode retourne {@code null}.
     */
    public Operation getOperation() {
        return operation;
    }

    /**
     * D�finit l'op�ration � appliquer sur les images lues.
     *
     * @param  operation L'op�ration � appliquer sur les images, ou {@code null} pour
     *         n'appliquer aucune op�ration.
     */
    public synchronized void setOperation(final Operation operation) {
        if (!Utilities.equals(operation, this.operation)) {
            clearCache();
            this.operation = operation;
            final int cl�;
            final Object param;
            if (operation != null) {
                param = new String[] {operation.getName(), series.getName()};
                cl�   = ResourceKeys.SET_OPERATION_$2;
            } else {
                param = series.getName();
                cl�   = ResourceKeys.UNSET_OPERATION_$1;
            }
            fireStateChanged("Operation");
            log("setOperation", Level.CONFIG, cl�, param);
        }
    }

    /**
     * Retourne la liste des images disponibles dans la plage de coordonn�es spatio-temporelles
     * pr�alablement s�lectionn�es. Ces plages auront �t� sp�cifi�es � l'aide des diff�rentes
     * m�thodes {@code set...} de cette classe.
     *
     * @return Liste d'images qui interceptent la plage de temps et la r�gion g�ographique d'int�r�t.
     * @throws CatalogException si un enregistrement est invalide.
     * @throws SQLException si la base de donn�es n'a pas pu �tre interrog�e pour une autre raison.
     */
    @Override
    public Set<CoverageReference> getEntries() throws CatalogException, SQLException {
        if (envelope == null) {
            /*
             * getEnvelope() doit �tre appel�e au moins une fois (sauf si l'enveloppe n'a
             * pas chang�) avant super.getEntries() afin d'�viter que le java.sql.Statement
             * de QueryType.LIST ne soit ferm� en pleine it�ration pour ex�cuter le Statement
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
                 * V�rifie si une entr�e existait d�j� pr�c�demment pour les m�mes coordonn�es
                 * spatio-temporelle mais une autre r�solution. Si c'�tait le cas, alors l'entr�e
                 * avec une r�solution proche de la r�solution demand�e sera retenue et les autres
                 * retir�es de la liste.
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
     * Retourne une des images disponibles dans la plage de coordonn�es spatio-temporelles
     * pr�alablement s�lectionn�es. Si plusieurs images interceptent la r�gion et la plage
     * de temps (c'est-�-dire si {@link #getEntries} retourne un tableau d'au moins deux
     * entr�es), alors le choix de l'image se fera en utilisant un objet
     * {@link CoverageComparator} par d�faut.
     *
     * @return Une image choisie arbitrairement dans la r�gion et la plage de date
     *         s�lectionn�es, ou {@code null} s'il n'y a pas d'image dans ces plages.
     * @throws CatalogException si un enregistrement est invalide.
     * @throws SQLException si la base de donn�es n'a pas pu �tre interrog�e pour une autre raison.
     */
    public synchronized CoverageReference getEntry() throws CatalogException, SQLException {
        /*
         * Obtient la liste des entr�es avant toute op�ration impliquant l'envelope,
         * puisque cette envelope peut avoir �t� calcul�e par 'getEntries()'.
         */
        final Set<CoverageReference> entries = getEntries();
        assert getEnvelope().equals(envelope) : envelope; // V�rifie que l'enveloppe n'a pas chang�e.
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
     * Retourne l'entr�e pour le nom de fichier sp�cifi�. Ces noms sont habituellement unique pour
     * une s�rie donn�e (mais pas obligatoirement). En cas de doublon, une exception sera lanc�e.
     *
     * @param  name Le nom du fichier.
     * @return L'entr�e demand�e, ou {@code null} si {@code name} �tait nul.
     * @throws CatalogException si aucun enregistrement ne correspond au nom demand�,
     *         ou si un enregistrement est invalide.
     * @throws SQLException si l'interrogation de la base de donn�es a �chou� pour une autre raison.
     */
    @Override
    public synchronized CoverageReference getEntry(final String name) throws CatalogException, SQLException {
        if (name == null) {
            return null;
        }
        if (envelope == null) {
            envelope = getEnvelope();
            // Voir le commentaire du code �quivalent de 'getEntries()'
        }
        return super.getEntry(escapeSearch(name));
    }

    /**
     * Obtient les plages de temps et de coordonn�es des images. L'objet retourn� ne contiendra que
     * les informations demand�es. Par exemple si {@link DataAvailability#t} est {@code null}, alors
     * la plage de temps ne sera pas examin�e.
     *
     * @param  ranges L'objet dans lequel ajouter les plages de cette s�ries. Pour chaque champs
     *         nul dans cet objet, les informations correspondantes ne seront pas interrog�es.
     * @return Un objet contenant les plages demand�es. Il ne s'agira pas n�cessairement du m�me
     *         objet que celui qui a �t� sp�cifi� en argument; �a d�pendra si cette m�thode est
     *         appel�e localement ou sur une machine distante.
     * @throws SQLException si la base de donn�es n'a pas pu �tre interrog�e.
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
                        // Il arrive parfois que des images soient prises � toutes les 24 heures,
                        // mais pendant 12 heures seulement. On veut �viter que de telles images
                        // apparaissent tout le temps entrecoup�es d'images manquantes.
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
     * Retourne la requ�te SQL � utiliser pour obtenir des r�f�rences vers des images.
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
     * Retourne l'index de l'argument pour le r�le sp�cifi�. Cette m�thode est r�s�rv�e � un usage
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
     * Configure la requ�te sp�cifi�e. Cette m�thode est appel�e automatiquement lorsque la table
     * a {@linkplain #fireStateChanged chang� d'�tat}.
     */
    @Override
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
     * Retourne l'image correspondant � l'enregistrement courant. Les classes d�riv�es peuvent
     * red�finir cette m�thode si elle souhaite contruire autrement la r�f�rence vers l'image.
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
     * Retourne les param�tres de cette table. Pour des raisons d'�conomie de m�moire (de tr�s
     * nombreux objets {@code Parameters} pouvant �tre cr��s), cette m�thode retourne un exemplaire
     * unique autant que possible. L'objet retourn� ne doit donc pas �tre modifi�!
     * <p>
     * Cette m�thode est appel�e par le constructeur de {@link GridCoverageEntry}.
     *
     * @param  seriesID Nom ID de la s�rie, pour fin de v�rification. Ce nom doit correspondre
     *                  � celui de la s�rie examin�e par cette table.
     * @param  formatID Nom ID du format des images.
     * @param  crsID    Nom ID du syst�me de r�f�rence des coordonn�es.
     * @param  pathname Chemin relatif des images.
     *
     * @return Un objet incluant les param�tres demand�es ainsi que ceux de la table.
     * @throws CatalogException si les param�tres n'ont pas pu �tre obtenus.
     * @throws SQLException si une erreur est survenue lors de l'acc�s � la base de donn�es.
     *
     * @todo L'impl�mentation actuelle n'accepte pas d'autres impl�m�ntations de Format que FormatEntry.
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
         * V�rifie que l'enveloppe n'a pas chang�. Note: getEnvelope() doit avoir �t� appel�e au
         * moins une fois (sauf si elle n'a pas chang�e) juste avant super.getEntries(), afin
         * d'�viter que le java.sql.Statement de QueryType.LIST n'aie �t� ferm� pour ex�cuter
         * le Statement de QueryType.BOUNDING_BOX.
         */
        assert getEnvelope().equals(envelope) : envelope;
        /*
         * Si les param�tres sp�cifi�s sont identiques � ceux qui avaient �t�
         * sp�cifi�s la derni�re fois, retourne le dernier bloc de param�tres.
         */
        if (parameters != null &&
            Utilities.equals(parameters.format     .getName(), formatID) &&
            Utilities.equals(parameters.coverageCRS.getName(), crsID)    &&
            Utilities.equals(parameters.pathname,              pathname))
        {
            return parameters;
        }
        /*
         * Construit un nouveau bloc de param�tres et proj�te les
         * coordonn�es vers le syst�me de coordonn�es de l'image.
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
     * Pr�pare l'�valuation d'un point.
     */
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
     * Vide la cache de toutes les r�f�rences vers les entr�es pr�c�demment cr��es.
     */
    @Override
    protected void clearCache() {
        super.clearCache();
        clearCacheKeepEntries();
    }

    /**
     * R�initialise les caches, mais en gardant les r�f�rences vers les entr�es d�j� cr��es.
     * Cette m�thode devrait �tre appell�e � la place de {@link #clearCache} lorsque l'�tat
     * de la table a chang�, mais que cet �tat n'affecte pas les prochaines entr�es � cr�er.
     */
    private void clearCacheKeepEntries() {
        coverage3D = null;
        parameters = null;
        comparator = null;
        envelope   = null;
    }

    /**
     * Enregistre un �v�nement dans le journal.
     */
    private void log(final String method, final Level level, final int cl�, final Object param) {
        final Resources resources = Resources.getResources(database.getLocale());
        final LogRecord record = resources.getLogRecord(level, cl�, param);
        record.setSourceClassName("CoverageTable");
        record.setSourceMethodName(method);
        CoverageReference.LOGGER.log(record);
    }

    /**
     * Retourne une cha�ne de caract�res d�crivant cette table.
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
