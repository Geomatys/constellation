package org.constellation.admin;

import org.apache.commons.lang3.StringUtils;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.ServiceDef;
import org.constellation.admin.dto.ServiceDTO;
import org.constellation.admin.exception.ConstellationException;
import org.constellation.admin.util.DefaultServiceConfiguration;
import org.constellation.business.IServiceBusiness;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.CstlConfigurationRuntimeException;
import org.constellation.configuration.Instance;
import org.constellation.configuration.ServiceStatus;
import org.constellation.configuration.TargetNotFoundException;
import org.constellation.dto.Details;
import org.constellation.engine.register.ConstellationPersistenceException;
import org.constellation.engine.register.Domain;
import org.constellation.engine.register.Service;
import org.constellation.engine.register.ServiceDetails;
import org.constellation.engine.register.ServiceExtraConfig;
import org.constellation.engine.register.CstlUser;
import org.constellation.engine.register.repository.DomainRepository;
import org.constellation.engine.register.repository.LayerRepository;
import org.constellation.engine.register.repository.ServiceRepository;
import org.constellation.engine.register.repository.UserRepository;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.security.SecurityManager;
import org.constellation.util.Util;
import org.constellation.ws.WSEngine;
import org.constellation.ws.Worker;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.util.FileUtilities;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Optional;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.constellation.engine.register.repository.DataRepository;
import org.constellation.engine.register.repository.DatasetRepository;

@Component
@Primary
public class ServiceBusiness implements IServiceBusiness {

    @Inject
    private SecurityManager securityManager;

    @Inject
    private UserRepository userRepository;

    @Inject
    private DomainRepository domainRepository;

    @Inject
    private ServiceRepository serviceRepository;
    
    @Inject
    private DataRepository dataRepository;
    
    @Inject
    private DatasetRepository datasetRepository;

    @Inject
    private LayerRepository layerRepository;

    /**
     * Creates a new service instance.
     *
     * @param serviceType
     * @param identifier
     *            The identifier of the service.
     * @param details
     *            the service metadata (can be null).
     * @param configuration
     *            the service configuration (can be null).
     * 
     * @return the configuration object just setted.
     * @throws org.constellation.configuration.ConfigurationException
     *             if the operation has failed for any reason
     */
    public Object create(final String serviceType, final String identifier, Object configuration, Details details, final Integer domainId)
            throws ConfigurationException {

        if (identifier == null || identifier.isEmpty()) {
            throw new ConfigurationException("Service instance identifier can't be null or empty.");
        }

        if (configuration == null) {
            configuration = DefaultServiceConfiguration.getDefaultConfiguration(serviceType);
        }
        Optional<CstlUser> user = userRepository.findOne(securityManager.getCurrentUserLogin());

        final String config = getStringFromObject(configuration, GenericDatabaseMarshallerPool.getInstance());
        final Service service = new Service();
        service.setConfig(config);
        service.setDate(new Date().getTime());
        service.setType(ServiceDef.Specification.valueOf(serviceType.toUpperCase()).name().toLowerCase());
        if (user.isPresent()) {
            service.setOwner(user.get().getId());
        }
        service.setIdentifier(identifier);
        service.setStatus(ServiceStatus.STOPPED.toString());
        // TODO metadata-Iso

        if (details == null) {
            final InputStream in = Util
                    .getResourceAsStream("org/constellation/xml/" + service.getType().toUpperCase() + "Capabilities.xml");
            if (in != null) {
                try {
                    final Unmarshaller u = GenericDatabaseMarshallerPool.getInstance().acquireUnmarshaller();
                    details = (Details) u.unmarshal(in);
                    details.setIdentifier(service.getIdentifier());
                    details.setLang("eng"); // default value
                    GenericDatabaseMarshallerPool.getInstance().recycle(u);
                    in.close();
                } catch (JAXBException | IOException ex) {
                    throw new ConfigurationException(ex);
                }
            } else {
                throw new ConfigurationException("Unable to find the capabilities skeleton from resource.");
            }
        } else if (details.getLang() == null) {
            details.setLang("eng");// default value
        }

        final String versions;
        if (details.getVersions() != null) {
            versions = StringUtils.join(details.getVersions(), "µ");
        } else {
            versions = "";
        }
        service.setVersions(versions);

        int serviceId = serviceRepository.create(service);
        if (domainId != null) {
            domainRepository.addServiceToDomain(serviceId, domainId);
        }
        setInstanceDetails(serviceType, identifier, details, details.getLang(), true);
        return configuration;
    }

    /**
     * Starts a service instance.
     *
     * @param serviceType
     *            The service type (WMS, WFS, ...)
     * @param identifier
     *            the service identifier
     * @throws org.constellation.configuration.ConfigurationException
     *             if the operation has failed for any reason
     */
    public void start(final String serviceType, final String identifier) throws ConfigurationException {
        if (identifier == null || identifier.isEmpty()) {
            throw new ConfigurationException("Service instance identifier can't be null or empty.");
        }
        final Service service = serviceRepository.findByIdentifierAndType(identifier, serviceType);
        if (service != null) {
            try {
                final Worker worker = WSEngine.buildWorker(serviceType, identifier);
                if (worker != null) {
                    WSEngine.addServiceInstance(serviceType, identifier, worker);
                    if (!worker.isStarted()) {
                        service.setStatus("ERROR");
                        serviceRepository.update(service);
                        throw new ConfigurationException("Unable to start the instance " + identifier + ".");
                    }
                    service.setStatus("STARTED");
                    serviceRepository.update(service);
                } else {
                    throw new ConfigurationException("The instance " + identifier + " can not be instanciated.");
                }
            } catch (IllegalArgumentException ex) {
                throw new ConfigurationException(ex.getMessage(), ex);
            }
        } else {
            throw new TargetNotFoundException(serviceType + " service instance with identifier \"" + identifier
                    + "\" not found. There is not configuration in the database.");
        }
    }

    public void start(final String serviceType) throws ConfigurationException {

        final List<Service> services = serviceRepository.findByType(serviceType);
        for (Service service : services) {
            try {
                final String identifier = service.getIdentifier();
                final Worker worker = WSEngine.buildWorker(serviceType, identifier);
                if (worker != null) {
                    WSEngine.addServiceInstance(serviceType, identifier, worker);
                    if (!worker.isStarted()) {
                        service.setStatus("ERROR");
                        serviceRepository.update(service);
                    }
                    service.setStatus("STARTED");
                    serviceRepository.update(service);
                } else {
                    throw new ConfigurationException("The instance " + identifier + " can not be instanciated.");
                }
            } catch (IllegalArgumentException ex) {
                throw new ConfigurationException(ex.getMessage(), ex);
            }
        }
    }

    /**
     * Stops a service instance.
     *
     * @param serviceType
     *            The service type (WMS, WFS, ...)
     * @param identifier
     *            the service identifier
     * @throws org.constellation.configuration.ConfigurationException
     *             if the operation has failed for any reason
     */
    public void stop(final String serviceType, final String identifier) throws ConfigurationException {
        if (identifier == null || identifier.isEmpty()) {
            throw new ConfigurationException("Service instance identifier can't be null or empty.");
        }
        final Service service = serviceRepository.findByIdentifierAndType(identifier, serviceType);
        if (service != null) {
            if (WSEngine.serviceInstanceExist(serviceType, identifier)) {
                WSEngine.shutdownInstance(serviceType, identifier);
                service.setStatus("STOPPED");
                serviceRepository.update(service);
            } else {
                throw new ConfigurationException("Instance " + identifier + " doesn't exist.");
            }
        } else {
            throw new TargetNotFoundException(serviceType + " service instance with identifier \"" + identifier
                    + "\" not found. There is not configuration in the database.");
        }
    }

    /**
     * Restarts a service instance.
     *
     * @param serviceType
     *            The service type (WMS, WFS, ...)
     * @param identifier
     *            the service identifier
     * @param closeFirst
     *            indicates if the service should be closed before trying to
     *            restart it
     * @throws org.constellation.configuration.ConfigurationException
     *             if the operation has failed for any reason
     */
    public void restart(final String serviceType, final String identifier, final boolean closeFirst) throws ConfigurationException {

        if (identifier == null || "".equals(identifier)) {
            buildWorkers(serviceType, null, closeFirst);
        } else {
            if (WSEngine.serviceInstanceExist(serviceType, identifier)) {
                buildWorkers(serviceType, identifier, closeFirst);
            } else {
                start(serviceType, identifier);
            }
        }
    }

    /**
     * Renames a service instance.
     *
     * @param serviceType
     *            The service type (WMS, WFS, ...)
     * @param identifier
     *            the current service identifier
     * @param newIdentifier
     *            the new service identifier
     * @throws org.constellation.configuration.ConfigurationException
     *             if the operation has failed for any reason
     */
    public void rename(final String serviceType, final String identifier, final String newIdentifier) throws ConfigurationException {
        final Service service = serviceRepository.findByIdentifierAndType(identifier, serviceType);
        if (service != null) {
            final Service newService = serviceRepository.findByIdentifierAndType(newIdentifier, serviceType);
            if (newService == null) {
                service.setIdentifier(newIdentifier);
                serviceRepository.update(service);

                // we stop the current worker
                WSEngine.shutdownInstance(serviceType, identifier);

                // start the new one
                final Worker newWorker = WSEngine.buildWorker(serviceType, newIdentifier);
                if (newWorker == null) {
                    throw new ConfigurationException("The instance " + newIdentifier
                            + " can be started, maybe there is no configuration directory with this name.");
                } else {
                    WSEngine.addServiceInstance(serviceType, newIdentifier, newWorker);
                    if (!newWorker.isStarted()) {
                        throw new ConfigurationException("unable to start the renamed instance");
                    }
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
     * @param serviceType
     *            The service type (WMS, WFS, ...)
     * @param identifier
     *            the service identifier
     * @throws org.constellation.configuration.ConfigurationException
     *             if the operation has failed for any reason
     */
    public void delete(final String serviceType, final String identifier) throws ConfigurationException {
        if (identifier == null || identifier.isEmpty()) {
            throw new ConfigurationException("Service instance identifier can't be null or empty.");
        }

        final Service service = getServiceByIdentifierAndType(serviceType, identifier);
        if (service != null) {

            // unregister the service instance if exist
            if (WSEngine.serviceInstanceExist(serviceType, identifier)) {
                WSEngine.shutdownInstance(serviceType, identifier);
            }

            if (serviceType.equalsIgnoreCase("csw")) {
                dataRepository.removeAllDataFromCSW(service.getId());
                datasetRepository.removeAllDatasetFromCSW(service.getId());
            }
            
            // delete from database
            serviceRepository.delete(service.getId());
            // delete folder
            final File instanceDir = ConfigDirectory.getInstanceDirectory(serviceType, identifier);
            if (instanceDir.isDirectory()) {
                FileUtilities.deleteDirectory(instanceDir);
            }
        } else {
            throw new ConfigurationException("There is no instance:" + identifier + " to delete");
        }
    }

    public void deleteAll() throws ConfigurationException {
        final List<Service> services = serviceRepository.findAll();
        for (Service service : services) {
            delete(service.getType(), service.getIdentifier());
        }
    }

    /**
     * Configures a service instance.
     *
     * @param serviceType
     *            The service type (WMS, WFS, ...)
     * @param identifier
     *            the service identifier.
     * @param configuration
     *            the service configuration (depending on implementation).
     * @param details
     *            the service metadata.
     * 
     * @throws org.constellation.configuration.ConfigurationException
     *             if the operation has failed for any reason
     */
    public void configure(final String serviceType, final String identifier, Details details, Object configuration)
            throws ConfigurationException {
        if (identifier == null || identifier.isEmpty()) {
            throw new ConfigurationException("Service instance identifier can't be null or empty.");
        }

        if (configuration == null) {
            configuration = DefaultServiceConfiguration.getDefaultConfiguration(serviceType);
        }

        // write configuration file.
        final Service service = serviceRepository.findByIdentifierAndType(identifier, serviceType);
        if (service == null) {
            throw new ConfigurationException("Service " + serviceType + ':' + identifier + " not found.");
        } else {
            service.setConfig(getStringFromObject(configuration, GenericDatabaseMarshallerPool.getInstance()));
            if (details != null) {
                setInstanceDetails(serviceType, identifier, details, details.getLang(), true);
            } else {
                details = getInstanceDetails(service.getId(), "eng");
            }
            service.setVersions(StringUtils.join(details.getVersions(), "µ"));
            serviceRepository.update(service);
        }
    }

    /**
     * Returns the configuration object of a service instance.
     *
     * @param serviceType
     *            The service type (WMS, WFS, ...)
     * @param identifier
     *            the service
     *
     * @return a configuration {@link Object} (depending on implementation)
     * @throws org.constellation.configuration.ConfigurationException
     *             if the operation has failed for any reason
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

    public Object getExtraConfiguration(final String serviceType, final String identifier, final String fileName)
            throws ConfigurationException {
        return getExtraConfiguration(serviceType, identifier, fileName, GenericDatabaseMarshallerPool.getInstance());
    }

    public Object getExtraConfiguration(final String serviceType, final String identifier, final String fileName, final MarshallerPool pool)
            throws ConfigurationException {
        try {
            final Service service = serviceRepository.findByIdentifierAndType(identifier, serviceType);
            if (service != null) {
                final ServiceExtraConfig conf = serviceRepository.getExtraConfig(service.getId(), fileName);
                if (conf != null) {
                    final String content = conf.getContent();
                    if (content != null) {
                        return getObjectFromString(content, pool);
                    }
                }
            }
        } catch (JAXBException ex) {
            throw new ConfigurationException(ex.getMessage(), ex);
        }
        return null;
    }

    public void setExtraConfiguration(final String serviceType, final String identifier, final String fileName, final Object config,
            final MarshallerPool pool) {
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
        final List<Service> services = serviceRepository.findByType(spec);
        final Map<String, ServiceStatus> status = new HashMap<>();
        for (Service service : services) {
            status.put(service.getIdentifier(), ServiceStatus.valueOf(service.getStatus()));
        }
        return status;
    }

    public List<String> getServiceIdentifiers(final String spec) {
        return serviceRepository.findIdentifiersByType(spec);
    }

    /**
     * Create new worker instance in service directory.
     *
     * @param serviceType
     * @param identifier
     * @param closeInstance
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
                    throw new ConfigurationException("The instance " + identifier
                            + " can't be started, maybe there is no configuration directory with this name.");
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

            for (String instanceID : getServiceIdentifiers(serviceType)) {
                try {
                    final Worker worker = WSEngine.buildWorker(serviceType, instanceID);
                    if (worker != null) {
                        workersMap.put(instanceID, worker);
                    } else {
                        throw new ConfigurationException("The instance " + instanceID
                                + " can be started, maybe there is no configuration directory with this name.");
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
     * @param serviceType
     *            The type of the service.
     * @param identifier
     *            the service identifier
     * @return
     * @throws TargetNotFoundException
     *             if the service with specified identifier does not exist
     * @throws org.constellation.configuration.ConfigurationException
     *             if the operation has failed for any reason
     */
    public Details getInstanceDetails(final String serviceType, final String identifier, final String language)
            throws ConfigurationException {
        final Service service = this.ensureExistingInstance(serviceType, identifier);
        return getInstanceDetails(service.getId(), language);
    }

    public Details getInstanceDetails(final int serviceId, String language) throws ConfigurationException {
        try {
            ServiceDetails details;
            if (language == null) {
                details = serviceRepository.getServiceDetailsForDefaultLang(serviceId);
            } else {
                details = serviceRepository.getServiceDetails(serviceId, language);
            }
            if (details == null) {
                details = serviceRepository.getServiceDetailsForDefaultLang(serviceId);
            }

            return (Details) getObjectFromString(details.getContent(), GenericDatabaseMarshallerPool.getInstance());

        } catch (JAXBException ex) {
            throw new ConstellationException(ex);
        }
    }

    /**
     * Updates a service instance metadata.
     *
     * @param serviceType
     *            The type of the service.
     * @param identifier
     *            the service identifier
     * @param details
     *            the service metadata
     * @throws TargetNotFoundException
     *             if the service with specified identifier does not exist
     * @throws org.constellation.configuration.ConfigurationException
     *             if the operation has failed for any reason
     */
    public void setInstanceDetails(final String serviceType, final String identifier, final Details details, final String language,
            final boolean default_) throws ConfigurationException {
        final Service service = this.ensureExistingInstance(serviceType, identifier);
        if (service != null) {
            final String xml = getStringFromObject(details, GenericDatabaseMarshallerPool.getInstance());
            final ServiceDetails serviceDetails = new ServiceDetails(service.getId(), language, xml, default_);
            serviceRepository.createOrUpdateServiceDetails(serviceDetails);
        }
    }

    /**
     * Ensure that a service instance really exists.
     *
     * @param spec
     *            The service type.
     * @param identifier
     *            the service identifier
     * @throws TargetNotFoundException
     *             if the service with specified identifier does not exist
     */
    public Service ensureExistingInstance(final String spec, final String identifier) throws TargetNotFoundException {
        Service service = serviceRepository.findByIdentifierAndType(identifier, spec);
        if (!WSEngine.serviceInstanceExist(spec.toUpperCase(), identifier)) {
            if (service == null) {
                throw new TargetNotFoundException(spec + " service instance with identifier \"" + identifier
                        + "\" not found. There is not configuration in the database.");
            }
        }
        return service;
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

    // Domain links

    public void addServiceToDomain(int serviceId, int domainId) {
        domainRepository.addServiceToDomain(serviceId, domainId);
    }

    @Transactional("txManager")
    public synchronized void removeServiceFromDomain(int serviceId, int domainId) {
        List<Domain> findByLinkedService = domainRepository.findByLinkedService(serviceId);
        if (findByLinkedService.size() == 1) {
            throw new CstlConfigurationRuntimeException("Could not unlink last domain from a service")
                    .withErrorCode("error.service.lastdomain");
        }
        domainRepository.removeServiceFromDomain(serviceId, domainId);
    }

    public List<ServiceDTO> getAllServicesByDomainId(int domainId, String lang) throws ConfigurationException {
        List<ServiceDTO> serviceDTOs = new ArrayList<>();
        List<Service> services = serviceRepository.findByDomain(domainId);
        for (Service service : services) {
            final Details details = getInstanceDetails(service.getId(), lang);
            final ServiceDTO serviceDTO = convertIntoServiceDto(service, details);
            serviceDTOs.add(serviceDTO);
        }
        return serviceDTOs;
    }

    public List<ServiceDTO> getAllServicesByDomainIdAndType(int domainId, String lang, String type) throws ConfigurationException {
        List<ServiceDTO> serviceDTOs = new ArrayList<>();
        List<Service> services = serviceRepository.findByDomainAndType(domainId, type);
        for (Service service : services) {
            final Details details = getInstanceDetails(service.getId(), lang);
            final ServiceDTO serviceDTO = convertIntoServiceDto(service, details);
            serviceDTOs.add(serviceDTO);
        }
        return serviceDTOs;
    }

    private ServiceDTO convertIntoServiceDto(final Service service, final Details details) {
        final ServiceDTO serviceDTO = new ServiceDTO();
        serviceDTO.setMetadataIso(service.getMetadataIso());
        serviceDTO.setOwner(service.getOwner());
        serviceDTO.setConfig(service.getConfig());
        serviceDTO.setDate(new Date(service.getDate()));
        serviceDTO.setDescription(details != null ? details.getDescription() : "");
        serviceDTO.setId(service.getId());
        serviceDTO.setIdentifier(service.getIdentifier());
        serviceDTO.setMetadataId(service.getMetadataId());
        serviceDTO.setStatus(service.getStatus());
        serviceDTO.setTitle(details != null ? details.getName() : "");
        serviceDTO.setType(service.getType());
        serviceDTO.setVersions(service.getVersions());
        return serviceDTO;
    }

    public Instance getI18nInstance(String serviceType, String identifier, String lang) {
        Instance instance = new Instance();
        final Service service = serviceRepository.findByIdentifierAndType(identifier, serviceType);
        try {
            final Details details = getInstanceDetails(serviceType, identifier, lang);
            instance.setId(service.getId());
            instance.set_abstract(details.getDescription());
            instance.setIdentifier(service.getIdentifier());
            int layersNumber = layerRepository.findByServiceId(service.getId()).size();
            instance.setLayersNumber(layersNumber);
            instance.setName(details.getName());
            instance.setType(service.getType());
            instance.setVersions(Arrays.asList(service.getVersions().split("µ")));
            instance.setStatus(ServiceStatus.valueOf(service.getStatus()));
            return instance;
        } catch (ConfigurationException e) {
            throw new ConstellationException(e);
        }
    }
}
