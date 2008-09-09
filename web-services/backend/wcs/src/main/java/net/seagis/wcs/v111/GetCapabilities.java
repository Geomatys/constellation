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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import net.seagis.ows.v110.AcceptFormatsType;
import net.seagis.ows.v110.AcceptVersionsType;
import net.seagis.ows.v110.GetCapabilitiesType;
import net.seagis.ows.v110.SectionsType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/ows/1.1}GetCapabilitiesType">
 *       &lt;sequence>
 *       &lt;/sequence>
 *       &lt;attribute name="service" use="required" type="{http://www.opengis.net/ows/1.1}ServiceType" fixed="WCS" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "GetCapabilities")
public class GetCapabilities extends GetCapabilitiesType {

    @XmlAttribute(required = true)
    private String service;

    /**
     * An empty constructor used by JAXB
     */
    GetCapabilities(){
    }
    
    
     /**
     * Build a new getCapabilities request version 1.1.1.
     */
    public GetCapabilities(AcceptVersionsType acceptVersions, SectionsType sections,
            AcceptFormatsType acceptFormats, String updateSequence){
        super (acceptVersions, sections, acceptFormats, updateSequence);
        this.service        = "WCS";
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
}
