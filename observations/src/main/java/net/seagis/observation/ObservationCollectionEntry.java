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

package net.seagis.observation;

import java.util.ArrayList;
import java.util.Collection;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import net.seagis.catalog.Entry;
import org.geotools.resources.Utilities;
import org.opengis.observation.ObservationCollection;

/**
 *
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ObservationCollection")
@XmlRootElement(name = "ObservationCollection")
public class ObservationCollectionEntry extends Entry implements ObservationCollection {

    /**
     *  The observation collection
     */
    @XmlElement(name="member", namespace="http://www.opengis.net/om/1.0")
    private Collection<ObservationEntry> member = new ArrayList<ObservationEntry>();
    
    /**
     * A JAXB constructor. 
     */
    public ObservationCollectionEntry() {}
    
    /**
     * Build a new Collection of Observation
     */
    public ObservationCollectionEntry(Collection<ObservationEntry> member) {
        this.member = member;
    }
    
    /**
     * override the getName() method of Entry 
     */
    @Override
    public String getName() {
        return this.name;
    }
    
    /**
     * Add a new Observation to the collection. 
     */
    public void add(ObservationEntry observation) {
        this.member.add(observation);
    }
    
    /**
     * Return a collection of Observation
     */
    @Override
    public Collection<ObservationEntry> getMember() {
        return this.member;
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
            final ObservationCollectionEntry that = (ObservationCollectionEntry) object;
            return Utilities.equals(this.member,   that.member);
        } 
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + (this.member != null ? this.member.hashCode() : 0);
        return hash;
    }
    
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("Observation Collection:").append('\n');
        s.append("super:").append(super.toString());
        int i = 1;
        for (ObservationEntry obs:member) {
            s.append("observation n" + i + ":").append('\n').append(obs.toString());
            i++;
        }
        return s.toString();
    }
    

}
