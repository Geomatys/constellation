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
import net.seagis.ows.AcceptFormatsType;
import net.seagis.ows.AcceptVersionsType;
import net.seagis.ows.SectionsType;


/**
 * <p>An xml binding class for a getCapabilities request.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="section" type="{http://www.opengis.net/wcs}CapabilitiesSectionType" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="service" use="required" type="{http://www.w3.org/2001/XMLSchema}string" fixed="WCS" />
 *       &lt;attribute name="version" type="{http://www.w3.org/2001/XMLSchema}string" fixed="1.0.0" />
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
    "section"
})
@XmlRootElement(name = "GetCapabilities")
public class GetCapabilities extends AbstractRequest {
    
    /*
     * 1.0.0 attribute
     */  
    @XmlElement(defaultValue = "/")
    private String section;
    
    /*
     *  1.1.1 attribute
     */ 
    @XmlElement(name = "AcceptVersions")
    private AcceptVersionsType acceptVersions;
    @XmlElement(name = "Sections")
    private SectionsType sections;
    @XmlElement(name = "AcceptFormats")
    private AcceptFormatsType acceptFormats;
    
    /*
     * Both version attribute 
     */
    @XmlAttribute(required = true)
    private String service;
    @XmlAttribute
    private String version;
    @XmlAttribute
    private String updateSequence;

    /**
     * An empty constructor used by JAXB
     */
    GetCapabilities(){
    }
    
    /**
     * Build a new getCapabilities request version 1.0.0.
     */
    public GetCapabilities(String version, String section, String updateSequence){
        this.version        = version;
        this.updateSequence = updateSequence;
        if (section == null) {
            section = "/";
        } else {
            this.section        = section;
        }
        this.service        = "WCS";
    }
    
     /**
     * Build a new getCapabilities request version 1.1.1.
     */
    public GetCapabilities(String version, AcceptVersionsType acceptVersions, SectionsType sections,
            AcceptFormatsType acceptFormats, String updateSequence){
        this.version        = version;
        this.updateSequence = updateSequence;
        this.acceptFormats  = acceptFormats;
        this.acceptVersions = acceptVersions;
        this.sections       = sections;
        this.service        = "WCS";
    }
    
    /**
     * return the requested section.
     * 
     * values possible in WCS 1.0.0:
     *  - /
     *  - /WCS_Capabilities/Service
     *  - /WCS_Capabilities/Capability
     *  - /WCS_Capabilities/ContentMetadata
     * 
     */
    public String getSection() {
        return section;
    }

    /**
     * Returns the service name always WCS here.
     */
    public String getService() {
        return "WCS";
    }

    /**
     * Return the version of the service.
     */
    public String getVersion() {
        return version;
    }
    
    /**
     * Return the Accepted version of the service.
     */
    public AcceptVersionsType getAcceptVersions() {
        return acceptVersions;
    }
    
     /**
     * return the requested section.
     * 
     * values possible in WCS 1.1.1:
     *  - ServiceIdentification
     *  - ServiceProvider
     *  - OperationsMetadata
     *  - Contents
     * 
     */
    public SectionsType geSections() {
        return sections;
    }
    
    /**
     * Return the Accepted format of the service.
     */
    public AcceptFormatsType getAcceptFormats() {
        return acceptFormats;
    }

        
    /**
     * Gets the value of the updateSequence property.
     */
    public String getUpdateSequence() {
        return updateSequence;
    }
}
