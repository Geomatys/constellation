package net.seagis.se;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for FillType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FillType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/se}GraphicFill" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/se}SvgParameter" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FillType", propOrder = {
    "graphicFill",
    "svgParameter"
})
public class FillType {

    @XmlElement(name = "GraphicFill")
    protected GraphicFillType graphicFill;
    @XmlElement(name = "SvgParameter")
    protected List<SvgParameterType> svgParameter;

    /**
     * Gets the value of the graphicFill property.
     * 
     * @return
     *     possible object is
     *     {@link GraphicFillType }
     *     
     */
    public GraphicFillType getGraphicFill() {
        return graphicFill;
    }

    /**
     * Sets the value of the graphicFill property.
     * 
     * @param value
     *     allowed object is
     *     {@link GraphicFillType }
     *     
     */
    public void setGraphicFill(GraphicFillType value) {
        this.graphicFill = value;
    }

    /**
     * Gets the value of the svgParameter property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the svgParameter property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSvgParameter().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SvgParameterType }
     * 
     * 
     */
    public List<SvgParameterType> getSvgParameter() {
        if (svgParameter == null) {
            svgParameter = new ArrayList<SvgParameterType>();
        }
        return this.svgParameter;
    }

}
