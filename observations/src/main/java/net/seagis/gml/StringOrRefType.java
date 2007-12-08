
package net.seagis.gml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import org.geotools.resources.Utilities;


/**
 * <p>Java class for StringOrRefType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="StringOrRefType">
 *   &lt;simpleContent>
 *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *       &lt;attGroup ref="{http://www.opengis.net/gml/3.2}AssociationAttributeGroup"/>
 *     &lt;/extension>
 *   &lt;/simpleContent>
 * &lt;/complexType>
 * </pre>
 * 
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StringOrRefType", propOrder = {
    "value"
})
public class StringOrRefType {

    @XmlValue
    protected String value;
    @XmlAttribute
    protected List<String> nilReason = new ArrayList<String>();
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

    /**
     * Gets the value of the value property.
     * 
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the nilReason property.
     * 
     */
    public List<String> getNilReason() {
        return this.nilReason;
    }

    /**
     * Gets the value of the remoteSchema property.
     * 
     */
    public String getRemoteSchema() {
        return remoteSchema;
    }

    /**
     * Sets the value of the remoteSchema property.
     * 
     */
    public void setRemoteSchema(String value) {
        this.remoteSchema = value;
    }

    /**
     * Gets the value of the actuate property.
     * 
     */
    public String getActuate() {
        return actuate;
    }

    /**
     * Sets the value of the actuate property.
     * 
     */
    public void setActuate(String value) {
        this.actuate = value;
    }

    /**
     * Gets the value of the arcrole property.
     * 
     */
    public String getArcrole() {
        return arcrole;
    }

    /**
     * Sets the value of the arcrole property.
     * 
     */
    public void setArcrole(String value) {
        this.arcrole = value;
    }

    /**
     * Gets the value of the href property.
     */
    public String getHref() {
        return href;
    }

    /**
     * Sets the value of the href property.
     * 
     */
    public void setHref(String value) {
        this.href = value;
    }

    /**
     * Gets the value of the role property.
     * 
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the value of the role property.
     * 
     */
    public void setRole(String value) {
        this.role = value;
    }

    /**
     * Gets the value of the show property.
     * 
     */
    public String getShow() {
        return show;
    }

    /**
     * Sets the value of the show property.
     */
    public void setShow(String value) {
        this.show = value;
    }

    /**
     * Gets the value of the title property.
     * 
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     */
    public void setTitle(String value) {
        this.title = value;
    }

    /**
     * Gets the value of the type property.
     */
    public String getType() {
        if (type == null) {
            return "simple";
        } else {
            return type;
        }
    }

    /**
     * Sets the value of the type property.
     */
    public void setType(String value) {
        this.type = value;
    }
    
     /**
     * Verifie si cette entree est identique l'objet specifie.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        final LocationPropertyType that = (LocationPropertyType) object;

        return Utilities.equals(this.actuate,            that.actuate)          &&
               Utilities.equals(this.value,              that._null)            &&
               Utilities.equals(this.arcrole,            that.arcrole)          &&
               Utilities.equals(this.type,               that.type)             &&
               Utilities.equals(this.href,               that.href)             &&
               Utilities.equals(this.nilReason,          that.nilReason)        &&
               Utilities.equals(this.remoteSchema,       that.remoteSchema)     &&
               Utilities.equals(this.show,               that.show)             &&
               Utilities.equals(this.role,               that.role)             &&
               Utilities.equals(this.title,              that.title);
        
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + (this.value != null ? this.value.hashCode() : 0);
        hash = 23 * hash + (this.nilReason != null ? this.nilReason.hashCode() : 0);
        hash = 23 * hash + (this.remoteSchema != null ? this.remoteSchema.hashCode() : 0);
        hash = 23 * hash + (this.actuate != null ? this.actuate.hashCode() : 0);
        hash = 23 * hash + (this.arcrole != null ? this.arcrole.hashCode() : 0);
        hash = 23 * hash + (this.href != null ? this.href.hashCode() : 0);
        hash = 23 * hash + (this.role != null ? this.role.hashCode() : 0);
        hash = 23 * hash + (this.show != null ? this.show.hashCode() : 0);
        hash = 23 * hash + (this.title != null ? this.title.hashCode() : 0);
        hash = 23 * hash + (this.type != null ? this.type.hashCode() : 0);
        return hash;
    }

}
