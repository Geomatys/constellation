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
import net.sicade.observation.sql.Database;
import net.sicade.observation.sql.StationTable;
import net.sicade.observation.sql.MeasurementTable;


/**
 * Table des paramètres environnementaux aux positions de pêches.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class EnvironmentTable extends MeasurementTable {
    /**
     * Requête SQL pour obtenir les mesures pour une station et un observable donnés.
     *
     * @todo L'utilisation d'instruction {@code LIKE} ralentit considérablement l'exécution
     *       de la requête. Il faudrait se débarasser de ce bricolage de façon à n'utiliser
     *       que l'opérateur =.
     */
    private static final ConfigurationKey SELECT = new ConfigurationKey("Environments:SELECT",
            "SELECT station, observable, value, error\n"  +
            "  FROM \"AllEnvironments\"\n"                +
            " WHERE (station LIKE ?) AND (observable LIKE ?)");

    /**
     * Requête SQL pour insérer les mesures pour une station et un observable donnés.
     */
    private static final ConfigurationKey INSERT = new ConfigurationKey("Environments:INSERT",
            "INSERT INTO \"Environments\" (station, observable, value, error)\n"  +
            "VALUES (?, ?, ?, ?)");

    /**
     * Construit une nouvelle connexion vers la table des mesures.
     *
     * @param  database Connexion vers la base de données des observations.
     */
    public EnvironmentTable(final Database database) {
        super(database, SELECT, INSERT);
    }

    /** 
     * Construit une nouvelle connexion vers la table des mesures pour les stations spécifiées.
     * 
     * @param  stations La table des stations à utiliser.
     */
    public EnvironmentTable(final StationTable stations) {
        super(stations, SELECT, INSERT);
    }

    /**
     * Construit une nouvelle connexion vers la table des mesures en utilisant une table
     * des stations du type spécifié.
     *
     * @param  database Connexion vers la base de données des observations.
     * @param  type Type de table des stations que devra utiliser cette instance. Un argument
     *         typique est <code>{@linkplain LongLineTable}.class</code>.
     * @param  providers Si les stations doivent être limitées à celles d'un fournisseur,
     *         liste de ces fournisseurs.
     */
    public EnvironmentTable(final Database                  database,
                            final Class<? extends StationTable> type,
                            final String...                providers)
    {
        this(database);
        final StationTable stations = database.getTable(type);
        for (String provider : providers) {
            stations.acceptableProvider(provider);
        }
        stations.setAbridged(true);
        setStationTable(stations);
    }
}
