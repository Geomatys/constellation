/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 * (C) 2007, Geomatys
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

package net.seagis.cat.csw.v202;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import net.seagis.ogc.FilterType;


/**
 * A search constraint that adheres to one of the following syntaxes:
 *          Filter   - OGC filter expression
 *          CqlText  - OGC CQL predicate
 * 
 * <p>Java class for QueryConstraintType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="QueryConstraintType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element ref="{http://www.opengis.net/ogc}Filter"/>
 *         &lt;element name="CqlText" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/choice>
 *       &lt;attribute name="version" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QueryConstraintType", propOrder = {
    "filter",
    "cqlText"
})
public class QueryConstraintType {

    @XmlElement(name = "Filter", namespace = "http://www.opengis.net/ogc")
    private FilterType filter;
    @XmlElement(name = "CqlText")
    private String cqlText;
    @XmlAttribute(required = true)
    private String version;

    /**
     * Empty constructor used by JAXB
     */
    QueryConstraintType(){
        
    }
    
    /**
     * Build a new Query constraint with a filter.
     */
    public QueryConstraintType(FilterType filter, String version){
        this.filter  = filter;
        this.version = version;
    }
    
    /**
     * Build a new Query constraint with a CQL text.
     */
    public QueryConstraintType(String cqlText, String version){
        this.cqlText = cqlText;
        this.version = version;
    }
    
    
    /**
     * Gets the value of the filter property.
     */
    public FilterType getFilter() {
        return filter;
    }

    /**
     * Gets the value of the cqlText property.
     */
    public String getCqlText() {
        return cqlText;
    }

    /**
     * Gets the value of the version property.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the value of the filter property.
     * 
     */
    public void setFilter(FilterType value) {
        this.filter = value;
    }

    /**
     * Sets the value of the cqlText property.
     * 
     */
    public void setCqlText(String value) {
        this.cqlText = value;
    }

    /**
     * Sets the value of the version property.
     * 
     */
    public void setVersion(String value) {
        this.version = value;
    }
}
