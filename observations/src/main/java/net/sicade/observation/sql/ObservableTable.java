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
 */
package net.sicade.observation.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.sicade.observation.Observable;
import net.sicade.observation.PropertyType;
import net.sicade.observation.Process;
import net.sicade.coverage.model.Distribution;
import net.sicade.catalog.CatalogException;
import net.sicade.coverage.model.DistributionTable;
import net.sicade.catalog.ConfigurationKey;
import net.sicade.catalog.Database;
import net.sicade.catalog.Query;
import net.sicade.catalog.SingletonTable;


/**
 * Connexion vers la table des {@linkplain Observable observables}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
@Deprecated
public class ObservableTable extends SingletonTable<Observable> {
    /**
     * Requête permettant de récupérer une entrée de la table des observables.
     */
    private static final ConfigurationKey SELECT = null; // new ConfigurationKey("Observables:SELECT",
//            "SELECT identifier AS name, identifier, phenomenon, procedure, NULL AS distribution, NULL AS description\n" +
//            "  FROM \"Observables\"\n" + 
//            " WHERE identifier=?");

    /** Numéro de colonne. */ static final int NAME         = 1;
    /** Numéro de colonne. */ static final int IDENTIFIER   = 2;
    /** Numéro de colonne. */ static final int PHENOMENON   = 3;
    /** Numéro de colonne. */ static final int PROCEDURE    = 4;
    /** Numéro de colonne. */ static final int DISTRIBUTION = 5;
    /** Numéro de colonne. */ static final int REMARKS      = 6;

    /**
     * Connexion vers la table des {@linkplain Phenomenon phénomènes}.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    private PhenomenonTable phenomenons;

    /**
     * Connexion vers la table des {@linkplain Procedure procedures}.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    private ProcedureTable procedures;

    /**
     * Connexion vers la table des {@linkplain Distribution distributions}.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    private DistributionTable distributions;
    
    /** 
     * Construit une nouvelle connexion vers la table des observables.
     * 
     * @param  database Connexion vers la base de données.
     */
    public ObservableTable(final Database database) {
        super(new Query(database)); // TODO
    }

    /**
     * Construit un observable pour l'entrée courante.
     */
    protected Observable createEntry(final ResultSet result) throws CatalogException, SQLException {
        final String name           = result.getString(NAME);
        final int    identifier     = result.getInt   (IDENTIFIER);
        final String phenomenonID   = result.getString(PHENOMENON);
        final String procedureID    = result.getString(PROCEDURE);
        final String distributionID = result.getString(DISTRIBUTION);
        final String remarks        = result.getString(REMARKS);
        if (phenomenons == null) {
            phenomenons = getDatabase().getTable(PhenomenonTable.class);
        }
        final PropertyType phenomenon = phenomenons.getEntry(phenomenonID);
        if (procedures == null) {
            procedures = getDatabase().getTable(ProcedureTable.class);
        }
        final Process procedure = procedures.getEntry(procedureID);
        if (distributions == null) {
            distributions = getDatabase().getTable(DistributionTable.class);
        }
        final Distribution distribution = distributions.getEntry(distributionID);
        return new ObservableEntry(identifier, name, phenomenon, procedure, distribution, remarks);
    }
}
