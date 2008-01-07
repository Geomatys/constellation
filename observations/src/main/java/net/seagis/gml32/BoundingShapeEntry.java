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

package net.seagis.gml32;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;
import org.geotools.resources.Utilities;


/**
 * <p>Java class for BoundingShapeType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BoundingShapeType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element ref="{http://www.opengis.net/gml/3.2}Envelope"/>
 *           &lt;element ref="{http://www.opengis.net/gml/3.2}Null"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *       &lt;attribute name="nilReason" type="{http://www.opengis.net/gml/3.2}NilReasonType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BoundingShapeType", propOrder = {
    "envelope",
    "envelopeWithTimePeriod",
    "_null"
})
public class BoundingShapeEntry {

    @XmlElement(name = "Envelope", nillable = true)
    protected EnvelopeEntry envelope;
    @XmlElement(name = "EnvelopeWithTimePeriod", nillable = true)
    protected EnvelopeWithTimePeriodType envelopeWithTimePeriod;
    @XmlList
    @XmlElement(name = "Null")
    protected List<String> _null;
    @XmlAttribute
    protected List<String> nilReason;

    public BoundingShapeEntry() {}
    
    public BoundingShapeEntry(EnvelopeEntry envelope) {
        this.envelope = envelope;
    }
    /**
     * Gets the value of the envelope property.
     * 
     * @return
     *     possible object is
     *     {@link EnvelopeType }
     *     
     */
    public EnvelopeEntry getEnvelope() {
        return envelope;
    }

    /**
     * Sets the value of the envelope property.
     * 
     * @param value
     *     allowed object is
     *     {@link EnvelopeType }
     *     
     */
    public void setEnvelope(EnvelopeEntry value) {
        this.envelope = value;
    }

    /**
     * Gets the value of the envelopeWithTimePeriod property.
     * 
     * @return
     *     possible object is
     *     {@link EnvelopeWithTimePeriodType }
     *     
     */
    public EnvelopeWithTimePeriodType getEnvelopeWithTimePeriod() {
        return envelopeWithTimePeriod;
    }

    /**
     * Sets the value of the envelopeWithTimePeriod property.
     * 
     * @param value
     *     allowed object is
     *     {@link EnvelopeWithTimePeriodType }
     *     
     */
    public void setEnvelopeWithTimePeriod(EnvelopeWithTimePeriodType value) {
        this.envelopeWithTimePeriod = value;
    }

    /**
     * Gets the value of the null property.
     * 
     */
    public List<String> getNull() {
        if (_null == null) {
            _null = new ArrayList<String>();
        }
        return this._null;
    }

    /**
     * Gets the value of the nilReason property.
     * 
     */
    public List<String> getNilReason() {
        if (nilReason == null) {
            nilReason = new ArrayList<String>();
        }
        return this.nilReason;
    }
    
     /**
     * Verifie si cette entree est identique l'objet specifie.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        final BoundingShapeEntry that = (BoundingShapeEntry) object;

        return Utilities.equals(this._null,                  that._null)                  &&
               Utilities.equals(this.envelope,               that.envelope)               &&
               Utilities.equals(this.envelopeWithTimePeriod, that.envelopeWithTimePeriod) &&
               Utilities.equals(this.nilReason,              that.nilReason);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + (this.envelope != null ? this.envelope.hashCode() : 0);
        hash = 47 * hash + (this.envelopeWithTimePeriod != null ? this.envelopeWithTimePeriod.hashCode() : 0);
        hash = 47 * hash + (this._null != null ? this._null.hashCode() : 0);
        hash = 47 * hash + (this.nilReason != null ? this.nilReason.hashCode() : 0);
        return hash;
    }

}
