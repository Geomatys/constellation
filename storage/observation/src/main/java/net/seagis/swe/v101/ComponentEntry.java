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
package net.seagis.swe.v101;

import net.seagis.catalog.Entry;
import org.geotools.util.Utilities;

/**
 * Une liste de phénomèmene pour un phenoméne composé.
 *
 * @version $Id:
 * @author Guilhem Legal
 */
public class ComponentEntry extends Entry {
    
    /**
     * L'identifiant du phénomène composé.
     */
    private String idCompositePhenomenon;
    
    /**
     * Le phenomene associé a ce phenomene composé.
     */
    private PhenomenonEntry component;
    
    /**
     * Crée une nouvelle liste de phénomene. 
     */
    public ComponentEntry(String idCompositePhenomenon, PhenomenonEntry component) {
        super(component.getId());
        this.idCompositePhenomenon = idCompositePhenomenon;
        this.component             = component;
    }

    /**
     * Retourne l'id du phénomène composé.
     */
    public String getIdCompositePhenomenon() {
        return idCompositePhenomenon;
    }

    /**
     * Retourne le phénomène associé.
     */
    public PhenomenonEntry getComponent() {
        return component;
    }
    
     /**
     * Vérifie si cette entré est identique à l'objet spécifié.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        final ComponentEntry that = (ComponentEntry) object;
        
        return  Utilities.equals(this.component,   that.component)   &&
                Utilities.equals(this.idCompositePhenomenon,   that.idCompositePhenomenon);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + (this.idCompositePhenomenon != null ? this.idCompositePhenomenon.hashCode() : 0);
        hash = 37 * hash + (this.component != null ? this.component.hashCode() : 0);
        return hash;
    }
    
}
