package net.seagis.sld;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import net.seagis.se.DescriptionType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/se}Name"/>
 *         &lt;element ref="{http://www.opengis.net/se}Description" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/sld}LayerFeatureConstraints" minOccurs="0"/>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element ref="{http://www.opengis.net/sld}NamedStyle"/>
 *           &lt;element ref="{http://www.opengis.net/sld}UserStyle"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "name",
    "description",
    "layerFeatureConstraints",
    "namedStyleOrUserStyle"
})
@XmlRootElement(name = "NamedLayer")
public class NamedLayer {

    @XmlElement(name = "Name", namespace = "http://www.opengis.net/se", required = true)
    protected String name;
    @XmlElement(name = "Description", namespace = "http://www.opengis.net/se")
    protected DescriptionType description;
    @XmlElement(name = "LayerFeatureConstraints")
   // protected LayerFeatureConstraints layerFeatureConstraints;
    @XmlElements({
        @XmlElement(name = "UserStyle", type = UserStyle.class),
        @XmlElement(name = "NamedStyle", type = NamedStyle.class)
    })
    protected List<Object> namedStyleOrUserStyle;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link DescriptionType }
     *     
     */
    public DescriptionType getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link DescriptionType }
     *     
     */
    public void setDescription(DescriptionType value) {
        this.description = value;
    }

    /**
     * Gets the value of the layerFeatureConstraints property.
     * 
     * @return
     *     possible object is
     *     {@link LayerFeatureConstraints }
     *     
     
   public LayerFeatureConstraints getLayerFeatureConstraints() {
        return layerFeatureConstraints;
    }

    /**
     * Sets the value of the layerFeatureConstraints property.
     * 
     * @param value
     *     allowed object is
     *     {@link LayerFeatureConstraints }
     *     
     
    public void setLayerFeatureConstraints(LayerFeatureConstraints value) {
        this.layerFeatureConstraints = value;
    }*/
    

    /**
     * Gets the value of the namedStyleOrUserStyle property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the namedStyleOrUserStyle property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNamedStyleOrUserStyle().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link UserStyle }
     * {@link NamedStyle }
     * 
     * 
     */
    public List<Object> getNamedStyleOrUserStyle() {
        if (namedStyleOrUserStyle == null) {
            namedStyleOrUserStyle = new ArrayList<Object>();
        }
        return this.namedStyleOrUserStyle;
    }

}
