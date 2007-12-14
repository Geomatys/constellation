package net.opengis.se;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for LegendGraphicType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="LegendGraphicType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/se}Graphic"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LegendGraphicType", propOrder = {
    "graphic"
})
public class LegendGraphicType {

    @XmlElement(name = "Graphic", required = true)
    protected GraphicType graphic;

    /**
     * Gets the value of the graphic property.
     * 
     * @return
     *     possible object is
     *     {@link GraphicType }
     *     
     */
    public GraphicType getGraphic() {
        return graphic;
    }

    /**
     * Sets the value of the graphic property.
     * 
     * @param value
     *     allowed object is
     *     {@link GraphicType }
     *     
     */
    public void setGraphic(GraphicType value) {
        this.graphic = value;
    }

}
