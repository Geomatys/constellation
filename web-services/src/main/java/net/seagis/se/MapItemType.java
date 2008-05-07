/*
 * Sicade - SystÃ¨mes intÃ©grÃ©s de connaissances pour l'aide Ã  la dÃ©cision en environnement
 * (C) 2005, Institut de Recherche pour le DÃ©veloppement
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


package net.seagis.se;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import net.seagis.coverage.web.ExpressionType;
import org.opengis.filter.expression.ExpressionVisitor;


/**
 * <p>Java class for MapItemType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MapItemType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/ogc}ExpressionType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/se}Data"/>
 *         &lt;element ref="{http://www.opengis.net/se}Value"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MapItemType", propOrder = {
    "data",
    "value"
})
public class MapItemType extends ExpressionType {

    @XmlElement(name = "Data")
    private double data;
    @XmlElement(name = "Value", required = true)
    private ParameterValueType value;

    /**
     * Empty Constructor used by JAXB.
     */
    MapItemType() {
        
    }
    
    /**
     * Build a new Map Item.
     */
    public MapItemType(double data, ParameterValueType value) {
        this.data  = data;
        this.value = value;
    }
    
    /**
     * Gets the value of the data property.
     */
    public double getData() {
        return data;
    }

    /**
     * Gets the value of the value property.
     */
    public ParameterValueType getValue() {
        return value;
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
