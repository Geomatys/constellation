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



package org.constellation.metadata.fra;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.geotools.metadata.iso.citation.CitationImpl;
import org.geotools.metadata.iso.constraint.LegalConstraintsImpl;



/**
 * <p>Java class for FRA_LegalConstraints_Type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FRA_LegalConstraints_Type">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.isotc211.org/2005/gmd}MD_LegalConstraints_Type">
 *       &lt;sequence>
 *         &lt;element name="citation" type="{http://www.isotc211.org/2005/gmd}CI_Citation_PropertyType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "citation"
})
@XmlRootElement(name="FRA_LegalConstraints")        
public class FRALegalConstraintsType extends LegalConstraintsImpl {

    private List<CitationImpl> citation;

    /**
     * Gets the value of the citation property.
     */
    public List<CitationImpl> getCitation() {
        if (citation == null) {
            citation = new ArrayList<CitationImpl>();
        }
        return this.citation;
    }
    
    /**
     * Sets the value of the citation property.
     * 
     */
    public void setCitation(List<CitationImpl> citation) {
        this.citation = citation;
    }
    
    
    /**
     * Sets the value of the citation property.
     * 
     */
    public void setCitation(CitationImpl citation) {
        if (this.citation == null) {
            this.citation = new ArrayList<CitationImpl>();
        }
        this.citation.add(citation);
    }

}
