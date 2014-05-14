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

package org.constellation.admin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.apache.commons.io.IOUtils;
import org.apache.sis.metadata.iso.DefaultMetadata;
import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import org.apache.sis.util.logging.Logging;
import org.apache.sis.xml.MarshallerPool;
import org.apache.sis.xml.XML;
import org.constellation.ServiceDef;
import org.constellation.admin.dao.DataRecord;
import org.constellation.admin.dao.LayerRecord;
import org.constellation.admin.dao.ProviderRecord;
import org.constellation.admin.dao.Record;
import org.constellation.admin.dao.SensorRecord;
import org.constellation.admin.dao.ServiceRecord;
import org.constellation.admin.dao.Session;
import org.constellation.admin.dao.StyleRecord;
import org.constellation.admin.dao.TaskRecord;
import org.constellation.admin.util.IOUtilities;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.configuration.DataBrief;
import org.constellation.configuration.Layer;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.ServiceProtocol;
import org.constellation.configuration.Source;
import org.constellation.configuration.StyleBrief;
import org.constellation.dto.CoverageMetadataBean;
import org.constellation.dto.Service;
import org.constellation.engine.register.ConfigurationService;
import org.constellation.engine.register.Provider;
import org.constellation.engine.register.repository.ProviderRepository;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.security.NoSecurityManagerException;
import org.constellation.security.SecurityManager;
import org.constellation.util.Util;
import org.constellation.utils.CstlMetadatas;
import org.constellation.utils.ISOMarshallerPool;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.util.FileUtilities;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;

/**
 * @author Guilhem Legal (Geomatys)
 */
public class ConfigurationEngine {

    private static final Logger LOGGER = Logging.getLogger(ConfigurationEngine.class);

    public static final String SERVICES_URL_KEY = "services.url";

    /**
     * TODO Temporary hack to activate JPA daos.
     */

    
    //Spring managed component 
    
    private static SecurityManager securityManager;

    private static ConfigurationService configurationService;
    
    private static ProviderRepository providerRepository;

    public static void setSecurityManager(SecurityManager securityManager) {
        ConfigurationEngine.securityManager = securityManager;
    }

    public static void setConfigurationService(ConfigurationService configurationService) {
        ConfigurationEngine.configurationService = configurationService;
    }

    public static void setProviderRepository(ProviderRepository providerRepository) {
        ConfigurationEngine.providerRepository = providerRepository;
    }

    //End of spring managed component.
    
    public static ParameterValueGroup getProviderConfiguration(final String serviceName,
            final ParameterDescriptorGroup desc) {

        final ParameterValueGroup params = desc.createValue();
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            final List<ProviderRecord> records = session.readProviders(serviceName);
            for (ProviderRecord record : records) {
                params.values().add(record.getConfig(desc.descriptor("source")));
            }
            return params;

        } catch (IOException | SQLException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while updating provider database", ex);
        } finally {
            if (session != null)
                session.close();
        }
        return null;
    }

   

    public static void storePoviderConfiguration(final String serviceName, final ParameterValueGroup params) {
        // TODO move from Configurator
    }

    public static Object getConfiguration(final String serviceType, final String serviceID) throws JAXBException,
            FileNotFoundException {
        return getConfiguration(serviceType, serviceID, null);
    }

    public static Object getConfiguration(final String serviceType, final String serviceID, final String fileName)
            throws JAXBException, FileNotFoundException {
        return getConfiguration(serviceType, serviceID, fileName, GenericDatabaseMarshallerPool.getInstance());
    }

    public static Object getConfiguration(final String serviceType, final String serviceID, final String fileName,
            final MarshallerPool pool) throws JAXBException, FileNotFoundException {

        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            final ServiceRecord rec = session.readService(serviceID,
                    ServiceDef.Specification.fromShortName(serviceType));
            if (rec != null) {
                final InputStream is;
                if (fileName == null) {
                    is = rec.getConfig();
                } else {
                    is = rec.getExtraFile(fileName);
                }
                if (is != null) {
                    final Unmarshaller u = pool.acquireUnmarshaller();
                    final Object config = u.unmarshal(is);
                    pool.recycle(u);
                    return config;
                }
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while updating provider database", ex);
        } finally {
            if (session != null)
                session.close();
        }

        throw new FileNotFoundException("The configuration (" + (fileName != null ? fileName : "default")
                + ") has not been found.");
    }

    public static void storeConfiguration(final String serviceType, final String serviceID, final Object obj,
            final Service metadata) throws JAXBException, IOException {
        storeConfiguration(serviceType, serviceID, null, obj, GenericDatabaseMarshallerPool.getInstance());
        if (metadata != null) {
            writeServiceMetadata(serviceID, serviceType, metadata, null);
        }
    }

    public static void storeConfiguration(final String serviceType, final String serviceID, final Object obj)
            throws JAXBException {
        storeConfiguration(serviceType, serviceID, null, obj, GenericDatabaseMarshallerPool.getInstance());
    }

    public static void storeConfiguration(final String serviceType, final String serviceID, final String fileName,
            final Object obj, final MarshallerPool pool) throws JAXBException {
        final ServiceDef.Specification spec = ServiceDef.Specification.fromShortName(serviceType);
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();

            final StringReader sr;
            if (obj != null) {
                final StringWriter sw = new StringWriter();
                final Marshaller m = pool.acquireMarshaller();
                m.marshal(obj, sw);
                pool.recycle(m);
                sr = new StringReader(sw.toString());

            } else {
                sr = null;
            }

            final String login = securityManager.getCurrentUserLogin();
            ServiceRecord service = session.readService(serviceID, spec);
            if (service == null) {
                if (fileName == null) {
                    service = session.writeService(serviceID, spec, sr, login);
                } else {
                    session.writeServiceExtraConfig(serviceID, spec, sr, fileName);
                }
            } else {
                if (obj instanceof LayerContext) {
                    session.deleteServiceLayer(service);
                    // save Layers
                    LayerContext context = (LayerContext) obj;
                    for (Source source : context.getLayers()) {
                        for (Layer layer : source.getInclude()) {
                            final QName dataName = layer.getName();
                            final DataRecord data = session.readData(dataName, source.getId());
                            session.writeLayer(dataName, layer.getAlias(), service, data, "", login);
                        }
                    }
                }

                if (fileName == null) {
                    service.setConfig(sr);
                } else {
                    service.setExtraFile(fileName, sr);
                }
            }

            // update service metadata for a WxS
            if (obj instanceof LayerContext) {
                final LayerContext context = (LayerContext) obj;
                final List<String> layerIds = new ArrayList<>();
                for (Source src : context.getLayers()) {
                    if (src.getLoadAll()) {
                        // TODO
                    } else {
                        for (Layer layer : src.getInclude()) {
                            final DataRecord record = session.readData(layer.getName(), src.getId());
                            if (record != null && record.hasIsoMetadata()) {
                                layerIds.add(record.getMetadataId());
                            }
                        }
                    }
                }
                if (service.hasIsoMetadata()) {
                    final DefaultMetadata servMeta = unmarshallMetadata(service.getIsoMetadata());
                    CstlMetadatas.updateServiceMetadataLayer(servMeta, layerIds);
                    final StringReader srm = marshallMetadata(servMeta);
                    service.setIsoMetadata(servMeta.getFileIdentifier(), srm);
                }
            }

        } catch (SQLException | IOException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while updating service database", ex);
        } finally {
            if (session != null)
                session.close();
        }
    }

    public static List<String> getServiceConfigurationIds(final String serviceType) {

        final List<String> results = new ArrayList<>();
        final ServiceDef.Specification spec = ServiceDef.Specification.fromShortName(serviceType);
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            final List<ServiceRecord> records = session.readServices(spec);
            for (ServiceRecord record : records) {
                results.add(record.getIdentifier());
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while get services in database", ex);
        } finally {
            if (session != null)
                session.close();
        }
        return results;
    }

    public static boolean serviceConfigurationExist(final String serviceType, final String identifier) {

        final ServiceDef.Specification spec = ServiceDef.Specification.fromShortName(serviceType);
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            return session.readService(identifier, spec) != null;

        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while get services in database", ex);
        } finally {
            if (session != null)
                session.close();
        }
        return false;
    }

    public static boolean deleteConfiguration(final String serviceType, final String identifier) {

        final ServiceDef.Specification spec = ServiceDef.Specification.fromShortName(serviceType);
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            // look fr existence
            final ServiceRecord serv = session.readService(identifier, spec);
            if (serv != null) {
                final File instanceDir = ConfigDirectory.getInstanceDirectory(serviceType, identifier);
                if (instanceDir.isDirectory()) {
                    FileUtilities.deleteDirectory(instanceDir);
                }
                session.deleteService(identifier, spec);
                return true;
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while deleting service in database", ex);
        } finally {
            if (session != null)
                session.close();
        }
        return false;
    }

    public static boolean renameConfiguration(final String serviceType, final String identifier, final String newID) {
        final ServiceDef.Specification spec = ServiceDef.Specification.fromShortName(serviceType);
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            final ServiceRecord service = session.readService(identifier, spec);
            service.setIdentifier(identifier);
            return true;
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while deleting service in database", ex);
            return false;
        } finally {
            if (session != null)
                session.close();
        }
    }

    public static void writeServiceMetadata(final String identifier, final String serviceType, final Service metadata,
            String language) throws IOException, JAXBException {
        ensureNonNull("metadata", metadata);

        if (language == null) {
            language = "eng";
        }
        final ServiceDef.Specification spec = ServiceDef.Specification.fromShortName(serviceType);
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            final ServiceRecord service = session.readService(identifier, spec);
            if (service != null) {
                final StringWriter sw = new StringWriter();
                final Marshaller m = GenericDatabaseMarshallerPool.getInstance().acquireMarshaller();
                m.marshal(metadata, sw);
                GenericDatabaseMarshallerPool.getInstance().recycle(m);
                final StringReader sr = new StringReader(sw.toString());
                service.setMetadata(language, sr);

                // ISO metadata
                String url = getConstellationProperty(SERVICES_URL_KEY, null);
                final DefaultMetadata isoMetadata = CstlMetadatas.defaultServiceMetadata(identifier, serviceType, url,
                        metadata);
                final StringReader srIso = marshallMetadata(isoMetadata);

                service.setIsoMetadata(isoMetadata.getFileIdentifier(), srIso);
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while updating service database", ex);
        } finally {
            if (session != null)
                session.close();
        }
    }

    public static Service readServiceMetadata(final String identifier, final String serviceType, String language)
            throws IOException, JAXBException {
        ensureNonNull("identifier", identifier);
        ensureNonNull("serviceType", serviceType);
        if (language == null) {
            language = "eng";
        }
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            final ServiceRecord rec = session.readService(identifier,
                    ServiceDef.Specification.fromShortName(serviceType));
            if (rec != null) {
                final InputStream is = rec.getMetadata(language);
                if (is != null) {
                    final Unmarshaller u = GenericDatabaseMarshallerPool.getInstance().acquireUnmarshaller();
                    final Service config = (Service) u.unmarshal(is);
                    GenericDatabaseMarshallerPool.getInstance().recycle(u);
                    return config;
                } else {
                    final InputStream in = Util.getResourceAsStream("org/constellation/xml/" + serviceType
                            + "Capabilities.xml");
                    if (in != null) {
                        final Unmarshaller u = GenericDatabaseMarshallerPool.getInstance().acquireUnmarshaller();
                        final Service metadata = (Service) u.unmarshal(in);
                        GenericDatabaseMarshallerPool.getInstance().recycle(u);
                        in.close();
                        metadata.setIdentifier(identifier);
                        return metadata;
                    } else {
                        throw new IOException("Unable to find the capabilities skeleton from resource.");
                    }
                }
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while updating provider database", ex);
        } finally {
            if (session != null)
                session.close();
        }
        return null;
    }

    public static void clearDatabase() {
        EmbeddedDatabase.clear();
    }

    public static File setupTestEnvironement(final String directoryName) {
        final File configDir = new File(directoryName);
        if (configDir.exists()) {
            FileUtilities.deleteDirectory(configDir);
        }
        configDir.mkdir();
        ConfigDirectory.setConfigDirectory(configDir);

        setSecurityManager(new DummySecurityManager());
        return configDir;
    }

    public static void shutdownTestEnvironement(final String directoryName) {
        FileUtilities.deleteDirectory(new File(directoryName));
        clearDatabase();
        ConfigDirectory.setConfigDirectory(null);
        setSecurityManager(null);
    }

    public static String getConstellationProperty(final String key, final String defaultValue) {
        return configurationService.getProperty(key, defaultValue);
    }

    public static void setConstellationProperty(final String key, final String value) {
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            if (session.readProperty(key) == null) {
                session.writeProperty(key, value);
            } else {
                session.updateProperty(key, value);
            }

            // update metadata when service URL key is updated
            if (SERVICES_URL_KEY.equals(key)) {
                updateServiceUrlForMetadata(value);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "An error occurred getting constellation property", ex);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public static Properties getMetadataTemplateProperties() {
        final File cstlDir = ConfigDirectory.getConfigDirectory();
        final File propFile = new File(cstlDir, "metadataTemplate.properties");
        final Properties prop = new Properties();
        if (propFile.exists()) {
            try {
                prop.load(new FileReader(propFile));
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "IOException while loading metadata template properties file", ex);
            }
        }
        return prop;
    }

    private static void updateServiceUrlForMetadata(final String url) {
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            final List<ServiceRecord> records = session.readServices();
            for (ServiceRecord record : records) {
                if (record.hasIsoMetadata()) {
                    final DefaultMetadata servMeta = unmarshallMetadata(record.getIsoMetadata());
                    CstlMetadatas.updateServiceMetadataURL(record.getIdentifier(), record.getType().name(), url,
                            servMeta);
                    final StringReader sr = marshallMetadata(servMeta);
                    record.setIsoMetadata(servMeta.getFileIdentifier(), sr);
                }
            }
        } catch (SQLException | JAXBException | IOException ex) {
            LOGGER.log(Level.WARNING, "An error occurred updating service URL", ex);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    /**
     * Save metadata on specific folder
     * 
     * @param metadata
     * @param dataName
     */
    public static void saveProviderMetadata(final DefaultMetadata metadata, final String dataName) {
        ensureNonNull("metadata", metadata);

        // save in database
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            final StringReader sr = marshallMetadata(metadata);
            final ProviderRecord provider = session.readProvider(dataName);
            if (provider != null) {
                provider.setMetadata(metadata.getFileIdentifier(), sr);
            }

        } catch (SQLException | IOException | JAXBException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while updating service database", ex);
        } finally {
            if (session != null)
                session.close();
        }
    }

    /**
     * Save metadata on specific folder
     * 
     * @param metadata
     * @param dataName
     * @param providerId
     */
    public static void saveDataMetadata(final DefaultMetadata metadata, final QName dataName, final String providerId) {
        ensureNonNull("metadata", metadata);

        // save in database
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            final StringReader sr = marshallMetadata(metadata);
            final DataRecord data = session.readData(dataName, providerId);
            if (data != null) {
                data.setIsoMetadata(metadata.getFileIdentifier(), sr);
            }

        } catch (SQLException | IOException | JAXBException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while updating service database", ex);
        } finally {
            if (session != null)
                session.close();
        }
    }

    /**
     * Load a metadata for a provider.
     * 
     * @param providerId
     * @param pool
     * @return
     */
    public static DefaultMetadata loadProviderMetadata(final String providerId, final MarshallerPool pool) {
        Session session = null;
        DefaultMetadata metadata = null;
        try {
            session = EmbeddedDatabase.createSession();
            final ProviderRecord provider = session.readProvider(providerId);
            if (provider != null) {
                final InputStream sr = provider.getMetadata();
                final Unmarshaller m = pool.acquireUnmarshaller();
                if (sr != null) {
                    metadata = (DefaultMetadata) m.unmarshal(sr);
                }
                pool.recycle(m);
                return metadata;
            }
        } catch (SQLException | IOException | JAXBException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while updating service database", ex);
        } finally {
            if (session != null)
                session.close();
        }
        return null;
    }

    /**
     * Load a metadata for a provider.
     *
     * @param providerId
     * @param dataId
     * @param pool
     * @return
     */
    public static DefaultMetadata loadIsoDataMetadata(final String providerId, final QName dataId,
            final MarshallerPool pool) {
        Session session = null;
        DefaultMetadata metadata = null;
        try {
            session = EmbeddedDatabase.createSession();
            final DataRecord dr = session.readData(dataId, providerId);
            if (dr != null) {
                final InputStream sr = dr.getIsoMetadata();
                final Unmarshaller m = pool.acquireUnmarshaller();
                if (sr != null) {
                    metadata = (DefaultMetadata) m.unmarshal(sr);
                }
                pool.recycle(m);
                return metadata;
            }
        } catch (SQLException | IOException | JAXBException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while updating service database", ex);
        } finally {
            if (session != null)
                session.close();
        }
        return null;
    }

    public static InputStream loadIsoMetadata(final String metadataID) {
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();

            final Record record = session.searchMetadata(metadataID);
            if (record instanceof ProviderRecord) {
                final ProviderRecord provider = (ProviderRecord) record;
                return provider.getMetadata();

            } else if (record instanceof ServiceRecord) {
                final ServiceRecord serv = (ServiceRecord) record;
                return serv.getIsoMetadata();

            } else if (record instanceof DataRecord) {
                final DataRecord data = (DataRecord) record;
                return data.getIsoMetadata();
            }

        } catch (SQLException | IOException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while reading provider metadata", ex);
        } finally {
            if (session != null)
                session.close();
        }
        return null;
    }

    public static List<DataBrief> getDataRecordsForMetadata(final String metadataId) {
        final List<DataRecord> records = new ArrayList<>();
        final List<DataBrief> recordsBrief = new ArrayList<>();

        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();

            final Record record = session.searchMetadata(metadataId);
            if (record instanceof DataRecord) {
                records.add((DataRecord) record);
            } else if (record instanceof ProviderRecord) {
                final ProviderRecord provider = (ProviderRecord) record;
                records.addAll(provider.getData());
            } else if (record instanceof ServiceRecord) {
                final ServiceRecord serv = (ServiceRecord) record;
                final List<LayerRecord> layers = session.readLayers(serv);
                for (final LayerRecord layer : layers) {
                    records.add(layer.getData());
                }
            }

            for (final DataRecord rec : records) {
                recordsBrief.add(_getDataBrief(session, rec));
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while reading provider metadata", ex);
        } finally {
            if (session != null)
                session.close();
        }

        return recordsBrief;
    }

    public static boolean existInternalMetadata(final String metadataID) {
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            final Record record = session.searchMetadata(metadataID);
            return record != null;

        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while looking for provider metadata existance", ex);
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return false;
    }

    public static List<String> getProviderIds() {
        return getProviderIds(false);
    }

    public static List<String> getProviderIds(final boolean hasMetadata) {
        final List<String> results = new ArrayList<>();
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            final List<ProviderRecord> providers = session.readProviders();
            for (ProviderRecord record : providers) {
                if (hasMetadata) {
                    if (record.hasMetadata()) {
                        results.add(record.getIdentifier());
                    }
                } else {
                    results.add(record.getIdentifier());
                }
            }
        } catch (SQLException | IOException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while updating service database", ex);
        } finally {
            if (session != null)
                session.close();
        }
        return results;
    }

    public static List<String> getInternalMetadataIds() {
        final List<String> results = new ArrayList<>();
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            final List<ProviderRecord> providers = session.readProviders();
            for (ProviderRecord record : providers) {
                if (record.hasMetadata()) {
                    results.add(record.getMetadataId());
                }
            }
            final List<ServiceRecord> services = session.readServices();
            for (ServiceRecord record : services) {
                if (record.hasIsoMetadata()) {
                    results.add(record.getMetadataId());
                }
            }
            final List<DataRecord> datas = session.readData();
            for (DataRecord record : datas) {
                if (record.isVisible() && record.hasIsoMetadata()) {
                    results.add(record.getMetadataId());
                }
            }
        } catch (SQLException | IOException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while updating service database", ex);
        } finally {
            if (session != null)
                session.close();
        }
        return results;
    }

    public static List<ProviderRecord> getProviders(final String serviceName) {
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            return session.readProviders(serviceName);
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while updating service database", ex);
        } finally {
            if (session != null)
                session.close();
        }
        return new ArrayList<>();
    }

    public static List<ProviderRecord> getProvidersFromParent(final String parentIdentifier) {
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            return session.readProvidersFromParent(parentIdentifier);
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while updating service database", ex);
        } finally {
            if (session != null)
                session.close();
        }
        return new ArrayList<>();
    }

    public static ProviderRecord getProvider(final String providerID) {
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            return session.readProvider(providerID);

        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while updating service database", ex);
        } finally {
            if (session != null)
                session.close();
        }
        return null;
    }

    public static void deleteProvider(final String providerID) {
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            session.deleteProvider(providerID);

        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while updating service database", ex);
        } finally {
            if (session != null)
                session.close();
        }
    }

    public static ProviderRecord writeProvider(final String identifier, final String parent,
            final ProviderRecord.ProviderType type, final String serviceName, final GeneralParameterValue config) {
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();

            String login = null;
            try {
                login = securityManager.getCurrentUserLogin();
            } catch (NoSecurityManagerException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }

            if (login == null) {
                // FIXME Wahhhhhhhhh !
                login = "admin";
            }
            return session.writeProvider(identifier, parent, type, serviceName, config, login);

        } catch (SQLException | IOException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while writing provider in database", ex);
        } finally {
            if (session != null)
                session.close();
        }
        return null;
    }

    public static void updateProvider(final ProviderRecord updatedProvider) {
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            session.updateProvider(updatedProvider);

        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while updating service database", ex);
        } finally {
            if (session != null)
                session.close();
        }
    }
    
    public static List<SensorRecord> getSensors() {
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            return session.readSensors();
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while reading sensor database", ex);
        } finally {
            if (session != null)
                session.close();
        }
        return new ArrayList<>();
    }
    
    public static List<SensorRecord> getSensorChildren(final String parentIdentififer) {
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            return session.readSensorsFromParent(parentIdentififer);
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while reading sensor database", ex);
        } finally {
            if (session != null)
                session.close();
        }
        return new ArrayList<>();
    }

    public static SensorRecord getSensor(final String sensorID) {
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            return session.readSensor(sensorID);

        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while reading service database", ex);
        } finally {
            if (session != null)
                session.close();
        }
        return null;
    }
    
    public static void deleteSensor(final String sensorID) {
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            session.deleteSensor(sensorID);

        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while updating service database", ex);
        } finally {
            if (session != null)
                session.close();
        }
    }

    public static SensorRecord writeSensor(final String identifier, final String type, final String parent) {
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();

            String login = null;
            try {
                login = securityManager.getCurrentUserLogin();
            } catch (NoSecurityManagerException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }

            if (login == null) {
                // FIXME Wahhhhhhhhh !
                login = "admin";
            }
            return session.writeSensor(identifier, type, parent, login);

        } catch (SQLException | IOException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while writing sensor in database", ex);
        } finally {
            if (session != null)
                session.close();
        }
        return null;
    }
    
    /**
     * Load a metadata for a provider.
     * 
     * 
     * @param providerId
     * @param pool
     * @param name
     * @return
     */
    public static CoverageMetadataBean loadDataMetadata(final String providerId, final QName name,
            final MarshallerPool pool) {
        Session session = null;
        CoverageMetadataBean metadata = null;
        try {
            session = EmbeddedDatabase.createSession();
            final DataRecord data = session.readData(name, providerId);
            if (data != null) {
                final InputStream sr = data.getMetadata();
                final Unmarshaller m = pool.acquireUnmarshaller();
                if (sr != null) {
                    metadata = (CoverageMetadataBean) m.unmarshal(sr);
                }
                pool.recycle(m);
                return metadata;
            }
        } catch (SQLException | IOException | JAXBException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while updating service database", ex);
        } finally {
            if (session != null)
                session.close();
        }
        return null;
    }

    /**
     * @param name
     * @param providerId
     * @return
     */

    public static DataBrief getData(QName name, String providerId) {
        return _getData(name, providerId);
    }

    public static void deleteData(final QName name, final String providerId) {
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            session.deleteData(name, providerId);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "error when try to delete data", e);
        } finally {
            if (session != null)
                session.close();
        }
    }

    public static DataRecord writeData(final QName name, final ProviderRecord provider, final DataRecord.DataType type, final boolean sensorable) {
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            String login = null;
            try {
                login = securityManager.getCurrentUserLogin();
            } catch (NoSecurityManagerException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }

            if (login == null) {
                login = "admin";
            }
            return session.writeData(name, provider, type, login, sensorable);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "error when try to delete data", e);
        } finally {
            if (session != null)
                session.close();
        }
        return null;
    }

    public static void updateDataVisibility(final QName name, final String providerId, final boolean visible) {
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            String login = null;
            try {
                login = securityManager.getCurrentUserLogin();
            } catch (NoSecurityManagerException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }

            final DataRecord dr = session.readData(name, providerId);
            if (dr != null) {
                dr.setVisible(visible);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "error when try to delete data", e);
        } finally {
            if (session != null)
                session.close();
        }
    }

    public static void updateDataSensorable(final QName name, final String providerId, final boolean sensorable) {
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            String login = null;
            try {
                login = securityManager.getCurrentUserLogin();
            } catch (NoSecurityManagerException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }

            final DataRecord dr = session.readData(name, providerId);
            dr.setSensorable(sensorable);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "error when try to delete data", e);
        } finally {
            if (session != null)
                session.close();
        }
    }

    private static DataBrief _getData(QName name, String providerId) {
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            final DataRecord record = session.readData(name, providerId);
            if (record != null) {
                return _getDataBrief(session, record);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "error when try to read data", e);
        } finally {
            if (session != null)
                session.close();
        }
        return null;
    }

    public static DataRecord getDataRecord(QName name, String providerId) {
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            return session.readData(name, providerId);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "error when try to read data", e);
        } finally {
            if (session != null)
                session.close();
        }
        return null;
    }

    /**
     * @param layerAlias
     * @param providerId
     * @return
     */
    public static DataBrief getDataLayer(final String layerAlias, final String providerId) {
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            DataRecord record = session.readDatafromLayer(layerAlias, providerId);
            if (record != null) {
                return _getDataBrief(session, record);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error on sql execution when search data layer", e);
        } finally {
            if (session != null)
                session.close();
        }
        return null;
    }

    /**
     * create a {@link org.constellation.configuration.DataBrief} with style
     * link and service link.
     * 
     * @param session
     *            current {@link org.constellation.admin.dao.Session} used
     * @param record
     *            data found
     * @return a {@link org.constellation.configuration.DataBrief} with all
     *         informations linked.
     * @throws SQLException
     *             if they have an error when search style, service or provider.
     */

    private static DataBrief _getDataBrief(final Session session, final DataRecord record) throws SQLException {
        final List<StyleRecord> styleRecords = session.readStyles(record);
        final List<ServiceRecord> serviceRecords = session.readDataServices(record);

        final DataBrief db = new DataBrief();
        db.setOwner(record.getOwnerLogin());
        db.setName(record.getName());
        db.setNamespace(record.getNamespace());
        db.setDate(record.getDate());
        db.setProvider(record.getProvider().getIdentifier());
        db.setType(record.getType().toString());
        db.setSensorable(record.isSensorable());
        db.setUsedAsSensor(record.isUsedAsSensor());

        final List<StyleBrief> styleBriefs = new ArrayList<>(0);
        for (StyleRecord styleRecord : styleRecords) {
            final StyleBrief sb = new StyleBrief();
            sb.setType(styleRecord.getType().toString());
            sb.setProvider(styleRecord.getProvider().getIdentifier());
            sb.setDate(styleRecord.getDate());
            sb.setName(styleRecord.getName());
            sb.setOwner(styleRecord.getOwnerLogin());
            styleBriefs.add(sb);
        }
        db.setTargetStyle(styleBriefs);

        final List<ServiceProtocol> serviceProtocols = new ArrayList<>(0);
        for (ServiceRecord serviceRecord : serviceRecords) {
            final List<String> protocol = new ArrayList<>(0);
            protocol.add(serviceRecord.getType().name());
            protocol.add(serviceRecord.getType().fullName);
            final ServiceProtocol sp = new ServiceProtocol(serviceRecord.getIdentifier(), protocol);
            serviceProtocols.add(sp);
        }
        db.setTargetService(serviceProtocols);

        return db;
    }

    // FIXME LayerRecord should not be exposed!
    public static LayerRecord getLayer(final String identifier, final ServiceDef.Specification specification,
            final QName name) {

        Session session = null;

        try {
            session = EmbeddedDatabase.createSession();
            ServiceRecord service = session.readService(identifier, specification);
            LayerRecord record = session.readLayer(name.getLocalPart(), service);
            return record;

        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return null;
    }

    public static void deleteLayer(final String identifier, final ServiceDef.Specification specification,
            final QName name) {
        Session session = null;

        try {
            session = EmbeddedDatabase.createSession();
            ServiceRecord service = session.readService(identifier, specification);
            session.deleteLayer(name, service);

        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public static List<StyleRecord> getStyleForData(final DataRecord record) {
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            return session.readStyles(record);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "error when try to read data", e);
        } finally {
            if (session != null)
                session.close();
        }
        return null;
    }

    public static StyleRecord getStyle(final String name, final String providerId) {
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            return session.readStyle(name, providerId);

        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return null;
    }

    public static StyleRecord writeStyle(final String name, final ProviderRecord provider,
            final StyleRecord.StyleType type, final MutableStyle body) {
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            String login = null;
            try {
                login = securityManager.getCurrentUserLogin();
            } catch (NoSecurityManagerException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }
            if (login == null) {
                login = "admin";
            }
            return session.writeStyle(name, provider, type, body, login);
        } catch (SQLException | IOException e) {
            LOGGER.log(Level.WARNING, "error when try to delete data", e);
        } finally {
            if (session != null)
                session.close();
        }
        return null;
    }

    public static void writeStyleForData(final StyleRecord style, final DataRecord record) {
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            session.writeStyledData(style, record);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "error when try to read data", e);
        } finally {
            if (session != null)
                session.close();
        }
    }

    public static void deleteStyleForData(final StyleRecord style, final DataRecord record) {
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            session.deleteStyledData(style, record);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "error when try to read data", e);
        } finally {
            if (session != null)
                session.close();
        }
    }

    public static void deleteStyle(final String name, final String providerId) {
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            session.deleteStyle(name, providerId);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "error when try to delete data", e);
        } finally {
            if (session != null)
                session.close();
        }
    }

    public static void writeCRSData(final QName name, final String providerId, final String layer) {
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            final DataRecord record = session.readData(name, providerId);
            session.writeCRSData(record, layer);

        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "error when try to delete data", e);
        } finally {
            if (session != null)
                session.close();
        }
    }

    public static TaskRecord getTask(final String uuidTask) {
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            return session.readTask(uuidTask);

        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return null;
    }

    public static void writeTask(final String identifier, final String type, final String owner) {
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            session.writeTask(identifier, type, owner);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "error when try to read data", e);
        } finally {
            if (session != null)
                session.close();
        }
    }

    private static DefaultMetadata unmarshallMetadata(final InputStream stream) throws JAXBException {
        final Unmarshaller um = ISOMarshallerPool.getInstance().acquireUnmarshaller();
        final DefaultMetadata meta = (DefaultMetadata) um.unmarshal(stream);
        ISOMarshallerPool.getInstance().recycle(um);
        return meta;
    }

    private static StringReader marshallMetadata(final DefaultMetadata meta) throws JAXBException {
        final StringWriter swIso = new StringWriter();
        final Marshaller mi = ISOMarshallerPool.getInstance().acquireMarshaller();
        mi.setProperty(XML.TIMEZONE, TimeZone.getTimeZone("GMT+2:00"));
        mi.marshal(meta, swIso);
        ISOMarshallerPool.getInstance().recycle(mi);
        return new StringReader(swIso.toString());
    }
}
