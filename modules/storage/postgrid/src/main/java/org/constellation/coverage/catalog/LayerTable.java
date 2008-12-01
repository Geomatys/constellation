/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.coverage.catalog;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.geotools.util.logging.Logging;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.constellation.catalog.Database;
import org.constellation.ws.Service;
import org.constellation.catalog.BoundedSingletonTable;
import org.constellation.catalog.NoSuchRecordException;
import org.constellation.catalog.CatalogException;
import org.constellation.catalog.QueryType;
import org.constellation.coverage.model.DescriptorTable;
import org.constellation.coverage.model.LinearModelTable;
import org.constellation.resources.i18n.ResourceKeys;
import org.constellation.resources.i18n.Resources;


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
     * Connection to the table of domains. Will be created when first needed.
     * This table will be shared by all {@code LayerTable} and should not be
     * flushed by this class, since it is independent of {@link LayerTable} settings.
     */
    private DomainOfLayerTable domains;

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
     * The service to be requested.
     */
    private Service service = Service.WCS;

    /**
     * Creates a layer table.
     *
     * @param database Connection to the database.
     */
    public LayerTable(final Database database) {
        this(new LayerQuery(database));
    }

    /**
     * Constructs a new {@code LayerTable} from the specified query.
     */
    private LayerTable(final LayerQuery query) {
        super(query);
        setIdentifierParameters(query.byName, null);
    }

    /**
     * Creates a layer table using the same initial configuration than the specified table.
     */
    public LayerTable(final LayerTable table) {
        super(table);
    }

    /**
     * Sets the service for the layers to be returned.
     */
    public synchronized void setService(final Service service) {
        ensureNonNull("service", service);
        if (!service.equals(this.service)) {
            this.service = service;
            flush();
            fireStateChanged("service");
        }
    }

    /**
     * Returns a CRS for the specified code from the {@code "spatial_ref_sys"} table.
     * This is mostly a convenience method leveraging the current connection for querying
     * the table. This method does <strong>not</strong> look in other CRS databases like what
     * {@link org.geotools.referencing.CRS#decode(String)} does.
     *
     * @param  code The CRS identifier.
     * @return The coordinate reference system for the given code.
     * @throws SQLException if an error occured while querying the database.
     * @throws FactoryException if the CRS was not found or can not be created.
     */
    public CoordinateReferenceSystem getSpatialReferenceSystem(final String code)
            throws SQLException, CatalogException, FactoryException
    {
        return getDatabase().getTable(GridGeometryTable.class).getSpatialReferenceSystem(code);
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
     * Completes the creation of the specified element. This method adds the following piece
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
        series.setService(service);
        entry.setSeries(series.getEntries());
        if (entry.fallback instanceof String) {
            entry.fallback = getEntry((String) entry.fallback);
        }
        final Database database = getDatabase();
        final GridCoverageTable data = new GridCoverageTable(database.getTable(GridCoverageTable.class));
        data.setLayer(layer);
        boolean changed;
        changed  = data.setTimeRange(getTimeRange());
        changed |= data.setVerticalRange(getVerticalRange());
        changed |= data.setGeographicBoundingBox(getGeographicBoundingBox());
        changed |= data.setPreferredResolution(getPreferredResolution());
        DomainOfLayerEntry domain = null;
        if (!changed) {
            // Settings had no effect, so we are better to use the global domain for efficienty.
            // This case occurs more often than it may look like since we often want to get the
            // bounding box of a Layer obtained from the global (unmodifiable) LayerTable.
            if (domains == null) {
                domains = getDatabase().getTable(DomainOfLayerTable.class);
            }
            try {
                domain = domains.getEntry(layer.getName());
            } catch (NoSuchRecordException exception) {
                Logging.recoverableException(Layer.LOGGER, LayerTable.class, "postCreateEntry", exception);
            }
        }
        entry.setGridCoverageTable(data, domain);
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
        if (property.equalsIgnoreCase("GeographicBoundingBox") ||
            property.equalsIgnoreCase("PreferredResolution")   ||
            property.equalsIgnoreCase("TimeRange"))
        {
            flush();
        }
        super.fireStateChanged(property);
    }

    /**
     * Clears this table cache.
     */
    @Override
    public synchronized void flush() {
        // Do not flush DomainOfLayerTable.
        if (series != null) {
            series.flush();
        }
        if (models != null) {
            models.flush();
        }
        super.flush();
    }
}
