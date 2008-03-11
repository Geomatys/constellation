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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import net.seagis.ogc.SortByType;


/**
 * Specifies a query to execute against instances of one or
 *          more object types. A set of ElementName elements may be included 
 *          to specify an adhoc view of the csw:Record instances in the result 
 *          set. Otherwise, use ElementSetName to specify a predefined view. 
 *          The Constraint element contains a query filter expressed in a 
 *          supported query language. A sorting criterion that specifies a 
 *          property to sort by may be included.
 * 
 *          typeNames - a list of object types to query.
 * 
 * <p>Java class for QueryType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="QueryType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/cat/csw/2.0.2}AbstractQueryType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element ref="{http://www.opengis.net/cat/csw/2.0.2}ElementSetName"/>
 *           &lt;element name="ElementName" type="{http://www.w3.org/2001/XMLSchema}QName" maxOccurs="unbounded"/>
 *         &lt;/choice>
 *         &lt;element ref="{http://www.opengis.net/cat/csw/2.0.2}Constraint" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/ogc}SortBy" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="typeNames" use="required" type="{http://www.opengis.net/cat/csw/2.0.2}TypeNameListType" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QueryType", propOrder = {
    "elementSetName",
    "elementName",
    "constraint",
    "sortBy"
})
public class QueryType extends AbstractQueryType {

    @XmlElement(name = "ElementSetName", defaultValue = "summary")
    private ElementSetNameType elementSetName;
    @XmlElement(name = "ElementName")
    private List<QName> elementName;
    @XmlElement(name = "Constraint")
    private QueryConstraintType constraint;
    @XmlElement(name = "SortBy", namespace = "http://www.opengis.net/ogc")
    private SortByType sortBy;
    @XmlAttribute(required = true)
    private List<QName> typeNames;

    /**
     * Gets the value of the elementSetName property.
     */
    public ElementSetNameType getElementSetName() {
        return elementSetName;
    }

    /**
     * Gets the value of the elementName property.
     * (unmodifiable)
     */
    public List<QName> getElementName() {
        if (elementName == null) {
            elementName = new ArrayList<QName>();
        }
        return Collections.unmodifiableList(elementName);
    }

    /**
     * Gets the value of the constraint property.
     */
    public QueryConstraintType getConstraint() {
        return constraint;
    }

    /**
     * Gets the value of the sortBy property.
     */
    public SortByType getSortBy() {
        return sortBy;
    }

    /**
     * Gets the value of the typeNames property.
     * (unmodifiable)
     */
    public List<QName> getTypeNames() {
        if (typeNames == null) {
            typeNames = new ArrayList<QName>();
        }
        return Collections.unmodifiableList(typeNames);
    }

}
