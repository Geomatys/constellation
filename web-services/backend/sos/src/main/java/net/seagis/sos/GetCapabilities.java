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
package net.seagis.sos;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import net.seagis.ows.v110.AcceptFormatsType;
import net.seagis.ows.v110.AcceptVersionsType;
import net.seagis.ows.v110.GetCapabilitiesType;
import net.seagis.ows.v110.SectionsType;
import org.geotools.resources.Utilities;


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
 *       &lt;attribute name="service" use="required" type="{http://www.opengis.net/ows/1.1}ServiceType" fixed="SOS" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetCapabilities")
@XmlRootElement(name = "GetCapabilities")
public class GetCapabilities extends GetCapabilitiesType {

    @XmlAttribute(required = true)
    private String service;

    /**
     * Construct a new request often directly from a XML file received by the SOS server.
     * this constructor is only used by JaxB.
     */
    GetCapabilities() {
    
    }
    
    /**
     * Build a new getCapabilities request with the specified service
     */
    public  GetCapabilities(AcceptVersionsType acceptVersions, SectionsType sections,
            AcceptFormatsType acceptFormats, String updateSequence, String service) {
        super(acceptVersions, sections, acceptFormats, updateSequence);
        this.service = service;
    }
    
    /**
     * Return the value of the service property (often "SOS").
     * 
     * @return 
     */
    public String getService() {
        if (service == null) {
            return "SOS";
        } else {
            return service;
        }
    }

    /**
     * Verify if this entry is identical to the specified object.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final GetCapabilities that = (GetCapabilities) object;
            return Utilities.equals(this.service, that.service);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + (this.service != null ? this.service.hashCode() : 0);
        return hash;
    }
    

}
