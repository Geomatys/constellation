/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 * (C) 2007, Geomatys
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

import java.sql.SQLException;
import java.sql.ResultSet;

import org.opengis.parameter.ParameterValueGroup;
import net.sicade.observation.CatalogException;
import net.sicade.observation.IllegalRecordException;
import net.sicade.observation.coverage.Operation;
import net.sicade.observation.sql.Column;
import net.sicade.observation.sql.Use;
import net.sicade.observation.sql.UsedBy;
import net.sicade.observation.sql.Database;
import net.sicade.observation.sql.Parameter;
import net.sicade.observation.sql.QueryType;
import net.sicade.observation.sql.Role;
import net.sicade.observation.sql.Shareable;
import net.sicade.observation.sql.SingletonTable;
import static net.sicade.observation.sql.QueryType.*;


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
     * Column name declared in the {@linkplain #query query}.
     */
    private final Column name, prefix, operation, remarks;

    /**
     * Parameter declared in the {@linkplain #query query}.
     */
    private final Parameter byName;

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
        final QueryType[] usage = {SELECT, LIST};
        name      = new Column   (query, "Operations", "name",        usage);
        prefix    = new Column   (query, "Operations", "prefix",      usage);
        operation = new Column   (query, "Operations", "operation",   usage);
        remarks   = new Column   (query, "Operations", "description", usage);
        byName    = new Parameter(query, name, SELECT);
        name.setRole(Role.NAME);
    }

    /**
     * Construit une opération pour l'enregistrement courant.
     *
     * @throws  SQLException            Si l'interrogation de la base de données a échoué.
     * @throws  IllegalRecordException  Si un des paramètres trouvés dans la base de données
     *          n'est pas connu par le groupe {@code parameters}, ou a une valeur invalide.
     */
    protected Operation createEntry(final ResultSet results) throws SQLException, CatalogException {
        final String              name      = results.getString(indexOf(this.name     ));
        final String              prefix    = results.getString(indexOf(this.prefix   ));
        final String              operation = results.getString(indexOf(this.operation));
        final String              remarks   = results.getString(indexOf(this.remarks  ));
        final OperationEntry      entry     = new OperationEntry(name, prefix, operation, remarks);
        final ParameterValueGroup values    = entry.getParameters();
        if (values != null) {
            if (parameters == null) {
                parameters = getDatabase().getTable(OperationParameterTable.class);
            }
            parameters.fillValues(name, values);
        }
        return entry;
    }
}
