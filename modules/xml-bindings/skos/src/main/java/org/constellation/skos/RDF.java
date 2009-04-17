/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
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

package org.constellation.skos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.geotoolkit.util.Utilities;

/**
 *
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="RDF", namespace="http://www.w3.org/1999/02/22-rdf-syntax-ns#")
public class RDF {
    
    @XmlElement(name="Concept", namespace = "http://www.w3.org/2004/02/skos/core#")
    private List<Concept> concept;

    public RDF() {
        concept = new ArrayList<Concept>();
    }
    
    public RDF(List<Concept> concept) {
        this.concept = concept;
    }
    
    public List<Concept> getConcept() {
        if (concept == null)
            concept = new ArrayList<Concept>();
        return concept;
    }

    public void setConcept(List<Concept> concept) {
        this.concept = concept;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[RDF]:").append('\n');
        sb.append("nb concept: ").append(getConcept().size()).append('\n');
        for (Concept c : concept) {
            sb.append(c).append('\n');
        }
        return sb.toString();
    }

    public Map<String, String> getShortMap() {
        Map<String, String> map = new HashMap<String, String>();
        if (getConcept().size() != 0) {
            for (Concept c : getConcept()) {
                String id = c.getExternalID();
                id = id.substring(id.lastIndexOf(':') + 1);
                map.put(id, c.getPrefLabel());
            }
        } 
        return map;
    }

    /*
     * Verifie si cette entree est identique a l'objet specifie.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof RDF) {
            final RDF that = (RDF) object;

            return Utilities.equals(this.concept, that.concept);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + (this.concept != null ? this.concept.hashCode() : 0);
        return hash;
    }
    
}
