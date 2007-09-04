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

import java.util.List;
import org.opengis.observation.CompositePhenomenon;
import org.opengis.observation.Phenomenon;

/**
 *CompositePhenomenonEntry.java
 *
 * @author Guilhem Legal
 * @author Mehdi Sidhoum
 */
public class CompositePhenomenonEntry extends PhenomenonEntry implements CompositePhenomenon{
    
    /**
     * Le phenomene de base.
     */
    private Phenomenon base;
    
    /**
     * le nombre de composant
     */
    private int dimension;
    
    /**
     * Les composants.
     */
    private List<Phenomenon> component;
   
    /** 
     * Crée un nouveau phenomene composé
     */
    public CompositePhenomenonEntry(final String id, final String name, final String description
            ,final List<Phenomenon> component) {
        super(id, name, description);
        this.component = component;
        
    }
    
    /**
     * Retourne le phenomene de base.
     */
    public Phenomenon getBase(){
        return base;
    }
    
    /**
     * Retourne les composants.
     */
    public List<Phenomenon> getComponent() {
        return component;
    }

    /**
     * Retourne le nombre de composant.
     */
    public int getDimension() {
        return dimension;
    }
        
    
}
