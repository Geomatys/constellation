package net.seagis.gml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import net.seagis.catalog.Entry;
import org.geotools.resources.Utilities;


/**
 * <p>Java class for AbstractGMLType complex type.
 * 
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractGMLType", propOrder = {
    "description",
    "descriptionReference",
    "name"
})
public abstract class AbstractGMLEntry extends Entry{

    //protected List<MetaDataPropertyType> metaDataProperty;
    //protected CodeWithAuthorityType identifier;
    protected String description;
    protected ReferenceEntry descriptionReference;
    protected String name;
    @XmlAttribute(namespace = "http://www.opengis.net/gml/3.2", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;

    /**
     *  Constructeur vide utilise par JAXB.
     */
    public AbstractGMLEntry() {}
    
    public AbstractGMLEntry(String id, String name, String description, ReferenceEntry descriptionReference) {
        super(id);
        this.id = id;
        this.name = name;
        this.description = description;
        this.descriptionReference = descriptionReference;
    }
    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link StringOrRefType }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the value of the descriptionReference property.
     * 
     * @return
     *     possible object is
     *     {@link ReferenceType }
     *     
     */
    public ReferenceEntry getDescriptionReference() {
        return descriptionReference;
    }


    @Override
    public String getName() {
        return name;
    }
    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * Verifie si cette entree est identique a l'objet specifie.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final AbstractGMLEntry that = (AbstractGMLEntry) object;

            return Utilities.equals(this.description,          that.description)          &&
                   Utilities.equals(this.descriptionReference, that.descriptionReference) &&
                   Utilities.equals(this.id,                   that.id)                   &&
                   Utilities.equals(this.name,                 that.name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 53 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }
    
    /**
     * Gets the value of the identifier property.
     * 
     * @return
     *     possible object is
     *     {@link CodeWithAuthorityType }
     *     
     
    public CodeWithAuthorityType getIdentifier() {
        return identifier;
    }

    /**
     * Sets the value of the identifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link CodeWithAuthorityType }
     *     
     
    public void setIdentifier(CodeWithAuthorityType value) {
        this.identifier = value;
    }*/
    
    /**
     * Gets the value of the metaDataProperty property.
     * 
     
    public List<MetaDataPropertyType> getMetaDataProperty() {
        if (metaDataProperty == null) {
            metaDataProperty = new ArrayList<MetaDataPropertyType>();
        }
        return this.metaDataProperty;
    }*/

}
