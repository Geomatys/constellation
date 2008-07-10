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
 * A 1D coordinate reference system used for the recording of time. 
 * 
 * <p>Java class for TemporalCRSType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TemporalCRSType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/gml}AbstractReferenceSystemType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/gml}usesTemporalCS"/>
 *         &lt;element ref="{http://www.opengis.net/gml}usesTemporalDatum"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TemporalCRSType", propOrder = {
    "usesTemporalCS",
    "usesTemporalDatum"
})
public class TemporalCRSType extends AbstractReferenceSystemType {

    @XmlElement(required = true)
    private TemporalCSRefType usesTemporalCS;
    @XmlElement(required = true)
    private TemporalDatumRefType usesTemporalDatum;

    /**
     * Gets the value of the usesTemporalCS property.
     */
    public TemporalCSRefType getUsesTemporalCS() {
        return usesTemporalCS;
    }

    /**
     * Sets the value of the usesTemporalCS property.
     */
    public void setUsesTemporalCS(TemporalCSRefType value) {
        this.usesTemporalCS = value;
    }

    /**
     * Gets the value of the usesTemporalDatum property.
     */
    public TemporalDatumRefType getUsesTemporalDatum() {
        return usesTemporalDatum;
    }

    /**
     * Sets the value of the usesTemporalDatum property.
     * 
     */
    public void setUsesTemporalDatum(TemporalDatumRefType value) {
        this.usesTemporalDatum = value;
    }

}
