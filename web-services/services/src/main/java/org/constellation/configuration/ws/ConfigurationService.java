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

import com.sun.jersey.spi.container.ContainerListener;
import com.sun.jersey.spi.container.ContainerNotifier;
import com.sun.jersey.spi.resource.Singleton;
import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
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

   @Context 
   ContainerNotifierImpl cn; 
    
    public ConfigurationService() {
        super("Configuration", false, new ServiceVersion(Service.OTHER, "1.0.0"));
        try {
            setXMLContext("org.constellation.ows.v110", "");
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
            
                return refreshIndex();
            
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
        return Response.ok("<restart>performing.</restart>", "text/xml").build();
    }
    
    private Response refreshIndex() {
        LOGGER.info("refresh index requested");
        int i = 0;
        String response   = "<refreshIndex>performing...</refreshIndex>";
        boolean succeed   = true;
        
        String home       = System.getProperty("user.home");
        File cswConfigDir = new File(home, ".sicade/csw_configuration/");
        File indexDir     = new File(cswConfigDir, "index");

        if (indexDir.exists() && indexDir.isDirectory()) {
            for (File f: indexDir.listFiles()) {
                f.delete();
            }
            succeed =indexDir.delete();
            
            if (!succeed) {
                response = "<refreshIndex>The service can't delete the index folder.</refreshIndex>";
            }
        } else {
            succeed = false;
            response = "<refreshIndex>the index folder does not exist.</refreshIndex>";
        }
        
        //then we restart the service
        if (succeed) {
            cn.reload();
        }
            
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
