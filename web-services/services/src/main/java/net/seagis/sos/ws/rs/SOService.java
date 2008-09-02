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

package net.seagis.sos.ws.rs;

// Jersey dependencies
import java.io.IOException;
import java.sql.SQLException;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import com.sun.jersey.spi.resource.Singleton;
import java.io.StringWriter;

//JAXB dependencies
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.xml.bind.JAXBException;

// geotools dependencies

// seaGIS dependencies
import net.seagis.catalog.NoSuchTableException;
import net.seagis.ows.v110.OWSWebServiceException;
import net.seagis.coverage.web.Service;
import net.seagis.coverage.web.ServiceVersion;
import net.seagis.coverage.web.WebServiceException;
import net.seagis.ws.rs.WebService;
import net.seagis.ows.v110.AcceptFormatsType;
import net.seagis.ows.v110.AcceptVersionsType;
import net.seagis.ows.v110.SectionsType;
import net.seagis.sos.Capabilities;
import net.seagis.sos.DescribeSensor;
import net.seagis.sos.GetCapabilities;
import net.seagis.sos.GetObservation;
import net.seagis.sos.GetResult;
import net.seagis.sos.InsertObservation;
import net.seagis.sos.RegisterSensor;
import net.seagis.sos.ws.SOSworker;
import static net.seagis.ows.OWSExceptionCode.*;

/**
 *
 * @author Guilhem Legal
 */
@Path("sos")
@Singleton
public class SOService extends WebService {

    private SOSworker worker;
    
    /**
     * Build a new Restfull SOS service.
     */
    public SOService() throws SQLException, NoSuchTableException, IOException, JAXBException {
        super("SOS", new ServiceVersion(Service.OWS, "1.0.0"));
        worker = new SOSworker();
        worker.setVersion(getCurrentVersion());
        setXMLContext("net.seagis.sos:net.seagis.gml.v311:net.seagis.swe:net.seagis.observation",
                      "");
    }

    @Override
    public Response treatIncomingRequest(Object objectRequest) throws JAXBException {
         try {
             worker.setServiceURL(getServiceURL());
             writeParameters();
             String request = "";
             if (objectRequest == null)
                request = (String) getParameter("REQUEST", true);
             
             if (request.equalsIgnoreCase("GetObservation") || (objectRequest instanceof GetObservation)) {
                GetObservation go = (GetObservation)objectRequest;
                if (go == null){
                    throw new OWSWebServiceException("The operation GetObservation is only requestable in XML",
                                                     OPERATION_NOT_SUPPORTED, "GetObservation", getCurrentVersion());
                }
                StringWriter sw = new StringWriter();
                marshaller.marshal(worker.getObservation(go), sw);
        
                return Response.ok(sw.toString(), "text/xml").build();
             
             } else if (request.equalsIgnoreCase("DescribeSensor") || (objectRequest instanceof DescribeSensor)) {
                DescribeSensor ds = (DescribeSensor)objectRequest;
                if (ds == null){
                    throw new OWSWebServiceException("The operation DescribeSensor is only requestable in XML",
                                                     OPERATION_NOT_SUPPORTED, "DescribeSensor", getCurrentVersion());
                }
        
                return Response.ok(worker.describeSensor(ds), "text/xml").build();
             
             } else if (request.equalsIgnoreCase("InsertObservation") || (objectRequest instanceof InsertObservation)) {
                InsertObservation is = (InsertObservation)objectRequest;
                if (is == null){
                    throw new OWSWebServiceException("The operation InsertObservation is only requestable in XML",
                                                     OPERATION_NOT_SUPPORTED, "InsertObservation", getCurrentVersion());
                }
        
                return Response.ok(worker.insertObservation(is), "text/xml").build();
             
             } else if (request.equalsIgnoreCase("GetResult") || (objectRequest instanceof GetResult)) {
                GetResult gr = (GetResult)objectRequest;
                if (gr == null){
                    throw new OWSWebServiceException("The operation GetResult is only requestable in XML",
                                                     OPERATION_NOT_SUPPORTED, "GetResult", getCurrentVersion());
                }
        
                return Response.ok(worker.getResult(gr), "text/xml").build();
             
             } else if (request.equalsIgnoreCase("RegisterSensor") || (objectRequest instanceof RegisterSensor)) {
                RegisterSensor rs = (RegisterSensor)objectRequest;
                if (rs == null){
                    throw new OWSWebServiceException("The operation RegisterSensor is only requestable in XML",
                                                     OPERATION_NOT_SUPPORTED, "RegisterSensor", getCurrentVersion());
                }
        
                return Response.ok(worker.registerSensor(rs), "text/xml").build();
             
             } else if (request.equalsIgnoreCase("GetCapabilities") || (objectRequest instanceof GetCapabilities)) {
                try {
                    worker.setStaticCapabilities((Capabilities)getCapabilitiesObject());
                } catch (IOException ex) {
                    throw new OWSWebServiceException("Unable to find change.properties",
                                                     NO_APPLICABLE_CODE, null, getCurrentVersion());  
                }
                GetCapabilities gc = (GetCapabilities)objectRequest;
                /*
                 * if the parameters have been send by GET or POST kvp,
                 * we build a request object with this parameter.
                 */
                if (gc == null) {
                    
                    gc = createNewGetCapabilities();
                }
                StringWriter sw = new StringWriter();
                marshaller.marshal(worker.getCapabilities(gc), sw);
        
                return Response.ok(sw.toString(), worker.getOutputFormat()).build();
                    
            } else {
                throw new OWSWebServiceException("The operation " + request + " is not supported by the service",
                                                 INVALID_PARAMETER_VALUE, "request", getCurrentVersion());
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
                    LOGGER.info("SENDING EXCEPTION: " + owsex.getExceptionCode().name() + " " + owsex.getMessage() + '\n');
                }
                StringWriter sw = new StringWriter();    
                marshaller.marshal(owsex.getExceptionReport(), sw);
                return Response.ok(cleanSpecialCharacter(sw.toString()), "text/xml").build();
            } else {
                throw new IllegalArgumentException("this service can't return WMS Exception");
            }
        }
    }
    
    /**
     * Build a new getCapabilities request from kvp encoding
     */
    private GetCapabilities createNewGetCapabilities() throws WebServiceException, JAXBException {
        
        String version = getParameter("acceptVersions", false);
        AcceptVersionsType versions;
        if (version != null) {
            if (version.indexOf(',') != -1) {
                version = version.substring(0, version.indexOf(','));
            } 
            versions = new AcceptVersionsType(version);
        } else {
            versions = new AcceptVersionsType("1.0.0");
        }
                    
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
                                                     INVALID_PARAMETER_VALUE, "Sections", getCurrentVersion());
                }   
            }
        } else {
            //if there is no requested Sections we add all the sections
            requestedSections = SectionsType.getExistingSections("1.1.1");
        }
        SectionsType sections     = new SectionsType(requestedSections);
        return new GetCapabilities(versions,
                                   sections,
                                   formats,
                                   null,
                                   getParameter("SERVICE", true));
        
    }
}
