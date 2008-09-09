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
package net.seagis.ows.v100;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * Human-readable descriptive information for the object it is included within.
 * This type shall be extended if needed for specific OWS use to include additional metadata for each type of information. This type shall not be restricted for a specific OWS to change the multiplicity (or optionality) of some elements. 
 * 
 * <p>Java class for DescriptionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DescriptionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/ows}Title" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/ows}Abstract" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/ows}Keywords" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DescriptionType", propOrder = {
    "title",
    "_abstract",
    "keywords"
})
@XmlSeeAlso({
    IdentificationType.class,
    ServiceIdentification.class
})
public class DescriptionType {

    @XmlElement(name = "Title")
    private String title;
    @XmlElement(name = "Abstract")
    private String _abstract;
    @XmlElement(name = "Keywords")
    private List<KeywordsType> keywords;

    /**
     * An empty constructor used by JAXB
     */
     DescriptionType() {
         
     }
     
     /**
     * Build a new description
     */
     public DescriptionType(String title, String _abstract,  List<KeywordsType> keywords) {
         this._abstract = _abstract;
         this.title     = title;
         this.keywords  = keywords;
     }
     
      /**
     * Build a new description
     */
     public DescriptionType(String title, String _abstract,  KeywordsType keywords) {
         this._abstract = _abstract;
         this.title     = title;
         this.keywords  = new ArrayList<KeywordsType>();
         this.keywords.add(keywords);
     }
     
    /**
     * Gets the value of the title property.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the value of the abstract property.
     */
    public String getAbstract() {
        return _abstract;
    }

    /**
     * Gets the value of the keywords property.
     * (unmodifiable)
     */
    public List<KeywordsType> getKeywords() {
        if (keywords == null) {
            keywords = new ArrayList<KeywordsType>();
        }
        return Collections.unmodifiableList(keywords);
    }

}
