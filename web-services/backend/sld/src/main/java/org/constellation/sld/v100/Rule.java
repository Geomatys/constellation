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
package org.constellation.sld.v100;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.constellation.ogc.v100.FilterType;


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
 *         &lt;element ref="{http://www.opengis.net/sld}Name" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/sld}Title" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/sld}Abstract" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/sld}LegendGraphic" minOccurs="0"/>
 *         &lt;choice minOccurs="0">
 *           &lt;element ref="{http://www.opengis.net/ogc}Filter"/>
 *           &lt;element ref="{http://www.opengis.net/sld}ElseFilter"/>
 *         &lt;/choice>
 *         &lt;element ref="{http://www.opengis.net/sld}MinScaleDenominator" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/sld}MaxScaleDenominator" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/sld}Symbolizer" maxOccurs="unbounded"/>
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
    "name",
    "title",
    "_abstract",
    "legendGraphic",
    "filter",
    "elseFilter",
    "minScaleDenominator",
    "maxScaleDenominator",
    "symbolizer"
})
@XmlRootElement(name = "Rule")
public class Rule {

    @XmlElement(name = "Name")
    private String name;
    @XmlElement(name = "Title")
    private String title;
    @XmlElement(name = "Abstract")
    private String _abstract;
    @XmlElement(name = "LegendGraphic")
    private LegendGraphic legendGraphic;
    @XmlElement(name = "Filter", namespace = "http://www.opengis.net/ogc")
    private FilterType filter;
    @XmlElement(name = "ElseFilter")
    private ElseFilter elseFilter;
    @XmlElement(name = "MinScaleDenominator")
    private Double minScaleDenominator;
    @XmlElement(name = "MaxScaleDenominator")
    private Double maxScaleDenominator;
    @XmlElementRef(name = "Symbolizer", namespace = "http://www.opengis.net/sld", type = JAXBElement.class)
    private List<JAXBElement<? extends SymbolizerType>> symbolizer;

    /**
     * Gets the value of the name property.
     * 
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the title property.
     * 
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     */
    public void setTitle(String value) {
        this.title = value;
    }

    /**
     * Gets the value of the abstract property.
     * 
     */
    public String getAbstract() {
        return _abstract;
    }

    /**
     * Sets the value of the abstract property.
     * 
     */
    public void setAbstract(String value) {
        this._abstract = value;
    }

    /**
     * Gets the value of the legendGraphic property.
     * 
     */
    public LegendGraphic getLegendGraphic() {
        return legendGraphic;
    }

    /**
     * Sets the value of the legendGraphic property.
     * 
     */
    public void setLegendGraphic(LegendGraphic value) {
        this.legendGraphic = value;
    }

    /**
     * Gets the value of the filter property.
     * 
     */
    public FilterType getFilter() {
        return filter;
    }

    /**
     * Sets the value of the filter property.
     * 
     */
    public void setFilter(FilterType value) {
        this.filter = value;
    }

    /**
     * Gets the value of the elseFilter property.
     * 
     */
    public ElseFilter getElseFilter() {
        return elseFilter;
    }

    /**
     * Sets the value of the elseFilter property.
     * 
     */
    public void setElseFilter(ElseFilter value) {
        this.elseFilter = value;
    }

    /**
     * Gets the value of the minScaleDenominator property.
     * 
     */
    public Double getMinScaleDenominator() {
        return minScaleDenominator;
    }

    /**
     * Sets the value of the minScaleDenominator property.
     * 
     */
    public void setMinScaleDenominator(Double value) {
        this.minScaleDenominator = value;
    }

    /**
     * Gets the value of the maxScaleDenominator property.
     * 
     */
    public Double getMaxScaleDenominator() {
        return maxScaleDenominator;
    }

    /**
     * Sets the value of the maxScaleDenominator property.
     * 
     */
    public void setMaxScaleDenominator(Double value) {
        this.maxScaleDenominator = value;
    }

    /**
     * Gets the value of the symbolizer property.
     * 
     */
    public List<JAXBElement<? extends SymbolizerType>> getSymbolizer() {
        if (symbolizer == null) {
            symbolizer = new ArrayList<JAXBElement<? extends SymbolizerType>>();
        }
        return this.symbolizer;
    }

}
