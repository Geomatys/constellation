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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.opengis.filter.capability.ArithmeticOperators;
import org.opengis.filter.capability.ComparisonOperators;
import org.opengis.filter.capability.ScalarCapabilities;


/**
 * <p>Java class for Scalar_CapabilitiesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Scalar_CapabilitiesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/ogc}LogicalOperators" minOccurs="0"/>
 *         &lt;element name="ComparisonOperators" type="{http://www.opengis.net/ogc}ComparisonOperatorsType" minOccurs="0"/>
 *         &lt;element name="ArithmeticOperators" type="{http://www.opengis.net/ogc}ArithmeticOperatorsType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Scalar_CapabilitiesType", propOrder = {
    "logicalOperators",
    "comparisonOperators",
    "arithmeticOperators"
})
public class ScalarCapabilitiesType implements ScalarCapabilities {

    @XmlElement(name = "LogicalOperators")
    private LogicalOperators logicalOperators;
    @XmlElement(name = "ComparisonOperators")
    private ComparisonOperatorsType comparisonOperators;
    @XmlElement(name = "ArithmeticOperators")
    private ArithmeticOperatorsType arithmeticOperators;

    /**
     * An empty constructor used by JAXB
     */
    ScalarCapabilitiesType() {
    }
    
     /**
     *Build a new Scalar Capabilities
     */
    public ScalarCapabilitiesType(ComparisonOperators comparison, ArithmeticOperators arithmetic, boolean logical) {
        if (logical) {
            this.logicalOperators = new LogicalOperators();
        }
        this.comparisonOperators = (ComparisonOperatorsType) comparison;
        this.arithmeticOperators = (ArithmeticOperatorsType) arithmetic;
    }
    
    /**
     * Gets the value of the logicalOperators property.
     */
    public LogicalOperators getLogicalOperators() {
        return logicalOperators;
    }

    /**
     * Gets the value of the comparisonOperators property.
     */
    public ComparisonOperatorsType getComparisonOperators() {
        return comparisonOperators;
    }

    /**
     * Gets the value of the arithmeticOperators property.
     */
    public ArithmeticOperatorsType getArithmeticOperators() {
        return arithmeticOperators;
    }

    public boolean hasLogicalOperators() {
        return logicalOperators != null;
    }
}
