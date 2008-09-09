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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;


/**
 * 
 * Requests the actual values of some specified property or data element.
 *         
 * 
 * <p>Java class for GetDomainType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetDomainType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/cat/csw}RequestBaseType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element name="PropertyName" type="{http://www.w3.org/2001/XMLSchema}QName"/>
 *           &lt;element name="ParameterName" type="{http://www.w3.org/2001/XMLSchema}QName"/>
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
    private QName propertyName;
    @XmlElement(name = "ParameterName")
    private QName parameterName;

    /**
     * Gets the value of the propertyName property.
     * 
     */
    public QName getPropertyName() {
        return propertyName;
    }

    /**
     * Sets the value of the propertyName property.
     * 
     */
    public void setPropertyName(QName value) {
        this.propertyName = value;
    }

    /**
     * Gets the value of the parameterName property.
     * 
     */
    public QName getParameterName() {
        return parameterName;
    }

    /**
     * Sets the value of the parameterName property.
     * 
     */
    public void setParameterName(QName value) {
        this.parameterName = value;
    }

}
