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
 *           &lt;element name="PropertyName" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
 *           &lt;element name="ParameterName" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
 *         &lt;/choice>
 *         &lt;choice minOccurs="0">
 *           &lt;element name="ListOfValues" type="{http://www.opengis.net/cat/csw/2.0.2}ListOfValuesType"/>
 *           &lt;element name="ConceptualScheme" type="{http://www.opengis.net/cat/csw/2.0.2}ConceptualSchemeType"/>
 *           &lt;element name="RangeOfValues" type="{http://www.opengis.net/cat/csw/2.0.2}RangeOfValuesType"/>
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
    @XmlSchemaType(name = "anyURI")
    private String propertyName;
    @XmlElement(name = "ParameterName")
    @XmlSchemaType(name = "anyURI")
    private String parameterName;
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

    /**
     * Gets the value of the listOfValues property.
     */
    public ListOfValuesType getListOfValues() {
        return listOfValues;
    }

    /**
     * Gets the value of the conceptualScheme property.
     */
    public ConceptualSchemeType getConceptualScheme() {
        return conceptualScheme;
    }

    /**
     * Gets the value of the rangeOfValues property.
     */
    public RangeOfValuesType getRangeOfValues() {
        return rangeOfValues;
    }

    /**
     * Gets the value of the type property.
     */
    public QName getType() {
        return type;
    }

    /**
     * Gets the value of the uom property.
     */
    public String getUom() {
        return uom;
    }
}
