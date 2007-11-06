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
import java.util.Iterator;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.opengis.observation.CompositePhenomenon;

// geotools dependencies
import org.geotools.resources.Utilities;

/**
  * Une propriété complexe composée de plusieur {@linkPlain Phenomenon phenomenon}
  *
  * @version $Id:
  * @author Guilhem Legal
  */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CompositePhenomenon", propOrder = {"base", "dimension", "component"})
public class CompositePhenomenonEntry extends PhenomenonEntry implements CompositePhenomenon{
    
    /**
     * Le phenomene de base.
     */
    private PhenomenonEntry base;
    
    /**
     * le nombre de composant
     */
    @XmlAttribute
    private int dimension;
    
    /**
     * Les composants.
     */
    @XmlElement(name="component")
    private Collection<PhenomenonEntry> component;
   
    /** 
     * constructeur vide utilisé par JAXB.
     */
    protected CompositePhenomenonEntry(){}
            
    /** 
     * Crée un nouveau phenomene composé
     */
    public CompositePhenomenonEntry(final String id, final String name, final String description,
            final PhenomenonEntry base, final Collection<PhenomenonEntry> component) {
        super(id, name, description);
        this.base = base;
        this.component = component;
        this.dimension = component.size();
        
    }
    
    /**
     * Retourne le phenomene de base.
     */
    @Override
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
    @Override
    public Collection<PhenomenonEntry> getComponent() {
        return component;
    }

    /**
     * Retourne le nombre de composant.
     */
    @Override
    public int getDimension() {
        return dimension;
    }
    
    /**
     * Retourne un code représentant ce phenomene composé.
     */
    @Override
    public final int hashCode() {
        return getId().hashCode();
    }

    /**
     * Vérifie si cette entré est identique à l'objet spécifié.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        final CompositePhenomenonEntry that = (CompositePhenomenonEntry) object;
        if ((this.component !=null && that.component == null)||(this.component ==null && that.component != null))
            return false;
        
        if (this.component !=null && that.component != null && this.component.size() != that.component.size())
            return false;
        
        if (this.component !=null) {
            Iterator<PhenomenonEntry> i = component.iterator();
            while (i.hasNext()) {
                if (!that.component.contains(i.next()))
                    return false;
            }
        }
        return Utilities.equals(this.getId(),             that.getId()) &&
               Utilities.equals(this.getDescription(),    that.getDescription()) &&
               Utilities.equals(this.getPhenomenonName(), that.getPhenomenonName()) &&
               Utilities.equals(this.base,                that.base) &&
               Utilities.equals(this.dimension,           that.dimension) ;
        
    }
   
    /**
     * Retourne une chaine de charactere representant la station.
     */
    @Override
    public String toString() { 
        StringBuilder s = new StringBuilder(super.toString() + '\n');
        if( base != null) {
            s.append("base: ").append(base.toString()).append('\n');
        } else {
            s.append("base is null (relatively normal)");
        }
        
        s.append("dimension:").append(dimension).append('\n');
        
        if (component != null) {
            Iterator i =  component.iterator();
            s.append("components :").append('\n');
            while (i.hasNext()) {
                s.append(i.next().toString()).append('\n');
            }
        } else {
             s.append("COMPONENT IS NULL");
        }
        return s.toString();
    }    
    
}
