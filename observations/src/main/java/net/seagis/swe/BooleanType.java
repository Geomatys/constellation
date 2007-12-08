package net.seagis.swe;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.geotools.resources.Utilities;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Boolean", propOrder = {
    "value"
})
public class BooleanType extends AbstractDataComponentEntry {

    protected java.lang.Boolean value;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String axisID;
    @XmlAttribute
    protected String referenceFrame;

    /**
     * A empty contructor used by JAXB
     */
    public BooleanType() {
        
    }
    
    /**
     * Build a new TimeType
     */
    public BooleanType(String definition, java.lang.Boolean value) {
        super(null, definition, false);
        this.value = value;
        
    }
    
    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.Boolean }
     *     
     */
    public java.lang.Boolean isValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.Boolean }
     *     
     */
    public void setValue(java.lang.Boolean value) {
        this.value = value;
    }

    /**
     * Gets the value of the axisID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAxisID() {
        return axisID;
    }

    /**
     * Sets the value of the axisID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAxisID(String value) {
        this.axisID = value;
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
     * Vérifie si cette entré est identique à l'objet spécifié.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
        final BooleanType that = (BooleanType) object;
        return Utilities.equals(this.referenceFrame, that.referenceFrame) &&
               Utilities.equals(this.axisID,         that.axisID)  &&
               Utilities.equals(this.value,          that.value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 11 * hash + (this.value != null ? this.value.hashCode() : 0);
        hash = 11 * hash + (this.axisID != null ? this.axisID.hashCode() : 0);
        hash = 11 * hash + (this.referenceFrame != null ? this.referenceFrame.hashCode() : 0);
        return hash;
    }
    
    @Override
    public String toString(){
        return "AxisId: " + axisID + " referenceFrame: " + referenceFrame + " value=" + value;
    }

}
