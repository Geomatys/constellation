package org.constellation.admin;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBException;
import org.apache.commons.beanutils.BeanUtils;
import org.constellation.admin.dto.ServiceDTO;
import org.constellation.admin.exception.ConstellationException;
import org.constellation.configuration.ConfigurationException;
import org.constellation.engine.register.Service;
import org.constellation.engine.register.repository.LayerRepository;
import org.constellation.engine.register.repository.ServiceRepository;
import org.constellation.util.ReflectionUtilities;
import org.constellation.ws.WSEngine;
import org.constellation.ws.Worker;
import org.geotoolkit.process.ProcessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServiceBusiness {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private LayerRepository layerRepository;

    ServiceDTO getService(int id) throws IllegalAccessException,
            InvocationTargetException {
        ServiceDTO returnService = new ServiceDTO();
        org.constellation.engine.register.Service service = serviceRepository
                .findById(id);
        BeanUtils.copyProperties(returnService, service);
        return returnService;
    }

    ServiceDTO create(ServiceDTO serviceDTO) {
        Service service = new Service();
        try {
            BeanUtils.copyProperties(service, serviceDTO);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ConstellationException(e);
        }
        int serviceId = serviceRepository.create(service);
        serviceDTO.setId(serviceId);
        return serviceDTO;
    }

    /**
     * Creates a new service instance.
     *
     * @param serviceType
     * @param identifier    The identifier of the service.
     * @param metadata      the service metadata (can be null).
     * @param configuration the service configuration (can be null).
     * @param configurationClass Class of the configuration object expected.
     * 
     * @return the configuration object just setted.
     * @throws org.constellation.configuration.ConfigurationException if the operation has failed for any reason
     */
    public Object createInstance(final String serviceType, final String identifier, Object configuration, final org.constellation.dto.Service metadata, final Class configurationClass) throws ConfigurationException {

        if (identifier == null || identifier.isEmpty()) {
            throw new ConfigurationException("Service instance identifier can't be null or empty.");
        }

        if (configuration == null) {
            configuration = ReflectionUtilities.newInstance(configurationClass);
        }

        boolean createConfig = false;
        try {
            final Object obj = ConfigurationEngine.getConfiguration(serviceType, identifier);
            if (obj.getClass().isAssignableFrom(configurationClass)) {
                configuration = obj;
            } else {
                throw new ConfigurationException("The configuration does not contain a " + configurationClass.getName() + " object");
            }
        } catch (JAXBException ex) {
            throw new ConfigurationException(ex.getMessage(), ex);
        } catch (FileNotFoundException ex) {
            createConfig = true;
        }

        if (createConfig) {
            //create config file for the default configuration.
            try {
                ConfigurationEngine.storeConfiguration(serviceType, identifier, configuration, metadata);
            } catch (JAXBException ex) {
                throw new ConfigurationException(ex.getMessage(), ex);
            } catch (IOException ex) {
                throw new ConfigurationException("An error occurred while trying to write service Metadata.", ex);
            }
        }
        return configuration;
    }
    
    /**
     * Starts a service instance.
     *
     * @param serviceType The service type (WMS, WFS, ...)
     * @param identifier the service identifier
     * @throws org.constellation.configuration.ConfigurationException if the operation has failed for any reason
     */
    public void startInstance(final String serviceType, final String identifier) throws ConfigurationException {
        if (identifier == null || identifier.isEmpty()) {
            throw new ConfigurationException("Service instance identifier can't be null or empty.");
        }
        
        try {
            final Worker worker = WSEngine.buildWorker(serviceType, identifier);
            if (worker != null) {
                WSEngine.addServiceInstance(serviceType, identifier, worker);
                if (!worker.isStarted()) {
                    throw new ConfigurationException("Unable to start the instance " + identifier + ".");
                }
            } else {
                throw new ConfigurationException("The instance " + identifier + " can not be instanciated.");
            }
        } catch (IllegalArgumentException ex) {
            throw new ConfigurationException(ex.getMessage(), ex);
        }
    }
    
    /**
     * Stops a service instance.
     *
     * @param serviceType The service type (WMS, WFS, ...)
     * @param identifier the service identifier
     * @throws org.constellation.configuration.ConfigurationException if the operation has failed for any reason
     */
    public void stopInstance(final String serviceType, final String identifier) throws ConfigurationException {
        if (identifier == null || identifier.isEmpty()) {
            throw new ConfigurationException("Service instance identifier can't be null or empty.");
        }

        if (WSEngine.serviceInstanceExist(serviceType, identifier)) {
            WSEngine.shutdownInstance(serviceType, identifier);
        } else {
            throw new ConfigurationException("Instance "+identifier+" doesn't exist.");
        }
    }
    
    /**
     * Restarts a service instance.
     *
     * @param serviceType The service type (WMS, WFS, ...)
     * @param identifier the service identifier
     * @param closeFirst indicates if the service should be closed before trying to restart it
     * @throws org.constellation.configuration.ConfigurationException if the operation has failed for any reason
     */
    public void restartInstance(final String serviceType, final String identifier, final boolean closeFirst) throws ConfigurationException {

        if (identifier == null || "".equals(identifier)) {
            buildWorkers(serviceType, null, closeFirst);
        } else {
            if (WSEngine.serviceInstanceExist(serviceType, identifier)) {
                buildWorkers(serviceType, identifier, closeFirst);
            } else {
                startInstance(identifier, serviceType);
            }
        }
     }
     
    /**
     * Renames a service instance.
     *
     * @param serviceType The service type (WMS, WFS, ...)
     * @param identifier    the current service identifier
     * @param newIdentifier the new service identifier
     * @throws org.constellation.configuration.ConfigurationException if the operation has failed for any reason
     */
     public void renameInstance(final String serviceType, final String identifier, final String newIdentifier) throws ConfigurationException {
        final List<String> existingService = ConfigurationEngine.getServiceConfigurationIds(serviceType);
        if (existingService.contains(identifier)) {
            if (!existingService.contains(newIdentifier)) {
                if (ConfigurationEngine.renameConfiguration(serviceType, identifier, newIdentifier)) {
                    // we stop the current worker
                    WSEngine.shutdownInstance(serviceType, identifier);

                    // start the new one
                    final Worker newWorker = WSEngine.buildWorker(serviceType, newIdentifier);
                    if (newWorker == null) {
                        throw new ConfigurationException("The instance " + newIdentifier + " can be started, maybe there is no configuration directory with this name.");
                    } else {
                        WSEngine.addServiceInstance(serviceType, newIdentifier, newWorker);
                        if (!newWorker.isStarted()) {
                            throw new ConfigurationException("unable to start the renamed instance");
                        }
                    }
                } else {
                    throw new ConfigurationException("Unable to rename the directory");
                }
            } else {
                throw new ConfigurationException("already existing instance:" + newIdentifier);
            }
        } else {
            throw new ConfigurationException("no existing instance:" + identifier);
        }
     }
     
     /**
     * Deletes a service instance.
     *
     * @param serviceType The service type (WMS, WFS, ...)
     * @param identifier the service identifier
     * @throws org.constellation.configuration.ConfigurationException if the operation has failed for any reason
     */
     public void deleteInstance(final String serviceType, final String identifier) throws ConfigurationException {
         if (identifier == null || identifier.isEmpty()) {
            throw new ConfigurationException("Service instance identifier can't be null or empty.");
        }

        //unregister the service instance if exist
        if (WSEngine.serviceInstanceExist(serviceType, identifier)) {
            WSEngine.shutdownInstance(serviceType, identifier);
        }

        //delete folder
        if (!ConfigurationEngine.deleteConfiguration(serviceType, identifier)) {
            throw new ConfigurationException("Service instance directory " + identifier + " can't be deleted.");
        }
     }
     
     /**
     * Configures a service instance.
     *
     * @param serviceType The service type (WMS, WFS, ...)
     * @param identifier    the service identifier.
     * @param configuration the service configuration (depending on implementation).
     * @param metadata      the service metadata.
     * @param configurationClass Class of the configuration object expected.
     * @throws org.constellation.configuration.ConfigurationException if the operation has failed for any reason
     */
     public void configureInstance(final String serviceType, final String identifier, final org.constellation.dto.Service metadata, Object configuration, final Class configurationClass) throws ConfigurationException {
         if (identifier == null || identifier.isEmpty()) {
            throw new ConfigurationException("Service instance identifier can't be null or empty.");
        }

        if (configuration == null) {
            configuration = ReflectionUtilities.newInstance(configurationClass);
        }

        //write configuration file.
        try {
            ConfigurationEngine.storeConfiguration(serviceType, identifier, configuration, metadata);
        } catch (JAXBException ex) {
            throw new ConfigurationException(ex.getMessage(), ex);
        } catch (IOException ex) {
            throw new ConfigurationException("An error occurred while trying to write serviceMetadata.xml file.");
        }
     }
     
    /**
     * Returns the configuration object of a service instance.
     *
     * @param serviceType The service type (WMS, WFS, ...)
     * @param identifier the service
     * @param configurationClass Class of the configuration object expected.
     * @return a configuration {@link Object} (depending on implementation)
     * @throws org.constellation.configuration.ConfigurationException if the operation has failed for any reason
     */
     public Object getInstanceConfiguration(final String serviceType, final String identifier, final Class configurationClass) throws ConfigurationException {
         if (identifier == null || identifier.isEmpty()) {
            throw new ConfigurationException("Service instance identifier can't be null or empty.");
        }

        try {
            final Object obj = ConfigurationEngine.getConfiguration(serviceType, identifier);
            if (obj.getClass().isAssignableFrom(configurationClass)) {
                return obj;
            } else {
                throw new ConfigurationException("The configuration does not contain a " + configurationClass.getName() + " object.");
            }
        } catch (JAXBException ex) {
            throw new ConfigurationException(ex.getMessage(), ex);
        } catch (FileNotFoundException ex) {
            throw new ConfigurationException("Service instance " + identifier + " doesn't exist.");
       }
     }
     
     /**
     * Create new worker instance in service directory.
     *
     * @param serviceDir
     * @param identifier
     * @throws ProcessException
     */
    private void buildWorkers(final String serviceType, final String identifier, final boolean closeInstance) throws ConfigurationException {

        /*
         * Single refresh
         */
        if (identifier != null) {
            if (closeInstance) {
                WSEngine.shutdownInstance(serviceType, identifier);
            }
            try {
                final Worker worker = WSEngine.buildWorker(serviceType, identifier);
                if (worker != null) {
                    WSEngine.addServiceInstance(serviceType, identifier, worker);
                    if (!worker.isStarted()) {
                        throw new ConfigurationException("Unable to start the instance " + identifier + ".");
                    }
                } else {
                    throw new ConfigurationException("The instance " + identifier + " can't be started, maybe there is no configuration directory with this name.");
                }
            } catch (IllegalArgumentException ex) {
                throw new ConfigurationException(ex.getMessage(), ex);
            }
            
        /*
         * Multiple refresh
         */
        } else {

            final Map<String, Worker> workersMap = new HashMap<>();
            if (closeInstance) {
                WSEngine.destroyInstances(serviceType);
            }

            for (String instanceID : ConfigurationEngine.getServiceConfigurationIds(serviceType)) {
                try {
                    final Worker worker = WSEngine.buildWorker(serviceType, instanceID);
                    if (worker != null) {
                        workersMap.put(instanceID, worker);
                    } else {
                        throw new ConfigurationException("The instance " + instanceID + " can be started, maybe there is no configuration directory with this name.");
                    }
                } catch (IllegalArgumentException ex) {
                    throw new ConfigurationException(ex.getMessage(), ex);
                }
            }
            WSEngine.setServiceInstances(serviceType, workersMap);
        }
    }
}
