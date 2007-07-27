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

// J2SE dependencies and extensions
import java.util.List;
import java.util.ArrayList;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import javax.units.Unit;

// Geotools dependencies
import org.geotools.coverage.Category;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import net.sicade.coverage.catalog.CatalogException;
import net.sicade.coverage.catalog.IllegalRecordException;
import net.sicade.sql.Table;
import net.sicade.sql.Database;
import net.sicade.sql.QueryType;
import net.sicade.resources.i18n.Resources;
import net.sicade.resources.i18n.ResourceKeys;


/**
 * Connection to a table of {@linkplain GridSampleDimension sample dimensions}. This table creates
 * instances of {@link GridSampleDimension} for a given format. Sample dimensions are one of the
 * components needed for creation of {@link GridCoverage2D}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class SampleDimensionTable extends Table {
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
        final ResultSet result = statement.executeQuery();
        while (result.next()) {
            final String identifier = result.getString(idIndex);
            final int          band = result.getInt   (bandIndex); // Comptées à partir de 1.
            final String unitSymbol = result.getString(unitIndex);
            final Unit         unit = (unitSymbol != null) ? Unit.searchSymbol(unitSymbol) : null;
            if (categories == null) {
                categories = getDatabase().getTable(CategoryTable.class);
            }
            final Category[] categoryArray = categories.getCategories(identifier);
            final GridSampleDimension sampleDimension;
            try {
                sampleDimension = new GridSampleDimension(identifier, categoryArray, unit);
            } catch (IllegalArgumentException exception) {
                throw new IllegalRecordException(result.getMetaData().getTableName(idIndex), exception);
            }
            if (band-1 != lastBand) {
                throw new IllegalRecordException(result.getMetaData().getTableName(bandIndex),
                                Resources.format(ResourceKeys.ERROR_NON_CONSECUTIVE_BANDS_$2,
                                                 new Integer(lastBand), new Integer(band)));
            }
            lastBand = band;
            sampleDimensions.add(sampleDimension);
        }
        result.close();
        return sampleDimensions.toArray(new GridSampleDimension[sampleDimensions.size()]);
    }
}
