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
package org.constellation.configuration.ws;

// J2SE dependencies
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

// Jersey dependencies
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.sun.jersey.spi.resource.Singleton;

// JAXB dependencies
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

// constellation dependencies
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.CSWCascadingType;
import org.constellation.configuration.UpdatePropertiesFileType;
import org.constellation.coverage.web.Service;
import org.constellation.coverage.web.ServiceVersion;
import org.constellation.coverage.web.WebServiceException;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.generic.nerc.CodeTableType;
import org.constellation.generic.nerc.WhatsListsResponse;
import org.constellation.metadata.Utils;
import org.constellation.metadata.index.GenericIndex;
import org.constellation.metadata.index.IndexLucene;
import org.constellation.metadata.index.MDWebIndex;
import org.constellation.metadata.io.CDIReader;
import org.constellation.metadata.io.CSRReader;
import org.constellation.metadata.io.EDMEDReader;
import org.constellation.metadata.io.GenericMetadataReader;
import org.constellation.ows.OWSExceptionCode;
import org.constellation.ows.v110.ExceptionReport;
import static org.constellation.ows.OWSExceptionCode.*;
import static org.constellation.generic.database.Automatic.*;
import org.constellation.ws.rs.ContainerNotifierImpl;
import org.constellation.ws.rs.OGCWebService;

// geotools dependencies
import org.geotools.metadata.note.Anchors;
import org.geotools.resources.JDBC;

// model dependencies
import org.mdweb.model.schemas.Standard;
import org.mdweb.sql.v20.Reader20;
import org.mdweb.utils.GlobalUtils;

// postgres dependencies
import org.postgresql.ds.PGSimpleDataSource;

/**
 * A Web service dedicate to perform administration and configuration operations
 * 
 * @author Guilhem Legal
 */
@Path("configuration")
@Singleton
public class ConfigurationService extends OGCWebService  {

    /**
     * A container notifier allowing to dynamically reload all the active service.
     */
    @Context
    private ContainerNotifierImpl cn;
    
    /**
     * A lucene Index used to pre-build a CSW index.
     */
    private IndexLucene indexer;
    
    private File cswConfigDir;

    private boolean CSWFunctionEnabled;
    
    private static Map<String, String> serviceDirectory = new HashMap<String, String>();
    static {
        serviceDirectory.put("CSW",      "csw_configuration");
        serviceDirectory.put("SOS",      "sos_configuration");
        serviceDirectory.put("MDSEARCH", "mdweb/search");
    }
    
    private static final ServiceVersion version = new ServiceVersion(Service.OTHER, "1.0.0");
    
            
    public ConfigurationService() {
        super("Configuration", version);
        try {
            setXMLContext("org.constellation.ows.v110:org.constellation.configuration:" +
                           "org.constellation.skos:org.constellation.generic.nerc", "");
            
            File sicadeDir    = getSicadeDirectory();
            cswConfigDir = new File(sicadeDir, "csw_configuration");
            File f         = null ;
            try {
                // we get the CSW configuration file
                f                  = new File(cswConfigDir, "config.properties");
                FileInputStream in = new FileInputStream(f);
                Properties prop    = new Properties();
                prop.load(in);
                in.close();
                
                String databaseType = prop.getProperty("DBType");
                
                // if The database is unknow to the service we use the generic metadata reader.
                if (databaseType != null && databaseType.equals("generic")) {
                    
                    JAXBContext jb = JAXBContext.newInstance("org.constellation.generic.database");
                    Unmarshaller genericUnmarshaller = jb.createUnmarshaller();
                    File configFile = new File(cswConfigDir, "generic-configuration.xml");
                    if (configFile.exists()) {
                        Automatic genericConfiguration = (Automatic) genericUnmarshaller.unmarshal(configFile);
                        BDD dbProperties = genericConfiguration.getBdd();
                        if (dbProperties == null) {
                            LOGGER.warning("the generic configuration file does not contains a BDD object. specific CSW operation will not be available.");
                            CSWFunctionEnabled = false;
                        } else {
                            JDBC.loadDriver(dbProperties.getClassName());
                            try {
                                Connection MDConnection = DriverManager.getConnection(dbProperties.getConnectURL(),
                                                                           dbProperties.getUser(),
                                                                           dbProperties.getPassword());
                            
                                GenericMetadataReader MDReader = null;
                                switch (genericConfiguration.getType()) {
                                        case CDI: 
                                            MDReader = new CDIReader(genericConfiguration, MDConnection, false);
                                            break;
                                        case CSR:
                                            MDReader = new CSRReader(genericConfiguration, MDConnection, false);
                                            break;
                                        case EDMED:
                                            MDReader = new EDMEDReader(genericConfiguration, MDConnection, false);
                                            break;
                                        default: 
                                            LOGGER.severe("specific CSW operation will not be available!" + '\n' +
                                            "cause: Unknow generic database type!");
                                    }                                           
                                indexer = new GenericIndex(MDReader);
                                CSWFunctionEnabled = true;
                            } catch (SQLException e){
                                LOGGER.warning("SQLException while connecting to the CSW database, specific CSW operation will not be available." + '\n' +
                                               "cause: " + e.getMessage());
                                CSWFunctionEnabled = false;
                            }
                        }
                    } else {
                        LOGGER.warning("No generic database configuration file have been found, specific CSW operation will not be available.");
                        CSWFunctionEnabled = false;
                    }
                } else {
                    PGSimpleDataSource dataSourceMD = new PGSimpleDataSource();
                    dataSourceMD.setServerName(prop.getProperty("MDDBServerName"));
                    dataSourceMD.setPortNumber(Integer.parseInt(prop.getProperty("MDDBServerPort")));
                    dataSourceMD.setDatabaseName(prop.getProperty("MDDBName"));
                    dataSourceMD.setUser(prop.getProperty("MDDBUser"));
                    dataSourceMD.setPassword(prop.getProperty("MDDBUserPassword"));
                    try {
                        Connection MDConnection    = dataSourceMD.getConnection();
                        Reader20 reader = new Reader20(Standard.ISO_19115, MDConnection);
                        indexer = new MDWebIndex(reader);
                        CSWFunctionEnabled = true;
                    } catch (SQLException e) {
                        LOGGER.warning("SQLException while connecting to the CSW database, specific CSW operation will not be available." + '\n' +
                                       "cause: " + e.getMessage());
                        CSWFunctionEnabled = false;
                    }
                    
                }

            } catch (FileNotFoundException e) {
                LOGGER.warning("No CSW configuration has been found, specific CSW operation will not be available." + '\n' +
                               "cause: " + e.getMessage());
                CSWFunctionEnabled = false;
            } catch (IOException e) {
                LOGGER.warning("The CSW configuration file can not be read, specific CSW operation will not be available." + '\n' +
                               "cause: " + e.getMessage());
                CSWFunctionEnabled = false;
            }
        } catch (JAXBException ex) {
            LOGGER.severe("JAXBexception while setting the JAXB context for configuration service");
            ex.printStackTrace();
            CSWFunctionEnabled = false;
        }
        LOGGER.info("Configuration service runing");
    }
    
    
    @Override
    public Response treatIncomingRequest(Object objectRequest) throws JAXBException {
        try {
            String request  = "";
            StringWriter sw = new StringWriter();
            
            if (objectRequest == null) {
                request = (String) getParameter("REQUEST", true);
            }
            
            if (request.equalsIgnoreCase("restart")) {
                
                marshaller.marshal(restartService(), sw);
                return Response.ok(sw.toString(), "text/xml").build();
                
            } else if (request.equalsIgnoreCase("refreshIndex")) {
            
                boolean asynchrone = Boolean.parseBoolean((String) getParameter("ASYNCHRONE", false));
                String service     = getParameter("SERVICE", false);
                
                marshaller.marshal(refreshIndex(asynchrone, service), sw);
                return Response.ok(sw.toString(), "text/xml").build();
            
            } else if (request.equalsIgnoreCase("refreshCascadedServers") || objectRequest instanceof CSWCascadingType) {
                
                CSWCascadingType refreshCS = (CSWCascadingType) objectRequest;
                
                marshaller.marshal(refreshCascadedServers(refreshCS), sw);
                return Response.ok(sw.toString(), "text/xml").build();
            
            } else if (request.equalsIgnoreCase("UpdatePropertiesFile") || objectRequest instanceof UpdatePropertiesFileType) {
                
                UpdatePropertiesFileType updateProp = (UpdatePropertiesFileType) objectRequest;
                
                marshaller.marshal(updatePropertiesFile(updateProp), sw);
                return Response.ok(sw.toString(), "text/xml").build();
                
            } else if (request.equalsIgnoreCase("download")) {    
                File f = downloadFile();
                
                return Response.ok(f, MediaType.MULTIPART_FORM_DATA_TYPE).build(); 
            
            } else if (request.equalsIgnoreCase("updateVocabularies")) {    
                                
                return Response.ok(updateVocabularies(),"text/xml").build(); 
            }
            else {
                throw new WebServiceException("The operation " + request + " is not supported by the service",
                                                 OPERATION_NOT_SUPPORTED, version, "Request");
            }
        
        } catch (WebServiceException ex) {
            final String code = transformCodeName(ex.getExceptionCode().name());
            final ExceptionReport report = new ExceptionReport(ex.getMessage(), code, ex.getLocator(), getCurrentVersion());
            if (!ex.getExceptionCode().equals(OWSExceptionCode.MISSING_PARAMETER_VALUE) &&
                    !ex.getExceptionCode().equals(OWSExceptionCode.VERSION_NEGOTIATION_FAILED) &&
                    !ex.getExceptionCode().equals(OWSExceptionCode.OPERATION_NOT_SUPPORTED)) {
                ex.printStackTrace();
            } else {
                LOGGER.info(ex.getMessage());
            }
            StringWriter sw = new StringWriter();
            marshaller.marshal(report, sw);
            return Response.ok(cleanSpecialCharacter(sw.toString()), "text/xml").build();
        }
        
    }
    
    /**
     * Restart all the web-services.
     * 
     * @return an Acknowlegement if the restart succeed.
     */
    private AcknowlegementType restartService() {
        LOGGER.info("\n restart requested \n");
        Anchors.clear();
        cn.reload();
        return new AcknowlegementType("success", "services succefully restarted");
    }
    
    /**
     * Destroy the CSW index directory in order that it will be recreated.
     * 
     * @param asynchrone
     * @return
     * @throws WebServiceException
     */
    private AcknowlegementType refreshIndex(boolean asynchrone, String service) throws WebServiceException {
        LOGGER.info("refresh index requested");
        String msg;
        if (service != null && service.equalsIgnoreCase("MDSEARCH")) {
            GlobalUtils.resetLuceneIndex();
            msg = "MDWeb search index succefully deleted";
        } else {
            
            if (!asynchrone) {
                File indexDir     = new File(cswConfigDir, "index");

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
                cn.reload();

            } else {
                if (CSWFunctionEnabled) {
                    File indexDir     = new File(cswConfigDir, "nextIndex");

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
                    try  {
                        indexer.createIndex();
                    } catch (SQLException ex) {
                        throw new WebServiceException("SQLException while creating the index.", NO_APPLICABLE_CODE, version);
                    }
                } else {
                    throw new WebServiceException("This CSW function is not enabled.", NO_APPLICABLE_CODE, version);
                }
            }
            msg = "CSW index succefully recreated";
        }
        return new AcknowlegementType("success", msg);
    }
    
    /**
     * Refresh the properties file used by the CSW service to store federated catalogues.
     * 
     * @param request
     * @return
     * @throws org.constellation.coverage.web.WebServiceException
     */
    private AcknowlegementType refreshCascadedServers(CSWCascadingType request) throws WebServiceException {
        LOGGER.info("refresh cascaded servers requested");
        
        File cascadingFile = new File(cswConfigDir, "CSWCascading.properties");
        
        Properties prop    = getPropertiesFromFile(cascadingFile);
        
        if (!request.isAppend()) {
            prop.clear();
        }
        
        for (String servName : request.getCascadedServices().keySet()) {
            prop.put(servName, request.getCascadedServices().get(servName));
        }
        
        storeProperties(prop, cascadingFile);
        
        return new AcknowlegementType("success", "CSW cascaded servers list refreshed");
    }
    
    /**
     * Update a properties file on the server file system.
     * 
     * @param request
     * @return
     * @throws org.constellation.coverage.web.WebServiceException
     */
    private AcknowlegementType updatePropertiesFile(UpdatePropertiesFileType request) throws WebServiceException {
        LOGGER.info("update properties file requested");
        
        String service  = request.getService();
        String fileName = request.getFileName();
        Map<String, String> newProperties = request.getProperties();
        
        if ( service == null) {
            throw new WebServiceException("You must specify the service parameter.",
                                              MISSING_PARAMETER_VALUE, version, "service");
        } else if (!serviceDirectory.keySet().contains(service)) {
            String msg = "Invalid value for the service parameter: " + service + '\n' +
                         "accepted values are:";
            for (String s: serviceDirectory.keySet()) {
                msg = msg + s + ',';
            }
            throw new WebServiceException(msg, MISSING_PARAMETER_VALUE, version, "service");
            
        }
        
        if (fileName == null) {
             throw new WebServiceException("You must specify the fileName parameter.", MISSING_PARAMETER_VALUE, version, "fileName");
        }
        
        if (newProperties == null || newProperties.size() == 0) {
             throw new WebServiceException("You must specify a non empty properties parameter.", MISSING_PARAMETER_VALUE, 
                     version, "properties");
        }
        
        File sicadeDir      = getSicadeDirectory();
        File configDir   = new File(sicadeDir, serviceDirectory.get(service));
        File propertiesFile = new File(configDir, fileName);
        
        Properties prop     = new Properties();
        if (propertiesFile.exists()) {
            for (String key : newProperties.keySet()) {
                prop.put(key, newProperties.get(key));
            }
        } else {
            throw new WebServiceException("The file does not exist: " + propertiesFile.getPath(),
                                          NO_APPLICABLE_CODE, version);
        }
        
        storeProperties(prop, propertiesFile);
        
        return new AcknowlegementType("success", "properties file sucessfully updated");
    }
    
    /**
     * Receive a file and write it into the static file path.
     * 
     * @param in The input stream.
     * @return an acknowledgement indicating if the operation succeed or not.
     *
     * @todo Not implemented. This is just a placeholder where we can customize the
     *       download action for some users. Will probably be removed in a future version.
     */
    @PUT
    public AcknowlegementType uploadFile(InputStream in) {
        LOGGER.info("uploading");
        try  {
            String layer = getParameter("layer", false);
            System.out.println("LAYER= " + layer);
            // TODO: implement upload action here.
            in.close();
        } catch (WebServiceException ex) {
            //must never happen in normal case
            LOGGER.severe("Webservice exception while get the layer parameter");
            return new AcknowlegementType("failed", "Webservice exception while get the layer parameter");
        } catch (IOException ex) {
            LOGGER.severe("IO exception while uploading file");
            ex.printStackTrace();
            return new AcknowlegementType("failed", "IO exception while performing upload");
        }
        return new AcknowlegementType("success", "the file has been successfully uploaded");
    }
    
    /**
     * Return a static file present on the server.
     * 
     * @return a file.
     *
     * @todo Not implemented. This is just a placeholder where we can customize the
     *       download action for some users. Will probably be removed in a future version.
     */
    private File downloadFile() throws WebServiceException {
        throw new WebServiceException("Not implemented", NO_APPLICABLE_CODE, version);
    }
    
    
    /**
     * Load the properties from a properies file. 
     * 
     * If the file does not exist it will be created and an empty Properties object will be return.
     * 
     * @param f a properties file.
     * 
     * @return a Properties Object.
     */
    private Properties getPropertiesFromFile(File f) throws WebServiceException {
        if (f != null) {
            Properties prop = new Properties();
            if (f.exists()) {

                FileInputStream in = null;
                try {
                    in = new FileInputStream(f);
                    prop.load(in);
                    in.close();

                //this case must never happen
                } catch (FileNotFoundException ex) {
                    LOGGER.severe("FileNotFound " + f.getPath() + " properties file");
                    throw new WebServiceException("FileNotFound " + f.getPath() + " properties file",
                            NO_APPLICABLE_CODE, version);

                } catch (IOException ex) {
                    LOGGER.severe("unable to load the " + f.getPath() + " properties file");
                    throw new WebServiceException("unable to load the " + f.getPath() + " properties file",
                            NO_APPLICABLE_CODE, version);
                }
            } else {
                try {
                    f.createNewFile();
                } catch (IOException ex) {
                    LOGGER.severe("unable to create the cascading properties file");
                    throw new WebServiceException("unable to create the cascading properties file",
                            NO_APPLICABLE_CODE, version);
                }
            }
            return prop;
        } else {
            throw new IllegalArgumentException(" the properties file can't be null");
        }
    }
    
    /**
     * store an Properties object "prop" into the specified File
     * 
     * @param prop A properties Object.
     * @param f    A file.
     * @throws org.constellation.coverage.web.WebServiceException
     */
    private void storeProperties(Properties prop, File f) throws WebServiceException {
        if (prop == null || f == null) {
            throw new IllegalArgumentException(" the properties or file can't be null");
        } else {
            try {
                FileOutputStream out = new FileOutputStream(f);
                prop.store(out, "");
                out.close();

            //must never happen    
            } catch (FileNotFoundException ex) {
                LOGGER.severe("FileNotFound " + f.getPath() + " properties file (no normal)");
                throw new WebServiceException("FileNotFound " + f.getPath() + " properties file",
                        NO_APPLICABLE_CODE, version);

            } catch (IOException ex) {
                LOGGER.severe("unable to store the " + f.getPath() + " properties file");
                throw new WebServiceException("unable to store the " + f.getPath() + "properties file",
                        NO_APPLICABLE_CODE, version);
            }
        }
    }
    
     /**
     * Update all the vocabularies skos files.
     */
    private AcknowlegementType updateVocabularies() throws WebServiceException {
        File vocabularyDir = new File(cswConfigDir, "vocabulary");
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
}
