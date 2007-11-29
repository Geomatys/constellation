package net.seagis.swe;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import net.seagis.gml.BaseUnitType;
import net.seagis.gml.UnitDefinitionType;

/**
 * <p>Java class for UomPropertyType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UomPropertyType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element ref="{http://www.opengis.net/gml/3.2}UnitDefinition"/>
 *         &lt;element ref="{http://www.opengis.net/gml/3.2}BaseUnit"/>
 *       &lt;/choice>
 *       &lt;attribute name="code" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="nilReason">
 *         &lt;simpleType>
 *           &lt;list itemType="{http://www.w3.org/2001/XMLSchema}string" />
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute ref="{http://www.opengis.net/gml/3.2}remoteSchema"/>
 *       &lt;attribute ref="{http://www.w3.org/1999/xlink}actuate"/>
 *       &lt;attribute ref="{http://www.w3.org/1999/xlink}arcrole"/>
 *       &lt;attribute ref="{http://www.w3.org/1999/xlink}href"/>
 *       &lt;attribute ref="{http://www.w3.org/1999/xlink}role"/>
 *       &lt;attribute ref="{http://www.w3.org/1999/xlink}show"/>
 *       &lt;attribute ref="{http://www.w3.org/1999/xlink}title"/>
 *       &lt;attribute ref="{http://www.w3.org/1999/xlink}type"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UomPropertyType", propOrder = {
    "unitDefinition",
    "baseUnit"
})
public class UomPropertyType {

    @XmlElement(name = "UnitDefinition", namespace = "http://www.opengis.net/gml/3.2", nillable = true)
    protected UnitDefinitionType unitDefinition;
    @XmlElement(name = "BaseUnit", namespace = "http://www.opengis.net/gml/3.2", nillable = true)
    protected BaseUnitType baseUnit;
    @XmlAttribute
    protected String code;
    @XmlAttribute
    protected List<String> nilReason;
    @XmlAttribute(namespace = "http://www.opengis.net/gml/3.2")
    protected String remoteSchema;
    @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
    protected String actuate;
    @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
    protected String arcrole;
    @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
    protected String href;
    @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
    protected String role;
    @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
    protected String show;
    @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
    protected String title;
    @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
    protected String type;

    
    public UomPropertyType() {}
    
    public UomPropertyType(String code, String ref) {
        this.code = code;
        this.href = href;
    }
    
    /**
     * Gets the value of the unitDefinition property.
     * 
     * @return
     *     possible object is
     *     {@link UnitDefinitionType }
     *     
     */
    public UnitDefinitionType getUnitDefinition() {
        return unitDefinition;
    }

    /**
     * Sets the value of the unitDefinition property.
     * 
     * @param value
     *     allowed object is
     *     {@link UnitDefinitionType }
     *     
     */
    public void setUnitDefinition(UnitDefinitionType value) {
        this.unitDefinition = value;
    }

    /**
     * Gets the value of the baseUnit property.
     * 
     * @return
     *     possible object is
     *     {@link BaseUnitType }
     *     
     */
    public BaseUnitType getBaseUnit() {
        return baseUnit;
    }

    /**
     * Sets the value of the baseUnit property.
     * 
     * @param value
     *     allowed object is
     *     {@link BaseUnitType }
     *     
     */
    public void setBaseUnit(BaseUnitType value) {
        this.baseUnit = value;
    }

    /**
     * Gets the value of the code property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCode() {
        return code;
    }

    /**
     * Sets the value of the code property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCode(String value) {
        this.code = value;
    }

    /**
     * Gets the value of the nilReason property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the nilReason property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNilReason().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getNilReason() {
        if (nilReason == null) {
            nilReason = new ArrayList<String>();
        }
        return this.nilReason;
    }

    /**
     * Gets the value of the remoteSchema property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRemoteSchema() {
        return remoteSchema;
    }

    /**
     * Sets the value of the remoteSchema property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRemoteSchema(String value) {
        this.remoteSchema = value;
    }

    /**
     * Gets the value of the actuate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getActuate() {
        return actuate;
    }

    /**
     * Sets the value of the actuate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setActuate(String value) {
        this.actuate = value;
    }

    /**
     * Gets the value of the arcrole property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getArcrole() {
        return arcrole;
    }

    /**
     * Sets the value of the arcrole property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setArcrole(String value) {
        this.arcrole = value;
    }

    /**
     * Gets the value of the href property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHref() {
        return href;
    }

    /**
     * Sets the value of the href property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHref(String value) {
        this.href = value;
    }

    /**
     * Gets the value of the role property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the value of the role property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRole(String value) {
        this.role = value;
    }

    /**
     * Gets the value of the show property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShow() {
        return show;
    }

    /**
     * Sets the value of the show property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShow(String value) {
        this.show = value;
    }

    /**
     * Gets the value of the title property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTitle(String value) {
        this.title = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

}
