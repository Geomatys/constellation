//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.0 in JDK 1.6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2007.09.13 at 04:28:10 PM CEST 
//


package net.seagis.ogc;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Existence_CapabilitiesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Existence_CapabilitiesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ExistenceOperators" type="{http://www.opengis.net/ogc}ExistenceOperatorsType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Existence_CapabilitiesType", propOrder = {
    "existenceOperators"
})
public class ExistenceCapabilitiesType {

    @XmlElement(name = "ExistenceOperators")
    protected ExistenceOperatorsType existenceOperators;

    /**
     * Gets the value of the existenceOperators property.
     * 
     * @return
     *     possible object is
     *     {@link ExistenceOperatorsType }
     *     
     */
    public ExistenceOperatorsType getExistenceOperators() {
        return existenceOperators;
    }

    /**
     * Sets the value of the existenceOperators property.
     * 
     * @param value
     *     allowed object is
     *     {@link ExistenceOperatorsType }
     *     
     */
    public void setExistenceOperators(ExistenceOperatorsType value) {
        this.existenceOperators = value;
    }

}
