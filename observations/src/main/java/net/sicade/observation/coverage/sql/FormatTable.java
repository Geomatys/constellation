/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sicade.observation.coverage.sql;

// J2SE dependencies
import java.sql.ResultSet;
import java.sql.SQLException;

// Seagis dependencies
import net.sicade.observation.coverage.Format;
import net.sicade.observation.ConfigurationKey;
import net.sicade.observation.CatalogException;
import net.sicade.observation.sql.SingletonTable;
import net.sicade.observation.sql.QueryType;
import net.sicade.observation.sql.Shareable;
import net.sicade.observation.sql.Database;
import net.sicade.observation.sql.UsedBy;
import net.sicade.observation.sql.Use;


/**
 * Connexion vers une table des {@linkplain Format formats d'images}. Cette table construit
 * des objets {@link Format} pour un nom spécifié. Ces formats sont utilisés pour le décodage
 * d'objets {@link org.geotools.coverage.grid.GridCoverage2D}.
 * <p>
 * Cette table est utilisée par {@link GridCoverageTable}, qui construit des objets
 * de plus haut niveau.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
@Use(SampleDimensionTable.class)
@UsedBy({SubSeriesTable.class, GridCoverageTable.class})
public class FormatTable extends SingletonTable<Format> implements Shareable {
    /**
     * Requête SQL utilisée pour obtenir le type MIME du format
     * (par exemple "image/png") dans la table des formats.
     */
    private static final ConfigurationKey SELECT = new ConfigurationKey("Formats:SELECT",
            "SELECT name,"         +   // [01] NAME
                  " mime,"         +   // [02] MIME
                  " extension,"    +   // [03] EXTENSION
                  " geophysics\n"  +   // [04] GEOPHYSICS
            "  FROM \"Formats\"\n" +
            " WHERE name=?");

    /** Numéro de colonne. */ private static final int NAME       = 1;
    /** Numéro de colonne. */ private static final int MIME       = 2;
    /** Numéro de colonne. */ private static final int EXTENSION  = 3;
    /** Numéro de colonne. */ private static final int GEOPHYSICS = 4;

    /**
     * Connexion vers la table des bandes.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    private SampleDimensionTable bands;

    /**
     * Construit une table en utilisant la connexion spécifiée.
     *
     * @param  database Connexion vers la base de données d'observations.
     */
    public FormatTable(final Database database) {
        super(database);
    }

    /**
     * Retourne la requête SQL à utiliser pour obtenir les formats.
     */
    @Override
    protected String getQuery(final QueryType type) throws SQLException {
        switch (type) {
            case SELECT: return getProperty(SELECT);
            default:     return super.getQuery(type);
        }
    }

    /**
     * Construit un format pour l'enregistrement courant.
     */
    protected Format createEntry(final ResultSet results) throws CatalogException, SQLException {
        final String       name  = results.getString (NAME);
        final String   mimeType  = results.getString (MIME);
        final String   extension = results.getString (EXTENSION);
        final boolean geophysics = results.getBoolean(GEOPHYSICS);
        if (bands == null) {
            bands = database.getTable(SampleDimensionTable.class);
        }
        return new FormatEntry(name, mimeType, extension, geophysics,
                               bands.getSampleDimensions(name));
    }
}
