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

import org.constellation.configuration.Instance;
import org.constellation.configuration.InstanceReport;
import org.constellation.configuration.LayerList;
import org.constellation.dto.Service;
import org.constellation.dto.StyleListBean;
import org.constellation.ws.rest.post.DataInformation;

import javax.inject.Inject;
import java.io.File;
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

    /**
     * Constellation manager used to communicate with the Constellation server.
     */
    @Inject
    private ConstellationService cstl;

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
            return cstl.openServer().services.newInstance(service, createdService);
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

        InstanceReport report = cstl.openServer().services.listInstance();
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
            instanceSum.setType(instance.getType().toLowerCase());
            instancesSummary.add(instanceSum);
        }

        return instancesSummary;
    }

    public LayerList getLayers(final String serviceName, final String serviceType){
        return cstl.openServer().services.getLayers(serviceType, serviceName);
    }

    public DataInformation uploadToServer(File newFile, String name, String dataType) {
        return cstl.openServer().providers.uploadData(newFile, name, dataType);
    }

    public StyleListBean getStyleList(){
        return cstl.openClient().getStyleList();
    }
}
