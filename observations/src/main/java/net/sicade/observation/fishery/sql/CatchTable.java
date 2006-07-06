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
package net.sicade.observation.fishery.sql;


// Sicade dependencies
import net.sicade.observation.ConfigurationKey;
import net.sicade.observation.fishery.Catch;
import net.sicade.observation.sql.Database;
import net.sicade.observation.sql.MeasurementTable;


/**
 * Connexion vers la table des {@linkplain Catch captures}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @todo Retourner une instance de {@link Catch}.
 */
public class CatchTable extends MeasurementTable {
    /**
     * Requête SQL pour obtenir les captures pour une station et un observable donnés.
     */
    private static final ConfigurationKey SELECT = new ConfigurationKey("Catchs:SELECT",
            "SELECT station, observable, value, error\n"  +
            "  FROM \"Catchs\"\n"                         +
            " WHERE (station LIKE ?) AND (observable LIKE ?)");

    /**
     * Construit une nouvelle connexion vers la table des captures.
     */
    public CatchTable(final Database database) {
        super(database, SELECT, null);
    }
}
