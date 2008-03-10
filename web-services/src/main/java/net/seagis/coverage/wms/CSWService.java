/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 * (C) 2007, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package net.seagis.coverage.wms;

import com.sun.ws.rest.spi.resource.Singleton;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import net.seagis.cat.csw.Capabilities;
import net.seagis.cat.csw.GetCapabilities;
import net.seagis.coverage.web.Version;
import net.seagis.coverage.web.WebServiceException;
import net.seagis.ows.AcceptFormatsType;
import net.seagis.ows.AcceptVersionsType;
import net.seagis.ows.OWSWebServiceException;
import net.seagis.ows.SectionsType;
import static net.seagis.ows.OWSExceptionCode.*;

/**
 *
 * @author legal
 */
@Path("csw")
@Singleton
public class CSWService extends WebService {
    
    private CSWworker worker;
    
    /**
     * Build a new Restfull CSW service.
     */
    public CSWService() throws JAXBException {
        super("CSW", new Version("2.0.2", true));
        worker = new CSWworker();
        worker.setVersion("2.0.2");
        setXMLContext("net.seagis.cat.csw:net.seagis.gml:net.seagis.gml","");
    }

    @Override
    public Response treatIncommingRequest(Object objectRequest) throws JAXBException {
        try {
            
            worker.setServiceURL(getServiceURL());
            writeParameters();
            String request = "";
            if (objectRequest == null)
                request = (String) getParameter("REQUEST", true);
            
            if (request.equalsIgnoreCase("GetCapabilities") || (objectRequest instanceof GetCapabilities)) {
                
                GetCapabilities gc = (GetCapabilities)objectRequest;
                /*
                 * if the parameters have been send by GET or POST kvp,
                 * we build a request object with this parameter.
                 */
                if (gc == null) {
                    
                    String version = getParameter("acceptVersions", false);
                    AcceptVersionsType versions;
                    if (version != null) {
                        if (version.indexOf(',') != -1) {
                           version = version.substring(0, version.indexOf(','));
                        } 
                        versions = new AcceptVersionsType(version);
                    } else {
                        versions = new AcceptVersionsType("2.0.2");
                    }
                    
                    worker.setStaticCapabilities((Capabilities)getCapabilitiesObject());
                    AcceptFormatsType formats = new AcceptFormatsType(getParameter("AcceptFormats", false));
                        
                    //We transform the String of sections in a list.
                    //In the same time we verify that the requested sections are valid. 
                    String section = getParameter("Sections", false);
                    List<String> requestedSections = new ArrayList<String>();
                    if (section != null && !section.equalsIgnoreCase("All")) {
                        final StringTokenizer tokens = new StringTokenizer(section, ",;");
                        while (tokens.hasMoreTokens()) {
                            final String token = tokens.nextToken().trim();
                            if (SectionsType.getExistingSections("1.1.1").contains(token)){
                                requestedSections.add(token);
                            } else {
                                throw new OWSWebServiceException("The section " + token + " does not exist",
                                                                INVALID_PARAMETER_VALUE, "Sections", getCurrentVersion().getVersionNumber());
                            }   
                        }
                    } else {
                        //if there is no requested Sections we add all the sections
                        requestedSections = SectionsType.getExistingSections("1.1.1");
                    }
                    SectionsType sections     = new SectionsType(requestedSections);
                    gc = new GetCapabilities(versions,
                                             sections,
                                             formats,
                                             null,
                                             getParameter("SERVICE", true));
                }
                StringWriter sw = new StringWriter();
                marshaller.marshal(worker.getCapabilities(gc), sw);
        
                return Response.ok(sw.toString(), "text/xml").build();
                    
            } else {
                throw new OWSWebServiceException("The operation " + request + " is not supported by the service",
                                                 INVALID_PARAMETER_VALUE, "request", getCurrentVersion().getVersionNumber());
            }
        
        } catch (WebServiceException ex) {
            /* We don't print the stack trace:
             * - if the user have forget a mandatory parameter.
             * - if the version number is wrong.
             */
            if (ex instanceof OWSWebServiceException) {
                OWSWebServiceException owsex = (OWSWebServiceException)ex;
                if (!owsex.getExceptionCode().equals(MISSING_PARAMETER_VALUE)   &&
                    !owsex.getExceptionCode().equals(VERSION_NEGOTIATION_FAILED)&& 
                    !owsex.getExceptionCode().equals(INVALID_PARAMETER_VALUE)&& 
                    !owsex.getExceptionCode().equals(OPERATION_NOT_SUPPORTED)) {
                    owsex.printStackTrace();
                } else {
                    logger.info("SENDING EXCEPTION: " + owsex.getExceptionCode().name() + " " + owsex.getMessage() + '\n');
                }
                StringWriter sw = new StringWriter();    
                marshaller.marshal(owsex.getExceptionReport(), sw);
                return Response.ok(cleanSpecialCharacter(sw.toString()), "text/xml").build();
            } else {
                throw new IllegalArgumentException("this service can't return WMS Exception");
            }
        }
    }
    
    

}
