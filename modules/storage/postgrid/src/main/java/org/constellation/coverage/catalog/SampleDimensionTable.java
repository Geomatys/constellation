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

import java.util.List;
import java.util.ArrayList;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.text.ParseException;
import javax.measure.unit.Unit;
import javax.measure.unit.UnitFormat;

import org.geotoolkit.coverage.Category;
import org.geotoolkit.coverage.GridSampleDimension;
import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.constellation.catalog.CatalogException;
import org.constellation.catalog.IllegalRecordException;
import org.constellation.catalog.Table;
import org.constellation.catalog.Database;
import org.constellation.catalog.QueryType;
import org.constellation.resources.i18n.Resources;
import org.constellation.resources.i18n.ResourceKeys;
import org.geotoolkit.resources.Errors;


/**
 * Connection to a table of {@linkplain GridSampleDimension sample dimensions}. This table creates
 * instances of {@link GridSampleDimension} for a given format. Sample dimensions are one of the
 * components needed for creation of {@link GridCoverage2D}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class SampleDimensionTable extends Table {
    /**
     * Connexion vers la table des {@linkplain Category catégories}.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    private CategoryTable categories;

    /**
     * Creates a sample dimension table.
     *
     * @param database Connection to the database.
     */
    public SampleDimensionTable(final Database database) {
        super(new SampleDimensionQuery(database));
    }

    /**
     * Returns the sample dimensions for the given format.
     *
     * @param  format The format name.
     * @return The sample dimensions for the given format.
     * @throws CatalogException if an inconsistent record is found in the database.
     * @throws SQLException if an error occured while reading the database.
     */
    public synchronized GridSampleDimension[] getSampleDimensions(final String format)
            throws CatalogException, SQLException
    {
        final SampleDimensionQuery query = (SampleDimensionQuery) super.query;
        final PreparedStatement statement = getStatement(QueryType.FILTERED_LIST);
        statement.setString(indexOf(query.byFormat), format);
        final int idIndex   = indexOf(query.identifier);
        final int bandIndex = indexOf(query.band);
        final int unitIndex = indexOf(query.units);
        int lastBand = 0;
        final List<GridSampleDimension> sampleDimensions = new ArrayList<GridSampleDimension>();
        final ResultSet results = statement.executeQuery();
        while (results.next()) {
            final String identifier = results.getString(idIndex);
            final int          band = results.getInt   (bandIndex); // Comptées à partir de 1.
            String unitSymbol = results.getString(unitIndex);
            Unit<?> unit = null;
            if (unitSymbol != null) {
                unitSymbol = unitSymbol.trim();
                if (unitSymbol.length() == 0) {
                    unit = Unit.ONE;
                } else try {
                    unit = (Unit) (UnitFormat.getInstance().parseObject(unitSymbol));
                } catch (ParseException e) {
                    throw new CatalogException(Errors.format(Errors.Keys.UNPARSABLE_STRING_$2,
                            "unit(" + unitSymbol + ')',
                            unitSymbol.substring(Math.max(0, e.getErrorOffset()))), e);
                }
            }
            if (categories == null) {
                categories = getDatabase().getTable(CategoryTable.class);
            }
            final Category[] categoryArray = categories.getCategories(identifier);
            final GridSampleDimension sampleDimension;
            try {
                sampleDimension = new GridSampleDimension(identifier, categoryArray, unit);
            } catch (IllegalArgumentException exception) {
                throw new IllegalRecordException(exception, this, results, idIndex, format);
            }
            if (band-1 != lastBand) {
                throw new IllegalRecordException(Resources.format(ResourceKeys.ERROR_NON_CONSECUTIVE_BANDS_$2,
                        Integer.valueOf(lastBand), Integer.valueOf(band)), this, results, bandIndex, format);
            }
            lastBand = band;
            sampleDimensions.add(sampleDimension);
        }
        results.close();
        return sampleDimensions.toArray(new GridSampleDimension[sampleDimensions.size()]);
    }
}
