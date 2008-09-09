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
package net.seagis.wcs.v111;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import net.seagis.ows.v110.CodeType;
import net.seagis.wcs.AbstractGetCoverage;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/wcs/1.1.1}RequestBaseType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/ows/1.1}Identifier"/>
 *         &lt;element name="DomainSubset" type="{http://www.opengis.net/wcs/1.1.1}DomainSubsetType"/>
 *         &lt;element name="RangeSubset" type="{http://www.opengis.net/wcs/1.1.1}RangeSubsetType" minOccurs="0"/>
 *         &lt;element name="Output" type="{http://www.opengis.net/wcs/1.1.1}OutputType"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "identifier",
    "domainSubset",
    "rangeSubset",
    "output"
})
@XmlRootElement(name = "GetCoverage")
public class GetCoverage extends AbstractGetCoverage {

    @XmlAttribute(required = true)
    private String service;
    @XmlAttribute(required = true)
    private String version;
    @XmlElement(name = "Identifier", namespace = "http://www.opengis.net/ows/1.1", required = true)
    private CodeType identifier;
    @XmlElement(name = "DomainSubset", required = true)
    private DomainSubsetType domainSubset;
    @XmlElement(name = "RangeSubset")
    private RangeSubsetType rangeSubset;
    @XmlElement(name = "Output", required = true)
    private OutputType output;

     /**
     * Empty constructor used by JAXB.
     */
    GetCoverage() {
    }
    
    /**
     * Build a new GetCoverage request (1.1.1)
     */
    public GetCoverage(CodeType identifier, DomainSubsetType domainSubset, 
            RangeSubsetType rangeSubset, OutputType output) {
        
        this.domainSubset        = domainSubset;
        this.output              = output;
        this.rangeSubset         = rangeSubset;
        this.service             = "WCS";
        this.identifier          = identifier;
        this.version             = "1.1.1";
        
    }
    
    /**
     * Identifier of the coverage that this GetCoverage operation request shall draw from. 
     */
    public CodeType getIdentifier() {
        return identifier;
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
     * Gets the value of the output property.
     */
    public OutputType getOutput() {
        return output;
    }

    @Override
    public String getVersion() {
        return version;
    }
}
