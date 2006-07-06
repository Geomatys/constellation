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
 * Connexion vers la table des {@linkplain Operation op�rations} susceptibles d'�tre appliqu�es
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
     * Requ�te SQL utilis�e par cette classe pour obtenir les op�rations.
     * L'ordre des colonnes est essentiel. 
     */
    private static final ConfigurationKey SELECT = new ConfigurationKey("Operations:SELECT", 
            "SELECT name, "            +   // [01] name
                   "prefix, "          +   // [02] prefix
                   "operation, "       +   // [03] operation
                   "description\n"     +   // [04] description
            "  FROM \"Operations\"\n"  +
            " WHERE name=?");

    /** Num�ro de colonne. */ private static final int NAME         =  1;
    /** Num�ro de colonne. */ private static final int PREFIX       =  2;
    /** Num�ro de colonne. */ private static final int OPERATION    =  3;
    /** Num�ro de colonne. */ private static final int DESCRIPTION  =  4;

    /** 
     * La table des param�tres des op�rations. Ne sera construite que la premi�re fois
     * o� elle sera n�cessaire.
     */
    private OperationParameterTable parameters;

    /** 
     * Construit une table des op�rations.
     * 
     * @param  database Connexion vers la base de donn�es.
     */
    public OperationTable(final Database database) {
        super(database);
    }

    /**
     * Retourne la requ�te SQL � utiliser pour obtenir les op�rations.
     */
    @Override
    protected String getQuery(final QueryType type) throws SQLException {
        switch (type) {
            case SELECT: return getProperty(SELECT);
            default:     return super.getQuery(type);
        }
    }

    /**
     * Construit une op�ration pour l'enregistrement courant.
     *
     * @throws  SQLException            Si l'interrogation de la base de donn�es a �chou�.
     * @throws  IllegalRecordException  Si un des param�tres trouv�s dans la base de donn�es
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
