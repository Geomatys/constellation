/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.observation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.constellation.catalog.CatalogException;
import org.constellation.catalog.Database;
import org.constellation.catalog.QueryType;
import org.constellation.catalog.SingletonTable;
import org.geotoolkit.observation.xml.v100.ProcessEntry;


/**
 * Connexion vers la table des {@linkplain Procedure procédures}.
 *
 * @version $Id$
 *
 * @author Martin Desruisseaux
 * @author Guilhem Legal
 */
public class ProcessTable extends SingletonTable<ProcessEntry> {
    
    /**
     * Construit une table des procédures.
     *
     * @param  database Connexion vers la base de données.
     */
    public ProcessTable(final Database database) {
        this(new ProcessQuery(database));
    }
    
    /**
     * Initialise l'identifiant de la table.
     */
    private ProcessTable(final ProcessQuery query) {
        super(query);
        setIdentifierParameters(query.byName, null);
    }
    
    /**
     * Construit une procédure pour l'enregistrement courant.
     */
    protected ProcessEntry createEntry(final ResultSet results) throws SQLException {
        final ProcessQuery query = (ProcessQuery) super.query;
        return new ProcessEntry(results.getString(indexOf(query.name)));
    }
    
    /**
     * Retourne un nouvel identifier (ou l'identifier du capteur passée en parametre si non-null)
     * et enregistre le nouveau capteur dans la base de donnée si il n'y est pas deja.
     *
     * @param proc le capteur a inserer dans la base de donnée.
     */
    public synchronized String getIdentifier(final ProcessEntry proc) throws SQLException, CatalogException {
        final ProcessQuery query  = (ProcessQuery) super.query;
        String id;
        boolean success = false;
        transactionBegin();
        try {
            if (proc.getName() != null) {
                final PreparedStatement statement = getStatement(QueryType.EXISTS);
                statement.setString(indexOf(query.name), proc.getName());
                final ResultSet result = statement.executeQuery();
                if(result.next()) {
                    success = true;
                    return proc.getName();
                } else {
                    id = proc.getName();
                }
            } else {
                id = searchFreeIdentifier("procedure");
            }
        
            final PreparedStatement statement = getStatement(QueryType.INSERT);
        
            statement.setString(indexOf(query.name), id);
            statement.setNull(indexOf(query.remarks), java.sql.Types.VARCHAR);
            updateSingleton(statement);
            success = true;
        } finally {
            transactionEnd(success);
        }
        return id;
    }
}
