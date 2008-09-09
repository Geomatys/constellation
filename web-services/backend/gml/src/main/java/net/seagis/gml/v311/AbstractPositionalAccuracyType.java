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
package net.seagis.gml.v311;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * Position error estimate (or accuracy) data. 
 * 
 * <p>Java class for AbstractPositionalAccuracyType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AbstractPositionalAccuracyType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/gml}measureDescription" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractPositionalAccuracyType", propOrder = {
    "measureDescription"
})
/*@XmlSeeAlso({
    RelativeInternalPositionalAccuracyType.class,
    CovarianceMatrixType.class,
    AbsoluteExternalPositionalAccuracyType.class
})*/
public abstract class AbstractPositionalAccuracyType {

    protected CodeType measureDescription;

    /**
     * Gets the value of the measureDescription property.
     * 
     */
    public CodeType getMeasureDescription() {
        return measureDescription;
    }

    /**
     * Sets the value of the measureDescription property.
     * 
    */
    public void setMeasureDescription(CodeType value) {
        this.measureDescription = value;
    }

}
