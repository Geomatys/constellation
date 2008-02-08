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

package net.seagis.sos.webservice;

// Jersey dependencies
import java.io.IOException;
import java.sql.SQLException;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import com.sun.ws.rest.spi.resource.Singleton;
import java.io.StringWriter;

//JAXB dependencies
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

// geotools dependencies
import javax.xml.bind.Marshaller;
import org.geotools.util.Version;

// seaGIS dependencies
import net.seagis.catalog.NoSuchTableException;
import net.seagis.ows.OWSWebServiceException;
import net.seagis.coverage.web.ServiceExceptionReport;
import net.seagis.coverage.web.WebServiceException;
import net.seagis.coverage.wms.NamespacePrefixMapperImpl;
import net.seagis.coverage.wms.WebService;
import net.seagis.observation.ObservationCollectionEntry;
import net.seagis.ows.AcceptFormatsType;
import net.seagis.ows.SectionsType;
import net.seagis.sos.Capabilities;
import net.seagis.sos.DescribeSensor;
import net.seagis.sos.GetCapabilities;
import net.seagis.sos.GetObservation;
import static net.seagis.coverage.wms.WMSExceptionCode.*;

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
        super("SOS", true, "1.0.0");
        worker = new SOSworker();
        worker.setVersion(new Version("1.0.0"));
        JAXBContext jbcontext = JAXBContext.newInstance("net.seagis.gml:net.seagis.observation:net.seagis.ows:net.seagis.sos:net.seagis.swe:net.seagis.ogc:net.seagis.coverage.web");
        unmarshaller = jbcontext.createUnmarshaller();
        jbcontext = JAXBContext.newInstance(Capabilities.class,
                                            ServiceExceptionReport.class,
                                            ObservationCollectionEntry.class);
        marshaller = jbcontext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapperImpl(""));
        
    }

    @Override
    public Response treatIncommingRequest(Object objectRequest) throws JAXBException {
         try {
             writeParameters();
             String request = "";
             if (objectRequest == null)
                request = (String) getParameter("REQUEST", true);
             
             if (request.equalsIgnoreCase("GetObservation") || (objectRequest instanceof GetObservation)) {
                GetObservation go = (GetObservation)objectRequest;
                if (go == null){
                    throw new OWSWebServiceException("The operation GetObservation is only requestable in XML for now",
                                         OPERATION_NOT_SUPPORTED, getCurrentVersion());
                }
                StringWriter sw = new StringWriter();
                marshaller.marshal(worker.getObservation(go), sw);
        
                return Response.ok(sw.toString(), "text/xml").build();
             
             } else if (request.equalsIgnoreCase("DescribeSensor") || (objectRequest instanceof DescribeSensor)) {
                DescribeSensor ds = (DescribeSensor)objectRequest;
                if (ds == null){
                    throw new OWSWebServiceException("The operation DescribeSensor is only requestable in XML for now",
                                         OPERATION_NOT_SUPPORTED, getCurrentVersion());
                }
        
                return Response.ok(worker.describeSensor(ds), "text/xml").build();
             
             } else if (request.equalsIgnoreCase("GetCapabilities") || (objectRequest instanceof GetCapabilities)) {
                
                GetCapabilities gc = (GetCapabilities)objectRequest;
                /*
                 * if the parameters have been send by GET or POST kvp,
                 * we build a request object with this parameter.
                 */
                if (gc == null) {
                    if (!getParameter("SERVICE", true).equalsIgnoreCase("SOS")) {
                        throw new OWSWebServiceException("The parameters SERVICE=SOS must be specify",
                                         MISSING_PARAMETER_VALUE, getCurrentVersion());
                    }
                    
                    AcceptFormatsType formats = new AcceptFormatsType(getParameter("AcceptFormats", false));
                        
                    //We transform the String of sections in a list.
                    //In the same time we verify that the requested sections are valid. 
                    String section = getParameter("Sections", false);
                    List<String> requestedSections = new ArrayList<String>();
                    if (section != null) {
                        final StringTokenizer tokens = new StringTokenizer(section, ",;");
                        while (tokens.hasMoreTokens()) {
                            final String token = tokens.nextToken().trim();
                            if (SectionsType.getExistingSections("1.1.1").contains(token)){
                                requestedSections.add(token);
                            } else {
                                throw new OWSWebServiceException("The section " + token + " does not exist",
                                                              INVALID_PARAMETER_VALUE, getCurrentVersion());
                            }   
                        }
                    } else {
                        //if there is no requested Sections we add all the sections
                        requestedSections = SectionsType.getExistingSections("1.1.1");
                    }
                    SectionsType sections     = new SectionsType(requestedSections);
                    gc = new GetCapabilities(null,
                                             sections,
                                             formats,
                                             null,
                                             "SOS");
                }
                StringWriter sw = new StringWriter();
                marshaller.marshal(worker.getCapabilities(gc), sw);
        
                return Response.ok(sw.toString(), "text/xml").build();
                    
            } else {
                throw new OWSWebServiceException("The operation " + request + " is not supported by the service",
                                              OPERATION_NOT_SUPPORTED, getCurrentVersion());
            }
             
         } catch (WebServiceException ex) {
            /* We don't print the stack trace:
             * - if the user have forget a mandatory parameter.
             * - if the version number is wrong.
             */
            if (ex instanceof OWSWebServiceException) {
                OWSWebServiceException owsex = (OWSWebServiceException)ex;
                if (!owsex.getExceptionCode().equals(MISSING_PARAMETER_VALUE) &&
                    !owsex.getExceptionCode().equals(VERSION_NEGOTIATION_FAILED)) {
                    owsex.printStackTrace();
                } else {
                    logger.info(owsex.getMessage());
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
