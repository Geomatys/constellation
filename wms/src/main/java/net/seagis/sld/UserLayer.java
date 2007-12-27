package net.seagis.sld;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
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
 *         &lt;element ref="{http://www.opengis.net/se}Name" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/se}Description" minOccurs="0"/>
 *         &lt;choice minOccurs="0">
 *           &lt;element ref="{http://www.opengis.net/sld}RemoteOWS"/>
 *         &lt;/choice>
 *         &lt;choice minOccurs="0">
 *           &lt;element ref="{http://www.opengis.net/sld}LayerFeatureConstraints"/>
 *           &lt;element ref="{http://www.opengis.net/sld}LayerCoverageConstraints"/>
 *         &lt;/choice>
 *         &lt;element ref="{http://www.opengis.net/sld}UserStyle" maxOccurs="unbounded"/>
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
    "remoteOWS",
    "layerFeatureConstraints",
    "layerCoverageConstraints",
    "userStyle"
})
@XmlRootElement(name = "UserLayer")
public class UserLayer {

    @XmlElement(name = "Name", namespace = "http://www.opengis.net/se")
    protected String name;
    @XmlElement(name = "Description", namespace = "http://www.opengis.net/se")
    protected DescriptionType description;
    @XmlElement(name = "RemoteOWS")
    protected RemoteOWS remoteOWS;
    @XmlElement(name = "LayerCoverageConstraints")
    protected LayerCoverageConstraints layerCoverageConstraints;
    @XmlElement(name = "UserStyle", required = true)
    protected List<UserStyle> userStyle;

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
     * Gets the value of the remoteOWS property.
     * 
     * @return
     *     possible object is
     *     {@link RemoteOWS }
     *     
     */
    public RemoteOWS getRemoteOWS() {
        return remoteOWS;
    }

    /**
     * Sets the value of the remoteOWS property.
     * 
     * @param value
     *     allowed object is
     *     {@link RemoteOWS }
     *     
     */
    public void setRemoteOWS(RemoteOWS value) {
        this.remoteOWS = value;
    }

    /**
     * Gets the value of the layerCoverageConstraints property.
     * 
     * @return
     *     possible object is
     *     {@link LayerCoverageConstraints }
     *     
     */
    public LayerCoverageConstraints getLayerCoverageConstraints() {
        return layerCoverageConstraints;
    }

    /**
     * Sets the value of the layerCoverageConstraints property.
     * 
     * @param value
     *     allowed object is
     *     {@link LayerCoverageConstraints }
     *     
     */
    public void setLayerCoverageConstraints(LayerCoverageConstraints value) {
        this.layerCoverageConstraints = value;
    }

    /**
     * Gets the value of the userStyle property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the userStyle property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUserStyle().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link UserStyle }
     * 
     * 
     */
    public List<UserStyle> getUserStyle() {
        if (userStyle == null) {
            userStyle = new ArrayList<UserStyle>();
        }
        return this.userStyle;
    }

}
