/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */


package net.seagis.swe;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import net.seagis.catalog.Entry;
import org.geotools.resources.Utilities;


/**
 * <p>Java class for componentPropertyType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="componentPropertyType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence minOccurs="0">
 *         &lt;element ref="{http://www.opengis.net/swe/1.0.1}component"/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://www.opengis.net/gml}AssociationAttributeGroup"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AnyScalarPropertyType", propOrder = {
    "component",
    "name"
})
public class AnyScalarPropertyType extends Entry {

    @XmlTransient
    private String idDataRecord;
    
    @XmlAttribute
    private String name;
    
    @XmlElementRef(name = "AbstractDataComponent", namespace = "http://www.opengis.net/swe/1.0.1", type = JAXBElement.class)
    private JAXBElement<? extends AbstractDataComponentEntry> component;
    
    @XmlAttribute(namespace = "http://www.opengis.net/gml")
    @XmlSchemaType(name = "anyURI")
    private String remoteSchema;
    @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
    private String type;
    @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
    @XmlSchemaType(name = "anyURI")
    private String href;
    @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
    @XmlSchemaType(name = "anyURI")
    private String role;
    @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
    @XmlSchemaType(name = "anyURI")
    private String arcrole;
    @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
    private String title;
    @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
    private String show;
    @XmlAttribute(namespace = "http://www.w3.org/1999/xlink")
    private String actuate;

    @XmlTransient
    private static ObjectFactory sweFactory = new ObjectFactory();
    
    /**
     * An empty constructor used by JAXB.
     */
    AnyScalarPropertyType() {
        
    }
    
    /**
     * Build a new component Property.
     */
    public AnyScalarPropertyType(String idDataRecord, String name, AbstractDataComponentEntry component) {
        super(name);
        this.name         = name;
        this.idDataRecord = idDataRecord;
        if (component instanceof TimeType) {
            this.component = sweFactory.createTime((TimeType)component);
        } else if (component instanceof QuantityType) {
            this.component = sweFactory.createQuantity((QuantityType)component);
        } else if (component instanceof BooleanType) {
            this.component = sweFactory.createBoolean((BooleanType)component);
        } else {
            throw new IllegalArgumentException("only TimeType, QuantityType and BooleanType are allowed");
        }
    }
    
    /**
     * surcharge le getName() de Entry
     */
    public String getName() {
        return this.name;
    }
    /** 
     * retourne l'identifiant du data record qui contient ce champ.
     */
    public String getIdDataRecord() {
        return idDataRecord;
    }
    
    /**
     * Gets the value of the phenomenon property.
     */
    public AbstractDataComponentEntry getComponent() {
        if (component != null) {
            return component.getValue();
        } else {
            return null;
        }
    }

    /**
     * Gets the value of the remoteSchema property.
     */
    public String getRemoteSchema() {
        return remoteSchema;
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
     * Gets the value of the href property.
     */
    public String getHref() {
        return href;
    }

    /**
     * Gets the value of the role property.
     */
    public String getRole() {
        return role;
    }

    /**
     * Gets the value of the arcrole property.
     */
    public String getArcrole() {
        return arcrole;
    }

    /**
     * Gets the value of the title property.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the value of the show property.
     */
    public String getShow() {
        return show;
    }

    /**
     * Gets the value of the actuate property.
     */
    public String getActuate() {
        return actuate;
    }
 /**
     * Verify if this entry is identical to specified object.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        boolean compo = false;
        final AnyScalarPropertyType that = (AnyScalarPropertyType) object;
        if (this.component != null && that.component != null) {
            compo = Utilities.equals(this.component.getValue(), that.component.getValue());
            //System.out.println("component NOT NULL :" + pheno);
        } else {
            compo = (this.component == null && that.component == null);
            //System.out.println("component NULL :" + pheno);
        }
        
        return compo                                                            &&
               Utilities.equals(this.actuate,            that.actuate)          &&
               Utilities.equals(this.arcrole,            that.arcrole)          &&  
               Utilities.equals(this.type,               that.type)             &&
               Utilities.equals(this.href,               that.href)             &&
               Utilities.equals(this.remoteSchema,       that.remoteSchema)     &&
               Utilities.equals(this.show,               that.show)             &&
               Utilities.equals(this.role,               that.role)             &&
               Utilities.equals(this.title,              that.title);
    }

    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + (this.component != null ? this.component.hashCode() : 0);
        hash = 47 * hash + (this.remoteSchema != null ? this.remoteSchema.hashCode() : 0);
        hash = 47 * hash + (this.actuate != null ? this.actuate.hashCode() : 0);
        hash = 47 * hash + (this.arcrole != null ? this.arcrole.hashCode() : 0);
        hash = 47 * hash + (this.href != null ? this.href.hashCode() : 0);
        hash = 47 * hash + (this.role != null ? this.role.hashCode() : 0);
        hash = 47 * hash + (this.show != null ? this.show.hashCode() : 0);
        hash = 47 * hash + (this.title != null ? this.title.hashCode() : 0);
        hash = 47 * hash + (this.type != null ? this.type.hashCode() : 0);
        return hash;
    }

    /**
     * Retourne une representation de l'objet.
     */
    
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        if (component != null)
            s.append(component.getValue()).append('\n');
        
        if(actuate != null) {
            s.append("actuate=").append(actuate).append('\n');
        }
        if(arcrole != null) {
            s.append("arcrole=").append(arcrole).append('\n');
        }
        if(href != null) {
            s.append("href=").append(href).append('\n');
        }
        if(role != null) {
            s.append("role=").append(role).append('\n');
        }
        if(show != null) {
            s.append("show=").append(show).append('\n');
        }
        if(title != null) {
            s.append("title=").append(title).append('\n');
        }
        if(title != null) {
            s.append("title=").append(title).append('\n');
        }
        return s.toString();
    }
    
}