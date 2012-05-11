/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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
package org.constellation.wps.ws;

import com.vividsolutions.jts.geom.Geometry;

import java.io.File;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import javax.measure.converter.UnitConverter;
import javax.measure.unit.Unit;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.constellation.ServiceDef;
import static org.constellation.api.CommonConstants.DEFAULT_CRS;
import static org.constellation.api.QueryConstants.*;
import org.constellation.configuration.Process;
import org.constellation.configuration.ProcessContext;
import org.constellation.configuration.ProcessFactory;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.wps.utils.WPSUtils;
import static org.constellation.wps.ws.WPSConstant.*;
import org.constellation.wps.ws.rs.WPSService;
import org.constellation.ws.AbstractWorker;
import org.constellation.ws.CstlServiceException;

import org.geotoolkit.geometry.isoonjts.GeometryUtils;
import org.geotoolkit.gml.JTStoGeometry;
import org.geotoolkit.gml.xml.v311.AbstractGeometryType;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import org.geotoolkit.ows.xml.v110.*;
import org.geotoolkit.process.AbstractProcess;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.process.ProcessingRegistry;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.util.ArgumentChecks;
import org.geotoolkit.util.converter.NonconvertibleObjectException;
import org.geotoolkit.util.converter.ObjectConverter;
import org.geotoolkit.wps.converters.WPSConvertersUtils;
import org.geotoolkit.wps.converters.outputs.complex.AbstractComplexOutputConverter;
import org.geotoolkit.wps.converters.outputs.references.AbstractReferenceOutputConverter;
import org.geotoolkit.wps.io.WPSIO;
import org.geotoolkit.wps.xml.WPSMarshallerPool;
import org.geotoolkit.wps.xml.v100.ExecuteResponse.ProcessOutputs;
import org.geotoolkit.wps.xml.v100.*;
import org.geotoolkit.xml.MarshallerPool;

import org.opengis.geometry.Envelope;
import org.opengis.parameter.*;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.FactoryException;
import org.opengis.util.NoSuchIdentifierException;

/**
 * WPS worker.Compute response of getCapabilities, DescribeProcess and Execute requests.
 *
 * @author Quentin Boileau
 */
public class WPSWorker extends AbstractWorker {

    private final ProcessContext context;
    /**
     * Supported CRS.
     */
    private static final SupportedCRSsType WPS_SUPPORTED_CRS;

    static {
        WPS_SUPPORTED_CRS = new SupportedCRSsType();

        //Default CRS.
        final SupportedCRSsType.Default defaultCRS = new SupportedCRSsType.Default();
        defaultCRS.setCRS(DEFAULT_CRS.get(0));
        WPS_SUPPORTED_CRS.setDefault(defaultCRS);

        //Supported CRS.
        final CRSsType supportedCRS = new CRSsType();
        supportedCRS.getCRS().addAll(DEFAULT_CRS);
        WPS_SUPPORTED_CRS.setSupported(supportedCRS);
    }
    private static final int TIMEOUT = 30;
    
    /**
     * List of process descriptor avaible.
     */
    private final List<ProcessDescriptor> ProcessDescriptorList = new ArrayList<ProcessDescriptor>();

    /**
     * Constructor.
     *
     * @param id
     * @param configurationDirectory
     */
    public WPSWorker(final String id, final File configurationDirectory) {
        super(id, configurationDirectory, ServiceDef.Specification.WPS);
        ProcessContext candidate = null;
        if (configurationDirectory != null) {
            final File lcFile = new File(configurationDirectory, "processContext.xml");
            if (lcFile.exists()) {
                Unmarshaller unmarshaller = null;
                try {
                    unmarshaller = GenericDatabaseMarshallerPool.getInstance().acquireUnmarshaller();
                    Object obj = unmarshaller.unmarshal(lcFile);
                    if (obj instanceof ProcessContext) {
                        candidate = (ProcessContext) obj;
                        isStarted = true;
                    } else {
                        startError = "The process context File does not contain a ProcessContext object";
                        isStarted = false;
                        LOGGER.log(Level.WARNING, startError);
                    }
                } catch (JAXBException ex) {
                    startError = "JAXBExeception while unmarshalling the process context File";
                    isStarted = false;
                    LOGGER.log(Level.WARNING, startError, ex);
                } finally {
                    if (unmarshaller != null) {
                        GenericDatabaseMarshallerPool.getInstance().release(unmarshaller);
                    }
                }
            } else {
                startError = "The configuration file processContext.xml has not been found";
                isStarted = false;
                LOGGER.log(Level.WARNING, "\nThe worker ({0}) is not working!\nCause: ", id);
            }
        } else {
            startError = "The configuration directory has not been found";
            isStarted = false;
            LOGGER.log(Level.WARNING, "\nThe worker ({0}) is not working!\nCause: " + startError, id);
        }
        this.context = candidate;
        fillProcessList();

        if (isStarted) {
            LOGGER.log(Level.INFO, "WPS worker {0} running", id);
        }
    }

    /**
     * Create process list from context file. 
     */
    private void fillProcessList() {
        if (context != null) {
            // Load all process from all factory
            if (Boolean.TRUE == context.getProcesses().getLoadAll()) {
                LOGGER.info("Loading all process");
                final Iterator<ProcessingRegistry> factoryIte = ProcessFinder.getProcessFactories();
                while (factoryIte.hasNext()) {
                    final ProcessingRegistry factory = factoryIte.next();
                    for (final ProcessDescriptor descriptor : factory.getDescriptors()) {
                        ProcessDescriptorList.add(descriptor);
                    }
                }
            } else {
                for (final ProcessFactory processFactory : context.getProcessFactories()) {
                    final ProcessingRegistry factory = ProcessFinder.getProcessFactory(processFactory.getAutorityCode());
                    if (factory != null) {
                        if (Boolean.TRUE == processFactory.getLoadAll()) {
                            LOGGER.log(Level.INFO, "loading all process for factory:{0}", processFactory.getAutorityCode());
                            for (final ProcessDescriptor descriptor : factory.getDescriptors()) {
                                ProcessDescriptorList.add(descriptor);
                            }
                        } else {
                            for (final Process process : processFactory.getInclude().getProcess()) {
                                try {
                                    final ProcessDescriptor desc = factory.getDescriptor(process.getId());
                                    if (desc != null) {
                                        ProcessDescriptorList.add(desc);
                                    }
                                } catch (NoSuchIdentifierException ex) {
                                    LOGGER.log(Level.WARNING, "Unable to find a process named:" + process.getId() + " in factory " + processFactory.getAutorityCode(), ex);
                                }
                            }
                        }
                    } else {
                        LOGGER.log(Level.WARNING, "No process factory found for authorityCode:{0}", processFactory.getAutorityCode());
                    }
                }
            }
        }
    }

    @Override
    protected MarshallerPool getMarshallerPool() {
        return WPSMarshallerPool.getInstance();
    }

    @Override
    public void destroy() {
    }

    //////////////////////////////////////////////////////////////////////
    //                      GetCapabilities
    //////////////////////////////////////////////////////////////////////
    /**
     * GetCapabilities request
     *
     * @param request
     * @return
     * @throws CstlServiceException
     */
    public WPSCapabilitiesType getCapabilities(final GetCapabilities request) throws CstlServiceException {
        isWorking();

        //check SERVICE=WPS
        if (!request.getService().equalsIgnoreCase(WPS_SERVICE)) {
            throw new CstlServiceException("The parameter " + SERVICE_PARAMETER + " must be specified as WPS",
                    INVALID_PARAMETER_VALUE, SERVICE_PARAMETER.toLowerCase());
        }

        //check LANGUAGE=en-EN
        if (request.getLanguage() != null && !request.getLanguage().equalsIgnoreCase(WPS_LANG)) {
            throw new CstlServiceException("The specified " + LANGUAGE_PARAMETER + " is not handled by the service. ",
                    INVALID_PARAMETER_VALUE, LANGUAGE_PARAMETER.toLowerCase());
        }

        List<String> versionsAccepted = null;
        if (request.getAcceptVersions() != null) {
            versionsAccepted = request.getAcceptVersions().getVersion();
        }

        boolean versionSupported = false;
        if (versionsAccepted != null) {

            if (versionsAccepted.contains(ServiceDef.WPS_1_0_0.version.toString())) {
                versionSupported = true;
            }
        }

        //if versionAccepted parameted is not define return the last getCapabilities
        if (versionsAccepted == null || versionSupported) {
            return getCapabilities100((org.geotoolkit.wps.xml.v100.GetCapabilities) request);
        } else {
            throw new CstlServiceException("The specified " + ACCEPT_VERSIONS_PARAMETER + " numbers are not handled by the service.",
                    VERSION_NEGOTIATION_FAILED, ACCEPT_VERSIONS_PARAMETER.toLowerCase());
        }

    }

    /**
     * GetCapabilities request for WPS 1.0.0.
     *
     * @param request
     * @return
     * @throws CstlServiceException
     */
    private WPSCapabilitiesType getCapabilities100(final GetCapabilities request) throws CstlServiceException {

        //set the current updateSequence parameter
        final boolean returnUS = returnUpdateSequenceDocument(request.getUpdateSequence());
        if (returnUS) {
            return new WPSCapabilitiesType("1.0.0", getCurrentUpdateSequence());
        }

        // We unmarshall the static capabilities document.
        final WPSCapabilitiesType staticCapabilities = (WPSCapabilitiesType) getStaticCapabilitiesObject(ServiceDef.WPS_1_0_0.version.toString(), ServiceDef.Specification.WPS.toString());

        staticCapabilities.getOperationsMetadata().updateURL(getServiceUrl());

        final ProcessOfferings offering = new ProcessOfferings();

        for (final ProcessDescriptor procDesc : ProcessDescriptorList) {
            if (WPSUtils.isSupportedProcess(procDesc)) {
                offering.getProcess().add(WPSUtils.generateProcessBrief(procDesc));
            }
        }

        staticCapabilities.setProcessOfferings(offering);
        staticCapabilities.setUpdateSequence(getCurrentUpdateSequence());
        return staticCapabilities;
    }

    //////////////////////////////////////////////////////////////////////
    //                      DescibeProcess
    //////////////////////////////////////////////////////////////////////
    /**
     * Describe process request.
     *
     * @param request
     * @return ProcessDescriptions
     * @throws CstlServiceException
     *
     */
    public ProcessDescriptions describeProcess(final DescribeProcess request) throws CstlServiceException {
        isWorking();

        //check SERVICE=WPS
        if (!request.getService().equalsIgnoreCase(WPS_SERVICE)) {
            throw new CstlServiceException("The parameter " + SERVICE_PARAMETER + " must be specified as WPS",
                    INVALID_PARAMETER_VALUE, SERVICE_PARAMETER.toLowerCase());
        }

        //check LANGUAGE=en-EN
        if (request.getLanguage() != null && !request.getLanguage().equalsIgnoreCase(WPS_LANG)) {
            throw new CstlServiceException("The specified " + LANGUAGE_PARAMETER + " is not handled by the service. ",
                    INVALID_PARAMETER_VALUE, LANGUAGE_PARAMETER.toLowerCase());
        }

        //check mandatory version is not missing.
        if (request.getVersion() == null || request.getVersion().isEmpty()) {
            throw new CstlServiceException("The parameter " + VERSION_PARAMETER + " must be specified.",
                    MISSING_PARAMETER_VALUE, VERSION_PARAMETER.toLowerCase());
        }

        //check VERSION=1.0.0
        if (request.getVersion().equals(ServiceDef.WPS_1_0_0.version.toString())) {
            return describeProcess100((org.geotoolkit.wps.xml.v100.DescribeProcess) request);
        } else {
            throw new CstlServiceException("The specified " + VERSION_PARAMETER + " number is not handled by the service.",
                    VERSION_NEGOTIATION_FAILED, VERSION_PARAMETER.toLowerCase());
        }
    }

    /**
     * Describe a process in WPS v1.0.0.
     *
     * @param request
     * @return ProcessDescriptions
     * @throws CstlServiceException
     */
    private ProcessDescriptions describeProcess100(DescribeProcess request) throws CstlServiceException {

        //check mandatory IDENTIFIER is not missing.
        if (request.getIdentifier() == null || request.getIdentifier().isEmpty()) {
            throw new CstlServiceException("The parameter " + IDENTIFER_PARAMETER + " must be specified.",
                    MISSING_PARAMETER_VALUE, IDENTIFER_PARAMETER.toLowerCase());
        }

        final ProcessDescriptions descriptions = new ProcessDescriptions();
        descriptions.setLang(WPS_LANG);
        descriptions.setService(WPS_SERVICE);
        descriptions.setVersion(WPS_1_0_0);

        for (final CodeType identifier : request.getIdentifier()) {

            // Find the process
            final ProcessDescriptor processDesc = WPSUtils.getProcessDescriptor(identifier.getValue());
            if (!WPSUtils.isSupportedProcess(processDesc)) {
                throw new CstlServiceException("Process " + identifier.getValue() + " not supported by the service.",
                        INVALID_PARAMETER_VALUE, IDENTIFER_PARAMETER.toLowerCase());
            }

            final ProcessDescriptionType descriptionType = new ProcessDescriptionType();
            descriptionType.setIdentifier(identifier);                                                                      //Process Identifier
            descriptionType.setTitle(WPSUtils.buildProcessTitle(processDesc));                                              //Process Title
            descriptionType.setAbstract(WPSUtils.capitalizeFirstLetter(processDesc.getProcedureDescription().toString()));  //Process abstract
            descriptionType.setProcessVersion(WPS_1_0_0);                                                                   //Process verstion 
            descriptionType.setWSDL(null);                                                                                  //TODO WSDL
            descriptionType.setStatusSupported(true);         
            descriptionType.setStoreSupported(WPSService.SUPPORT_STORAGE);

            // Get process input and output descriptors
            final ParameterDescriptorGroup input = processDesc.getInputDescriptor();
            final ParameterDescriptorGroup output = processDesc.getOutputDescriptor();

            ///////////////////////////////
            //  Process Input parameters
            ///////////////////////////////
            final ProcessDescriptionType.DataInputs dataInputs = new ProcessDescriptionType.DataInputs();
            for (final GeneralParameterDescriptor param : input.descriptors()) {

                // If the Parameter Descriptor isn't a GroupeParameterDescriptor
                if (param instanceof ParameterDescriptor) {
                    final InputDescriptionType in = new InputDescriptionType();
                    final ParameterDescriptor paramDesc = (ParameterDescriptor) param;

                    // Parameter informations
                    in.setIdentifier(new CodeType(WPSUtils.buildProcessIOIdentifiers(processDesc, paramDesc, WPSIO.IOType.INPUT)));
                    in.setTitle(WPSUtils.capitalizeFirstLetter(paramDesc.getName().getCode()));
                    in.setAbstract(WPSUtils.capitalizeFirstLetter(paramDesc.getRemarks().toString()));

                    //set occurs
                    in.setMaxOccurs(BigInteger.valueOf(paramDesc.getMaximumOccurs()));
                    in.setMinOccurs(BigInteger.valueOf(paramDesc.getMinimumOccurs()));
                    // Input class
                    final Class clazz = paramDesc.getValueClass();

                    // BoundingBox type
                    if (WPSIO.isSupportedBBoxInputClass(clazz)) {
                        in.setBoundingBoxData(WPS_SUPPORTED_CRS);
                        
                         //Simple object (Integer, double, ...) and Object which need a conversion from String like affineTransform or WKT Geometry
                    } else if (WPSIO.isSupportedLiteralInputClass(clazz)) {
                        final LiteralInputType literal = new LiteralInputType();

                        if (paramDesc.getDefaultValue() != null) {
                            literal.setDefaultValue(paramDesc.getDefaultValue().toString()); //default value if enable
                        }

                        if (paramDesc.getUnit() != null) {
                            literal.setUOMs(WPSUtils.generateUOMs(paramDesc));
                        }
                        //AllowedValues setted
                        if (paramDesc.getValidValues() != null && !paramDesc.getValidValues().isEmpty()) {
                            literal.setAllowedValues(new AllowedValues(paramDesc.getValidValues()));
                        } else {
                            literal.setAnyValue(new AnyValue());
                        }
                        literal.setValuesReference(null);
                        literal.setDataType(WPSUtils.createDataType(clazz));


                        in.setLiteralData(literal);
                        
                        //Complex type (XML, ...)     
                    } else if (WPSIO.isSupportedComplexInputClass(clazz)) {
                        in.setComplexData(WPSUtils.describeComplex(clazz, WPSIO.IOType.INPUT, WPSIO.FormChoice.COMPLEX));

                        //Reference type (XML, ...)    
                    } else if (WPSIO.isSupportedReferenceInputClass(clazz)) {
                        in.setComplexData(WPSUtils.describeComplex(clazz, WPSIO.IOType.INPUT, WPSIO.FormChoice.REFERENCE));
                        
                       

                    } else {
                        throw new CstlServiceException("Process input not supported.", NO_APPLICABLE_CODE);
                    }

                    dataInputs.getInput().add(in);
                } else {
                    throw new CstlServiceException("Process parameter invalid", NO_APPLICABLE_CODE);
                }
            }
            if (!dataInputs.getInput().isEmpty()) {
                descriptionType.setDataInputs(dataInputs);
            }

            ///////////////////////////////
            //  Process Output parameters
            ///////////////////////////////
            final ProcessDescriptionType.ProcessOutputs dataOutput = new ProcessDescriptionType.ProcessOutputs();
            for (GeneralParameterDescriptor param : output.descriptors()) {
                final OutputDescriptionType out = new OutputDescriptionType();

                //simple paramater
                if (param instanceof ParameterDescriptor) {
                    final ParameterDescriptor paramDesc = (ParameterDescriptor) param;

                    //parameter informations
                    out.setIdentifier(new CodeType(WPSUtils.buildProcessIOIdentifiers(processDesc, paramDesc, WPSIO.IOType.OUTPUT)));
                    out.setTitle(WPSUtils.capitalizeFirstLetter(paramDesc.getName().getCode()));
                    out.setAbstract(WPSUtils.capitalizeFirstLetter(paramDesc.getRemarks().toString()));

                    //input class
                    final Class clazz = paramDesc.getValueClass();

                    //BoundingBox type
                    if (WPSIO.isSupportedBBoxOutputClass(clazz)) {
                        out.setBoundingBoxOutput(WPS_SUPPORTED_CRS);
                        
                        //Simple object (Integer, double) and Object which need a conversion from String like affineTransform or Geometry
                    } else if (WPSIO.isSupportedLiteralOutputClass(clazz)) {

                        final LiteralOutputType literal = new LiteralOutputType();
                        literal.setDataType(WPSUtils.createDataType(clazz));
                        if (paramDesc.getUnit() != null) {
                            literal.setUOMs(WPSUtils.generateUOMs(paramDesc));
                        }

                        out.setLiteralOutput(literal);
                        
                        //Complex type (XML, raster, ...)
                    } else if (WPSIO.isSupportedComplexOutputClass(clazz)) {
                        out.setComplexOutput((SupportedComplexDataInputType) WPSUtils.describeComplex(clazz, WPSIO.IOType.OUTPUT, WPSIO.FormChoice.COMPLEX));

                    } else if (WPSIO.isSupportedReferenceOutputClass(clazz)) {
                        out.setComplexOutput((SupportedComplexDataInputType) WPSUtils.describeComplex(clazz, WPSIO.IOType.OUTPUT, WPSIO.FormChoice.REFERENCE));

                    } else {
                        throw new CstlServiceException("Process output not supported.", NO_APPLICABLE_CODE);
                    }

                } else {
                    throw new CstlServiceException("Process parameter invalid", NO_APPLICABLE_CODE);
                }

                dataOutput.getOutput().add(out);
            }
            descriptionType.setProcessOutputs(dataOutput);
            descriptions.getProcessDescription().add(descriptionType);
        }

        return descriptions;
    }

    //////////////////////////////////////////////////////////////////////
    //                      Execute
    //////////////////////////////////////////////////////////////////////
    /**
     * Redirect execute requests from the WPS version requested.
     *
     * @param request
     * @return execute response (Raw data or Document response) depends of the ResponseFormType in execute request
     * @throws CstlServiceException
     */
    public Object execute(Execute request) throws CstlServiceException {
        isWorking();


        //check SERVICE=WPS
        if (!request.getService().equalsIgnoreCase(WPS_SERVICE)) {
            throw new CstlServiceException("The parameter " + SERVICE_PARAMETER + " must be specified as WPS",
                    INVALID_PARAMETER_VALUE, SERVICE_PARAMETER.toLowerCase());
        }

        //check LANGUAGE=en-EN
        if (request.getLanguage() != null && !request.getLanguage().equalsIgnoreCase(WPS_LANG)) {
            throw new CstlServiceException("The specified " + LANGUAGE_PARAMETER + " is not handled by the service. ",
                    INVALID_PARAMETER_VALUE, LANGUAGE_PARAMETER.toLowerCase());
        }

        //check mandatory version is not missing.
        if (request.getVersion() == null || request.getVersion().isEmpty()) {
            throw new CstlServiceException("The parameter " + VERSION_PARAMETER + " must be specified.",
                    MISSING_PARAMETER_VALUE, VERSION_PARAMETER.toLowerCase());
        }

        //check VERSION=1.0.0
        if (request.getVersion().equals(ServiceDef.WPS_1_0_0.version.toString())) {
            return execute100((org.geotoolkit.wps.xml.v100.Execute) request);
        } else {
            throw new CstlServiceException("The specified " + VERSION_PARAMETER + " number is not handled by the service.",
                    VERSION_NEGOTIATION_FAILED, VERSION_PARAMETER.toLowerCase());
        }
    }

    /**
     * Execute a process in wps v1.0.0.
     *
     * @param request
     * @return
     * @throws CstlServiceException
     */
    private Object execute100(Execute request) throws CstlServiceException {

        //check mandatory IDENTIFIER is not missing.
        if (request.getIdentifier() == null || request.getIdentifier().getValue() == null || request.getIdentifier().getValue().isEmpty()) {
            throw new CstlServiceException("The parameter " + IDENTIFER_PARAMETER + " must be specified.",
                    MISSING_PARAMETER_VALUE, IDENTIFER_PARAMETER.toLowerCase());
        }

        /*
         * Get the requested output form
         */
        final ResponseFormType responseForm = request.getResponseForm();
        if (responseForm == null) {
            throw new CstlServiceException("The response form should be defined.", MISSING_PARAMETER_VALUE, "responseForm");
        }
        
        final OutputDefinitionType rawData = responseForm.getRawDataOutput();
        final ResponseDocumentType respDoc = responseForm.getResponseDocument();


        boolean isOutputRaw = rawData != null ? true : false; // the default output is a ResponseDocument
        boolean isOutputRespDoc = respDoc != null ? true : false;

        if (!isOutputRaw && !isOutputRespDoc) {
            throw new CstlServiceException("The response form should be defined.", MISSING_PARAMETER_VALUE, "responseForm");
        }
        if(isOutputRespDoc && respDoc.isStatus() && !respDoc.isStoreExecuteResponse()){
             throw new CstlServiceException("Set the storeExecuteResponse to true if you want to see status in response documents.", INVALID_PARAMETER_VALUE, "storeExecuteResponse");
        }
        
        final StatusType status = new StatusType();
        LOGGER.log(Level.INFO, "Process Execute : {0}", request.getIdentifier().getValue());
        //Find the process
        final ProcessDescriptor processDesc = WPSUtils.getProcessDescriptor(request.getIdentifier().getValue());

        if (!WPSUtils.isSupportedProcess(processDesc)) {
            throw new CstlServiceException("Process " + request.getIdentifier().getValue() + " not supported by the service.",
                    INVALID_PARAMETER_VALUE, IDENTIFER_PARAMETER.toLowerCase());
        }

        //check requested INPUT/OUTPUT. Throw an CstlException otherwise.
        WPSUtils.checkValidInputOuputRequest(processDesc, request);
        try {
            final GregorianCalendar c = new GregorianCalendar();
            c.setTime(new Date());
            status.setCreationTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(c) );
        } catch (DatatypeConfigurationException ex) {
            throw new CstlServiceException(ex);
        }
        status.setProcessAccepted("Process "+request.getIdentifier().getValue()+" found.");
        
        String respDocFileName = UUID.randomUUID().toString();
        /*
         * ResponseDocument attributs
         */
        boolean isLineage = isOutputRespDoc ? respDoc.isLineage() : false;
        boolean useStatus = isOutputRespDoc ? respDoc.isStatus() : false;
        boolean useStorage = isOutputRespDoc ? respDoc.isStoreExecuteResponse() : false;
        final List<DocumentOutputDefinitionType> wantedOutputs = isOutputRespDoc ? respDoc.getOutput() : null;

        //LOGGER.log(Level.INFO, "Request : [Lineage=" + isLineage + ", Storage=" + useStatus + ", Status=" + useStorage + "]");


        //Input temporary files used by the process. In order to delete them at the end of the process.
        List<File> files = null;

        //Create Process and Inputs
        final ParameterValueGroup in = processDesc.getInputDescriptor().createValue();

        ///////////////////////
        //   Process INPUT
        //////////////////////
        List<InputType> requestInputData = new ArrayList<InputType>();
        if (request.getDataInputs() != null && request.getDataInputs().getInput() != null) {
            requestInputData = request.getDataInputs().getInput();
        }
        final List<GeneralParameterDescriptor> processInputDesc = processDesc.getInputDescriptor().descriptors();


        //Fill input process with there default values
        for (final GeneralParameterDescriptor inputGeneDesc : processInputDesc) {

            if (inputGeneDesc instanceof ParameterDescriptor) {
                final ParameterDescriptor inputDesc = (ParameterDescriptor) inputGeneDesc;

                if (inputDesc.getDefaultValue() != null) {
                    in.parameter(inputDesc.getName().getCode()).setValue(inputDesc.getDefaultValue());
                }
            } else {
                throw new CstlServiceException("Process parameter invalid", OPERATION_NOT_SUPPORTED);
            }
        }

        //Fill process input with datas from execute request.
        fillProcessInputFromRequest(in, requestInputData, processInputDesc, files);


        ///////////////////////
        //   RUN Process
        //////////////////////


        //Give input parameter to the process
        final org.geotoolkit.process.Process process = processDesc.createProcess(in);
                
        //Sumbit the process execution to the ExecutorService
        final List<GeneralParameterDescriptor> processOutputDesc = processDesc.getOutputDescriptor().descriptors();
        ParameterValueGroup result = null;
        
        if (isOutputRaw) {
            
            ////////
            // RAW Sync no timeout
            ////////
            final Future<ParameterValueGroup> future = WPSService.EXECUTOR.submit(process);
            try {
                result = future.get();

            } catch (InterruptedException ex) {
                throw new CstlServiceException("", ex, NO_APPLICABLE_CODE);
            } catch (ExecutionException ex) {
                throw new CstlServiceException("Process execution failed", ex, NO_APPLICABLE_CODE);
            }
            return createRawOutput(rawData, processOutputDesc, result);

        } else {
            
            final ExecuteResponse response = new ExecuteResponse();
            response.setService(WPS_SERVICE);
            response.setVersion(WPS_1_0_0);
            response.setLang(WPS_LANG);
            response.setServiceInstance(getServiceUrl() + "SERVICE=WPS&REQUEST=GetCapabilities");

            //Give a bief process description into the execute response
            response.setProcess(WPSUtils.generateProcessBrief(processDesc));
            
            //Lineage option.
            if (respDoc.isLineage()) {
                //Inputs
                response.setDataInputs(request.getDataInputs());
                final OutputDefinitionsType outputsDef = new OutputDefinitionsType();
                outputsDef.getOutput().addAll(respDoc.getOutput());
                //Outputs
                response.setOutputDefinitions(outputsDef);
            }
            
            if(useStatus){
                ////////
                // DOC Async
                ////////
                response.setStatus(status);
                process.addListener(new WPSProcessListener(request, response, respDocFileName, ServiceDef.WPS_1_0_0, getServiceUrl()));
                WPSService.EXECUTOR.submit(process);
                
            } else {
               
                ////////
                // DOC Sync + timeout
                ////////
                final Future<ParameterValueGroup> future = WPSService.EXECUTOR.submit(process);
                try {
                    result = future.get(TIMEOUT, TimeUnit.SECONDS);

                } catch (InterruptedException ex) {
                    throw new CstlServiceException(ex.getMessage(), ex, NO_APPLICABLE_CODE);
                } catch (ExecutionException ex) {
                    //process exception
                    throw new CstlServiceException(ex.getMessage(), ex, NO_APPLICABLE_CODE);
                } catch (TimeoutException ex) {
                    ((AbstractProcess) process).cancelProcess();
                    future.cancel(true);
                    throw new CstlServiceException("Process execution timeout. This process is too long and had been canceled,"
                            + " re-run request with status set to true.", NO_APPLICABLE_CODE);
                }
                
                final ExecuteResponse.ProcessOutputs outputs = new ExecuteResponse.ProcessOutputs();
                fillOutputsFromProcessResult(outputs, wantedOutputs, processOutputDesc, result, getServiceUrl());
                response.setProcessOutputs(outputs);
                
            }

            if (respDoc.isStoreExecuteResponse()) {
                if (!WPSService.SUPPORT_STORAGE) {
                    throw new CstlServiceException("Storage not supported.", STORAGE_NOT_SUPPORTED, "storeExecuteResponse");
                }
                response.setStatusLocation(WPSUtils.getTempDirectoryURL(getServiceUrl()) + "/" + respDocFileName); //Output data URL
                WPSUtils.storeResponse(response, respDocFileName);
            }

            //Delete input temporary files 
            //WPSUtils.cleanTempFiles(files);
            return response;
        }
    }

    /**
     * For each inputs in Execute request, this method will find corresponding {@link ParameterDescriptor ParameterDescriptor} input in the
     * process and fill the {@link ParameterValueGroup ParameterValueGroup} with the data.
     *
     * @param in
     * @param requestInputData
     * @param processInputDesc
     * @param files
     * @throws CstlServiceException
     */
    private void fillProcessInputFromRequest(final ParameterValueGroup in, final List<InputType> requestInputData,
            final List<GeneralParameterDescriptor> processInputDesc, List<File> files) throws CstlServiceException {

        ArgumentChecks.ensureNonNull("in", in);
        ArgumentChecks.ensureNonNull("requestInputData", requestInputData);

        for (final InputType inputRequest : requestInputData) {

            if (inputRequest.getIdentifier() == null || inputRequest.getIdentifier().getValue() == null || inputRequest.getIdentifier().getValue().isEmpty()) {
                throw new CstlServiceException("Empty input Identifier.", INVALID_PARAMETER_VALUE);
            }

            final String inputIdentifier = inputRequest.getIdentifier().getValue();
            final String inputIdentifierCode = WPSUtils.extractProcessIOCode(inputIdentifier);

            //Check if it's a valid input identifier and hold it if found.
            ParameterDescriptor inputDescriptor = null;
            for (final GeneralParameterDescriptor processInput : processInputDesc) {
                if (processInput.getName().getCode().equals(inputIdentifierCode) && processInput instanceof ParameterDescriptor) {
                    inputDescriptor = (ParameterDescriptor) processInput;
                    break;
                }
            }
            if (inputDescriptor == null) {
                throw new CstlServiceException("Invalid or unknow input identifier " + inputIdentifier + ".", INVALID_PARAMETER_VALUE, inputIdentifier);
            }

            boolean isReference = false;
            boolean isBBox = false;
            boolean isComplex = false;
            boolean isLiteral = false;

            if (inputRequest.getReference() != null) {
                isReference = true;
            } else {
                if (inputRequest.getData() != null) {

                    final DataType dataType = inputRequest.getData();
                    if (dataType.getBoundingBoxData() != null) {
                        isBBox = true;
                    } else if (dataType.getComplexData() != null) {
                        isComplex = true;
                    } else if (dataType.getLiteralData() != null) {
                        isLiteral = true;
                    } else {
                        throw new CstlServiceException("Input Data element not found.");
                    }
                } else {
                    throw new CstlServiceException("Input doesn't have data or reference.");
                }
            }


            /*
             * Get expected input Class from the process input
             */
            final Class expectedClass = inputDescriptor.getValueClass();

            Object dataValue = null;
            //LOGGER.log(Level.INFO, "Input : " + inputIdentifier + " : expected Class " + expectedClass.getCanonicalName());


            /**
             * Handle referenced input data.
             */
            if (isReference) {

                //Check if the expected class is supported for reference using
                if (!WPSIO.isSupportedReferenceInputClass(expectedClass)) {
                    throw new CstlServiceException("The input" + inputIdentifier + " can't handle reference.", INVALID_PARAMETER_VALUE, inputIdentifier);
                }
                final InputReferenceType requestedRef = inputRequest.getReference();
                if (requestedRef.getHref() == null) {
                    throw new CstlServiceException("Invalid reference input : href can't be null.", INVALID_PARAMETER_VALUE, inputIdentifier);
                }
                try {
                    dataValue = WPSConvertersUtils.convertFromReference(requestedRef, expectedClass);
                } catch (NonconvertibleObjectException ex) {
                    throw new CstlServiceException(ex.getMessage(), ex, NO_APPLICABLE_CODE);
                }
                
                if (dataValue instanceof File) {
                    if (files == null) {
                        files = new ArrayList<File>();
                    }
                    files.add((File) dataValue);
                }
            }


            /**
             * Handle Bbox input data.
             */
            if (isBBox) {
                final BoundingBoxType bBox = inputRequest.getData().getBoundingBoxData();
                final List<Double> lower = bBox.getLowerCorner();
                final List<Double> upper = bBox.getUpperCorner();
                final String crs = bBox.getCrs();
                final int dimension = bBox.getDimensions();

                //Check if it's a 2D boundingbox
                if (dimension != 2 || lower.size() != 2 || upper.size() != 2) {
                    throw new CstlServiceException("Invalid data input : Only 2 dimension boundingbox supported.", OPERATION_NOT_SUPPORTED, inputIdentifier);
                }

                CoordinateReferenceSystem crsDecode;
                try {
                    crsDecode = CRS.decode(crs);
                } catch (FactoryException ex) {
                    throw new CstlServiceException("Invalid data input : CRS not supported.",
                            ex, OPERATION_NOT_SUPPORTED, inputIdentifier);
                }

                final Envelope envelop = GeometryUtils.createCRSEnvelope(crsDecode, lower.get(0), lower.get(1), upper.get(0), upper.get(1));
                dataValue = envelop;
            }

            /**
             * Handle Complex input data.
             */
            if (isComplex) {
                //Check if the expected class is supproted for complex using
                if (!WPSIO.isSupportedComplexInputClass(expectedClass)) {
                    throw new CstlServiceException("Complex value expected", INVALID_PARAMETER_VALUE, inputIdentifier);
                }

                final ComplexDataType complex = inputRequest.getData().getComplexData();

                if (complex.getContent() == null || complex.getContent().size() <= 0) {
                    throw new CstlServiceException("Missing data input value.", INVALID_PARAMETER_VALUE, inputIdentifier);

                } else {

                    try {
                        dataValue = WPSConvertersUtils.convertFromComplex(expectedClass, complex);
                    } catch (NonconvertibleObjectException ex) {
                        throw new CstlServiceException(ex.getMessage(), ex, NO_APPLICABLE_CODE);
                    }
                }
            }

            /**
             * Handle Literal input data.
             */
            if (isLiteral) {
                //Check if the expected class is supproted for literal using
                if (!WPSIO.isSupportedLiteralInputClass(expectedClass)) {
                    throw new CstlServiceException("Literal value expected", INVALID_PARAMETER_VALUE, inputIdentifier);
                }

                final LiteralDataType literal = inputRequest.getData().getLiteralData();
                final String data = literal.getValue();
                
                if (inputDescriptor.getUnit() != null) {
                    final Unit paramUnit = inputDescriptor.getUnit();
                    final Unit requestedUnit = Unit.valueOf(literal.getUom());
                    final UnitConverter converter = requestedUnit.getConverterTo(paramUnit);
                    dataValue = Double.valueOf(converter.convert(Double.valueOf(data)));
                    
                } else {
                    
                    try {
                        dataValue = WPSConvertersUtils.convertFromString(data, expectedClass);
                    } catch (NonconvertibleObjectException ex) {
                        throw new CstlServiceException(ex.getMessage(), ex, NO_APPLICABLE_CODE);
                    }
                }
            }

            try {
                in.parameter(inputIdentifierCode).setValue(dataValue);
            } catch (InvalidParameterValueException ex) {
                throw new CstlServiceException("Invalid data input value.", ex, INVALID_PARAMETER_VALUE, inputIdentifier);
            }
        }
    }

    /**
     * Fill outputs of the ProcessOutputs object using the process result, the list of requested outputs and the list of
     * process output desciptors.
     *
     * @param outputs
     * @param wantedOutputs
     * @param processOutputDesc
     * @param result
     * @param serviceURL
     * @throws CstlServiceException
     */
    public static void fillOutputsFromProcessResult(final ProcessOutputs outputs, final List<DocumentOutputDefinitionType> wantedOutputs,
            final List<GeneralParameterDescriptor> processOutputDesc, final ParameterValueGroup result, final String serviceURL) 
            throws CstlServiceException {
        if(result == null){
            throw new CstlServiceException("Empty process result.", NO_APPLICABLE_CODE);
        }
        
        for (final DocumentOutputDefinitionType outputsRequest : wantedOutputs) {

            if (outputsRequest.getIdentifier() == null || outputsRequest.getIdentifier().getValue() == null || outputsRequest.getIdentifier().getValue().isEmpty()) {
                throw new CstlServiceException("Empty output Identifier.", INVALID_PARAMETER_VALUE);
            }

            final String outputIdentifier = outputsRequest.getIdentifier().getValue();
            final String outputIdentifierCode = WPSUtils.extractProcessIOCode(outputIdentifier);

            //Check if it's a valid input identifier and hold it if found.
            ParameterDescriptor outputDescriptor = null;
            for (final GeneralParameterDescriptor processInput : processOutputDesc) {
                if (processInput.getName().getCode().equals(outputIdentifierCode) && processInput instanceof ParameterDescriptor) {
                    outputDescriptor = (ParameterDescriptor) processInput;
                    break;
                }
            }
            if (outputDescriptor == null) {
                throw new CstlServiceException("Invalid or unknow output identifier " + outputIdentifier + ".", INVALID_PARAMETER_VALUE, outputIdentifier);
            }

            //output value object.
            final Object outputValue = result.parameter(outputIdentifierCode).getValue();
            
            outputs.getOutput().add(createDocumentResponseOutput(outputDescriptor, outputsRequest, outputValue, serviceURL));

        }//end foreach wanted outputs

    }

    /**
     * Create {@link OutputDataType output} object for one requested output.
     *
     * @param outputIdentifier
     * @param outputDescriptor
     * @param requestedOutput
     * @param outputValue
     * @param serviceURL
     * @return
     * @throws CstlServiceException
     */
    public static OutputDataType createDocumentResponseOutput(final ParameterDescriptor outputDescriptor, final DocumentOutputDefinitionType requestedOutput,
            final Object outputValue, final String serviceURL) throws CstlServiceException {

        final OutputDataType outData = new OutputDataType();

        final String outputIdentifier = requestedOutput.getIdentifier().getValue();
        final String outputIdentifierCode = WPSUtils.extractProcessIOCode(outputIdentifier);

        //set Ouput informations
        outData.setIdentifier(new CodeType(outputIdentifier));
        
        //support custom title/abstract.
        final LanguageStringType titleOut = requestedOutput.getTitle() != null ? 
                requestedOutput.getTitle() : WPSUtils.capitalizeFirstLetter(outputIdentifierCode);
        final LanguageStringType abstractOut = requestedOutput.getAbstract() != null ? 
                requestedOutput.getAbstract() : WPSUtils.capitalizeFirstLetter(outputDescriptor.getRemarks().toString());
        outData.setTitle(titleOut);
        outData.setAbstract(abstractOut);

        final Class outClass = outputDescriptor.getValueClass(); // output class
        
        if (requestedOutput.isAsReference()) {
            final OutputReferenceType ref = createReferenceOutput(outClass, requestedOutput, outputValue, serviceURL);
            
            outData.setReference(ref);
        } else {
            
            final DataType data = new DataType();

            if (WPSIO.isSupportedBBoxOutputClass(outClass)) {
                org.opengis.geometry.Envelope envelop = (org.opengis.geometry.Envelope) outputValue;
                data.setBoundingBoxData(new BoundingBoxType(envelop));

            } else if (WPSIO.isSupportedComplexOutputClass(outClass)) {
                
                final Map<String, Object> parameters = new HashMap<String, Object>();
                parameters.put(AbstractComplexOutputConverter.OUT_DATA, outputValue);
                parameters.put(AbstractComplexOutputConverter.OUT_TMP_DIR_PATH, WPSUtils.getTempDirectoryPath());
                parameters.put(AbstractComplexOutputConverter.OUT_TMP_DIR_URL, WPSUtils.getTempDirectoryURL(serviceURL));
                parameters.put(AbstractComplexOutputConverter.OUT_ENCODING, requestedOutput.getEncoding());
                parameters.put(AbstractComplexOutputConverter.OUT_MIME, requestedOutput.getMimeType());
                parameters.put(AbstractComplexOutputConverter.OUT_SCHEMA, requestedOutput.getSchema());
                try {    
                
                    final ObjectConverter converter = WPSIO.getConverter(outClass, WPSIO.IOType.OUTPUT, WPSIO.FormChoice.COMPLEX, 
                        requestedOutput.getMimeType(), requestedOutput.getEncoding(), requestedOutput.getSchema());
                     
                    data.setComplexData((ComplexDataType) converter.convert(parameters));
                } catch (NonconvertibleObjectException ex) {
                    throw new CstlServiceException(ex.getMessage(), ex, NO_APPLICABLE_CODE);
                }

            } else if (WPSIO.isSupportedLiteralOutputClass(outClass)) {
                
                final LiteralDataType literal = new LiteralDataType();
                literal.setDataType(WPSUtils.getDataTypeString(outClass));
                literal.setValue(WPSConvertersUtils.convertToString(outputValue));
                if (outputDescriptor.getUnit() != null) {
                    literal.setUom(outputDescriptor.getUnit().toString());
                }
                data.setLiteralData(literal);
                
            } else {
                throw new CstlServiceException("Process output parameter invalid", MISSING_PARAMETER_VALUE, outputIdentifier);
            }
            
            outData.setData(data);
        }
        return outData;
    }
    
    /**
     * Create reference output.
     * 
     * @param clazz
     * @param requestedOutput
     * @param outputValue
     * @param serviceURL
     * @return
     * @throws CstlServiceException 
     */
    private static OutputReferenceType createReferenceOutput(final Class clazz, final DocumentOutputDefinitionType requestedOutput, 
            final Object outputValue, final String serviceURL) throws CstlServiceException {
        
        try {
            final Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put(AbstractReferenceOutputConverter.OUT_DATA, outputValue);
            parameters.put(AbstractReferenceOutputConverter.OUT_TMP_DIR_PATH, WPSUtils.getTempDirectoryPath());
            parameters.put(AbstractReferenceOutputConverter.OUT_TMP_DIR_URL, WPSUtils.getTempDirectoryURL(serviceURL));
            parameters.put(AbstractReferenceOutputConverter.OUT_ENCODING, requestedOutput.getEncoding());
            parameters.put(AbstractReferenceOutputConverter.OUT_MIME, requestedOutput.getMimeType());
            parameters.put(AbstractReferenceOutputConverter.OUT_SCHEMA, requestedOutput.getSchema());

            final ObjectConverter converter = WPSIO.getConverter(clazz, WPSIO.IOType.OUTPUT, WPSIO.FormChoice.REFERENCE, 
                    requestedOutput.getMimeType(), requestedOutput.getEncoding(), requestedOutput.getSchema());

            if (converter == null) {
                throw new CstlServiceException("Reference Output not supported, no converter found with " + WPSUtils.outputDefinitionToString(requestedOutput), 
                        OPERATION_NOT_SUPPORTED, requestedOutput.getIdentifier().getValue());
            }

            return (OutputReferenceType) converter.convert(parameters);

        } catch (NonconvertibleObjectException ex) {
            throw new CstlServiceException(ex.getMessage(), ex, NO_APPLICABLE_CODE);
        }
    }


    /**
     * Handle Raw output. 
     *
     * @param outputValue
     * @return
     * @throws CstlServiceException
     */
    private Object createRawOutput(final OutputDefinitionType rawData, final List<GeneralParameterDescriptor> processOutputDesc,
            final ParameterValueGroup result) throws CstlServiceException {

        final String outputIdentifier = rawData.getIdentifier().getValue();
        final String outputIdentifierCode = WPSUtils.extractProcessIOCode(outputIdentifier);

        //Check if it's a valid input identifier and hold it if found.
        ParameterDescriptor outputDescriptor = null;
        for (final GeneralParameterDescriptor processInput : processOutputDesc) {
            if (processInput.getName().getCode().equals(outputIdentifierCode) && processInput instanceof ParameterDescriptor) {
                outputDescriptor = (ParameterDescriptor) processInput;
                break;
            }
        }
        if (outputDescriptor == null) {
            throw new CstlServiceException("Invalid or unknow output identifier " + outputIdentifier + ".", INVALID_PARAMETER_VALUE, outputIdentifier);
        }

        //output value object.
        final Object outputValue = result.parameter(outputIdentifierCode).getValue();

        if (outputValue instanceof Geometry) {
            try {
                
                final Geometry jtsGeom = (Geometry) outputValue;
                final AbstractGeometryType gmlGeom = JTStoGeometry.toGML(jtsGeom);
                return gmlGeom;

            } catch (FactoryException ex) {
                throw new CstlServiceException(ex);
            }
        }

        if (outputValue instanceof Envelope) {
            return new BoundingBoxType((Envelope) outputValue);
        }
        return outputValue;
    }
}