package org.constellation.admin;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.admin.dto.ServiceDTO;
import org.constellation.admin.exception.ConstellationException;
import org.constellation.admin.util.DefaultServiceConfiguration;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.ServiceStatus;
import org.constellation.configuration.TargetNotFoundException;
import org.constellation.engine.register.ConstellationPersistenceException;
import org.constellation.engine.register.Service;
import org.constellation.engine.register.ServiceExtraConfig;
import org.constellation.engine.register.repository.LayerRepository;
import org.constellation.engine.register.repository.ServiceRepository;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.security.SecurityManager;
import org.constellation.ws.WSEngine;
import org.constellation.ws.Worker;
import org.geotoolkit.process.ProcessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServiceBusiness {

    @Autowired
    private SecurityManager securityManager;
            
    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private LayerRepository layerRepository;

    ServiceDTO getService(int id) throws IllegalAccessException, InvocationTargetException {
        final ServiceDTO returnService = new ServiceDTO();
        org.constellation.engine.register.Service service = serviceRepository.findById(id);
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
     * 
     * @return the configuration object just setted.
     * @throws org.constellation.configuration.ConfigurationException if the operation has failed for any reason
     */
    public Object create(final String serviceType, final String identifier, Object configuration, final org.constellation.dto.Service metadata) throws ConfigurationException {

        if (identifier == null || identifier.isEmpty()) {
            throw new ConfigurationException("Service instance identifier can't be null or empty.");
        }

        if (configuration == null) {
            configuration = DefaultServiceConfiguration.getDefaultConfiguration(serviceType);
        }

        final String config   = getStringFromObject(configuration, GenericDatabaseMarshallerPool.getInstance());
        final Service service = new Service();
        service.setConfig(config);
        service.setDate(new Date().getTime());
        service.setType(serviceType);
        service.setOwner(securityManager.getCurrentUserLogin());
        service.setIdentifier(identifier);
        service.setStatus(ServiceStatus.STOPPED.toString());
        // @TODO
        service.setVersions("1.3.0");
        serviceRepository.create(service);
        
        return configuration;
    }
    
    /**
     * Starts a service instance.
     *
     * @param serviceType The service type (WMS, WFS, ...)
     * @param identifier the service identifier
     * @throws org.constellation.configuration.ConfigurationException if the operation has failed for any reason
     */
    public void start(final String serviceType, final String identifier) throws ConfigurationException {
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
    public void stop(final String serviceType, final String identifier) throws ConfigurationException {
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
    public void restart(final String serviceType, final String identifier, final boolean closeFirst) throws ConfigurationException {

        if (identifier == null || "".equals(identifier)) {
            buildWorkers(serviceType, null, closeFirst);
        } else {
            if (WSEngine.serviceInstanceExist(serviceType, identifier)) {
                buildWorkers(serviceType, identifier, closeFirst);
            } else {
                start(identifier, serviceType);
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
     public void rename(final String serviceType, final String identifier, final String newIdentifier) throws ConfigurationException {
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
     public void delete(final String serviceType, final String identifier) throws ConfigurationException {
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
     * 
     * @throws org.constellation.configuration.ConfigurationException if the operation has failed for any reason
     */
     public void configure(final String serviceType, final String identifier, final org.constellation.dto.Service metadata, Object configuration) throws ConfigurationException {
         if (identifier == null || identifier.isEmpty()) {
            throw new ConfigurationException("Service instance identifier can't be null or empty.");
        }

        if (configuration == null) {
            configuration = DefaultServiceConfiguration.getDefaultConfiguration(serviceType);
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
     *
     * @return a configuration {@link Object} (depending on implementation)
     * @throws org.constellation.configuration.ConfigurationException if the operation has failed for any reason
     */
    public Object getConfiguration(final String serviceType, final String identifier) throws ConfigurationException {
        if (identifier == null || identifier.isEmpty()) {
            throw new ConfigurationException("Service instance identifier can't be null or empty.");
        }
        try {
            final Service service = serviceRepository.findByIdentifierAndType(identifier, serviceType);
            if (service != null) {
                final String confXml = service.getConfig();
                return getObjectFromString(confXml, GenericDatabaseMarshallerPool.getInstance());
            }
        } catch (JAXBException ex) {
            throw new ConfigurationException(ex.getMessage(), ex);
        }
        return null;
    }
    
    public Object getExtraConfiguration(final String serviceType, final String identifier, final String fileName) throws ConfigurationException {
        return getExtraConfiguration(serviceType, identifier, fileName, GenericDatabaseMarshallerPool.getInstance());
    }
    
    public Object getExtraConfiguration(final String serviceType, final String identifier, final String fileName, final MarshallerPool pool) throws ConfigurationException {
        try {
            final Service service = serviceRepository.findByIdentifierAndType(identifier, serviceType);
            if (service != null) {
                final ServiceExtraConfig conf = serviceRepository.getExtraConfig(service.getId(), fileName);
                final String content = conf.getContent();
                if (content != null) {
                    return getObjectFromString(content, pool);
                }
            }
        } catch (JAXBException ex) {
            throw new ConfigurationException(ex.getMessage(), ex);
        }
        return null;
    }
    
    public void setExtraConfiguration(final String serviceType, final String identifier, final String fileName, final Object config, final MarshallerPool pool) {
        final Service service = serviceRepository.findByIdentifierAndType(identifier, serviceType);
        if (service != null) {
            final String content = getStringFromObject(config, pool);
            final ServiceExtraConfig conf = new ServiceExtraConfig(service.getId(), fileName, content);
            serviceRepository.updateExtraFile(service, fileName, content);
        }
    }
     
     /**
     * Returns all service instances (for current specification) status.
     *
     * @param spec
     * @return a {@link Map} of {@link ServiceStatus} status
     */
    public Map<String, ServiceStatus> getStatus(final String spec) {
        final Map<String, ServiceStatus> status = new HashMap<>();
        for (Map.Entry<String, Boolean> entry : WSEngine.getEntriesStatus(spec)) {
            status.put(entry.getKey(), entry.getValue() ? ServiceStatus.STARTED : ServiceStatus.ERROR);
        }
        final List<String> serviceIDs = ConfigurationEngine.getServiceConfigurationIds(spec);
        for (String serviceID : serviceIDs) {
            if (!WSEngine.serviceInstanceExist(spec, serviceID)) {
                status.put(serviceID, ServiceStatus.STOPPED);
            }
        }
        return status;
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

    public org.constellation.engine.register.Service getServiceByIdentifierAndType(String serviceType, String identifier) {
        return serviceRepository.findByIdentifierAndType(identifier, serviceType);
    }
    
    /**
     * Returns a service instance metadata.
     *
     * @param serviceType The type of the service.
     * @param identifier the service identifier
     * @return 
     * @throws TargetNotFoundException if the service with specified identifier does not exist
     * @throws org.constellation.configuration.ConfigurationException if the operation has failed for any reason
     */
    public org.constellation.dto.Service getInstanceMetadata(final String serviceType, final String identifier) throws ConfigurationException {
        this.ensureExistingInstance(serviceType, identifier);
        try {
            // todo add language parameter
            return ConfigurationEngine.readServiceMetadata(identifier, serviceType, null);
        } catch (JAXBException | IOException ex) {
            throw new ConfigurationException("The serviceMetadata.xml file can't be read.", ex);
        }
    }
    
    /**
     * Updates a service instance metadata.
     *
     * @param serviceType The type of the service.
     * @param identifier the service identifier
     * @param metadata   the service metadata
     * @throws TargetNotFoundException if the service with specified identifier does not exist
     * @throws org.constellation.configuration.ConfigurationException if the operation has failed for any reason
     */
    public void setInstanceMetadata(final String serviceType, final String identifier, final org.constellation.dto.Service metadata) throws ConfigurationException {
        this.ensureExistingInstance(serviceType, identifier);
        final Object config = getConfiguration(serviceType, identifier);
        this.configure(serviceType, identifier, metadata, config);
    }
    
    /**
     * Ensure that a service instance really exists.
     *
     * @param spec The service type.
     * @param identifier the service identifier
     * @throws TargetNotFoundException if the service with specified identifier does not exist
     */
    public void ensureExistingInstance(final String spec, final String identifier) throws TargetNotFoundException {
        if (!WSEngine.serviceInstanceExist(spec, identifier)) {
            if (!ConfigurationEngine.serviceConfigurationExist(spec, identifier)) {
                throw new TargetNotFoundException(spec + " service instance with identifier \"" + identifier +
                        "\" not found. There is not configuration in the database.");
            }
        }
    }
    
    private String getStringFromObject(final Object obj, final MarshallerPool pool) {
        String config = null;
        if (obj != null) {
            try {
                final StringWriter sw = new StringWriter();
                final Marshaller m = pool.acquireMarshaller();
                m.marshal(obj, sw);
                pool.recycle(m);
                config = sw.toString();
            } catch (JAXBException e) {
                throw new ConstellationPersistenceException(e);
            }
        }
        return config;
    }
    
    private Object getObjectFromString(final String xml, final MarshallerPool pool) throws JAXBException {
        if (xml != null) {
            final Unmarshaller u = pool.acquireUnmarshaller();
            final Object config = u.unmarshal(new StringReader(xml));
            pool.recycle(u);
            return config;
        }
        return null;
    }
}
