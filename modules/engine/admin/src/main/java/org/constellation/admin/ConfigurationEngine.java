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

package org.constellation.admin;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.util.logging.Logging;
import org.apache.sis.xml.MarshallerPool;
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
import org.constellation.configuration.ConfigDirectory;
import org.constellation.configuration.DataBrief;
import org.constellation.configuration.ServiceProtocol;
import org.constellation.configuration.StyleBrief;
import org.constellation.dto.CoverageMetadataBean;
import org.constellation.dto.Service;
import org.constellation.engine.register.ConfigurationService;
import org.constellation.engine.register.MetadataIOUtils;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.security.NoSecurityManagerException;
import org.constellation.security.SecurityManager;
import org.constellation.util.Util;
import org.constellation.utils.CstlMetadatas;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.util.FileUtilities;
import org.opengis.parameter.GeneralParameterValue;

/**
 * @author Guilhem Legal (Geomatys)
 */
public class ConfigurationEngine {

    private static final Logger LOGGER = Logging.getLogger(ConfigurationEngine.class);

    public static final String SERVICES_URL_KEY = "services.url";

    /**
     * TODO Temporary hack to activate JPA daos.
     */

    // Spring managed component

    private static SecurityManager securityManager;

    private static ConfigurationService configurationService;


    public static void setSecurityManager(SecurityManager securityManager) {
        ConfigurationEngine.securityManager = securityManager;
    }

    public static void setConfigurationService(ConfigurationService configurationService) {
        ConfigurationEngine.configurationService = configurationService;
    }


    // End of spring managed component.

   
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
            configurationService.storeConfiguration(serviceType, serviceID, fileName, obj, pool, securityManager.getCurrentUserLogin());
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

    public static void writeServiceMetadata(final String identifier, final String serviceType, final Service metadata,
            String language) throws IOException, JAXBException {
        ensureNonNull("metadata", metadata);

        if (language == null) {
            language = "eng";
        }
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            final ServiceRecord service = session.readService(identifier, serviceType);
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
                final StringReader srIso = MetadataIOUtils.marshallMetadata(isoMetadata);

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
            final ServiceRecord rec = session.readService(identifier,serviceType.toLowerCase());
            if (rec != null) {
                final InputStream is = rec.getMetadata(language);
                if (is != null) {
                    final Unmarshaller u = GenericDatabaseMarshallerPool.getInstance().acquireUnmarshaller();
                    final Service config = (Service) u.unmarshal(is);
                    GenericDatabaseMarshallerPool.getInstance().recycle(u);
                    return config;
                } else {
                    final InputStream in = Util.getResourceAsStream("org/constellation/xml/" + serviceType.toUpperCase()
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
                    final DefaultMetadata servMeta = MetadataIOUtils.unmarshallMetadata(record.getIsoMetadata());
                    CstlMetadatas.updateServiceMetadataURL(record.getIdentifier(), record.getType().name(), url,
                            servMeta);
                    final StringReader sr = MetadataIOUtils.marshallMetadata(servMeta);
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
            final StringReader sr = MetadataIOUtils.marshallMetadata(metadata);
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
            final StringReader sr = MetadataIOUtils.marshallMetadata(metadata);
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

            final Record record = session.searchMetadata(metadataID, true);
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

            final Record record = session.searchMetadata(metadataId, true);
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

    public static boolean existInternalMetadata(final String metadataID, final boolean includeService) {
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            final Record record = session.searchMetadata(metadataID, includeService);
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

    public static List<String> getInternalMetadataIds(final boolean includeService) {
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
            if (includeService) {
                final List<ServiceRecord> services = session.readServices();
                for (ServiceRecord record : services) {
                    if (record.hasIsoMetadata()) {
                        results.add(record.getMetadataId());
                    }
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

    public static List<ProviderRecord> getProviders() {
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            return session.readProviders();
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while reading provider records in database", ex);
        } finally {
            if (session != null)
                session.close();
        }
        return new ArrayList<>();
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

        configurationService.deleteProvider(providerID);

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

        configurationService.deleteData(name.getNamespaceURI(), name.getLocalPart(), providerId);

    }

    public static DataRecord writeData(final QName name, final ProviderRecord provider, final DataRecord.DataType type,
            final boolean sensorable) {
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

    public static void updateDataSubtype(final QName name, final String providerId, final String subtype) {
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
            dr.setSubtype(subtype);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "error when try to delete data", e);
        } finally {
            if (session != null)
                session.close();
        }
    }

    public static void linkDataToSensor(final QName name, final String providerId, final String sensorId) {
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            String login = null;
            try {
                login = securityManager.getCurrentUserLogin();
            } catch (NoSecurityManagerException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }

            final DataRecord data = session.readData(name, providerId);
            final SensorRecord sensor = session.readSensor(sensorId);
            session.writeSensoredData(data, sensor);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "error when try to delete data", e);
        } finally {
            if (session != null)
                session.close();
        }
    }

    public static void unlinkDataToSensor(final QName name, final String providerId, final String sensorId) {
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            String login = null;
            try {
                login = securityManager.getCurrentUserLogin();
            } catch (NoSecurityManagerException ex) {
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
            }

            final DataRecord data = session.readData(name, providerId);
            final SensorRecord sensor = session.readSensor(sensorId);
            session.deleteSensoredData(data, sensor);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "error when try to delete data", e);
        } finally {
            if (session != null)
                session.close();
        }
    }

    public static List<DataRecord> getDataLinkedSensor(final String sensorId) {
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            final SensorRecord sensor = session.readSensor(sensorId);
            if (sensor != null) {
                return session.readSensoredDataFromSensor(sensor);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "error when try to read data linked to sensor", e);
        } finally {
            if (session != null)
                session.close();
        }
        return new ArrayList<>();
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
        db.setSubtype(record.getSubtype());
        db.setSensorable(record.isSensorable());
        db.setTargetSensor(record.getLinkedSensors());

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
    public static LayerRecord getLayer(final String identifier, final String specification,
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

    public static void deleteLayer(final String identifier, final String specification,
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

}
