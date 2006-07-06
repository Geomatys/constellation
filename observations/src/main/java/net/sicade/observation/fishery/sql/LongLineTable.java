/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2006, Institut de Recherche pour le Développement
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
package net.sicade.observation.fishery.sql;

// J2SE dependencies
import java.sql.ResultSet;
import java.sql.SQLException;
import java.awt.geom.Point2D;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;
import org.opengis.metadata.quality.DataQuality;

// Sicade dependencies
import net.sicade.util.DateRange;
import net.sicade.observation.Station;
import net.sicade.observation.Platform;
import net.sicade.observation.ConfigurationKey;
import net.sicade.observation.sql.Database;
import net.sicade.observation.sql.QueryType;
import net.sicade.observation.sql.StationTable;


/**
 * Table des {@linkplain LongLine ligne de palangre}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class LongLineTable extends StationTable {
    /**
     * Requête SQL pour obtenir une station à partir de son identifiant.
     */
    private static final ConfigurationKey SELECT = new ConfigurationKey("LongLines:SELECT",
            "SELECT identifier AS name, identifier, platform, quality, provider, \"startTime\", \"endTime\", x, y\n" +
            "  FROM \"LongLinesLocations\"\n" +
            " WHERE name LIKE ?\n"            +
            " ORDER BY identifier");

    /**
     * Construit une nouvelle connexion vers la table des lignes de palangres.
     */
    public LongLineTable(final Database database) {
        super(database);
    }

    /**
     * Retourne la requête à utiliser pour obtenir les lignes de palangres.
     */
    @Override
    protected String getQuery(final QueryType type) throws SQLException {
        switch (type) {
            case SELECT: {
                return getProperty(SELECT);
            }
            default: {
                return super.getQuery(type);
            }
        }
    }

    /**
     * Construit une station à partir des informations spécifiées.
     *
     * @todo A mettre à jour lorsque l'on aura implémenté une classe {@code LongLineEntry}.
     */
    @Override
    protected Station createEntry(final int          identifier,
                                  final String       name,
                                  final Point2D      coordinate,
                                  final DateRange    timeRange,
                                  final Platform     platform,
                                  final DataQuality  quality,
                                  final Citation     provider,
                                  final ResultSet    result)
            throws SQLException
    {
        return super.createEntry(identifier, name, coordinate, timeRange, platform, quality, provider, result);
    }
}
