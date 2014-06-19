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
