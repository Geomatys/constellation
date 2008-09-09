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
package org.constellation.se;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.opengis.filter.expression.ExpressionVisitor;


/**
 * <p>Java class for RecodeType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RecodeType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/se}FunctionType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/se}LookupValue"/>
 *         &lt;element ref="{http://www.opengis.net/se}MapItem" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RecodeType", propOrder = {
    "lookupValue",
    "mapItem"
})
public class RecodeType extends FunctionType  {

    @XmlElement(name = "LookupValue", required = true)
    private ParameterValueType lookupValue;
    @XmlElement(name = "MapItem", required = true)
    private List<MapItemType> mapItem;

    /**
     * Empty Constructor used by JAXB.
     */
    RecodeType() {
        
    }
    
    /**
     * Build a new Recode type.
     */
    public RecodeType(ParameterValueType lookupValue, List<MapItemType> mapItem) {
        this.lookupValue = lookupValue;
        this.mapItem     = mapItem;
    }
    
    /**
     * Gets the value of the lookupValue property.
     */
    public ParameterValueType getLookupValue() {
        return lookupValue;
    }

    /**
     * Gets the value of the mapItem property.
     */
    public List<MapItemType> getMapItem() {
        if (mapItem == null) {
            mapItem = new ArrayList<MapItemType>();
        }
        return Collections.unmodifiableList(mapItem);
    }

    public Object evaluate(Object object) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public <T> T evaluate(Object object, Class<T> context) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object accept(ExpressionVisitor visitor, Object extraData) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
