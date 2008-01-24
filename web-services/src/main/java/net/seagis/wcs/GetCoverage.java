/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
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


package net.seagis.wcs;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>An xml binding class for a getCoverage request.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="sourceCoverage" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="domainSubset" type="{http://www.opengis.net/wcs}DomainSubsetType"/>
 *         &lt;element name="rangeSubset" type="{http://www.opengis.net/wcs}RangeSubsetType" minOccurs="0"/>
 *         &lt;element ref="{http://www.opengis.net/wcs}interpolationMethod" minOccurs="0"/>
 *         &lt;element name="output" type="{http://www.opengis.net/wcs}OutputType"/>
 *       &lt;/sequence>
 *       &lt;attribute name="service" use="required" type="{http://www.w3.org/2001/XMLSchema}string" fixed="WCS" />
 *       &lt;attribute name="version" use="required" type="{http://www.w3.org/2001/XMLSchema}string" fixed="1.0.0" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "sourceCoverage",
    "domainSubset",
    "rangeSubset",
    "interpolationMethod",
    "output"
})
@XmlRootElement(name = "GetCoverage")
public class GetCoverage extends AbstractRequest {

    @XmlElement(required = true)
    private String sourceCoverage;
    @XmlElement(required = true)
    private DomainSubsetType domainSubset;
    private RangeSubsetType rangeSubset;
    private InterpolationMethodType interpolationMethod;
    @XmlElement(required = true)
    private OutputType output;
    @XmlAttribute(required = true)
    private String service;
    @XmlAttribute(required = true)
    private String version;

    /**
     * Gets the value of the sourceCoverage property.
     */
    public String getSourceCoverage() {
        return sourceCoverage;
    }

    /**
     * Gets the value of the domainSubset property.
     */
    public DomainSubsetType getDomainSubset() {
        return domainSubset;
    }

    /**
     * Gets the value of the rangeSubset property.
     */
    public RangeSubsetType getRangeSubset() {
        return rangeSubset;
    }

    /**
     * Spatial interpolation method to be used in resampling data from its original
     * form to the requested CRS and/or grid size. 
     * Method shall be among those listed for the requested coverage in the DescribeCoverage response.
     */
    public InterpolationMethodType getInterpolationMethod() {
        return interpolationMethod;
    }

    /**
     * Gets the value of the output property.
     */
    public OutputType getOutput() {
        return output;
    }

    /**
     * Gets the value of the service property.
     */
    public String getService() {
        if (service == null) {
            return "WCS";
        } else {
            return service;
        }
    }

    /**
     * Gets the value of the version property.
     */
    public String getVersion() {
        if (version == null) {
            return "1.0.0";
        } else {
            return version;
        }
    }
}
