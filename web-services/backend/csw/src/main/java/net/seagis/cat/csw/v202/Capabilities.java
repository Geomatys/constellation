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
package net.seagis.cat.csw.v202;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import net.seagis.ogc.FilterCapabilities;
import net.seagis.ows.v100.CapabilitiesBaseType;
import net.seagis.ows.v100.OperationsMetadata;
import net.seagis.ows.v100.ServiceIdentification;
import net.seagis.ows.v100.ServiceProvider;


/**
 * This type extends ows:CapabilitiesBaseType defined in OGC-05-008 
 *          to include information about supported OGC filter components. A 
 *          profile may extend this type to describe additional capabilities.
 * 
 * <p>Java class for CapabilitiesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CapabilitiesType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/ows}CapabilitiesBaseType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/ogc}Filter_Capabilities"/>
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
    "filterCapabilities"
})
@XmlRootElement(name="Capabilities")
public class Capabilities extends CapabilitiesBaseType {

    @XmlElement(name = "Filter_Capabilities", namespace = "http://www.opengis.net/ogc", required = true)
    private FilterCapabilities filterCapabilities;

    /**
     * An empty constructor used by JAXB
     */
    Capabilities(){
    }
    
    /**
     * Build a new Capabilities document
     */
    public Capabilities(ServiceIdentification serviceIdentification, ServiceProvider serviceProvider,
            OperationsMetadata operationsMetadata, String version, String updateSequence, FilterCapabilities filterCapabilities){
        super(serviceIdentification, serviceProvider, operationsMetadata, version, updateSequence);
            this.filterCapabilities = filterCapabilities;
    }
    
    /**
     * Gets the value of the filterCapabilities property.
     */
    public FilterCapabilities getFilterCapabilities() {
        return filterCapabilities;
    }
}
