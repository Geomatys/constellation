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
import org.geotoolkit.internal.sql.table.LocalCache.Stmt;
import org.geotoolkit.internal.sql.table.QueryType;
import org.geotoolkit.internal.sql.table.SingletonTable;

// OpenGis dependencies
import org.geotoolkit.swe.xml.v101.PhenomenonEntry;
import org.opengis.observation.Phenomenon;


/**
 * Connexion vers la table des {@linkplain Phenomenon phénomènes}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Guilhem Legal
 */
public class PhenomenonTable extends SingletonTable<PhenomenonEntry> {
   
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
    protected PhenomenonEntry createEntry(final ResultSet results, Comparable<?> identifier) throws SQLException, CatalogException {
        final PhenomenonQuery LocalQuery = (PhenomenonQuery) super.query;
        return new PhenomenonEntry(results.getString(indexOf(LocalQuery.identifier)),
                                   results.getString(indexOf(LocalQuery.name)),
                                   results.getString(indexOf(LocalQuery.remarks)));
    }
    
     /**
     * Retourne un nouvel identifier (ou l'identifier du phenomene passée en parametre si non-null)
     * et enregistre le nouveau phenomene dans la base de donnée.
     *
     * @param result le resultat a inserer dans la base de donnée.
     */
    public String getIdentifier(final PhenomenonEntry pheno) throws SQLException, CatalogException {
        final PhenomenonQuery localQuery  = (PhenomenonQuery) super.query;
        String id;
        boolean success = false;
        synchronized (getLock()) {
            transactionBegin();
            try {
                if (pheno.getId() != null) {
                    final Stmt statement = getStatement(QueryType.EXISTS);
                    statement.statement.setString(indexOf(localQuery.identifier), pheno.getId());
                    final ResultSet result = statement.statement.executeQuery();
                    if(result.next()) {
                        success = true;
                        result.close();
                        release(statement);
                        return pheno.getId();
                    } else {
                        id = pheno.getId();
                    }
                    result.close();
                    release(statement);
                } else {
                    id = searchFreeIdentifier("pheno");
                }
                final Stmt statement = getStatement(QueryType.INSERT);
                statement.statement.setString(indexOf(localQuery.identifier), id);
                statement.statement.setString(indexOf(localQuery.name), pheno.getName());
                statement.statement.setString(indexOf(localQuery.remarks), pheno.getDescription());

                updateSingleton(statement.statement);
                release(statement);
                success = true;
            } finally {
                transactionEnd(success);
            }
        }
        return id;
    }
}
