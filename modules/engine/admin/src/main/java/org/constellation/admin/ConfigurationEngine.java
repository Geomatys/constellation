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


import org.apache.shiro.SecurityUtils;
import org.apache.shiro.UnavailableSecurityManagerException;
import org.apache.shiro.subject.Subject;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.util.logging.Logging;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.ServiceDef;
import org.constellation.admin.dao.DataRecord;
import org.constellation.admin.dao.LayerRecord;
import org.constellation.admin.dao.ProviderRecord;
import org.constellation.admin.dao.ServiceRecord;
import org.constellation.admin.dao.Session;
import org.constellation.admin.dao.StyleRecord;
import org.constellation.admin.dao.UserRecord;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.configuration.DataBrief;
import org.constellation.configuration.Layer;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.ServiceProtocol;
import org.constellation.configuration.Source;
import org.constellation.configuration.StyleBrief;
import org.constellation.dto.Service;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.util.Util;
import org.geotoolkit.util.FileUtilities;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;

/**
 * @author Guilhem Legal (Geomatys)
 */
public class ConfigurationEngine {

    private static final Logger LOGGER = Logging.getLogger(ConfigurationEngine.class);

    public static ParameterValueGroup getProviderConfiguration(final String serviceName, final ParameterDescriptorGroup desc) {

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
            if (session != null) session.close();
        }
        return null;
    }

    public static void storePoviderConfiguration(final String serviceName, final ParameterValueGroup params) {
        // TODO move from Configurator
    }

    public static Object getConfiguration(final String serviceType, final String serviceID) throws JAXBException, FileNotFoundException {
        return getConfiguration(serviceType, serviceID, null);
    }

    public static Object getConfiguration(final String serviceType, final String serviceID, final String fileName) throws JAXBException, FileNotFoundException {
        return getConfiguration(serviceType, serviceID, fileName, GenericDatabaseMarshallerPool.getInstance());
    }

    public static Object getConfiguration(final String serviceType, final String serviceID, final String fileName, final MarshallerPool pool) throws JAXBException, FileNotFoundException {
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            final ServiceRecord rec = session.readService(serviceID, ServiceDef.Specification.fromShortName(serviceType));
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
            if (session != null) session.close();
        }

        throw new FileNotFoundException("The configuration (" + fileName != null ? fileName : "default" + ") has not been found.");
    }

    public static void storeConfiguration(final String serviceType, final String serviceID, final Object obj, final Service metadata) throws JAXBException, IOException {
        storeConfiguration(serviceType, serviceID, null, obj, GenericDatabaseMarshallerPool.getInstance());
        if (metadata != null) {
            writeServiceMetadata(serviceID, serviceType, metadata, null);
        }
    }

    public static void storeConfiguration(final String serviceType, final String serviceID, final Object obj) throws JAXBException {
        storeConfiguration(serviceType, serviceID, null, obj, GenericDatabaseMarshallerPool.getInstance());
    }

    public static void storeConfiguration(final String serviceType, final String serviceID, final String fileName, final Object obj, final MarshallerPool pool) throws JAXBException {
        final ServiceDef.Specification spec = ServiceDef.Specification.fromShortName(serviceType);
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();

            //get current user credential
            Subject subject = null;
            try {
                subject = SecurityUtils.getSubject();
            }catch (UnavailableSecurityManagerException ex){
                LOGGER.log(Level.WARNING, ex.getLocalizedMessage());
            }

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

            UserRecord userRecord = session.readUser(subject.getPrincipal().toString());
            final ServiceRecord service = session.readService(serviceID, spec);
            if (service == null) {
                if (fileName == null) {

                    session.writeService(serviceID, spec, sr, userRecord);
                } else {
                    session.writeServiceExtraConfig(serviceID, spec, sr, fileName);
                }
            } else {
                if (obj instanceof LayerContext) {
                    session.deleteServiceLayer(service);
                    //save Layers
                    LayerContext context = (LayerContext) obj;
                    for (Source source : context.getLayers()) {
                        for (Layer layer : source.getInclude()) {
                            final QName dataName = layer.getName();
                            final DataRecord data = session.readData(dataName, source.getId());
                            session.writeLayer(dataName, layer.getAlias(), service, data, "", userRecord);
                        }
                    }
                }

                if (fileName == null) {
                    service.setConfig(sr);
                } else {
                    service.setExtraFile(fileName, sr);
                }
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while updating service database", ex);
        } finally {
            if (session != null) session.close();
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
            if (session != null) session.close();
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
            if (session != null) session.close();
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
                session.deleteService(identifier, spec);
                return true;
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while deleting service in database", ex);
        } finally {
            if (session != null) session.close();
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
            if (session != null) session.close();
        }
    }

    public static void writeServiceMetadata(final String identifier, final String serviceType, final Service metadata, String language) throws IOException, JAXBException {
        ensureNonNull("metadata", metadata);

        if (language == null) {
            language = "eng";
        }
        final ServiceDef.Specification spec = ServiceDef.Specification.fromShortName(serviceType);
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            final StringWriter sw = new StringWriter();
            final Marshaller m = GenericDatabaseMarshallerPool.getInstance().acquireMarshaller();
            m.marshal(metadata, sw);
            GenericDatabaseMarshallerPool.getInstance().recycle(m);
            final StringReader sr = new StringReader(sw.toString());
            final ServiceRecord service = session.readService(identifier, spec);
            if (service != null) {
                service.setMetadata(language, sr);
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while updating service database", ex);
        } finally {
            if (session != null) session.close();
        }
    }

    public static Service readServiceMetadata(final String identifier, final String serviceType, String language) throws IOException, JAXBException {
        ensureNonNull("identifier", identifier);
        ensureNonNull("serviceType", serviceType);
        if (language == null) {
            language = "eng";
        }
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            final ServiceRecord rec = session.readService(identifier, ServiceDef.Specification.fromShortName(serviceType));
            if (rec != null) {
                final InputStream is = rec.getMetadata(language);
                if (is != null) {
                    final Unmarshaller u = GenericDatabaseMarshallerPool.getInstance().acquireUnmarshaller();
                    final Service config = (Service) u.unmarshal(is);
                    GenericDatabaseMarshallerPool.getInstance().recycle(u);
                    return config;
                } else {
                    final InputStream in = Util.getResourceAsStream("org/constellation/xml/" + serviceType + "Capabilities.xml");
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
            if (session != null) session.close();
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
        return configDir;
    }

    public static void shutdownTestEnvironement(final String directoryName) {
        FileUtilities.deleteDirectory(new File(directoryName));
        clearDatabase();
        ConfigDirectory.setConfigDirectory(null);
    }

    public static String getConstellationProperty(final String key, final String defaultValue) {
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            final String value = session.readProperty(key);
            if (value == null) {
                return defaultValue;
            }
            return value;
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "An error occurred getting constellation property", ex);
        } finally {
            if (session != null) session.close();
        }
        return defaultValue;
    }

    /**
     * Save metadata on specific folder
     *
     * @param fileMetadata
     * @param dataName
     * @param pool
     */
    public static void saveMetaData(final DefaultMetadata fileMetadata, final String dataName, final MarshallerPool pool) {
        ensureNonNull("metadata", fileMetadata);

        //save in database
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            final StringWriter sw = new StringWriter();
            final Marshaller m = pool.acquireMarshaller();
            m.marshal(fileMetadata, sw);
            pool.recycle(m);
            final StringReader sr = new StringReader(sw.toString());
            final ProviderRecord provider = session.readProvider(dataName);
            if (provider != null) {
                provider.setMetadata(sr);
            }

        } catch (SQLException | IOException | JAXBException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while updating service database", ex);
        } finally {
            if (session != null) session.close();
        }
    }

    /**
     * Load a metadata for a provider.
     *
     * @param providerId
     * @param pool
     * @return
     */
    public static DefaultMetadata loadMetadata(final String providerId, final MarshallerPool pool) {
        Session session = null;
        DefaultMetadata metadata = null;
        try {
            session = EmbeddedDatabase.createSession();
            final ProviderRecord provider = session.readProvider(providerId);
            if (provider != null) {
                final InputStream sr = provider.getMetadata();
                final Unmarshaller m = pool.acquireUnmarshaller();
                if(sr!=null){
                    metadata = (DefaultMetadata) m.unmarshal(sr);
                }
                pool.recycle(m);
                return metadata;
            }
        } catch (SQLException | IOException | JAXBException ex) {
            LOGGER.log(Level.WARNING, "An error occurred while updating service database", ex);
        } finally {
            if (session != null) session.close();
        }
        return null;
    }

    /**
     * @param name
     * @param providerId
     * @return
     */

    public static DataBrief getData(QName name, String providerId){
        Session session = null;
        try {
            session = EmbeddedDatabase.createSession();
            final DataRecord record = session.readData(name, providerId);
            if (record != null) {
                return getDataBrief(session, record);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "error when try to read data", e);
        } finally {
            if (session != null) session.close();
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
                return getDataBrief(session, record);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error on sql execution when search data layer", e);
        } finally {
            if (session != null) session.close();
        }
        return null;
    }

    /**
     * create a {@link org.constellation.configuration.DataBrief} with style link and service link.
     *
     * @param session current {@link org.constellation.admin.dao.Session} used
     * @param record  data found
     * @return a {@link org.constellation.configuration.DataBrief} with all informations linked.
     * @throws SQLException if they have an error when search style, service or provider.
     */
    private static DataBrief getDataBrief(final Session session, final DataRecord record) throws SQLException {
        final List<StyleRecord> styleRecords = session.readStyles(record);
        final List<ServiceRecord> serviceRecords = session.readDataServices(record);

        final DataBrief db = new DataBrief();
        db.setOwner(record.getOwnerLogin());
        db.setName(record.getName());
        db.setNamespace(record.getNamespace());
        db.setDate(record.getDate());
        db.setProvider(record.getProvider().getIdentifier());
        db.setType(record.getType().toString());

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
            protocol.add(serviceRecord.getType().fullName);
            final ServiceProtocol sp = new ServiceProtocol(serviceRecord.getIdentifier(), protocol);
            serviceProtocols.add(sp);
        }
        db.setTargetService(serviceProtocols);

        return db;
    }

    public static LayerRecord getLayer(final String identifier, final ServiceDef.Specification specification, final QName name) {
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
}
