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
             String request = "";
             if (objectRequest == null)
                request = (String) getParameter("REQUEST", true);

             if (request.equalsIgnoreCase("GetObservation") || (objectRequest instanceof GetObservation)) {
                final GetObservation go = (GetObservation) objectRequest;
                if (go == null){
                    throwUnsupportedGetMethod("GetObservation");
                }
                serviceDef = getVersionFromNumber(go.getVersion());
                final Object response = worker.getObservation(go);

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

             if (request.equalsIgnoreCase("DescribeSensor") || (objectRequest instanceof DescribeSensor)) {
                DescribeSensor ds = (DescribeSensor)objectRequest;
                if (ds == null){
                    ds = createDescribeSensor();
                }
                serviceDef = getVersionFromNumber(ds.getVersion());
                
                final AbstractSensorML sensor = worker.describeSensor(ds);
                return Response.ok(sensor, MimeType.TEXT_XML).build();
             }

             if (request.equalsIgnoreCase("GetFeatureInterest") || (objectRequest instanceof GetFeatureOfInterest)) {
                final GetFeatureOfInterest gf = (GetFeatureOfInterest)objectRequest;

                if (gf == null) {
                    throwUnsupportedGetMethod("GetFeatureOfInterest");
                }
                if (gf.getVersion() != null)
                    serviceDef = getVersionFromNumber(gf.getVersion());
                final SOSResponseWrapper response = new SOSResponseWrapper(worker.getFeatureOfInterest(gf));
                return Response.ok(response, worker.getOutputFormat()).build();

             }

             if (request.equalsIgnoreCase("InsertObservation") || (objectRequest instanceof InsertObservation)) {
                final InsertObservation is = (InsertObservation)objectRequest;
                if (is == null){
                    throwUnsupportedGetMethod("InsertObservation");
                }
                serviceDef = getVersionFromNumber(is.getVersion());
                return Response.ok(worker.insertObservation(is), MimeType.TEXT_XML).build();

             }

             if (request.equalsIgnoreCase("GetResult") || (objectRequest instanceof GetResult)) {
                final GetResult gr = (GetResult)objectRequest;
                if (gr == null){
                    throwUnsupportedGetMethod("GetResult");
                }
                serviceDef = getVersionFromNumber(gr.getVersion());

                return Response.ok(worker.getResult(gr), MimeType.TEXT_XML).build();

             }

             if (request.equalsIgnoreCase("RegisterSensor") || (objectRequest instanceof RegisterSensor)) {
                final RegisterSensor rs = (RegisterSensor)objectRequest;
                if (rs == null){
                    throwUnsupportedGetMethod("RegisterSensor");
                }
                serviceDef = getVersionFromNumber(rs.getVersion());

                return Response.ok(worker.registerSensor(rs), MimeType.TEXT_XML).build();

             }

             if (request.equalsIgnoreCase("GetCapabilities") || (objectRequest instanceof GetCapabilities)) {
                worker.setSkeletonCapabilities((Capabilities)getStaticCapabilitiesObject());
                GetCapabilities gc = (GetCapabilities)objectRequest;
                /*
                 * if the parameters have been send by GET or POST kvp,
                 * we build a request object with this parameter.
                 */
                if (gc == null) {

                    gc = createNewGetCapabilities();
                }
                if (gc.getVersion() != null) {
                    serviceDef = getVersionFromNumber(gc.getVersion().toString());
                }
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
        final String exceptionCode = getExceptionCodeRepresentation(ex.getExceptionCode());
        final ExceptionReport report = new ExceptionReport(ex.getMessage(), exceptionCode, ex.getLocator(),
                                                     serviceDef.exceptionVersion.toString());
        return Response.ok(report, MimeType.TEXT_XML).build();
    }

    /**
     * Build a new getCapabilities request from kvp encoding
     */
    private GetCapabilities createNewGetCapabilities() throws CstlServiceException, JAXBException {

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
    private DescribeSensor createDescribeSensor() throws CstlServiceException, JAXBException {

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
