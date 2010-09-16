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
package org.constellation.util;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.geotoolkit.wcs.xml.v100.WCSCapabilitiesType;
import org.geotoolkit.wcs.xml.v111.Capabilities;
import org.geotoolkit.wms.xml.v111.WMT_MS_Capabilities;
import org.geotoolkit.wms.xml.v130.WMSCapabilities;


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
    private Object[] wmsCapabilities;
    
    @XmlElementRefs ({
        @XmlElementRef(name = "WCS_Capabilities", namespace = "http://www.opengis.net/wcs", type = WCSCapabilitiesType.class),
        @XmlElementRef(name = "Capabilities", namespace = "http://www.opengis.net/wcs/1.1.1", type = Capabilities.class)
    })
    private Object[] wcsCapabilities;
    
    @XmlElementRef(name= "Capabilities", namespace="http://www.opengis.net/cat/csw/2.0.2", type = org.geotoolkit.csw.xml.v202.Capabilities.class)
    private Object[] cswCapabilities;
    
    private Object[] sosCapabilities;
    
    public Object[] getWMSCapabilities() {
        return wmsCapabilities.clone();
    }

    public void setWMSCapabilities(Object[] wmsCapabilities) {
        this.wmsCapabilities = wmsCapabilities.clone();
    }

    public Object[] getWCSCapabilities() {
        return wcsCapabilities.clone();
    }

    public void setWCSCapabilities(Object[] wcsCapabilities) {
        this.wcsCapabilities = wcsCapabilities.clone();
    }

    public Object[] getCSWCapabilities() {
        return cswCapabilities.clone();
    }

    public void setCSWCapabilities(Object[] cswCapabilities) {
        this.cswCapabilities = cswCapabilities.clone();
    }

    public Object[] getSOSCapabilities() {
        return sosCapabilities.clone();
    }

    public void setSOSCapabilities(Object[] sosCapabilities) {
        this.sosCapabilities = sosCapabilities.clone();
    }
    
    
    
    

}
