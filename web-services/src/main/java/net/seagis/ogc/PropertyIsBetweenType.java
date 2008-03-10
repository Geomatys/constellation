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


package net.seagis.ogc;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import net.seagis.coverage.web.ExpressionType;


/**
 * <p>Java class for PropertyIsBetweenType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PropertyIsBetweenType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/ogc}ComparisonOpsType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/ogc}expression"/>
 *         &lt;element name="LowerBoundary" type="{http://www.opengis.net/ogc}LowerBoundaryType"/>
 *         &lt;element name="UpperBoundary" type="{http://www.opengis.net/ogc}UpperBoundaryType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PropertyIsBetweenType", propOrder = {
    "expression",
    "literal",
    "propertyName",
    "lowerBoundary",
    "upperBoundary"
})
public class PropertyIsBetweenType extends ComparisonOpsType {

    @XmlElement(nillable = true)
    private ExpressionType expression;
    @XmlElement(name = "Literal", nillable = true)
    private LiteralType literal;
    @XmlElement(name = "PropertyName", nillable = true)
    private String propertyName;
    @XmlElement(name = "LowerBoundary", required = true)
    private LowerBoundaryType lowerBoundary;
    @XmlElement(name = "UpperBoundary", required = true)
    private UpperBoundaryType upperBoundary;

    /**
     * Gets the value of the expression property.
     */
    public ExpressionType getExpression() {
        return expression;
    }

    /**
     * Gets the value of the literal property.
     */
    public LiteralType getLiteral() {
        return literal;
    }

    /**
     * Gets the value of the propertyName property.
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * Gets the value of the lowerBoundary property.
     */
    public LowerBoundaryType getLowerBoundary() {
        return lowerBoundary;
    }

    /**
     * Gets the value of the upperBoundary property.
     */
    public UpperBoundaryType getUpperBoundary() {
        return upperBoundary;
    }
}
