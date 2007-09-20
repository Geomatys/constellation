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

import java.util.Collection;
import java.util.List;
import org.opengis.observation.CompositePhenomenon;
import org.opengis.observation.Phenomenon;

// geotools dependencies
import org.geotools.resources.Utilities;

/**
  * Une propriété complexe composée de plusieur {@linkPlain Phenomenon phenomenon}
  *
  * @version $Id:
  * @author Guilhem Legal
  */
public class CompositePhenomenonEntry extends PhenomenonEntry implements CompositePhenomenon{
    
    /**
     * Le phenomene de base.
     */
    private PhenomenonEntry base;
    
    /**
     * le nombre de composant
     */
    private int dimension;
    
    /**
     * Les composants.
     */
    private Collection<PhenomenonEntry> component;
   
    /** 
     * Crée un nouveau phenomene composé
     */
    public CompositePhenomenonEntry(final String id, final String name, final String description
            ,final PhenomenonEntry base, final Collection<PhenomenonEntry> component) {
        super(id, name, description);
        this.base = base;
        this.component = component;
        this.dimension = component.size();
        
    }
    
    /**
     * Retourne le phenomene de base.
     */
    public PhenomenonEntry getBase(){
        return base;
    }
    
    /**
     * Ajoute un composant a la liste 
     */
    public void addComponent(PhenomenonEntry phenomenon) {
        component.add(phenomenon);
    }
    
    /**
     * Retourne les composants.
     */
    public Collection<PhenomenonEntry> getComponent() {
        return component;
    }

    /**
     * Retourne le nombre de composant.
     */
    public int getDimension() {
        return dimension;
    }
    
    /**
     * Retourne un code représentant ce phenomene composé.
     */
    @Override
    public final int hashCode() {
        return base.hashCode() ^ component.hashCode();
    }

    /**
     * Vérifie si cette entré est identique à l'objet spécifié.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final CompositePhenomenonEntry that = (CompositePhenomenonEntry) object;
            return Utilities.equals(this.base,         that.base) &&
                   Utilities.equals(this.component,    that.component) &&
                   Utilities.equals(this.dimension,    that.dimension) ;
        }
        return false;
    }
        
    
}
