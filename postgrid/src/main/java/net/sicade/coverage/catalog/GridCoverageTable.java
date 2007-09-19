/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 * (C) 2007, Geomatys
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
package net.sicade.coverage.catalog;

import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.text.DateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.opengis.coverage.Coverage;
import org.opengis.geometry.Envelope;
import org.opengis.geometry.DirectPosition;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;
import org.geotools.geometry.TransformedDirectPosition;
import org.geotools.coverage.CoverageStack;
import org.geotools.util.NumberRange;
import org.geotools.util.RangeSet;
import org.geotools.resources.Utilities;
import org.geotools.resources.geometry.XRectangle2D;

import net.sicade.catalog.CatalogException;
import net.sicade.coverage.model.Operation;
import net.sicade.catalog.BoundedSingletonTable;
import net.sicade.catalog.ConfigurationKey;
import net.sicade.catalog.Database;
import net.sicade.catalog.QueryType;
import net.sicade.resources.i18n.Resources;
import net.sicade.resources.i18n.ResourceKeys;
import static net.sicade.catalog.QueryType.*;


/**
 * Connexion vers une table d'images. Cette table contient des références vers des images sous
 * forme d'objets {@link CoverageReference}. Une table {@code GridCoverageTable} est capable
 * de fournir la liste des images qui interceptent une certaines région géographique et une
 * certaine plage de dates.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class GridCoverageTable extends BoundedSingletonTable<CoverageReference> implements DataConnection {
    /**
     * Le modèle à utiliser pour formatter des angles.
     */
    static final String ANGLE_PATTERN = "D°MM.m'";

    /**
     * Nombre de millisecondes dans une journée.
     */
    private static final double DAY = 24*60*60*1000.0;

    /**
     * Réference vers la couche d'images.
     */
    private Layer layer;

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
     * @todo Codés en dur pour l'instant. Peut avoir besoin d'être paramètrables dans une
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
     * Table of grid geometry. Will be created only when first needed.
     */
    private transient GridGeometryTable gridGeometryTable;

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
     * Derniers paramètres à avoir été construits. Ces paramètres sont
     * retenus afin d'éviter d'avoir à les reconstruires trop souvent
     * si c'est évitable.
     */
    private transient GridCoverageSettings parameters;

    /**
     * The set of available dates. Will be computed by
     * {@link #getAvailableTimes} when first needed.
     */
    private transient SortedSet<Date> availableTimes;

    /**
     * The set of available altitudes. Will be computed by
     * {@link #getAvailableElevations} when first needed.
     */
    private transient SortedSet<Number> availableElevations;

    /**
     * The set of available altitudes for each dates. Will be computed by
     * {@link #getAvailableCentroids} when first needed.
     */
    private transient SortedMap<Date, SortedSet<Number>> availableCentroids;

    /**
     * Une vue tri-dimensionnelle de toutes les données d'une couche.
     * Ne sera construite que la première fois où elle sera nécessaire.
     */
    private transient CoverageStack coverage3D;

    /**
     * Une instance d'une coordonnées à utiliser avec {@link #evaluate}.
     */
    private transient TransformedDirectPosition position;

    /**
     * Un buffer pré-alloué à utiliser avec {@link #evaluate}.
     */
    private transient double[] samples;

    /**
     * Constructs a new {@code GridCoverageTable}.
     *
     * @param connection The connection to the database.
     */
    public GridCoverageTable(final Database database) {
        this(new GridCoverageQuery(database));
    }

    /**
     * Constructs a new {@code GridCoverageTable} from the specified query.
     */
    private GridCoverageTable(final GridCoverageQuery query) {
        super(query, net.sicade.catalog.CRS.XYT);
        setIdentifierParameters(query.byFilename, null);
        setExtentParameters(query.byStartTime, query.byHorizontalExtent);
        this.dateFormat = DateFormat.getDateInstance(DateFormat.LONG);
        this.dateFormat.setTimeZone(getDatabase().getTimeZone());
    }

    /**
     * Constructs a new {@code GridCoverageTable} with the same initial configuration
     * than the specified table.
     */
    public GridCoverageTable(final GridCoverageTable table) {
        super(table);
        layer             = table.layer;
        operation         = table.operation;
        resolution        = table.resolution;
        dateFormat        = table.dateFormat;
        gridGeometryTable = table.gridGeometryTable;
        formatTable       = table.formatTable;
        comparator        = table.comparator;
        parameters        = table.parameters;
        coverage3D        = table.coverage3D;
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
     * Returns the layer for the coverages in this table.
     */
    public Layer getLayer() {
        return layer;
    }

    /**
     * Sets the layer for the coverages in this table.
     */
    public synchronized void setLayer(final Layer layer) {
        if (!layer.equals(this.layer)) {
            clearCacheKeepEntries();
            this.layer = layer;
            fireStateChanged("Layer");
            log("setLayer", Level.CONFIG, ResourceKeys.SET_SERIES_$1, layer.getName());
        }
    }

    /**
     * Sets the later as a string. This methode should be avoided as much as possible -
     * use {@link #setLayer(Layer)} instead. However it still useful for debugging purpose.
     */
    public void setLayer(final String layer) {
        setLayer(new LayerEntry(layer, null, 1, null));
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
                                new String[]{startText, endText, layer.getName()});
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
                GeographicBoundingBoxImpl.toString(area, ANGLE_PATTERN, getDatabase().getLocale()),
                layer.getName()
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
                    layer.getName()
                };
            } else {
                resolution = null;
                clé = ResourceKeys.UNSET_RESOLUTION_$1;
                param = layer.getName();
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
                param = new String[] {operation.getName(), layer.getName()};
                clé   = ResourceKeys.SET_OPERATION_$2;
            } else {
                param = layer.getName();
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
                            // Entries not equal according the "ORDER BY" clause.
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
     * une couche donnée (mais pas obligatoirement). En cas de doublon, une exception sera lancée.
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
        return super.getEntry(name);
    }

    /**
     * Returns the set of dates when a coverage is available. Only the images in
     * the currently {@linkplain #getEnvelope selected envelope} are considered.
     *
     * @return The set of dates.
     * @throws SQLException If an error occured while reading the database.
     * @throws CatalogException if an illegal record was found.
     */
    public synchronized SortedSet<Date> getAvailableTimes()
            throws SQLException, CatalogException
    {
        if (availableTimes == null) {
            final SortedMap<Date, SortedSet<Number>> centroids = getAvailableCentroids();
            /*
             * The above line should have computed 'availableTimes' as a side effect. If it was
             * not the case (for example because the user overrided the method), then cast the
             * centroids key set. Note that the cast is likely to fail; the user would be well
             * advised to override getAvailableTimes() in addition of getAvailableCentroids().
             */
            if (availableTimes == null) {
                availableTimes = (SortedSet<Date>) centroids.keySet();
            }
        }
        return availableTimes;
    }

    /**
     * Returns the set of altitudes where a coverage is available. Only the images in
     * the currently {@linkplain #getEnvelope selected envelope} are considered.
     * <p>
     * If different images have different set of altitudes, then this method returns
     * only the altitudes found in every images.
     *
     * @return The set of altitudes. May be empty, but will never be null.
     * @throws SQLException If an error occured while reading the database.
     * @throws CatalogException if an illegal record was found.
     */
    public synchronized SortedSet<Number> getAvailableElevations()
            throws SQLException, CatalogException
    {
        if (availableElevations == null) {
            final SortedSet<Number> commons = new TreeSet<Number>();
            final SortedMap<Date, SortedSet<Number>> centroids = getAvailableCentroids();
            final Iterator<SortedSet<Number>> iterator = centroids.values().iterator();
            if (iterator.hasNext()) {
                commons.addAll(iterator.next());
                while (iterator.hasNext()) {
                    final SortedSet<Number> altitudes = iterator.next();
                    for (final Iterator<Number> it=commons.iterator(); it.hasNext();) {
                        if (!altitudes.contains(it.next())) {
                            it.remove();
                        }
                    }
                    if (commons.isEmpty()) {
                        break; // No need to continue.
                    }
                }
            }
            availableElevations = Collections.unmodifiableSortedSet(commons);
        }
        return availableElevations;
    }

    /**
     * Returns the available altitudes for each dates. This method returns portion of "centroids",
     * i.e. vertical ranges are replaced by the middle vertical points and temporal ranges are
     * replaced by the middle time. This method considers only the vertical and temporal axis.
     * The horizontal axis are omitted.
     *
     * @return An immutable collection of centroids. Keys are the dates, are values ar the set
     *         of altitudes for that date.
     * @throws SQLException If an error occured while reading the database.
     */
    final synchronized SortedMap<Date, SortedSet<Number>> getAvailableCentroids()
            throws SQLException, CatalogException
    {
        if (availableCentroids == null) {
            final SortedMap<Date,List<String>> centroids = new TreeMap<Date,List<String>>();
            final GridCoverageQuery query     = (GridCoverageQuery) super.query;
            final Calendar          calendar  = getCalendar();
            final PreparedStatement statement = getStatement(QueryType.AVAILABLE_DATA);
            final ResultSet         results   = statement.executeQuery();
            final int startTimeIndex = indexOf(query.startTime);
            final int endTimeIndex   = indexOf(query.endTime);
            final int extentIndex    = indexOf(query.spatialExtent);
            while (results.next()) {
                final Date startTime = results.getTimestamp(startTimeIndex, calendar);
                final Date   endTime = results.getTimestamp(  endTimeIndex, calendar);
                final Date      time;
                if (startTime != null) {
                    if (endTime != null) {
                        time = new Date((startTime.getTime() + endTime.getTime()) / 2);
                    } else {
                        time = new Date(startTime.getTime());
                    }
                } else if (endTime != null) {
                    time = new Date(endTime.getTime());
                } else {
                    continue;
                }
                /*
                 * Now get the spatial extent identifiers. We do not extract the altitudes now,
                 * because many records will typically use the same spatial extents. So we just
                 * extract the identifiers for now, and will get the altitudes only once later.
                 */
                List<String> extents = centroids.get(time);
                if (extents == null) {
                    extents = new ArrayList<String>(1); // We will usually have only one element.
                    centroids.put(time, extents);
                }
                extents.add(results.getString(extentIndex));
            }
            /*
             * Now get the altitudes for all dates. Note: 'availableTimes' must be
             * determined before we wrap 'availableCentroids' in an unmodifiable map.
             */
            if (gridGeometryTable == null) {
                gridGeometryTable = getDatabase().getTable(GridGeometryTable.class);
            }
            availableCentroids = gridGeometryTable.identifiersToAltitudes(centroids);
            Set<Date> keySet = availableCentroids.keySet();
            if (!(keySet instanceof SortedSet)) {
                keySet = new TreeSet<Date>(keySet);
                // This is a hack for Java 5 (Java 6 returns directly an instance of SortedSet).
                // TODO: remove this hack when we will be allowed to target Java 6, and invoke
                //       TreeSet.navigableKeySet() instead.
            }
            availableTimes = Collections.unmodifiableSortedSet((SortedSet<Date>) keySet);
            availableCentroids = Collections.unmodifiableSortedMap(availableCentroids);
        }
        return availableCentroids;
    }

    /**
     * Returns the range of date of available images.
     *
     * @param  addTo If non-null, the set where to add the time range of available coverages.
     * @return The time range of available coverages. This method returns {@code addTo} if it
     *         was non-null or a new object otherwise.
     * @throws SQLException If an error occured while reading the database.
     */
    public synchronized RangeSet getAvailableTimeRanges(RangeSet addTo) throws SQLException {
        final GridCoverageQuery query = (GridCoverageQuery) super.query;
        long  lastEndTime        = Long.MIN_VALUE;
        final Calendar calendar  = getCalendar();
        final ResultSet  result  = getStatement(AVAILABLE_DATA).executeQuery();
        final int startTimeIndex = indexOf(query.startTime);
        final int   endTimeIndex = indexOf(query.endTime);
        if (addTo == null) {
            addTo = new RangeSet(Date.class);
        }
        while (result.next()) {
            final long timeInterval = Math.round(layer.getTimeInterval() * DAY);
            final Date    startTime = result.getTimestamp(startTimeIndex, calendar);
            final Date      endTime = result.getTimestamp(  endTimeIndex, calendar);
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
                addTo.add(startTime, endTime);
            }
        }
        result.close();
        return addTo;
    }

    /**
     * Configure la requête spécifiée. Cette méthode est appelée automatiquement lorsque la table
     * a {@linkplain #fireStateChanged changé d'état}.
     */
    @Override
    protected void configure(final QueryType type, final PreparedStatement statement) throws SQLException {
        super.configure(type, statement);
        final GridCoverageQuery query = (GridCoverageQuery) super.query;
        int index = query.byLayer.indexOf(type);
        if (index != 0) {
            final String name = (layer != null) ? layer.getName() : null;
            statement.setString(index, name);
            // TODO: need to take in account the case where the layer is null.
        }
        index = query.byVisibility.indexOf(type);
        if (index != 0) {
            statement.setBoolean(index, true);
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
        final GridCoverageQuery query = (GridCoverageQuery) super.query;
        final String layer     = result.getString   (indexOf(query.layer));
        final String series    = result.getString   (indexOf(query.series));
        final String pathname  = result.getString   (indexOf(query.pathname));
        final String filename  = result.getString   (indexOf(query.filename));
        final String extension = result.getString   (indexOf(query.extension));
        final Date   startTime = result.getTimestamp(indexOf(query.startTime), calendar);
        final Date   endTime   = result.getTimestamp(indexOf(query.endTime),   calendar);
        final short  timeIndex = result.getShort    (indexOf(query.index));
        final String extent    = result.getString   (indexOf(query.spatialExtent));
        final String format    = result.getString   (indexOf(query.format));
        if (gridGeometryTable == null) {
            gridGeometryTable = getDatabase().getTable(GridGeometryTable.class);
        }
        final GridGeometryEntry geometry = gridGeometryTable.getEntry(extent);
        final NumberRange  verticalRange = getVerticalRange();
        final short band = geometry.indexOf(0.5*(verticalRange.getMinimum() + verticalRange.getMaximum()));
        return new GridCoverageEntry(this, layer, series, pathname, filename, extension, startTime,
                    endTime, timeIndex, geometry, band, format, null).canonicalize();
    }

    /**
     * Retourne les paramètres de cette table. Pour des raisons d'économie de mémoire (de très
     * nombreux objets {@code GridCoverageSettings} pouvant être créés), cette méthode retourne un exemplaire
     * unique autant que possible. L'objet retourné ne doit donc pas être modifié!
     * <p>
     * Cette méthode est appelée par le constructeur de {@link GridCoverageEntry}.
     *
     * @param seriesID    Nom ID de la couche, pour fin de vérification. Ce nom doit correspondre
     *                     à celui de la couche examinée par cette table.
     * @param formatID    Nom ID du format des images.
     * @param coverageCRS Système de référence des coordonnées.
     * @param pathname    Chemin relatif des images.
     * @param extension   Extension (sans le point) des noms de fichier des images à lire.
     * @return Un objet incluant les paramètres demandées ainsi que ceux de la table.
     * @throws CatalogException si les paramètres n'ont pas pu être obtenus.
     * @throws SQLException si une erreur est survenue lors de l'accès à la base de données.
     * @todo L'implémentation actuelle n'accepte pas d'autres implémentations de Format que FormatEntry.
     */
    final synchronized GridCoverageSettings getParameters(final String seriesID,
                                                final String formatID,
                                                final CoordinateReferenceSystem coverageCRS,
                                                final String pathname,
                                                final String extension)
            throws CatalogException, SQLException
    {
        final String seriesName = layer.getName();
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
            Utilities.equals(parameters.format.getName(), formatID) &&
            Utilities.equals(parameters.coverageCRS,   coverageCRS) &&
            Utilities.equals(parameters.pathname,         pathname) &&
            Utilities.equals(parameters.extension,       extension))
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
            formatTable = getDatabase().getTable(FormatTable.class);
        }
        parameters = new GridCoverageSettings(layer,
                                    (FormatEntry) formatTable.getEntry(formatID),
                                    pathname.intern(),
                                    extension.intern(),
                                    operation,
                                    getCoordinateReferenceSystem(),
                                    coverageCRS,
                                    geographicArea,
                                    resolution,
                                    dateFormat,
                                    getProperty(ConfigurationKey.ROOT_DIRECTORY),
                                    getProperty(ConfigurationKey.ROOT_URL),
                                    getProperty(ConfigurationKey.URL_ENCODING));
        return parameters;
    }

    /**
     * Prépare l'évaluation d'un point.
     */
    @SuppressWarnings("fallthrough")
    private void prepare(final DirectPosition location)
            throws CatalogException, SQLException, IOException
    {
        assert Thread.holdsLock(this);
        if (coverage3D == null) {
            coverage3D = new CoverageStack(getLayer().getName(), getCoordinateReferenceSystem(), getEntries());
            position   = new TransformedDirectPosition(null, getCoordinateReferenceSystem(), null);
        }
        try {
            position.transform(location);
        } catch (TransformException e) {
            throw new CatalogException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public synchronized double evaluate(final double x, final double y, final double t, final short band)
            throws CatalogException, SQLException, IOException
    {
        //prepare(x, y, t); TODO
        samples = coverage3D.evaluate(position, samples);
        return samples[band];
    }

    /**
     * {@inheritDoc}
     */
    public synchronized double[] snap(final double x, final double y, final double t)
            throws CatalogException, SQLException, IOException
    {
        //prepare(x, y, t); TODO
        coverage3D.snap(position);
        return position.ordinates.clone();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public synchronized List<Coverage> coveragesAt(final DirectPosition position)
            throws CatalogException, SQLException, IOException
    {
        prepare(position);
        return coverage3D.coveragesAt(position.getOrdinate(position.getDimension() - 1));
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
        availableElevations = null;
        availableTimes     = null;
        availableCentroids = null;
    }

    /**
     * Enregistre un évènement dans le journal.
     */
    private void log(final String method, final Level level, final int clé, final Object param) {
        final Resources resources = Resources.getResources(getDatabase().getLocale());
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
                                                      ANGLE_PATTERN, getDatabase().getLocale());
        } catch (CatalogException e) {
            area = e.getLocalizedMessage();
        }
        final StringBuilder buffer = new StringBuilder(Utilities.getShortClassName(this));
        buffer.append("[\"");
        buffer.append(String.valueOf(layer));
        buffer.append("\": ");
        buffer.append(area);
        buffer.append(']');
        return buffer.toString();
    }
}
