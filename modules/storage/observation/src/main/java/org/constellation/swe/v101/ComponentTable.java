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
import org.geotoolkit.internal.sql.table.CatalogException;
import org.geotoolkit.internal.sql.table.Database;
import org.geotoolkit.internal.sql.table.LocalCache.Stmt;
import org.geotoolkit.internal.sql.table.QueryType;
import org.geotoolkit.internal.sql.table.SingletonTable;
import org.geotoolkit.swe.xml.v101.ComponentEntry;
import org.geotoolkit.swe.xml.v101.PhenomenonEntry;
import org.geotoolkit.util.Utilities;

/**
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class ComponentTable extends SingletonTable<ComponentEntry>{
    
    /**
     * identifiant secondaire de la table.
     */
    private String idCompositePhenomenon;
    
    /**
     * un lien vers la table des phénomènes.
     */
    private PhenomenonTable phenomenons;
    
    /**
     * Construit une table des phenomene composé.
     *
     * @param  database Connexion vers la base de données.
     */
    public ComponentTable(final Database database) {
        this(new ComponentQuery(database));
    }
    
    /**
     * Construit une nouvelle table non partagée
     */
    private ComponentTable(final ComponentTable table) {
        super(table);
    }

    /**
     * Returns a copy of this table. This is a copy constructor used for obtaining
     * a new instance to be used concurrently with the original instance.
     */
    @Override
    protected ComponentTable clone() {
        return new ComponentTable(this);
    }
    
    /**
     * Initialise l'identifiant de la table.
     */
    private ComponentTable(final ComponentQuery query) {
        super(query, query.byComponent);
    }
    
    
    @Override
    protected ComponentEntry createEntry(final ResultSet results, Comparable<?> identifier) throws CatalogException, SQLException {
        final ComponentQuery query = (ComponentQuery) super.query;
        
        if (phenomenons == null) {
            phenomenons = getDatabase().getTable(PhenomenonTable.class);
        }
        final PhenomenonEntry component = (PhenomenonEntry)phenomenons.getEntry(results.getString(indexOf(query.idComponent)));
        
        return new ComponentEntry(results.getString(indexOf(query.idCompositePhenomenon)), component);
    }
    
    /**
     * Specifie les parametres a utiliser dans la requetes de type "type".
     */
    @Override
    protected void configure(final QueryType type, final PreparedStatement statement) throws SQLException, CatalogException {
        super.configure(type, statement);
        final ComponentQuery query = (ComponentQuery) super.query;
        if (! type.equals(QueryType.INSERT))
            statement.setString(indexOf(query.byComposite), idCompositePhenomenon);
        
    }
    
    
    public String getIdCompositePhenomenon() {
        return idCompositePhenomenon;
    }
    
    public synchronized void setIdCompositePhenomenon(String idCompositePhenomenon) {
        if (!Utilities.equals(this.idCompositePhenomenon, idCompositePhenomenon)) {
            this.idCompositePhenomenon = idCompositePhenomenon;
            fireStateChanged("idCompositePhenomenon");
        }
        
    }
    
    /**
     *Insere un nouveau composant d'une phenomene composé dans la base de donnée.
     *
     */
    public void getIdentifier(String idComposite, PhenomenonEntry pheno) throws SQLException, CatalogException {
        final ComponentQuery query  = (ComponentQuery) super.query;
        boolean success = false;
        synchronized (getLock()) {
            transactionBegin();
            try {
                if (phenomenons == null) {
                    phenomenons = getDatabase().getTable(PhenomenonTable.class);
                }
                final String idPheno = phenomenons.getIdentifier(pheno);

                Stmt statement = getStatement(QueryType.EXISTS);
                statement.statement.setString(indexOf(query.idCompositePhenomenon), idComposite);
                statement.statement.setString(indexOf(query.idComponent), idPheno);
                final ResultSet result = statement.statement.executeQuery();
                if(result.next()) {
                    result.close();
                    release(statement);
                    success = true;
                    return ;
                }
                result.close();
                release(statement);

                statement = getStatement(QueryType.INSERT);
                statement.statement.setString(indexOf(query.idCompositePhenomenon), idComposite);
                statement.statement.setString(indexOf(query.idComponent), idPheno);
                updateSingleton(statement.statement);
                release(statement);
                success = true;
            } finally {
                transactionEnd(success);
            }
        }
    }
    
}
