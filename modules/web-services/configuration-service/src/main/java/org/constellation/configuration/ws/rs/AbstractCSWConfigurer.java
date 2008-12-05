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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.CSWCascadingType;
import org.constellation.configuration.exception.ConfigurationException;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.metadata.Utils;
import org.constellation.metadata.factory.AbstractCSWFactory;
import org.constellation.metadata.index.IndexLucene;
import org.constellation.metadata.io.MetadataReader;
import org.constellation.ws.WebServiceException;
import org.constellation.ws.rs.ContainerNotifierImpl;
import org.geotools.factory.FactoryNotFoundException;
import org.geotools.factory.FactoryRegistry;
import org.geotools.metadata.note.Anchors;
import org.mdweb.utils.GlobalUtils;
import static org.constellation.configuration.ws.rs.ConfigurationService.*;
import static org.constellation.ows.OWSExceptionCode.*;

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
     * A JAXB unmarshaller used to create java object from XML file.
     */
    protected Unmarshaller unmarshaller;

    /**
     * A JAXB marshaller used to transform the java object in XML String.
     */
    protected Marshaller marshaller;
    
    /**
     * A lucene Index used to pre-build a CSW index.
     */
    protected IndexLucene indexer;
    
     /**
     * A Reader to the database.
     */
    protected MetadataReader reader;
    
    private static FactoryRegistry factory = new FactoryRegistry(AbstractCSWFactory.class);
    
    
    public AbstractCSWConfigurer(Marshaller marshaller, Unmarshaller unmarshaller, ContainerNotifierImpl cn) throws ConfigurationException {
        this.marshaller        = marshaller;
        this.unmarshaller      = unmarshaller;
        this.containerNotifier = cn;
        File cswConfigDir = serviceDirectory.get("CSW");
        try {
            // we get the CSW configuration file
            JAXBContext jb = JAXBContext.newInstance("org.constellation.generic.database");
            Unmarshaller configUnmarshaller = jb.createUnmarshaller();

            File configFile = new File(cswConfigDir, "config.xml");
            if (configFile.exists()) {
                Automatic config = (Automatic) configUnmarshaller.unmarshal(configFile);
                BDD db = config.getBdd();
                if (db == null) {
                    throw new ConfigurationException("the configuration file does not contains a BDD object.");
                } else {
                    Connection MDConnection = db.getConnection();
                    
                    AbstractCSWFactory CSWfactory = factory.getServiceProvider(AbstractCSWFactory.class, null, null, null);
                    reader  = CSWfactory.getMetadataReader(config, MDConnection);
                    indexer = CSWfactory.getIndex(config.getType(), reader, MDConnection);
                }
            } else {
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
        
        File cascadingFile = new File(serviceDirectory.get("CSW"), "CSWCascading.properties");
        Properties prop;
        try {
            prop    = Utils.getPropertiesFromFile(cascadingFile);
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
    public AcknowlegementType refreshIndex(boolean asynchrone, String service) throws WebServiceException {
        LOGGER.info("refresh index requested");
        String msg;
        if (service != null && service.equalsIgnoreCase("MDSEARCH")) {
            GlobalUtils.resetLuceneIndex();
            msg = "MDWeb search index succefully deleted";
        } else {
            
            if (!asynchrone) {
                File indexDir = new File(serviceDirectory.get("CSW"), "index");

                if (indexDir.exists() && indexDir.isDirectory()) {
                    for (File f: indexDir.listFiles()) {
                        f.delete();
                    }
                    boolean succeed = indexDir.delete();

                    if (!succeed) {
                        throw new WebServiceException("The service can't delete the index folder.", NO_APPLICABLE_CODE, version);
                    }
                } else if (indexDir.exists() && !indexDir.isDirectory()){
                    indexDir.delete();
                }

                //then we restart the services
                Anchors.clear();
                restart();

            } else {
                File indexDir     = new File(serviceDirectory.get("CSW"), "nextIndex");

                if (indexDir.exists() && indexDir.isDirectory()) {
                    for (File f: indexDir.listFiles()) {
                        f.delete();
                    }
                    boolean succeed = indexDir.delete();

                    if (!succeed) {
                        throw new WebServiceException("The service can't delete the next index folder.", NO_APPLICABLE_CODE, version);
                    }
                } else if (indexDir.exists() && !indexDir.isDirectory()){
                    indexDir.delete();
                }
                indexer.setFileDirectory(indexDir);
                indexer.createIndex();
            }
            msg = "CSW index succefully recreated";
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
     * destroy all the resource and close the connection.
     */
    public abstract void destroy();
    
    
    public abstract AcknowlegementType updateContacts() throws WebServiceException;
    
    public abstract AcknowlegementType updateVocabularies() throws WebServiceException;

}
