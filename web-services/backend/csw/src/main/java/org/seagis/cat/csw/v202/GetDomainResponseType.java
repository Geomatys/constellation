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
package net.seagis.cat.csw.v202;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Returns the actual values for some property. In general this is a
 *          subset of the value domain (that is, set of permissible values),
 *          although in some cases these may be the same.
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
 *         &lt;element name="DomainValues" type="{http://www.opengis.net/cat/csw/2.0.2}DomainValuesType" maxOccurs="unbounded"/>
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
@XmlRootElement(name="GetDomainResponse")
public class GetDomainResponseType {

    @XmlElement(name = "DomainValues", required = true)
    private List<DomainValuesType> domainValues;

    /**
     * An empty constructor used by JAXB
     */
    GetDomainResponseType() {
        
    }
    
    /**
     * build a new respon to a getDomain request
     */
    public GetDomainResponseType(List<DomainValuesType> domainValues) {
        this.domainValues = domainValues;
    }
    
    /**
     * Gets the value of the domainValues property.
     * (unmodifiable)
     */
    public List<DomainValuesType> getDomainValues() {
        if (domainValues == null) {
            domainValues = new ArrayList<DomainValuesType>();
        }
        return this.domainValues;
    }

}
