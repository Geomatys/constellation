package net.seagis.se;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ColorReplacementType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ColorReplacementType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/se}Recode"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ColorReplacementType", propOrder = {
    "recode"
})
public class ColorReplacementType {

    @XmlElement(name = "Recode", required = true)
    protected RecodeType recode;

    /**
     * Gets the value of the recode property.
     * 
     * @return
     *     possible object is
     *     {@link RecodeType }
     *     
     */
    public RecodeType getRecode() {
        return recode;
    }

    /**
     * Sets the value of the recode property.
     * 
     * @param value
     *     allowed object is
     *     {@link RecodeType }
     *     
     */
    public void setRecode(RecodeType value) {
        this.recode = value;
    }

}
