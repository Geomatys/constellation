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
package org.constellation.gml.v311;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * Definition of a coordinate system axis. 
 * 
 * <p>Java class for CoordinateSystemAxisType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CoordinateSystemAxisType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/gml}CoordinateSystemAxisBaseType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/gml}axisID" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/gml}remarks" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/gml}axisAbbrev"/>
 *         &lt;element ref="{http://www.opengis.net/gml}axisDirection"/>
 *       &lt;/sequence>
 *       &lt;attribute ref="{http://www.opengis.net/gml}uom use="required""/>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CoordinateSystemAxisType", propOrder = {
    "axisID",
    "axisAbbrev",
    "axisDirection"
})
public class CoordinateSystemAxisType extends CoordinateSystemAxisBaseType {

    private List<IdentifierType> axisID;
    @XmlElement(required = true)
    private CodeType axisAbbrev;
    @XmlElement(required = true)
    private CodeType axisDirection;
    @XmlAttribute(namespace = "http://www.opengis.net/gml", required = true)
    @XmlSchemaType(name = "anyURI")
    private String uom;

    /**
     * Set of alternative identifications of this coordinate system axis. 
     * The first axisID, if any, is normally the primary identification code, 
     * and any others are aliases. 
     * Gets the value of the axisID property.
     * 
     * @return An unmodifiable list of the axis identifier.
     */
    public List<IdentifierType> getAxisID() {
        if (axisID == null) {
            axisID = new ArrayList<IdentifierType>();
        }
        return Collections.unmodifiableList(axisID);
    }

    /**
     * Gets the value of the axisAbbrev property.
     */
    public CodeType getAxisAbbrev() {
        return axisAbbrev;
    }

    /**
     * Gets the value of the axisDirection property.
     * 
     */
    public CodeType getAxisDirection() {
        return axisDirection;
    }

    /**
     * Gets the value of the uom property.
     * 
     */
    public String getUom() {
        return uom;
    }
 }
