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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
    
    private List<ServiceProtocol> availableServices = new ArrayList<ServiceProtocol>();
    
    public ServiceReport() {
        
    }
    
    public ServiceReport(final Map<String, List<String>> availableServices) {
        setAvailableServices(availableServices);
    }

    /**
     * @return the availableServices
     */
    public Map<String, List<String>> getAvailableServices() {
        final Map<String, List<String>> response = new HashMap<String, List<String>>();
        for (ServiceProtocol sp : availableServices) {
            response.put(sp.getName(), sp.getProtocol());
        }
        return response;
    }

    /**
     * @param availableServices the availableServices to set
     */
    public final void setAvailableServices(final Map<String, List<String>> availableServices) {
        if (availableServices != null) {
            for (Entry<String, List<String>> entry : availableServices.entrySet()) {
                this.availableServices.add(new ServiceProtocol(entry.getKey(), entry.getValue()));
            }
        }
    }
}
