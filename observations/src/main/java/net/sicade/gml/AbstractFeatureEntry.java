package net.sicade.gml;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;
import net.opengis.gml.LocationPropertyType;


/**
 * The basic feature model is given by the gml:AbstractFeatureType.
 * The content model for gml:AbstractFeatureType adds two specific properties suitable for geographic features to the content model defined in gml:AbstractGMLType. 
 * The value of the gml:boundedBy property describes an envelope that encloses the entire feature instance, and is primarily useful for supporting rapid searching for features that occur in a particular location. 
 * The value of the gml:location property describes the extent, position or relative location of the feature.
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractFeatureType", propOrder = {
    "boundedBy",
    "location"
})
public abstract class AbstractFeatureEntry extends AbstractGMLEntry {

    @XmlElement(nillable = true)
    protected BoundingShapeEntry boundedBy;
    @XmlElementRef(name = "location", namespace = "http://www.opengis.net/gml/3.2", type = JAXBElement.class)
    protected JAXBElement<? extends LocationPropertyType> location;

    /**
     *  Constructeur vide utilise par JAXB.
     */
    public AbstractFeatureEntry() {}
    
    /**
     * cree un nouveau "Feature"
     */
    public AbstractFeatureEntry(String id, String name, String description, ReferenceEntry descriptionReference,
            BoundingShapeEntry boundedBy) {
        super(id, name, description, descriptionReference);
        this.boundedBy = boundedBy;
    }
        
    /**
     * Gets the value of the boundedBy property.
     * 
     * @return
     *     possible object is
     *     {@link BoundingShapeType }
     *     
     */
    public BoundingShapeEntry getBoundedBy() {
        return boundedBy;
    }

    /**
     * Sets the value of the boundedBy property.
     * 
     * @param value
     *     allowed object is
     *     {@link BoundingShapeType }
     *     
     */
    public void setBoundedBy(BoundingShapeEntry value) {
        this.boundedBy = value;
    }

    /**
     * Gets the value of the location property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link PriorityLocationPropertyType }{@code >}
     *     {@link JAXBElement }{@code <}{@link LocationPropertyType }{@code >}
     *     
     */
    public JAXBElement<? extends LocationPropertyType> getLocation() {
        return location;
    }

    /**
     * Sets the value of the location property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link PriorityLocationPropertyType }{@code >}
     *     {@link JAXBElement }{@code <}{@link LocationPropertyType }{@code >}
     *     
     */
    public void setLocation(JAXBElement<? extends LocationPropertyType> value) {
        this.location = ((JAXBElement<? extends LocationPropertyType> ) value);
    }

}
