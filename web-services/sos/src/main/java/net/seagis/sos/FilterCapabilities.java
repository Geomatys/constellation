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


package net.seagis.sos;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import net.seagis.ogc.IdCapabilitiesType;
import net.seagis.ogc.ScalarCapabilitiesType;
import net.seagis.ogc.SpatialCapabilitiesType;
import net.seagis.ogc.TemporalCapabilitiesType;


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
 *         &lt;element ref="{http://www.opengis.net/ogc}Spatial_Capabilities"/>
 *         &lt;element ref="{http://www.opengis.net/ogc}Temporal_Capabilities"/>
 *         &lt;element ref="{http://www.opengis.net/ogc}Scalar_Capabilities"/>
 *         &lt;element ref="{http://www.opengis.net/ogc}Id_Capabilities"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "spatialCapabilities",
    "temporalCapabilities",
    "scalarCapabilities",
    "idCapabilities"
})
@XmlRootElement(name = "Filter_Capabilities")
public class FilterCapabilities {

    @XmlElement(name = "Spatial_Capabilities", namespace = "http://www.opengis.net/ogc", required = true)
    private SpatialCapabilitiesType spatialCapabilities;
    @XmlElement(name = "Temporal_Capabilities", namespace = "http://www.opengis.net/ogc", required = true)
    private TemporalCapabilitiesType temporalCapabilities;
    @XmlElement(name = "Scalar_Capabilities", namespace = "http://www.opengis.net/ogc", required = true)
    private ScalarCapabilitiesType scalarCapabilities;
    @XmlElement(name = "Id_Capabilities", namespace = "http://www.opengis.net/ogc", required = true)
    private IdCapabilitiesType idCapabilities;

    /**
     * An empty constructor used by JAXB
     */
    FilterCapabilities(){
        
    }
    
    /**
     * Gets the value of the spatialCapabilities property.
     */
    public SpatialCapabilitiesType getSpatialCapabilities() {
        return spatialCapabilities;
    }

    /**
     * Gets the value of the temporalCapabilities property.
     */
    public TemporalCapabilitiesType getTemporalCapabilities() {
        return temporalCapabilities;
    }

    /**
     * Gets the value of the scalarCapabilities property.
     */
    public ScalarCapabilitiesType getScalarCapabilities() {
        return scalarCapabilities;
    }

    /**
     * Gets the value of the idCapabilities property.
     */
    public IdCapabilitiesType getIdCapabilities() {
        return idCapabilities;
    }
}
