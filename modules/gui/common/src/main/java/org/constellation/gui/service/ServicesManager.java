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
                URL serverUrl = new URL("http://localhost:8090/constellation/api/1/");
                ConstellationServer cs = new ConstellationServer(serverUrl, "admin", "admin");
                return cs.services.newInstance(service, createdService);
            } catch (MalformedURLException e) {
                LOGGER.log(Level.WARNING, "error on url", e);
            }
        }
        return false;
    }

    /**
     * Get all service List
     *
     * @return A {@link Instance} {@link List} to display this.
     */
    public List<InstanceSummary> getServiceList() {
        List<InstanceSummary> instancesSummary = new ArrayList<InstanceSummary>(0);

        try {
            URL serverUrl = new URL("http://localhost:8090/constellation/api/1/");
            ConstellationServer cs = new ConstellationServer(serverUrl, "admin", "admin");
            InstanceReport report = cs.services.listInstance();
            //map server side object on client side object
            for (Instance instance : report.getInstances()) {
                InstanceSummary instanceSum = new InstanceSummary();
                if(instance.get_abstract().isEmpty()){
                    instanceSum.set_abstract("-");
                }else{
                    instanceSum.set_abstract(instance.get_abstract());
                }
                instanceSum.setLayersNumber(instance.getLayersNumber());
                instanceSum.setName(instance.getName());
                instanceSum.setStatus(instance.getStatus().toString());
                instanceSum.setType(instance.getType());
                instancesSummary.add(instanceSum);
            }
        } catch (MalformedURLException e) {
            LOGGER.log(Level.WARNING, "", e);
        }

        return instancesSummary;
    }


//    public static Object getService(final String identifier, final String type, final List<String> versions) {
//        // TODO get full service data
//        String s = type.toLowerCase();
//        if (s.equals("wms")) {
//            if(LOGGER.isLoggable(Level.INFO)){
//                LOGGER.log(Level.INFO, "on WMS service");
//            }
//            return askWMSCapabilities(identifier, versions);
//
//        } else {
//            System.out.println("do nothing");
//        }
//        return null;
//    }
//
//    /**
//     * return an object to know main service informations
//     *
//     * @param identifier service identifier
//     * @param versions service version
//     * @return an {@link AbstractWMSCapabilities}
//     */
//    private static AbstractWMSCapabilities askWMSCapabilities(final String identifier, final List<String> versions) {
//        //TODO : 1) get metadatas
//        //TODO : 2) get layers
//
//        return null;
//    }
}
