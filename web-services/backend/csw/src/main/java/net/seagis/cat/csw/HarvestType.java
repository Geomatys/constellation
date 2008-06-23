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


package net.seagis.cat.csw;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.Duration;


/**
 * 
 *          Requests that the catalogue attempt to harvest a resource from some 
 *          network location identified by the source URL.
 * 
 *          Source          - a URL from which the resource is retrieved
 *          ResourceType    - normally a URI that specifies the type of the resource
 *                            (DCMES v1.1) being harvested if it is known.
 *          ResourceFormat  - a media type indicating the format of the 
 *                            resource being harvested.  The default is 
 *                            "application/xml".
 *          ResponseHandler - a reference to some endpoint to which the 
 *                            response shall be forwarded when the
 *                            harvest operation has been completed
 *          HarvestInterval - an interval expressed using the ISO 8601 syntax; 
 *                            it specifies the interval between harvest 
 *                            attempts (e.g., P6M indicates an interval of 
 *                            six months).
 *          
 * 
 * <p>Java class for HarvestType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="HarvestType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/cat/csw/2.0.2}RequestBaseType">
 *       &lt;sequence>
 *         &lt;element name="Source" type="{http://www.w3.org/2001/XMLSchema}anyURI"/>
 *         &lt;element name="ResourceType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="ResourceFormat" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="HarvestInterval" type="{http://www.w3.org/2001/XMLSchema}duration" minOccurs="0"/>
 *         &lt;element name="ResponseHandler" type="{http://www.w3.org/2001/XMLSchema}anyURI" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HarvestType", propOrder = {
    "source",
    "resourceType",
    "resourceFormat",
    "harvestInterval",
    "responseHandler"
})
@XmlRootElement(name = "Harvest")
public class HarvestType extends RequestBaseType {

    @XmlElement(name = "Source", required = true)
    @XmlSchemaType(name = "anyURI")
    private String source;
    @XmlElement(name = "ResourceType", required = true)
    private String resourceType;
    @XmlElement(name = "ResourceFormat", defaultValue = "application/xml")
    private String resourceFormat;
    @XmlElement(name = "HarvestInterval")
    private Duration harvestInterval;
    @XmlElement(name = "ResponseHandler")
    @XmlSchemaType(name = "anyURI")
    private List<String> responseHandler;

    /**
     * An empty constructor used by JAXB
     */
    HarvestType() {
        
    }
    
    /**
     * Build a new harvest request
     */
    public HarvestType(String service, String version, String source, String resourceType, 
            String resourceFormat, String handler, Duration harvestInterval) {
        super(service, version);
        this.source          = source;
        this.resourceType    = resourceType;
        this.resourceFormat  = resourceFormat;
        this.responseHandler = new ArrayList<String>();
        this.responseHandler.add(handler);
        this.harvestInterval = harvestInterval;
        
    }
    
    /**
     * Gets the value of the source property.
     */
    public String getSource() {
        return source;
    }

    /**
     * Gets the value of the resourceType property.
     */
    public String getResourceType() {
        return resourceType;
    }

    /**
     * Gets the value of the resourceFormat property.
     */
    public String getResourceFormat() {
        return resourceFormat;
    }

    
    /**
     * Gets the value of the harvestInterval property.
     */
    public Duration getHarvestInterval() {
        return harvestInterval;
    }

    /**
     * Gets the value of the responseHandler property.
     * (unmodifiable)
     */
    public List<String> getResponseHandler() {
        if (responseHandler == null) {
            responseHandler = new ArrayList<String>();
        }
        return Collections.unmodifiableList(responseHandler);
    }

}
