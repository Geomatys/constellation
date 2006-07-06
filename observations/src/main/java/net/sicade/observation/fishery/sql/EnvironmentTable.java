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
package net.sicade.observation.fishery.sql;

// Sicade dependencies
import net.sicade.observation.ConfigurationKey;
import net.sicade.observation.sql.Database;
import net.sicade.observation.sql.StationTable;
import net.sicade.observation.sql.MeasurementTable;


/**
 * Table des param�tres environnementaux aux positions de p�ches.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class EnvironmentTable extends MeasurementTable {
    /**
     * Requ�te SQL pour obtenir les mesures pour une station et un observable donn�s.
     *
     * @todo L'utilisation d'instruction {@code LIKE} ralentit consid�rablement l'ex�cution
     *       de la requ�te. Il faudrait se d�barasser de ce bricolage de fa�on � n'utiliser
     *       que l'op�rateur =.
     */
    private static final ConfigurationKey SELECT = new ConfigurationKey("Environments:SELECT",
            "SELECT station, observable, value, error\n"  +
            "  FROM \"AllEnvironments\"\n"                +
            " WHERE (station LIKE ?) AND (observable LIKE ?)");

    /**
     * Requ�te SQL pour ins�rer les mesures pour une station et un observable donn�s.
     */
    private static final ConfigurationKey INSERT = new ConfigurationKey("Environments:INSERT",
            "INSERT INTO \"Environments\" (station, observable, value, error)\n"  +
            "VALUES (?, ?, ?, ?)");

    /**
     * Construit une nouvelle connexion vers la table des mesures.
     *
     * @param  database Connexion vers la base de donn�es des observations.
     */
    public EnvironmentTable(final Database database) {
        super(database, SELECT, INSERT);
    }

    /** 
     * Construit une nouvelle connexion vers la table des mesures pour les stations sp�cifi�es.
     * 
     * @param  stations La table des stations � utiliser.
     */
    public EnvironmentTable(final StationTable stations) {
        super(stations, SELECT, INSERT);
    }

    /**
     * Construit une nouvelle connexion vers la table des mesures en utilisant une table
     * des stations du type sp�cifi�.
     *
     * @param  database Connexion vers la base de donn�es des observations.
     * @param  type Type de table des stations que devra utiliser cette instance. Un argument
     *         typique est <code>{@linkplain LongLineTable}.class</code>.
     * @param  providers Si les stations doivent �tre limit�es � celles d'un fournisseur,
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
