/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 * (C) 2007, Geomatys
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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.opengis.filter.FilterVisitor;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.expression.Expression;


/**
 * <p>Java class for PropertyIsLikeType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PropertyIsLikeType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/ogc}ComparisonOpsType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/ogc}PropertyName"/>
 *         &lt;element ref="{http://www.opengis.net/ogc}Literal"/>
 *       &lt;/sequence>
 *       &lt;attribute name="escapeChar" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="matchCase" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="singleChar" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="wildCard" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PropertyIsLikeType", propOrder = {
    "propertyName",
    "literal"
})
public class PropertyIsLikeType extends ComparisonOpsType implements PropertyIsLike {

    @XmlElement(name = "PropertyName", required = true)
    private PropertyNameType propertyName;
    @XmlElement(name = "Literal", required = true)
    private LiteralType literal;
    @XmlAttribute(required = true)
    private String escapeChar;
    @XmlAttribute
    private Boolean matchCase;
    @XmlAttribute(required = true)
    private String singleChar;
    @XmlAttribute(required = true)
    private String wildCard;

    /**
     * An empty constructor used by JAXB.
     */
    PropertyIsLikeType() {
        
    }
    
    /**
     *Build a new Property is like operator
     */
    public PropertyIsLikeType(Expression expr, String pattern, String wildcard, String singleChar, String escape) {
        this.escapeChar   = escape;
        this.propertyName = (PropertyNameType) expr;
        this.singleChar   = singleChar;
        this.wildCard     = wildcard;
        this.literal      = new LiteralType(pattern);
    }
    
    /**
     * Gets the value of the propertyName property.
     */
    public PropertyNameType getPropertyName() {
        return propertyName;
    }

    /**
     * Gets the value of the literal property.
     */
    public String getLiteral() {
        return literal.getStringValue();
    }

    /**
     * Gets the value of the escapeChar property.
     */
    public String getEscapeChar() {
        if (escapeChar == null)
            escapeChar = "\\";
        
        return escapeChar;
    }

    /**
     * Gets the value of the matchCase property.
    */
    public boolean isMatchCase() {
        if (matchCase == null) {
            return true;
        } else {
            return matchCase;
        }
    }

    /**
     * Gets the value of the singleChar property.
     */
    public String getSingleChar() {
        return singleChar;
    }

    /**
     * Gets the value of the wildCard property.
     */
    public String getWildCard() {
        return wildCard;
    }
    
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(super.toString());
        if (propertyName != null) {
            s.append("PropertyName= ").append(propertyName.toString()).append('\n');
        } else s.append("PropertyName null").append('\n');
        
        if (literal != null) {
           s.append("Litteral= ").append(literal.toString()).append('\n');
        } else s.append("Literal null").append('\n');
        
        s.append("matchCase= ").append(matchCase).append(" escape=").append(escapeChar);
        s.append(" single=").append(singleChar).append(" wildCard=").append(wildCard);
        
        return s.toString();
    }

    public Expression getExpression() {
        return propertyName;
    }

    public String getEscape() {
        return escapeChar;
    }

    public boolean evaluate(Object object) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object accept(FilterVisitor visitor, Object extraData) {
        return visitor.visit(this,extraData);
    }
}
