/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
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
package org.constellation.ws.rs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.configuration.Instance;
import org.constellation.configuration.InstanceReport;
import org.constellation.configuration.Layer;
import org.constellation.configuration.ServiceStatus;
import org.constellation.dto.AddLayer;
import org.constellation.dto.Service;
import org.constellation.process.ConstellationProcessFactory;
import org.constellation.process.service.DeleteServiceDescriptor;
import org.constellation.process.service.RestartServiceDescriptor;
import org.constellation.process.service.StartServiceDescriptor;
import org.constellation.process.service.StopServiceDescriptor;
import org.constellation.util.ReflectionUtilities;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.WSEngine;
import org.constellation.ws.Worker;
import org.constellation.ServiceDef.Specification;
import org.constellation.utils.MetadataUtilities;

import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.NoSuchIdentifierException;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import org.constellation.process.service.RenameServiceDescriptor;

/**
 * Utility class to configure and manage OGC Services. Contains static map to find specific implementation for each service type.
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 */
public class OGCServiceConfiguration {

    private static final Logger LOGGER = Logger.getLogger(OGCServiceConfiguration.class.getName());

    /**
     * List all implementations by service.
     */
    private final static Map<Specification, ServiceConfiguration> serviceUtilities = new HashMap<>(0);

    public Map<Specification, ServiceConfiguration> getServiceUtilities() {
        return serviceUtilities;
    }

    /**
     * Determines and returns a service status.
     *
     * @param serviceType the service type (WMS, CSW, WPS...)
     * @param identifier  the service identifier
     */
    public Instance getInstance(final String serviceType, final String identifier) {
        for (Map.Entry<String, Boolean> entry : WSEngine.getEntriesStatus(serviceType)) {
            if (entry.getKey().equals(identifier)) {
                return new Instance(entry.getKey(), serviceType, entry.getValue() ? ServiceStatus.WORKING : ServiceStatus.ERROR);
            }
            final File instanceDirectory = new File(getServiceDirectory(serviceType), identifier);
            if (instanceDirectory.exists() && instanceDirectory.isDirectory()) {
                return new Instance(identifier, serviceType, ServiceStatus.NOT_STARTED);
            }
        }
        return null;
    }

    /**
     * List all service instance by type
     *
     * @param serviceType service list type
     * @return a {@link InstanceReport} which contains service list
     */
    public InstanceReport listInstance(final String serviceType) {
        LOGGER.finer("listing instances");
        final List<Instance> instances = new ArrayList<>();
        // 1- First we list the instance in the map
        for (Map.Entry<String, Boolean> entry : WSEngine.getEntriesStatus(serviceType)) {
            final ServiceStatus status;
            if (entry.getValue()) {
                status = ServiceStatus.WORKING;
            } else {
                status = ServiceStatus.ERROR;
            }
            instances.add(new Instance(entry.getKey(), serviceType, status));
        }
        // 2- Then we list the instance not yet started
        final File serviceDirectory = getServiceDirectory(serviceType);
        if (serviceDirectory != null && serviceDirectory.isDirectory()) {
            for (File instanceDirectory : serviceDirectory.listFiles()) {
                final String name = instanceDirectory.getName();
                if (instanceDirectory.isDirectory() && !name.startsWith(".") && !WSEngine.serviceInstanceExist(serviceType, name)) {
                    instances.add(new Instance(name, serviceType, ServiceStatus.NOT_STARTED));
                }
            }
        }
        final InstanceReport report = new InstanceReport(instances);
        return report;
    }

    /**
     * List all service instance
     *
     * @return a {@link InstanceReport} which contains service list
     */
    public InstanceReport listInstance() {
        LOGGER.finer("listing all instance");
        final Set<String> serviceTypes =  WSEngine.getRegisteredServices().keySet();
        final List<Instance> instanceReports = new ArrayList<>(0);

        //  loop on all service type which exist on server
        for (String serviceType : serviceTypes) {
            final File serviceDirectory = getServiceDirectory(serviceType);
            if (serviceDirectory != null && serviceDirectory.isDirectory()) {

                //  loop on all service on service type
                for (File instanceDirectory : serviceDirectory.listFiles()) {
                    //get instance name
                    final String name = instanceDirectory.getName();

                    final Specification st = Specification.valueOf(serviceType);
                    final ServiceConfiguration configuration = serviceUtilities.get(st);

                    //get layer number
                    final Worker worker = buildWorker(serviceType, name);
                    final Integer layersNumber = configuration.getlayersNumber(worker).size();

                    //get service abstract
                    final String _abstract = configuration.getAbstract(instanceDirectory);

                    final boolean serviceExist = WSEngine.serviceInstanceExist(serviceType, name);
                    ServiceStatus status;
                    Instance currentInstance;

                    // get service state
                    if (instanceDirectory.isDirectory() && !name.startsWith(".")) {
                        if (serviceExist) {
                            status = ServiceStatus.WORKING;
                        } else {
                            status = ServiceStatus.ERROR;
                        }
                        if (instanceDirectory.isDirectory() && !name.startsWith(".") && !serviceExist) {
                            status = ServiceStatus.NOT_STARTED;
                        }
                        currentInstance = new Instance(name, serviceType, status);

                        //add abstract & layer number on instance
                        currentInstance.set_abstract(_abstract);
                        currentInstance.setLayersNumber(layersNumber);

                        //add instance on list
                        instanceReports.add(currentInstance);
                    }


                }
            }
        }
        final InstanceReport report = new InstanceReport(instanceReports);
        return report;
    }

    /**
     * Stop a service
     *
     * @param serviceType service choosen type
     * @param id          service identifier
     * @return {@link AcknowlegementType} : to know on client side server state after operation call
     */
    public AcknowlegementType stop(final String serviceType, final String id) {
        LOGGER.info("stopping an instance");
        AcknowlegementType response;
        try {
            final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, StopServiceDescriptor.NAME);
            final ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
            inputs.parameter(StopServiceDescriptor.IDENTIFIER_NAME).setValue(id);
            inputs.parameter(StopServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceType);

            final org.geotoolkit.process.Process proc = desc.createProcess(inputs);
            proc.call();
            response = new AcknowlegementType("Success", "instance succesfully stopped");
        } catch (NoSuchIdentifierException | ProcessException ex) {
            response = new AcknowlegementType("Error", "unable to stop the instance : " + ex.getMessage());
        }

        return response;
    }

    /**
     * stop a service
     *
     * @param serviceType service choosen type
     * @param id          service identifier
     * @return {@link AcknowlegementType} : to know on client side server state after operation call
     */
    public AcknowlegementType start(final String serviceType, final String id) {
        LOGGER.info("starting an instance");
        AcknowlegementType response;
        try {
            final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, StartServiceDescriptor.NAME);
            final ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
            inputs.parameter(StartServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceType);
            inputs.parameter(StartServiceDescriptor.IDENTIFIER_NAME).setValue(id);
            inputs.parameter(StartServiceDescriptor.SERVICE_DIRECTORY_NAME).setValue(getServiceDirectory(serviceType));

            final org.geotoolkit.process.Process proc = desc.createProcess(inputs);
            proc.call();
            response = new AcknowlegementType("Success", "new instance succefully started");
        } catch (NoSuchIdentifierException | ProcessException ex) {
            response = new AcknowlegementType("Error", "unable to start the instance : " + ex.getMessage());
        }
        return response;
    }

    /**
     * restart a service
     *
     * @param serviceType service choosen type
     * @param id          service identifier
     * @return {@link AcknowlegementType} : to know on client side server state after operation call
     */
    public AcknowlegementType restart(final String serviceType, final String id, final boolean isclosedFirst) {
        LOGGER.info("refreshing the workers");
        AcknowlegementType response;
        try {
            final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, RestartServiceDescriptor.NAME);
            final ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
            inputs.parameter(RestartServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceType);
            inputs.parameter(RestartServiceDescriptor.IDENTIFIER_NAME).setValue(id);
            inputs.parameter(RestartServiceDescriptor.CLOSE_NAME).setValue(isclosedFirst);
            inputs.parameter(RestartServiceDescriptor.SERVICE_DIRECTORY_NAME).setValue(getServiceDirectory(serviceType));

            final org.geotoolkit.process.Process proc = desc.createProcess(inputs);
            proc.call();
            response = new AcknowlegementType("Success", "instances succefully restarted");
        } catch (NoSuchIdentifierException | ProcessException ex) {
            response = new AcknowlegementType("Error", "unable to start the instance : " + ex.getMessage());
        }
        return response;
    }

    /**
     * delete a service
     *
     * @param serviceType service choosen type
     * @param id          service identifier
     * @return {@link AcknowlegementType} : to know on client side server state after operation call
     */
    public AcknowlegementType delete(final String serviceType, final String id) {
        LOGGER.info("deleting an instance");
        AcknowlegementType response;

        try {
            final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, DeleteServiceDescriptor.NAME);
            final ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
            inputs.parameter(DeleteServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceType);
            inputs.parameter(DeleteServiceDescriptor.IDENTIFIER_NAME).setValue(id);
            inputs.parameter(DeleteServiceDescriptor.SERVICE_DIRECTORY_NAME).setValue(getServiceDirectory(serviceType));

            final org.geotoolkit.process.Process proc = desc.createProcess(inputs);
            proc.call();
            response = new AcknowlegementType("Success", "instance succesfully deleted");
        } catch (NoSuchIdentifierException | ProcessException ex) {
            response = new AcknowlegementType("Error", "unable to delete the instance : " + ex.getMessage());
        }

        return response;
    }

    /**
     * rename a service
     *
     * @param serviceType service choosen type
     * @param id          service identifier
     * @return {@link AcknowlegementType} : to know on client side server state after operation call
     */
    public AcknowlegementType rename(final String serviceType, final String id, final String newName) throws CstlServiceException {
        AcknowlegementType response;

        try {
            final ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, RenameServiceDescriptor.NAME);
            final ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
            inputs.parameter(RenameServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceType);
            inputs.parameter(RenameServiceDescriptor.IDENTIFIER_NAME).setValue(id);
            inputs.parameter(RenameServiceDescriptor.SERVICE_DIRECTORY_NAME).setValue(getServiceDirectory(serviceType));
            inputs.parameter(RenameServiceDescriptor.NEW_NAME_NAME).setValue(newName);

            final org.geotoolkit.process.Process proc = desc.createProcess(inputs);
            proc.call();
            response = new AcknowlegementType("Success", "instance succesfully deleted");
        } catch (NoSuchIdentifierException | ProcessException ex) {
            response = new AcknowlegementType("Error", "unable to rename the instance : " + ex.getMessage());
        }
        return response;
    }


    /**
     * Return the dedicated Web-service configuration directory.
     *
     * @param serviceType service choosen type
     * @return {@link File} service type directory
     */
    public File getServiceDirectory(String serviceType) {
        final File configDirectory = ConfigDirectory.getConfigDirectory();
        if (configDirectory != null && configDirectory.isDirectory()) {
            final File serviceDirectory = new File(configDirectory, serviceType);
            if (serviceDirectory.isDirectory()) {
                return serviceDirectory;
            } else {
                LOGGER.log(Level.INFO, "The service configuration directory: {0} does not exist or is not a directory, creating new one.", serviceDirectory.getPath());
                if (!serviceDirectory.mkdir()) {
                    LOGGER.log(Level.WARNING, "The service was unable to create the directory.{0}", serviceDirectory.getPath());
                } else {
                    return serviceDirectory;
                }
            }
        } else {
            if (configDirectory == null) {
                LOGGER.warning("The service was unable to find a config directory.");
            } else {
                LOGGER.log(Level.WARNING, "The configuration directory: {0} does not exist or is not a directory.", configDirectory.getPath());
            }
        }
        return null;
    }

    /**
     * Create a worker after rename operation
     *
     * @param serviceType service choosen type
     * @param identifier  service identifier
     * @return specific service {@link Worker}
     */
    private Worker buildWorker(final String serviceType, final String identifier) {
        final File serviceDirectory = getServiceDirectory(serviceType);
        if (serviceDirectory != null) {
            final File instanceDirectory = new File(serviceDirectory, identifier);
            if (instanceDirectory.isDirectory()) {
                Specification st = Specification.valueOf(serviceType);
                final Worker newWorker = createWorker(instanceDirectory, serviceUtilities.get(st).getWorkerClass());
                if (newWorker != null) {
                    WSEngine.addServiceInstance(serviceType, instanceDirectory.getName(), newWorker);
                }
                return newWorker;
            } else {
                LOGGER.log(Level.WARNING, "The instance directory: {0} does not exist or is not a directory.", instanceDirectory.getPath());
            }
        }
        return null;
    }

    /**
     * Configure a service
     *
     * @param serviceType  service choosen type
     * @param identifier   service identifier
     * @param configuration service configuration
     * @return {@link AcknowlegementType} : to know on client side server state after operation call
     */
    public AcknowlegementType configure(final String serviceType, final String identifier, final Object configuration) throws CstlServiceException {
        final AcknowlegementType response;
        final File serviceDirectory = getServiceDirectory(serviceType);
        if (serviceDirectory != null && serviceDirectory.isDirectory()) {
            File instanceDirectory = new File(serviceDirectory, identifier);
            Specification st = Specification.valueOf(serviceType);
            serviceUtilities.get(st).configureInstance(instanceDirectory, configuration, null, serviceType);
            response = new AcknowlegementType("Success", "Instance correctly configured");
        } else {
            throw new CstlServiceException("Unable to find a configuration directory.", NO_APPLICABLE_CODE);
        }
        return response;
    }

    /**
     * Give service configuration
     *
     * @param serviceType service choosen type
     * @param identifier  service identifier
     * @return Object which define service configuration
     * @throws CstlServiceException
     */
    public Object getConfiguration(final String serviceType, final String identifier) throws CstlServiceException {
        final File serviceDirectory = getServiceDirectory(serviceType);
        final Object response;
        if (serviceDirectory != null && serviceDirectory.isDirectory()) {
            File instanceDirectory = new File(serviceDirectory, identifier);
            if (instanceDirectory.isDirectory()) {
                Specification st = Specification.valueOf(serviceType);
                response = serviceUtilities.get(st).getInstanceConfiguration(instanceDirectory, serviceType);
            } else {
                throw new CstlServiceException("Unable to find an instance:" + identifier, NO_APPLICABLE_CODE);
            }
        } else {
            throw new CstlServiceException("Unable to find a configuration directory.", NO_APPLICABLE_CODE);
        }
        return response;
    }

    /**
     * Build a new instance of Web service worker with the specified configuration directory
     *
     * @param instanceDirectory The configuration directory of the instance.
     * @return a specific {@link Worker}
     */
    private Worker createWorker(final File instanceDirectory, final Class workerClass) {
        return (Worker) ReflectionUtilities.newInstance(workerClass, instanceDirectory.getName(), instanceDirectory);
    }

    /**
     * create new service instance
     *
     * @param serviceType   service choosen type
     * @param identifier    service identifier
     * @param objectRequest object to define service
     * @return {@link AcknowlegementType} : to know on client side server state after operation call
     * @throws CstlServiceException
     */
    public AcknowlegementType newInstance(String serviceType, String identifier, Object objectRequest) throws CstlServiceException {
        LOGGER.info("creating an instance");
        final AcknowlegementType response;
        final File serviceDirectory = getServiceDirectory(serviceType);
        if (serviceDirectory != null && serviceDirectory.isDirectory()) {
            final File instanceDirectory = new File(serviceDirectory, identifier);
            if (instanceDirectory.mkdir()) {
//                reset
                Specification st = Specification.valueOf(serviceType);
                if (objectRequest != null && objectRequest instanceof Service) {
                    Service tocreated = (Service) objectRequest;
                    serviceUtilities.get(st).basicConfigure(instanceDirectory, tocreated, serviceType);
                } else {
                    //create basic conf
                    serviceUtilities.get(st).basicConfigure(instanceDirectory, null, serviceType);
                }
                response = new AcknowlegementType("Success", "instance succefully created");
            } else {
                response = new AcknowlegementType("Error", "unable to create an instance");
            }
        } else {
            throw new CstlServiceException("Unable to find a configuration directory.", NO_APPLICABLE_CODE);
        }
        return response;
    }

    /**
     * return data list?
     * @param serviceType
     * @param id
     */
    public List<Layer> getdatas(String serviceType, String id) {
        Worker worker = buildWorker(serviceType, id);
        Specification type = Specification.valueOf(serviceType);
        return serviceUtilities.get(type).getlayersNumber(worker);
    }

    /**
     *
     *
     * @param serviceType
     * @param id
     * @param addedLayer
     * @return
     */
    public boolean addLayer(String serviceType, String id, final AddLayer addedLayer){
        Specification type = Specification.valueOf(serviceType);
        return serviceUtilities.get(type).addLayer(addedLayer);
    }

    public Service getMetadata(String serviceType, String identifier) {
        File serviceTypeDirectory = getServiceDirectory(serviceType);
        File currentServiceDirectory = new File(serviceTypeDirectory, identifier);
        if(currentServiceDirectory.exists() && currentServiceDirectory.isDirectory()){
            try{
                return MetadataUtilities.readMetadata(currentServiceDirectory);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "error on serviceMetadataParsing", e);
            }
        }
        return null;
    }

    /**
     * Updates the a service metadata with the specified {@link Service} object.
     *
     * @param serviceType the service type (WMS, CSW, WPS...)
     * @param service     the new service metadata
     * @throws CstlServiceException if failed to update the service metadata
     */
    public void setMetadata(final String serviceType, final Service service) throws CstlServiceException {
        ensureNonNull("serviceType", serviceType);
        ensureNonNull("service",     service);

        final File serviceDirectory  = getServiceDirectory(serviceType);
        final File instanceDirectory = new File(serviceDirectory, service.getIdentifier());
        if (instanceDirectory.exists() && instanceDirectory.isDirectory()) {
            final Specification type = Specification.valueOf(serviceType);
            final Object configuration = WSEngine.getInstance(serviceType, service.getIdentifier()).getConfiguration();
            serviceUtilities.get(type).configureInstance(instanceDirectory, configuration, service, serviceType);
        } else {
            throw new CstlServiceException("The " + serviceType + " service with identifier \"" +
                    service.getIdentifier() + "\" does not exists.");
        }
    }
}
