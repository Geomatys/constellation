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
import net.sicade.observation.CatalogException;
import net.sicade.observation.IllegalRecordException;
import net.sicade.observation.sql.Column;
import net.sicade.observation.sql.Use;
import net.sicade.observation.sql.UsedBy;
import net.sicade.observation.sql.Table;
import net.sicade.observation.sql.Database;
import net.sicade.observation.sql.Parameter;
import net.sicade.observation.sql.QueryType;
import net.sicade.observation.sql.Shareable;
import net.sicade.resources.i18n.Resources;
import net.sicade.resources.i18n.ResourceKeys;
import static net.sicade.observation.sql.QueryType.*;


/**
 * Connexion vers une table des {@linkplain GridSampleDimension bandes}. Cette table construit des
 * objets {@link GridSampleDimension} pour un format d'image individuel. Les bandes sont une des
 * composantes d'un objet {@link org.geotools.coverage.grid.GridCoverage2D}, mais ne correspondent
 * pas directement à un {@linkplain net.sicade.observation.Element élément} du paquet des
 * observations.
 * <p>
 * Cette table est utilisée par {@link FormatTable}, qui construit des objets de
 * plus haut niveau.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
@Use(CategoryTable.class)
@UsedBy(FormatTable.class)
public class SampleDimensionTable extends Table implements Shareable {
    /**
     * Column name declared in the {@linkplain #query query}.
     */
    private final Column identifier, format, band, units;

    /**
     * Parameter declared in the {@linkplain #query query}.
     */
    private final Parameter byFormat;

    /**
     * Connexion vers la table des {@linkplain Category catégories}.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    private CategoryTable categories;

    /**
     * Construit une table en utilisant la connexion spécifiée.
     *
     * @param database  Connexion vers la base de données d'observations.
     */
    public SampleDimensionTable(final Database database) {
        super(database);
        final QueryType[] usage = {LIST, FILTERED_LIST};
        identifier = new Column   (query, "SampleDimensions", "identifier", usage);
        format     = new Column   (query, "SampleDimensions", "format",     LIST );
        band       = new Column   (query, "SampleDimensions", "band",       usage);
        units      = new Column   (query, "SampleDimensions", "units",      usage);
        byFormat   = new Parameter(query, format, FILTERED_LIST);
        band.setOrdering("ASC");
    }

    /**
     * Retourne les bandes qui se rapportent au format spécifié.
     *
     * @param  format Nom du format pour lequel on veut les bandes.
     * @return Les listes des bandes du format demandé.
     * @throws IllegalRecordException si une incohérence a été trouvée dans les enregistrements.
     * @throws SQLException si l'interrogation de la table a échoué.
     */
    public synchronized GridSampleDimension[] getSampleDimensions(final String format)
            throws CatalogException, SQLException
    {
        final PreparedStatement statement = getStatement(FILTERED_LIST);
        statement.setString(indexOf(byFormat), format);
        final int idIndex   = indexOf(identifier);
        final int bandIndex = indexOf(band);
        final int unitIndex = indexOf(units);
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
