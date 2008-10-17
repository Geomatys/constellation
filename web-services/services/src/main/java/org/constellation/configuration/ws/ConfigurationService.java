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
import java.io.StringWriter;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import org.constellation.configuration.CSWCascadingType;
import org.constellation.coverage.web.Service;
import org.constellation.coverage.web.ServiceVersion;
import org.constellation.coverage.web.WMSWebServiceException;
import org.constellation.coverage.web.WebServiceException;
import org.constellation.ows.OWSExceptionCode;
import static org.constellation.ows.OWSExceptionCode.*;
import org.constellation.ows.v110.OWSWebServiceException;
import org.constellation.ws.rs.ContainerNotifierImpl;
import org.constellation.ws.rs.WebService;

/**
 * A Web service dedicate to perform administration and configuration operations
 * 
 * @author Guilhem Legal
 */
@Path("configuration")
@Singleton
public class ConfigurationService extends WebService  {

    /**
     * The user directory where to store the configuration file on Unix platforms.
     */
    private static final String UNIX_DIRECTORY = ".sicade";

    /**
     * The user directory where to store the configuration file on Windows platforms.
     */
    private static final String WINDOWS_DIRECTORY = "Application Data\\Sicade";
    
    
   @Context 
   ContainerNotifierImpl cn; 
    
    public ConfigurationService() {
        super("Configuration", false, new ServiceVersion(Service.OTHER, "1.0.0"));
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
            String request = "";
            
            if (objectRequest == null) {
                request = (String) getParameter("REQUEST", true);
            }
            
            if (request.equalsIgnoreCase("restart")) {
                return restartService();
                
            } else if (request.equalsIgnoreCase("refreshIndex")) {
            
                boolean synchrone = Boolean.parseBoolean((String) getParameter("SYNCHRONE", false));
                
                return refreshIndex(synchrone);
            
            } else if (request.equalsIgnoreCase("refreshCascadedServers") || objectRequest instanceof CSWCascadingType) {
                
                CSWCascadingType refreshCS = (CSWCascadingType) objectRequest;
                
                return refreshCascadedServers(refreshCS);
            
                
            } else {
                throw new OWSWebServiceException("The operation " + request + " is not supported by the service",
                                                 OPERATION_NOT_SUPPORTED, "Request", getCurrentVersion());
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
    
    private Response restartService() {
        LOGGER.info("restart requested");
        cn.reload();
        return Response.ok("<restart>services succefully restarted</restart>", "text/xml").build();
    }
    
    private Response refreshIndex(boolean synchrone) throws OWSWebServiceException {
        LOGGER.info("refresh index requested");
        int i = 0;
        String response   = "<refreshIndex>CSW index succefully recreated</refreshIndex>";
        
        
        File home = new File(System.getProperty("user.home"));
        if (System.getProperty("os.name", "").startsWith("Windows")) {
            home = new File(home, WINDOWS_DIRECTORY);
        } else {
            home = new File(home, UNIX_DIRECTORY);
        }
        
        File cswConfigDir = new File(home, "csw_configuration");
        File indexDir     = new File(cswConfigDir, "index");

        if (indexDir.exists() && indexDir.isDirectory()) {
            for (File f: indexDir.listFiles()) {
                f.delete();
            }
            boolean succeed =indexDir.delete();
            
            if (!succeed) {
                throw new OWSWebServiceException("The service can't delete the index folder.",
                                                 NO_APPLICABLE_CODE,
                                                 null, getCurrentVersion());
            }
        } else {
            throw new OWSWebServiceException("the index folder does not exist.",
                                                 NO_APPLICABLE_CODE,
                                                 null, getCurrentVersion());
        }
        
        //then we restart the services
        cn.reload();
            
        return Response.ok(response, "text/xml").build();
    }
    
    private Response refreshCascadedServers(CSWCascadingType request) throws OWSWebServiceException {
        LOGGER.info("refresh cascaded servers requested");
        
        String response = "<resfreshCascadedServers>ok</resfreshCascadedServers>";
        
        File home = new File(System.getProperty("user.home"));
        if (System.getProperty("os.name", "").startsWith("Windows")) {
            home = new File(home, WINDOWS_DIRECTORY);
        } else {
            home = new File(home, UNIX_DIRECTORY);
        }
        
        File cswConfigDir  = new File(home, "csw_configuration");
        File cascadingFile = new File(cswConfigDir, "CSWCascading.properties");
        
        Properties prop = new Properties();
        if (cascadingFile.exists()) {
            FileInputStream in = null;
            try {
                in = new FileInputStream(cascadingFile);
                prop.load(in);
                in.close();
            
            //this case must never happen
            } catch (FileNotFoundException ex) {
                LOGGER.severe("FileNotFound cascading properties file (no normal)");
                throw new OWSWebServiceException("FileNotFound cascading properties file",
                                                 NO_APPLICABLE_CODE,
                                                 null, getCurrentVersion());
            
            }  catch (IOException ex) {
                LOGGER.severe("unable to load the cascading properties file");
                throw new OWSWebServiceException("unable to load the cascading properties file",
                                                 NO_APPLICABLE_CODE,
                                                 null, getCurrentVersion());
            }
        } else {
            try {
                cascadingFile.createNewFile();
            } catch (IOException ex) {
                LOGGER.severe("unable to create the cascading properties file");
                throw new OWSWebServiceException("unable to create the cascading properties file",
                                                 NO_APPLICABLE_CODE,
                                                 null, getCurrentVersion());
            }
        }
        
        for (String servName : request.getCascadedServices().keySet()) {
            prop.put(servName, request.getCascadedServices().get(servName));
        }
        
        try {
            FileOutputStream out = new FileOutputStream(cascadingFile);
            prop.store(out, "");
            out.close();
        
        //must never happen    
        } catch (FileNotFoundException ex) {
            LOGGER.severe("FileNotFound cascading properties file (no normal)");
            throw new OWSWebServiceException("FileNotFound cascading properties file",
                                             NO_APPLICABLE_CODE,
                                             null, getCurrentVersion());

        } catch (IOException ex) {
            LOGGER.severe("unable to store the cascading properties file");
            throw new OWSWebServiceException("unable to store the cascading properties file",
                                             NO_APPLICABLE_CODE,
                                             null, getCurrentVersion());
        }
        
        //then we restart the services
        cn.reload();
        
        return Response.ok(response, "text/xml").build();
    }
    
    
    @Override
    public int hashCode() {
        return "configuration".hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return obj.hashCode() == this.hashCode();
    }

}
