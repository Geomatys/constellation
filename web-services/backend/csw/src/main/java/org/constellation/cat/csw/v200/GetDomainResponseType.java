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
package net.seagis.cat.csw.v200;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * Returns the actual values for some property. 
 * In general this is a subset of the value domain (that is, set of permissible values),
 * although in some cases these may coincide. 
 * Multiple value ranges may be returned if the property can assume values from multiple value domains (e.g. multiple taxonomies).
 *          
 * 
 * <p>Java class for GetDomainResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetDomainResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="DomainValues" type="{http://www.opengis.net/cat/csw}DomainValuesType" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetDomainResponseType", propOrder = {
    "domainValues"
})
public class GetDomainResponseType {

    @XmlElement(name = "DomainValues", required = true)
    private List<DomainValuesType> domainValues;

    /**
     * Gets the value of the domainValues property.
     * 
     */
    public List<DomainValuesType> getDomainValues() {
        if (domainValues == null) {
            domainValues = new ArrayList<DomainValuesType>();
        }
        return this.domainValues;
    }

}
