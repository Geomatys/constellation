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
import net.sicade.catalog.CatalogException;
import net.sicade.catalog.SingletonTable;
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
    private String idCompositePhenomenons;
    
    /**
     *
     */
    public ComponentTable() {
    }
    
    protected ComponentEntry createEntry(final ResultSet results) throws CatalogException, SQLException {
    }
    
    public String getIdCompositePhenomenons() {
        return idCompositePhenomenons;
    }
    
    public void setIdCompositePhenomenons(String idCompositePhenomenons) {
        if (!Utilities.equals(this.idCompositePhenomenons, idCompositePhenomenons)) {
            this.idCompositePhenomenons = idCompositePhenomenons;
            fireStateChanged("idDataBlock");
        }
        
    }
    
}
