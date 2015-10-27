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

import org.apache.sis.util.ArgumentChecks;
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
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.NotSupportedException;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.sis.geometry.GeneralEnvelope;
import org.apache.sis.referencing.CommonCRS;
import static org.constellation.api.QueryConstants.*;

import static org.constellation.api.QueryConstants.ACCEPT_VERSIONS_PARAMETER;
import static org.constellation.api.QueryConstants.REQUEST_PARAMETER;
import static org.constellation.api.QueryConstants.SERVICE_PARAMETER;
import static org.constellation.api.QueryConstants.UPDATESEQUENCE_PARAMETER;
import static org.constellation.api.QueryConstants.VERSION_PARAMETER;
import org.constellation.wps.utils.WPSUtils;
import static org.constellation.wps.ws.WPSConstant.AS_REFERENCE_PARAMETER;
import static org.constellation.wps.ws.WPSConstant.BODY_PARAMETER;
import static org.constellation.wps.ws.WPSConstant.BODY_REFERENCE_PARAMETER;
import static org.constellation.wps.ws.WPSConstant.DATA_INPUTS_PARAMETER;
import static org.constellation.wps.ws.WPSConstant.DATA_TYPE_PARAMETER;
import static org.constellation.wps.ws.WPSConstant.DESCRIBEPROCESS;
import static org.constellation.wps.ws.WPSConstant.ENCODING_PARAMETER;
import static org.constellation.wps.ws.WPSConstant.EXECUTE;
import static org.constellation.wps.ws.WPSConstant.FORMAT_PARAMETER;
import static org.constellation.wps.ws.WPSConstant.GETCAPABILITIES;
import static org.constellation.wps.ws.WPSConstant.HEADER_PARAMETER;
import static org.constellation.wps.ws.WPSConstant.HREF_PARAMETER;
import static org.constellation.wps.ws.WPSConstant.IDENTIFER_PARAMETER;
import static org.constellation.wps.ws.WPSConstant.LANGUAGE_PARAMETER;
import static org.constellation.wps.ws.WPSConstant.LINEAGE_PARAMETER;
import static org.constellation.wps.ws.WPSConstant.METHOD_PARAMETER;
import static org.constellation.wps.ws.WPSConstant.MIME_TYPE_PARAMETER;
import static org.constellation.wps.ws.WPSConstant.RAW_DATA_OUTPUT_PARAMETER;
import static org.constellation.wps.ws.WPSConstant.RESPONSE_DOCUMENT_PARAMETER;
import static org.constellation.wps.ws.WPSConstant.SCHEMA_PARAMETER;
import static org.constellation.wps.ws.WPSConstant.STATUS_PARAMETER;
import static org.constellation.wps.ws.WPSConstant.STORE_EXECUTE_RESPONSE_PARAMETER;
import static org.constellation.wps.ws.WPSConstant.UOM_PARAMETER;
import org.geotoolkit.gml.xml.v311.DirectPositionType;
import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_FORMAT;
import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_REQUEST;
import static org.geotoolkit.ows.xml.OWSExceptionCode.MISSING_PARAMETER_VALUE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.OPERATION_NOT_SUPPORTED;
import org.geotoolkit.ows.xml.v110.BoundingBoxType;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.wps.xml.v100.ComplexDataType;
import org.geotoolkit.wps.xml.v100.LiteralDataType;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.FactoryException;
import org.opengis.geometry.MismatchedDimensionException;

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
     * The default CRS to apply on a bounding box when no CRS are provided with
     * a GET request using the execute method
     */
    private static final CoordinateReferenceSystem DEFAULT_CRS = CommonCRS.WGS84.normalizedGeographic();

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
        LOGGER.log(Level.INFO, "WPS REST service running ({0} instances)", getWorkerMapSize());
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
            EXECUTOR = null;
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
        final String version = getParameter(VERSION_PARAMETER, true);
        final String service = getParameter(SERVICE_PARAMETER, true);
        final String identifier =  getParameter(IDENTIFER_PARAMETER, true);
        final String language = getParameter(LANGUAGE_PARAMETER, true);
        final String dataInputs = getParameter(DATA_INPUTS_PARAMETER, false);
        final String respDoc = getParameter(RESPONSE_DOCUMENT_PARAMETER, false);
        final String respRawData = getParameter(RAW_DATA_OUTPUT_PARAMETER, false);
        final String lineage = getParameter(LINEAGE_PARAMETER, false);
        final String status = getParameter(STATUS_PARAMETER, false);
        final String storeExecuteResponse = getParameter(STORE_EXECUTE_RESPONSE_PARAMETER, false);

        if (ServiceDef.getServiceDefinition(service, version).equals(ServiceDef.WPS_1_0_0)) {
            final Execute exec = new Execute();
            exec.setLanguage(language);
            exec.setIdentifier(new CodeType(identifier));

            // Check dataInputs nullity
            if (dataInputs != null && !dataInputs.isEmpty())
                exec.setDataInputs(extractInput(identifier, dataInputs));

            boolean statusBoolean = extractOutputParameter(status);
            boolean lineageBoolean = extractOutputParameter(lineage);
            boolean storeExecuteResponseBoolean = extractOutputParameter(storeExecuteResponse);

            if (respDoc != null && !respDoc.isEmpty()) {
                ResponseFormType responseForm = extractResponseForm(respDoc, false);
                responseForm.getResponseDocument().setLineage(lineageBoolean);
                responseForm.getResponseDocument().setStatus(statusBoolean);
                responseForm.getResponseDocument().setStoreExecuteResponse(storeExecuteResponseBoolean);
                exec.setResponseForm(responseForm);
            }
            else if (respRawData != null && !respRawData.isEmpty()) {
                if (lineage != null || status != null || storeExecuteResponse != null)
                    throw new CstlServiceException("lineage, status and storeExecuteResponse can not be set alongside a RawDataOutput");
                exec.setResponseForm(extractResponseForm(respRawData, true));
            }
            return exec;
        } else
            throw new CstlServiceException("The version number specified for this request is not handled.");
    }

    /**
     * Helper method that extracts a boolean from one of the following WPS GET
     * argument : lineage, status, storeExecuteResponse
     * @param parameter should be a string extracted using getParameter with one
     * of the following arguments : STATUS_PARAMETER, LINEAGE_PARAMETER, STATUS_PARAMETER
     * @return the value of the extracted boolean
     * @throws CstlServiceException if the extraced value is not a boolean
     */
    static boolean extractOutputParameter(String parameter) throws CstlServiceException {
        if (parameter == null)
            return false;

        Map<String, Map> inputMap = extractDataFromKvpString(parameter);

        // Since this method is used with three different parameters
        // which are expected to just contain a boolean the map must have exactly
        // one element.
        assert inputMap.keySet().size() == 1;
        String value = inputMap.keySet().iterator().next();

        if ("true".equalsIgnoreCase(value))
            return true;
        else if ("false".equalsIgnoreCase(value))
            return false;

        throw new CstlServiceException("Expected values for lineage, status and storeExecuteResponse are true or false, the current value is " + value);
    }

    /**
     * Helper method to detect wether a given input is a reference input or not
     *
     * Since a reference has a mandatory 'href' attribute the test consist in checking
     * if this value exists in the attributes map.
     *
     * @param attributesMap attributes map for a given input
     * @return true if a 'href' attribute is detected
     */
    static boolean detectReference(final Map<String, String> attributesMap) {
        return attributesMap.keySet().contains(HREF_PARAMETER.toLowerCase());
    }

    /**
     * Helper method to detect wether a given input is a bounding box or not.
     *
     * Detecting a bounding box is a little tricky in some cases.
     *
     * When the literal has no declared attribute and bounding box has no CRS
     * they can not be distinguished.
     *
     * eg :
     *  literal -> array=42,26,30,102
     *  bounding box -> bbox=104,16,27,83
     *
     * So the solution is to get the input's class type by using its
     * ParameterDescriptor through the WPSUtils.getClassFromIOIdentifier method.
     *
     * But BoundingBox has no attributes, so if there's an attributes map with more
     * than one key (because it contains always at least one key) it means that
     * we are not reading a bounding box
     *
     * @param processIdentifier identifier of the current process
     * @param inputIdentifier input's identifier which may be a bounding box
     * @param attributesMap attributes map for a given input
     * @return true if a bounding box is detected
     */
    static boolean detectBoundingBox(final String processIdentifier, final String inputIdentifier, final Map<String, String> attributesMap) throws CstlServiceException {
        if (attributesMap.keySet().size() > 1)
            return false;

        Class inputType;
        try {
            inputType = WPSUtils.getIOClassFromIdentifier(processIdentifier, inputIdentifier);
        }
        catch (ParameterNotFoundException ex) {
            throw new CstlServiceException("Can not found the input " + inputIdentifier + " in the process " + processIdentifier + "\n" + ex.getLocalizedMessage());
        }

        return inputType == org.opengis.geometry.Envelope.class;
    }

    /**
     * Parse the decoded arguments of a GET request
     * @param processIdentifier process identifier, useful to give hints to the detect bounding box method
     * @param dataInputs the decoded arguments
     * @return a DataInputsType containing all the inputs read from the GET request
     * and translated into WPS Object
     * @throws CstlServiceException when an unknown attribute read
     */
    static DataInputsType extractInput(final String processIdentifier, final String dataInputs) throws CstlServiceException {
        ArgumentChecks.ensureNonEmpty("processIdentifier", processIdentifier);
        ArgumentChecks.ensureNonEmpty("dataInputs", dataInputs);

        final DataInputsType inputsType = new DataInputsType();
        List<InputType> inputTypeList = inputsType.getInput();
        Map<String, Map> inputMap = extractDataFromKvpString(dataInputs);

        for (String inputIdentifier : inputMap.keySet()) {
            Map<String, String> attributesMap = inputMap.get(inputIdentifier);

            if (detectReference(attributesMap))
                inputTypeList.add(readReference(inputIdentifier, attributesMap));
            else if (detectBoundingBox(processIdentifier, inputIdentifier, attributesMap))
                inputTypeList.add(readBoundingBoxData(inputIdentifier, attributesMap));
            else
                inputTypeList.add(readLiteralData(inputIdentifier, attributesMap));
        }
        return inputsType;
    }

    /**
     * Read an input assuming it's a reference input and encapsulate it into an InputType
     * @param inputIdentifier input identifier of the current input being processed
     * @param attributesMap attributes map associated with the current input
     * @return an InputReferenceType encapsulated into an InputType
     * @throws CstlServiceException when an unknown attributes is read
     */
    static InputType readReference(final String inputIdentifier, final Map<String, String> attributesMap) throws CstlServiceException {
        final InputType inputType = new InputType();
        inputType.setIdentifier(new CodeType(inputIdentifier));
        InputReferenceType inputRef = new InputReferenceType();

        for (String key : attributesMap.keySet()) {
            String value = attributesMap.get(key);

            if (key.equalsIgnoreCase(MIME_TYPE_PARAMETER) || key.equalsIgnoreCase(FORMAT_PARAMETER))
                inputRef.setMimeType(value);
            else if (key.equalsIgnoreCase(ENCODING_PARAMETER))
                inputRef.setEncoding(value);
            else if (key.equalsIgnoreCase(SCHEMA_PARAMETER))
                inputRef.setSchema(value);
            else if (key.equalsIgnoreCase(HREF_PARAMETER))
                inputRef.setHref(value);
            else if (key.equalsIgnoreCase(METHOD_PARAMETER)         ||
                     key.equalsIgnoreCase(BODY_PARAMETER)           ||
                     key.equalsIgnoreCase(BODY_REFERENCE_PARAMETER) ||
                     key.equalsIgnoreCase(HEADER_PARAMETER))
                throw new NotSupportedException("The " + key + " attribute is not supported in a GET request");

            else if (!(key.equals(inputIdentifier) && value == null))
                throw new CstlServiceException("Trying to set an InputReference with the unknown attribute " + key + " (value : " + value + ")");
        }

        inputType.setReference(inputRef);
        return inputType;
    }

    /**
     * Read an input assuming it's a literal data and encapsulate it into an InputType
     * @param inputIdentifier input identifier of the current input being processed
     * @param attributesMap attributes of the current input
     * @return a LiteralDataType encapsulated into an InputType
     * @throws CstlServiceException when an unknown attribute is read
     */
    static InputType readLiteralData(final String inputIdentifier, final Map<String, String> attributesMap) throws CstlServiceException {
        final InputType inputType = new InputType();
        inputType.setIdentifier(new CodeType(inputIdentifier));

        LiteralDataType literalData = new LiteralDataType();

        for (String key : attributesMap.keySet()) {
            String value = attributesMap.get(key);

            if (key.equalsIgnoreCase(DATA_TYPE_PARAMETER))
                literalData.setDataType(value);
            else if (key.equalsIgnoreCase(UOM_PARAMETER))
                literalData.setUom(value);
            else if (inputIdentifier.equals(key))
                literalData.setValue(value);
            else
                throw new CstlServiceException("Trying to set a LiteralData with the unknown attribute " + key + " (value : " + value + ")");
        }

        // Ensure the literal has a value
        if (literalData.getValue() == null || literalData.getValue().isEmpty())
            throw new CstlServiceException("No value given to " + inputIdentifier);

        DataType dataType = new DataType();
        dataType.setLiteralData(literalData);
        inputType.setData(dataType);
        return inputType;
    }

    /**
     * Read an input assuming it's a bounding box
     * @param inputIdentifier identifier of the current input being processed
     * @param attributesMap attributes of the current input
     * @return a BoundingBoxType encapsulated into an InputType
     */
    static InputType readBoundingBoxData(final String inputIdentifier, final Map<String, String> attributesMap) throws CstlServiceException {
        final InputType inputType = new InputType();
        inputType.setIdentifier(new CodeType(inputIdentifier));
        DataType dataType = new DataType();

        // A bounding box input has no attributes
        // So the only key in the attributesMap is equals to inputIdentifier
        // and its value is the bounding box string to parse
        assert attributesMap.size() == 1;

        String bboxString = attributesMap.values().iterator().next();
        String comaSeparatedStrings[] = bboxString.split(",");

        /*
         * These variables indicate if there is a crs code in the coma-separated string
         * and how many dimension there is in the crs
         */
        CoordinateReferenceSystem crs = null;


        //-- reading coordinate list.
        //-- in case where length is odd the last interger should be equals to coordinates numbers
        final List<Double> coords = new ArrayList<>();

        // Pre analysis
        for (String value : comaSeparatedStrings) {
            if (NumberUtils.isNumber(value)) {
                coords.add(Double.valueOf(value));
            } else {
                // If a CRS has been already read...abort
                if (crs != null)
                    throw new CstlServiceException("Two CRS found while reading the " + inputIdentifier + " BoundingBox");
                try {
                    // If when reading the crs you already read an odd number of
                    // bounding box coordinates there is a problem
                    if (coords.size() % 2 != 0)
                        throw new CstlServiceException("An odd number of bounding box coordinates has been read.");

                    crs = CRS.decode(value);
                } catch (FactoryException ex) {
                    throw new CstlServiceException(ex);
                }
            }
        }

        final int coordsListLength = coords.size();

        // If no coordinate has been read...abort
        if (coords.isEmpty())
            throw new CstlServiceException("Could not read any coordinate from the BoundingBox");

        // Extract BoundingBox dimension
        final int bboxDimension = coordsListLength >> 1;

        //-- if coordinates numbers is odd
        if (coordsListLength % 2 != 0) {//-- coordinateElement & 1 == 0
           /*
            * In the the following strings the number N tell us how many dimension
            * there is in the bounding box :
            * 46,102,... 47,103,... crs code,N
            * 46,102,... 47,103, ...N,crs code
            *
            * But this number is not mandatory.
            */
            final int evenListLength = coordsListLength & ~1;//-- = -1 on odd number
            final int dimensionHint = (int) StrictMath.round(coords.get(evenListLength));

            //-- check that cast double to integer has no problem
            if (StrictMath.abs(coords.get(evenListLength) - dimensionHint) > 1E-12)
                throw new CstlServiceException("The dimension parameter is not an integer : " + coords.get(evenListLength));

            ArgumentChecks.ensureStrictlyPositive("dimensionHint", dimensionHint);

            assert dimensionHint >= 2 : "Expected dimension hint equal or greater than 4, adapted for Geographical coordinates. Found : " + dimensionHint;

            if (evenListLength != dimensionHint * 2)
                throw new CstlServiceException("Expected " + evenListLength + " coordinates whereas " + dimensionHint + " was expected.");
        }

        if (crs != null && bboxDimension != crs.getCoordinateSystem().getDimension())
            throw new CstlServiceException("Reading coordinates number does not match with CRS dimension number.\n"
                    + " CRS dimension : "+crs.getCoordinateSystem().getDimension()+". Coordinates number : "+bboxDimension);

        //-- bind list -> array
        final double[] coordsArrayLower = new double[bboxDimension];
        final double[] coordsArrayUpper = new double[bboxDimension];

        for (int i = 0; i < bboxDimension; i++) {
            coordsArrayLower[i] = coords.get(i);
            coordsArrayUpper[i] = coords.get(i + bboxDimension);
        }

        final GeneralEnvelope generalEnvelope = new GeneralEnvelope(coordsArrayLower, coordsArrayUpper);

        if (crs == null) {
            // If no CRS are provided we set a default one which is the WGS84.
            // But this CRS can not be applied on every bounding box, so we have to
            // check the dimensions and raise an error when they are different
            if (bboxDimension > DEFAULT_CRS.getCoordinateSystem().getDimension())
                throw new CstlServiceException("No CRS provided and the default 2D CRS"
                                             + " can not be applied because the bounding box has " + bboxDimension + " dimensions.");

            generalEnvelope.setCoordinateReferenceSystem(DEFAULT_CRS);
        }
        else
            generalEnvelope.setCoordinateReferenceSystem(crs);


        dataType.setBoundingBoxData(new BoundingBoxType(generalEnvelope));
        inputType.setData(dataType);
        return inputType;
    }

    /**
     * Parse the decoded arguments of a GET request in order to extract the response
     * form.
     *
     * This method assumes that responseString contains only the response field of
     * the GET request and that it is URL decoded
     *
     * @param responseString the string containing document response attributes
     * @param isRawData set to true if responseString contains raw data
     * @return a ResponseDocumentType encapsulated into a ResponseFormType
     * @throws CstlServiceException when an unknown attribute is read
     */
    static ResponseFormType extractResponseForm(final String responseString, boolean isRawData) throws CstlServiceException {
        ArgumentChecks.ensureNonEmpty("responseString", responseString);

        Map<String, Map> inputMap = extractDataFromKvpString(responseString);
        ResponseDocumentType responseDocument = new ResponseDocumentType();
        ResponseFormType responseForm = new ResponseFormType();
        responseForm.setResponseDocument(responseDocument);

        for (String inputIdentifier : inputMap.keySet()) {
            Map<String, String> attributesMap = inputMap.get(inputIdentifier);
            OutputDefinitionType docOutput;
            if (isRawData)
                docOutput = new OutputDefinitionType();
            else
                docOutput = new DocumentOutputDefinitionType();

            docOutput.setIdentifier(new CodeType(inputIdentifier));

            for (String key : attributesMap.keySet()) {
                String value = attributesMap.get(key);

                if (key.equalsIgnoreCase(MIME_TYPE_PARAMETER) || key.equalsIgnoreCase(FORMAT_PARAMETER))
                    docOutput.setMimeType(value);
                else if (key.equalsIgnoreCase(ENCODING_PARAMETER))
                    docOutput.setEncoding(value);
                else if (key.equalsIgnoreCase(SCHEMA_PARAMETER))
                    docOutput.setSchema(value);
                else if (key.equalsIgnoreCase(UOM_PARAMETER))
                    docOutput.setUom(value);
                else if (key.equalsIgnoreCase(AS_REFERENCE_PARAMETER)) {
                    if (!isRawData)
                        ((DocumentOutputDefinitionType)docOutput).setAsReference(Boolean.parseBoolean(value));
                    else
                        throw new CstlServiceException("Trying to set RawDataOutput with unknown attribute " + key + " (value : " + value + ")");
                }
                else if (!key.equals(inputIdentifier)) {
                    if (isRawData)
                        throw new CstlServiceException("Trying to set RawDataOutput with unknown attribute " + key + " (value : " + value + ")");
                    else
                        throw new CstlServiceException("Trying to set DocumentOutputDefinition with unknown attribute " + key + " (value : " + value + ")");
                }
            }

            // We can have more than one DocumentOutputDefinition
            // but we can have just one RawDataOutput. Since the code to read
            // rawdata attribute is almost the same as the one to read
            // DocumentOutputDefinition (see the above condition against
            // AS_REFERENCE_PARAMETER), we kept everything in the same method
            // and just break the loop when a RawData has been read
            if (isRawData) {
                responseForm.setRawDataOutput(docOutput);
                break;
            }
            else
                responseDocument.getOutput().add((DocumentOutputDefinitionType) docOutput);
        }
        return responseForm;
    }

    static Map extractDataFromKvpString(final String inputString) throws CstlServiceException {

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
