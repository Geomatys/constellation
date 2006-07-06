/*
 * Sicade - Syst�mes int�gr�s de connaissances pour l'aide � la d�cision en environnement
 * (C) 2005, Institut de Recherche pour le D�veloppement
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
 * des objets {@link Format} pour un nom sp�cifi�. Ces formats sont utilis�s pour le d�codage
 * d'objets {@link org.geotools.coverage.grid.GridCoverage2D}.
 * <p>
 * Cette table est utilis�e par {@link GridCoverageTable}, qui construit des objets
 * de plus haut niveau.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
@Use(SampleDimensionTable.class)
@UsedBy({SubSeriesTable.class, GridCoverageTable.class})
public class FormatTable extends SingletonTable<Format> implements Shareable {
    /**
     * Requ�te SQL utilis�e pour obtenir le type MIME du format
     * (par exemple "image/png") dans la table des formats.
     */
    private static final ConfigurationKey SELECT = new ConfigurationKey("Formats:SELECT",
            "SELECT name,"         +   // [01] NAME
                  " mime,"         +   // [02] MIME
                  " extension,"    +   // [03] EXTENSION
                  " geophysics\n"  +   // [04] GEOPHYSICS
            "  FROM \"Formats\"\n" +
            " WHERE name=?");

    /** Num�ro de colonne. */ private static final int NAME       = 1;
    /** Num�ro de colonne. */ private static final int MIME       = 2;
    /** Num�ro de colonne. */ private static final int EXTENSION  = 3;
    /** Num�ro de colonne. */ private static final int GEOPHYSICS = 4;

    /**
     * Connexion vers la table des bandes.
     * Une connexion (potentiellement partag�e) sera �tablie la premi�re fois o� elle sera n�cessaire.
     */
    private SampleDimensionTable bands;

    /**
     * Construit une table en utilisant la connexion sp�cifi�e.
     *
     * @param  database Connexion vers la base de donn�es d'observations.
     */
    public FormatTable(final Database database) {
        super(database);
    }

    /**
     * Retourne la requ�te SQL � utiliser pour obtenir les formats.
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
