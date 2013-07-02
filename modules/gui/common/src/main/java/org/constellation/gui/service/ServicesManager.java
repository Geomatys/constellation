/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2012, Geomatys
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
package org.constellation.gui.service;

import org.constellation.admin.service.ConstellationServer;
import org.constellation.configuration.Instance;
import org.constellation.configuration.InstanceReport;
import org.constellation.dto.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Juzu service to call constellation services server side
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 */
public class ServicesManager {

    private static final Logger LOGGER = Logger.getLogger(ServicesManager.class.getName());

    public ServicesManager() {
    }

    /**
     * create service with {@link Service} capabilities information
     *
     * @param createdService {@link Service} object which contain capability service information
     * @param service        service type as {@link String}
     * @return <code>true</code> if succeded, <code>false</code> if not succeded
     */
    public boolean createServices(Service createdService, String service) {
        if (createdService != null) {
            LOGGER.log(Level.INFO, "service will be created : " + createdService.getName());
            try {
                URL serverUrl = new URL("http://localhost:8090/constellation/services");
                ConstellationServer cs = new ConstellationServer(serverUrl, "admin", "admin");
                return cs.services.newInstance(service, createdService);
            } catch (MalformedURLException e) {
                LOGGER.log(Level.WARNING, "error on url", e);
            }
        }
        return false;
    }

    public List<ServiceSummary> getServiceList() {
        final List<ServiceSummary> serviceSummary = new ArrayList<ServiceSummary>(0);

        try {
            //TODO get availables services.
            URL serverUrl = new URL("http://localhost:8090/constellation/services");
            ConstellationServer cs = new ConstellationServer(serverUrl, "admin", "admin");
            InstanceReport report =  cs.services.listInstance();
            for (Instance instance : report.getInstances()) {
                ServiceSummary currentSummary = new ServiceSummary();
                currentSummary.setName(instance.getName());
                currentSummary.setType(instance.getType());
                currentSummary.setState(instance.getStatus().toString());
                serviceSummary.add(currentSummary);

            }

        } catch (MalformedURLException e) {
            LOGGER.log(Level.WARNING, "", e);
        }

        //todo for each service, get main information.
        return serviceSummary;
    }
}
