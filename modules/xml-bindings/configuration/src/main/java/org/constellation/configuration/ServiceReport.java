/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
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

package org.constellation.configuration;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Create a report about the available services.
 * 
 * @author Guilhem Legal (Geomatys)
 * @since 0.7
 */
@XmlRootElement(name ="ServiceReport")
@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceReport {
    
    private List<String> availableServices;
    
    public ServiceReport() {
        
    }
    
    public ServiceReport(final List<String> availableServices) {
        this.availableServices = availableServices;
    }

    /**
     * @return the availableServices
     */
    public List<String> getAvailableServices() {
        return availableServices;
    }

    /**
     * @param availableServices the availableServices to set
     */
    public void setAvailableServices(List<String> availableServices) {
        this.availableServices = availableServices;
    }
}
