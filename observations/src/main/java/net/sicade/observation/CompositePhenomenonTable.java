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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import net.sicade.catalog.Database;
import net.sicade.catalog.SingletonTable;
import org.opengis.observation.CompositePhenomenon;
import org.opengis.observation.Phenomenon;

/**
 * Connexion vers la table des {@linkplain CompositePhenomenon phénoménes composé}.
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class CompositePhenomenonTable extends SingletonTable<CompositePhenomenon>{
    
    /**
     * Connexion vers la table des {@linkplain Phenomenon phénomènes}.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    protected PhenomenonTable phenomenons;
    
    /**
     * Connexion vers la table des {@linkplain ComponentTable composants}.
     * Une connexion (potentiellement partagée) sera établie la première fois où elle sera nécessaire.
     */
    protected ComponentTable components;
    
    
   /**
     * Construit une table des phénoménes composé.
     * 
     * @param  database Connexion vers la base de données.
     */
    public CompositePhenomenonTable(final Database database) {
        super(new CompositePhenomenonQuery(database));
    }
    
    /**
     * Initialise l'identifiant de la table.
     */
    private CompositePhenomenonTable(final CompositePhenomenonQuery query) {
        super(query);
        setIdentifierParameters(query.byIdentifier, null);
    }
    
    /**
     * Construit un phénoméne pour l'enregistrement courant.
     */
    protected CompositePhenomenon createEntry(final ResultSet results) throws SQLException {
        final CompositePhenomenonQuery query = (CompositePhenomenonQuery) super.query;
        
        String idCompositePhenomenon = results.getString(indexOf(query.identifier));
        
        if (phenomenons == null) {
            phenomenons = getDatabase().getTable(PhenomenonTable.class);
        }
        Phenomenon base = phenomenons.getEntry(results.getString(indexOf(query.base)));
        
        if (components == null) {
            components =  getDatabase().getTable(ComponentTable.class);
        }
        components.setIdCompositePhenomenon(idCompositePhenomenon);
        Collection<ComponentEntry> entries = components.getEntries();
        
        Collection<Phenomenon> components = new HashSet<Phenomenon>();
        
        Iterator i = entries.iterator();
        while(i.hasNext()) {
            ComponentEntry c =(ComponentEntry) i.next();
            components.add(c.getComponent());
        }
        
        return new CompositePhenomenonEntry(results.getString(indexOf(query.name   )),
                                   results.getString(indexOf(query.remarks)),
                                   idCompositePhenomenon,
                                   base,
                                   components);
    }
    
}
