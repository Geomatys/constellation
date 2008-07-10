/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2008, Geomatys
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


package net.seagis.gml.v311;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * A contextually local coordinate reference system; which can be divided into two broad categories:
 * - earth-fixed systems applied to engineering activities on or near the surface of the earth;
 * - CRSs on moving platforms such as road vehicles, vessels, aircraft, or spacecraft.
 * For further information, see OGC Abstract Specification Topic 2. 
 * 
 * <p>Java class for EngineeringCRSType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EngineeringCRSType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/gml}AbstractReferenceSystemType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/gml}usesCS"/>
 *         &lt;element ref="{http://www.opengis.net/gml}usesEngineeringDatum"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EngineeringCRSType", propOrder = {
    "usesCS",
    "usesEngineeringDatum"
})
public class EngineeringCRSType extends AbstractReferenceSystemType {

    @XmlElement(required = true)
    private CoordinateSystemRefType usesCS;
    @XmlElement(required = true)
    private EngineeringDatumRefType usesEngineeringDatum;

    /**
     * Gets the value of the usesCS property.
     * 
     */
    public CoordinateSystemRefType getUsesCS() {
        return usesCS;
    }

    /**
     * Sets the value of the usesCS property.
     * 
     */
    public void setUsesCS(CoordinateSystemRefType value) {
        this.usesCS = value;
    }

    /**
     * Gets the value of the usesEngineeringDatum property.
     * 
    */
    public EngineeringDatumRefType getUsesEngineeringDatum() {
        return usesEngineeringDatum;
    }

    /**
     * Sets the value of the usesEngineeringDatum property.
     * 
     */
    public void setUsesEngineeringDatum(EngineeringDatumRefType value) {
        this.usesEngineeringDatum = value;
    }

}
