/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2009, Geomatys
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
import org.geotoolkit.ows.xml.RequestBase;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.annotation.PreDestroy;
import com.sun.jersey.spi.resource.Singleton;

//JAXB dependencies
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.xml.bind.JAXBException;

// Constellation dependencies
import org.constellation.ServiceDef;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.rs.OGCWebService;
import org.geotoolkit.ows.xml.v110.AcceptFormatsType;
import org.geotoolkit.ows.xml.v110.AcceptVersionsType;
import org.geotoolkit.ows.xml.v110.ExceptionReport;
import org.geotoolkit.ows.xml.v110.SectionsType;
import org.geotoolkit.sos.xml.v100.Capabilities;
import org.geotoolkit.sos.xml.v100.DescribeSensor;
import org.geotoolkit.sos.xml.v100.GetCapabilities;
import org.geotoolkit.sos.xml.v100.GetObservation;
import org.geotoolkit.sos.xml.v100.GetResult;
import org.geotoolkit.sos.xml.v100.InsertObservation;
import org.geotoolkit.sos.xml.v100.RegisterSensor;
import org.constellation.sos.ws.SOSworker;
import org.constellation.ws.MimeType;
import org.geotoolkit.observation.xml.v100.ObservationCollectionEntry;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.geotoolkit.sos.xml.SOSMarshallerPool;
import org.geotoolkit.sos.xml.SOSResponseWrapper;
import org.geotoolkit.sos.xml.v100.GetFeatureOfInterest;
import org.geotoolkit.sos.xml.v100.GetFeatureOfInterestTime;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

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
    public SOService() throws JAXBException, CstlServiceException {
        super(ServiceDef.SOS_1_0_0);
        worker = new SOSworker(null);
        setXMLContext(SOSMarshallerPool.getInstance());
    }

    @Override
    public Response treatIncomingRequest(Object objectRequest) throws JAXBException {
        ServiceDef serviceDef = null;
        try {
            worker.setServiceURL(getServiceURL());
            logParameters();

            final RequestBase request;
            if (objectRequest == null) {
                request = adaptQuery(getParameter("REQUEST", true));
            } else if (objectRequest instanceof RequestBase) {
                request = (RequestBase) objectRequest;
            } else {
                throw new CstlServiceException("The operation " + objectRequest.getClass().getName() + " is not supported by the service",
                        INVALID_PARAMETER_VALUE, "request");
            }

            serviceDef = getVersionFromNumber(request.getVersion());

             if (request instanceof GetObservation) {
                final GetObservation go = (GetObservation) request;
                final Object response   = worker.getObservation(go);

                String outputFormat = go.getResponseFormat();
                if (outputFormat != null  && outputFormat.startsWith(MimeType.TEXT_XML)) {
                    outputFormat = MimeType.TEXT_XML;
                }
                Object marshalled;
                if (response instanceof ObservationCollectionEntry) {
                    marshalled = new SOSResponseWrapper((ObservationCollectionEntry) response);
                } else if (response instanceof String) {
                    marshalled = (String) response;
                } else {
                    throw new IllegalArgumentException("Unexpected response type from SOSWorker.getObservation()");
                }
                return Response.ok(marshalled, outputFormat).build();
             }

             if (request instanceof DescribeSensor) {
                final DescribeSensor ds       = (DescribeSensor)request;
                final AbstractSensorML sensor = worker.describeSensor(ds);
                return Response.ok(sensor, MimeType.TEXT_XML).build();
             }

             if (request instanceof GetFeatureOfInterest) {
                final GetFeatureOfInterest gf     = (GetFeatureOfInterest)request;
                final SOSResponseWrapper response = new SOSResponseWrapper(worker.getFeatureOfInterest(gf));
                return Response.ok(response, worker.getOutputFormat()).build();
             }

             if (request instanceof InsertObservation) {
                final InsertObservation is = (InsertObservation)request;
                return Response.ok(worker.insertObservation(is), MimeType.TEXT_XML).build();
             }

             if (request instanceof GetResult) {
                final GetResult gr = (GetResult)request;
                return Response.ok(worker.getResult(gr), MimeType.TEXT_XML).build();
             }

             if (request instanceof RegisterSensor) {
                final RegisterSensor rs = (RegisterSensor)request;
                return Response.ok(worker.registerSensor(rs), MimeType.TEXT_XML).build();
             }

             if (request instanceof GetFeatureOfInterestTime) {
                final GetFeatureOfInterestTime gft = (GetFeatureOfInterestTime)request;
                final SOSResponseWrapper response = new SOSResponseWrapper(worker.getFeatureOfInterestTime(gft));
                return Response.ok(response, MimeType.TEXT_XML).build();
             }

             if (request instanceof GetCapabilities) {
                worker.setSkeletonCapabilities((Capabilities)getStaticCapabilitiesObject(ServiceDef.SOS_1_0_0));
                final GetCapabilities gc = (GetCapabilities)request;
                return Response.ok(worker.getCapabilities(gc), worker.getOutputFormat()).build();
             }

             throw new CstlServiceException("The operation " + request + " is not supported by the service",
                     INVALID_PARAMETER_VALUE, "request");


        } catch (CstlServiceException ex) {
            return processExceptionResponse(ex, serviceDef);

        }
    }

    private void throwUnsupportedGetMethod(String operationName) throws CstlServiceException {
        throw new CstlServiceException("The operation " + operationName + " is only requestable in XML",
                                                  OPERATION_NOT_SUPPORTED, operationName);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected Response processExceptionResponse(final CstlServiceException ex, ServiceDef serviceDef) {
        logException(ex);
        
        if (serviceDef == null) {
            serviceDef = getBestVersion(null);
        }
        final String exceptionCode   = getOWSExceptionCodeRepresentation(ex.getExceptionCode());
        final ExceptionReport report = new ExceptionReport(ex.getMessage(), exceptionCode, ex.getLocator(),
                                                     serviceDef.exceptionVersion.toString());
        return Response.ok(report, MimeType.TEXT_XML).build();
    }

    /**
     * Build request object fom KVP parameters.
     *
     * @param request
     * @return
     * @throws CstlServiceException
     */
    private RequestBase adaptQuery(String request) throws CstlServiceException {
         if ("GetObservation"    .equalsIgnoreCase(request) ||
             "GetFeatureInterest".equalsIgnoreCase(request) ||
             "InsertObservation" .equalsIgnoreCase(request) ||
             "GetResult"         .equalsIgnoreCase(request) ||
             "RegisterSensor"    .equalsIgnoreCase(request)
         ){
             throwUnsupportedGetMethod(request);

         } else if ("DescribeSensor".equalsIgnoreCase(request)) {
             return createDescribeSensor();
         } else if ("GetCapabilities".equalsIgnoreCase(request)) {
             return createNewGetCapabilities();
         }
         throw new CstlServiceException("The operation " + request + " is not supported by the service",
                        INVALID_PARAMETER_VALUE, "request");
    }
    
    /**
     * Build a new getCapabilities request from kvp encoding
     */
    private GetCapabilities createNewGetCapabilities() throws CstlServiceException {

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

        final AcceptFormatsType formats = new AcceptFormatsType(getParameter("AcceptFormats", false));

        //We transform the String of sections in a list.
        //In the same time we verify that the requested sections are valid.
        final String section = getParameter("Sections", false);
        List<String> requestedSections = new ArrayList<String>();
        if (section != null && !section.equalsIgnoreCase("All")) {
            final StringTokenizer tokens = new StringTokenizer(section, ",;");
            while (tokens.hasMoreTokens()) {
                final String token = tokens.nextToken().trim();
                if (SectionsType.getExistingSections("1.1.1").contains(token)){
                    requestedSections.add(token);
                } else {
                    throw new CstlServiceException("The section " + token + " does not exist",
                                                  INVALID_PARAMETER_VALUE, "Sections");
                }
            }
        } else {
            //if there is no requested Sections we add all the sections
            requestedSections = SectionsType.getExistingSections("1.1.1");
        }
        final SectionsType sections     = new SectionsType(requestedSections);
        return new GetCapabilities(versions,
                                   sections,
                                   formats,
                                   null,
                                   getParameter("SERVICE", true));

    }

    /**
     * Build a new getCapabilities request from kvp encoding
     */
    private DescribeSensor createDescribeSensor() throws CstlServiceException {

        return new DescribeSensor(getParameter("VERSION", true),
                                  getParameter("SERVICE", true),
                                  getParameter("PROCEDURE", true),
                                  getParameter("OUTPUTFORMAT", true));


    }

    /**
     * Shutodown the SOS service.
     */
    @PreDestroy
    @Override
    public void destroy() {
        LOGGER.info("Shutting down the REST SOS service facade");
        if (worker != null) {
            worker.destroy();
        }
    }
}
