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

import org.constellation.ServiceDef.Specification;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.Instance;
import org.constellation.configuration.InstanceReport;
import org.constellation.dto.DataInformation;
import org.constellation.dto.Restart;
import org.constellation.dto.Service;
import org.constellation.dto.StyleListBean;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
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
public class  ServicesManager {

    private static final Logger LOGGER = Logger.getLogger(ServicesManager.class.getName());

    /**
     * Constellation manager used to communicate with the Constellation server.
     */
    @Inject
    private ConstellationService cstl;

    /**
     * Creates a new service instance with specified {@link org.constellation.dto.Service} metadata.
     *
     * @param metadata    the service metadata
     * @param serviceType the service type (WMS, CSW, WPS...)
     * @return {@code true} on success, {@code false} on fail
     * @throws IOException on HTTP communication error or response entity parsing error
     */
    public boolean createServices(final Service metadata, final Specification serviceType) throws IOException {
        final AcknowlegementType response = cstl.openClient().services.newInstance(serviceType, metadata);
        return "success".equalsIgnoreCase(response.getStatus());
    }

    /**
     * Loads a service metadata.
     *
     * @param serviceId   the service identifier
     * @param serviceType the service type (WMS, CSW, WPS...)
     * @return the {@link org.constellation.dto.Service} instance
     */
    public Service getMetadata(final String serviceId, final Specification serviceType) throws IOException {
        return cstl.openClient().services.getMetadata(serviceType, serviceId);
    }

    /**
     * Configures an existing service metadata.
     *
     * @param metadata    the service metadata
     * @param serviceType the service type (WMS, CSW, WPS...)
     * @return {@code true} on success, otherwise {@code false}
     */
    public boolean setMetadata(final Service metadata, final Specification serviceType) throws IOException {
        final AcknowlegementType response = cstl.openClient().services.setMetadata(serviceType, metadata);
        return "success".equalsIgnoreCase(response.getStatus());
    }

    /**
     * Gets and returns a service {@link Instance}.
     *
     * @param serviceId   the service identifier
     * @param serviceType the service type (WMS, CSW, WPS...)
     * @return an {@link Instance} instance
     */
    public Instance getInstance(final String serviceId, final Specification serviceType) throws IOException {
        return cstl.openClient().services.getInstance(serviceType, serviceId);
    }

    /**
     * Restarts a service.
     *
     * @param serviceId   the service identifier
     * @param serviceType the service type (WMS, CSW, WPS...)
     * @return {@code true} on success, otherwise {@code false}
     */
    public boolean restartService(final String serviceId, final Specification serviceType) throws IOException {
        final Restart restart = new Restart();
        restart.setForced(true);
        restart.setCloseFirst(true);
        final AcknowlegementType response = cstl.openClient().services.restart(serviceType, serviceId, restart);
        return "success".equalsIgnoreCase(response.getStatus());
    }

    /**
     * Stops a service.
     *
     * @param serviceId   the service identifier
     * @param serviceType the service type (WMS, CSW, WPS...)
     * @return {@code true} on success, otherwise {@code false}
     */
    public boolean stopService(final String serviceId, final Specification serviceType) throws IOException {
        final AcknowlegementType response = cstl.openClient().services.stop(serviceType, serviceId);
        return "success".equalsIgnoreCase(response.getStatus());
    }

    /**
     * Starts a service.
     *
     * @param serviceId   the service identifier
     * @param serviceType the service type (WMS, CSW, WPS...)
     * @return {@code true} on success, otherwise {@code false}
     */
    public boolean startService(final String serviceId, final Specification serviceType) throws IOException {
        final AcknowlegementType response = cstl.openClient().services.start(serviceType, serviceId);
        return "success".equalsIgnoreCase(response.getStatus());
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

            // Build service capabilities URL.
            String capabilitiesUrl = cstl.getUrl() + "WS/"+instance.getType().toLowerCase()+"/" + instance.getName() +"?REQUEST=GetCapabilities&SERVICE="+instance.getType().toUpperCase();


            if (instance.getVersions().size()>0) {
                double version=0;
                String selectedVersion = "";

                for (String currentVersion : instance.getVersions()) {
                    double testedVersion = Double.parseDouble(currentVersion.replace(".", ""));

                    if(testedVersion>version){
                        version= testedVersion;
                        selectedVersion = currentVersion;
                    }
                }
                capabilitiesUrl += "&VERSION=" + selectedVersion;
            }
            instanceSum.setCapabilitiesUrl(capabilitiesUrl);

            instancesSummary.add(instanceSum);
        }

        return instancesSummary;
    }

    public DataInformation uploadToServer(File newFile, String name, String dataType) {
        return cstl.openServer().providers.uploadData(newFile, name, dataType);
    }

    public StyleListBean getStyleList() {
        try {
            return cstl.openClient().providers.getStyleList();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error on message receive", e);
        }
        return null;
    }
}
