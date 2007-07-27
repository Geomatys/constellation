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
package net.sicade.coverage.catalog.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.geotools.resources.CharUtilities;

import net.sicade.observation.Distribution;
import net.sicade.coverage.catalog.CatalogException;
import net.sicade.coverage.catalog.NoSuchRecordException;
import net.sicade.coverage.catalog.Descriptor;
import net.sicade.coverage.catalog.Operation;
import net.sicade.coverage.catalog.Layer;
import net.sicade.coverage.catalog.RegionOfInterest;
import net.sicade.sql.SingletonTable;
import net.sicade.sql.Database;


/**
 * Connection to a table of {@linkplain Descriptor descriptors}. The informations required by the
 * descriptors are splitted in three tables: {@link LayerTable}, {@link RegionOfInterestTable} and
 * {@link OperationTable}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
public class DescriptorTable extends SingletonTable<Descriptor> {
    /**
     * La table des couches. Elle sera construite la première fois où elle sera nécessaire.
     */
    private LayerTable layer;

    /**
     * La table des opérations. Ne sera construite que la première fois où elle sera nécessaire.
     */
    private OperationTable operations;

    /**
     * La table des positions relatives.
     * Ne sera construite que la première fois où elle sera nécessaire.
     */
    private RegionOfInterestTable offsets;

    /**
     * La table des distributions.
     * Ne sera construite que la première fois où elle sera nécessaire.
     */
    private DistributionTable distributions;

    /**
     * Creates a format table.
     * 
     * @param database Connection to the database.
     */
    public DescriptorTable(final Database database) {
        super(new DescriptorQuery(database));
        final DescriptorQuery query = (DescriptorQuery) this.query;
        setIdentifierParameters(query.bySymbol, query.byIdentifier);
    }

    /**
     * Creates a new table using the same connection than the specified table.
     * This is useful when we want to change the configuration of the new table
     * while preserving the original table from changes.
     *
     * @param table The table to clone.
     *
     * @see #setLayerTable
     */
    protected DescriptorTable(final DescriptorTable table) {
        super(table);
    }

    /**
     * Sets the layer table to use. This method is invoked by {@link LayerTable} immediately after
     * the creation of this {@code DescriptorTable}. Note that the instance given to this method
     * should not be cached by {@link Database#getTable}.
     * <p>
     * Implementation note: in theory, {@code LayerTable} are not {@linkplain Shareable shareable}.
     * However this restriction doesn't prevent {@code DescriptorTable} implements {@link Shareable}
     * because it doesn't modify the {@code LayerTable} configuration.
     *
     * @param  layer The layer table to use.
     * @throws IllegalStateException if this table is already associated to an other layer table.
     */
    protected synchronized void setLayerTable(final LayerTable layer) throws IllegalStateException {
        if (this.layer != layer) {
            if (this.layer != null) {
                throw new IllegalStateException();
            }
            this.layer = layer;
        }
    }

    /**
     * Returns a new entry for the given name. If no entry is found for the given name and if
     * the name contains only digit characters, then this method tries to parse the name as an
     * {@linkplain Integer integer} and invokes {@link #getEntry(int)}. Otherwise, if the name
     * ends with some digits, those digits are converted to some unicode characters (e.g. digits
     * as indices) and the new name is tried again.
     *
     * @param  name The name of the element to fetch.
     * @return The element for the given name, or {@code null} if {@code name} was null.
     * @throws CatalogException if no element has been found for the specified name,
     *         or if an element contains invalid data.
     * @throws SQLException if an error occured will reading from the database.
     */
    public Descriptor getEntryLenient(final String name) throws CatalogException, SQLException {
        try {
            return getEntry(name);
        } catch (final NoSuchRecordException exception) {
            /*
             * Aucune entrée n'a été trouvée pour le nom. Essaie comme identifiant numérique.
             * Si l'identifiant est purement numérique mais la recherche échoue pour ce dernier
             * aussi, on ne fera pas d'autres tentatives.
             */
            int identifier = 0;
            try {
                identifier = Integer.parseInt(name);
            } catch (NumberFormatException dummy) {
                /*
                 * L'identifiant n'est pas numérique. Essaie de remplacer les derniers chiffres
                 * par les caractères unicodes correspondant à ces même chiffres en indices.
                 */
                final StringBuilder builder = new StringBuilder(name);
                for (int i=builder.length(); --i>=0;) {
                    final char c = builder.charAt(i);
                    final char n = CharUtilities.toSubScript(c);
                    if (c == n) {
                        break;
                    }
                    builder.setCharAt(i, n);
                }
                String modified = builder.toString();
                if (!modified.equals(name)) try {
                    return getEntry(modified);
                } catch (NoSuchRecordException ignore) {
                    throw exception;
                }
            }
            return getEntry(identifier);
        }
    }

    /**
     * Creates a descriptor from the current row in the specified result set.
     *
     * @param  results The result set to read.
     * @return The entry for current row in the specified result set.
     * @throws CatalogException if an inconsistent record is found in the database.
     * @throws SQLException if an error occured while reading the database.
     */
    protected Descriptor createEntry(final ResultSet results) throws CatalogException, SQLException {
        final DescriptorQuery query = (DescriptorQuery) super.query;
        final String    symbol       = results.getString (indexOf(query.symbol      ));
        final int       identifier   = results.getInt    (indexOf(query.identifier  ));
        final String    phenomenon   = results.getString (indexOf(query.layer       ));
        final String    procedure    = results.getString (indexOf(query.operation   ));
        final String    position     = results.getString (indexOf(query.region      ));
        final short     band = (short)(results.getShort  (indexOf(query.band        )) - 1);
        final String    distribution = results.getString (indexOf(query.distribution));
        if (offsets == null) {
            offsets = getDatabase().getTable(RegionOfInterestTable.class);
        }
        final RegionOfInterest offset = offsets.getEntry(position);
        if (layer == null) {
            setLayerTable(getDatabase().getTable(LayerTable.class));
        }
        final Layer layer = this.layer.getEntry(phenomenon);
        if (operations == null) {
            operations = getDatabase().getTable(OperationTable.class);
        }
        final Operation operation = operations.getEntry(procedure);
        if (distributions == null) {
            distributions = getDatabase().getTable(DistributionTable.class);
        }
        final Distribution distributionEntry = distributions.getEntry(distribution);
        return new DescriptorEntry(identifier, symbol, layer, operation, band, offset, distributionEntry, null);
    }
}
