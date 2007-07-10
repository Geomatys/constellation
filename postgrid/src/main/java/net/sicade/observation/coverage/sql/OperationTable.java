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
package net.sicade.observation.coverage.sql;

// J2SE dependencies
import java.sql.SQLException;
import java.sql.ResultSet;

// OpenGIS dependencies
import org.opengis.parameter.ParameterValueGroup;

// Sicade dependencies
import net.sicade.observation.ConfigurationKey;
import net.sicade.observation.IllegalRecordException;
import net.sicade.observation.coverage.Operation;
import net.sicade.observation.sql.Use;
import net.sicade.observation.sql.UsedBy;
import net.sicade.observation.sql.Database;
import net.sicade.observation.sql.QueryType;
import net.sicade.observation.sql.Shareable;
import net.sicade.observation.sql.SingletonTable;


/**
 * Connexion vers la table des {@linkplain Operation opérations} susceptibles d'être appliquées
 * sur des images.
 * 
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
@Use(OperationParameterTable.class)
@UsedBy(DescriptorTable.class)
public class OperationTable extends SingletonTable<Operation> implements Shareable {
    /**
     * Requête SQL utilisée par cette classe pour obtenir les opérations.
     * L'ordre des colonnes est essentiel. 
     */
    private static final ConfigurationKey SELECT = new ConfigurationKey("Operations:SELECT", 
            "SELECT name, "            +   // [01] name
                   "prefix, "          +   // [02] prefix
                   "operation, "       +   // [03] operation
                   "description\n"     +   // [04] description
            "  FROM \"Operations\"\n"  +
            " WHERE name=?");

    /** Numéro de colonne. */ private static final int NAME         =  1;
    /** Numéro de colonne. */ private static final int PREFIX       =  2;
    /** Numéro de colonne. */ private static final int OPERATION    =  3;
    /** Numéro de colonne. */ private static final int DESCRIPTION  =  4;

    /** 
     * La table des paramètres des opérations. Ne sera construite que la première fois
     * où elle sera nécessaire.
     */
    private OperationParameterTable parameters;

    /** 
     * Construit une table des opérations.
     * 
     * @param  database Connexion vers la base de données.
     */
    public OperationTable(final Database database) {
        super(database);
    }

    /**
     * Retourne la requête SQL à utiliser pour obtenir les opérations.
     */
    @Override
    protected String getQuery(final QueryType type) throws SQLException {
        switch (type) {
            case SELECT: return getProperty(SELECT);
            default:     return super.getQuery(type);
        }
    }

    /**
     * Construit une opération pour l'enregistrement courant.
     *
     * @throws  SQLException            Si l'interrogation de la base de données a échoué.
     * @throws  IllegalRecordException  Si un des paramètres trouvés dans la base de données
     *          n'est pas connu par le groupe {@code parameters}, ou a une valeur invalide.
     */
    protected Operation createEntry(final ResultSet results) throws SQLException, IllegalRecordException {
        final String              name      = results.getString(NAME);
        final String              prefix    = results.getString(PREFIX);
        final String              operation = results.getString(OPERATION);
        final String              remarks   = results.getString(DESCRIPTION);
        final OperationEntry      entry     = new OperationEntry(name, prefix, operation, remarks);
        final ParameterValueGroup values    = entry.getParameters();
        if (values != null) {
            if (parameters == null) {
                parameters = database.getTable(OperationParameterTable.class);
            }
            parameters.fillValues(name, values);
        }
        return entry;
    }
}
