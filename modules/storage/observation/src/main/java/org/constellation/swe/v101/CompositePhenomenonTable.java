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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import org.geotoolkit.internal.sql.table.CatalogException;
import org.geotoolkit.internal.sql.table.Database;
import org.geotoolkit.internal.sql.table.LocalCache;
import org.geotoolkit.internal.sql.table.LocalCache.Stmt;
import org.geotoolkit.internal.sql.table.NoSuchTableException;
import org.geotoolkit.internal.sql.table.QueryType;
import org.geotoolkit.internal.sql.table.SingletonTable;
import org.geotoolkit.swe.xml.v101.ComponentType;
import org.geotoolkit.swe.xml.v101.CompositePhenomenonType;
import org.geotoolkit.swe.xml.v101.PhenomenonType;

/**
 * Connexion vers la table des {@linkplain CompositePhenomenon phénoménes composé}.
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class CompositePhenomenonTable extends SingletonTable<CompositePhenomenonType> {
    
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
        super(query, query.byName);
    }

     /**
     * Construit une nouvelle table non partagée
     */
    private CompositePhenomenonTable(final CompositePhenomenonTable table) {
        super(table);
    }

    /**
     * Returns a copy of this table. This is a copy constructor used for obtaining
     * a new instance to be used concurrently with the original instance.
     */
    @Override
    protected CompositePhenomenonTable clone() {
        return new CompositePhenomenonTable(this);
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
    @Override
    protected CompositePhenomenonType createEntry(final LocalCache lc, final ResultSet results, Comparable<?> identifier) throws SQLException, CatalogException{
        final CompositePhenomenonQuery query = (CompositePhenomenonQuery) super.query;
        
        final String idCompositePhenomenon = results.getString(indexOf(query.identifier));
        
        components = getComponentTable();
        components.setIdCompositePhenomenon(idCompositePhenomenon);
        final Collection<ComponentType> entries = components.getEntries();
        
        final Collection<PhenomenonType> compos = new HashSet<PhenomenonType>();
        
        final Iterator i = entries.iterator();
        while(i.hasNext()) {
            final ComponentType c =(ComponentType) i.next();
            compos.add(c.getComponent());
        }
        
        return new CompositePhenomenonType(idCompositePhenomenon,
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
    public String getIdentifier(final CompositePhenomenonType pheno) throws SQLException, CatalogException {
        final CompositePhenomenonQuery query  = (CompositePhenomenonQuery) super.query;
        String id;
        boolean success = false;
        final LocalCache lc = getLocalCache();
        synchronized (lc) {
            transactionBegin(lc);
            try {
                if (pheno.getId() != null) {
                    final Stmt statement = getStatement(lc, QueryType.EXISTS);
                    statement.statement.setString(indexOf(query.identifier), pheno.getId());
                    final ResultSet result = statement.statement.executeQuery();
                    if(result.next()) {
                        success = true;
                        result.close();
                        release(lc, statement);
                        return pheno.getId();
                    } else {
                        id = pheno.getId();
                    }
                    result.close();
                    release(lc, statement);
                } else {
                    id = searchFreeIdentifier(lc, "compositepheno");
                }
                final Stmt statement = getStatement(lc, QueryType.INSERT);

                statement.statement.setString(indexOf(query.identifier), id);
                statement.statement.setString(indexOf(query.name), pheno.getName());
                statement.statement.setString(indexOf(query.remarks), pheno.getDescription());
                statement.statement.setInt(indexOf(query.dimension), pheno.getDimension());
                updateSingleton(statement.statement);
                release(lc, statement);

                components = getComponentTable();
                final Iterator<PhenomenonType> i = pheno.getComponent().iterator();

                while(i.hasNext()) {
                    final PhenomenonType ph = i.next();
                    components.getIdentifier(id, ph);
                }
                success = true;
            } finally {
                transactionEnd(lc, success);
            }
        }
        return id;
    }
    
    public ComponentTable getComponentTable() throws NoSuchTableException {
        if (components == null) {
            components =  getDatabase().getTable(ComponentTable.class);
        }
        return components;
    }
    
}
