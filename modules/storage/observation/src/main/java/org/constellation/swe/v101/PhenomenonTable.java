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
package org.constellation.swe.v101;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.geotoolkit.internal.sql.table.CatalogException;
import org.geotoolkit.internal.sql.table.Database;
import org.geotoolkit.internal.sql.table.LocalCache;
import org.geotoolkit.internal.sql.table.LocalCache.Stmt;
import org.geotoolkit.internal.sql.table.QueryType;
import org.geotoolkit.internal.sql.table.SingletonTable;

// OpenGis dependencies
import org.geotoolkit.swe.xml.v101.PhenomenonType;


/**
 * Connexion to the phenomenon table.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Guilhem Legal
 */
public class PhenomenonTable extends SingletonTable<PhenomenonType> implements Cloneable {
   
    /**
     * Construit une table des phénomènes.
     * 
     * @param  database Connexion vers la base de données.
     */
    public PhenomenonTable(final Database database) {
        this(new PhenomenonQuery(database));
    }
    
    /**
     * Initialise l'identifiant de la table.
     */
    protected PhenomenonTable(final PhenomenonQuery query) {
        super(query,query.byName);
    }
     /**
     * Construit une nouvelle table non partagée
     */
    private PhenomenonTable(final PhenomenonTable table) {
        super(table);
    }

    /**
     * Returns a copy of this table. This is a copy constructor used for obtaining
     * a new instance to be used concurrently with the original instance.
     */
    @Override
    protected PhenomenonTable clone() {
        return new PhenomenonTable(this);
    }

    /**
     * Construit un phénomène pour l'enregistrement courant.
     */
    @Override
    protected PhenomenonType createEntry(final LocalCache lc, final ResultSet results, Comparable<?> identifier) throws SQLException, CatalogException {
        final PhenomenonQuery localQuery = (PhenomenonQuery) super.query;
        return new PhenomenonType(results.getString(indexOf(localQuery.identifier)),
                                   results.getString(indexOf(localQuery.name)),
                                   results.getString(indexOf(localQuery.remarks)));
    }
    
     /**
     * Retourne un nouvel identifier (ou l'identifier du phenomene passée en parametre si non-null)
     * et enregistre le nouveau phenomene dans la base de donnée.
     *
     * @param result le resultat a inserer dans la base de donnée.
     */
    public String getIdentifier(final PhenomenonType pheno) throws SQLException, CatalogException {
        final PhenomenonQuery localQuery  = (PhenomenonQuery) super.query;
        String id;
        boolean success = false;
        final LocalCache lc = getLocalCache();
        synchronized (lc) {
            transactionBegin(lc);
            try {
                if (pheno.getId() != null) {
                    final Stmt statement = getStatement(lc, QueryType.EXISTS);
                    statement.statement.setString(indexOf(localQuery.identifier), pheno.getId());
                    final ResultSet result = statement.statement.executeQuery();
                    if(result.next()) {
                        success = true;
                        result.close();
                        release(lc,statement);
                        return pheno.getId();
                    } else {
                        id = pheno.getId();
                    }
                    result.close();
                    release(lc, statement);
                } else {
                    id = searchFreeIdentifier(lc, "pheno");
                }
                final Stmt statement = getStatement(lc, QueryType.INSERT);
                statement.statement.setString(indexOf(localQuery.identifier), id);
                statement.statement.setString(indexOf(localQuery.name), pheno.getName());
                statement.statement.setString(indexOf(localQuery.remarks), pheno.getDescription());

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
