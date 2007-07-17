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
package net.sicade.observation.coverage.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.Set;

import org.geotools.resources.Utilities;
import net.sicade.observation.CatalogException;
import net.sicade.observation.coverage.Format;
import net.sicade.observation.coverage.Layer;
import net.sicade.observation.coverage.Series;
import net.sicade.observation.sql.Use;
import net.sicade.observation.sql.UsedBy;
import net.sicade.observation.sql.Database;
import net.sicade.observation.sql.QueryType;
import net.sicade.observation.sql.SingletonTable;


/**
 * Connection to a table of series. This connection is used internally by the
 * {@linkplain LayerTable layer table}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
@Use(FormatTable.class)
@UsedBy(LayerTable.class)
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
        super(new SeriesQuery(database));
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
    public synchronized Set<Series> getEntries() throws CatalogException, SQLException {
        return getEntries(layer==null ? QueryType.LIST : QueryType.FILTERED_LIST);
    }

    /**
     * Invoked automatically by for a newly created statement or when this table changed its state.
     * The default implementation setup the SQL parameter for the {@linkplain #getLayer currently
     * selected layer}.
     */
    @Override
    protected void configure(final QueryType type, final PreparedStatement statement) throws SQLException {
        super.configure(type, statement);
        final SeriesQuery query = (SeriesQuery) super.query;
        final int index = query.byOwner.indexOf(type);
        if (index != 0) {
            statement.setString(1, layer!=null ? layer.getName() : null);
        }
    }

    /**
     * Creates a series entry for the current row in the specified result set.
     */
    protected Series createEntry(final ResultSet results) throws CatalogException, SQLException {
        final SeriesQuery query = (SeriesQuery) super.query;
        final String name = results.getString(indexOf(query.name));
        final String remarks = null;
        if (formats == null) {
            formats = getDatabase().getTable(FormatTable.class);
        }
        final Format format = formats.getEntry(results.getString(indexOf(query.format)));
        return new SeriesEntry(name, format, remarks);
    }

    /**
     * A shareable instance of {@link SeriesTable}. <strong>Do not use</strong>. This is for
     * {@link LayerTable} internal working only. This class had to be public because it needs
     * to be accessible to {@link Database#getTable}.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     *
     * @todo Replace by a {@code getEntries(Layer)} method.
     */
    @Deprecated
    public static final class Shareable extends SeriesTable implements net.sicade.observation.sql.Shareable {
        /**
         * Creates a series table.
         * 
         * @param database Connection to the database.
         */
        public Shareable(final Database database) {
            super(database);
        }

        /**
         * VERY BAD TRICK HERE. We need to do something better.
         */
        @Deprecated
        protected void fireStateChanged(final String property) {
            try {
                super.fireStateChanged(property);
            } catch (IllegalStateException e) {
                // Ignore the fact that we are violating the Shareable contract.
            }
        }
    }
}
