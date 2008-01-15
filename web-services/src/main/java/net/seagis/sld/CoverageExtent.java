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


package net.seagis.sld;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element ref="{http://www.opengis.net/sld}RangeAxis" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/sld}TimePeriod" minOccurs="0"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "rangeAxis",
    "timePeriod"
})
@XmlRootElement(name = "CoverageExtent")
public class CoverageExtent {

    @XmlElement(name = "RangeAxis")
    private List<RangeAxis> rangeAxis;
    @XmlElement(name = "TimePeriod")
    private String timePeriod;

    /**
     * Empty Constructor used by JAXB.
     */
    CoverageExtent() {
        
    }
    
    /**
     * Build a new Coverage extent with the specified range axis.
     */
    public CoverageExtent(List<RangeAxis> rangeAxis) {
        this.rangeAxis = rangeAxis;
    }
    
     /**
     * Build a new Coverage extent with the specified time period.
     */
    public CoverageExtent(String timePeriod) {
        this.timePeriod = timePeriod;
    }
    
    /**
     * Gets the value of the rangeAxis property.
     */
    public List<RangeAxis> getRangeAxis() {
        if (rangeAxis == null) {
            rangeAxis = new ArrayList<RangeAxis>();
        }
        return Collections.unmodifiableList(rangeAxis);
    }

    /**
     * Gets the value of the timePeriod property.
     */
    public String getTimePeriod() {
        return timePeriod;
    }
}
