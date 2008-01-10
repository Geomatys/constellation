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


package net.seagis.wcs;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * 
 * Metadata for a WCS server, also known as Capabilities document. 
 * Reply from a WCS that performed the GetCapabilities operation. 
 * 
 * WCS version 1.0.0 
 * 
 * <p>Java class for WCS_CapabilitiesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="WCS_CapabilitiesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/wcs}Service"/>
 *         &lt;element ref="{http://www.opengis.net/wcs}Capability"/>
 *         &lt;element ref="{http://www.opengis.net/wcs}ContentMetadata"/>
 *       &lt;/sequence>
 *       &lt;attribute name="version" use="required" type="{http://www.w3.org/2001/XMLSchema}string" fixed="1.0.0" />
 *       &lt;attribute name="updateSequence" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "service",
    "capability",
    "contentMetadata"
})
@XmlRootElement(name="WCS_Capabilities")        
public class WCSCapabilitiesType {

    @XmlElement(name = "Service", required = true)
    private ServiceType service;
    @XmlElement(name = "Capability", required = true)
    private WCSCapabilityType capability;
    @XmlElement(name = "ContentMetadata", required = true)
    private ContentMetadata contentMetadata;
    @XmlAttribute(required = true)
    private String version;
    @XmlAttribute
    private String updateSequence;

    /**
     * Gets the value of the service property.
     */
    public ServiceType getService() {
        return service;
    }
    
    /**
     * Gets the value of the service property.
     */
    public void setService(ServiceType service) {
        this.service = service;
    }

    /**
     * Gets the value of the capability property.
     * 
    */
    public WCSCapabilityType getCapability() {
        return capability;
    }

    /**
     * Gets the value of the contentMetadata property.
     */
    public ContentMetadata getContentMetadata() {
        return contentMetadata;
    }

    /**
     * Sets the value of the contentMetadata property.
     */
    public void setContentMetadata(ContentMetadata value) {
        this.contentMetadata = value;
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

    /**
     * Gets the value of the updateSequence property.
     */
    public String getUpdateSequence() {
        return updateSequence;
    }
}
