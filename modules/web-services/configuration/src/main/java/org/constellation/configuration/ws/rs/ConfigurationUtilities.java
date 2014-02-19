package org.constellation.configuration.ws.rs;

import static org.constellation.api.CommonConstants.SUCCESS;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;

import org.apache.sis.util.logging.Logging;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.admin.dao.ProviderRecord;
import org.constellation.configuration.AbstractConfigurer;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.provider.LayerProvider;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.ProviderService;
import org.constellation.provider.StyleProviderProxy;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.WSEngine;
import org.constellation.ws.Worker;
import org.geotoolkit.parameter.ParametersExt;
import org.geotoolkit.xml.parameter.ParameterValueReader;
import org.opengis.parameter.ParameterValueGroup;

/**
 * @author Benjamin Garcia (Geomatys)
 */
public class ConfigurationUtilities {

    private static final Logger LOGGER = Logging.getLogger(ConfigurationUtilities.class);

    public static final String ADMIN_FOLDER_NAME = "admin";
    public static final String OLD_AUTHENTICATION_FOLDER = "auth";
    public static final String SOURCE_DESCRIPTOR_NAME = "source";
    public static final String SOURCE_ID_DESCRIPTOR_NAME = "id";
    public static final String SLD_PROVIDER_NAME = "sld";

    public static final FileFilter XML_FILE_FILTER = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            return pathname.isFile() && pathname.getName().toLowerCase().endsWith(".xml");
        }
    };


    @Deprecated
    public static AcknowlegementType deleteUser(final String userName) {
//        Session session = null;
//        try {
//            session = EmbeddedDatabase.createSession();
//            session.deleteUser(userName);
//            return new AcknowlegementType(SUCCESS, "The user has been deleted");
//        } catch (SQLException ex) {
//            LOGGER.log(Level.WARNING, "Error while deleting user", ex);
//        } finally {
//            if (session != null) session.close();
//        }
//        return new AcknowlegementType("Failure", "An error occurs");

        return new AcknowlegementType("Failure", "Operation no longer supported");
    }

    @Deprecated
    public static AcknowlegementType updateUser(final String userName, final String password, final String oldLogin) {
//        Session session = null;
//        try {
//            session = EmbeddedDatabase.createSession();
//            session.updateUser(oldLogin, userName, password, "Default Constellation Administrator", Arrays.asList("cstl-admin"));
//            return new AcknowlegementType(SUCCESS, "The user has been changed");
//        } catch (SQLException ex) {
//            LOGGER.log(Level.WARNING, "Error while deleting user", ex);
//        } finally {
//            if (session != null) session.close();
//        }
//        return new AcknowlegementType("Failure", "An error occurs");

        return new AcknowlegementType("Failure", "Operation no longer supported");
    }

    public static AcknowlegementType setConfigPath(final String path) throws CstlServiceException {
        // Set the new user directory
        if (path != null && !path.isEmpty()) {
            final File userDirectory = new File(path);
            if (!userDirectory.isDirectory()) {
                userDirectory.mkdir();
            }
            ConfigDirectory.setConfigDirectory(userDirectory);
        }

        return new AcknowlegementType(SUCCESS, path);
    }

    public static AcknowlegementType getConfigPath() throws CstlServiceException {
        final String path = ConfigDirectory.getConfigDirectory().getPath();
        return new AcknowlegementType(SUCCESS, path);
    }

    /**
     * Restart all the web-services, reload the providers.
     * If some services are currently indexing, the service will not restart
     * unless you specified the flag "forced".
     *
     * @return an Acknowledgment if the restart succeed.
     */
    public static AcknowlegementType restartService(final boolean forced, final List<AbstractConfigurer> configurers) {
        LOGGER.info("\n restart requested \n");

        for (String serviceType : WSEngine.getRegisteredServices().keySet()) {
            final Map<String, Worker> workersMap = new HashMap<>();
            for (String instanceID : WSEngine.getInstanceNames(serviceType)) {
                try {
                    final Worker worker = WSEngine.buildWorker(serviceType, instanceID);
                    if (worker != null) {
                        workersMap.put(instanceID, worker);
                    } else {
                        LOGGER.log(Level.WARNING, "The instance {0} can be started, maybe there is no configuration directory with this name.", instanceID);
                    }
                } catch (IllegalArgumentException ex) {
                    LOGGER.log(Level.WARNING, ex.getMessage(), ex);
                }
            }
            WSEngine.setServiceInstances(serviceType, workersMap);
        }
        return new AcknowlegementType(SUCCESS, "services successfully restarted");
    }

    public static boolean configurerLock(final AbstractConfigurer[] configurers) {
        for (AbstractConfigurer configurer : configurers) {
            if (configurer.isLock()) return true;
        }
        return false;
    }



    /**
     * Read constellation configuration from given directory, to re-write it into data-base configuration.
     *
     * /!\ Warning : When call this method, Only providers (and for coverage providers, image readers) previously loaded
     * by the application can be converted.
     * Ex : If you get a file data-store.xml containing feature store parameters, but your application has not loaded
     * dependency cstl-store-data-featurestore, trying to import you feature-stores will raise an error.
     *
     * @param configFolder Source folder containing Constellation configuration. If null, Constellation will try getting
     *                     directory from default configuration location.
     * @throws java.io.IOException If no configuration directory can be found.
     * @throws java.lang.IllegalArgumentException if given file is not a directory.
     * @throws javax.xml.bind.JAXBException If we cannot get unmarshaller for service configuration reading.
     * @throws javax.xml.stream.XMLStreamException If we cannot dispose XML reader properly.
     */
    public static void FileToDBConfig(File configFolder) throws IOException, JAXBException, XMLStreamException {
        if (configFolder == null) {
            configFolder = ConfigDirectory.getConfigDirectory();
            if (!configFolder.isDirectory()) {
                throw new IOException("Constellation cannot find any valid configuration folder.");
            }
        }
        if (!configFolder.isDirectory()) {
            throw new IllegalArgumentException("Input data is not a folder : " + configFolder.getAbsolutePath());
        }

        final MarshallerPool pool = GenericDatabaseMarshallerPool.getInstance();
        final Unmarshaller unmarshaller = pool.acquireUnmarshaller();

        // List service folders to get their configuration
        final File[] services = configFolder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                final String fileName = pathname.getName().toLowerCase();
                return pathname.isDirectory()
                        && !fileName.contains("provider")
                        && !fileName.contains(ADMIN_FOLDER_NAME)
                        && !fileName.contains(OLD_AUTHENTICATION_FOLDER);
            }
        });

        for (File serviceDir : services) {
            try {
                LOGGER.log(Level.FINE, "Service --> {0}", serviceDir.getName());
                final File[] instances = serviceDir.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.isDirectory();
                    }
                });

                for (File instance : instances) {
                    LOGGER.log(Level.FINE, "\tInstance --> {0}", instance.getName());
                    boolean exists = false;
                    try {
                        ConfigurationEngine.getConfiguration(serviceDir.getName(), instance.getName());
                        //LOGGER.log(Level.INFO, "Service cannot be imported, because one with the same name already exists : " + serviceDir.getName() + " : " + instance.getName());
                        exists = true;
                    } catch (FileNotFoundException e) {
                        // Ok. If we got here, it means Constellation did not found any service with given name.
                    }
                    for (File config : instance.listFiles(XML_FILE_FILTER)) {
                        try {
                            final Object conf = unmarshaller.unmarshal(config);
                            // If service already exists, we specify a filename, which means we want to add extra configuration to the service.
                            if (exists) {
                                ConfigurationEngine.storeConfiguration(serviceDir.getName(), instance.getName(), config.getName().replaceAll("\\.xml$", ""), conf, pool);
                            } else {
                                ConfigurationEngine.storeConfiguration(serviceDir.getName(), instance.getName(), conf);
                                exists = true;
                            }
                            LOGGER.log(Level.FINE, "\t\t{0}", conf.toString());
                        } catch (Exception e) {
                            LOGGER.log(Level.FINE, "Following file cannot be read : " + config.getName());
                        }
                    }
                }
            } catch (Exception e) {
                // Unknown service ?
                LOGGER.log(Level.FINE, "Service type " + serviceDir.getName() + " is not recognized.");
            }
        }

        // No more need for the unmarhsaller, as providers are not managed with JAXB.
        pool.recycle(unmarshaller);

        // Parse provider folder.
        final File[] providers = configFolder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory() && pathname.getName().toLowerCase().contains("provider");
            }
        });

        ParameterValueGroup sourceGroup;
        List<ParameterValueGroup> sources;
        final LayerProviderProxy layerProxy = LayerProviderProxy.getInstance();
        final StyleProviderProxy styleProxy = StyleProviderProxy.getInstance();
        for (File providerDir : providers) {
            LOGGER.log(Level.FINE, "Providers");
            for (File providerFile : providerDir.listFiles(XML_FILE_FILTER)) {
                LOGGER.log(Level.FINE, "\tType --> {0}", providerFile.getName());
                // We cannot use marshaller here, because provider file has dynamical structure. We're forced to iterate
                // through all possible provider types to find the right ones.
                ParameterValueReader paramReader = null;
                try {
                    // Style configuration file
                    if (providerFile.getName().toLowerCase().contains(SLD_PROVIDER_NAME)) {
                        for (final ProviderService service : styleProxy.getServices()) {
                            try {
                                paramReader = new ParameterValueReader(service.getServiceDescriptor());
                                paramReader.setInput(providerFile);
                                sourceGroup = (ParameterValueGroup) paramReader.read();
                                sources = ParametersExt.getGroups(sourceGroup, SOURCE_DESCRIPTOR_NAME);
                                for (ParameterValueGroup source : sources) {
                                    try {
                                        final String sourceName = (String) source.parameter(SOURCE_ID_DESCRIPTOR_NAME).getValue();
                                        LOGGER.log(Level.FINE, "\t\tProvider type : {0} | Service name : {1} \n\t\t {2}",
                                                new String[]{ProviderRecord.ProviderType.STYLE.name(), service.getName(), source.toString()});
                                        if (ConfigurationEngine.getProvider(sourceName) == null) {
                                            ConfigurationEngine.writeProvider(sourceName, ProviderRecord.ProviderType.STYLE, service.getName(), source);
                                        } else {
                                            LOGGER.log(Level.FINE, "Provider cannot be imported. A provider with the same name already exists : " + sourceName);
                                        }
                                    } catch (Exception e) {
                                        // A problem occured while creating the provider, check the next one.
                                        LOGGER.log(Level.WARNING, e.getLocalizedMessage());
                                    }
                                }
                            } catch (Exception e) {
                                LOGGER.log(Level.FINE, e.getLocalizedMessage());
                                // Not that service,
                            }
                        }
                    } else {
                        // Try to get a valid layer configuration
                        for (final ProviderService service : layerProxy.getServices()) {
                            try {
                                paramReader = new ParameterValueReader(service.getServiceDescriptor());
                                paramReader.setInput(providerFile);
                                sourceGroup = (ParameterValueGroup) paramReader.read();
                                sources = ParametersExt.getGroups(sourceGroup, SOURCE_DESCRIPTOR_NAME);
                                for (ParameterValueGroup source : sources) {
                                    try {
                                        LOGGER.log(Level.FINE, "\t\tProvider type : {0} | Service name : {1} \n\t\t {2}",
                                                new String[]{ProviderRecord.ProviderType.LAYER.name(), service.getName(), source.toString()});
                                        final String sourceName = (String) source.parameter(SOURCE_ID_DESCRIPTOR_NAME).getValue();
                                        if (ConfigurationEngine.getProvider(sourceName) == null) {
                                            ConfigurationEngine.writeProvider(sourceName, ProviderRecord.ProviderType.LAYER, service.getName(), source);
                                        } else {
                                            LOGGER.log(Level.FINE, "Provider cannot be imported. A provider with the same name already exists : " + sourceName);
                                        }
                                    } catch (Exception e) {
                                        // A problem occured while creating the provider, check the next one.
                                        LOGGER.log(Level.WARNING, e.getLocalizedMessage());
                                    }
                                }
                            } catch (Exception e) {
                                LOGGER.log(Level.FINE, e.getLocalizedMessage());
                                // Not that service,
                            }
                        }
                    }

                    // Brut force solution : we restart all providers, otherwise, Constellation seems in trouble to work with newly imported data.
                    for (LayerProvider provider : LayerProviderProxy.getInstance().getProviders()) {
                        provider.reload();
                    }

                } catch (Exception e) {
                    LOGGER.log(Level.FINE, "Following file cannot be read : " + providerFile.getName(), e);
                } finally {
                    if (paramReader != null) {
                        paramReader.dispose();
                    }
                }
            }
        }

        // Finally, get files at configuration root. Should get only scheduler.
        // TODO : Re-activate and complete when tasks will be ported into database configuration.
//        final File[] others = configFolder.listFiles(new FileFilter() {
//            @Override
//            public boolean accept(File pathname) {
//                return !pathname.isDirectory();
//            }
//        });
//        for (File other : others) {
//            LOGGER.log(Level.INFO, "Others --> {0}", other.getName());
//            try {
//                final TasksReader reader = new TasksReader();
//                reader.setInput(other);
//                final List<Task> tasks = reader.read();
//
//            } catch (Exception e) {
//                LOGGER.log(Level.INFO, "Following file cannot be read : " + other.getName());
//            }
//        }
    }

}
