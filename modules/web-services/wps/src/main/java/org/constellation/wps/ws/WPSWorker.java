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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.geotoolkit.process.ProcessException;
import org.geotoolkit.ows.xml.v110.AnyValue;
import org.geotoolkit.ows.xml.v110.BoundingBoxType;
import org.geotoolkit.ows.xml.v110.CodeType;
import org.geotoolkit.ows.xml.v110.LanguageStringType;
import org.geotoolkit.ows.xml.v110.AllowedValues;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.util.converter.NonconvertibleObjectException;
import org.geotoolkit.util.converter.ObjectConverter;
import org.geotoolkit.wps.xml.v100.ComplexDataType;
import org.geotoolkit.wps.xml.v100.DescribeProcess;
import org.geotoolkit.wps.xml.v100.Execute;
import org.geotoolkit.wps.xml.v100.ExecuteResponse;
import org.geotoolkit.wps.xml.v100.GetCapabilities;
import org.geotoolkit.wps.xml.v100.InputDescriptionType;
import org.geotoolkit.wps.xml.v100.InputType;
import org.geotoolkit.wps.xml.v100.LiteralDataType;
import org.geotoolkit.wps.xml.v100.LiteralInputType;
import org.geotoolkit.wps.xml.v100.OutputDescriptionType;
import org.geotoolkit.wps.xml.v100.ProcessDescriptionType;
import org.geotoolkit.wps.xml.v100.ProcessDescriptions;
import org.geotoolkit.wps.xml.v100.WPSCapabilitiesType;
import org.geotoolkit.wps.xml.v100.ProcessOfferings;
import org.geotoolkit.xml.MarshallerPool;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.wps.xml.v100.DataType;
import org.geotoolkit.wps.xml.v100.OutputDataType;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.wps.xml.v100.CRSsType;
import org.geotoolkit.wps.xml.v100.SupportedCRSsType;
import org.geotoolkit.gml.xml.v311.AbstractGeometryType;
import org.geotoolkit.wps.xml.v100.DocumentOutputDefinitionType;
import org.geotoolkit.wps.xml.v100.OutputDefinitionType;
import org.geotoolkit.wps.xml.v100.OutputDefinitionsType;
import org.geotoolkit.wps.xml.v100.ResponseDocumentType;
import org.geotoolkit.wps.xml.v100.ResponseFormType;
import org.geotoolkit.wps.xml.v100.StatusType;
import org.geotoolkit.wps.xml.v100.ProcessStartedType;
import org.geotoolkit.geometry.isoonjts.GeometryUtils;
import org.geotoolkit.geometry.jts.JTSEnvelope2D;
import org.geotoolkit.gml.JTStoGeometry;
import org.geotoolkit.wps.xml.WPSMarshallerPool;
import org.geotoolkit.process.ProcessingRegistry;
import org.geotoolkit.wps.xml.v100.LiteralOutputType;
import org.geotoolkit.wps.xml.v100.*;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.util.FactoryException;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.NoSuchIdentifierException;

import org.constellation.wps.utils.WPSUtils;
import org.constellation.ServiceDef;
import org.constellation.ws.AbstractWorker;
import org.constellation.ws.CstlServiceException;
import org.constellation.configuration.Process;
import org.constellation.configuration.ProcessContext;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.configuration.ProcessFactory;
import static org.constellation.api.QueryConstants.*;
import static org.constellation.api.CommonConstants.*;
import static org.constellation.wps.ws.WPSConstant.*;

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
        ProcessContext candidate          = null;
        if (configurationDirectory != null) {
            final File lcFile = new File(configurationDirectory, "processContext.xml");
            if (lcFile.exists()) {
                Unmarshaller unmarshaller = null;
                try {
                    unmarshaller = GenericDatabaseMarshallerPool.getInstance().acquireUnmarshaller();
                    Object obj   = unmarshaller.unmarshal(lcFile);
                    if (obj instanceof ProcessContext) {
                        candidate = (ProcessContext) obj;
                        isStarted = true;
                    } else {
                        startError = "The process context File does not contain a ProcessContext object";
                        isStarted  = false;
                        LOGGER.log(Level.WARNING, startError);
                    }
                } catch (JAXBException ex) {
                    startError = "JAXBExeception while unmarshalling the process context File";
                    isStarted  = false;
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
            isStarted  = false;
            LOGGER.log(Level.WARNING, "\nThe worker ({0}) is not working!\nCause: " + startError, id);
        }
        this.context = candidate;
        fillProcessList();
        
        if (isStarted) {
            LOGGER.log(Level.INFO, "WPS worker {0} running", id);
        }
    }
    
    private void fillProcessList() {
        if (context != null) {
            // Load all process from all factory
            if (Boolean.TRUE == context.getProcesses().getLoadAll()) {
                final Iterator<ProcessingRegistry> factoryIte = ProcessFinder.getProcessFactories();
                while (factoryIte.hasNext()) {
                    final ProcessingRegistry factory = factoryIte.next();
                    for (final ProcessDescriptor descriptor : factory.getDescriptors()) {
                        ProcessDescriptorList.add(descriptor);
                    }
                }
            } else {
                for (ProcessFactory processFactory : context.getProcessFactories()) {
                    final ProcessingRegistry factory = ProcessFinder.getProcessFactory(processFactory.getAutorityCode());
                    if (factory != null) {
                        if (Boolean.TRUE == processFactory.getLoadAll()) {
                            for (final ProcessDescriptor descriptor : factory.getDescriptors()) {
                                ProcessDescriptorList.add(descriptor);
                            }
                        } else {
                            for (Process process : processFactory.getInclude().getProcess()) {
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
            throw new CstlServiceException("The parameter "+ SERVICE_PARAMETER +" must be specified as WPS",
                    INVALID_PARAMETER_VALUE, SERVICE_PARAMETER.toLowerCase());
        }
        
        //check LANGUAGE=en-EN
        if(request.getLanguage() != null && !request.getLanguage().equalsIgnoreCase(WPS_LANG)){
             throw new CstlServiceException("The specified "+ LANGUAGE_PARAMETER +" is not handled by the service. ",
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
            throw new CstlServiceException("The parameter "+ SERVICE_PARAMETER +" must be specified as WPS",
                    INVALID_PARAMETER_VALUE, SERVICE_PARAMETER.toLowerCase());
        }
        
        //check LANGUAGE=en-EN
        if(request.getLanguage() != null && !request.getLanguage().equalsIgnoreCase(WPS_LANG)){
            throw new CstlServiceException("The specified "+ LANGUAGE_PARAMETER +" is not handled by the service. ",
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
                throw new CstlServiceException("Process "+ identifier.getValue() +" not supported by the service.",
                        INVALID_PARAMETER_VALUE, IDENTIFER_PARAMETER.toLowerCase());
            }
            
            final ProcessDescriptionType descriptionType = new ProcessDescriptionType();
            descriptionType.setIdentifier(identifier);          //Process Identifier
            descriptionType.setTitle(WPSUtils.capitalizeFirstLetter(processDesc.getIdentifier().getCode()));                //Process Title
            descriptionType.setAbstract(WPSUtils.capitalizeFirstLetter(processDesc.getProcedureDescription().toString()));  //Process abstract
            descriptionType.setProcessVersion(WPS_1_0_0);
            descriptionType.setWSDL(null);                      //TODO WSDL
            descriptionType.setStatusSupported(false);          //TODO support process status
            descriptionType.setStoreSupported(false);           //TODO support process storage

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
                    if (clazz.equals(Envelope.class)) {
                        in.setBoundingBoxData(WPS_SUPPORTED_CRS);

                        //Complex type (XML, ...)     
                    } else if (WPSIO.isSupportedComplexInputClass(clazz)) {
                        in.setComplexData(WPSUtils.describeComplex(clazz, WPSIO.IOType.INPUT));

                        //Simple object (Integer, double, ...) and Object which need a conversion from String like affineTransform or WKT Geometry
                    } else if(WPSIO.isSupportedLiteralInputClass(clazz)){
                        final LiteralInputType literal = new LiteralInputType();

                        if (paramDesc.getDefaultValue() != null) {
                            literal.setDefaultValue(paramDesc.getDefaultValue().toString()); //default value if enable
                        }
                        
                        if(paramDesc.getUnit() != null){
                            literal.setUOMs(WPSUtils.generateUOMs(paramDesc));
                        }
                        //AllowedValues setted
                        if(paramDesc.getValidValues() != null && !paramDesc.getValidValues().isEmpty()){
                            literal.setAllowedValues(new AllowedValues(paramDesc.getValidValues()));
                        }else{
                            literal.setAnyValue(new AnyValue());
                        }
                        literal.setValuesReference(null);
                        literal.setDataType(WPSUtils.createDataType(clazz));
                        

                        in.setLiteralData(literal);
                        
                    }else{
                        throw new CstlServiceException("Process input not supported.", NO_APPLICABLE_CODE);
                    }
                    
                    dataInputs.getInput().add(in);
                } else {
                    throw new CstlServiceException("Process parameter invalid", NO_APPLICABLE_CODE);
                }
            }
            if(!dataInputs.getInput().isEmpty()){
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
                    if (clazz.equals(JTSEnvelope2D.class)) {
                        out.setBoundingBoxOutput(WPS_SUPPORTED_CRS);

                        //Complex type (XML, raster, ...)
                    } else if (WPSIO.isSupportedComplexOutputClass(clazz)) {
                        out.setComplexOutput((SupportedComplexDataInputType) WPSUtils.describeComplex(clazz, WPSIO.IOType.OUTPUT));

                        //Simple object (Integer, double) and Object which need a conversion from String like affineTransform or Geometry
                    } else if (WPSIO.isSupportedLiteralOutputClass(clazz)) {

                        final LiteralOutputType literal = new LiteralOutputType();
                        literal.setDataType(WPSUtils.createDataType(clazz));
                        if(paramDesc.getUnit() != null){
                            literal.setUOMs(WPSUtils.generateUOMs(paramDesc));
                        }

                        out.setLiteralOutput(literal);
                    }else{
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
            throw new CstlServiceException("The parameter "+ SERVICE_PARAMETER +" must be specified as WPS",
                    INVALID_PARAMETER_VALUE, SERVICE_PARAMETER.toLowerCase());
        }
        
        //check LANGUAGE=en-EN
        if(request.getLanguage() != null && !request.getLanguage().equalsIgnoreCase(WPS_LANG)){
            throw new CstlServiceException("The specified "+ LANGUAGE_PARAMETER +" is not handled by the service. ",
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
        if (request.getIdentifier() == null ||  request.getIdentifier().getValue() == null ||request.getIdentifier().getValue().isEmpty()) {
            throw new CstlServiceException("The parameter " + IDENTIFER_PARAMETER + " must be specified.",
                    MISSING_PARAMETER_VALUE, IDENTIFER_PARAMETER.toLowerCase());
        }
        
        final StatusType status = new StatusType();
        LOGGER.log(Level.INFO, "Process Execute : {0}", request.getIdentifier().getValue());
        //Find the process
        final ProcessDescriptor processDesc = WPSUtils.getProcessDescriptor(request.getIdentifier().getValue());

        if (!WPSUtils.isSupportedProcess(processDesc)) {
            throw new CstlServiceException("Process "+ request.getIdentifier().getValue() +" not supported by the service.",
                        INVALID_PARAMETER_VALUE, IDENTIFER_PARAMETER.toLowerCase());
        }

        //check requested INPUT/OUTPUT. Throw an CstlException otherwise.
        WPSUtils.checkValidInputOuputRequest(processDesc, request);
        
        //status.setProcessAccepted("Process "+request.getIdentifier().getValue()+" found.");

        boolean isOutputRaw = false; // the default output is a ResponseDocument

        /*
         * Get the requested output form
         */
        final ResponseFormType responseForm = request.getResponseForm();
        final OutputDefinitionType rawData = responseForm.getRawDataOutput();
        final ResponseDocumentType respDoc = responseForm.getResponseDocument();

        /*
         * Raw output data attributs
         */
        String rawOutputID = null;
        String rawOutputMime = null;
        String rawOutputEncoding = null;
        String rawOutputSchema = null;
        String rawOutputUom = null;

        /*
         * ResponseDocument attributs
         */
        boolean isLineage = false;
        boolean useStatus = false;
        boolean useStorage = false;
        ExecuteResponse response = null;
        List<DocumentOutputDefinitionType> wantedOutputs = null;

        /*
         * Raw Data
         */
        if (rawData != null) {
            isOutputRaw = true;
            rawOutputID = rawData.getIdentifier().getValue();
            rawOutputMime = rawData.getMimeType();
            rawOutputEncoding = rawData.getEncoding();
            rawOutputSchema = rawData.getSchema();
            rawOutputUom = rawData.getUom();

            /*
             * ResponseDocument
             */
        } else if (respDoc != null) {

            isLineage = respDoc.isLineage();
            useStatus = respDoc.isStatus();
            useStorage = respDoc.isStoreExecuteResponse();

            //outputs
            wantedOutputs = respDoc.getOutput();

            response = new ExecuteResponse();
            response.setService(WPS_SERVICE);
            response.setVersion(WPS_1_0_0);
            response.setLang(WPS_LANG);
            response.setServiceInstance(getServiceUrl());

            //Give a bief process description into the execute response
            response.setProcess(WPSUtils.generateProcessBrief(processDesc));
            
            LOGGER.log(Level.INFO, "Request : [Lineage=" + isLineage + ", Storage=" + useStorage + ", Status=" + useStatus +"]");

            if (isLineage) {
                //Inputs
                response.setDataInputs(request.getDataInputs());
                final OutputDefinitionsType outputsDef = new OutputDefinitionsType();
                outputsDef.getOutput().addAll(respDoc.getOutput());
                //Outputs
                response.setOutputDefinitions(outputsDef);
            }

            if (useStorage) {
                response.setStatusLocation(null); //Output data URL
            }

            if (useStatus) {
                response.setStatus(status);
            }
        } else {
            throw new CstlServiceException("ResponseFrom element not present. This should be a RawData or a ResponseDocument", MISSING_PARAMETER_VALUE );
        }
        
        //Input temporary files used by the process. In order to delete them at the end of the process.
        List<File> files = null;

        //Create Process and Inputs
        final ParameterValueGroup in = processDesc.getInputDescriptor().createValue();

        ///////////////////////
        //   Process INPUT
        //////////////////////
        List<InputType> requestInputData = new ArrayList<InputType>();
        if(request.getDataInputs() != null && request.getDataInputs().getInput() != null){
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

        //Each input from the request
        for (final InputType inputRequest : requestInputData) {

            if (inputRequest.getIdentifier() == null || inputRequest.getIdentifier().getValue() == null || inputRequest.getIdentifier().getValue().isEmpty()) {
                throw new CstlServiceException("Missing input Identifier.", INVALID_PARAMETER_VALUE);
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
                throw new CstlServiceException("Invalid or unknow input Identifier.", INVALID_PARAMETER_VALUE, inputIdentifier);
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
            LOGGER.log(Level.INFO, "Input : " + inputIdentifier + " : expected Class " + expectedClass.getCanonicalName());

            
            /**
             * Handle referenced input data.
             */
            if (isReference) {
                
                //Check if the expected class is supproted for literal using
                if (!WPSIO.isSupportedReferenceInputClass(expectedClass)) {
                    throw new CstlServiceException("Reference value expected", INVALID_PARAMETER_VALUE, inputIdentifier);
                }

                LOGGER.log(Level.INFO,"LOG -> Input -> Reference");
                final String href = inputRequest.getReference().getHref();
                final String method = inputRequest.getReference().getMethod();
                final String mime = inputRequest.getReference().getMimeType();
                final String encoding = inputRequest.getReference().getEncoding();
                final String schema = inputRequest.getReference().getSchema();

                dataValue = WPSUtils.reachReferencedData(href, method, mime, encoding, schema, expectedClass, inputIdentifier);
                if (dataValue instanceof FeatureCollection) {
                    dataValue = (FeatureCollection) dataValue;
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
                LOGGER.log(Level.INFO, "LOG -> Input -> Boundingbox");
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

                LOGGER.log(Level.INFO, "LOG -> Input -> Complex");

                final ComplexDataType complex = inputRequest.getData().getComplexData();
                final String mime = complex.getMimeType();
                final String encoding = complex.getEncoding();
                final List<Object> content = complex.getContent();
                final String schema = complex.getSchema();

                if (content.size() <= 0) {
                    throw new CstlServiceException("Missing data input value.", INVALID_PARAMETER_VALUE, inputIdentifier);

                } else {

                    final List<Object> inputObject = new ArrayList<Object>();
                    for (final Object obj : content) {
                        if (obj != null) {
                            if (!(obj instanceof String)) {
                                inputObject.add(obj);
                            }
                        }
                    }

                    if (inputObject == null) {
                        throw new CstlServiceException("Invalid data input value : Empty value.", INVALID_PARAMETER_VALUE, inputIdentifier);
                    }

                    /*
                     * Extract Data from inputObject array
                     */
                    dataValue = WPSUtils.extractComplexInput(expectedClass, inputObject, schema, mime, encoding, inputIdentifier);
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

                LOGGER.log(Level.INFO, "LOG -> Input -> Literal");

                final LiteralDataType literal = inputRequest.getData().getLiteralData();
                final String data = literal.getValue();

                //convert String into expected type
                dataValue = WPSUtils.convertFromString(data, expectedClass);
                LOGGER.log(Level.INFO, "DEBUG -> Input -> Literal -> Value={0}", dataValue);
            }

            try {
                in.parameter(inputIdentifierCode).setValue(dataValue);
            } catch (InvalidParameterValueException ex) {
                throw new CstlServiceException("Invalid data input value.", ex, INVALID_PARAMETER_VALUE, inputIdentifier);
            }
        }

        //Give input parameter to the process
        final org.geotoolkit.process.Process process = processDesc.createProcess(in);

        //Status
        final ProcessStartedType started = new ProcessStartedType();
        started.setValue("Process " + request.getIdentifier().getValue() + " is started");
        started.setPercentCompleted(0);
        //status.setProcessStarted(started);

        //Run the process
        final ParameterValueGroup result;
        try {
            result = process.call();
        } catch (ProcessException ex) {
            //TODO handle process failed.
            throw new CstlServiceException("Process execution failed", ex, null);
        }

        ///////////////////////
        //   Process OUTPUT
        //////////////////////
        /*
         * Storage data
         */
        if (useStorage) {
            //TODO storage output
            throw new UnsupportedOperationException("Output storage not yet implemented");
            /*
             * No strorage
             */
        } else {
            /*
             * Raw Data returned
             */
            if (isOutputRaw) {
                LOGGER.log(Level.INFO, "LOG -> Output -> Raw");
                final Object outputValue = result.parameter(rawOutputID).getValue();
                LOGGER.log(Level.INFO, "DEBUG -> Output -> Raw -> Value={0}", outputValue);

                if (outputValue instanceof Geometry) {
                    try {
                        final Geometry jtsGeom = (Geometry) outputValue;
                        final AbstractGeometryType gmlGeom = JTStoGeometry.toGML(jtsGeom);
                        return gmlGeom;
                    } catch (NoSuchAuthorityCodeException ex) {
                        throw new CstlServiceException(ex);
                    } catch (FactoryException ex) {
                        throw new CstlServiceException(ex);
                    }
                }

                if (outputValue instanceof Envelope) {
                    return new BoundingBoxType((Envelope) outputValue);
                }
                return outputValue;

                /*
                 * DocumentResponse returned
                 */
            } else {
                LOGGER.log(Level.INFO,"LOG -> Output -> Document");
                final ExecuteResponse.ProcessOutputs outputs = new ExecuteResponse.ProcessOutputs();
                //Process Outputs
                for (final GeneralParameterDescriptor outputDescriptor : processDesc.getOutputDescriptor().descriptors()) {

                    final OutputDataType outData = new OutputDataType();

                    //set Ouput informations
                    final String outputIdentifier = outputDescriptor.getName().getCode();
                    outData.setIdentifier(new CodeType(outputIdentifier));
                    outData.setTitle(new LanguageStringType(outputIdentifier));
                    outData.setAbstract(new LanguageStringType(outputDescriptor.getRemarks().toString()));

                    /*
                     * Output value from process
                     */
                    final Object outputValue = result.parameter(outputIdentifier).getValue();

                    final DataType data = new DataType();
                    if (outputDescriptor instanceof ParameterDescriptor) {

                        final ParameterDescriptor outParamDesc = (ParameterDescriptor) outputDescriptor;
                        /*
                         * Output Class
                         */
                        final Class outClass = outParamDesc.getValueClass();

                        /*
                         * Bounding Box
                         */
                        if (outClass.equals(Envelope.class)) {
                            LOGGER.log(Level.INFO,"LOG -> Output -> BoundingBox");
                            org.opengis.geometry.Envelope envelop = (org.opengis.geometry.Envelope) outputValue;

                            data.setBoundingBoxData(new BoundingBoxType(envelop));

                            /*
                             * Complex
                             */
                        } else if (WPSIO.isSupportedComplexOutputClass(outClass)) {
                            LOGGER.log(Level.INFO,"LOG -> Output -> Complex");
                            final ComplexDataType complex = new ComplexDataType();

                            for (final DocumentOutputDefinitionType wO : wantedOutputs) {
                                final String wantedOutputIdentifier = WPSUtils.extractProcessIOCode(wO.getIdentifier().getValue());
                                if (outputIdentifier.equals(wantedOutputIdentifier)) {
                                    complex.setEncoding(wO.getEncoding());
                                    complex.setMimeType(wO.getMimeType());
                                    complex.setSchema(wO.getSchema());
                                }
                            }

                            final ObjectConverter converter = WPSIO.getConverter(outClass, WPSIO.IOType.OUTPUT, WPSIO.DataType.COMPLEX, complex.getMimeType());
                            
                            if (converter == null) {
                                throw new CstlServiceException("Input complex not supported, no converter found.",
                                        OPERATION_NOT_SUPPORTED, outputIdentifier);
                            }

                            try {
                                complex.getContent().addAll((Collection<Object>) converter.convert(outputValue));
                            } catch (NonconvertibleObjectException ex) {
                                throw new CstlServiceException(ex, INVALID_PARAMETER_VALUE, outputIdentifier);
                            }

                            data.setComplexData(complex);

                            /*
                             * Literal
                             */
                        } else if (WPSIO.isSupportedLiteralOutputClass(outClass)) {
                            LOGGER.log(Level.INFO,"LOG -> Output -> Literal");
                            final LiteralDataType literal = new LiteralDataType();
                            literal.setDataType(outClass.getCanonicalName());
                            if (outputValue == null) {
                                literal.setValue(null);
                            } else {
                                literal.setValue(outputValue.toString());
                            }
                            data.setLiteralData(literal);

                        } else {
                            throw new CstlServiceException("Process output parameter invalid", OPERATION_NOT_SUPPORTED, outputIdentifier);
                        }
                    } else {
                        throw new CstlServiceException("Process output parameter invalid", OPERATION_NOT_SUPPORTED, outputIdentifier);
                    }

                    outData.setData(data);
                    outputs.getOutput().add(outData);
                }

                response.setProcessOutputs(outputs);

                if (useStatus) {
                    response.setStatus(new StatusType());
                }
                status.setProcessSucceeded("Process " + request.getIdentifier().getValue() + " finiched.");
                if (useStatus) {
                    response.setStatus(status);
                }

                //Delete input temporary files 
                if (files != null) {
                    for (final File f : files) {
                        f.delete();
                    }
                }

                return response;
            }
        }
    }
}