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

// J2SE dependencies
import java.sql.ResultSet;
import java.sql.SQLException;

// Constellation dependencies
import org.constellation.catalog.ConfigurationKey;
import org.constellation.catalog.Database;
import org.constellation.catalog.SingletonTable;
import org.constellation.catalog.CatalogException;
import org.constellation.sampling.SamplingFeatureTable;


/**
 * Intéroge la base de données pour obtenir la liste des catégories d'espèces observées.
 *
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
@Deprecated
public class CategoryTable extends SingletonTable<CategoryEntry> {
    /**
     * Requête SQL pour obtenir une catégorie à partir de son identifiant.
     */
    private static final ConfigurationKey SELECT = null; // new ConfigurationKey("Categories:SELECT",
//            "SELECT symbol, identifier, phenomenon, procedure, stage, NULL as remarks\n" +
//            "  FROM \"Categories\"\n"                                                    +
//            " WHERE symbol LIKE ?\n"                                                     +
//            "   AND selected=TRUE\n"                                                     +
//            " ORDER BY identifier");

    /** Numéro de colonne. */ private static final int  FEATUREOFINTEREST= 1;
    /** Numéro de colonne. */ private static final int  PHENOMENON = 2;
    /** Numéro de colonne. */ private static final int  PROCEDURE  = 3;
    /** Numéro de colonne. */ private static final int  STAGE      = 4;
    /** Numéro de colonne. */ private static final int  REMARKS    = 5;
     /** Numéro de colonne. */ private static final int  NAME      = 6;

    /**
     * Table des stationss.
     * Ne sera construite que la première fois où elle sera nécessaire/
     */
    private transient SamplingFeatureTable stations;
    
    /**
     * Table des espèces.
     * Ne sera construite que la première fois où elle sera nécessaire/
     */
    private transient SpeciesTable species;

    /**
     * Table des stades de développement.
     * Ne sera construite que la première fois où elle sera nécessaire/
     */
    private transient StageTable stages;

    /**
     * Type de pêche.
     *
     * @todo Codé en dur pour l'instant. Devra être puisé dans une table dans une version future.
     */
    private static final FisheryTypeEntry fisheryType = new FisheryTypeEntry("pêche", null);

    /**
     * Construit une connexion vers la table des catégories.
     *
     * @param  database Connexion vers la base de données.
     */
    public CategoryTable(final Database database) {
        super(new org.constellation.catalog.Query(database, "category")); // TODO
    }

    /**
     * Construit une catégorie pour l'enregistrement courant.
     */
    protected CategoryEntry createEntry(final ResultSet result) throws SQLException, CatalogException {
        final String station     = result.getString(FEATUREOFINTEREST);
        final String phenomenon = result.getString(PHENOMENON);
        final String procedure  = result.getString(PROCEDURE);
        final String stage      = result.getString(STAGE);
        final String remarks    = result.getString(REMARKS);
        
        if (stations == null) {
            stations = getDatabase().getTable(SamplingFeatureTable.class);
        }
        if (species == null) {
            species = getDatabase().getTable(SpeciesTable.class);
        }
        if (stages == null) {
            stages = getDatabase().getTable(StageTable.class);
        }
        return new CategoryEntry(result.getString(NAME),
                                 remarks,
                                 stations.getEntry(station), 
                                 species.getEntry(phenomenon),
                                 stages.getEntry(stage),
                                 fisheryType,null,null,null,null,null,null);
    }
}
