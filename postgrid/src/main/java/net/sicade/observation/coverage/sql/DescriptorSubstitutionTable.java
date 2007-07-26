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

import net.sicade.observation.sql.Table;
import net.sicade.observation.sql.Database;
import net.sicade.observation.sql.QueryType;
import net.sicade.observation.CatalogException;
import net.sicade.observation.IllegalRecordException;
import net.sicade.observation.coverage.LinearModel;
import net.sicade.observation.coverage.Descriptor;
import net.sicade.resources.i18n.ResourceKeys;
import net.sicade.resources.i18n.Resources;


/**
 * Connection to a table capable to substitute a {@linkplain Descriptor descriptor} by a sum of
 * {@linkplain LinearModel.Term linear terms}. Such substitution may be applied for example in
 * order to compute a temporal gradient on the fly. The {@link #expand} method replaces a single
 * descriptor by a linear combinaison of other descriptors.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class DescriptorSubstitutionTable extends Table {
    /**
     * The descriptor table. Will be created only when first needed.
     */
    private DescriptorTable descriptors;

    /**
     * Creates a descriptor substitution table.
     * 
     * @param database Connection to the database.
     */
    public DescriptorSubstitutionTable(final Database database) {
        super(new DescriptorSubstitutionQuery(database));
    }

    /**
     * Creates a new table using the same connection than the specified table.
     * This is useful when we want to change the configuration of the new table
     * while preserving the original table from changes.
     *
     * @param table The table to clone.
     *
     * @see #setDescriptorTable
     */
    protected DescriptorSubstitutionTable(final DescriptorSubstitutionTable table) {
        super(table);
    }

    /**
     * Sets the descriptor table to use. This method is invoked by {@link LinearModelTable}
     * immediately after the creation of this {@code DescriptorSubstitutionTable}. Note that
     * the instance given to this method should not be cached by {@link Database#getTable}.
     *
     * @param  descriptors The descriptor table to use.
     * @throws IllegalStateException if this table is already associated to an other descriptor table.
     */
    protected synchronized void setDescriptorTable(final DescriptorTable descriptors)
            throws IllegalStateException
    {
        if (this.descriptors != descriptors) {
            if (this.descriptors != null) {
                throw new IllegalStateException();
            }
            this.descriptors = descriptors;
        }
    }

    /**
     * Returns the linear model terms for the specified descriptor, or {@code null} if none.
     * If this method returns a non-null value, then the descriptor given in argument can be
     * computed by a linear model described by the returned terms.
     *
     * @param  descriptor The descriptor to replace.
     * @return The linear model terms, or {@code null} if none.
     * @throws CatalogException if an inconsistent record is found in the database.
     * @throws SQLException if an error occured while reading the database.
     */
    public synchronized LinearModel.Term[] expand(final Descriptor descriptor)
            throws CatalogException, SQLException
    {
        final DescriptorSubstitutionQuery query = (DescriptorSubstitutionQuery) super.query;
        final PreparedStatement statement = getStatement(QueryType.SELECT);
        final String key = descriptor.getName();
        statement.setString(indexOf(query.bySymbol), key);
        final ResultSet results = statement.executeQuery();
        if (!results.next()) {
            results.close();
            return null;
        }
        final String symbol1 = results.getString(indexOf(query.symbol1));
        final String symbol2 = results.getString(indexOf(query.symbol2));
        if (results.next()) {
            final String table = results.getMetaData().getTableName(1);
            results.close();
            throw new IllegalRecordException(table, Resources.format(
                      ResourceKeys.ERROR_DUPLICATED_RECORD_$1, key));
        }
        results.close();
        if (key.equals(symbol1) || key.equals(symbol2)) {
            throw new IllegalRecordException(null, "Définition récursive d'un gradient temporel.");
        }
        if (descriptors == null) {
            descriptors = getDatabase().getTable(DescriptorTable.class);
        }
        final Descriptor d1 = descriptors.getEntry(symbol1);
        final Descriptor d2 = descriptors.getEntry(symbol2);
        final double scale = 1.0 / (d2.getRegionOfInterest().getDayOffset() -
                                    d1.getRegionOfInterest().getDayOffset());
        return new LinearModelTerm[] {
            new LinearModelTerm( scale, d2),
            new LinearModelTerm(-scale, d1)
        };
    }
}
