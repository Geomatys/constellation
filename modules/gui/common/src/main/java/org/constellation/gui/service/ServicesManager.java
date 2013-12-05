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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.constellation.ServiceDef.Specification;
import org.constellation.configuration.Instance;
import org.constellation.configuration.InstanceReport;
import org.constellation.dto.Service;
import org.constellation.dto.StyleListBean;

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
     * @throws IOException if the operation has failed
     */
    public void createServices(final Service metadata, final Specification serviceType) throws IOException {
        cstl.openClient().services.newInstance(serviceType, metadata);
    }

    /**
     * Loads a service metadata.
     *
     * @param serviceId   the service identifier
     * @param serviceType the service type (WMS, CSW, WPS...)
     * @return the {@link Service} instance
     */
    public Service getMetadata(final String serviceId, final Specification serviceType) {
        try {
            return cstl.openClient().services.getMetadata(serviceType, serviceId);
        } catch (IOException ex) {
            return new Service();
        }
    }

    /**
     * Configures an existing service metadata.
     *
     * @param metadata    the service metadata
     * @param serviceType the service type (WMS, CSW, WPS...)
     * @throws IOException if the operation has failed
     */
    public void setMetadata(final Service metadata, final Specification serviceType) throws IOException {
        cstl.openClient().services.setMetadata(serviceType, metadata.getIdentifier(), metadata);
    }

    /**
     * Gets and returns a service {@link Instance}.
     *
     * @param serviceId   the service identifier
     * @param serviceType the service type (WMS, CSW, WPS...)
     * @return an {@link Instance} instance
     * @throws IOException if the operation has failed
     */
    public Instance getInstance(final String serviceId, final Specification serviceType) throws IOException {
        return cstl.openClient().services.getInstance(serviceType, serviceId);
    }

    /**
     * Restarts a service.
     *
     * @param serviceId   the service identifier
     * @param serviceType the service type (WMS, CSW, WPS...)
     * @throws IOException if the operation has failed
     */
    public void restartService(final String serviceId, final Specification serviceType) throws IOException {
        cstl.openClient().services.restart(serviceType, serviceId, true);
    }

    /**
     * Deletes a service.
     *
     * @param serviceId   the service identifier
     * @param serviceType the service type (WMS, CSW, WPS...)
     * @throws IOException if the operation has failed
     */
    public void deleteService(final String serviceId, final Specification serviceType) throws IOException {
        cstl.openClient().services.delete(serviceType, serviceId);
    }

    /**
     * Stops a service.
     *
     * @param serviceId   the service identifier
     * @param serviceType the service type (WMS, CSW, WPS...)
     * @throws IOException if the operation has failed
     */
    public void stopService(final String serviceId, final Specification serviceType) throws IOException {
        cstl.openClient().services.stop(serviceType, serviceId);
    }

    /**
     * Starts a service.
     *
     * @param serviceId   the service identifier
     * @param serviceType the service type (WMS, CSW, WPS...)
     * @throws IOException if the operation has failed
     */
    public void startService(final String serviceId, final Specification serviceType) throws IOException {
        cstl.openClient().services.start(serviceType, serviceId);
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
            if(instance.get_abstract() == null || instance.get_abstract().isEmpty()){
                instanceSum.set_abstract("-");
            }else{
                instanceSum.set_abstract(instance.get_abstract());
            }
            instanceSum.setLayersNumber(instance.getLayersNumber() != null ? instance.getLayersNumber() : 0);
            instanceSum.setName(instance.getName());
            instanceSum.setStatus(instance.getStatus().toString());
            instanceSum.setType(instance.getType().toLowerCase());

            // Build service capabilities URL.
            String capabilitiesUrl = cstl.getUrl() + "WS/"+instance.getType().toLowerCase()+"/" + instance.getName() +"?REQUEST=GetCapabilities&SERVICE="+instance.getType().toUpperCase();
            // Build service URL for logs.
            String logsURL = cstl.getUrl() + "api/1/log/"+instance.getType().toLowerCase()+"/" + instance.getName();


            if (instance.getVersions()!= null && instance.getVersions().size()>0) {
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
            instanceSum.setLogsURL(logsURL);
            
            instancesSummary.add(instanceSum);
        }

        return instancesSummary;
    }

    public StyleListBean getStyleList() {
        try {
            return cstl.openClient().providers.getStyles();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error on message receive", e);
        }
        return null;
    }


}
