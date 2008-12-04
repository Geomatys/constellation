/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
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
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

// JAXB dependencies
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

// Constellation dependencies
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.CSWCascadingType;
import org.constellation.configuration.exception.ConfigurationException;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.generic.nerc.CodeTableType;
import org.constellation.generic.nerc.WhatsListsResponse;
import org.constellation.metadata.Utils;
import org.constellation.metadata.index.IndexLucene;
import org.constellation.metadata.io.GenericCSWFactory;
import org.constellation.metadata.io.MetadataReader;
import org.constellation.ws.WebServiceException;
import org.constellation.ws.rs.ContainerNotifierImpl;
import static org.constellation.generic.database.Automatic.*;
import static org.constellation.configuration.ws.rs.ConfigurationService.*;
import static org.constellation.ows.OWSExceptionCode.*;

// geotools dependencies
import org.geotools.metadata.note.Anchors;
import org.mdweb.utils.GlobalUtils;

/**
 *
 * @author Guilhem Legal
 */
public class CSWconfigurer {

    private Logger LOGGER = Logger.getLogger("org.constellation.configuration.ws.rs");
    
    /**
     * A lucene Index used to pre-build a CSW index.
     */
    private IndexLucene indexer;
    
    /**
     * A JAXB unmarshaller used to create java object from XML file.
     */
    protected Unmarshaller unmarshaller;

    /**
     * A JAXB marshaller used to transform the java object in XML String.
     */
    protected Marshaller marshaller;
    
    /**
     * A container notifier allowing to restart the webService. 
     */
    private ContainerNotifierImpl containerNotifier;
    
    
    public CSWconfigurer(Marshaller marshaller, Unmarshaller unmarshaller, ContainerNotifierImpl cn) throws ConfigurationException {
        this.marshaller        = marshaller;
        this.unmarshaller      = unmarshaller;
        this.containerNotifier = cn;
        File cswConfigDir = serviceDirectory.get("CSW");
        try {
            // we get the CSW configuration file
            JAXBContext jb = JAXBContext.newInstance("org.constellation.generic.database");
            Unmarshaller genericUnmarshaller = jb.createUnmarshaller();

            File configFile = new File(cswConfigDir, "config.xml");
            if (configFile.exists()) {
                Automatic config = (Automatic) genericUnmarshaller.unmarshal(configFile);
                BDD db = config.getBdd();
                if (db == null) {
                    throw new ConfigurationException("the generic configuration file does not contains a BDD object.");
                } else {
                    Connection MDConnection = db.getConnection();
                    MetadataReader MDReader = GenericCSWFactory.getMetadataReader(config, MDConnection);
                    indexer = GenericCSWFactory.getIndex(config.getType(), MDReader, MDConnection);
                }
            } else {
                throw new ConfigurationException("No generic database configuration file have been found");
            }
        } catch (SQLException e) {
            throw new ConfigurationException("SQL Exception while creating CSWConfigurer.", e.getMessage());
        } catch (WebServiceException e) {
            throw new ConfigurationException("WebServiceException while creating CSWConfigurer.", e.getMessage());
        } catch (JAXBException ex) {
            throw new ConfigurationException("JAXBexception while setting the JAXB context for configuration service");
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
                File indexDir     = new File(serviceDirectory.get("CSW"), "index");

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
                containerNotifier.reload();

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
     * Update all the vocabularies skos files.
     */
    public AcknowlegementType updateVocabularies() throws WebServiceException {
        File vocabularyDir = new File(serviceDirectory.get("CSW"), "vocabulary");
        if (!vocabularyDir.exists()) {
            vocabularyDir.mkdir();
        }
        //  we get the Skos and description file for each used list
        saveVocabularyFile("P021",   vocabularyDir);
        saveVocabularyFile("L031",   vocabularyDir);
        saveVocabularyFile("L061",   vocabularyDir);
        saveVocabularyFile("C77",    vocabularyDir);
        saveVocabularyFile("EDMERP", vocabularyDir);
        saveVocabularyFile("L05",    vocabularyDir);
        saveVocabularyFile("L021",   vocabularyDir);
        saveVocabularyFile("L081",   vocabularyDir);
        saveVocabularyFile("L241",   vocabularyDir);
        saveVocabularyFile("L231",   vocabularyDir);
        saveVocabularyFile("C381",   vocabularyDir);
        saveVocabularyFile("C320",   vocabularyDir);
        saveVocabularyFile("C174",   vocabularyDir);
        saveVocabularyFile("C16",    vocabularyDir);
        saveVocabularyFile("C371",   vocabularyDir);
        saveVocabularyFile("L181",   vocabularyDir);
        saveVocabularyFile("C16",    vocabularyDir);
        saveVocabularyFile("L101",    vocabularyDir);
        
        return new AcknowlegementType("success", "the vocabularies has been succefully updated");
    }
    
    /**
     * 
     * @param listNumber
     */
    private void saveVocabularyFile(String listNumber, File directory) throws WebServiceException {
        CodeTableType VocaDescription = getVocabularyDetails(listNumber);
        String filePrefix = "SDN.";
        String url = "http://vocab.ndg.nerc.ac.uk/list/" + listNumber + "/current";
        try {
            if (VocaDescription != null) {
                File f = new File(directory, filePrefix + listNumber + ".xml");
                marshaller.marshal(VocaDescription, f);
            } else {
                LOGGER.severe("no description for vocabulary: " + listNumber + " has been found");
            }
            
            Object vocab = Utils.getUrlContent(url, unmarshaller);
            if (vocab != null) {
                File f = new File(directory, filePrefix + listNumber + ".rdf");
                marshaller.marshal(vocab, f);
            } else {
                LOGGER.severe("no skos file have been found for :" + listNumber);
            }
        
        } catch (JAXBException ex) {
            LOGGER.severe("JAXBException while marshalling the vocabulary: " + url);
            throw new WebServiceException("JAXBException while marshalling the vocabulary: " + url, NO_APPLICABLE_CODE, version);
        } catch (MalformedURLException ex) {
            LOGGER.severe("The url: " + url + " is malformed");
        } catch (IOException ex) {
            LOGGER.severe("IO exception while contacting the URL:" + url);
            throw new WebServiceException("IO exception while contacting the URL:" + url, NO_APPLICABLE_CODE, version);
        }
    }
    
    /**
     * 
     * @param listNumber
     * @param directory
     * @return
     * @throws org.constellation.coverage.web.WebServiceException
     */
    private CodeTableType getVocabularyDetails(String listNumber) throws WebServiceException {
        String url = "http://vocab.ndg.nerc.ac.uk/axis2/services/vocab/whatLists?categoryKey=http://vocab.ndg.nerc.ac.uk/term/C980/current/CL12";
        CodeTableType result = null;
        try {
            
            Object obj = Utils.getUrlContent(url, unmarshaller);
            if (obj instanceof WhatsListsResponse) {
                WhatsListsResponse listDetails = (WhatsListsResponse) obj;
                result = listDetails.getCodeTableFromKey(listNumber);
            }
        
        } catch (MalformedURLException ex) {
            LOGGER.severe("The url: " + url + " is malformed");
        } catch (IOException ex) {
            LOGGER.severe("IO exception while contacting the URL:" + url);
            throw new WebServiceException("IO exception while contacting the URL:" + url, NO_APPLICABLE_CODE, version);
        }
        return result;
    }
    
    public void destroy() {
        indexer.destroy();
    }
    
}
