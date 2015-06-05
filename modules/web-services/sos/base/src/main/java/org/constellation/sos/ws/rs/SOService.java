/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.sos.ws.rs;

import org.constellation.ServiceDef;
import org.constellation.ServiceDef.Specification;
import org.constellation.sos.configuration.SOSConfigurer;
import org.constellation.sos.ws.SOSworker;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.MimeType;
import org.constellation.ws.ServiceConfigurer;
import org.constellation.ws.UnauthorizedException;
import org.constellation.ws.Worker;
import org.constellation.ws.rs.OGCWebService;
import org.geotoolkit.ows.xml.AcceptFormats;
import org.geotoolkit.ows.xml.AcceptVersions;
import org.geotoolkit.ows.xml.OWSXmlFactory;
import org.geotoolkit.ows.xml.RequestBase;
import org.geotoolkit.ows.xml.Sections;
import org.geotoolkit.ows.xml.v110.ExceptionReport;
import org.geotoolkit.ows.xml.v110.SectionsType;
import org.geotoolkit.sos.xml.GetCapabilities;
import org.geotoolkit.sos.xml.GetFeatureOfInterest;
import org.geotoolkit.sos.xml.GetObservation;
import org.geotoolkit.sos.xml.GetObservationById;
import org.geotoolkit.sos.xml.GetResult;
import org.geotoolkit.sos.xml.GetResultTemplate;
import org.geotoolkit.sos.xml.InsertObservation;
import org.geotoolkit.sos.xml.InsertResult;
import org.geotoolkit.sos.xml.InsertResultTemplate;
import org.geotoolkit.sos.xml.ResponseModeType;
import org.geotoolkit.sos.xml.SOSMarshallerPool;
import org.geotoolkit.sos.xml.SOSResponseWrapper;
import org.geotoolkit.sos.xml.v100.GetFeatureOfInterestTime;
import org.geotoolkit.swes.xml.DeleteSensor;
import org.geotoolkit.swes.xml.DescribeSensor;
import org.geotoolkit.swes.xml.InsertSensor;
import org.geotoolkit.util.StringUtilities;
import org.opengis.filter.Filter;
import org.opengis.filter.spatial.BBOX;
import org.opengis.observation.ObservationCollection;
import org.opengis.temporal.Instant;
import org.opengis.temporal.Period;

import javax.inject.Singleton;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;

import static org.constellation.api.QueryConstants.ACCEPT_FORMATS_PARAMETER;
import static org.constellation.api.QueryConstants.ACCEPT_VERSIONS_PARAMETER;
import static org.constellation.api.QueryConstants.REQUEST_PARAMETER;
import static org.constellation.api.QueryConstants.SECTIONS_PARAMETER;
import static org.constellation.api.QueryConstants.SERVICE_PARAMETER;
import static org.constellation.api.QueryConstants.UPDATESEQUENCE_PARAMETER;
import static org.constellation.api.QueryConstants.VERSION_PARAMETER;
import static org.constellation.sos.ws.SOSConstants.ACCEPTED_OUTPUT_FORMATS;
import static org.constellation.sos.ws.SOSConstants.FEATURE_OF_INTEREST;
import static org.constellation.sos.ws.SOSConstants.OBSERVATION;
import static org.constellation.sos.ws.SOSConstants.OBSERVATION_ID;
import static org.constellation.sos.ws.SOSConstants.OBSERVED_PROPERTY;
import static org.constellation.sos.ws.SOSConstants.OFFERING;
import static org.constellation.sos.ws.SOSConstants.OM_NAMESPACE;
import static org.constellation.sos.ws.SOSConstants.OUTPUT_FORMAT;
import static org.constellation.sos.ws.SOSConstants.PROCEDURE;
import static org.constellation.sos.ws.SOSConstants.PROCEDURE_DESCRIPTION_FORMAT;
import static org.constellation.sos.ws.SOSConstants.RESPONSE_FORMAT;
import static org.constellation.sos.ws.SOSConstants.RESPONSE_MODE;
import static org.constellation.sos.ws.SOSConstants.RESULT_MODEL;
import static org.constellation.sos.ws.SOSConstants.SRS_NAME;
import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_PARAMETER_VALUE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.MISSING_PARAMETER_VALUE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.OPERATION_NOT_SUPPORTED;
import static org.geotoolkit.sos.xml.SOSXmlFactory.buildAcceptVersion;
import static org.geotoolkit.sos.xml.SOSXmlFactory.buildBBOX;
import static org.geotoolkit.sos.xml.SOSXmlFactory.buildDeleteSensor;
import static org.geotoolkit.sos.xml.SOSXmlFactory.buildDescribeSensor;
import static org.geotoolkit.sos.xml.SOSXmlFactory.buildGetCapabilities;
import static org.geotoolkit.sos.xml.SOSXmlFactory.buildGetFeatureOfInterest;
import static org.geotoolkit.sos.xml.SOSXmlFactory.buildGetObservation;
import static org.geotoolkit.sos.xml.SOSXmlFactory.buildGetObservationById;
import static org.geotoolkit.sos.xml.SOSXmlFactory.buildGetResult;
import static org.geotoolkit.sos.xml.SOSXmlFactory.buildGetResultTemplate;
import static org.geotoolkit.sos.xml.SOSXmlFactory.buildTimeDuring;
import static org.geotoolkit.sos.xml.SOSXmlFactory.buildTimeEquals;
import static org.geotoolkit.sos.xml.SOSXmlFactory.buildTimeInstant;
import static org.geotoolkit.sos.xml.SOSXmlFactory.buildTimePeriod;

// Jersey dependencies
//JAXB dependencies
// Constellation dependencies
// Geotoolkit dependencies

/**
 *
 * @author Guilhem Legal
 * @author Benjamin Garcia (Geomatys)
 *
 * @version 0.9
 */
@Path("sos/{serviceId}")
@Singleton
public class SOService extends OGCWebService<SOSworker> {

    /**
     * Build a new Restfull SOS service.
     */
    public SOService() throws CstlServiceException {
        super(Specification.SOS);
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
    protected Class<? extends ServiceConfigurer> getConfigurerClass() {
        return SOSConfigurer.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response treatIncomingRequest(final Object objectRequest, final SOSworker worker) {
        ServiceDef serviceDef = null;
        try {
            final RequestBase request;
            if (objectRequest == null) {
                request = adaptQuery(getParameter(REQUEST_PARAMETER, true), worker);
            } else if (objectRequest instanceof RequestBase) {
                request = (RequestBase) objectRequest;
            } else {
                throw new CstlServiceException("The operation " + objectRequest.getClass().getName() + " is not supported by the service",
                        INVALID_PARAMETER_VALUE, "request");
            }

            serviceDef = worker.getVersionFromNumber(request.getVersion());
            final String currentVersion;
            if (request.getVersion() != null) {
                currentVersion = request.getVersion().toString();
            } else {
                currentVersion = null;
            }

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
                final Object marshalled;
                if (response instanceof ObservationCollection) {
                    marshalled = new SOSResponseWrapper(response, currentVersion);
                } else {
                    marshalled = response;
                 }
                return Response.ok(marshalled, outputFormat).build();
             }
             
             if (request instanceof GetObservationById) {
                final GetObservationById ds   = (GetObservationById)request;
                return Response.ok(worker.getObservationById(ds), MimeType.TEXT_XML).build();
             }

             if (request instanceof DescribeSensor) {
                final DescribeSensor ds       = (DescribeSensor)request;
                return Response.ok(worker.describeSensor(ds), MimeType.TEXT_XML).build();
             }

             if (request instanceof GetFeatureOfInterest) {
                final GetFeatureOfInterest gf     = (GetFeatureOfInterest)request;
                final SOSResponseWrapper response = new SOSResponseWrapper(worker.getFeatureOfInterest(gf), currentVersion);
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
             
             if (request instanceof DeleteSensor) {
                final DeleteSensor rs = (DeleteSensor)request;
                return Response.ok(worker.deleteSensor(rs), MimeType.TEXT_XML).build();
             }
             
             if (request instanceof InsertResult) {
                final InsertResult rs = (InsertResult)request;
                return Response.ok(worker.insertResult(rs), MimeType.TEXT_XML).build();
             }
             
             if (request instanceof InsertResultTemplate) {
                final InsertResultTemplate rs = (InsertResultTemplate)request;
                return Response.ok(worker.insertResultTemplate(rs), MimeType.TEXT_XML).build();
             }
             
             if (request instanceof GetResultTemplate) {
                final GetResultTemplate rs = (GetResultTemplate)request;
                return Response.ok(worker.getResultTemplate(rs), MimeType.TEXT_XML).build();
             }

             if (request instanceof GetFeatureOfInterestTime) {
                final GetFeatureOfInterestTime gft = (GetFeatureOfInterestTime)request;
                final SOSResponseWrapper response = new SOSResponseWrapper(worker.getFeatureOfInterestTime(gft), currentVersion);
                return Response.ok(response, MimeType.TEXT_XML).build();
             }

             if (request instanceof GetCapabilities) {
                final GetCapabilities gc = (GetCapabilities)request;
                return Response.ok(worker.getCapabilities(gc), getCapabilitiesOutputFormat(gc)).build();
             }

             throw new CstlServiceException("The operation " + request + " is not supported by the service",
                     INVALID_PARAMETER_VALUE, "request");


        } catch (CstlServiceException ex) {
            return processExceptionResponse(ex, serviceDef, worker);

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
    protected Response processExceptionResponse(final CstlServiceException ex, ServiceDef serviceDef, final Worker w) {
         // asking for authentication
        if (ex instanceof UnauthorizedException) {
            return Response.status(Response.Status.UNAUTHORIZED).header("WWW-Authenticate", " Basic").build();
        }
        logException(ex);

        final String exceptionVersion;
        if (serviceDef == null) {
            if  (w != null) {
                exceptionVersion = w.getBestVersion(null).exceptionVersion.toString();
            } else {
                exceptionVersion = null;
            }
        } else {
            exceptionVersion = serviceDef.exceptionVersion.toString();
        }
        final String exceptionCode   = getOWSExceptionCodeRepresentation(ex.getExceptionCode());
        final ExceptionReport report = new ExceptionReport(ex.getMessage(), exceptionCode, ex.getLocator(), exceptionVersion);
        return Response.ok(report, MimeType.TEXT_XML).build();
    }

    /**
     * Build request object from KVP parameters.
     *
     * @param request
     * @return
     * @throws CstlServiceException
     */
    private RequestBase adaptQuery(String request, final Worker w) throws CstlServiceException {
         if ("InsertObservation" .equalsIgnoreCase(request) ||
             "RegisterSensor"    .equalsIgnoreCase(request)
         ){
             throwUnsupportedGetMethod(request);

         } else if ("GetFeatureOfInterest".equalsIgnoreCase(request)) {
             return createGetFeatureOfInterest(w);
         } else if ("GetObservation".equalsIgnoreCase(request)) {
             return createGetObservation(w);
         } else if ("GetResult".equalsIgnoreCase(request)) {
             return createGetResult(w);
         } else if ("DescribeSensor".equalsIgnoreCase(request)) {
             return createDescribeSensor(w);
         } else if ("DeleteSensor".equalsIgnoreCase(request)) {
             return createDeleteSensor(w);
         } else if ("GetResultTemplate".equalsIgnoreCase(request)) {
             return createGetResultTemplate(w);
         } else if ("GetObservationById".equalsIgnoreCase(request)) {
             return createGetObservationById(w);
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
        worker.checkVersionSupported(currentVersion, true);

        final List<String> versions = new ArrayList<>();
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
        List<String> requestedSections = new ArrayList<>();
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
    private DescribeSensor createDescribeSensor(final Worker worker) throws CstlServiceException {
        final String service = getParameter(SERVICE_PARAMETER, true);
        if (service.isEmpty()) {
            throw new CstlServiceException("The parameter service must be specified", MISSING_PARAMETER_VALUE, "service");
        } else if (!"SOS".equals(service)) {
            throw new CstlServiceException("The parameter service value must be \"SOS\"", INVALID_PARAMETER_VALUE, "service");
        }
        final String currentVersion = getParameter(VERSION_PARAMETER, true);
        if (currentVersion.isEmpty()) {
            throw new CstlServiceException("The parameter version must be specified", MISSING_PARAMETER_VALUE, "version");
        }
        worker.checkVersionSupported(currentVersion, false);
        final String procedure = getParameter(PROCEDURE, true);
        if (procedure.isEmpty()) {
            throw new CstlServiceException("The parameter procedure must be specified", MISSING_PARAMETER_VALUE, "procedure");
        }
        final String varName;
        if (currentVersion.equals("1.0.0")) {
            varName = OUTPUT_FORMAT;
        } else {
            varName = PROCEDURE_DESCRIPTION_FORMAT;
        }
        final String outputFormat = getParameter(varName, true); 
        if (outputFormat.isEmpty()) {
            throw new CstlServiceException("The parameter " + varName + " must be specified", MISSING_PARAMETER_VALUE, varName);
        }
        return buildDescribeSensor(currentVersion,
                                   service,
                                   procedure,
                                   outputFormat);
    }

    private GetFeatureOfInterest createGetFeatureOfInterest(final Worker worker) throws CstlServiceException {
        final String service = getParameter(SERVICE_PARAMETER, true);
        if (service.isEmpty()) {
            throw new CstlServiceException("The parameter service must be specified", MISSING_PARAMETER_VALUE, "service");
        } else if (!"SOS".equals(service)) {
            throw new CstlServiceException("The parameter service value must be \"SOS\"", INVALID_PARAMETER_VALUE, "service");
        }
        final String currentVersion = getParameter(VERSION_PARAMETER, true);
        if (currentVersion.isEmpty()) {
            throw new CstlServiceException("The parameter version must be specified", MISSING_PARAMETER_VALUE, "version");
        }
        worker.checkVersionSupported(currentVersion, false);
        if (currentVersion.equals("1.0.0")) {
            final String featureID = getParameter("FeatureOfInterestId", true);
            final List<String> fidList = StringUtilities.toStringList(featureID);
            return buildGetFeatureOfInterest(getParameter(VERSION_PARAMETER, true),getParameter(SERVICE_PARAMETER, true), fidList);
        } else {
            final String obpList = getParameter(OBSERVED_PROPERTY, false);
            final List<String> observedProperties;
            if (obpList != null) {
                observedProperties = StringUtilities.toStringList(obpList);
            } else {
                observedProperties = new ArrayList<>();
            }
            final String prList = getParameter(PROCEDURE, false);
            final List<String> procedures;
            if (prList != null) {
                procedures = StringUtilities.toStringList(prList);
            } else {
                procedures = new ArrayList<>();
            }
            final String foList = getParameter(FEATURE_OF_INTEREST, false);
            final List<String> foids;
            if (foList != null) {
                foids = StringUtilities.toStringList(foList);
            } else {
                foids = new ArrayList<>();
            }
            final String bboxStr = getParameter("spatialFilter", false);
            final Filter spatialFilter;
            if (bboxStr != null) {
                spatialFilter = parseBBoxFilter(bboxStr);
            } else {
                spatialFilter = null;
            }
            return buildGetFeatureOfInterest(currentVersion, service, foids, observedProperties, procedures, spatialFilter);
        }
    }
    
    private DeleteSensor createDeleteSensor(final Worker worker) throws CstlServiceException {
        final String service = getParameter(SERVICE_PARAMETER, true);
        if (service.isEmpty()) {
            throw new CstlServiceException("The parameter service must be specified", MISSING_PARAMETER_VALUE, "service");
        } else if (!"SOS".equals(service)) {
            throw new CstlServiceException("The parameter service value must be \"SOS\"", INVALID_PARAMETER_VALUE, "service");
        }
        final String currentVersion = getParameter(VERSION_PARAMETER, true);
        if (currentVersion.isEmpty()) {
            throw new CstlServiceException("The parameter version must be specified", MISSING_PARAMETER_VALUE, "version");
        }
        worker.checkVersionSupported(currentVersion, false);
        final String procedure = getParameter(PROCEDURE, true);
        if (procedure.isEmpty()) {
            throw new CstlServiceException("The parameter procedure must be specified", MISSING_PARAMETER_VALUE, "procedure");
        }
        return buildDeleteSensor(currentVersion,
                                 service,
                                 procedure);
    }
    
    private GetResult createGetResult(final Worker worker) throws CstlServiceException {
        final String service = getParameter(SERVICE_PARAMETER, true);
        if (service.isEmpty()) {
            throw new CstlServiceException("The parameter service must be specified", MISSING_PARAMETER_VALUE, "service");
        } else if (!"SOS".equals(service)) {
            throw new CstlServiceException("The parameter service value must be \"SOS\"", INVALID_PARAMETER_VALUE, "service");
        }
        final String currentVersion = getParameter(VERSION_PARAMETER, true);
        if (currentVersion.isEmpty()) {
            throw new CstlServiceException("The parameter version must be specified", MISSING_PARAMETER_VALUE, "version");
        }
        worker.checkVersionSupported(currentVersion, false);
        if (currentVersion.equals("2.0.0")) {
            final String offering = getParameter(OFFERING, true);
            if (offering.isEmpty()) {
                throw new CstlServiceException("The parameter offering must be specified", MISSING_PARAMETER_VALUE, OFFERING);
            }
            final String observedProperty = getParameter(OBSERVED_PROPERTY, true);
            if (observedProperty.isEmpty()) {
                throw new CstlServiceException("The parameter observedProperty must be specified", MISSING_PARAMETER_VALUE, OBSERVED_PROPERTY);
            }
            final String foList = getParameter(FEATURE_OF_INTEREST, false);
            final List<String> foids;
            if (foList != null) {
                foids = StringUtilities.toStringList(foList);
            } else {
                foids = new ArrayList<>();
            }
            final String bboxStr = getParameter("spatialFilter", false);
            final Filter spatialFilter;
            if (bboxStr != null) {
                spatialFilter = parseBBoxFilter(bboxStr);
            } else {
                spatialFilter = null;
            }
            final String tempStr = getParameter("temporalFilter", false);
            final List<Filter> temporalFilters = new ArrayList<>();
            if (tempStr != null) {
                temporalFilters.add(parseTemporalFilter(tempStr));
            }
            return buildGetResult(currentVersion, service, offering, observedProperty, foids, spatialFilter, temporalFilters);
        } else {
            throwUnsupportedGetMethod("GetResult");
            return null;
        }
    }
    
    private GetResultTemplate createGetResultTemplate(final Worker worker) throws CstlServiceException {
        final String service = getParameter(SERVICE_PARAMETER, true);
        if (service.isEmpty()) {
            throw new CstlServiceException("The parameter service must be specified", MISSING_PARAMETER_VALUE, "service");
        } else if (!"SOS".equals(service)) {
            throw new CstlServiceException("The parameter service value must be \"SOS\"", INVALID_PARAMETER_VALUE, "service");
        }
        final String currentVersion = getParameter(VERSION_PARAMETER, true);
        if (currentVersion.isEmpty()) {
            throw new CstlServiceException("The parameter version must be specified", MISSING_PARAMETER_VALUE, "version");
        }
        worker.checkVersionSupported(currentVersion, false);
        final String offering = getParameter(OFFERING, true);
        if (offering.isEmpty()) {
            throw new CstlServiceException("The parameter offering must be specified", MISSING_PARAMETER_VALUE, OFFERING);
        }
        final String observedProperty = getParameter(OBSERVED_PROPERTY, true);
        if (observedProperty.isEmpty()) {
            throw new CstlServiceException("The parameter observedProperty must be specified", MISSING_PARAMETER_VALUE, OBSERVED_PROPERTY);
        }
        return buildGetResultTemplate(currentVersion, service, offering, observedProperty);
    }
    
    private GetObservationById createGetObservationById(final Worker worker) throws CstlServiceException {
        final String service = getParameter(SERVICE_PARAMETER, true);
        if (service.isEmpty()) {
            throw new CstlServiceException("The parameter service must be specified", MISSING_PARAMETER_VALUE, "service");
        } else if (!"SOS".equals(service)) {
            throw new CstlServiceException("The parameter service value must be \"SOS\"", INVALID_PARAMETER_VALUE, "service");
        }
        final String currentVersion = getParameter(VERSION_PARAMETER, true);
        if (currentVersion.isEmpty()) {
            throw new CstlServiceException("The parameter version must be specified", MISSING_PARAMETER_VALUE, "version");
        }
        worker.checkVersionSupported(currentVersion, false);
        final List<String> observations;
        final String srsName;
        final QName resultModel;
        final ResponseModeType responseMode;
        final String responseFormat;
        if (currentVersion.equals("2.0.0")) {
            final String observationList = getParameter(OBSERVATION, true);
            if (observationList.isEmpty()) {
                throw new CstlServiceException("The parameter observation must be specified", MISSING_PARAMETER_VALUE, OBSERVATION);
            } else {
                observations = StringUtilities.toStringList(observationList);
            }
            srsName        = null;
            resultModel    = null;
            responseMode   = null;
            responseFormat = null;
        } else {
            final String observationList = getParameter(OBSERVATION_ID, true);
            if (observationList.isEmpty()) {
                throw new CstlServiceException("The parameter observationID must be specified", MISSING_PARAMETER_VALUE, OBSERVATION_ID);
            } else {
                observations = StringUtilities.toStringList(observationList);
            }
            srsName = getParameter(SRS_NAME, false);
            responseFormat = getParameter(RESPONSE_FORMAT, true);
            final String rm = getParameter(RESULT_MODEL, false);
            if (rm != null && rm.indexOf(':') != -1) {
                resultModel = new QName(OM_NAMESPACE, rm.substring(rm.indexOf(':')));
            } else if (rm != null){
                resultModel = new QName(rm);
            } else {
                resultModel = null;
            }
            final String rmd = getParameter(RESPONSE_MODE, false);
            if (rmd != null) {
                responseMode = ResponseModeType.fromValue(rm);
            } else {
                responseMode = null;
            }
        }
        return buildGetObservationById(currentVersion, service, observations, resultModel, responseMode, srsName, responseFormat);
    }
    
    private GetObservation createGetObservation(final Worker worker) throws CstlServiceException {
        final String service = getParameter(SERVICE_PARAMETER, true);
        if (service.isEmpty()) {
            throw new CstlServiceException("The parameter service must be specified", MISSING_PARAMETER_VALUE, "service");
        } else if (!"SOS".equals(service)) {
            throw new CstlServiceException("The parameter service value must be \"SOS\"", INVALID_PARAMETER_VALUE, "service");
        }
        final String currentVersion = getParameter(VERSION_PARAMETER, true);
        if (currentVersion.isEmpty()) {
            throw new CstlServiceException("The parameter version must be specified", MISSING_PARAMETER_VALUE, "version");
        }
        worker.checkVersionSupported(currentVersion, false);
        
        if (currentVersion.equals("2.0.0")) {
            final String offList = getParameter(OFFERING, false);
            final List<String> offering;
            if (offList != null) {
                offering = StringUtilities.toStringList(offList);
            } else {
                offering = new ArrayList<>();
            }
            final String obpList = getParameter(OBSERVED_PROPERTY, false);
            final List<String> observedProperties;
            if (obpList != null) {
                observedProperties = StringUtilities.toStringList(obpList);
            } else {
                observedProperties = new ArrayList<>();
            }
            final String prList = getParameter(PROCEDURE, false);
            final List<String> procedures;
            if (prList != null) {
                procedures = StringUtilities.toStringList(prList);
            } else {
                procedures = new ArrayList<>();
            }
            final String foList = getParameter(FEATURE_OF_INTEREST, false);
            final List<String> foids;
            if (foList != null) {
                foids = StringUtilities.toStringList(foList);
            } else {
                foids = new ArrayList<>();
            }
            final String responseFormat = getParameter(RESPONSE_FORMAT, false);
            final String bboxStr = getParameter("spatialFilter", false);
            final Filter spatialFilter;
            if (bboxStr != null) {
                spatialFilter = parseBBoxFilter(bboxStr);
            } else {
                spatialFilter = null;
            }
            final String tempStr = getParameter("temporalFilter", false);
            final List<Filter> temporalFilters = new ArrayList<>();
            if (tempStr != null) {
                temporalFilters.add(parseTemporalFilter(tempStr));
            }
            return buildGetObservation(currentVersion, service, offering, observedProperties, procedures, foids, responseFormat, temporalFilters, spatialFilter, null, null, null, null);
        } else {
            throw new CstlServiceException("The operation GetObservation is only requestable in XML via POST method for 1.0.0 version",
                                                  OPERATION_NOT_SUPPORTED, "GetObservation");
        }
    }
    
    private BBOX parseBBoxFilter(final String bboxStr) {
        final String[] part = bboxStr.split(",");
        final String valueReference = part[0];
        final double[] coord = new double[4];
        int j = 0;
        for (int i=1; i < 5; i++) {
            coord[j] = Double.parseDouble(part[i]);
            j++;
        }
        final String srsName;
        if (part.length > 5) {
            srsName = part[5];
        } else {
            srsName = "urn:ogc:def:crs:EPSG::4326";
        }
        return buildBBOX("2.0.0", valueReference, coord[0], coord[1], coord[2], coord[3], srsName);
    }
    
    private Filter parseTemporalFilter(final String tempStr) {
        final String[] part = tempStr.split(",");
        final String valueReference = part[0];
        final int slash = part[1].indexOf('/');
        if (slash != -1) {
            final String dateBegin = part[1].substring(0, slash);
            final String dateEnd   = part[1].substring(slash + 1);
            final Period period    = buildTimePeriod("2.0.0", null, dateBegin, dateEnd);
            return buildTimeDuring("2.0.0", valueReference, period);
        } else {
            final Instant instant = buildTimeInstant("2.0.0", null, part[1]);
            return buildTimeEquals("2.0.0", valueReference, instant);
        }
    }
}
