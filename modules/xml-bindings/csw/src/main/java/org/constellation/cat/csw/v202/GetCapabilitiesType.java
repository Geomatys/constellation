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
package org.constellation.cat.csw.v202;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import org.constellation.cat.csw.GetCapabilities;
import org.constellation.ows.v100.AcceptFormatsType;
import org.constellation.ows.v100.AcceptVersionsType;
import org.constellation.ows.v100.SectionsType;


/**
 * 
 * Request for a description of service capabilities. See OGC 05-008 
 * for more information.
 *          
 * 
 * <p>Java class for GetCapabilitiesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetCapabilitiesType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/ows}GetCapabilitiesType">
 *       &lt;attribute name="service" type="{http://www.opengis.net/ows}ServiceType" default="http://www.opengis.net/cat/csw" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "GetCapabilities")
public class GetCapabilitiesType extends org.constellation.ows.v100.GetCapabilitiesType implements GetCapabilities {

    @XmlAttribute
    private String service;
    
    /**
     * An empty constructor used by JAXB
     */
    GetCapabilitiesType() {
    }
    
    /**
     * Build a new getCapabilities request with the specified service
     * 
     * @param acceptVersions The different versions accepted by the client.
     * @param sections The different sections of the capabilities document requested.
     *                 one or more of "ServiceIdentification", "ServiceProvider", "OperationsMetadata", "Filter_Capabilities", "All".
     * @param acceptFormats The different fomat (MIME type) accepted by the client.
     * @param updateSequence not used yet.
     * @param service MUST be CSW.
     */
    public GetCapabilitiesType(AcceptVersionsType acceptVersions, SectionsType sections,
            AcceptFormatsType acceptFormats, String updateSequence, String service) {
        super(acceptVersions, sections, acceptFormats, updateSequence);
        this.service = service;
    }
    

    /**
     * Gets the value of the service property.
     */
    public String getService() {
        if (service == null) {
            return "CSW";
        } else {
            return service;
        }
    }
}
