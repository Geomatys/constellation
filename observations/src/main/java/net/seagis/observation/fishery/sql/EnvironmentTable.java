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
package net.seagis.observation.fishery.sql;

// Sicade dependencies
import net.seagis.catalog.ConfigurationKey;
import net.seagis.catalog.Database;
import net.seagis.observation.MeasurementQuery;
import net.seagis.observation.SamplingFeatureTable;
import net.seagis.observation.MeasurementTable;


/**
 * Table des paramètres environnementaux aux positions de pêches.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
@Deprecated
public class EnvironmentTable extends MeasurementTable {
    /**
     * Requête SQL pour obtenir les mesures pour une station et un observable donnés.
     */
    private static final ConfigurationKey SELECT = null; // new ConfigurationKey("Environments:SELECT",
//            "SELECT station, observable, value, error\n"  +
//            "  FROM \"AllEnvironments\"\n"                +
//            " WHERE (station = ?) AND (observable = ?)");

    /**
     * Requête SQL pour insérer les mesures pour une station et un observable donnés.
     */
    private static final ConfigurationKey INSERT = null; // new ConfigurationKey("Environments:INSERT",
//            "INSERT INTO \"Environments\" (station, observable, value, error)\n"  +
//            "VALUES (?, ?, ?, ?)");

    /**
     * Construit une nouvelle connexion vers la table des mesures.
     *
     * @param  database Connexion vers la base de données des observations.
     */
    public EnvironmentTable(final Database database) {
        super(database);
    }

    /** 
     * Construit une nouvelle connexion vers la table des mesures pour les stations spécifiées.
     * 
     * @param  stations La table des stations à utiliser.
     */
    public EnvironmentTable(final SamplingFeatureTable stations) {
        super(new MeasurementQuery(null));
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
                            final Class<? extends SamplingFeatureTable> type,
                            final String...                providers)
    {
        this(database);
        final SamplingFeatureTable stations = database.getTable(type);
        stations.setAbridged(true);
       // setStationTable(stations);
    }
}
