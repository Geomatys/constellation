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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.constellation.catalog.CatalogException;
import org.constellation.catalog.Database;
import org.constellation.catalog.QueryType;
import org.constellation.catalog.SingletonTable;

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
public class PhenomenonTable<EntryType extends Phenomenon> extends SingletonTable<PhenomenonEntry> {
   
    /**
     * Construit une table des phénomènes.
     * 
     * @param  database Connexion vers la base de données.
     */
    public PhenomenonTable(final Database database) {
        super(new PhenomenonQuery(database));
        final PhenomenonQuery query = new PhenomenonQuery(database);
        setIdentifierParameters(query.byName, null);
    }
    
    /**
     * Initialise l'identifiant de la table.
     */
    protected PhenomenonTable(final PhenomenonQuery query) {
        super(query);
        setIdentifierParameters(query.byName, null);
    }
    
    /**
     * Construit un phénomène pour l'enregistrement courant.
     */
    protected PhenomenonEntry createEntry(final ResultSet results) throws SQLException, CatalogException {
        final PhenomenonQuery query = (PhenomenonQuery) super.query;
        return new PhenomenonEntry(results.getString(indexOf(query.identifier)),
                                   results.getString(indexOf(query.name)),
                                   results.getString(indexOf(query.remarks)));
    }
    
     /**
     * Retourne un nouvel identifier (ou l'identifier du phenomene passée en parametre si non-null)
     * et enregistre le nouveau phenomene dans la base de donnée.
     *
     * @param result le resultat a inserer dans la base de donnée.
     */
    public synchronized String getIdentifier(final PhenomenonEntry pheno) throws SQLException, CatalogException {
        final PhenomenonQuery query  = (PhenomenonQuery) super.query;
        String id;
        boolean success = false;
        transactionBegin();
        try {
            if (pheno.getId() != null) {
                final PreparedStatement statement = getStatement(QueryType.EXISTS);
                statement.setString(indexOf(query.identifier), pheno.getId());
                final ResultSet result = statement.executeQuery();
                if(result.next()) {
                    success = true;
                    return pheno.getId();
                } else {
                    id = pheno.getId();
                }
            } else {
                id = searchFreeIdentifier("pheno");
            }
            final PreparedStatement statement = getStatement(QueryType.INSERT);
            statement.setString(indexOf(query.identifier), id);
            statement.setString(indexOf(query.name), pheno.getName());
            statement.setString(indexOf(query.remarks), pheno.getDescription());
        
            updateSingleton(statement);
            success = true;
        } finally {
            transactionEnd(success);
        }
        return id;
    }
}
