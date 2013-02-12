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
import org.geotoolkit.util.StringUtilities;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Marshaller;
import java.util.logging.Level;
import java.io.File;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import com.sun.jersey.spi.resource.Singleton;

//JAXB dependencies
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import javax.xml.bind.JAXBException;

// Constellation dependencies
import org.constellation.ServiceDef;
import org.constellation.configuration.DataSourceType;
import org.constellation.configuration.SOSConfiguration;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.sos.ws.SOSworker;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.rs.OGCWebService;
import org.constellation.ws.MimeType;
import org.constellation.ws.UnauthorizedException;
import org.constellation.ws.Worker;

import static org.constellation.api.QueryConstants.*;
import static org.constellation.sos.ws.SOSConstants.*;

// Geotoolkit dependencies
import org.geotoolkit.ows.xml.RequestBase;
import org.geotoolkit.ows.xml.v110.ExceptionReport;
import org.geotoolkit.ows.xml.v110.SectionsType;
import org.geotoolkit.ows.xml.AcceptFormats;
import org.geotoolkit.ows.xml.AcceptVersions;
import org.geotoolkit.ows.xml.Sections;
import org.geotoolkit.ows.xml.OWSXmlFactory;
import org.geotoolkit.swes.xml.DescribeSensor;
import org.geotoolkit.swes.xml.InsertSensor;
import org.geotoolkit.sos.xml.GetCapabilities;
import org.geotoolkit.sos.xml.GetObservation;
import org.geotoolkit.sos.xml.GetResult;
import org.geotoolkit.sos.xml.InsertObservation;
import org.geotoolkit.sos.xml.SOSMarshallerPool;
import org.geotoolkit.sos.xml.SOSResponseWrapper;
import org.geotoolkit.sos.xml.GetFeatureOfInterest;
import org.geotoolkit.sos.xml.v100.GetFeatureOfInterestTime;
import org.geotoolkit.sml.xml.AbstractSensorML;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import static org.geotoolkit.sos.xml.SOSXmlFactory.*;

import org.opengis.observation.ObservationCollection;

/**
 *
 * @author Guilhem Legal
 */
@Path("sos/{serviceId}")
@Singleton
public class SOService extends OGCWebService<SOSworker> {

    /**
     * Build a new Restfull SOS service.
     */
    public SOService() throws CstlServiceException {
        super(ServiceDef.SOS_1_0_0, ServiceDef.SOS_2_0_0);
        setXMLContext(SOSMarshallerPool.getInstance());
        LOGGER.log(Level.INFO, "SOS REST service running ({0} instances)\n", getWorkerMapSize());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class getWorkerClass() {
        return SOSworker.class;
    }



    /**
     * {@inheritDoc}
     */
    @Override
    public Response treatIncomingRequest(final Object objectRequest, final SOSworker worker) {
        ServiceDef serviceDef = null;
        try {
            worker.setServiceUrl(getServiceURL());
            logParameters();

            final RequestBase request;
            if (objectRequest == null) {
                request = adaptQuery(getParameter(REQUEST_PARAMETER, true), worker);
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
                if (outputFormat != null) {
                    if (outputFormat.startsWith(MimeType.TEXT_XML)) {
                        outputFormat = MimeType.TEXT_XML;
                    } else if (outputFormat.startsWith("text/")) {
                        outputFormat = MimeType.TEXT_PLAIN;
                    }
                }
                Object marshalled;
                if (response instanceof ObservationCollection) {
                    marshalled = new SOSResponseWrapper(response);
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
                return Response.ok(response, MimeType.TEXT_XML).build();
             }

             if (request instanceof InsertObservation) {
                final InsertObservation is = (InsertObservation)request;
                return Response.ok(worker.insertObservation(is), MimeType.TEXT_XML).build();
             }

             if (request instanceof GetResult) {
                final GetResult gr = (GetResult)request;
                return Response.ok(worker.getResult(gr), MimeType.TEXT_XML).build();
             }

             if (request instanceof InsertSensor) {
                final InsertSensor rs = (InsertSensor)request;
                return Response.ok(worker.registerSensor(rs), MimeType.TEXT_XML).build();
             }

             if (request instanceof GetFeatureOfInterestTime) {
                final GetFeatureOfInterestTime gft = (GetFeatureOfInterestTime)request;
                final SOSResponseWrapper response = new SOSResponseWrapper(worker.getFeatureOfInterestTime(gft));
                return Response.ok(response, MimeType.TEXT_XML).build();
             }

             if (request instanceof GetCapabilities) {
                final GetCapabilities gc = (GetCapabilities)request;
                return Response.ok(worker.getCapabilities(gc), getCapabilitiesOutputFormat(gc)).build();
             }

             throw new CstlServiceException("The operation " + request + " is not supported by the service",
                     INVALID_PARAMETER_VALUE, "request");


        } catch (CstlServiceException ex) {
            return processExceptionResponse(ex, serviceDef);

        }
    }

    /**
     * Throw an CstlServiceException when a request is not available in GET.
     *
     * @param operationName The name of the request. (example getCapabilities)
     *
     * @throws CstlServiceException every time.
     */
    private void throwUnsupportedGetMethod(String operationName) throws CstlServiceException {
        throw new CstlServiceException("The operation " + operationName + " is only requestable in XML via POST method",
                                                  OPERATION_NOT_SUPPORTED, operationName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Response processExceptionResponse(final CstlServiceException ex, ServiceDef serviceDef) {
         // asking for authentication
        if (ex instanceof UnauthorizedException) {
            return Response.status(Response.Status.UNAUTHORIZED).header("WWW-Authenticate", " Basic").build();
        }
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
     * {@inheritDoc}
     */
    @Override
    protected void configureInstance(final File instanceDirectory, final Object configuration) throws CstlServiceException {
        if (configuration instanceof SOSConfiguration) {
            final File configurationFile = new File(instanceDirectory, "config.xml");
            Marshaller marshaller = null;
            try {
                marshaller = GenericDatabaseMarshallerPool.getInstance().acquireMarshaller();
                marshaller.marshal(configuration, configurationFile);

            } catch(JAXBException ex) {
                throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
            } finally {
                if (marshaller != null) {
                    GenericDatabaseMarshallerPool.getInstance().release(marshaller);
                }
            }
        } else {
            throw new CstlServiceException("The configuration Object is not a SOSConfiguration", INVALID_PARAMETER_VALUE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void basicConfigure(final File instanceDirectory) throws CstlServiceException {
        final SOSConfiguration baseConfig = new SOSConfiguration(new Automatic(null, new BDD()), new Automatic(null, new BDD()));
        baseConfig.setObservationReaderType(DataSourceType.FILESYSTEM);
        baseConfig.setObservationFilterType(DataSourceType.LUCENE);
        baseConfig.setObservationWriterType(DataSourceType.FILESYSTEM);
        baseConfig.setSMLType(DataSourceType.FILESYSTEM);
        configureInstance(instanceDirectory, baseConfig);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object getInstanceConfiguration(File instanceDirectory) throws CstlServiceException {
        final File configurationFile = new File(instanceDirectory, "config.xml");
        if (configurationFile.exists()) {
            Unmarshaller unmarshaller = null;
            try {
                unmarshaller = GenericDatabaseMarshallerPool.getInstance().acquireUnmarshaller();
                Object obj = unmarshaller.unmarshal(configurationFile);
                if (obj instanceof SOSConfiguration) {
                    return obj;
                } else {
                    throw new CstlServiceException("The config.xml file does not contain a SOSConfiguration object");
                }
            } catch (JAXBException ex) {
                throw new CstlServiceException(ex);
            } finally {
                if (unmarshaller != null) {
                    GenericDatabaseMarshallerPool.getInstance().release(unmarshaller);
                }
            }
        } else {
            throw new CstlServiceException("Unable to find a file config.xml");
        }
    }

    /**
     * Build request object from KVP parameters.
     *
     * @param request
     * @return
     * @throws CstlServiceException
     */
    private RequestBase adaptQuery(String request, final Worker w) throws CstlServiceException {
         if ("GetObservation"    .equalsIgnoreCase(request) ||
             "InsertObservation" .equalsIgnoreCase(request) ||
             "GetResult"         .equalsIgnoreCase(request) ||
             "RegisterSensor"    .equalsIgnoreCase(request)
         ){
             throwUnsupportedGetMethod(request);

         } else if ("GetFeatureOfInterest".equalsIgnoreCase(request)) {
             return createGetFeatureOfInterest();
         } else if ("DescribeSensor".equalsIgnoreCase(request)) {
             return createDescribeSensor();
         } else if ("GetCapabilities".equalsIgnoreCase(request)) {
             return createNewGetCapabilities(w);
         }
         throw new CstlServiceException("The operation " + request + " is not supported by the service",
                        INVALID_PARAMETER_VALUE, "request");
    }

    /**
     * Build a new getCapabilities request from kvp encoding
     */
    private GetCapabilities createNewGetCapabilities(final Worker worker) throws CstlServiceException {

        String version        = getParameter(ACCEPT_VERSIONS_PARAMETER, false);
        String currentVersion = getParameter(VERSION_PARAMETER, false);
        
        if (currentVersion == null) {
            currentVersion = worker.getBestVersion(null).version.toString();
        }
        isVersionSupported(currentVersion);

        final List<String> versions = new ArrayList<String>();
        if (version != null) {
            String[] vArray = version.split(",");
            versions.addAll(Arrays.asList(vArray));
        } else {
            versions.add(currentVersion);
        }
        final AcceptVersions acceptVersions = buildAcceptVersion(currentVersion, versions);
        
        final String format = getParameter(ACCEPT_FORMATS_PARAMETER, false);
        final AcceptFormats formats;
        if (format != null) {
            formats = OWSXmlFactory.buildAcceptFormat("1.1.0", Arrays.asList(format));
        } else {
            formats = null;
        }

        final String updateSequence = getParameter(UPDATESEQUENCE_PARAMETER, false);

        //We transform the String of sections in a list.
        //In the same time we verify that the requested sections are valid.
        final String section = getParameter(SECTIONS_PARAMETER, false);
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
        final Sections sections = OWSXmlFactory.buildSections("1.1.0", requestedSections);
        return buildGetCapabilities(currentVersion,
                                   acceptVersions,
                                   sections,
                                   formats,
                                   updateSequence,
                                   getParameter(SERVICE_PARAMETER, true));

    }
    
    private String getCapabilitiesOutputFormat(final GetCapabilities request) {
        final AcceptFormats formats = request.getAcceptFormats();
        if (formats != null && formats.getOutputFormat().size() > 0 ) {
            for (String form: formats.getOutputFormat()) {
                if (ACCEPTED_OUTPUT_FORMATS.contains(form)) {
                    return form;
                }
            }
        }
        return MimeType.APPLICATION_XML;
    }

    /**
     * Build a new getCapabilities request from kvp encoding
     */
    private DescribeSensor createDescribeSensor() throws CstlServiceException {
        return buildDescribeSensor(getParameter(VERSION_PARAMETER, true),
                                  getParameter(SERVICE_PARAMETER, true),
                                  getParameter(PROCEDURE, true),
                                  getParameter(OUTPUT_FORMAT, true));


    }

    private GetFeatureOfInterest createGetFeatureOfInterest() throws CstlServiceException {
        final String featureID = getParameter("FeatureOfInterestId", true);
        final List<String> fidList = StringUtilities.toStringList(featureID);
        return buildGetFeatureOfInterest(getParameter(VERSION_PARAMETER, true),getParameter(SERVICE_PARAMETER, true), fidList);
    }
}
