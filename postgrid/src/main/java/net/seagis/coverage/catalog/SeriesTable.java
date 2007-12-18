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

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.geotools.resources.Utilities;
import net.seagis.catalog.CatalogException;
import net.seagis.catalog.ConfigurationKey;
import net.seagis.catalog.Database;
import net.seagis.catalog.QueryType;
import net.seagis.catalog.SingletonTable;
import net.seagis.resources.i18n.ResourceKeys;
import net.seagis.resources.i18n.Resources;


/**
 * Connection to a table of series. This connection is used internally by the
 * {@linkplain LayerTable layer table}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class SeriesTable extends SingletonTable<Series> {
    /**
     * Connection to the format table. This connection will be etablished
     * when first needed and may be shared by many series tables.
     */
    private FormatTable formats;

    /**
     * The layer for which we want the series, {@code null} for fetching all series.
     */
    private Layer layer;

    /**
     * Creates a series table.
     *
     * @param database Connection to the database.
     */
    public SeriesTable(final Database database) {
        this(new SeriesQuery(database));
    }

    /**
     * Creates a series table using the specified query.
     */
    private SeriesTable(final SeriesQuery query) {
        super(query);
        setIdentifierParameters(query.byName, null);
    }

    /**
     * Creates a series table connected to the same database than the specified one.
     */
    public SeriesTable(final SeriesTable table) {
        super(table);
    }

    /**
     * Returns the layer for the series to be returned by {@link #getEntries() getEntries()}.
     * The default value is {@code null}, which means that no filtering should be performed.
     */
    public Layer getLayer() {
        return layer;
    }

    /**
     * Sets the layer for the series to be returned. Next call to {@link #getEntries() getEntries()}
     * will filters the series in order to returns only the one in this layer. A {@code null} value
     * will remove the filtering, so all series will be returned no matter their layer.
     */
    public synchronized void setLayer(final Layer layer) {
        if (!Utilities.equals(layer, this.layer)) {
            this.layer = layer;
            fireStateChanged("layer");
        }
    }

    /**
     * Returns the series available in the database. If {@link #getLayer} has been invoked with
     * a non-null value, then only the series for that layer are returned.
     *
     * @return The set of series. May be empty, but never {@code null}.
     * @throws CatalogException if a series contains invalid data.
     * @throws SQLException if an error occured will reading from the database.
     */
    @Override
    public synchronized Set<Series> getEntries() throws CatalogException, SQLException {
        return getEntries(layer==null ? QueryType.LIST : QueryType.FILTERED_LIST);
    }

    /**
     * Invoked automatically by for a newly created statement or when this table changed its state.
     * The default implementation setup the SQL parameter for the {@linkplain #getLayer currently
     * selected layer}.
     */
    @Override
    protected void configure(final QueryType type, final PreparedStatement statement)
            throws CatalogException, SQLException
    {
        super.configure(type, statement);
        final SeriesQuery query = (SeriesQuery) super.query;
        final int index = query.byLayer.indexOf(type);
        if (index != 0) {
            statement.setString(1, layer!=null ? layer.getName() : null);
        }
    }

    /**
     * Creates a series from the current row in the specified result set.
     */
    @Override
    protected Series createEntry(final ResultSet results) throws CatalogException, SQLException {
        final SeriesQuery query     = (SeriesQuery) super.query;
        final String  name          = results.getString (indexOf(query.name));
        final String  pathname      = results.getString (indexOf(query.pathname));
        final String  extension     = results.getString (indexOf(query.extension));
        final boolean visible       = results.getBoolean(indexOf(query.visible));
        final String  quicklook     = results.getString (indexOf(query.quicklook));
        final String  rootDirectory = getProperty(ConfigurationKey.ROOT_DIRECTORY);
        final String  rootURL       = getProperty(ConfigurationKey.ROOT_URL);
        if (formats == null) {
            formats = getDatabase().getTable(FormatTable.class);
        }
        final Format format = formats.getEntry(results.getString(indexOf(query.format)));
        return new SeriesEntry(name, layer, rootDirectory != null ? rootDirectory : rootURL,
                               pathname, extension, format, visible, null);
    }

    /**
     * Returns the identifier for a series having the specified properties. If no
     * matching record is found, then a new one is created and added to the database.
     *
     * @param  layer     The layer name.
     * @param  path      The path relative to the root directory, or the base URL.
     * @param  extension The extension to add to filenames.
     * @param  format    The format for the series considered.
     * @return The identifier of a matching entry (never {@code null}).
     * @throws CatalogException if a logical error occured.
     * @throws SQLException if an error occured while reading from or writing to the database.
     */
    final synchronized String getIdentifier(final String layer,
            final String path, final String extension, final String format)
            throws SQLException, CatalogException
    {
        final SeriesQuery query = (SeriesQuery) super.query;
        PreparedStatement statement = getStatement(QueryType.FILTERED_LIST);
        statement.setString(indexOf(query.byLayer), layer);

        String ID = null;
        final int idIndex = indexOf(query.name);
        final int pnIndex = indexOf(query.pathname);
        final int exIndex = indexOf(query.extension);
        final int ftIndex = indexOf(query.format);
        final ResultSet results = statement.executeQuery();
        while (results.next()) {
            final String nextID = results.getString(idIndex);
            String candidate = results.getString(pnIndex);
            if (candidate == null || !candidate.equals(path)) {
                continue;
            }
            candidate = results.getString(exIndex);
            if (candidate == null || !candidate.equals(extension)) {
                continue;
            }
            candidate = results.getString(ftIndex);
            if (candidate == null || !candidate.equals(format)) {
                continue;
            }
            if (ID != null && !ID.equals(nextID)) {
                // Could happen if there is insuffisient conditions in the WHERE clause.
                final LogRecord record = Resources.getResources(getDatabase().getLocale()).
                        getLogRecord(Level.WARNING, ResourceKeys.ERROR_DUPLICATED_RECORD_$1, nextID);
                record.setSourceClassName("SeriesTable");
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
        LayerTable layers = getDatabase().getTable(LayerTable.class);
        final boolean layerExists = layers.exists(layer);
        transactionBegin();
        try {
            final String layerName = layerExists ? layer : layers.getIdentifier(layer);
            ID = searchFreeIdentifier(layer);
            statement = getStatement(QueryType.INSERT);
            statement.setString (indexOf(query.name),      ID);
            statement.setString (indexOf(query.layer),     layerName);
            statement.setString (indexOf(query.pathname),  path);
            statement.setString (indexOf(query.extension), extension);
            statement.setString (indexOf(query.format),    format);
            success = updateSingleton(statement);
            // 'success' must be assigned last in this try block.
        } finally {
            transactionEnd(success);
        }
        return ID;
    }
}
