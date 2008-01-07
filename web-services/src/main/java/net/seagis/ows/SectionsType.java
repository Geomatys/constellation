/*
 * Sicade - SystÃ¨mes intÃ©grÃ©s de connaissances pour l'aide Ã  la dÃ©cision en environnement
 * (C) 2005, Institut de Recherche pour le DÃ©veloppement
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


package net.seagis.ows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.geotools.resources.Utilities;


/**
 * Unordered list of zero or more names of requested sections in complete service metadata document. Each Section value shall contain an allowed section name as specified by each OWS specification. See Sections parameter subclause for more information.  
 * 
 * <p>Java class for SectionsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SectionsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Section" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SectionsType", propOrder = {
    "section"
})
public class SectionsType {

    @XmlElement(name = "Section")
    private List<String> section = new ArrayList<String>();

    /**
     * Empty constructor used bye JAXB.
     */
    SectionsType(){
    }
    
    /**
     * Build a new list of Section.
     */
    public SectionsType(List<String> section){
        this.section = section;
    }
    
    /**
     * Gets the value of the section property.
     */
    public List<String> getSection() {
       return Collections.unmodifiableList(section);
    }
    
     /**
     * Add a new section to the list.
     * 
     * @param section a new section.
     */
    public void add(String section) {
        this.section.add(section);
    }
    
    /**
     * Verify that this entry is identical to the specified object.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        final SectionsType that = (SectionsType) object;
        return Utilities.equals(this.section, that.section);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + (this.section != null ? this.section.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("Sections:").append('\n');
        for (String sec:section) {
            s.append(sec).append('\n');
        }
        return s.toString();
    }
}
