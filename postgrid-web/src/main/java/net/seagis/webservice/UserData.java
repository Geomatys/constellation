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

package net.seagis.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import net.seagis.wcs.v100.WCSCapabilitiesType;
import net.seagis.wcs.v111.Capabilities;
import net.seagis.wms.v111.WMT_MS_Capabilities;
import net.seagis.wms.v130.WMSCapabilities;


/**
 * This object record the webService metadatas for a specific user.
 * 
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UserData")
@XmlRootElement(name="UserData")
public class UserData {
    
    @XmlElementRefs ({
        @XmlElementRef(name = "WMSCapabilities", namespace = "http://www.opengis.net/wms", type = WMSCapabilities.class),
        @XmlElementRef(name = "WMT_MS_Capabilities", namespace = "http://www.opengis.net/wms", type = WMT_MS_Capabilities.class)
    })
    private Object[] WMSCapabilities;
    
    @XmlElementRefs ({
        @XmlElementRef(name = "WCS_Capabilities", namespace = "http://www.opengis.net/wcs", type = WCSCapabilitiesType.class),
        @XmlElementRef(name = "Capabilities", namespace = "http://www.opengis.net/wcs/1.1.1", type = Capabilities.class)
    })
    private Object[] WCSCapabilities;
    
    @XmlElementRef(name= "Capabilities", namespace="http://www.opengis.net/cat/csw/2.0.2", type = net.seagis.cat.csw.Capabilities.class)
    private Object[] CSWCapabilities;
    
    private Object[] SOSCapabilities;
    
    public UserData() {
        
    }

    public Object[] getWMSCapabilities() {
        return WMSCapabilities;
    }

    public void setWMSCapabilities(Object[] WMSCapabilities) {
        this.WMSCapabilities = WMSCapabilities;
    }

    public Object[] getWCSCapabilities() {
        return WCSCapabilities;
    }

    public void setWCSCapabilities(Object[] WCSCapabilities) {
        this.WCSCapabilities = WCSCapabilities;
    }

    public Object[] getCSWCapabilities() {
        return CSWCapabilities;
    }

    public void setCSWCapabilities(Object[] CSWCapabilities) {
        this.CSWCapabilities = CSWCapabilities;
    }

    public Object[] getSOSCapabilities() {
        return SOSCapabilities;
    }

    public void setSOSCapabilities(Object[] SOSCapabilities) {
        this.SOSCapabilities = SOSCapabilities;
    }
    
    
    
    

}
