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
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

// JAXB dependencies
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

// constellation dependencies
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.CSWCascadingType;
import org.constellation.configuration.exception.ConfigurationException;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.util.Utils;
import org.constellation.metadata.factory.AbstractCSWFactory;
import org.constellation.metadata.index.IndexLucene;
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
    protected Map<String, IndexLucene> indexers = new HashMap<String, IndexLucene>();
    
    /**
     * A list of Reader to the database.
     */
    protected List<MetadataReader> readers = new ArrayList<MetadataReader>();

    /**
     * A generic factory to get the correct CSW Factory.
     */
    private static FactoryRegistry factory = new FactoryRegistry(AbstractCSWFactory.class);

    
    public AbstractCSWConfigurer(ContainerNotifierImpl cn) throws ConfigurationException {
        this.containerNotifier = cn;
        File cswConfigDir = getConfigurationDirectory();
        if (cswConfigDir == null || (cswConfigDir != null && !cswConfigDir.isDirectory()))
            throw new ConfigurationException("No configuration directory have been found");
        try {
            JAXBContext jb = JAXBContext.newInstance("org.constellation.generic.database");
            Unmarshaller configUnmarshaller = jb.createUnmarshaller();

            for (File configFile: cswConfigDir.listFiles(new ConfigurationFileFilter(null))) {
                //we get the csw ID (if single mode return "")
                String id = getConfigID(configFile);

                // we get the CSW configuration file
                Automatic config = (Automatic) configUnmarshaller.unmarshal(configFile);
                BDD db = config.getBdd();
                if (db == null) {
                    throw new ConfigurationException("the configuration file does not contains a BDD object.");
                } else {
                    Connection MDConnection = db.getConnection();
                    
                    AbstractCSWFactory CSWfactory = factory.getServiceProvider(AbstractCSWFactory.class, null, null, null);
                    LOGGER.finer("loaded Factory: " + CSWfactory.getClass().getName());
                    MetadataReader currentReader = CSWfactory.getMetadataReader(config, MDConnection, new File(cswConfigDir, "data"), null, cswConfigDir);
                    indexers.put(id, CSWfactory.getIndex(config.getType(), currentReader, MDConnection));
                    readers.add(currentReader);
                }
            }
            if (readers.size() == 0) {
                throw new ConfigurationException("No database configuration file have been found");
            }
        } catch (SQLException e) {
            throw new ConfigurationException("SQL Exception while creating CSWConfigurer.", e.getMessage());
        } catch (WebServiceException e) {
            throw new ConfigurationException("WebServiceException while creating CSWConfigurer.", e.getMessage());
        } catch (JAXBException ex) {
            throw new ConfigurationException("JAXBexception while setting the JAXB context for configuration service");
        } catch (FactoryNotFoundException ex) {
            throw new ConfigurationException("Unable to find a CSW factory for CSW in configuration service");
        } catch (IllegalArgumentException ex) {
            throw new ConfigurationException("IllegalArgumentException: " + ex.getMessage());
        }
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
     *
     * @param configurationDirectory
     * @throws org.constellation.ws.WebServiceException
     */
    private void synchroneIndexRefresh(File configurationDirectory, String id) throws WebServiceException {
        //we delete each index directory
        for (File indexDir : configurationDirectory.listFiles(new indexDirectoryFilter(id))) {
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
     *
     * @param configurationDirectory
     * @throws org.constellation.ws.WebServiceException
     */
    private void asynchroneIndexRefresh(File configurationDirectory, String id) throws WebServiceException {
        /*
         * we delete each pre-builded index directory.
         * if there is a specific id in parameter we only delete the specified profile
         */
        for (File indexDir : configurationDirectory.listFiles(new nextIndexDirectoryFilter(id))) {
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
            String currentId    = getConfigID(configFile);
            File nexIndexDir    = new File(configurationDirectory, currentId + "nextIndex");
            IndexLucene indexer = indexers.get(currentId);
            if (indexer != null) {
                nexIndexDir.mkdir();
                indexer.setFileDirectory(nexIndexDir);
                indexer.createIndex();
            } else {
                throw new WebServiceException("There is no indexer for the id:" + id, NO_APPLICABLE_CODE);
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
     * destroy all the resource and close the connection.
     */
    public abstract void destroy();
    
    
    public abstract AcknowlegementType updateContacts() throws WebServiceException;
    
    public abstract AcknowlegementType updateVocabularies() throws WebServiceException;

    protected abstract File getConfigurationDirectory();

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
     * An internal class to filter the configuration directory and return the configuration files.
     */
    private class ConfigurationFileFilter implements FilenameFilter {

        private String prefix;

        public ConfigurationFileFilter(String id) {
            prefix = "";
            if (id != null)
                prefix = id;
        }

        public boolean accept(File dir, String name) {
            return (name.endsWith(prefix + "config.xml"));
        }

    }

    /**
     * An internal class to filter the configuration directory and return the index directory.
     */
    private class indexDirectoryFilter implements FilenameFilter {

        private String prefix;

        public indexDirectoryFilter(String id) {
            prefix = "";
            if (id != null)
                prefix = id;
        }

        public boolean accept(File dir, String name) {
            File f = new File(dir, name);
            return (name.endsWith(prefix + "index") && f.isDirectory());
        }

    }

    /**
     * An internal class to filter the configuration directory and return the pre-builded index directory.
     */
    private class nextIndexDirectoryFilter implements FilenameFilter {

        private String prefix;
        
        public nextIndexDirectoryFilter(String id) {
            prefix = "";
            if (id != null)
                prefix = id;
        }
        
        public boolean accept(File dir, String name) {
            File f = new File(dir, name);
            return (name.endsWith(prefix + "nextIndex") && f.isDirectory());
        }

    }
}
