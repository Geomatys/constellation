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

import com.sun.jersey.spi.resource.Singleton;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.configuration.CSWCascadingType;
import org.constellation.configuration.UpdatePropertiesFileType;
import org.constellation.coverage.web.Service;
import org.constellation.coverage.web.ServiceVersion;
import org.constellation.coverage.web.WMSWebServiceException;
import org.constellation.coverage.web.WebServiceException;
import org.constellation.ows.OWSExceptionCode;
import static org.constellation.ows.OWSExceptionCode.*;
import org.constellation.ows.v110.OWSWebServiceException;
import org.constellation.ws.rs.ContainerNotifierImpl;
import org.constellation.ws.rs.OGCWebService;

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
    ContainerNotifierImpl cn;

    private static Map<String, String> serviceDirectory = new HashMap<String, String>();
    static {
        serviceDirectory.put("CSW",      "csw_configuration");
        serviceDirectory.put("SOS",      "sos_configuration");
        serviceDirectory.put("MDSEARCH", "MDWeb_search");
    }
    
    private static final ServiceVersion version = new ServiceVersion(Service.OTHER, "1.0.0");
    
            
    public ConfigurationService() {
        super("Configuration", version);
        try {
            setXMLContext("org.constellation.ows.v110:org.constellation.configuration", "");
        } catch (JAXBException ex) {
            LOGGER.severe("JAXBexception while setting the JAXB context for configuration service");
            ex.printStackTrace();
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
            
                boolean synchrone = Boolean.parseBoolean((String) getParameter("SYNCHRONE", false));
                
                marshaller.marshal(refreshIndex(synchrone), sw);
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
            } else {
                throw new OWSWebServiceException("The operation " + request + " is not supported by the service",
                                                 OPERATION_NOT_SUPPORTED, "Request", version);
            }
        
        } catch (WebServiceException ex) {

            if (ex instanceof WMSWebServiceException) {
                ex = new OWSWebServiceException(ex.getMessage(),
                        OWSExceptionCode.valueOf(ex.getExceptionCode().name()),
                        null,
                        getVersionFromNumber(ex.getVersion()));
            }
            /* We don't print the stack trace:
             * - if the user have forget a mandatory parameter.
             * - if the version number is wrong.
             */
           
            OWSWebServiceException owsex = (OWSWebServiceException) ex;
            if (!owsex.getExceptionCode().equals(OWSExceptionCode.MISSING_PARAMETER_VALUE) &&
                    !owsex.getExceptionCode().equals(OWSExceptionCode.VERSION_NEGOTIATION_FAILED) &&
                    !owsex.getExceptionCode().equals(OWSExceptionCode.OPERATION_NOT_SUPPORTED)) {
                owsex.printStackTrace();
            } else {
                LOGGER.info(owsex.getMessage());
            }
            StringWriter sw = new StringWriter();
            marshaller.marshal(owsex.getExceptionReport(), sw);
            return Response.ok(cleanSpecialCharacter(sw.toString()), "text/xml").build();
        }
        
    }
    
    /**
     * Restart all the web-services.
     * 
     * @return an Acknowlegement if the restart succeed.
     */
    private AcknowlegementType restartService() {
        LOGGER.info("restart requested");
        cn.reload();
        return new AcknowlegementType("success", "services succefully restarted");
    }
    
    /**
     * Destroy the CSW index directory in order that it will be recreated.
     * 
     * @param synchrone
     * @return
     * @throws org.constellation.ows.v110.OWSWebServiceException
     */
    private AcknowlegementType refreshIndex(boolean synchrone) throws OWSWebServiceException {
        LOGGER.info("refresh index requested");
        
        File sicadeDir    = getSicadeDirectory(); 
        File cswConfigDir = new File(sicadeDir, "csw_configuration");
        File indexDir     = new File(cswConfigDir, "index");

        if (indexDir.exists() && indexDir.isDirectory()) {
            for (File f: indexDir.listFiles()) {
                f.delete();
            }
            boolean succeed =indexDir.delete();
            
            if (!succeed) {
                throw new OWSWebServiceException("The service can't delete the index folder.",
                                                 NO_APPLICABLE_CODE,
                                                 null, version);
            }
        } else {
            throw new OWSWebServiceException("the index folder does not exist.",
                                                 NO_APPLICABLE_CODE,
                                                 null, version);
        }
        
        //then we restart the services
        cn.reload();
            
        return new AcknowlegementType("success", "CSW index succefully recreated");
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
        
        File sicadeDir     = getSicadeDirectory();
        File cswConfigDir  = new File(sicadeDir, "csw_configuration");
        File cascadingFile = new File(cswConfigDir, "CSWCascading.properties");
        
        Properties prop    = getPropertiesFromFile(cascadingFile);
        
        if (!request.isAppend()) {
            prop.clear();
        }
        
        for (String servName : request.getCascadedServices().keySet()) {
            prop.put(servName, request.getCascadedServices().get(servName));
        }
        
        storeProperties(prop, cascadingFile);
        
        //then we restart the services
        cn.reload();
        
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
            throw new OWSWebServiceException("You must specify the service parameter.",
                                              MISSING_PARAMETER_VALUE,
                                             "service", version);
        } else if (!serviceDirectory.keySet().contains(service)) {
            String msg = "Invalid value for the service parameter: " + service + '\n' +
                         "accepted values are:";
            for (String s: serviceDirectory.keySet()) {
                msg = msg + s + ',';
            }
            throw new OWSWebServiceException(msg, MISSING_PARAMETER_VALUE,
                                             "service", version);
            
        }
        
        if (fileName == null) {
             throw new OWSWebServiceException("You must specify the fileName parameter.", MISSING_PARAMETER_VALUE,
                                             "fileName", version);
        }
        
        if (newProperties == null || newProperties.size() == 0) {
             throw new OWSWebServiceException("You must specify a non empty properties parameter.", MISSING_PARAMETER_VALUE,
                                             "properties", version);
        }
        
        File sicadeDir      = getSicadeDirectory();
        File cswConfigDir   = new File(sicadeDir, serviceDirectory.get(service));
        File propertiesFile = new File(cswConfigDir, fileName);
        
        Properties prop     = new Properties();
        if (propertiesFile.exists()) {
            for (String key : newProperties.keySet()) {
                prop.put(key, newProperties.get(key));
            }
        } else {
            throw new OWSWebServiceException("The file does not exist: " + propertiesFile.getPath(),
                                              NO_APPLICABLE_CODE, null, version);
        }
        
        storeProperties(prop, propertiesFile);
        
        //then we restart the services
        cn.reload();
        
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
        throw new OWSWebServiceException("Not implemented", NO_APPLICABLE_CODE, null, version);
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
                    throw new OWSWebServiceException("FileNotFound " + f.getPath() + " properties file",
                            NO_APPLICABLE_CODE,
                            null, version);

                } catch (IOException ex) {
                    LOGGER.severe("unable to load the " + f.getPath() + " properties file");
                    throw new OWSWebServiceException("unable to load the " + f.getPath() + " properties file",
                            NO_APPLICABLE_CODE,
                            null, version);
                }
            } else {
                try {
                    f.createNewFile();
                } catch (IOException ex) {
                    LOGGER.severe("unable to create the cascading properties file");
                    throw new OWSWebServiceException("unable to create the cascading properties file",
                            NO_APPLICABLE_CODE,
                            null, version);
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
                throw new OWSWebServiceException("FileNotFound " + f.getPath() + " properties file",
                        NO_APPLICABLE_CODE,
                        null, version);

            } catch (IOException ex) {
                LOGGER.severe("unable to store the " + f.getPath() + " properties file");
                throw new OWSWebServiceException("unable to store the " + f.getPath() + "properties file",
                        NO_APPLICABLE_CODE,
                        null, version);
            }
        }
    } 
}
