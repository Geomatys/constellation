package org.constellation.ws.rs;

import org.constellation.api.ServiceType;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.configuration.Instance;
import org.constellation.configuration.InstanceReport;
import org.constellation.configuration.Layer;
import org.constellation.configuration.ServiceStatus;
import org.constellation.dto.AccessConstraint;
import org.constellation.dto.Contact;
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
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.ProcessFinder;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.NoSuchIdentifierException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;


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
    private static Map<ServiceType, ServiceConfiguration> serviceUtilities = new HashMap<ServiceType, ServiceConfiguration>(0);

    public Map<ServiceType, ServiceConfiguration> getServiceUtilities() {
        return serviceUtilities;
    }

    public void setServiceUtilities(Map<ServiceType, ServiceConfiguration> serviceUtilities) {
        this.serviceUtilities = serviceUtilities;
    }

    /**
     * List all service instance by type
     *
     * @param serviceType service list type
     * @return a {@link InstanceReport} which contains service list
     */
    public InstanceReport listInstance(final String serviceType) {
        LOGGER.finer("listing instances");
        final List<Instance> instances = new ArrayList<Instance>();
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
        Set<String> serviceTypes =  WSEngine.getRegisteredServices().keySet();
        List<Instance> instanceReports = new ArrayList<Instance>(0);

        //  loop on all service type which exist on server
        for (String serviceType : serviceTypes) {
            final File serviceDirectory = getServiceDirectory(serviceType);
            if (serviceDirectory != null && serviceDirectory.isDirectory()) {

                //  loop on all service on service type
                for (File instanceDirectory : serviceDirectory.listFiles()) {
                    //get instance name
                    final String name = instanceDirectory.getName();

                    final ServiceType st = ServiceType.valueOf(serviceType);
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
            ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, StopServiceDescriptor.NAME);
            ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
            inputs.parameter(StopServiceDescriptor.IDENTIFIER_NAME).setValue(id);
            inputs.parameter(StopServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceType);

            org.geotoolkit.process.Process proc = desc.createProcess(inputs);
            proc.call();
            response = new AcknowlegementType("Success", "instance succesfully stopped");
        } catch (NoSuchIdentifierException ex) {
            response = new AcknowlegementType("Error", "unable to stop the instance : " + ex.getMessage());
        } catch (ProcessException ex) {
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
            ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, StartServiceDescriptor.NAME);
            ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
            inputs.parameter(StartServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceType);
            inputs.parameter(StartServiceDescriptor.IDENTIFIER_NAME).setValue(id);
            inputs.parameter(StartServiceDescriptor.SERVICE_DIRECTORY_NAME).setValue(getServiceDirectory(serviceType));

            org.geotoolkit.process.Process proc = desc.createProcess(inputs);
            proc.call();
            response = new AcknowlegementType("Success", "new instance succefully started");
        } catch (NoSuchIdentifierException ex) {
            response = new AcknowlegementType("Error", "unable to start the instance : " + ex.getMessage());
        } catch (ProcessException ex) {
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
            ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, RestartServiceDescriptor.NAME);
            ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
            inputs.parameter(RestartServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceType);
            inputs.parameter(RestartServiceDescriptor.IDENTIFIER_NAME).setValue(id);
            inputs.parameter(RestartServiceDescriptor.CLOSE_NAME).setValue(isclosedFirst);
            inputs.parameter(RestartServiceDescriptor.SERVICE_DIRECTORY_NAME).setValue(getServiceDirectory(serviceType));

            org.geotoolkit.process.Process proc = desc.createProcess(inputs);
            proc.call();
            response = new AcknowlegementType("Success", "instances succefully restarted");
        } catch (NoSuchIdentifierException ex) {
            response = new AcknowlegementType("Error", "unable to start the instance : " + ex.getMessage());
        } catch (ProcessException ex) {
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
            ProcessDescriptor desc = ProcessFinder.getProcessDescriptor(ConstellationProcessFactory.NAME, DeleteServiceDescriptor.NAME);
            ParameterValueGroup inputs = desc.getInputDescriptor().createValue();
            inputs.parameter(DeleteServiceDescriptor.SERVICE_TYPE_NAME).setValue(serviceType);
            inputs.parameter(DeleteServiceDescriptor.IDENTIFIER_NAME).setValue(id);
            inputs.parameter(DeleteServiceDescriptor.SERVICE_DIRECTORY_NAME).setValue(getServiceDirectory(serviceType));

            org.geotoolkit.process.Process proc = desc.createProcess(inputs);
            proc.call();
            response = new AcknowlegementType("Success", "instance succesfully deleted");
        } catch (NoSuchIdentifierException ex) {
            response = new AcknowlegementType("Error", "unable to delete the instance : " + ex.getMessage());
        } catch (ProcessException ex) {
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
        final AcknowlegementType response;
        // we stop the current worker
        WSEngine.shutdownInstance(serviceType, id);
        final File serviceDirectory = getServiceDirectory(serviceType);
        if (serviceDirectory != null && serviceDirectory.isDirectory()) {
            final File instanceDirectory = new File(serviceDirectory, id);
            final File newDirectory = new File(serviceDirectory, newName);

            if (instanceDirectory.isDirectory()) {
                if (!newDirectory.exists()) {
                    if (instanceDirectory.renameTo(newDirectory)) {
                        final Worker newWorker = buildWorker(serviceType, newName);
                        if (newWorker == null) {
                            throw new CstlServiceException("The instance " + newName + " can be started, maybe there is no configuration directory with this name.", INVALID_PARAMETER_VALUE);
                        } else {
                            if (newWorker.isStarted()) {
                                response = new AcknowlegementType("Success", "instance succefully renamed");
                            } else {
                                response = new AcknowlegementType("Error", "unable to start the renamed instance");
                            }
                        }
                    } else {
                        response = new AcknowlegementType("Error", "Unable to rename the directory");
                    }
                } else {
                    response = new AcknowlegementType("Error", "already existing instance:" + newName);
                }
            } else {
                response = new AcknowlegementType("Error", "no existing instance:" + id);
            }
        } else {
            throw new CstlServiceException("Unable to find a configuration directory.", NO_APPLICABLE_CODE);
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
                ServiceType st = ServiceType.valueOf(serviceType);
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
            ServiceType st = ServiceType.valueOf(serviceType);
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
                ServiceType st = ServiceType.valueOf(serviceType);
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
                ServiceType st = ServiceType.valueOf(serviceType);
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
        ServiceType type = ServiceType.valueOf(serviceType);
        return serviceUtilities.get(type).getlayersNumber(worker);
    }

    public Service getMetadata(String serviceType, String identifier) {
        File serviceTypeDirectory = getServiceDirectory(serviceType);
        File currentServiceDirectory = new File(serviceTypeDirectory, identifier);
        if(currentServiceDirectory.exists() && currentServiceDirectory.isDirectory()){
            try{
                //unmarshall serviceMetadata.xml File to create Service object
                JAXBContext context = JAXBContext.newInstance(Service.class, Contact.class, AccessConstraint.class);
                final Unmarshaller unmarshaller = context.createUnmarshaller();
                final File wMSServiceMetadata = new File(currentServiceDirectory, "serviceMetadata.xml");
                final Service service = (Service) unmarshaller.unmarshal(wMSServiceMetadata);
                return service;
            }catch (JAXBException e){
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
            final ServiceType type = ServiceType.valueOf(serviceType);
            final Object configuration = WSEngine.getInstance("WMS", service.getIdentifier()).getConfiguration();
            serviceUtilities.get(type).configureInstance(instanceDirectory, configuration, service, serviceType);
        } else {
            throw new CstlServiceException("The " + serviceType + " service with identifier \"" +
                    service.getIdentifier() + "\" does not exists.");
        }
    }
}
