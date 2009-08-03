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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import org.constellation.catalog.CatalogException;
import org.constellation.catalog.Database;
import org.constellation.catalog.NoSuchTableException;
import org.constellation.catalog.QueryType;
import org.constellation.catalog.SingletonTable;
import org.geotoolkit.swe.xml.v101.ComponentEntry;
import org.geotoolkit.swe.xml.v101.CompositePhenomenonEntry;
import org.geotoolkit.swe.xml.v101.PhenomenonEntry;

/**
 * Connexion vers la table des {@linkplain CompositePhenomenon phénoménes composé}.
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class CompositePhenomenonTable extends SingletonTable<CompositePhenomenonEntry> {
    
    /**
     * Connexion vers la table des {@linkplain ComponentTable composants}.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    private ComponentTable components;
    
    
   /**
     * Construit une table des phénoménes composé.
     * 
     * @param  database Connexion vers la base de données.
     */
    public CompositePhenomenonTable(final Database database) {
        this(new CompositePhenomenonQuery(database));
    }
    
    /**
     * Initialise l'identifiant de la table.
     */
    private CompositePhenomenonTable(final CompositePhenomenonQuery query) {
        super(query);
        setIdentifierParameters(query.byName, null);
    }
    
    /**
     * Initialise l'identifiant de la table.
     */
   /* private CompositePhenomenonTable(final CompositePhenomenonQuery query) {
        super(query);
        setIdentifierParameters(query.byIdentifier, null);
    }*/
    
    /**
     * Construit un phénoméne pour l'enregistrement courant.
     */
    protected CompositePhenomenonEntry createEntry(final ResultSet results) throws SQLException, CatalogException{
        final CompositePhenomenonQuery query = (CompositePhenomenonQuery) super.query;
        
        final String idCompositePhenomenon = results.getString(indexOf(query.identifier));
        
        components = getComponentTable();
        components.setIdCompositePhenomenon(idCompositePhenomenon);
        final Collection<ComponentEntry> entries = components.getEntries();
        
        final Collection<PhenomenonEntry> compos = new HashSet<PhenomenonEntry>();
        
        final Iterator i = entries.iterator();
        while(i.hasNext()) {
            final ComponentEntry c =(ComponentEntry) i.next();
            compos.add(c.getComponent());
        }
        
        return new CompositePhenomenonEntry(idCompositePhenomenon,
                                            results.getString(indexOf(query.name)),
                                            results.getString(indexOf(query.remarks)),
                                            null,
                                            compos);
    }
    
    /**
     * Retourne un nouvel identifier (ou l'identifier du phenomene passée en parametre si non-null)
     * et enregistre le nouveau phenomene dans la base de donnée.
     *
     * @param pheno le phenomene a inserer dans la base de donnée.
     */
    public synchronized String getIdentifier(final CompositePhenomenonEntry pheno) throws SQLException, CatalogException {
        final CompositePhenomenonQuery query  = (CompositePhenomenonQuery) super.query;
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
                result.close();
            } else {
                id = searchFreeIdentifier("compositepheno");
            }
            final PreparedStatement statement = getStatement(QueryType.INSERT);
        
            statement.setString(indexOf(query.identifier), id);
            statement.setString(indexOf(query.name), pheno.getName());
            statement.setString(indexOf(query.remarks), pheno.getDescription());
            statement.setInt(indexOf(query.dimension), pheno.getDimension());
            updateSingleton(statement); 

            components = getComponentTable();
            final Iterator<PhenomenonEntry> i = pheno.getComponent().iterator();
        
            while(i.hasNext()) {
                final PhenomenonEntry ph = i.next();
                components.getIdentifier(id, ph);
            }
            success = true;
        } finally {
            transactionEnd(success);
        }
        return id;
    }
    
    public ComponentTable getComponentTable() throws NoSuchTableException {
        if (components == null) {
            components =  getDatabase().getTable(ComponentTable.class);
            components =  new ComponentTable(components);
        }
        return components;
    }
    
}
