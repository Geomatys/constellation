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

import java.io.StringWriter;

// Jersey dependencies
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import com.sun.jersey.spi.resource.Singleton;

//JAXB dependencies
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.annotation.PreDestroy;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

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
import org.constellation.util.Util;
import org.geotoolkit.observation.xml.v100.ObservationCollectionEntry;
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
        setXMLContext("org.geotoolkit.sos.xml.v100:org.geotoolkit.gml.xml.v311modified:org.geotoolkit.swe.xml.v100:org.geotoolkit.swe.xml.v101:" +
                "org.geotoolkit.observation.xml:org.geotoolkit.sampling.xml:org.geotoolkit.sml.xml.v100:org.geotoolkit.sml.xml.v101", "");
    }

    @Override
    public Response treatIncomingRequest(Object objectRequest) throws JAXBException {
        Marshaller marshaller = null;
        ServiceDef serviceDef = null;
        try {
             marshaller = marshallerPool.acquireMarshaller();
             worker.setServiceURL(getServiceURL());
             logParameters();
             String request = "";
             if (objectRequest == null)
                request = (String) getParameter("REQUEST", true);

             if (request.equalsIgnoreCase("GetObservation") || (objectRequest instanceof GetObservation)) {
                GetObservation go = (GetObservation)objectRequest;
                if (go == null){
                    throw new CstlServiceException("The operation GetObservation is only requestable in XML",
                                                     OPERATION_NOT_SUPPORTED, "GetObservation");
                }
                serviceDef = getVersionFromNumber(go.getVersion());
                Object response = worker.getObservation(go);

                String outputFormat = go.getResponseFormat();
                if (outputFormat != null  && outputFormat.startsWith("text/xml")) {
                    outputFormat = "text/xml";
                }

                String marshalled;
                if (response instanceof ObservationCollectionEntry) {
                     StringWriter sw = new StringWriter();
                     marshaller.marshal(response, sw);
                     marshalled = sw.toString();
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
                StringWriter sw = new StringWriter();
                marshaller.marshal(worker.describeSensor(ds), sw);

                return Response.ok(sw.toString(), "text/xml").build();

             }

             if (request.equalsIgnoreCase("InsertObservation") || (objectRequest instanceof InsertObservation)) {
                InsertObservation is = (InsertObservation)objectRequest;
                if (is == null){
                    throw new CstlServiceException("The operation InsertObservation is only requestable in XML",
                                                     OPERATION_NOT_SUPPORTED, "InsertObservation");
                }
                serviceDef = getVersionFromNumber(is.getVersion());
                StringWriter sw = new StringWriter();
                marshaller.marshal(worker.insertObservation(is), sw);

                return Response.ok(sw.toString(), "text/xml").build();

             }

             if (request.equalsIgnoreCase("GetResult") || (objectRequest instanceof GetResult)) {
                GetResult gr = (GetResult)objectRequest;
                if (gr == null){
                    throw new CstlServiceException("The operation GetResult is only requestable in XML",
                                                     OPERATION_NOT_SUPPORTED, "GetResult");
                }
                serviceDef = getVersionFromNumber(gr.getVersion());
                StringWriter sw = new StringWriter();
                marshaller.marshal(worker.getResult(gr), sw);

                return Response.ok(sw.toString(), "text/xml").build();

             }

             if (request.equalsIgnoreCase("RegisterSensor") || (objectRequest instanceof RegisterSensor)) {
                RegisterSensor rs = (RegisterSensor)objectRequest;
                if (rs == null){
                    throw new CstlServiceException("The operation RegisterSensor is only requestable in XML",
                                                  OPERATION_NOT_SUPPORTED, "RegisterSensor");
                }
                serviceDef = getVersionFromNumber(rs.getVersion());
                StringWriter sw = new StringWriter();
                marshaller.marshal(worker.registerSensor(rs), sw);

                return Response.ok(sw.toString(), "text/xml").build();

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
                if (gc.getVersion() != null)
                    serviceDef = getVersionFromNumber(gc.getVersion().toString());
                StringWriter sw = new StringWriter();
                marshaller.marshal(worker.getCapabilities(gc), sw);

                return Response.ok(sw.toString(), worker.getOutputFormat()).build();

             }

             throw new CstlServiceException("The operation " + request + " is not supported by the service",
                     INVALID_PARAMETER_VALUE, "request");


        } catch (CstlServiceException ex) {
            return processExceptionResponse(ex, marshaller, serviceDef);

        } finally {
            if (marshaller != null) {
                marshallerPool.release(marshaller);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Response processExceptionResponse(final CstlServiceException ex, final Marshaller marshaller,
                                                ServiceDef serviceDef) throws JAXBException {
        /* We don't print the stack trace:
         * - if the user have forget a mandatory parameter.
         * - if the version number is wrong.
         */
        if (!ex.getExceptionCode().equals(MISSING_PARAMETER_VALUE) &&
                !ex.getExceptionCode().equals(VERSION_NEGOTIATION_FAILED) &&
                !ex.getExceptionCode().equals(INVALID_PARAMETER_VALUE) &&
                !ex.getExceptionCode().equals(OPERATION_NOT_SUPPORTED)) {
            ex.printStackTrace();
        } else {
            LOGGER.info("SENDING EXCEPTION: " + ex.getExceptionCode().name() + " " + ex.getMessage() + '\n');
        }

        if (workingContext) {
            if (serviceDef == null) {
                serviceDef = getBestVersion(null);
            }
            StringWriter sw = new StringWriter();
            ExceptionReport report = new ExceptionReport(ex.getMessage(), ex.getExceptionCode().name(), ex.getLocator(),
                                                         serviceDef.exceptionVersion.toString());
            marshaller.marshal(report, sw);
            return Response.ok(Util.cleanSpecialCharacter(sw.toString()), "text/xml").build();
        } else {
            return Response.ok("The SOS server is not running cause: unable to create JAXB context!", "text/plain").build();
        }
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
                    throw new CstlServiceException("The section " + token + " does not exist",
                                                  INVALID_PARAMETER_VALUE, "Sections");
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
        LOGGER.info("Destroying SOS service");
        if (worker != null) {
            worker.destroy();
        }
    }
}
