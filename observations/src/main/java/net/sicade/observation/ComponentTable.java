/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
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
package net.sicade.observation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.sicade.catalog.CatalogException;
import net.sicade.catalog.Database;
import net.sicade.catalog.QueryType;
import net.sicade.catalog.SingletonTable;
import net.sicade.swe.SimpleDataRecordQuery;
import org.geotools.resources.Utilities;

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
    public ComponentTable(final ComponentTable table) {
        super(table);
    }
    
    /**
     * Initialise l'identifiant de la table.
     */
    private ComponentTable(final ComponentQuery query) {
        super(query);
        setIdentifierParameters(query.byComponent, null);
    }
    
    
    protected ComponentEntry createEntry(final ResultSet results) throws CatalogException, SQLException {
        final ComponentQuery query = (ComponentQuery) super.query;
                
        if (phenomenons == null) {
            phenomenons = getDatabase().getTable(PhenomenonTable.class);
        }
        PhenomenonEntry component = (PhenomenonEntry)phenomenons.getEntry(results.getString(indexOf(query.idComponent)));
        
        return new ComponentEntry(results.getString(indexOf(query.idCompositePhenomenon)), component);
    }
    
    /**
     * Specifie les parametres a utiliser dans la requetes de type "type".
     */
    @Override
    protected void configure(final QueryType type, final PreparedStatement statement) throws SQLException {
        super.configure(type, statement);
        final ComponentQuery query = (ComponentQuery) super.query;
        statement.setString(indexOf(query.byComposite), idCompositePhenomenon);
        
    }
    
    
    public String getIdCompositePhenomenon() {
        return idCompositePhenomenon;
    }
    
    public void setIdCompositePhenomenon(String idCompositePhenomenon) {
        if (!Utilities.equals(this.idCompositePhenomenon, idCompositePhenomenon)) {
            this.idCompositePhenomenon = idCompositePhenomenon;
            fireStateChanged("idCompositePhenomenon");
        }
        
    }
    
}
