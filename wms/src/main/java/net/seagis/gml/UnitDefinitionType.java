
package net.seagis.gml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import org.geotools.resources.Utilities;


/**
 * <p>Java class for UnitDefinitionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UnitDefinitionType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/gml/3.2}DefinitionType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/gml/3.2}quantityType" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/gml/3.2}quantityTypeReference" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/gml/3.2}catalogSymbol" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UnitDefinitionType", propOrder = {
    "quantityType",
    "quantityTypeReference",
    "catalogSymbol"
})
@XmlSeeAlso({ BaseUnitType.class }) 
public class UnitDefinitionType extends DefinitionType {

    protected StringOrRefType quantityType;
    protected ReferenceEntry quantityTypeReference;
    protected CodeType catalogSymbol;

    /**
     * Gets the value of the quantityType property.
     * 
     * @return
     *     possible object is
     *     {@link StringOrRefType }
     *     
     */
    public StringOrRefType getQuantityType() {
        return quantityType;
    }

    /**
     * Sets the value of the quantityType property.
     * 
     * @param value
     *     allowed object is
     *     {@link StringOrRefType }
     *     
     */
    public void setQuantityType(StringOrRefType value) {
        this.quantityType = value;
    }

    /**
     * Gets the value of the quantityTypeReference property.
     * 
     * @return
     *     possible object is
     *     {@link ReferenceType }
     *     
     */
    public ReferenceEntry getQuantityTypeReference() {
        return quantityTypeReference;
    }

    /**
     * Sets the value of the quantityTypeReference property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReferenceType }
     *     
     */
    public void setQuantityTypeReference(ReferenceEntry value) {
        this.quantityTypeReference = value;
    }

    /**
     * Gets the value of the catalogSymbol property.
     * 
     * @return
     *     possible object is
     *     {@link CodeType }
     *     
     */
    public CodeType getCatalogSymbol() {
        return catalogSymbol;
    }

    /**
     * Sets the value of the catalogSymbol property.
     * 
     * @param value
     *     allowed object is
     *     {@link CodeType }
     *     
     */
    public void setCatalogSymbol(CodeType value) {
        this.catalogSymbol = value;
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
            final UnitDefinitionType that = (UnitDefinitionType) object;
            return Utilities.equals(this.catalogSymbol,        that.catalogSymbol)        &&
                   Utilities.equals(this.quantityType,        that.quantityType)        &&
                   Utilities.equals(this.quantityTypeReference,       that.quantityTypeReference); 
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + (this.quantityType != null ? this.quantityType.hashCode() : 0);
        hash = 43 * hash + (this.quantityTypeReference != null ? this.quantityTypeReference.hashCode() : 0);
        hash = 43 * hash + (this.catalogSymbol != null ? this.catalogSymbol.hashCode() : 0);
        return hash;
    }
    
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("[UnitdefinitionType]").append('\n');
        if (catalogSymbol != null)
            s.append("CatalogSymbol=").append(catalogSymbol.toString()).append('\n');
        if (quantityType != null)
            s.append("quantityType=").append(quantityType.toString()).append('\n');
        if (quantityTypeReference != null)
            s.append("quantityTypeReference=").append(quantityTypeReference).append('\n');
        return s.toString();
    }
    
}
