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

import java.sql.ResultSet;
import java.sql.SQLException;

import net.seagis.catalog.CRS;
import net.seagis.catalog.Database;
import net.seagis.catalog.BoundedSingletonTable;
import net.seagis.catalog.CatalogException;
import net.seagis.coverage.model.DescriptorTable;
import net.seagis.coverage.model.LinearModelTable;


/**
 * Connection to a table of {@linkplain Layer layers}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @todo L'implémentation actuelle ne transmet pas l'enveloppe spatio-temporelle de {@code this}
 *       vers les objets {@link GridCoverageTable} créés. Il faudrait le faire, en prennant soin
 *       de transmettre cette informations aux objets RMI aussi. Dans la version actuelle, ce n'est
 *       pris en compte que pour les connections locales.
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
        double       period    = results.getDouble(indexOf(query.period   ));
        if (results.wasNull()) {
            period = Double.NaN;
        }
        final String fallback  = results.getString(indexOf(query.fallback));
        final String remarks   = results.getString(indexOf(query.remarks ));
        final LayerEntry entry = new LayerEntry(name, thematic, period, remarks);
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
        final GridCoverageTable data;
        data = new GridCoverageTable(getDatabase().getTable(GridCoverageTable.class));
        data.setLayer(layer);
        data.setEnvelope(getEnvelope());
        data.trimEnvelope();
        entry.setDataConnection(data);
        if (models == null) {
            final Database database = getDatabase();
            DescriptorTable descriptors = database.getTable(DescriptorTable.class);
            descriptors = new DescriptorTable(descriptors); // Protect the shared instance from changes.
            descriptors.setLayerTable(this);
            models = new LinearModelTable(database.getTable(LinearModelTable.class));
            models.setDescriptorTable(descriptors);
        }
        entry.model = models.getEntry(entry);
    }
}
