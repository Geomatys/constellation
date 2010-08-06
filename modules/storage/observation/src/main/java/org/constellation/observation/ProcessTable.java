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

import java.sql.ResultSet;
import java.sql.SQLException;
import org.geotoolkit.internal.sql.table.CatalogException;
import org.geotoolkit.internal.sql.table.Database;
import org.geotoolkit.internal.sql.table.LocalCache;
import org.geotoolkit.internal.sql.table.LocalCache.Stmt;
import org.geotoolkit.internal.sql.table.QueryType;
import org.geotoolkit.internal.sql.table.SingletonTable;
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
        super(query, query.byName);
    }

    /**
     * Construit une nouvelle table non partagée
     */
    private ProcessTable(final ProcessTable table) {
        super(table);
    }

    /**
     * Returns a copy of this table. This is a copy constructor used for obtaining
     * a new instance to be used concurrently with the original instance.
     */
    @Override
    protected ProcessTable clone() {
        return new ProcessTable(this);
    }

    /**
     * Construit une procédure pour l'enregistrement courant.
     */
    @Override
    protected ProcessEntry createEntry(final LocalCache lc, final ResultSet results, Comparable<?> identifier) throws SQLException {
        final ProcessQuery query = (ProcessQuery) super.query;
        return new ProcessEntry(results.getString(indexOf(query.name)));
    }
    
    /**
     * Retourne un nouvel identifier (ou l'identifier du capteur passée en parametre si non-null)
     * et enregistre le nouveau capteur dans la base de donnée si il n'y est pas deja.
     *
     * @param proc le capteur a inserer dans la base de donnée.
     */
    public String getIdentifier(final ProcessEntry proc) throws SQLException, CatalogException {
        final ProcessQuery query  = (ProcessQuery) super.query;
        String id;
        boolean success = false;
        final LocalCache lc = getLocalCache();
        synchronized (lc) {
            transactionBegin(lc);
            try {
                if (proc.getName() != null) {
                    final Stmt statement = getStatement(lc, QueryType.EXISTS);
                    statement.statement.setString(indexOf(query.name), proc.getName());
                    final ResultSet result = statement.statement.executeQuery();
                    if(result.next()) {
                        success = true;
                        result.close();
                        release(lc, statement);
                        return proc.getName();
                    } else {
                        id = proc.getName();
                    }
                    result.close();
                    release(lc, statement);
                } else {
                    id = searchFreeIdentifier(lc, "procedure");
                }

                final Stmt statement = getStatement(lc, QueryType.INSERT);

                statement.statement.setString(indexOf(query.name), id);
                statement.statement.setNull(indexOf(query.remarks), java.sql.Types.VARCHAR);
                updateSingleton(statement.statement);
                release(lc, statement);
                success = true;
            } finally {
                transactionEnd(lc, success);
            }
        }
        return id;
    }
}
