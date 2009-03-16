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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import org.constellation.sml.Component;


/**
 * <p>Java class for ComponentType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ComponentType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/sensorML/1.0}AbstractComponentType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/sensorML/1.0}method" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ComponentType")
public class ComponentType extends AbstractComponentType implements Component {

    private MethodPropertyType method;

    /**
     * @return the method
     */
    public MethodPropertyType getMethod() {
        return method;
    }

    /**
     * @param method the method to set
     */
    public void setMethod(MethodPropertyType method) {
        this.method = method;
    }


}
