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
package net.seagis.se;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import net.seagis.coverage.web.ExpressionType;


/**
 * <p>Java class for FunctionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FunctionType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/ogc}ExpressionType">
 *       &lt;attribute name="fallbackValue" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FunctionType")
/*@XmlSeeAlso({
    CategorizeType.class,
    TrimType.class,
    InterpolateType.class,
    RecodeType.class,
    StringPositionType.class,
    SubstringType.class,
    ConcatenateType.class,
    StringLengthType.class,
    FormatDateType.class,
    ChangeCaseType.class,
    FormatNumberType.class
})*/
public abstract class FunctionType extends ExpressionType {

    @XmlAttribute(required = true)
    private String fallbackValue;

    /**
     * Empty Constructor used by JAXB.
     */
    FunctionType() {
        
    }
    
    /**
     * Build a new Function.
     */
    public FunctionType(String fallbackValue) {
        this.fallbackValue = fallbackValue;
    }
    
    /**
     * Gets the value of the fallbackValue property.
     */
    public String getFallbackValue() {
        return fallbackValue;
    }
}
