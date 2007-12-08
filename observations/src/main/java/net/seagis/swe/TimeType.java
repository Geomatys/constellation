package net.seagis.swe;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;
import org.geotools.resources.Utilities;



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
    protected List<String> value = new ArrayList<String>();
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
     */
    public List<String> getValue() {
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
    
    /**
     * Vérifie si cette entré est identique à l'objet spécifié.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
        final TimeType that = (TimeType) object;
        return Utilities.equals(this.localFrame,     that.localFrame)     &&
               Utilities.equals(this.referenceFrame, that.referenceFrame) &&
               Utilities.equals(this.referenceTime,  that.referenceTime)  &&
               Utilities.equals(this.uom,            that.uom)            &&
               Utilities.equals(this.value,          that.value);
        } 
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 61 * hash + (this.uom != null ? this.uom.hashCode() : 0);
        hash = 61 * hash + (this.value != null ? this.value.hashCode() : 0);
        hash = 61 * hash + (this.localFrame != null ? this.localFrame.hashCode() : 0);
        hash = 61 * hash + (this.referenceFrame != null ? this.referenceFrame.hashCode() : 0);
        hash = 61 * hash + (this.referenceTime != null ? this.referenceTime.hashCode() : 0);
        return hash;
    }
    
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("[TimeType]").append('\n').append("super:").append(super.toString()).append('\n');
        s.append("localFrame:").append(localFrame).append('\n');
        s.append("referenceFrame:").append(referenceFrame).append('\n');
        s.append("referenceTime:").append(referenceTime).append('\n');
        s.append("value:").append('\n');
        for (String ss:value){
            s.append(ss).append('\n');
        }
        if (uom != null) {
            s.append("uom: ").append(uom.toString());
        }
        return s.toString();
    }
    
}
