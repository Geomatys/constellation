/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.gui.service;

import org.apache.sis.util.logging.Logging;
import org.constellation.ServiceDef.Specification;
import org.constellation.configuration.AbstractConfigurationObject;
import org.constellation.configuration.Instance;
import org.constellation.configuration.InstanceReport;
import org.constellation.dto.Service;
import org.constellation.dto.StyleListBrief;

import javax.inject.Inject;
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

    private static final Logger LOGGER = Logging.getLogger(ServicesManager.class);

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
     * Gets and returns a service configuration.
     *
     * @param serviceId   the service identifier
     * @param serviceType the service type (WMS, CSW, WPS...)
     * @return the service configuration.
     * @throws IOException if the operation has failed
     */
    public Object getInstanceConfiguration(final String serviceId, final Specification serviceType) throws IOException {
        return cstl.openClient().services.getInstanceConfiguration(serviceType, serviceId);
    }

    /**
     * Gets and returns a service configuration.
     *
     * @param serviceId   the service identifier
     * @param serviceType the service type (WMS, CSW, WPS...)
     * @param config      the service configuration object
     * @throws IOException if the operation has failed
     */
    public void setInstanceConfiguration(final String serviceId, final Specification serviceType, final AbstractConfigurationObject config) throws IOException {
        cstl.openClient().services.setInstanceConfiguration(serviceType, serviceId, config);
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
            instanceSum.setIdentifier(instance.getIdentifier());
            instanceSum.setStatus(instance.getStatus().toString());
            instanceSum.setType(instance.getType().toLowerCase());
            buildServiceUrl(instance.getType(), instance.getIdentifier(), instance.getVersions(), instanceSum);


            instancesSummary.add(instanceSum);
        }

        return instancesSummary;
    }

    public void buildServiceUrl(final String type, final String identifier, final List<String> versions, final InstanceSummary instanceSum) {
        // Build service capabilities URL.
        String capabilitiesUrl = cstl.getUrl() + "/WS/"+type.toLowerCase()+"/" + identifier +"?REQUEST=GetCapabilities&SERVICE="+type.toUpperCase();
        // Build service URL for logs.
        String logsURL = cstl.getUrl() + "/api/1/log/"+type.toLowerCase()+"/" + identifier;


        if (versions!= null && versions.size()>0) {
            double version=0;
            String selectedVersion = "";

            for (String currentVersion : versions) {
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
    }

    public StyleListBrief getStyleList() {
        try {
            return cstl.openClient().providers.getStyles("");
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error on message receive", e);
        }
        return null;
    }


}
