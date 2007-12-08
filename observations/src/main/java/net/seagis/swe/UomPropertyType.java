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
import org.geotools.resources.Utilities;

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

    
    public UomPropertyType() {}
    
    public UomPropertyType(String code, String href) {
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
     */
    public List<String> getNilReason() {
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
    
    /**
     * Vérifie si cette entré est identique à l'objet spécifié.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        final UomPropertyType that = (UomPropertyType) object;
        return Utilities.equals(this.actuate,        that.actuate)        &&
               Utilities.equals(this.arcrole,        that.arcrole)        &&
               Utilities.equals(this.baseUnit,       that.baseUnit)       &&
               Utilities.equals(this.code,           that.code)           &&
               Utilities.equals(this.href,           that.href)           &&
               Utilities.equals(this.nilReason,      that.nilReason)      &&
               Utilities.equals(this.remoteSchema,   that.remoteSchema)   &&
               Utilities.equals(this.role,           that.role)           &&
               Utilities.equals(this.show,           that.show)           &&
               Utilities.equals(this.title,          that.title)          &&
               Utilities.equals(this.type,           that.type)           &&
               Utilities.equals(this.unitDefinition, that.unitDefinition);
        
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + (this.unitDefinition != null ? this.unitDefinition.hashCode() : 0);
        hash = 41 * hash + (this.baseUnit != null ? this.baseUnit.hashCode() : 0);
        hash = 41 * hash + (this.code != null ? this.code.hashCode() : 0);
        hash = 41 * hash + (this.nilReason != null ? this.nilReason.hashCode() : 0);
        hash = 41 * hash + (this.remoteSchema != null ? this.remoteSchema.hashCode() : 0);
        hash = 41 * hash + (this.actuate != null ? this.actuate.hashCode() : 0);
        hash = 41 * hash + (this.arcrole != null ? this.arcrole.hashCode() : 0);
        hash = 41 * hash + (this.href != null ? this.href.hashCode() : 0);
        hash = 41 * hash + (this.role != null ? this.role.hashCode() : 0);
        hash = 41 * hash + (this.show != null ? this.show.hashCode() : 0);
        hash = 41 * hash + (this.title != null ? this.title.hashCode() : 0);
        hash = 41 * hash + (this.type != null ? this.type.hashCode() : 0);
        return hash;
    }
    
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        if(actuate != null && !actuate.equals("")) 
            s.append("actuate:").append(actuate).append('\n');
        if(arcrole != null && !arcrole.equals("")) 
            s.append("arcrole:").append(arcrole).append('\n');
        if(baseUnit != null) 
            s.append("baseUnit:").append(baseUnit.toString()).append('\n');
        if(code != null && !code.equals("")) 
            s.append("code:").append(code).append('\n');
        if(href != null && !href.equals("")) 
            s.append("href:").append(href).append('\n');
        s.append("nilReason:").append('\n');
        for (String ss:nilReason) {
            s.append(ss).append('\n');
        }
        if(remoteSchema != null && !remoteSchema.equals("")) 
            s.append("remoteSchema:").append(remoteSchema).append('\n');
        if(role != null && !role.equals("")) 
            s.append("role:").append(role).append('\n');
        if(show != null && !show.equals("")) 
            s.append("show:").append(show).append('\n');
        if(title != null && !title.equals("")) 
            s.append("title:").append(title).append('\n');
        if(type != null && !type.equals("")) 
            s.append("type:").append(type).append('\n');
        if(unitDefinition != null) 
            s.append("unitDefinition:").append(unitDefinition).append('\n');
        
        
        
        
        
        
        return s.toString();
    }

}
