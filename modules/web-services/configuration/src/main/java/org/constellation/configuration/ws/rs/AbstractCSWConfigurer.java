/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2008, Geomatys
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
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

// JAXB dependencies
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

// constellation dependencies
import org.constellation.cat.csw.v202.ElementSetType;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.CSWCascadingType;
import org.constellation.configuration.exception.ConfigurationException;
import org.constellation.configuration.filter.ConfigurationFileFilter;
import org.constellation.configuration.filter.IndexDirectoryFilter;
import org.constellation.configuration.filter.NextIndexDirectoryFilter;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.util.Util;
import org.constellation.metadata.factory.AbstractCSWFactory;
import org.constellation.lucene.index.AbstractIndexer;
import org.constellation.metadata.io.MetadataReader;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.rs.ContainerNotifierImpl;
import static org.constellation.configuration.ws.rs.ConfigurationService.*;
import static org.constellation.ows.OWSExceptionCode.*;

// Geotools dependencies
import org.geotools.factory.FactoryNotFoundException;
import org.geotools.factory.FactoryRegistry;
import org.geotools.metadata.note.Anchors;

// MDWeb dependencies
import org.mdweb.utils.GlobalUtils;

/**
 * The base for The CSW configurer.
 *
 * @author Guilhem Legal
 */
public abstract class AbstractCSWConfigurer {
    
    protected Logger LOGGER = Logger.getLogger("org.constellation.configuration.ws.rs");
    
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
    private AbstractCSWFactory CSWfactory;

    /**
     * Build a new CSW configurer.
     * 
     * @param cn a injected container notifier allowing to reload all the jersey web-services.
     * @throws org.constellation.configuration.exception.ConfigurationException
     */
    public AbstractCSWConfigurer(ContainerNotifierImpl cn) throws ConfigurationException {
        this.containerNotifier = cn;

        File cswConfigDir = getConfigurationDirectory();
        if (cswConfigDir == null || (cswConfigDir != null && !cswConfigDir.isDirectory())) {
            throw new ConfigurationException("No configuration directory have been found");
        }
        
        try {
            Unmarshaller configUnmarshaller = JAXBContext.newInstance("org.constellation.generic.database").createUnmarshaller();
            CSWfactory = factory.getServiceProvider(AbstractCSWFactory.class, null, null, null);

            for (File configFile : cswConfigDir.listFiles(new ConfigurationFileFilter(null))) {
                //we get the csw ID (if single mode return "")
                String id = getConfigID(configFile);
                // we get the CSW configuration file
                Automatic config = (Automatic) configUnmarshaller.unmarshal(configFile);
                serviceConfiguration.put(id, config);
            }
            
        } catch (JAXBException ex) {
            throw new ConfigurationException("JAXBexception while setting the JAXB context for configuration service", ex.getMessage());
        } catch (FactoryNotFoundException ex) {
            throw new ConfigurationException("Unable to find a CSW factory for CSW in configuration service", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            throw new ConfigurationException("IllegalArgumentException: " + ex.getMessage());
        }
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
    protected AbstractIndexer initIndexer(String serviceID, File cswConfigDir, MetadataReader currentReader) throws CstlServiceException {

        // we get the CSW configuration file
        Automatic config = serviceConfiguration.get(serviceID);

        if (config != null) {
            BDD db = config.getBdd();
            if (db == null) {
                throw new CstlServiceException("the configuration file does not contains a BDD object.", NO_APPLICABLE_CODE);
            } else {
                try {
                    Connection MDConnection      = db.getConnection();
                    if (currentReader == null)
                        currentReader = CSWfactory.getMetadataReader(config, MDConnection, new File(cswConfigDir, "data"), null, cswConfigDir);
                    AbstractIndexer indexer      = CSWfactory.getIndexer(config.getType(), currentReader, MDConnection, cswConfigDir, serviceID);
                    return indexer;
                    
                } catch (JAXBException ex) {
                    throw new CstlServiceException("JAXBException while initializing the indexer!", NO_APPLICABLE_CODE);
                } catch (SQLException ex) {
                    throw new CstlServiceException("SQLException while initializing the indexer!", NO_APPLICABLE_CODE);
                }
            }
        } else {
            throw new CstlServiceException("there is no configuration file correspounding to this ID:" + serviceID, NO_APPLICABLE_CODE);
        }
    }

    /**
     * Build a new Metadata reader for the specified service ID.
     *
     * @param serviceID the service identifier (form multiple CSW) default: ""
     * @param cswConfigDir the CSW configuration directory.
     *
     * @return A metadata reader.
     * @throws org.constellation.ws.CstlServiceException
     */
    protected MetadataReader initReader(String serviceID, File cswConfigDir) throws CstlServiceException {

        // we get the CSW configuration file
        Automatic config = serviceConfiguration.get(serviceID);

        if (config != null) {
            BDD db = config.getBdd();
            if (db == null) {
                throw new CstlServiceException("the configuration file does not contains a BDD object.", NO_APPLICABLE_CODE);
            } else {
                try {
                    Connection MDConnection      = db.getConnection();
                    MetadataReader currentReader = CSWfactory.getMetadataReader(config, MDConnection, new File(cswConfigDir, "data"), null, cswConfigDir);
                    return currentReader;

                } catch (JAXBException ex) {
                    throw new CstlServiceException("JAXBException while initializing the reader!", NO_APPLICABLE_CODE);
                } catch (SQLException ex) {
                    throw new CstlServiceException("SQLException while initializing the reader!", NO_APPLICABLE_CODE);
                }
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
    public Set<String> getAllServiceIDs() {
        return serviceConfiguration.keySet();
    }

    /**
     * Refresh the properties file used by the CSW service to store federated catalogues.
     * 
     * @param request
     * @return
     * @throws org.constellation.coverage.web.CstlServiceException
     */
    public AcknowlegementType refreshCascadedServers(CSWCascadingType request) throws CstlServiceException {
        LOGGER.info("refresh cascaded servers requested");
        
        File cascadingFile = new File(getConfigurationDirectory(), "CSWCascading.properties");
        Properties prop;
        try {
            prop = Util.getPropertiesFromFile(cascadingFile);
        } catch (IOException ex) {
            throw new CstlServiceException("IO exception while loading the cascading properties file",
                            NO_APPLICABLE_CODE, version);
        }
        
        if (!request.isAppend()) {
            prop.clear();
        }
        
        for (String servName : request.getCascadedServices().keySet()) {
            prop.put(servName, request.getCascadedServices().get(servName));
        }
        try {
            Util.storeProperties(prop, cascadingFile);
        } catch (IOException ex) {
            throw new CstlServiceException("unable to store the cascading properties file",
                        NO_APPLICABLE_CODE, version);
        }
        
        return new AcknowlegementType("success", "CSW cascaded servers list refreshed");
    }
    
    /**
     * Destroy the CSW index directory in order that it will be recreated.
     * 
     * @param asynchrone a flag for indexation mode.
     * @param service The service type (CSW, MDSearch, ...)
     * @param id The service identifier.
     * 
     * @return
     * @throws CstlServiceException
     */
    public AcknowlegementType refreshIndex(boolean asynchrone, String service, String id) throws CstlServiceException {
        LOGGER.info("refresh index requested");
        String msg;

        // MDWeb Search indexation
        if (service != null && service.equalsIgnoreCase("MDSEARCH")) {
            GlobalUtils.resetLuceneIndex();
            msg = "MDWeb search index succefully deleted";

        // CSW indexation
        } else {
            
            File cswConfigDir = getConfigurationDirectory();
            if (!asynchrone) {
                synchroneIndexRefresh(cswConfigDir, id);
            } else {
                asynchroneIndexRefresh(cswConfigDir, id);
            }
            
            msg = "CSW index succefully recreated";
        }
        return new AcknowlegementType("success", msg);
    }

    /**
     * Delete The index folder and call the restart() method.
     *
     * @param configurationDirectory The CSW configuration directory.
     * @param id The service identifier.
     *
     * @throws org.constellation.ws.CstlServiceException
     */
    private void synchroneIndexRefresh(File configurationDirectory, String id) throws CstlServiceException {
        //we delete each index directory
        for (File indexDir : configurationDirectory.listFiles(new IndexDirectoryFilter(id))) {
            for (File f : indexDir.listFiles()) {
                f.delete();
            }
            if (!indexDir.delete()) {
                throw new CstlServiceException("The service can't delete the index folder.", NO_APPLICABLE_CODE);
            }
        }
        //then we restart the services
        Anchors.clear();
        restart();
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
    private void asynchroneIndexRefresh(File configurationDirectory, String id) throws CstlServiceException {
        /*
         * we delete each pre-builded index directory.
         * if there is a specific id in parameter we only delete the specified profile
         */
        for (File indexDir : configurationDirectory.listFiles(new NextIndexDirectoryFilter(id))) {
            for (File f : indexDir.listFiles()) {
                f.delete();
            }
            if (!indexDir.delete()) {
                throw new CstlServiceException("The service can't delete the next index folder.", NO_APPLICABLE_CODE);
            }
        }

        /*
         * then we create all the nextIndex directory and create the indexes
         * if there is a specific id in parameter we only index the specified profile
         */
        for (File configFile : configurationDirectory.listFiles(new ConfigurationFileFilter(id))) {
            String currentId        = getConfigID(configFile);
            File nexIndexDir        = new File(configurationDirectory, currentId + "nextIndex");
            AbstractIndexer indexer = null;
            try {
                indexer = initIndexer(currentId, configurationDirectory, null);
                if (indexer != null) {
                    nexIndexDir.mkdir();
                    indexer.setFileDirectory(nexIndexDir);
                    indexer.createIndex();

                } else {
                    throw new CstlServiceException("Unable to create an indexer for the id:" + id, NO_APPLICABLE_CODE);
                }
            } catch (IllegalArgumentException ex) {
                LOGGER.severe("unable to create an indexer for configuration file:" + configFile.getName());
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
        String msg;

        if (service != null && service.equalsIgnoreCase("MDSEARCH")) {
            throw new CstlServiceException("This method is not yet available for this service.", OPERATION_NOT_SUPPORTED);

        // CSW indexation
        } else {

            File cswConfigDir = getConfigurationDirectory();
            
        /*
         * then we create all the nextIndex directory and create the indexes
         * if there is a specific id in parameter we only index the specified profile
         */
        for (File configFile : cswConfigDir.listFiles(new ConfigurationFileFilter(id))) {
            String currentId        = getConfigID(configFile);
            AbstractIndexer indexer = null;
            MetadataReader reader   = null;
            try {
                reader  = initReader(currentId, cswConfigDir);
                List<Object> objectToIndex = new ArrayList<Object>();
                if (reader != null) {
                    for (String identifier : identifiers) {
                        objectToIndex.add(reader.getMetadata(identifier, MetadataReader.ISO_19115, ElementSetType.FULL, null));
                    }
                } else {
                    throw new CstlServiceException("Unable to create a reader for the id:" + id, NO_APPLICABLE_CODE);
                }

                indexer = initIndexer(currentId, cswConfigDir, reader);
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
        }

            msg = "The specified record have been added to the CSW index";
        }
        return new AcknowlegementType("success", msg);
    }

    /**
     * Reload all the web-services.
     */
    protected void restart() {
        containerNotifier.reload();
    }

    /**
     * Return the ID of the CSW given by the configuration file Name.
     */
    private String getConfigID(File configFile) {
        if (configFile == null || (configFile != null && !configFile.exists()))
            return "";
        String ID = configFile.getName();
        if (ID.indexOf("config.xml") != -1) {
            ID = ID.substring(0, ID.indexOf("config.xml"));
            return ID;
        }
        return "";
    }

    /**
     * Because the injectable fields are null at initialization time
     * @param containerNotifier
     */
    public void setContainerNotifier(ContainerNotifierImpl containerNotifier) {
        this.containerNotifier = containerNotifier;
    }

    public abstract AcknowlegementType updateContacts() throws CstlServiceException;
    
    public abstract AcknowlegementType updateVocabularies() throws CstlServiceException;

    protected abstract File getConfigurationDirectory();

    /**
     * destroy all the resource and close the connection.
     */
    public void destroy() {
    }
}
