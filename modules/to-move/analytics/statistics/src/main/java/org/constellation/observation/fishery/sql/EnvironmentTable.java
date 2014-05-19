/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.observation.fishery.sql;

// Constellation dependencies
import org.constellation.catalog.ConfigurationKey;
import org.constellation.catalog.Database;
import org.constellation.catalog.NoSuchTableException;
import org.constellation.observation.MeasurementQuery;
import org.constellation.observation.MeasurementTable;
import org.constellation.sampling.SamplingFeatureTable;


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
                            final String...                providers) throws NoSuchTableException
    {
        this(database);
        final SamplingFeatureTable stations = database.getTable(type);
        stations.setAbridged(true);
       // setStationTable(stations);
    }
}
