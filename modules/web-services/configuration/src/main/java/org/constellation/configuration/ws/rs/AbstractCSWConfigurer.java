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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

// JAXB dependencies
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

// constellation dependencies
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.CSWCascadingType;
import org.constellation.configuration.exception.ConfigurationException;
import org.constellation.configuration.filter.ConfigurationFileFilter;
import org.constellation.configuration.filter.IndexDirectoryFilter;
import org.constellation.configuration.filter.NextIndexDirectoryFilter;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.util.Utils;
import org.constellation.metadata.factory.AbstractCSWFactory;
import org.constellation.metadata.index.AbstractIndexer;
import org.constellation.metadata.io.MetadataReader;
import org.constellation.ws.WebServiceException;
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
     * A lucene Index used to pre-build a CSW index.
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
     * @param cn
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
     * @param serviceID
     * @return
     * @throws org.constellation.ws.WebServiceException
     */
    protected AbstractIndexer initIndexer(String serviceID) throws WebServiceException {

        // we get the CSW configuration file
        Automatic config = serviceConfiguration.get(serviceID);

        if (config != null) {
            BDD db = config.getBdd();
            if (db == null) {
                throw new WebServiceException("the configuration file does not contains a BDD object.", NO_APPLICABLE_CODE);
            } else {
                try {
                    File cswConfigDir            = getConfigurationDirectory();
                    Connection MDConnection      = db.getConnection();
                    MetadataReader currentReader = CSWfactory.getMetadataReader(config, MDConnection, new File(cswConfigDir, "data"), null, cswConfigDir);
                    AbstractIndexer indexer      = CSWfactory.getIndexer(config.getType(), currentReader, MDConnection, cswConfigDir, serviceID);
                    return indexer;
                    
                } catch (JAXBException ex) {
                    throw new WebServiceException("JAXBException while initializing the indexer!", NO_APPLICABLE_CODE);
                } catch (SQLException ex) {
                    throw new WebServiceException("SQLException while initializing the indexer!", NO_APPLICABLE_CODE);
                }
            }
        } else {
            throw new WebServiceException("there is no configuration file correspounding to this ID:" + serviceID, NO_APPLICABLE_CODE);
        }
    }

    /**
     * Build a new Metadata reader for the specified service ID.
     *
     * @param serviceID
     * @return
     * @throws org.constellation.ws.WebServiceException
     */
    protected MetadataReader initReader(String serviceID) throws WebServiceException {

        // we get the CSW configuration file
        Automatic config = serviceConfiguration.get(serviceID);

        if (config != null) {
            BDD db = config.getBdd();
            if (db == null) {
                throw new WebServiceException("the configuration file does not contains a BDD object.", NO_APPLICABLE_CODE);
            } else {
                try {
                    File cswConfigDir            = getConfigurationDirectory();
                    Connection MDConnection      = db.getConnection();
                    MetadataReader currentReader = CSWfactory.getMetadataReader(config, MDConnection, new File(cswConfigDir, "data"), null, cswConfigDir);
                    return currentReader;

                } catch (JAXBException ex) {
                    throw new WebServiceException("JAXBException while initializing the reader!", NO_APPLICABLE_CODE);
                } catch (SQLException ex) {
                    throw new WebServiceException("SQLException while initializing the reader!", NO_APPLICABLE_CODE);
                }
            }
        } else {
            throw new WebServiceException("there is no configuration file correspounding to this ID:" + serviceID, NO_APPLICABLE_CODE);
        }
    }

    public Set<String> getAllServiceIDs() {
        return serviceConfiguration.keySet();
    }

    /**
     * Refresh the properties file used by the CSW service to store federated catalogues.
     * 
     * @param request
     * @return
     * @throws org.constellation.coverage.web.WebServiceException
     */
    public AcknowlegementType refreshCascadedServers(CSWCascadingType request) throws WebServiceException {
        LOGGER.info("refresh cascaded servers requested");
        
        File cascadingFile = new File(getConfigurationDirectory(), "CSWCascading.properties");
        Properties prop;
        try {
            prop = Utils.getPropertiesFromFile(cascadingFile);
        } catch (IOException ex) {
            throw new WebServiceException("IO exception while loading the cascading properties file",
                            NO_APPLICABLE_CODE, version);
        }
        
        if (!request.isAppend()) {
            prop.clear();
        }
        
        for (String servName : request.getCascadedServices().keySet()) {
            prop.put(servName, request.getCascadedServices().get(servName));
        }
        try {
            Utils.storeProperties(prop, cascadingFile);
        } catch (IOException ex) {
            throw new WebServiceException("unable to store the cascading properties file",
                        NO_APPLICABLE_CODE, version);
        }
        
        return new AcknowlegementType("success", "CSW cascaded servers list refreshed");
    }
    
    /**
     * Destroy the CSW index directory in order that it will be recreated.
     * 
     * @param asynchrone
     * @return
     * @throws WebServiceException
     */
    public AcknowlegementType refreshIndex(boolean asynchrone, String service, String id) throws WebServiceException {
        LOGGER.info("refresh index requested");
        String msg;
        if (service != null && service.equalsIgnoreCase("MDSEARCH")) {
            GlobalUtils.resetLuceneIndex();
            msg = "MDWeb search index succefully deleted";
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
     * @param configurationDirectory
     * @throws org.constellation.ws.WebServiceException
     */
    private void synchroneIndexRefresh(File configurationDirectory, String id) throws WebServiceException {
        //we delete each index directory
        for (File indexDir : configurationDirectory.listFiles(new IndexDirectoryFilter(id))) {
            for (File f : indexDir.listFiles()) {
                f.delete();
            }
            if (!indexDir.delete()) {
                throw new WebServiceException("The service can't delete the index folder.", NO_APPLICABLE_CODE);
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
     * @param configurationDirectory
     * @throws org.constellation.ws.WebServiceException
     */
    private void asynchroneIndexRefresh(File configurationDirectory, String id) throws WebServiceException {
        /*
         * we delete each pre-builded index directory.
         * if there is a specific id in parameter we only delete the specified profile
         */
        for (File indexDir : configurationDirectory.listFiles(new NextIndexDirectoryFilter(id))) {
            for (File f : indexDir.listFiles()) {
                f.delete();
            }
            if (!indexDir.delete()) {
                throw new WebServiceException("The service can't delete the next index folder.", NO_APPLICABLE_CODE);
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
                indexer = initIndexer(currentId);
                if (indexer != null) {
                    nexIndexDir.mkdir();
                    indexer.setFileDirectory(nexIndexDir);
                    indexer.createIndex();

                } else {
                    throw new WebServiceException("There is no indexer for the id:" + id, NO_APPLICABLE_CODE);
                }
            } finally {
                if (indexer != null) {
                    indexer.destroy();
                }
            }
        }
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
    
    public abstract AcknowlegementType updateContacts() throws WebServiceException;
    
    public abstract AcknowlegementType updateVocabularies() throws WebServiceException;

    protected abstract File getConfigurationDirectory();

    /**
     * destroy all the resource and close the connection.
     */
    public void destroy() {
    }
}
