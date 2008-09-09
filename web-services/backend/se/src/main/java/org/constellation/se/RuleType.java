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
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;
//import net.opengis.ogc.FilterType;


/**
 * <p>Java class for RuleType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RuleType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/se}Name" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/se}Description" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/se}LegendGraphic" minOccurs="0"/>
 *         &lt;choice minOccurs="0">
 *           &lt;element ref="{http://www.opengis.net/ogc}Filter"/>
 *           &lt;element ref="{http://www.opengis.net/se}ElseFilter"/>
 *         &lt;/choice>
 *         &lt;element ref="{http://www.opengis.net/se}MinScaleDenominator" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/se}MaxScaleDenominator" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/se}Symbolizer" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RuleType", propOrder = {
    "name",
    "description",
    "legendGraphic",
    "filter",
    "elseFilter",
    "minScaleDenominator",
    "maxScaleDenominator",
    "symbolizer"
})
public class RuleType {

    @XmlElement(name = "Name")
    private String name;
    @XmlElement(name = "Description")
    private DescriptionType description;
    @XmlElement(name = "LegendGraphic")
    private LegendGraphicType legendGraphic;
    //@XmlElement(name = "Filter", namespace = "http://www.opengis.net/ogc")
    //private FilterType filter;
    @XmlElement(name = "ElseFilter")
    private ElseFilterType elseFilter;
    @XmlElement(name = "MinScaleDenominator")
    private Double minScaleDenominator;
    @XmlElement(name = "MaxScaleDenominator")
    private Double maxScaleDenominator;
    @XmlElementRef(name = "Symbolizer", namespace = "http://www.opengis.net/se", type = JAXBElement.class)
    private List<JAXBElement<? extends SymbolizerType>> symbolizer;

    /**
     * Empty Constructor used by JAXB.
     */
    RuleType() {
        
    }
    
    /**
     * Build a new Rule.
     */
    public RuleType(String name, DescriptionType description, LegendGraphicType legendGraphic,
            ElseFilterType elseFilter, Double minScaleDenominator, Double maxScaleDenominator,
            List<JAXBElement<? extends SymbolizerType>> symbolizer) {
        
        this.name                = name;
        this.description         = description;
        this.legendGraphic       = legendGraphic;
        this.elseFilter          = elseFilter;
        this.minScaleDenominator = minScaleDenominator;
        this.maxScaleDenominator = maxScaleDenominator;
        this.symbolizer          = symbolizer;
    }
    
    /**
     * Gets the value of the name property.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the value of the description property.
     */
    public DescriptionType getDescription() {
        return description;
    }

    /**
     * Gets the value of the legendGraphic property.
     */
    public LegendGraphicType getLegendGraphic() {
        return legendGraphic;
    }

    /**
     * Gets the value of the filter property.
     * 
     * @return
     *     possible object is
     *     {@link FilterType }
     *     
     
    public FilterType getFilter() {
        return filter;
    }

    /**
     * Sets the value of the filter property.
     * 
     * @param value
     *     allowed object is
     *     {@link FilterType }
     *     
     
    public void setFilter(FilterType value) {
        this.filter = value;
    }*/

    /**
     * Gets the value of the elseFilter property.
     * 
     */
    public ElseFilterType getElseFilter() {
        return elseFilter;
    }

    /**
     * Gets the value of the minScaleDenominator property.
     */
    public Double getMinScaleDenominator() {
        return minScaleDenominator;
    }

    /**
     * Gets the value of the maxScaleDenominator property.
     */
    public Double getMaxScaleDenominator() {
        return maxScaleDenominator;
    }

    /**
     * Gets the value of the symbolizer property.
     * (unmodifiable)
     */
    public List<JAXBElement<? extends SymbolizerType>> getSymbolizer() {
        if (symbolizer == null) {
            symbolizer = new ArrayList<JAXBElement<? extends SymbolizerType>>();
        }
        return Collections.unmodifiableList(symbolizer);
    }

}
