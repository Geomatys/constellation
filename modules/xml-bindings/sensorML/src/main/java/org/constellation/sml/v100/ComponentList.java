/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.sml.v100;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.constellation.sml.AbstractComponentList;
import org.geotoolkit.util.Utilities;

/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="component" maxOccurs="unbounded">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence minOccurs="0">
 *                   &lt;element ref="{http://www.opengis.net/sensorML/1.0}_Process"/>
 *                 &lt;/sequence>
 *                 &lt;attGroup ref="{http://www.opengis.net/gml}AssociationAttributeGroup"/>
 *                 &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}token" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "component"
})
public class ComponentList implements AbstractComponentList {

    @XmlElement(required = true)
    private List<ComponentPropertyType> component;

    public ComponentList() {

    }

    public ComponentList(AbstractComponentList component) {
        this.component = (List<ComponentPropertyType>) component.getComponent();
    }

    public ComponentList(List<ComponentPropertyType> component) {
        this.component = component;
    }

    /**
     * Gets the value of the component property.
     *
     */
    public List<ComponentPropertyType> getComponent() {
        if (component == null) {
            component = new ArrayList<ComponentPropertyType>();
        }
        return this.component;
    }


    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof ComponentList) {
            final ComponentList that = (ComponentList) object;
            return Utilities.equals(this.component, that.component);

        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 43 * hash + (this.component != null ? this.component.hashCode() : 0);
        return hash;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[ComponentList]").append("\n");
        if (component != null) {
            sb.append("component:").append('\n');
            for (ComponentPropertyType k : component) {
                sb.append("component: ").append(k).append('\n');
            }
        }
        return sb.toString();
     }

}

