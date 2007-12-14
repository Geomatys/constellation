package net.opengis.se;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *         A "SymbolizerType" is an abstract type for encoding the graphical
 *         properties used to portray geographic information.  Concrete Symbolizer
 *         types are derived from this base type.
 *       
 * 
 * <p>Java class for SymbolizerType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SymbolizerType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/se}Name" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/se}Description" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/se}BaseSymbolizer" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="version" type="{http://www.opengis.net/se}VersionType" />
 *       &lt;attribute name="uom" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SymbolizerType", propOrder = {
    "name",
    "description",
    "baseSymbolizer"
})
/*@XmlSeeAlso({
    LineSymbolizerType.class,
    PointSymbolizerType.class,
    PolygonSymbolizerType.class,
    TextSymbolizerType.class,
    RasterSymbolizerType.class
})*/
public abstract class SymbolizerType {

    @XmlElement(name = "Name")
    protected String name;
    @XmlElement(name = "Description")
    protected DescriptionType description;
    @XmlElement(name = "BaseSymbolizer")
    protected BaseSymbolizerType baseSymbolizer;
    @XmlAttribute
    protected String version;
    @XmlAttribute
    @XmlSchemaType(name = "anyURI")
    protected String uom;

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
     * Gets the value of the baseSymbolizer property.
     * 
     * @return
     *     possible object is
     *     {@link BaseSymbolizerType }
     *     
     */
    public BaseSymbolizerType getBaseSymbolizer() {
        return baseSymbolizer;
    }

    /**
     * Sets the value of the baseSymbolizer property.
     * 
     * @param value
     *     allowed object is
     *     {@link BaseSymbolizerType }
     *     
     */
    public void setBaseSymbolizer(BaseSymbolizerType value) {
        this.baseSymbolizer = value;
    }

    /**
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }

    /**
     * Gets the value of the uom property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUom() {
        return uom;
    }

    /**
     * Sets the value of the uom property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUom(String value) {
        this.uom = value;
    }

}
