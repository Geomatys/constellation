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
package net.seagis.coverage.catalog;

import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.opengis.coverage.Coverage;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;
import org.geotools.coverage.CoverageStack;
import org.geotools.util.NumberRange;
import org.geotools.util.RangeSet;
import org.geotools.resources.Classes;
import org.geotools.resources.Utilities;
import org.geotools.resources.geometry.XRectangle2D;
import org.geotools.image.io.mosaic.TileManager;

import net.seagis.catalog.CatalogException;
import net.seagis.coverage.model.Operation;
import net.seagis.catalog.BoundedSingletonTable;
import net.seagis.catalog.Database;
import net.seagis.catalog.QueryType;
import net.seagis.resources.i18n.Resources;
import net.seagis.resources.i18n.ResourceKeys;
import static net.seagis.catalog.QueryType.*;


/**
 * Connection to a table of grid coverages. This table builds references in the form of
 * {@link CoverageReference} objects, which will defer the image loading until first needed.
 * A {@code GridCoverageTable} can produce a list of available image intercepting a given
 * {@linkplain #setGeographicArea geographic area} and {@linkplain #setTimeRange time range}.
 * <p>
 * <strong>Note:</strong> for proper working of this class, the SQL query must sort entries by
 * end time. If this condition is changed, then {@link GridCoverageEntry#equalsAsSQL} must be
 * updated accordingly.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Sam Hiatt
 */
public class GridCoverageTable extends BoundedSingletonTable<CoverageReference> {
    /**
     * Pattern to use for formatting angle.
     */
    static final String ANGLE_PATTERN = "D°MM.m'";

    /**
     * Amount of milliseconds in a day.
     */
    static final long MILLIS_IN_DAY = 24*60*60*1000L;

    /**
     * The currently selected layer, or {@code null} if not yet set.
     */
    private Layer layer;

    /**
     * Operation to apply on the image to be read, or {@code null} if none.
     */
    private Operation operation;

    /**
     * Dimension index for (<var>x</var>,<var>y</var>) in a coordinate.
     *
     * @todo Codés en dur pour l'instant. Peut avoir besoin d'être paramètrables dans une
     *       version future.
     */
    private static final int xDimension=0, yDimension=1;

    /**
     * Object to use for formatting dates. The symbols are local-dependent, but the time zone
     * should be GMT or the timezone applicable to the region of interest. This is used for
     * loggings only, so it is not a big deal if not accurate.
     */
    private final DateFormat dateFormat;

    /**
     * Shared instance of a table of grid geometries. Will be created only when first needed.
     */
    private transient GridGeometryTable gridGeometryTable;

    /**
     * Shared instance of a table of tiles. Will be created only when first needed.
     */
    private transient TileTable tileTable;

    /**
     * Comparator for selecting the "best" image when more than one is available in
     * the spatio-temporal area of interest. Will be created only when first needed.
     */
    private transient CoverageComparator comparator;

    /**
     * Last settings used for creating {@link CoverageReference} instances.
     */
    private transient GridCoverageSettings settings;

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
     * A 2, 3 or 4-dimensional view over this table.
     * Will be created only when first needed.
     */
    private transient Coverage asCoverage;

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
    GridCoverageTable(final GridCoverageQuery query) {
        super(query, net.seagis.catalog.CRS.XYT);
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
        dateFormat        = table.dateFormat;
        gridGeometryTable = table.gridGeometryTable;
        tileTable         = table.tileTable;
        comparator        = table.comparator;
        settings          = table.settings;
        asCoverage        = table.asCoverage;
    }

    /**
     * Returns the series for the current layer. The default implementation expects a layer
     * with only one series. The {@link WritableGridCoverageTable} will override this method
     * with a more appropriate value.
     */
    synchronized Series getSeries() throws CatalogException {
        final Iterator<Series> iterator = getNonNullLayer().getSeries().iterator();
        if (iterator.hasNext()) {
            final Series series = iterator.next();
            if (!iterator.hasNext()) {
                return series;
            }
        }
        throw new CatalogException(Resources.format(ResourceKeys.ERROR_NO_SERIES_SELECTION));
    }

    /**
     * Returns the layer for the coverages in this table, or {@code null} if not yet set.
     */
    public Layer getLayer() {
        return layer;
    }

    /**
     * Returns the layer for the coverages in this table.
     *
     * @throws CatalogException if the layer is not set.
     */
    final Layer getNonNullLayer() throws CatalogException {
        assert Thread.holdsLock(this);
        if (layer == null) {
            throw new CatalogException("Aucune couche n'a été spécifiée."); // TODO: localize
        }
        return layer;
    }

    /**
     * Returns the name of the current layer, or {@code null} if none.
     */
    private String getLayerName() {
        return (layer != null) ? layer.getName() : null;
    }

    /**
     * Sets the layer for the coverages in this table.
     */
    public synchronized void setLayer(final Layer layer) {
        if (!layer.equals(this.layer)) {
            flushExceptEntries();
            this.layer = layer;
            fireStateChanged("Layer");
            log("setLayer", Level.CONFIG, ResourceKeys.SET_LAYER_$1, layer.getName());
        }
    }

    /**
     * Sets the layer as a string.
     *
     * @param  name The layer name.
     * @throws CatalogException if no layer was found for the given name,
     *         or if an other logical error occured.
     * @throws SQLException if the database access failed for an other reason.
     */
    public synchronized void setLayer(final String name) throws CatalogException, SQLException {
        // We don't keep a reference to the layer table since this method
        // should only be a commodity and should not be invoked often.
        setLayer(getDatabase().getTable(LayerTable.class).getEntry(name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean setTimeRange(final Date startTime, final Date endTime) {
        final boolean change = super.setTimeRange(startTime, endTime);
        if (change) {
            flushExceptEntries();
            final String startText, endText;
            synchronized (dateFormat) {
                startText = dateFormat.format(startTime);
                endText   = dateFormat.format(  endTime);
            }
            log("setTimeRange", Level.CONFIG, ResourceKeys.SET_TIME_RANGE_$3,
                                new String[]{startText, endText, getLayerName()});
        }
        return change;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean setGeographicBoundingBox(final GeographicBoundingBox area) {
        final boolean change = super.setGeographicBoundingBox(area);
        if (change) {
            flush();
            log("setGeographicArea", Level.CONFIG, ResourceKeys.SET_GEOGRAPHIC_AREA_$2, new String[] {
                GeographicBoundingBoxImpl.toString(area, ANGLE_PATTERN, getDatabase().getLocale()),
                getLayerName()
            });
        }
        return change;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean setPreferredResolution(final Dimension2D resolution) {
        final boolean change = super.setPreferredResolution(resolution);
        if (change) {
            flush();
            final int clé;
            final Object param;
            if (resolution != null) {
                clé = ResourceKeys.SET_RESOLUTION_$3;
                param = new Object[] {
                    resolution.getWidth(),
                    resolution.getHeight(),
                    getLayerName()
                };
            } else {
                clé = ResourceKeys.UNSET_RESOLUTION_$1;
                param = getLayerName();
            }
            log("setPreferredResolution", Level.CONFIG, clé, param);
        }
        return change;
    }

    /**
     * Returns the operation to apply on rasters. It may be for example a gradient magnitude.
     * If no operation are applied, then this method returns {@code null}.
     */
    public Operation getOperation() {
        return operation;
    }

    /**
     * Sets the operation to apply on rasters, or {@code null} if none.
     */
    public synchronized void setOperation(final Operation operation) {
        if (!Utilities.equals(operation, this.operation)) {
            flush();
            this.operation = operation;
            final int clé;
            final Object param;
            if (operation != null) {
                param = new String[] {operation.getName(), getLayerName()};
                clé   = ResourceKeys.SET_OPERATION_$2;
            } else {
                param = getLayerName();
                clé   = ResourceKeys.UNSET_OPERATION_$1;
            }
            fireStateChanged("Operation");
            log("setOperation", Level.CONFIG, clé, param);
        }
    }

    /**
     * Returns the two-dimensional coverages that intercept the
     * {@linkplain #getEnvelope current spatio-temporal envelope}.
     *
     * @return List of coverages in the current envelope of interest.
     * @throws CatalogException if a record is invalid.
     * @throws SQLException if an error occured while reading the database.
     */
    @Override
    public Set<CoverageReference> getEntries() throws CatalogException, SQLException {
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
                        if (!oldEntry.equalsAsSQL(newEntry)) {
                            // Entries not equal according the "ORDER BY" clause.
                            break;
                        }
                        final GridCoverageEntry lowestResolution = oldEntry.getLowestResolution(newEntry);
                        if (lowestResolution != null) {
                            // Two entries has the same spatio-temporal coordinates.
                            if (lowestResolution.hasEnoughResolution()) {
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
     * Returns one of the two-dimensional coverages that intercept the
     * {@linkplain #getEnvelope current spatio-temporal envelope}. If more than one coverage
     * intercept the envelope (i.e. if {@link #getEntries} returns a set containing at least
     * two elements), then a coverage will be selected using the default
     * {@link CoverageComparator}.
     *
     * @return A coverage intercepting the given envelope, or {@code null} if none.
     * @throws CatalogException if a record is invalid.
     * @throws SQLException if an error occured while reading the database.
     */
    public synchronized CoverageReference getEntry() throws CatalogException, SQLException {
        final Iterator<CoverageReference> entries = getEntries().iterator();
        CoverageReference best = null;
        if (entries.hasNext()) {
            best = entries.next();
            if (entries.hasNext()) {
                if (comparator == null) {
                    comparator = new CoverageComparator(getCoordinateReferenceSystem(), getEnvelope());
                }
                do {
                    final CoverageReference entry = entries.next();
                    if (comparator.compare(entry, best) <= -1) {
                        best = entry;
                    }
                } while (entries.hasNext());
            }
        }
        return best;
    }

    /**
     * Returns the set of dates when a coverage is available. Only the images in
     * the currently {@linkplain #getEnvelope selected envelope} are considered.
     *
     * @return The set of dates.
     * @throws CatalogException if an illegal record was found.
     * @throws SQLException if an error occured while reading the database.
     */
    public synchronized SortedSet<Date> getAvailableTimes()
            throws CatalogException, SQLException
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
     * @throws CatalogException if an illegal record was found.
     * @throws SQLException if an error occured while reading the database.
     */
    public synchronized SortedSet<Number> getAvailableElevations()
            throws CatalogException, SQLException
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
     * @return An immutable collection of centroids. Keys are the dates, and values are the set
     *         of altitudes for a given date.
     * @throws CatalogException if an illegal record was found.
     * @throws SQLException if an error occured while reading the database.
     */
    final synchronized SortedMap<Date, SortedSet<Number>> getAvailableCentroids()
            throws CatalogException, SQLException
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
     * Returns the range of date for available images.
     *
     * @param  addTo If non-null, the set where to add the time range of available coverages.
     * @return The time range of available coverages. This method returns {@code addTo} if it
     *         was non-null or a new object otherwise.
     * @throws CatalogException if an illegal record was found.
     * @throws SQLException if an error occured while reading the database.
     */
    public synchronized RangeSet getAvailableTimeRanges(RangeSet addTo)
            throws CatalogException, SQLException
    {
        final GridCoverageQuery query = (GridCoverageQuery) super.query;
        long  lastEndTime        = Long.MIN_VALUE;
        final Calendar calendar  = getCalendar();
        final ResultSet  result  = getStatement(AVAILABLE_DATA).executeQuery();
        final int startTimeIndex = indexOf(query.startTime);
        final int   endTimeIndex = indexOf(query.endTime);
        final long timeInterval  = Math.round((layer!=null ? layer.getTimeInterval() : 1) * MILLIS_IN_DAY);
        if (addTo == null) {
            addTo = new RangeSet(Date.class);
        }
        while (result.next()) {
            final Date startTime = result.getTimestamp(startTimeIndex, calendar);
            final Date   endTime = result.getTimestamp(  endTimeIndex, calendar);
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
     * Configures the specified query. This method is invoked automatically after this table
     * {@linkplain #fireStateChanged changed its state}.
     *
     * @throws CatalogException if the statement can not be configured.
     * @throws SQLException if a SQL error occured while configuring the statement.
     */
    @Override
    protected void configure(final QueryType type, final PreparedStatement statement)
            throws CatalogException, SQLException
    {
        super.configure(type, statement);
        final GridCoverageQuery query = (GridCoverageQuery) super.query;
        int index = query.byLayer.indexOf(type);
        if (index != 0) {
            final String name = getLayerName();
            if (name != null) {
                statement.setString(index, name);
            } else {
                statement.setNull(index, Types.VARCHAR);
            }
        }
        index = query.bySeries.indexOf(type);
        if (index != 0) {
            final Series series = getSeries();
            assert getNonNullLayer().getSeries().contains(series) : series;
            statement.setString(index, series.getName());
        }
        index = query.byVisibility.indexOf(type);
        if (index != 0) {
            statement.setBoolean(index, true);
        }
    }

    /**
     * Creates an entry from the current row in the specified result set.
     *
     * @throws CatalogException if an illegal record was found.
     * @throws SQLException if an error occured while reading the database.
     */
    protected CoverageReference createEntry(final ResultSet result) throws CatalogException, SQLException {
        assert Thread.holdsLock(this);
        final Calendar calendar = getCalendar();
        final GridCoverageQuery query = (GridCoverageQuery) super.query;
        final String    seriesID  = result.getString   (indexOf(query.series));
        final String    filename  = result.getString   (indexOf(query.filename));
        final Timestamp startTime = result.getTimestamp(indexOf(query.startTime), calendar);
        final Timestamp endTime   = result.getTimestamp(indexOf(query.endTime),   calendar);
        final short     index     = result.getShort    (indexOf(query.index)); // We expect 0 if null.
        final String    extent    = result.getString   (indexOf(query.spatialExtent));
        /*
         * Gets the SeriesEntry in which this coverage is declared. The entry should be available
         * from the layer HashMap. If not, we will query the SeriesTable as a fallback, but there
         * is probably a bug (so it is not worth to keep a reference to the series table).
         */
        final Layer layer = getNonNullLayer();
        Series series = layer.getSeries(seriesID);
        if (series == null) {
            LOGGER.warning(Resources.format(ResourceKeys.ERROR_WRONG_LAYER_$1, getLayerName()));
            series = getDatabase().getTable(SeriesTable.class).getEntry(seriesID);
        }
        /*
         * Process to the entry creation.
         */
        if (gridGeometryTable == null) {
            gridGeometryTable = getDatabase().getTable(GridGeometryTable.class);
        }
        final GridGeometryEntry geometry = gridGeometryTable.getEntry(extent);
        final NumberRange  verticalRange = getVerticalRange();
        final short band = geometry.indexOf(0.5*(verticalRange.getMinimum() + verticalRange.getMaximum()));
        final GridCoverageEntry entry = new GridCoverageEntry(this,
                series, filename, index, startTime, endTime, geometry, band, null);
        final GridCoverageEntry cached = entry.unique();
        if (cached == entry) {
            if (tileTable == null) {
                tileTable = getDatabase().getTable(TileTable.class);
            }
            final TileManager[] managers;
            try {
                managers = tileTable.getTiles(layer, startTime, endTime, geometry.horizontalSRID);
            } catch (IOException e) {
                throw new CatalogException(e);
            }
            if (managers != null && managers.length != 0) {
                cached.setTiles(managers);
            }
        }
        return cached;
    }

    /**
     * Returns the current values of some settings in this table. Those settings are grouped in
     * a single object in order to reduce memory usage, because a large amount of coverages may
     * share a single instance of {@code GridCoverageSettings}. The returned object should be
     * considered immutable - <strong>do not modify</strong>.
     * <p>
     * This method is invoked by {@link GridCoverageEntry} constructor.
     *
     * @param  coverageCRS CRS of the coverages to be created.
     * @return An object containing the given CRS and parameter values defined in this table.
     * @throws CatalogException if an illegal record was found.
     * @throws SQLException if an error occured while reading the database.
     *
     * @todo L'implémentation actuelle n'accepte pas d'autres implémentations de Format que FormatEntry.
     */
    final synchronized GridCoverageSettings getSettings(final CoordinateReferenceSystem coverageCRS)
            throws CatalogException, SQLException
    {
        /*
         * Si les paramètres spécifiés sont identiques à ceux qui avaient été
         * spécifiés la dernière fois, retourne le dernier bloc de paramètres.
         */
        if (settings != null && Utilities.equals(settings.coverageCRS, coverageCRS)) {
            return settings;
        }
        /*
         * Construit un nouveau bloc de paramètres et projète les
         * coordonnées vers le système de coordonnées de l'image.
         */
        final Envelope envelope = getEnvelope();
        final Rectangle2D geographicArea = XRectangle2D.createFromExtremums(
                            envelope.getMinimum(xDimension), envelope.getMinimum(yDimension),
                            envelope.getMaximum(xDimension), envelope.getMaximum(yDimension));
        final Dimension2D resolution = getPreferredResolution();
        settings = new GridCoverageSettings(operation, getCoordinateReferenceSystem(),
                            coverageCRS, geographicArea, resolution, dateFormat);
        return settings;
    }

    /**
     * Returns a <var>n</var>-dimensional coverage backed by this table.
     *
     * @return A 2, 3 or 4 dimensional coverage, or {@code null} if there is no data.
     * @throws CatalogException if an illegal record was found.
     * @throws SQLException if an error occured while reading the database.
     * @throws IOException If an error occured while reading an image file.
     *
     * @todo Current implementation does not take depth in account, so there is actually
     *       no 4-D coverage yet.
     */
    public synchronized Coverage asCoverage() throws CatalogException, SQLException, IOException {
        if (asCoverage == null) {
            final Set<CoverageReference> entries = getEntries();
            switch (entries.size()) {
                case 0: {
                    // No data - coverage will stay null.
                    break;
                }
                case 1: {
                    asCoverage = entries.iterator().next().getCoverage(null);
                    break;
                }
                default: {
                    final CoordinateReferenceSystem crs = getCoordinateReferenceSystem();
                    asCoverage = new CoverageStack(getLayer().getName(), crs, entries);
                    break;
                }
            }
        }
        return asCoverage;
    }

    /**
     * Vide la cache de toutes les références vers les entrées précédemment créées.
     */
    @Override
    public synchronized void flush() {
        flushExceptEntries();
        super.flush();
    }

    /**
     * Réinitialise les caches, mais en gardant les références vers les entrées déjà créées.
     * Cette méthode devrait être appellée à la place de {@link #flush} lorsque l'état
     * de la table a changé, mais que cet état n'affecte pas les prochaines entrées à créer.
     */
    private void flushExceptEntries() {
        asCoverage          = null;
        settings            = null;
        comparator          = null;
        availableElevations = null;
        availableTimes      = null;
        availableCentroids  = null;
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
        final String area = GeographicBoundingBoxImpl.toString(getGeographicBoundingBox(),
                    ANGLE_PATTERN, getDatabase().getLocale());
        final StringBuilder buffer = new StringBuilder(Classes.getShortClassName(this));
        buffer.append("[\"").append(String.valueOf(layer)).append("\": ").append(area).append(']');
        return buffer.toString();
    }
}
