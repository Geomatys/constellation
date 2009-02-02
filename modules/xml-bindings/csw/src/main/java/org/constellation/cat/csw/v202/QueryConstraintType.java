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
package org.constellation.cat.csw.v202;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.constellation.cat.csw.QueryConstraint;
import org.constellation.ogc.FilterType;
import org.geotools.util.Utilities;


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
public class QueryConstraintType implements QueryConstraint {

    @XmlElement(name = "Filter", namespace = "http://www.opengis.net/ogc")
    private FilterType filter;
    @XmlElement(name = "CqlText")
    private String cqlText;
    @XmlAttribute(required = true)
    private String version;

    /**
     * Empty constructor used by JAXB
     */
    public QueryConstraintType(){
        
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
    
    /**
     * Verify if this entry is identical to the specified object.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof QueryConstraintType) {
            final QueryConstraintType that = (QueryConstraintType) object;
            return Utilities.equals(this.cqlText,  that.cqlText)   &&
                   Utilities.equals(this.filter,  that.filter)   &&
                   Utilities.equals(this.version,  that.version);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 61 * hash + (this.filter != null ? this.filter.hashCode() : 0);
        hash = 61 * hash + (this.cqlText != null ? this.cqlText.hashCode() : 0);
        hash = 61 * hash + (this.version != null ? this.version.hashCode() : 0);
        return hash;
    }
    
     @Override
    public String toString() {
        StringBuilder s = new StringBuilder("[GetRecordsType]").append('\n');
        
        if (filter != null) {
            s.append("filter: ").append(filter).append('\n');
        }
        if (cqlText != null) {
            s.append("cqlText: ").append(cqlText).append('\n');
        }
        if (version != null) {
            s.append("version: ").append(version).append('\n');
        }
        return s.toString();
    }
}
