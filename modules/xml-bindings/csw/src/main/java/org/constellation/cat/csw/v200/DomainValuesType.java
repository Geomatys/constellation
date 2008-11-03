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
package org.constellation.cat.csw.v200;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;


/**
 * <p>Java class for DomainValuesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DomainValuesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element name="PropertyName" type="{http://www.w3.org/2001/XMLSchema}QName"/>
 *           &lt;element name="ParameterName" type="{http://www.w3.org/2001/XMLSchema}QName"/>
 *         &lt;/choice>
 *         &lt;choice minOccurs="0">
 *           &lt;element name="ListOfValues" type="{http://www.opengis.net/cat/csw}ListOfValuesType"/>
 *           &lt;element name="ConceptualScheme" type="{http://www.opengis.net/cat/csw}ConceptualSchemeType"/>
 *           &lt;element name="RangeOfValues" type="{http://www.opengis.net/cat/csw}RangeOfValuesType"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *       &lt;attribute name="type" use="required" type="{http://www.w3.org/2001/XMLSchema}QName" />
 *       &lt;attribute name="uom" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DomainValuesType", propOrder = {
    "propertyName",
    "parameterName",
    "listOfValues",
    "conceptualScheme",
    "rangeOfValues"
})
public class DomainValuesType {

    @XmlElement(name = "PropertyName")
    private QName propertyName;
    @XmlElement(name = "ParameterName")
    private QName parameterName;
    @XmlElement(name = "ListOfValues")
    private ListOfValuesType listOfValues;
    @XmlElement(name = "ConceptualScheme")
    private ConceptualSchemeType conceptualScheme;
    @XmlElement(name = "RangeOfValues")
    private RangeOfValuesType rangeOfValues;
    @XmlAttribute(required = true)
    private QName type;
    @XmlAttribute
    @XmlSchemaType(name = "anyURI")
    private String uom;

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

    /**
     * Gets the value of the listOfValues property.
     * 
     */
    public ListOfValuesType getListOfValues() {
        return listOfValues;
    }

    /**
     * Sets the value of the listOfValues property.
     * 
     */
    public void setListOfValues(ListOfValuesType value) {
        this.listOfValues = value;
    }

    /**
     * Gets the value of the conceptualScheme property.
     * 
     */
    public ConceptualSchemeType getConceptualScheme() {
        return conceptualScheme;
    }

    /**
     * Sets the value of the conceptualScheme property.
     * 
     */
    public void setConceptualScheme(ConceptualSchemeType value) {
        this.conceptualScheme = value;
    }

    /**
     * Gets the value of the rangeOfValues property.
     * 
     */
    public RangeOfValuesType getRangeOfValues() {
        return rangeOfValues;
    }

    /**
     * Sets the value of the rangeOfValues property.
     * 
     */
    public void setRangeOfValues(RangeOfValuesType value) {
        this.rangeOfValues = value;
    }

    /**
     * Gets the value of the type property.
     * 
     */
    public QName getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     */
    public void setType(QName value) {
        this.type = value;
    }

    /**
     * Gets the value of the uom property.
     * 
     */
    public String getUom() {
        return uom;
    }

    /**
     * Sets the value of the uom property.
     * 
     */
    public void setUom(String value) {
        this.uom = value;
    }

}
