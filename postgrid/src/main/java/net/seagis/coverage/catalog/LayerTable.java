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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import net.seagis.catalog.CRS;
import net.seagis.catalog.Database;
import net.seagis.catalog.BoundedSingletonTable;
import net.seagis.catalog.CatalogException;
import net.seagis.catalog.QueryType;
import net.seagis.coverage.model.DescriptorTable;
import net.seagis.coverage.model.LinearModelTable;
import net.seagis.resources.i18n.ResourceKeys;
import net.seagis.resources.i18n.Resources;


/**
 * Connection to a table of {@linkplain Layer layers}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @todo Current version do yet take in bounding box in account for layer filtered list.
 */
public class LayerTable extends BoundedSingletonTable<Layer> {
    /**
     * Connection to the table of linear models. Will be created when first needed.
     */
    private LinearModelTable models;

    /**
     * Connection to the table of series. A new instance will be
     * created when first needed for each layer table.
     * <p>
     * <b>Note:</b> a previous version was used to share this instance, but we had
     * to remove this sharing because it produces wrong values for Series.getLayer().
     */
    private SeriesTable series;

    /**
     * Creates a layer table.
     *
     * @param database Connection to the database.
     */
    public LayerTable(final Database database) {
        super(new LayerQuery(database), CRS.XYT);
        setIdentifierParameters(((LayerQuery) query).byName, null);
    }

    /**
     * Creates a layer table using the same initial configuration than the specified table.
     */
    public LayerTable(final LayerTable table) {
        super(table);
    }

    /**
     * Creates a layer from the current row in the specified result set.
     *
     * @param  results The result set to read.
     * @return The entry for current row in the specified result set.
     * @throws CatalogException if an inconsistent record is found in the database.
     * @throws SQLException if an error occured while reading the database.
     */
    protected Layer createEntry(final ResultSet results) throws CatalogException, SQLException {
        final LayerQuery query = (LayerQuery) super.query;
        final String name      = results.getString(indexOf(query.name     ));
        final String thematic  = results.getString(indexOf(query.thematic ));
        final String procedure = results.getString(indexOf(query.procedure));
        double       period    = results.getDouble(indexOf(query.period   ));
        if (results.wasNull()) {
            period = Double.NaN;
        }
        final String fallback  = results.getString(indexOf(query.fallback));
        final String remarks   = results.getString(indexOf(query.remarks ));
        final LayerEntry entry = new LayerEntry(name, thematic, procedure, period, remarks);
        entry.fallback = fallback; // Will be replaced by a Layer in postCreateEntry.
        return entry;
    }

    /**
     * Completes the creation of the specified element. This method add the following piece
     * to the given layer:
     * <p>
     * <ul>
     *   <li>The {@linkplain Layer#getFallback fallback layer}, if any.</li>
     *   <li>The {@linkplain Layer#getSeries series}.</li>
     *   <li>The {@linkplain LayerEntry#setDataConnection data connection}.</li>
     *   <li>The {@linkplain LayerEntry#getModel linear model}, if any.</li>
     * </ul>
     */
    @Override
    protected void postCreateEntry(final Layer layer) throws CatalogException, SQLException {
        super.postCreateEntry(layer);
        final LayerEntry entry = (LayerEntry) layer;
        if (series == null) {
            series = new SeriesTable(getDatabase().getTable(SeriesTable.class));
        }
        series.setLayer(entry);
        entry.setSeries(series.getEntries());
        if (entry.fallback instanceof String) {
            entry.fallback = getEntry((String) entry.fallback);
        }
        final Database database = getDatabase();
        final GridCoverageTable data = new GridCoverageTable(database.getTable(GridCoverageTable.class));
        data.setLayer(layer);
        data.setTimeRange(getTimeRange());
        data.setEnvelope(getEnvelope());
        data.setPreferredResolution(getPreferredResolution());
        if (false) {
            data.trimEnvelope(); // TODO revisit
        }
        entry.setGridCoverageTable(data);
        if (models == null) {
            DescriptorTable descriptors = database.getTable(DescriptorTable.class);
            descriptors = new DescriptorTable(descriptors); // Protect the shared instance from changes.
            descriptors.setLayerTable(this);
            models = new LinearModelTable(database.getTable(LinearModelTable.class));
            models.setDescriptorTable(descriptors);
        }
        entry.model = models.getEntry(entry);
    }

    /**
     * Returns the identifier for the specified layer. If no matching record is found and
     * {@code allowCreate} is {@code true}, then a new one is created and added to the database.
     *
     * @param  name The name of the layer.
     * @return The identifier of a matching entry, or {@code null} if none if none was
     *         found and {@code newIdentifier} is {@code null}.
     * @throws SQLException if an error occured while reading or writing the database.
     */
    final synchronized String getIdentifier(final String name) throws SQLException, CatalogException {
        final LayerQuery query = (LayerQuery) super.query;
        PreparedStatement statement = getStatement(QueryType.SELECT);
        statement.setString(indexOf(query.byName), name);
        String ID = null;
        final int idIndex = indexOf(query.name);
        final ResultSet results = statement.executeQuery();
        while (results.next()) {
            final String nextID = results.getString(idIndex);
            if (ID != null && !ID.equals(nextID)) {
                // Could happen if there is insuffisient conditions in the WHERE clause.
                final LogRecord record = Resources.getResources(getDatabase().getLocale()).
                        getLogRecord(Level.WARNING, ResourceKeys.ERROR_DUPLICATED_RECORD_$1, nextID);
                record.setSourceClassName("LayerTable");
                record.setSourceMethodName("getIdentifier");
                LOGGER.log(record);
                continue;
            }
            ID = nextID;
        }
        results.close();
        if (ID != null) {
            return ID;
        }
        /*
         * No match found. Adds a new record in the database.
         */
        boolean success = false;
        transactionBegin();
        try {
            ID = searchFreeIdentifier(name);
            statement = getStatement(QueryType.INSERT);
            statement.setString(indexOf(query.name), ID);
            success = updateSingleton(statement);
            // 'success' must be assigned last in this try block.
        } finally {
            transactionEnd(success);
        }
        return ID;
    }

    /**
     * Invoked when the state of this field changed. This method {@linkplain #flush clears
     * the cache} if the changed property is the geographic bounding box or the time range.
     */
    @Override
    protected void fireStateChanged(final String property) {
        super.fireStateChanged(property);
        if (property.equalsIgnoreCase("GeographicBoundingBox") || property.equalsIgnoreCase("TimeRange")) {
            flush();
        }
    }

    /**
     * Clears this table cache.
     */
    @Override
    public synchronized void flush() {
        if (series != null) {
            series.flush();
        }
        if (models != null) {
            models.flush();
        }
        super.flush();
    }
}
