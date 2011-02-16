/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2009, Geomatys
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

package org.constellation.configuration.ws.rs;

// J2SE dependencies
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

// JAXB dependencies
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

// constellation dependencies
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.exception.ConfigurationException;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.metadata.factory.AbstractCSWFactory;
import org.constellation.metadata.io.AbstractMetadataReader;
import org.constellation.metadata.io.CSWMetadataReader;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.rs.ContainerNotifierImpl;

// Geotoolkit dependencies
import org.geotoolkit.factory.FactoryNotFoundException;
import org.geotoolkit.factory.FactoryRegistry;
import org.geotoolkit.lucene.IndexingException;
import org.geotoolkit.lucene.index.AbstractIndexer;
import org.geotoolkit.lucene.index.AbstractIndexer.IndexDirectoryFilter;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

/**
 * The base for The CSW configurer.
 *
 * @author Guilhem Legal
 */
public abstract class AbstractCSWConfigurer {
    
    protected static final Logger LOGGER = Logger.getLogger("org.constellation.configuration.ws.rs");
    
    /**
     * A container notifier allowing to restart the webService. 
     */
    private ContainerNotifierImpl containerNotifier;
    
    /**
     * A Map of service configuration.
     */
    private Map<String, Automatic> serviceConfiguration = new HashMap<String, Automatic>();
    
    /**
     * A generic factory to get the correct CSW Factory.
     */
    private static FactoryRegistry factory = new FactoryRegistry(AbstractCSWFactory.class);

    /**
     * A CSW factory
     */
    private AbstractCSWFactory cswfactory;

    /**
     * Build a new CSW configurer.
     * 
     * @param cn a injected container notifier allowing to reload all the jersey web-services.
     * @throws org.constellation.configuration.exception.ConfigurationException
     */
    public AbstractCSWConfigurer(ContainerNotifierImpl cn) throws ConfigurationException {
        this.containerNotifier = cn;

        try {
            cswfactory = factory.getServiceProvider(AbstractCSWFactory.class, null, null, null);

        } catch (FactoryNotFoundException ex) {
            throw new ConfigurationException("Unable to find a CSW factory for CSW in configuration service", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            throw new ConfigurationException("IllegalArgumentException: " + ex.getMessage());
        }
        refreshServiceConfiguration();
    }


    /**
     * Build a new Indexer for the specified service ID.
     * 
     * @param serviceID the service identifier (form multiple CSW) default: ""
     * @param cswConfigDir the CSW configuration directory.
     *
     * @return A lucene Indexer
     * @throws org.constellation.ws.CstlServiceException
     */
    protected AbstractIndexer initIndexer(String serviceID, CSWMetadataReader currentReader) throws CstlServiceException {

        // we get the CSW configuration file
        final Automatic config = serviceConfiguration.get(serviceID);
        if (config != null) {
            try {
                if (currentReader == null) {
                    currentReader = cswfactory.getMetadataReader(config);
                }
                return cswfactory.getIndexer(config, currentReader, "");

            } catch (Exception ex) {
                throw new CstlServiceException("An eception occurs while initializing the indexer!" + '\n' +
                        "cause:" + ex.getMessage(), NO_APPLICABLE_CODE);
            }
        } else {
            throw new CstlServiceException("there is no configuration file correspounding to this ID:" + serviceID, NO_APPLICABLE_CODE);
        }
    }

    /**
     * Build a new Metadata reader for the specified service ID.
     *
     * @param serviceID the service identifier (form multiple CSW) default: ""
     *
     * @return A metadata reader.
     * @throws org.constellation.ws.CstlServiceException
     */
    protected CSWMetadataReader initReader(String serviceID) throws CstlServiceException {

        // we get the CSW configuration file
        final Automatic config = serviceConfiguration.get(serviceID);
        if (config != null) {
            try {
                return cswfactory.getMetadataReader(config);

            } catch (MetadataIoException ex) {
                throw new CstlServiceException("JAXBException while initializing the reader!", NO_APPLICABLE_CODE);
            }

        } else {
            throw new CstlServiceException("there is no configuration file correspounding to this ID:" + serviceID, NO_APPLICABLE_CODE);
        }
    }

    /**
     * Return all the founded CSW service identifiers.
     *
     * @return all the founded CSW service identifiers.
     */
    public List<String> getAllServiceIDs() {
        final List<String> result = new ArrayList<String>();
        for (String id : serviceConfiguration.keySet()) {
            result.add(id);
        }
        return result;
    }

    /**
     * Refresh the map of configuration object.
     * 
     * @throws ConfigurationException
     */
    private void refreshServiceConfiguration() throws ConfigurationException {
        serviceConfiguration    = new HashMap<String, Automatic>();
        final File cswConfigDir = getConfigurationDirectory();
        try {
            final Unmarshaller configUnmarshaller = GenericDatabaseMarshallerPool.getInstance().acquireUnmarshaller();

            for (File instanceDirectory : cswConfigDir.listFiles()) {
                if (instanceDirectory.isDirectory()) {
                    //we get the csw ID
                    final String id = instanceDirectory.getName();
                    final File configFile = new File(instanceDirectory, "config.xml");
                    if (configFile.exists()) {
                        // we get the CSW configuration file
                        final Automatic config = (Automatic) configUnmarshaller.unmarshal(configFile);
                        config.setConfigurationDirectory(instanceDirectory);
                        serviceConfiguration.put(id, config);
                    }
                }
            }
            GenericDatabaseMarshallerPool.getInstance().release(configUnmarshaller);

        } catch (JAXBException ex) {
            throw new ConfigurationException("JAXBexception while setting the JAXB context for configuration service", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            throw new ConfigurationException("IllegalArgumentException: " + ex.getMessage());
        }
    }
    
    /**
     * Destroy the CSW index directory in order that it will be recreated.
     * 
     * @param asynchrone a flag for indexation mode.
     * @param id The service identifier.
     * 
     * @return
     * @throws CstlServiceException
     */
    public AcknowlegementType refreshIndex(boolean asynchrone, String id) throws CstlServiceException {
        String suffix = "";
        if (asynchrone) {
            suffix = " (asynchrone)";
        }
        if (id != null && !id.isEmpty()) {
            suffix = suffix + " id:" + id;
        }
        LOGGER.log(Level.INFO, "refresh index requested{0}", suffix);
        try {
            refreshServiceConfiguration();
        } catch (ConfigurationException ex) {
            throw new CstlServiceException(ex);
        }
        final List<File> cswInstanceDirectories = new ArrayList<File>();
        if ("all".equals(id)) {
            cswInstanceDirectories.addAll(getAllCswInstanceDirectory());
        } else {
            cswInstanceDirectories.add(getCswInstanceDirectory(id));
        }
        if (!asynchrone) {
            synchroneIndexRefresh(cswInstanceDirectories);
        } else {
            asynchroneIndexRefresh(cswInstanceDirectories);
        }

        final String msg = "CSW index succefully recreated";
        return new AcknowlegementType("success", msg);
    }

    /**
     * Delete The index folder and call the restart() method.
     *
     * TODO maybe we can directly recreate the index here (fusion of synchrone/asynchrone)
     *
     * @param configurationDirectory The CSW configuration directory.
     *
     * @throws org.constellation.ws.CstlServiceException
     */
    private void synchroneIndexRefresh(List<File> cswInstanceDirectories) throws CstlServiceException {
        boolean deleted = false;
        for (File cswInstanceDirectory : cswInstanceDirectories) {
            //we delete each index directory
            for (File indexDir : cswInstanceDirectory.listFiles(new IndexDirectoryFilter(null))) {
                deleted = true;
                for (File f : indexDir.listFiles()) {
                    final boolean sucess = f.delete();
                    if (!sucess) {
                        throw new CstlServiceException("The service can't delete the index file:" + f.getPath(), NO_APPLICABLE_CODE);
                    }
                }
                if (!indexDir.delete()) {
                    throw new CstlServiceException("The service can't delete the index folder.", NO_APPLICABLE_CODE);
                }
            }
        }

        //if we have deleted something we restart the services
        if (deleted) {
            restart();
        } else {
            LOGGER.log(Level.INFO, "there is no index to delete");
        }
    }

    /**
     * Build a new Index in a new folder.
     * This index will be used at the next restart of the server.
     *
     * @param id The service identifier.
     * @param configurationDirectory  The CSW configuration directory.
     * 
     * @throws org.constellation.ws.CstlServiceException
     */
    private void asynchroneIndexRefresh(List<File> cswInstanceDirectories) throws CstlServiceException {
        for (File cswInstanceDirectory : cswInstanceDirectories) {
            String id = cswInstanceDirectory.getName();
            final File nexIndexDir        = new File(cswInstanceDirectory, "index-" + System.currentTimeMillis());
            AbstractIndexer indexer = null;
            try {
                indexer = initIndexer(id, null);
                if (indexer != null) {
                    final boolean success = nexIndexDir.mkdir();
                    if (!success) {
                        throw new CstlServiceException("Unable to create a directory nextIndex for  the id:" + id, NO_APPLICABLE_CODE);
                    }
                    indexer.setFileDirectory(nexIndexDir);
                    indexer.createIndex();

                } else {
                    throw new CstlServiceException("Unable to create an indexer for the id:" + id, NO_APPLICABLE_CODE);
                }
            } catch (IllegalArgumentException ex) {
                LOGGER.log(Level.SEVERE, "unable to create an indexer for id:{0}", id);
            } catch (IndexingException ex) {
                throw new CstlServiceException("An eception occurs while creating the index!" + '\n' +
                        "cause:" + ex.getMessage(), NO_APPLICABLE_CODE);
            } finally {
                if (indexer != null) {
                    indexer.destroy();
                }
            }
        }
    }

    /**
     * Add some csw record to the index.
     *
     * @param asynchrone
     * @return
     * @throws CstlServiceException
     */
    public AcknowlegementType addToIndex(String service, String id, List<String> identifiers) throws CstlServiceException {
        LOGGER.info("Add to index requested");

        AbstractIndexer indexer  = null;
        CSWMetadataReader reader = null;
        try {
            reader  = initReader(id);
            final List<Object> objectToIndex = new ArrayList<Object>();
            if (reader != null) {
                try {
                    for (String identifier : identifiers) {
                        objectToIndex.add(reader.getMetadata(identifier, AbstractMetadataReader.ISO_19115, null));
                    }
                } catch (MetadataIoException ex) {
                    throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
                }
            } else {
                throw new CstlServiceException("Unable to create a reader for the id:" + id, NO_APPLICABLE_CODE);
            }

            indexer = initIndexer(id, reader);
            if (indexer != null) {
                for (Object obj : objectToIndex) {
                    indexer.indexDocument(obj);
                }
            } else {
                throw new CstlServiceException("Unable to create an indexer for the id:" + id, NO_APPLICABLE_CODE);
            }

        } finally {
            if (indexer != null) {
                indexer.destroy();
            }
        }
        
        final String msg = "The specified record have been added to the CSW index";
        return new AcknowlegementType("success", msg);
    }

    /**
     * Reload all the web-services.
     */
    protected boolean restart() {
        if (containerNotifier != null) {
            containerNotifier.reload();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Because the injectable fields are null at initialization time
     * @param containerNotifier
     */
    public void setContainerNotifier(ContainerNotifierImpl containerNotifier) {
        this.containerNotifier = containerNotifier;
    }

    /**
     * Return the CSW configuration directory.
     * example for regular constellation configuration it return the file USER_DIRECTORY/.constellation/CSW
     *
     * @return Return the CSW configuration directory.
     * @throws ConfigurationException
     */
    protected File getConfigurationDirectory() throws ConfigurationException {
        final File configDir = ConfigDirectory.getConfigDirectory();
        final File cswConfigDir;
        if (configDir == null || !configDir.isDirectory()) {
            throw new ConfigurationException("No configuration directory have been found");
        } else {
            cswConfigDir = new File(configDir, "CSW");
            if (cswConfigDir == null || !cswConfigDir.isDirectory()) {
                throw new ConfigurationException("No CSW configuration directory have been found");
            }
            return cswConfigDir;
        }
    }
    
    /**
     * Update all the vocabularies skos files and the list of contact.
     */
    public AcknowlegementType updateVocabularies() throws CstlServiceException {
        throw new CstlServiceException("This method is not supported by the current implementation.", OPERATION_NOT_SUPPORTED);
    }

    /**
     * Update all the contact retrieved from files and the list of contact.
     */
    public AcknowlegementType updateContacts() throws CstlServiceException {
        throw new CstlServiceException("This method is not supported by the current implementation.", OPERATION_NOT_SUPPORTED);
    }

    protected abstract File getCswInstanceDirectory(String instanceId);

    protected abstract List<File> getAllCswInstanceDirectory();

    /**
     * destroy all the resource and close the connection.
     */
    public void destroy() {
    }
}
