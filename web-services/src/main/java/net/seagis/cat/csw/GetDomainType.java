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


package net.seagis.cat.csw;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * Requests the actual values of some specified request parameter 
 *         or other data element.
 * 
 * <p>Java class for GetDomainType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetDomainType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/cat/csw/2.0.2}RequestBaseType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element name="PropertyName" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
 *           &lt;element name="ParameterName" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetDomainType", propOrder = {
    "propertyName",
    "parameterName"
})
public class GetDomainType extends RequestBaseType {

    @XmlElement(name = "PropertyName")
    @XmlSchemaType(name = "anyURI")
    private String propertyName;
    @XmlElement(name = "ParameterName")
    @XmlSchemaType(name = "anyURI")
    private String parameterName;

    /**
     * An empty constructor used by JAXB
     */
    GetDomainType() {
        
    }
    
    public GetDomainType(String service, String version, String propertyName) {
        super(service, version);
        this.propertyName = propertyName;
    }
    
    /**
     * Gets the value of the propertyName property.
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * Gets the value of the parameterName property.
     */
    public String getParameterName() {
        return parameterName;
    }
}
