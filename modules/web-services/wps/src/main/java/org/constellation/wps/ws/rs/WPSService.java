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
package org.constellation.wps.ws.rs;

import org.constellation.ServiceDef;
import org.constellation.ServiceDef.Specification;
import org.constellation.wps.configuration.WPSConfigurer;
import org.constellation.wps.ws.WPSWorker;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.MimeType;
import org.constellation.ws.ServiceConfigurer;
import org.constellation.ws.Worker;
import org.constellation.ws.rs.OGCWebService;
import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.ows.xml.RequestBase;
import org.geotoolkit.ows.xml.v110.AcceptVersionsType;
import org.geotoolkit.ows.xml.v110.CodeType;
import org.geotoolkit.ows.xml.v110.ExceptionReport;
import org.geotoolkit.wps.xml.WPSMarshallerPool;
import org.geotoolkit.wps.xml.v100.DataInputsType;
import org.geotoolkit.wps.xml.v100.DataType;
import org.geotoolkit.wps.xml.v100.DescribeProcess;
import org.geotoolkit.wps.xml.v100.DocumentOutputDefinitionType;
import org.geotoolkit.wps.xml.v100.Execute;
import org.geotoolkit.wps.xml.v100.GetCapabilities;
import org.geotoolkit.wps.xml.v100.InputReferenceType;
import org.geotoolkit.wps.xml.v100.InputType;
import org.geotoolkit.wps.xml.v100.OutputDefinitionType;
import org.geotoolkit.wps.xml.v100.ProcessDescriptions;
import org.geotoolkit.wps.xml.v100.ResponseDocumentType;
import org.geotoolkit.wps.xml.v100.ResponseFormType;
import org.geotoolkit.wps.xml.v100.WPSCapabilitiesType;

import javax.inject.Singleton;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import static org.constellation.api.QueryConstants.ACCEPT_VERSIONS_PARAMETER;
import static org.constellation.api.QueryConstants.REQUEST_PARAMETER;
import static org.constellation.api.QueryConstants.SERVICE_PARAMETER;
import static org.constellation.api.QueryConstants.UPDATESEQUENCE_PARAMETER;
import static org.constellation.api.QueryConstants.VERSION_PARAMETER;
import static org.constellation.wps.ws.WPSConstant.DESCRIBEPROCESS;
import static org.constellation.wps.ws.WPSConstant.EXECUTE;
import static org.constellation.wps.ws.WPSConstant.GETCAPABILITIES;
import static org.constellation.wps.ws.WPSConstant.IDENTIFER_PARAMETER;
import static org.constellation.wps.ws.WPSConstant.LANGUAGE_PARAMETER;
import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_FORMAT;
import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_REQUEST;
import static org.geotoolkit.ows.xml.OWSExceptionCode.MISSING_PARAMETER_VALUE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.OPERATION_NOT_SUPPORTED;

/**
 * WPS web service class.
 *
 * @author Quentin Boileau (Geomatys).
 * @author Benjamin Garcia (Geomatys).
 *
 * @version 0.9
 */
@Path("wps/{serviceId}")
@Singleton
public class WPSService extends OGCWebService<WPSWorker> {

    /**
     * Executor thread pool.
     */
    public static ExecutorService EXECUTOR;

    /**
     * Build a new instance of the webService and initialize the JAXB context.
     */
    public WPSService() {
        super(Specification.WPS);

        setFullRequestLog(true);
        //we build the JAXB marshaller and unmarshaller to bind java/xml
        setXMLContext(WPSMarshallerPool.getInstance());
        LOGGER.log(Level.INFO, "WPS REST service running ({0} instances)\n", getWorkerMapSize());
    }

    @Override
    protected Class getWorkerClass() {
        return WPSWorker.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<? extends ServiceConfigurer> getConfigurerClass() {
        return WPSConfigurer.class;
    }

    @Override
    public void destroy() {
        super.destroy();

        //Shutdown the WPS scheduler.
        LOGGER.log(Level.INFO, "Shutdown executor pool");
        if (EXECUTOR != null) {
            EXECUTOR.shutdown();
        }
    }

    public static synchronized ExecutorService getExecutor() {
        if (EXECUTOR == null) {
            EXECUTOR = Executors.newCachedThreadPool();
        }
        return EXECUTOR;
    }

    @Override
    protected Response treatIncomingRequest(final Object objectRequest, final WPSWorker worker) {
        final UriInfo uriContext = getUriContext();

        ServiceDef version = null;
        String requestName = null;
        try {
            // Handle an empty request by sending a basic web page.
            if ((null == objectRequest) && (0 == uriContext.getQueryParameters().size())) {
                return Response.ok(getIndexPage(), MimeType.TEXT_HTML).build();
            }

            // if the request is not an xml request we fill the request parameter.
            final RequestBase request;
            if (objectRequest == null) {
                //build objectRequest from parameters
                version = worker.getVersionFromNumber(getParameter(VERSION_PARAMETER, false)); // needed if exception is launch before request build
                requestName = getParameter(REQUEST_PARAMETER, true);
                request = adaptQuery(requestName, worker);
            } else if (objectRequest instanceof RequestBase) {
                request = (RequestBase) objectRequest;
            } else {
                throw new CstlServiceException("The operation " + objectRequest.getClass().getName() + " is not supported by the service",
                        OPERATION_NOT_SUPPORTED, objectRequest.getClass().getName());
            }

            version = worker.getVersionFromNumber(request.getVersion());

            /*
             * GetCapabilities request
             */
            if (request instanceof GetCapabilities) {
                final GetCapabilities getcaps = (GetCapabilities) request;
                final WPSCapabilitiesType capsResponse = worker.getCapabilities(getcaps);
                return Response.ok(capsResponse, MimeType.TEXT_XML).build();
            }

            /*
             * DescribeProcess request
             */
            if (request instanceof DescribeProcess) {
                final DescribeProcess descProc = (DescribeProcess) request;
                final ProcessDescriptions describeResponse = worker.describeProcess(descProc);
                return Response.ok(describeResponse, MimeType.TEXT_XML).build();
            }

            /*
             * Execute request
             */
            if (request instanceof Execute) {
                final Execute exec = (Execute) request;
                final Object executeResponse = worker.execute(exec);

                boolean isTextPlain = false;
                boolean isImage = false;
                //if response is a literal
                if (executeResponse instanceof String  || executeResponse instanceof Double
                 || executeResponse instanceof Float   || executeResponse instanceof Integer
                 || executeResponse instanceof Boolean || executeResponse instanceof Long) {
                    isTextPlain = true;
                }
                if (executeResponse instanceof RenderedImage || executeResponse instanceof BufferedImage
                        || executeResponse instanceof GridCoverage2D) {
                    isImage = true;
                }
                if (isTextPlain)  {
                    return Response.ok(executeResponse.toString(), MimeType.TEXT_PLAIN).build();
                } else if (isImage) {
                    return Response.ok(executeResponse.toString(), MimeType.IMAGE_PNG).build();
                } else {
                    return Response.ok(executeResponse, MimeType.TEXT_XML).build();
                }

            }

            throw new CstlServiceException("This service can not handle the requested operation: " + request.getClass().getName() + ".",
                    OPERATION_NOT_SUPPORTED, requestName);

        } catch (CstlServiceException ex) {
            /*
             * This block handles all the exceptions which have been generated anywhere in the service and transforms them to a response
             * message for the protocol stream which JAXB, in this case, will then marshall and serialize into an XML message HTTP response.
             */
            return processExceptionResponse(ex, version, worker);

        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Response processExceptionResponse(final CstlServiceException ex, ServiceDef serviceDef, final Worker worker) {
        logException(ex);

        // SEND THE HTTP RESPONSE
        if (serviceDef == null) {
            serviceDef = ServiceDef.WPS_1_0_0;
        }
        final String exceptionCode   = getOWSExceptionCodeRepresentation(ex.getExceptionCode());
        final ExceptionReport report = new ExceptionReport(ex.getMessage(), exceptionCode, ex.getLocator(),
                                                     serviceDef.exceptionVersion.toString());
        return Response.ok(report, MimeType.TEXT_XML).build();
    }

    /**
     * Handle GET request in KVP.
     *
     * @param request
     * @return GetCapabilities or DescribeProcess or Execute object.
     * @throws CstlServiceException if request is unknow.
     */
    public RequestBase adaptQuery(final String request, final Worker w) throws CstlServiceException {

        if (GETCAPABILITIES.equalsIgnoreCase(request)) {
            return adaptKvpGetCapabilitiesRequest();
        } else if (DESCRIBEPROCESS.equalsIgnoreCase(request)) {
            return adaptKvpDescribeProcessRequest(w);
        } else if (EXECUTE.equalsIgnoreCase(request)) {
            return adaptKvpExecuteRequest();
        }
        throw new CstlServiceException("The operation " + request + " is not supported by the service",
                OPERATION_NOT_SUPPORTED, request);
    }

    /**
     * Create GetCapabilities object from kvp parameters.
     *
     * @return GetCapabilities object.
     * @throws CstlServiceException
     */
    private GetCapabilities adaptKvpGetCapabilitiesRequest() throws CstlServiceException {

        final GetCapabilities capabilities = new GetCapabilities();
        capabilities.setService(getParameter(SERVICE_PARAMETER, true));
        capabilities.setLanguage(getParameter(LANGUAGE_PARAMETER, false));
        capabilities.setUpdateSequence(getParameter(UPDATESEQUENCE_PARAMETER, false));

        final String acceptVersionsParam = getParameter(ACCEPT_VERSIONS_PARAMETER, false);
        if(acceptVersionsParam!= null){
            final String[] acceptVersions = acceptVersionsParam.split(",");
            capabilities.setAcceptVersions(new AcceptVersionsType(acceptVersions));
        }
        return capabilities;
    }

    /**
     * Create DescribeProcess object from kvp parameters.
     *
     * @return DescribeProcess object.
     * @throws CstlServiceException if mandatory parameters are missing.
     */
    private DescribeProcess adaptKvpDescribeProcessRequest(final Worker w) throws CstlServiceException {

        final String strVersion = getParameter(VERSION_PARAMETER, true);
        w.checkVersionSupported(strVersion, false);

        final DescribeProcess describe = new DescribeProcess();
        describe.setService(getParameter(SERVICE_PARAMETER, true));
        describe.setVersion(strVersion);
        describe.setLanguage(getParameter(LANGUAGE_PARAMETER, false));

        final String allIdentifiers = getParameter(IDENTIFER_PARAMETER, true);
        if (allIdentifiers != null) {
            final String[] splitStr = allIdentifiers.split(",");

            final List<String> identifiers = Arrays.asList(splitStr);

            for (final String ident : identifiers) {
                describe.getIdentifier().add(new CodeType(ident));
            }
            return describe;
        } else {
            throw new CstlServiceException("The parameter " + IDENTIFER_PARAMETER + " must be specified.",
                    MISSING_PARAMETER_VALUE, IDENTIFER_PARAMETER.toLowerCase());
        }
    }

    /**
     * Create Execute object from kvp parameters.
     *
     * @return Execute object.
     * @throws CstlServiceException
     */
    private Execute adaptKvpExecuteRequest() throws CstlServiceException {

        throw new UnsupportedOperationException("Not yet implemented");

//        final String strVersion = getParameter(VERSION_PARAMETER, true);
//        isVersionSupported(strVersion);
//        final ServiceDef serviceDef = getVersionFromNumber(strVersion);
//
//        if (serviceDef.equals(ServiceDef.WPS_1_0_0)) {
//            final Execute exec = new Execute();
//
//            exec.setIdentifier(new CodeType(getParameter("Identifier", true)));
//            final String dataInputs = getParameter("DataInputs", true);
//            final String respDoc = getParameter("ResponseDocument", false);
//            final String respRawData = getParameter("RawDataOutput", false);
//
//            exec.setDataInputs(extractDataInput(dataInputs));
//            exec.setResponseForm(extractResponseDocument(respDoc, respRawData));
//
//            return exec;
//        } else {
//            throw new CstlServiceException("The version number specified for this request " +
//                    "is not handled.", VERSION_NEGOTIATION_FAILED, VERSION_PARAMETER.toLowerCase());
//        }

    }

    /**
     *
     * @param dataInputs
     * @return
     */
    private DataInputsType extractDataInput(final String dataInputs) throws CstlServiceException {

        final DataInputsType inputsData = new DataInputsType();

        //extract input data from dataInputs String
        Map<String, Map> inputMap = extractDataFromKvpString(dataInputs);

        final List<InputType> inputList = new ArrayList<>();

        //Each input
        for (Map.Entry<String, Map> oneInput : inputMap.entrySet()) {
            final InputType input = new InputType();
            //Input Identifier
            input.setIdentifier(new CodeType(oneInput.getKey()));

            final Map<String, String> attMap = (Map<String, String>) oneInput.getValue();

            boolean isEncapsulated = true;
            String inputEncapulatedValue = null;
            //find if it's an isEncapsulated or an referenced input data
            //if for the identifier attribut, the value is null, it's a referenced input data.
            for (Map.Entry<String, String> entry : attMap.entrySet()) {
                if (entry.getKey().equals(oneInput.getKey()) && entry.getValue() != null) {
                    isEncapsulated = false;
                } else if (entry.getKey().equals(oneInput.getKey())) {
                    inputEncapulatedValue = entry.getValue();
                }
            }

            //encapsulated
            if (isEncapsulated) {

                final DataType data = new DataType();

//                for (Map.Entry<String,String> att : attMap.entrySet()) {
//                    if(dataType.equalsIgnoreCase("boundingBox")){
//
//                        data.setBoundingBoxData(null);
//                    }else if(dataType.equalsIgnoreCase("complex")){
//
//                        data.setComplexData(null);
//                    }else if(dataType.equalsIgnoreCase("literal")){
//                        final LiteralInputType literalInput = new LiteralInputType();
//                        literalInput.s
//                        data.setLiteralData(null);
//                    }else{
//                        throw new CstlServiceException("Undefine input data type", INVALID_REQUEST, VERSION_PARAMETER.toLowerCase());
//                    }
//                }
//                input.setData(data);

            } else { //Reference
                final InputReferenceType inputRef = new InputReferenceType();
                for (Map.Entry<String, String> att : attMap.entrySet()) {
                    if (att.getKey().equalsIgnoreCase("xlink:href")) { // Href mendatory
                        inputRef.setHref(att.getValue());
                        continue;
                    } else if (att.getKey().equalsIgnoreCase("schema")) { //Schema optional
                        inputRef.setSchema(att.getValue());
                        continue;
                    } else if (att.getKey().equalsIgnoreCase("encoding")) { //Encoding optional
                        inputRef.setEncoding(att.getValue());
                        continue;
                    } else if (att.getKey().equalsIgnoreCase("method")) { //Method optional
                        inputRef.setMethod(att.getValue());
                        continue;
                    } else if (att.getKey().equalsIgnoreCase("mimeType")) { //MimeType optional
                        inputRef.setMimeType(att.getValue());
                        continue;
                    } else if (att.getKey().equalsIgnoreCase("body")) { //Body optional
                        //TODO
                        //inputRef.setBody(att.getValue());
                        continue;
                    } else if (att.getKey().equalsIgnoreCase("bodyReference")) { //BodyReference optional
                        //TODO
                        //inputRef.setBodyReference(new InputReferenceType.BodyReference().setHref(XML));
                        continue;
                    } else {
                        throw new CstlServiceException("Invalid DataInputs format, unrecognized parameter"
                                + att.getKey(), INVALID_REQUEST, VERSION_PARAMETER.toLowerCase());
                    }
                }
                if (inputRef.getHref() == null) {
                    throw new CstlServiceException("The href parameter value is mendatory", MISSING_PARAMETER_VALUE, VERSION_PARAMETER.toLowerCase());
                }

                input.setReference(inputRef);
            }
            inputList.add(input);
        }

        throw new UnsupportedOperationException("Not yet implemented");
    }

    private ResponseFormType extractResponseDocument(final String respDoc, final String rawData) throws CstlServiceException {

        ResponseFormType responseForm = new ResponseFormType();
        if (respDoc != null) {

            //Get data in the respDoc String
            final Map<String, Map> responseDocData = extractDataFromKvpString(respDoc);

            final ResponseDocumentType responseDoc = new ResponseDocumentType();

            final String strLineage = getParameter("lineage", false);
            if (strLineage.equals("true")) {
                responseDoc.setLineage(true);
            } else {
                responseDoc.setLineage(false);
            }
            //TODO get ouputs

            responseDoc.setStatus(false);
            responseDoc.setStoreExecuteResponse(false);

        } else {
            if (rawData != null) {

                //Get data in the rawData String
                final Map<String, Map> responseRawData = extractDataFromKvpString(rawData);

                OutputDefinitionType rawOutputData = new DocumentOutputDefinitionType();
                rawOutputData.setIdentifier(null);
                rawOutputData.setEncoding(null);
                rawOutputData.setMimeType(null);
                rawOutputData.setUom(null);
            } else {
                //respDoc and rawData = null
            }
        }



        throw new UnsupportedOperationException("Not yet implemented");
    }

    private static Map extractDataFromKvpString(final String inputString) throws CstlServiceException {

        final String[] allInputs = inputString.split(";");
        Map<String, Map> inputMap = new HashMap<>();
        for (String input : allInputs) {
            final String[] attribs = input.split("@");
            final String inputIdent = attribs[0].split("=")[0];

            final Map<String, String> attributsMap = new HashMap<>();
            for (String attribut : attribs) {
                String[] splitAttribute = attribut.split("=");

                if (splitAttribute.length == 2) {
                    attributsMap.put(splitAttribute[0], splitAttribute[1]);
                } else if (splitAttribute.length == 1) {
                    attributsMap.put(splitAttribute[0], null);
                } else {
                    throw new CstlServiceException("Invalid DataInputs format", INVALID_FORMAT, VERSION_PARAMETER.toLowerCase());
                }
            }
            inputMap.put(inputIdent, attributsMap);
        }
        return inputMap;
    }

    /**
     * Get an html page for the root resource.
     */
    private String getIndexPage() {
        return "<html>\n"
                + "  <title>Constellation WPS</title>\n"
                + "  <body>\n"
                + "    <h1><i>Constellation:</i></h1>\n"
                + "    <h1>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Web Processing Service</h1>\n"
                + "    <p>\n"
                + "      In order to access this service, you must form a valid request.\n"
                + "    </p\n"
                + "    <p>\n"
                + "      Try using a <a href=\"" + getUriContext().getAbsolutePath().toString()
                + "?service=WPS&request=GetCapabilities\""
                + ">Get Capabilities</a> request to obtain the 'Capabilities'<br>\n"
                + "      document which describes the resources available on this server.\n"
                + "    </p>\n"
                + "  </body>\n"
                + "</html>\n";
    }
}
