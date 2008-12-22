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
package org.constellation.sos.ws.rs;

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
import javax.annotation.PreDestroy;
import javax.xml.bind.JAXBException;

// geotools dependencies

// Constellation dependencies
import org.constellation.catalog.NoSuchTableException;
import org.constellation.ws.ServiceType;
import org.constellation.ws.ServiceVersion;
import org.constellation.ws.WebServiceException;
import org.constellation.ws.rs.OGCWebService;
import org.constellation.ows.v110.AcceptFormatsType;
import org.constellation.ows.v110.AcceptVersionsType;
import org.constellation.ows.v110.ExceptionReport;
import org.constellation.ows.v110.SectionsType;
import org.constellation.sos.Capabilities;
import org.constellation.sos.DescribeSensor;
import org.constellation.sos.GetCapabilities;
import org.constellation.sos.GetObservation;
import org.constellation.sos.GetResult;
import org.constellation.sos.InsertObservation;
import org.constellation.sos.RegisterSensor;
import org.constellation.sos.ws.SOSworker;
import org.constellation.util.Utils;
import static org.constellation.ows.OWSExceptionCode.*;

/**
 *
 * @author Guilhem Legal
 */
@Path("sos")
@Singleton
public class SOService extends OGCWebService {

    private SOSworker worker;
    
    /**
     * Build a new Restfull SOS service.
     */
    public SOService() throws SQLException, NoSuchTableException, IOException, JAXBException, WebServiceException {
        super("SOS", new ServiceVersion(ServiceType.OWS, "1.0.0"));
        worker = new SOSworker(SOSworker.TRANSACTIONAL);
        setXMLContext("org.constellation.sos:org.constellation.gml.v311:org.constellation.swe.v100:org.constellation.swe.v101:" +
                "org.constellation.observation:org.constellation.sml.v100:org.constellation.sml.v101", "");
    }

    @Override
    public Response treatIncomingRequest(Object objectRequest) throws JAXBException {
         try {
             worker.setServiceURL(getServiceURL());
             logParameters();
             String request = "";
             if (objectRequest == null)
                request = (String) getParameter("REQUEST", true);
             
             if (request.equalsIgnoreCase("GetObservation") || (objectRequest instanceof GetObservation)) {
                GetObservation go = (GetObservation)objectRequest;
                if (go == null){
                    throw new WebServiceException("The operation GetObservation is only requestable in XML",
                                                     OPERATION_NOT_SUPPORTED, getActingVersion(), "GetObservation");
                }
                StringWriter sw = new StringWriter();
                marshaller.marshal(worker.getObservation(go), sw);
        
                return Response.ok(sw.toString(), "text/xml").build();
             
             } else if (request.equalsIgnoreCase("DescribeSensor") || (objectRequest instanceof DescribeSensor)) {
                DescribeSensor ds = (DescribeSensor)objectRequest;
                if (ds == null){
                    throw new WebServiceException("The operation DescribeSensor is only requestable in XML",
                                                  OPERATION_NOT_SUPPORTED, getActingVersion(), "DescribeSensor");
                }
                StringWriter sw = new StringWriter();
                marshaller.marshal(worker.describeSensor(ds), sw);

                return Response.ok(sw.toString(), "text/xml").build();
             
             } else if (request.equalsIgnoreCase("InsertObservation") || (objectRequest instanceof InsertObservation)) {
                InsertObservation is = (InsertObservation)objectRequest;
                if (is == null){
                    throw new WebServiceException("The operation InsertObservation is only requestable in XML",
                                                     OPERATION_NOT_SUPPORTED, getActingVersion(), "InsertObservation");
                }
                StringWriter sw = new StringWriter();
                marshaller.marshal(worker.insertObservation(is), sw);

                return Response.ok(sw.toString(), "text/xml").build();
             
             } else if (request.equalsIgnoreCase("GetResult") || (objectRequest instanceof GetResult)) {
                GetResult gr = (GetResult)objectRequest;
                if (gr == null){
                    throw new WebServiceException("The operation GetResult is only requestable in XML",
                                                     OPERATION_NOT_SUPPORTED, getActingVersion(), "GetResult");
                }
                StringWriter sw = new StringWriter();
                marshaller.marshal(worker.getResult(gr), sw);

                return Response.ok(sw.toString(), "text/xml").build();
             
             } else if (request.equalsIgnoreCase("RegisterSensor") || (objectRequest instanceof RegisterSensor)) {
                RegisterSensor rs = (RegisterSensor)objectRequest;
                if (rs == null){
                    throw new WebServiceException("The operation RegisterSensor is only requestable in XML",
                                                  OPERATION_NOT_SUPPORTED, getActingVersion(), "RegisterSensor");
                }
                StringWriter sw = new StringWriter();
                marshaller.marshal(worker.registerSensor(rs), sw);

                return Response.ok(sw.toString(), "text/xml").build();
             
             } else if (request.equalsIgnoreCase("GetCapabilities") || (objectRequest instanceof GetCapabilities)) {
                try {
                    worker.setStaticCapabilities((Capabilities)getStaticCapabilitiesObject());
                } catch (IOException ex) {
                    throw new WebServiceException("Unable to find change.properties",
                                                     NO_APPLICABLE_CODE, getActingVersion());
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
                throw new WebServiceException("The operation " + request + " is not supported by the service",
                                                 INVALID_PARAMETER_VALUE, getActingVersion(), "request");
            }
             
         } catch (WebServiceException ex) {
            /* We don't print the stack trace:
             * - if the user have forget a mandatory parameter.
             * - if the version number is wrong.
             */
            if (!ex.getExceptionCode().equals(MISSING_PARAMETER_VALUE)   &&
                !ex.getExceptionCode().equals(VERSION_NEGOTIATION_FAILED)&& 
                !ex.getExceptionCode().equals(INVALID_PARAMETER_VALUE)&& 
                !ex.getExceptionCode().equals(OPERATION_NOT_SUPPORTED)) {
                ex.printStackTrace();
            } else {
                LOGGER.info("SENDING EXCEPTION: " + ex.getExceptionCode().name() + " " + ex.getMessage() + '\n');
            }
            ServiceVersion version = ex.getVersion();
            if (marshaller != null) {
                if (version == null) {
                    version = getActingVersion();
                }
                StringWriter sw = new StringWriter();
                ExceptionReport report = new ExceptionReport(ex.getMessage(), ex.getExceptionCode().name(), ex.getLocator(), version);
                marshaller.marshal(report, sw);
                return Response.ok(Utils.cleanSpecialCharacter(sw.toString()), "text/xml").build();
            } else {
                return Response.ok("The SOS server is not running cause: unable to create JAXB context!", "text/plain").build();
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
                    throw new WebServiceException("The section " + token + " does not exist",
                                                  INVALID_PARAMETER_VALUE, getActingVersion(), "Sections");
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

    /**
     * Shutodown the SOS service.
     */
    @PreDestroy
    public void destroy() {
        LOGGER.info("Destroying SOS service");
        if (worker != null) {
            worker.destroy();
        }
    }
}
