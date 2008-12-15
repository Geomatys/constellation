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

package org.constellation.swe.v100;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/swe/1.0}AbstractDataComponentType">
 *       &lt;sequence>
 *         &lt;element name="constraint" type="{http://www.opengis.net/swe/1.0}AllowedValuesPropertyType" minOccurs="0"/>
 *         &lt;element name="quality" type="{http://www.opengis.net/swe/1.0}QualityPropertyType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="value" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://www.opengis.net/swe/1.0}SimpleComponentAttributeGroup"/>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "constraint",
    "quality",
    "value"
})
@XmlRootElement(name = "Count")
public class Count extends AbstractDataComponentType {

    private AllowedValuesPropertyType constraint;
    private List<QualityPropertyType> quality;
    private BigInteger value;
    @XmlAttribute
    @XmlSchemaType(name = "anyURI")
    private String referenceFrame;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    private String axisID;

    /**
     * Gets the value of the constraint property.
     * 
     */
    public AllowedValuesPropertyType getConstraint() {
        return constraint;
    }

    /**
     * Sets the value of the constraint property.
     */
    public void setConstraint(AllowedValuesPropertyType value) {
        this.constraint = value;
    }

    /**
     * Gets the value of the quality property.
     */
    public List<QualityPropertyType> getQuality() {
        if (quality == null) {
            quality = new ArrayList<QualityPropertyType>();
        }
        return this.quality;
    }

    /**
     * Gets the value of the value property.
     */
    public BigInteger getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     */
    public void setValue(BigInteger value) {
        this.value = value;
    }

    /**
     * Gets the value of the referenceFrame property.
     */
    public String getReferenceFrame() {
        return referenceFrame;
    }

    /**
     * Sets the value of the referenceFrame property.
     * 
     */
    public void setReferenceFrame(String value) {
        this.referenceFrame = value;
    }

    /**
     * Gets the value of the axisID property.
     */
    public String getAxisID() {
        return axisID;
    }

    /**
     * Sets the value of the axisID property.
     */
    public void setAxisID(String value) {
        this.axisID = value;
    }

}
