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
package net.sicade.observation.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.sicade.observation.Observable;
import net.sicade.observation.Phenomenon;
import net.sicade.observation.Procedure;
import net.sicade.observation.Distribution;
import net.sicade.observation.ConfigurationKey;
import net.sicade.observation.CatalogException;


/**
 * Connexion vers la table des {@linkplain Observable observables}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
public class ObservableTable extends SingletonTable<Observable> implements Shareable {
    /**
     * Requ�te permettant de r�cup�rer une entr�e de la table des observables.
     */
    private static final ConfigurationKey SELECT = new ConfigurationKey("Observables:SELECT",
            "SELECT identifier AS name, identifier, phenomenon, procedure, NULL AS distribution, NULL AS description\n" +
            "  FROM \"Observables\"\n" + 
            " WHERE identifier=?");

    /** Num�ro de colonne. */ static final int NAME         = 1;
    /** Num�ro de colonne. */ static final int IDENTIFIER   = 2;
    /** Num�ro de colonne. */ static final int PHENOMENON   = 3;
    /** Num�ro de colonne. */ static final int PROCEDURE    = 4;
    /** Num�ro de colonne. */ static final int DISTRIBUTION = 5;
    /** Num�ro de colonne. */ static final int REMARKS      = 6;

    /**
     * Connexion vers la table des {@linkplain Phenomenon ph�nom�nes}.
     * Une connexion (potentiellement partag�e) sera �tablie la premi�re fois o� elle sera n�cessaire.
     */
    private PhenomenonTable phenomenons;

    /**
     * Connexion vers la table des {@linkplain Procedure procedures}.
     * Une connexion (potentiellement partag�e) sera �tablie la premi�re fois o� elle sera n�cessaire.
     */
    private ProcedureTable procedures;

    /**
     * Connexion vers la table des {@linkplain Distribution distributions}.
     * Une connexion (potentiellement partag�e) sera �tablie la premi�re fois o� elle sera n�cessaire.
     */
    private DistributionTable distributions;
    
    /** 
     * Construit une nouvelle connexion vers la table des observables.
     * 
     * @param  database Connexion vers la base de donn�es.
     */
    public ObservableTable(final Database database) {
        super(database);
    }

    /**
     * Retourne la requ�te SQL � utiliser pour obtenir les observables.
     */
    @Override
    protected String getQuery(final QueryType type) throws SQLException {
        switch (type) {
            case SELECT: return getProperty(SELECT);
            default:     return super.getQuery(type);
        }
    }

    /**
     * Construit un observable pour l'entr�e courante.
     */
    protected Observable createEntry(final ResultSet result) throws CatalogException, SQLException {
        final String name           = result.getString(NAME);
        final int    identifier     = result.getInt   (IDENTIFIER);
        final String phenomenonID   = result.getString(PHENOMENON);
        final String procedureID    = result.getString(PROCEDURE);
        final String distributionID = result.getString(DISTRIBUTION);
        final String remarks        = result.getString(REMARKS);
        if (phenomenons == null) {
            phenomenons = database.getTable(PhenomenonTable.class);
        }
        final Phenomenon phenomenon = phenomenons.getEntry(phenomenonID);
        if (procedures == null) {
            procedures = database.getTable(ProcedureTable.class);
        }
        final Procedure procedure = procedures.getEntry(procedureID);
        if (distributions == null) {
            distributions = database.getTable(DistributionTable.class);
        }
        final Distribution distribution = distributions.getEntry(distributionID);
        return new ObservableEntry(identifier, name, phenomenon, procedure, distribution, remarks);
    }
}
