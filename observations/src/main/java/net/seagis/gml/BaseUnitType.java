
package net.seagis.gml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.geotools.resources.Utilities;


/**
 * <p>Java class for BaseUnitType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BaseUnitType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/gml/3.2}UnitDefinitionType">
 *       &lt;sequence>
 *         &lt;element name="unitsSystem" type="{http://www.opengis.net/gml/3.2}ReferenceType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BaseUnitType", propOrder = {
    "unitsSystem"
})
public class BaseUnitType extends UnitDefinitionType {

    @XmlElement(required = true)
    protected ReferenceEntry unitsSystem;

    /**
     * Gets the value of the unitsSystem property.
     * 
     * @return
     *     possible object is
     *     {@link ReferenceType }
     *     
     */
    public ReferenceEntry getUnitsSystem() {
        return unitsSystem;
    }

    /**
     * Sets the value of the unitsSystem property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReferenceType }
     *     
     */
    public void setUnitsSystem(ReferenceEntry value) {
        this.unitsSystem = value;
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
            final BaseUnitType that = (BaseUnitType) object;
            return Utilities.equals(this.unitsSystem,        that.unitsSystem); 
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (this.unitsSystem != null ? this.unitsSystem.hashCode() : 0);
        return hash;
    }
    
    @Override
    public String toString() {
        return "BaseUnit unitSystem:" + unitsSystem.toString();
    }


}
