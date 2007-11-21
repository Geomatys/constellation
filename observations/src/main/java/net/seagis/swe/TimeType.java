package net.seagis.swe;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;



@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Time", propOrder = {
    "uom",
    "value"
})
public class TimeType extends AbstractDataComponentEntry {

    protected UomPropertyType uom;
    //protected AllowedTimesPropertyType constraint;
    //protected QualityPropertyType quality;
    @XmlList
    protected List<String> value;
    @XmlAttribute
    protected String localFrame;
    @XmlAttribute
    protected String referenceFrame;
    @XmlAttribute
    protected String referenceTime;

    /**
     * A empty contructor used by JAXB
     */
    public TimeType() {
        
    }
    
    /**
     * Build a new TimeType
     */
    public TimeType(String definition, String uomCode, String uomHref) {
        super(null, definition, false);
        this.uom = new UomPropertyType(uomCode, uomHref);
        
    }
    
    /**
     * Gets the value of the uom property.
     * 
     * @return
     *     possible object is
     *     {@link UomPropertyType }
     *     
     */
    public UomPropertyType getUom() {
        return uom;
    }

    /**
     * Sets the value of the uom property.
     * 
     * @param value
     *     allowed object is
     *     {@link UomPropertyType }
     *     
     */
    public void setUom(UomPropertyType value) {
        this.uom = value;
    }

    /**
     * Gets the value of the value property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the value property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getValue().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getValue() {
        if (value == null) {
            value = new ArrayList<String>();
        }
        return this.value;
    }

    /**
     * Gets the value of the localFrame property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocalFrame() {
        return localFrame;
    }

    /**
     * Sets the value of the localFrame property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocalFrame(String value) {
        this.localFrame = value;
    }

    /**
     * Gets the value of the referenceFrame property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReferenceFrame() {
        return referenceFrame;
    }

    /**
     * Sets the value of the referenceFrame property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReferenceFrame(String value) {
        this.referenceFrame = value;
    }

    /**
     * Gets the value of the referenceTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReferenceTime() {
        return referenceTime;
    }

    /**
     * Sets the value of the referenceTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReferenceTime(String value) {
        this.referenceTime = value;
    }
}
