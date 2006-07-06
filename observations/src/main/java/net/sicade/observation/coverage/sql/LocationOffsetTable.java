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

// Sicade dependencies
import net.sicade.observation.ConfigurationKey;
import net.sicade.observation.sql.Use;
import net.sicade.observation.sql.UsedBy;
import net.sicade.observation.sql.Database;
import net.sicade.observation.sql.QueryType;
import net.sicade.observation.sql.Shareable;
import net.sicade.observation.sql.SingletonTable;
import net.sicade.observation.coverage.LocationOffset;


/**
 * Connexion vers la table des {@linkplain LocationOffset décalage spatio-temporels} relatifs aux
 * positions des {@linkplain net.sicade.observation.Observation observations}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
@UsedBy(DescriptorTable.class)
public class LocationOffsetTable extends SingletonTable<LocationOffset> implements Shareable {
    /**
     * La requête SQL à utiliser pour obtnir une position relative.
     */
    private static final ConfigurationKey SELECT = new ConfigurationKey("LocationOffsets:SELECT",
            "SELECT name, dx, dy, dz, dt\n" +
            "  FROM \"LocationOffsets\"\n"  +
            " WHERE name=? ORDER BY dt DESC, dz DESC, dy DESC, dx DESC");

    /** Numéro de colonne. */ private static final int NAME = 1;
    /** Numéro de colonne. */ private static final int DX   = 2;
    /** Numéro de colonne. */ private static final int DY   = 3;
    /** Numéro de colonne. */ private static final int DZ   = 4;
    /** Numéro de colonne. */ private static final int DT   = 5;

    /**
     * Construit une table en utilisant la connexion spécifiée.
     *
     * @param  database Connexion vers la base de données d'observations.
     */
    public LocationOffsetTable(final Database database) {
        super(database);
    }

    /**
     * Retourne la requête SQL à utiliser pour obtenir les décalages spatio-temporels.
     */
    @Override
    protected String getQuery(final QueryType type) throws SQLException {
        switch (type) {
            case SELECT: return getProperty(SELECT);
            default:     return super.getQuery(type);
        }
    }

    /**
     * Construit un décalage spatio-temporel pour l'enregistrement courant.
     */
    protected LocationOffset createEntry(final ResultSet results) throws SQLException {
        return new LocationOffsetEntry(results.getString(NAME),
                                       results.getDouble(DX),
                                       results.getDouble(DY),
                                       results.getDouble(DZ),
                            Math.round(results.getDouble(DT)*LocationOffsetEntry.DAY));
    }
}
