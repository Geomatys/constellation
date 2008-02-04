
package net.seagis.ogc;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import net.seagis.gml32.EnvelopeEntry;
import net.seagis.gml32.EnvelopeWithTimePeriodType;


/**
 * <p>Java class for BBOXType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BBOXType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/ogc}SpatialOpsType">
 *       &lt;sequence>
 *         &lt;element name="PropertyName" type="{http://www.opengis.net/ogc}PropertyNameType" minOccurs="0"/>
 *         &lt;choice>
 *           &lt;element ref="{http://www.opengis.net/gml/3.2}Envelope"/>
 *           &lt;element ref="{http://www.opengis.net/gml/3.2}EnvelopeWithTimePeriod"/>
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
@XmlType(name = "BBOXType", propOrder = {
    "propertyName",
    "envelope",
    "envelopeWithTimePeriod"
})
public class BBOXType
    extends SpatialOpsType
{

    @XmlElement(name = "PropertyName")
    private String propertyName;
    @XmlElement(name = "Envelope", namespace = "http://www.opengis.net/gml/3.2", nillable = true)
    private EnvelopeEntry envelope;
    @XmlElement(name = "EnvelopeWithTimePeriod", namespace = "http://www.opengis.net/gml/3.2", nillable = true)
    private EnvelopeWithTimePeriodType envelopeWithTimePeriod;

    /**
     * Gets the value of the propertyName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * Sets the value of the propertyName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPropertyName(String value) {
        this.propertyName = value;
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

}
