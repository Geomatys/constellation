/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
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

import java.util.Collection;
import com.vividsolutions.jts.geom.Geometry;

import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.measure.unit.Unit;
import javax.xml.bind.JAXBException;

import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.converters.StringToAffineTransformConverter;
import org.geotoolkit.process.converters.StringToCRSConverter;
import org.geotoolkit.process.converters.StringToFilterConverter;
import org.geotoolkit.process.converters.StringToGeometryConverter;
import org.geotoolkit.process.converters.StringToSortByConverter;
import org.geotoolkit.process.converters.StringToUnitConverter;
import org.geotoolkit.util.collection.UnmodifiableArrayList;
import org.geotoolkit.ows.xml.v110.AnyValue;
import org.geotoolkit.ows.xml.v110.BoundingBoxType;
import org.geotoolkit.ows.xml.v110.CodeType;
import org.geotoolkit.ows.xml.v110.DomainMetadataType;
import org.geotoolkit.ows.xml.v110.LanguageStringType;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.process.converters.StringToFeatureCollectionConverter;
import org.geotoolkit.util.converter.ConverterRegistry;
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
import org.geotoolkit.wps.xml.v100.ProcessBriefType;
import org.geotoolkit.wps.xml.v100.ProcessDescriptionType;
import org.geotoolkit.wps.xml.v100.ProcessDescriptions;
import org.geotoolkit.wps.xml.v100.WPSCapabilitiesType;
import org.geotoolkit.wps.xml.v100.ProcessOfferings;
import org.geotoolkit.xml.MarshallerPool;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.memory.GenericExtendFeatureIterator.FeatureExtend;
import org.geotoolkit.wps.xml.v100.ComplexDataCombinationType;
import org.geotoolkit.wps.xml.v100.ComplexDataCombinationsType;
import org.geotoolkit.wps.xml.v100.ComplexDataDescriptionType;
import org.geotoolkit.wps.xml.v100.DataType;
import org.geotoolkit.wps.xml.v100.OutputDataType;
import org.geotoolkit.wps.xml.v100.SupportedComplexDataInputType;
import org.geotoolkit.wps.xml.v100.SupportedComplexDataType;
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
import org.geotoolkit.data.FeatureIterator;
import org.geotoolkit.feature.FeatureTypeBuilder;
import org.geotoolkit.feature.type.DefaultFeatureType;
import org.geotoolkit.feature.type.DefaultGeometryType;
import org.geotoolkit.feature.type.DefaultPropertyDescriptor;
import org.geotoolkit.geometry.isoonjts.GeometryUtils;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.geometry.jts.JTSEnvelope2D;
import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.gml.JTStoGeometry;
import org.geotoolkit.process.converters.StringToNumberRangeConverter;
import org.geotoolkit.util.NumberRange;
import org.geotoolkit.wps.xml.WPSMarshallerPool;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.sort.SortBy;
import org.opengis.parameter.InvalidParameterValueException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.util.FactoryException;
import org.opengis.feature.Property;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.constellation.ServiceDef;
import org.constellation.wps.converters.ComplexToFeatureArrayConverter;
import org.constellation.wps.converters.ComplexToFeatureCollectionArrayConverter;
import org.constellation.wps.converters.ComplexToFeatureCollectionConverter;
import org.constellation.wps.converters.ComplexToFeatureConverter;
import org.constellation.wps.converters.ComplexToFeatureTypeConverter;
import org.constellation.wps.converters.ComplexToGeometryArrayConverter;
import org.constellation.wps.converters.ComplexToGeometryConverter;
import org.constellation.wps.converters.FeatureCollectionToComplexConverter;
import org.constellation.wps.converters.FeatureToComplexConverter;
import org.constellation.wps.converters.GeometryArrayToComplexConverter;
import org.constellation.wps.converters.GeometryToComplexConverter;
import org.constellation.wps.converters.ReferenceToFeatureCollectionConverter;
import org.constellation.wps.converters.ReferenceToFeatureConverter;
import org.constellation.wps.converters.ReferenceToFeatureTypeConverter;
import org.constellation.wps.converters.ReferenceToFileConverter;
import org.constellation.wps.converters.ReferenceToGeometryConverter;
import org.constellation.wps.converters.ReferenceToGridCoverage2DConverter;
import org.constellation.wps.converters.ReferenceToGridCoverageReaderConverter;
import org.constellation.ws.AbstractWorker;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.process.ProcessingRegistry;
import org.geotoolkit.wps.xml.v100.LiteralOutputType;
import org.geotoolkit.wps.xml.v100.SupportedUOMsType;
import org.geotoolkit.wps.xml.v100.UOMsType;

import org.opengis.util.NoSuchIdentifierException;
import static org.constellation.api.QueryConstants.*;
import static org.constellation.wps.ws.WPSConstant.*;

/**
 * WPS worker.Compute response of getCapabilities, DescribeProcess and Execute requests. 
 * 
 * @author Quentin Boileau
 */
public class WPSWorker extends AbstractWorker {

    /**
     * List of literal converters. Used to convert a String to an Object like
     * AffineTransform, Coodinate Reference System, ...
     */
    private static final List LITERAL_CONVERTERS = UnmodifiableArrayList.wrap(
            StringToFeatureCollectionConverter.getInstance(),
            StringToUnitConverter.getInstance(),
            StringToGeometryConverter.getInstance(),
            StringToCRSConverter.getInstance(),
            StringToAffineTransformConverter.getInstance(),
            StringToFilterConverter.getInstance(),
            StringToSortByConverter.getInstance(),
            StringToNumberRangeConverter.getInstance());
    
    /**
     * List of reference converters.Used to extract an Object from a Reference like an URL.
     * For example to a Feature or FeatureCollection, to a Geometry or a coverage.
     */
    private static final List REFERENCE_CONVERTERS = UnmodifiableArrayList.wrap(
            ReferenceToFeatureCollectionConverter.getInstance(),
            ReferenceToFeatureConverter.getInstance(),
            ReferenceToFeatureTypeConverter.getInstance(),
            ReferenceToFileConverter.getInstance(),
            ReferenceToGeometryConverter.getInstance(),
            ReferenceToGridCoverage2DConverter.getInstance(),
            ReferenceToGridCoverageReaderConverter.getInstance());

    /**
     * List of complex converters
     */
    private static final List COMPLEX_CONVERTERS = UnmodifiableArrayList.wrap(
            ComplexToFeatureCollectionConverter.getInstance(),
            ComplexToFeatureCollectionArrayConverter.getInstance(),
            ComplexToFeatureConverter.getInstance(),
            ComplexToFeatureArrayConverter.getInstance(),
            ComplexToFeatureTypeConverter.getInstance(),
            ComplexToGeometryConverter.getInstance(),
            ComplexToGeometryArrayConverter.getInstance());
    
    /**
     * List of output complex converters. They used to convert an output from a process
     * into a complex WPS output like GML.
     */
    private static final List OUTPUT_COMPLEX_CONVERTERS = UnmodifiableArrayList.wrap(
           GeometryToComplexConverter.getInstance(),
           GeometryArrayToComplexConverter.getInstance(),
           FeatureToComplexConverter.getInstance(),
           FeatureCollectionToComplexConverter.getInstance());
  
     /**
     * List of supported <b>input</b> Class for a Complex <b>input</b>.
     */
    private static final List COMPLEX_INPUT_TYPE_LIST = UnmodifiableArrayList.wrap(
            Feature.class,
            FeatureCollection.class,
            Feature[].class,
            FeatureCollection[].class,
            FeatureType.class,
            Geometry.class);

    /**
     * List of supported <b>output</b> Class for a Complex <b>output</b>.
     */
    private static final List COMPLEX_OUTPUT_TYPE_LIST = UnmodifiableArrayList.wrap(
            Feature.class,
            FeatureCollection.class,
            Geometry.class,
            Geometry[].class);
    
    /**
     * List of supported <b>input</b> Class for a Literal <b>input</b>.
     */
    private static final List LITERAL_INPUT_TYPE_LIST = UnmodifiableArrayList.wrap(
            Number.class,Boolean.class,String.class,
            Unit.class,
            AffineTransform.class,
            org.opengis.filter.Filter.class,
            CoordinateReferenceSystem.class,
            SortBy[].class,
            NumberRange[].class);
    
    /**
     * List of supported <b>output</b> Class for a Literal <b>output</b>.
     */
    private static final List LITERAL_OUPUT_TYPE_LIST = UnmodifiableArrayList.wrap(
            Number.class,Boolean.class,String.class,
            Unit.class,
            AffineTransform.class,
            CoordinateReferenceSystem.class);

    /*
     * List of supported <b>input</b> Class for a Reference <b>input</b>.
     */
    private static final List REFERENCE_INPUT_TYPE_LIST = UnmodifiableArrayList.wrap(
            Feature.class,
            FeatureCollection.class,
            Geometry.class,
            File.class,
            FeatureType.class,
            FeatureExtend.class,
            GridCoverage2D.class,
            GridCoverageReader.class);
    
    /*
     * List of supported <b>output</b> Class for a Reference <b>output</b>.
     */
    private static final List REFERENCE_OUTPUT_TYPE_LIST = UnmodifiableArrayList.wrap();
    
    /*
     * A list supported CRS
     */
    private static final List<String> SUPPORTED_CRS = new ArrayList<String>();

    public WPSWorker(final String id, final File configurationDirectory) {
        super(id, configurationDirectory, ServiceDef.Specification.WPS);
        isStarted = true;
        if (isStarted) {
            LOGGER.log(Level.INFO, "WPS worker {0} running", id);
        }
        initSupportedCRS();
    }

    @Override
    protected MarshallerPool getMarshallerPool() {
        return WPSMarshallerPool.getInstance();
    }

    @Override
    public void destroy() {
    }

    /**
     * GetCapabilities request
     * @param request
     * @return
     * @throws CstlServiceException
     */
    public WPSCapabilitiesType getCapabilities(GetCapabilities request) throws CstlServiceException {
        isWorking();

        final String service = request.getService();

        if (!(service.equalsIgnoreCase(WPS_SERVICE))) {
            throw new CstlServiceException("The version number specified for this request "
                    + "is not handled.", VERSION_NEGOTIATION_FAILED, VERSION_PARAMETER.toLowerCase());
        }
        
        List<String> versionsAccepted = new ArrayList<String>();
        if(request.getAcceptVersions() == null){
            versionsAccepted.add(ServiceDef.WPS_1_0_0.version.toString());
        }else{
            versionsAccepted = request.getAcceptVersions().getVersion();
        }
        
        boolean versionSupported = false;
        for (String version : versionsAccepted) {
            if(version.equals(ServiceDef.WPS_1_0_0.version.toString())){
                versionSupported = true;
            }
        }
        
        if(versionSupported){
           return getCapabilities100((org.geotoolkit.wps.xml.v100.GetCapabilities) request);
        } else {
            throw new CstlServiceException("The version number specified for this request "
                    + "is not handled.", VERSION_NEGOTIATION_FAILED, VERSION_PARAMETER.toLowerCase());
        }
        
    }

    /**
     * GetCapabilities request for WPS 1.0.0
     * @param request
     * @return
     * @throws CstlServiceException
     */
    private WPSCapabilitiesType getCapabilities100(GetCapabilities request) throws CstlServiceException {

        // We unmarshall the static capabilities document.
        final WPSCapabilitiesType staticCapabilities;
        try {
            staticCapabilities = (WPSCapabilitiesType) getStaticCapabilitiesObject(ServiceDef.WPS_1_0_0.version.toString(), ServiceDef.Specification.WPS.toString());
        } catch (JAXBException ex) {
            throw new CstlServiceException(ex);
        }

        final WPSCapabilitiesType capabilities = new WPSCapabilitiesType();
        capabilities.setService(WPS_SERVICE);
        capabilities.setServiceIdentification(staticCapabilities.getServiceIdentification());
        capabilities.setServiceProvider(staticCapabilities.getServiceProvider());
        
        final ProcessOfferings offering = new ProcessOfferings();

        final Iterator<ProcessingRegistry> factoryIte = ProcessFinder.getProcessFactories();

        while (factoryIte.hasNext()) {

            final ProcessingRegistry factory = factoryIte.next();
            ProcessBriefType brief = new ProcessBriefType();

            for (ProcessDescriptor descriptor : factory.getDescriptors()) {
                
                if(isSupportedProcess(descriptor)){
                    brief = processBrief(descriptor);
                    offering.getProcess().add(brief);
                }
            }
        }

        capabilities.setProcessOfferings(offering);
        return capabilities;
    }

    /*
     *  DiscribeProcess request
     */
    
    /**
     * Describe process request
     * @param request
     * @return
     * @throws CstlServiceException
     */
    public ProcessDescriptions describeProcess(DescribeProcess request) throws CstlServiceException {
        isWorking();

        final String service = request.getService();
        if (!(service.equalsIgnoreCase(WPS_SERVICE))) {
            throw new CstlServiceException("The version number specified for this request "
                    + "is not handled.", VERSION_NEGOTIATION_FAILED, VERSION_PARAMETER.toLowerCase());
        }

        final String version = request.getVersion().toString();

        if (version.isEmpty()) {
            throw new CstlServiceException("The parameter VERSION must be specified.",
                    MISSING_PARAMETER_VALUE, VERSION_PARAMETER.toLowerCase());
        }

        if (version.equals(ServiceDef.WPS_1_0_0.version.toString())) {
            return describeProcess100((org.geotoolkit.wps.xml.v100.DescribeProcess) request);
        } else {
            throw new CstlServiceException("The version number specified for this discribeProcess request "
                    + "is not handled.", NO_APPLICABLE_CODE, VERSION_PARAMETER.toLowerCase());
        }
    }

    /**
     * Describe a process in WPS v1.0.0
     * @param request
     * @return
     * @throws CstlServiceException
     */
    private ProcessDescriptions describeProcess100(DescribeProcess request) throws CstlServiceException {

        if (request.getIdentifier().isEmpty()) {
            throw new CstlServiceException("The parameter Identifier must be specified.",
                    MISSING_PARAMETER_VALUE, "Identifier");
        }
        
        final ProcessDescriptions descriptions = new ProcessDescriptions();
        descriptions.setLang(WPS_LANG);
        descriptions.setService(WPS_SERVICE);
        descriptions.setVersion(WPS_1_0_0);
        
        for (CodeType identifier : request.getIdentifier()) {
            
            final ProcessDescriptionType descriptionType = new ProcessDescriptionType();
            descriptionType.setIdentifier(identifier);          //Process Identifier

            descriptionType.setProcessVersion(WPS_1_0_0);       
            descriptionType.setWSDL(null);                      //TODO WSDL
            descriptionType.setStatusSupported(false);          //TODO support process status
            descriptionType.setStoreSupported(false);           //TODO support process storage

        // Find the process
            final ProcessDescriptor processDesc = getProcessDescriptor(identifier.getValue());
            if(!isSupportedProcess(processDesc)){
             throw new CstlServiceException("Process not supported by the service.",
                    OPERATION_NOT_SUPPORTED, identifier.getValue());
            }
            
            descriptionType.setTitle(new LanguageStringType(processDesc.getIdentifier().getCode()));          //Process Title
            descriptionType.setAbstract(new LanguageStringType(processDesc.getProcedureDescription().toString()));  //Process abstract

        // Get process input and output descriptors
            final ParameterDescriptorGroup input = processDesc.getInputDescriptor();
            final ParameterDescriptorGroup output = processDesc.getOutputDescriptor();

            /******************
            *  Process Input parameters
            ******************/
            final ProcessDescriptionType.DataInputs dataInputs = new ProcessDescriptionType.DataInputs();
            for (GeneralParameterDescriptor param : input.descriptors()) {
                final InputDescriptionType in = new InputDescriptionType();
                
                // If the Parameter Descriptor isn't a GroupeParameterDescriptor
                if (param instanceof ParameterDescriptor) {
                    final ParameterDescriptor paramDesc = (ParameterDescriptor) param;
                    
                // Parameter informations
                    in.setIdentifier(new CodeType(paramDesc.getName().getCode()));
                    in.setTitle(new LanguageStringType(paramDesc.getName().getCode()));
                    in.setAbstract(new LanguageStringType(paramDesc.getRemarks().toString()));

                    //set occurs
                    in.setMaxOccurs(BigInteger.valueOf(paramDesc.getMaximumOccurs()));
                    in.setMinOccurs(BigInteger.valueOf(paramDesc.getMinimumOccurs()));
                // Input class
                    final Class clazz = paramDesc.getValueClass();

                // BoundingBox type
                    if (clazz.equals(Envelope.class)) {
                        in.setBoundingBoxData(getSupportedCRS());

                //Complex type (XML, ...)     
                    } else 
                    if (COMPLEX_INPUT_TYPE_LIST.contains(clazz)) {
                        in.setComplexData(describeComplex(clazz));
                        
                  //Simple object (Integer, double, ...) and Object which need a conversion from String like affineTransform or Geometry
                    } else {
                        final LiteralInputType literal = new LiteralInputType();
                        
                        if(paramDesc.getDefaultValue() != null){
                            literal.setDefaultValue(paramDesc.getDefaultValue().toString()); //default value if enable
                        }
                        literal.setAnyValue(new AnyValue());
                        literal.setDataType(createDataType(clazz));
                        literal.setUOMs(getSupportedUOM());
                        
                        in.setLiteralData(literal);
                    }
                    
                } else {
                    throw new CstlServiceException("Process parameter invalid", OPERATION_NOT_SUPPORTED);
                }
                dataInputs.getInput().add(in);
            }
            descriptionType.setDataInputs(dataInputs);

            /******************
            *  Process Output parameters
            ******************/
            final ProcessDescriptionType.ProcessOutputs dataOutput = new ProcessDescriptionType.ProcessOutputs();
            for (GeneralParameterDescriptor param : output.descriptors()) {
                final OutputDescriptionType out = new OutputDescriptionType();

                //simple paramater
                if (param instanceof ParameterDescriptor) {
                    final ParameterDescriptor paramDesc = (ParameterDescriptor) param;
                    
                 //parameter informations
                    out.setIdentifier(new CodeType(paramDesc.getName().getCode()));
                    out.setTitle(new LanguageStringType(paramDesc.getName().getCode()));
                    out.setAbstract(new LanguageStringType(paramDesc.getRemarks().toString()));
                    
                //input class
                    final Class clazz = paramDesc.getValueClass();
                    
                //BoundingBox type
                    if (clazz.equals(JTSEnvelope2D.class)) {
                        out.setBoundingBoxOutput(getSupportedCRS());

                //Complex type (XML, raster, ...)
                    } else if (COMPLEX_INPUT_TYPE_LIST.contains(clazz)) {     
                        out.setComplexOutput((SupportedComplexDataType)describeComplex(clazz));
                        
                //Simple object (Integer, double) and Object which need a conversion from String like affineTransform or Geometry
                    } else if(LITERAL_INPUT_TYPE_LIST.contains(clazz)){
                        
                        final LiteralOutputType literal = new LiteralOutputType();
                        literal.setUOMs(getSupportedUOM());
                        literal.setDataType(createDataType(clazz));
                         
                        out.setLiteralOutput(literal);
                    }

                } else {
                    throw new CstlServiceException("Process parameter invalid", OPERATION_NOT_SUPPORTED);
                }

                dataOutput.getOutput().add(out);
            }
            descriptionType.setProcessOutputs(dataOutput);
            descriptions.getProcessDescription().add(descriptionType);
        }

        return descriptions;
    }

    /*
     * Execute a process request
     */
    
    /**
     * Redirect execute requests from the WPS version requested.
     * @param request
     * @return execute response (Raw data or Document response) depends of the ResponseFormType in execute request
     * @throws CstlServiceException
     */
    public Object execute(Execute request) throws CstlServiceException {
        isWorking();

        final String service = request.getService();
        if (!(service.equalsIgnoreCase(WPS_SERVICE))) {
            throw new CstlServiceException("The version number specified for this request "
                    + "is not handled.", VERSION_NEGOTIATION_FAILED, VERSION_PARAMETER.toLowerCase());
        }

        final String version = request.getVersion().toString();
        if (version.isEmpty()) {
            throw new CstlServiceException("The parameter VERSION must be specified.",
                    MISSING_PARAMETER_VALUE, VERSION_PARAMETER.toLowerCase());
        }

        if (version.equals(ServiceDef.WPS_1_0_0.version.toString())) {
            return execute100((org.geotoolkit.wps.xml.v100.Execute) request);
        } else {
            throw new CstlServiceException("The version number specified for this discribeProcess request "
                    + "is not handled.", NO_APPLICABLE_CODE, VERSION_PARAMETER.toLowerCase());
        }
    }

    /**
     * Execute a process in wps v1.0
     * @param request
     * @return
     * @throws CstlServiceException
     */
    private Object execute100(Execute request) throws CstlServiceException {
        if (request.getIdentifier() == null) {
            throw new CstlServiceException("The parameter Identifier must be specified.",
                    MISSING_PARAMETER_VALUE, "identifier");
        }
        final StatusType status = new StatusType();
        LOGGER.info("LOG -> Process : "+request.getIdentifier().getValue());
        //Find the process
        final ProcessDescriptor processDesc = getProcessDescriptor(request.getIdentifier().getValue());
        
        if(!isSupportedProcess(processDesc)){
             throw new CstlServiceException("Process not supported by the service.",
                    OPERATION_NOT_SUPPORTED, request.getIdentifier().getValue());
        }
        
        //status.setProcessAccepted("Process "+request.getIdentifier().getValue()+" found.");

        boolean isOutputRaw = false; // the default output is a ResponseDocument

        /* Get the requested output form */
        final ResponseFormType responseForm = request.getResponseForm();
        final OutputDefinitionType rawData = responseForm.getRawDataOutput();
        final ResponseDocumentType respDoc = responseForm.getResponseDocument();

        /* Raw output data attributs */
        String rawOutputID = null;
        String rawOutputMime = null;
        String rawOutputEncoding = null;
        String rawOutputSchema = null;
        String rawOutputUom = null;
        
        /* ResponseDocument attributs */
        boolean isLineage = false;
        boolean useStatus = false;
        boolean useStorage = false;
        ExecuteResponse response = null;
        List<DocumentOutputDefinitionType> wantedOutputs = null;

        /* Raw Data*/
        if(rawData != null){
            isOutputRaw = true;
            rawOutputID = rawData.getIdentifier().getValue();
            rawOutputMime = rawData.getMimeType();
            rawOutputEncoding = rawData.getEncoding();
            rawOutputSchema = rawData.getSchema();
            rawOutputUom = rawData.getUom();

        /* ResponseDocument */
        }else if(respDoc != null){

            isLineage = respDoc.isLineage();
            
            // Status and storage desactivated for now
            useStatus = respDoc.isStatus();
            //useStorage = respDoc.isStoreExecuteResponse();

            wantedOutputs = respDoc.getOutput();

            response = new ExecuteResponse();
            response.setService(WPS_SERVICE);
            response.setVersion(WPS_1_0_0);
            response.setLang(WPS_LANG);
            response.setServiceInstance(null);      //TODO getCapabilities URL

            //Give a bief process description into the execute response
            response.setProcess(processBrief(processDesc));
            
            LOGGER.info("LOG -> Lineage="+isLineage);
            LOGGER.info("LOG -> Storage="+useStorage);
            LOGGER.info("LOG -> Status="+useStatus);
            
            if(isLineage){
                //Inputs
                response.setDataInputs(request.getDataInputs());
                final OutputDefinitionsType outputsDef = new OutputDefinitionsType();
                outputsDef.getOutput().addAll(respDoc.getOutput());
                //Outputs
                response.setOutputDefinitions(outputsDef);
            }

            if(useStorage){
                response.setStatusLocation(null); //Output data URL
            }

            if(useStatus){
                response.setStatus(status);
            }
        }else{
            
        }
        //Input temporary files used by the process. In order to delete them at the end of the process.
        List<File> files = null;
        
        //Create Process and Inputs
        final ParameterValueGroup in = processDesc.getInputDescriptor().createValue();

        /******************
         * Process INPUT
         ******************/
        final List<InputType> requestInputData = request.getDataInputs().getInput();
        final List<GeneralParameterDescriptor> processInputDesc = processDesc.getInputDescriptor().descriptors();

        /* Check for a missing input parameter */
        if(requestInputData.size() != processInputDesc.size()){
            for (GeneralParameterDescriptor generalParameterDescriptor : processInputDesc) {
                boolean inputFound = false;
                final String processInputID = generalParameterDescriptor.getName().getCode();
                for(InputType inputRequest : requestInputData) {
                    if(inputRequest.getIdentifier().getValue().equals(processInputID)){
                        inputFound = true;
                    }
                }
                // if the parameter is not found and if it's a mandatory parameter
                if(!inputFound){
                    if(generalParameterDescriptor.getMinimumOccurs() != 0){
                        throw new CstlServiceException("Mandatory input parameter is"
                                + "missing.", MISSING_PARAMETER_VALUE, processInputID);
                    }
                }
            }
        }

        //Fill input process with there default values
        for(GeneralParameterDescriptor inputGeneDesc : processDesc.getInputDescriptor().descriptors()){
            
            if(inputGeneDesc instanceof ParameterDescriptor){
                final ParameterDescriptor inputDesc = (ParameterDescriptor)inputGeneDesc;
                
                if (inputDesc.getDefaultValue() != null) {
                    in.parameter(inputDesc.getName().getCode()).setValue(inputDesc.getDefaultValue());
                }
            }else{
                throw new CstlServiceException("Process parameter invalid", OPERATION_NOT_SUPPORTED);
            }
        }
        
        //Each input from the request
        for (InputType inputRequest : requestInputData) {

            if(inputRequest.getIdentifier() == null){
                throw new CstlServiceException("Missing input Identifier.", INVALID_PARAMETER_VALUE);
            }
            
            final String inputIdentifier = inputRequest.getIdentifier().getValue();
            
            //Check if it's a valid input identifier
            final List<GeneralParameterDescriptor> processInputList = processDesc.getInputDescriptor().descriptors();
            boolean existInput = false;
            for (GeneralParameterDescriptor processInput : processInputList) {
                if(processInput.getName().getCode().equals(inputIdentifier)){
                    existInput = true;
                }
            }
            if(!existInput){
                throw new CstlServiceException("Unknow input Identifier.", INVALID_PARAMETER_VALUE,inputIdentifier);
            }

            final GeneralParameterDescriptor inputGeneralDescriptor = processDesc.getInputDescriptor().descriptor(inputIdentifier);
            ParameterDescriptor inputDescriptor;

            if (inputGeneralDescriptor instanceof ParameterDescriptor) {
                inputDescriptor = (ParameterDescriptor) inputGeneralDescriptor;
            } else {
                throw new CstlServiceException("The input Identifier is invalid.", INVALID_PARAMETER_VALUE,inputIdentifier);
            }
            
            /*
             * Get expected input Class from the process input
             */
            final Class expectedClass = inputDescriptor.getValueClass();
                      

            Object dataValue = null;
            LOGGER.info("Expected Class = "+expectedClass.getCanonicalName());
            
            /*
             * A referenced input data
             */
            if (inputRequest.getReference() != null) {
                
                //Check if the expected class is supproted for literal using
                if(!isSupportedClass("reference",expectedClass)){
                    throw new CstlServiceException("Reference value expected",INVALID_PARAMETER_VALUE,inputIdentifier); 
                }
                
                LOGGER.info("LOG -> Input -> Reference");
                final String href = inputRequest.getReference().getHref();
                final String method = inputRequest.getReference().getMethod();
                final String mime = inputRequest.getReference().getMimeType();
                final String encoding = inputRequest.getReference().getEncoding();
                final String schema = inputRequest.getReference().getSchema();

                dataValue = reachReferencedData(href, method, mime, encoding, schema, expectedClass, inputIdentifier);
                if(dataValue instanceof FeatureCollection){
                    dataValue = (FeatureCollection) dataValue;
                }
                if(dataValue instanceof File){
                    if(files == null){
                        files = new ArrayList<File>();
                    }
                    files.add((File) dataValue);
                }
                
            /*
             * Encapsulated data into the Execute Request
             */
            } else if (inputRequest.getData() != null) {
                
                /* BoundingBox data */
                if (inputRequest.getData().getBoundingBoxData() != null) {
                    LOGGER.info("LOG -> Input -> Boundingbox");
                    final BoundingBoxType bBox = inputRequest.getData().getBoundingBoxData();
                    final List<Double> lower = bBox.getLowerCorner();
                    final List<Double> upper = bBox.getUpperCorner();
                    final String crs = bBox.getCrs();
                    final int dimension = bBox.getDimensions();
                    
                    //Check if it's a 2D boundingbox
                    if(dimension != 2 || lower.size() != 2 || upper.size() != 2){
                        throw new CstlServiceException("Invalid data input : Only 2 dimension boundingbox supported.",OPERATION_NOT_SUPPORTED, inputIdentifier);
                    }

                    CoordinateReferenceSystem crsDecode;
                    try {
                        crsDecode = CRS.decode(crs);
                    } catch (FactoryException ex) {
                         throw new CstlServiceException("Invalid data input : CRS not supported.",
                               ex , OPERATION_NOT_SUPPORTED, inputIdentifier);
                    }

                    final Envelope envelop = GeometryUtils.createCRSEnvelope(crsDecode,lower.get(0), lower.get(1), upper.get(0),upper.get(1));
                    dataValue = envelop;

                /* Complex data (XML, raster, ...) */
                } else if (inputRequest.getData().getComplexData() != null) {
                    
                    //Check if the expected class is supproted for complex using
                    if(!isSupportedClass("complex",expectedClass)){
                        throw new CstlServiceException("Complex value expected",INVALID_PARAMETER_VALUE,inputIdentifier); 
                    }
                    
                    LOGGER.info("LOG -> Input -> Complex");

                    final ComplexDataType complex = inputRequest.getData().getComplexData();
                    final String mime = complex.getMimeType();
                    final String encoding = complex.getEncoding();
                    final List<Object> content = complex.getContent();
                    final String schema = complex.getSchema();
                               
                    if(content.size() <= 0 ){
                        throw new CstlServiceException("Missing data input value.", INVALID_PARAMETER_VALUE, inputIdentifier);
                        
                    }else{
                   
                        final List<Object> inputObject = new ArrayList<Object>();
                        for(Object obj : content){
                            if(obj != null){
                                if(!(obj instanceof String)){
                                    inputObject.add(obj);
                                }
                            }
                        }
                        
                        if(inputObject == null){
                            throw new CstlServiceException("Invalid data input value : Empty value.", INVALID_PARAMETER_VALUE, inputIdentifier);
                        }
                        
                        /*
                         * Extract Data from inputObject array
                         */
                        dataValue = extractComplexInput(expectedClass, inputObject, schema, mime, encoding, inputIdentifier);
                    }

                /* Literal data */
                } else if (inputRequest.getData().getLiteralData() != null) {
                    //Check if the expected class is supproted for literal using
                    if(!isSupportedClass("literal",expectedClass)){
                        throw new CstlServiceException("Literal value expected",INVALID_PARAMETER_VALUE,inputIdentifier); 
                    }
                    
                    LOGGER.info("LOG -> Input -> Literal");
                    
                    final LiteralDataType literal = inputRequest.getData().getLiteralData();
                    final String data = literal.getValue();
                    
                    //convert String into expected type
                    dataValue = convertFromString(data, expectedClass);
                    LOGGER.info("DEBUG -> Input -> Literal -> Value="+dataValue);

                } else {
                    throw new CstlServiceException("Invalid input data type.", INVALID_REQUEST, inputIdentifier);
                }
            } else {
                throw new CstlServiceException("Invalid input data format.", INVALID_REQUEST,inputIdentifier);
            }

            try{
                in.parameter(inputIdentifier).setValue(dataValue);
            }catch(InvalidParameterValueException ex ){
                throw new CstlServiceException("Invalid data input value.",ex, INVALID_PARAMETER_VALUE,inputIdentifier);
            }
        }

        //Give input parameter to the process
        final org.geotoolkit.process.Process proc = processDesc.createProcess(in);
        
        //Status
        ProcessStartedType started = new ProcessStartedType();
        started.setValue("Process "+request.getIdentifier().getValue()+" is started");
        started.setPercentCompleted(0);
        //status.setProcessStarted(started);
        
        //Run the process
        final ParameterValueGroup result;
        try {
            result = proc.call();
        } catch (ProcessException ex) {
            throw new CstlServiceException("Process execution failed");
        }
        
        /******************
         * Process OUTPUT *
         ******************/
     
        /* Storage data */
        if(useStorage){
            //TODO storage output
            throw new UnsupportedOperationException("Output storage not yet implemented");
        /* No strorage */
        }else{
            /* Raw Data returned */
            if(isOutputRaw){
                LOGGER.info("LOG -> Output -> Raw");
                final Object outputValue = result.parameter(rawOutputID).getValue();
                LOGGER.info("DEBUG -> Output -> Raw -> Value="+outputValue);
                
                if(outputValue instanceof Geometry){
                    try {
                        final Geometry jtsGeom = (Geometry)outputValue;
                        final AbstractGeometryType gmlGeom = JTStoGeometry.toGML(jtsGeom);
                        return gmlGeom;
                    } catch (NoSuchAuthorityCodeException ex) {
                        throw new CstlServiceException(ex);
                    } catch (FactoryException ex) {
                        throw new CstlServiceException(ex);
                    }
                   
                }
                
                if(outputValue instanceof Envelope){
                    return new BoundingBoxType((Envelope)outputValue);
                }
                return outputValue;
               
            /* DocumentResponse returned */
            }else{
                LOGGER.info("LOG -> Output -> Document");
                final ExecuteResponse.ProcessOutputs outputs = new ExecuteResponse.ProcessOutputs();
                //Process Outputs
                for (GeneralParameterDescriptor outputDescriptor : processDesc.getOutputDescriptor().descriptors()) {

                    final OutputDataType outData = new OutputDataType();

                    //set Ouput informations
                    final String outputIdentifier = outputDescriptor.getName().getCode();
                    outData.setIdentifier(new CodeType(outputIdentifier));
                    outData.setTitle(new LanguageStringType(outputIdentifier));
                    outData.setAbstract(new LanguageStringType(outputDescriptor.getRemarks().toString()));

                    /* Output value from process */
                    final Object outputValue = result.parameter(outputIdentifier).getValue();

                    final DataType data = new DataType();
                    if (outputDescriptor instanceof ParameterDescriptor) {

                        final ParameterDescriptor outParamDesc = (ParameterDescriptor) outputDescriptor;
                        /* Output Class */
                        final Class outClass = outParamDesc.getValueClass();

                        /* Bounding Box */
                        if (outClass.equals(Envelope.class)) {
                            LOGGER.info("LOG -> Output -> BoundingBox");
                            org.opengis.geometry.Envelope envelop = (org.opengis.geometry.Envelope) outputValue;
                            
                            data.setBoundingBoxData(new BoundingBoxType(envelop));

                        /* Complex */
                        } else if (isSupportedClassOutput("complex", outClass)) {
                           LOGGER.info("LOG -> Output -> Complex");
                            final ComplexDataType complex = new ComplexDataType();

                            for(DocumentOutputDefinitionType wO : wantedOutputs){
                                if(wO.getIdentifier().getValue().equals(outputIdentifier)){
                                    complex.setEncoding(wO.getEncoding());
                                    complex.setMimeType(wO.getMimeType());
                                    complex.setSchema(wO.getSchema());
                                }
                            }

                            ObjectConverter converter = null;
                            for (ObjectConverter conv : (List<ObjectConverter>) OUTPUT_COMPLEX_CONVERTERS) {

                                if (conv.getSourceClass().isAssignableFrom(outClass)) {
                                    converter = conv;
                                }
                            }   

                            if (converter == null) {
                                throw new CstlServiceException("Input complex not supported, no converter found.",
                                        OPERATION_NOT_SUPPORTED,outputIdentifier);
                            }

                            try{
                                complex.getContent().addAll((Collection<Object>)converter.convert(outputValue));
                            } catch (NonconvertibleObjectException ex) {
                                throw new CstlServiceException(ex, INVALID_PARAMETER_VALUE, outputIdentifier);
                            }
                                                        
                            data.setComplexData(complex);

                        /* Literal */
                        } else if(isSupportedClassOutput("literal", outClass)){
                            LOGGER.info("LOG -> Output -> Literal");
                            final LiteralDataType literal = new LiteralDataType();
                            literal.setDataType(outClass.getCanonicalName());
                            if(outputValue == null){
                                literal.setValue(null);
                            }else{
                                literal.setValue(outputValue.toString());
                            }
                            data.setLiteralData(literal);
                            
                        }else{
                             throw new CstlServiceException("Process output parameter invalid", OPERATION_NOT_SUPPORTED, outputIdentifier);
                        }
                    } else {
                         throw new CstlServiceException("Process output parameter invalid", OPERATION_NOT_SUPPORTED, outputIdentifier);
                    }

                    outData.setData(data);
                    outputs.getOutput().add(outData);
                }
                
                response.setProcessOutputs(outputs);

                if(useStatus){
                    response.setStatus(new StatusType());
                }
                status.setProcessSucceeded("Process "+request.getIdentifier().getValue()+" finiched.");
                if(useStatus){
                    response.setStatus(status);
                }
                
                //Delete input temporary files 
                if(files != null){
                    for (File f : files) {
                        f.delete();
                    }
                }
                
                return response;
            }
        }
    }

    /**
     * Return the process descriptor from a process identifier
     * @param identifier like "vector.buffer"
     * @return ProcessDescriptor
     * @throws CstlServiceException in case of an unknown process identifier
     */
    private static ProcessDescriptor getProcessDescriptor(final String identifier) throws CstlServiceException {

        final int split = identifier.indexOf(".");
        final String processFactory = identifier.substring(0, split);
        final String processName = identifier.substring(split + 1, identifier.length());
        if (processName.indexOf(".") != -1) {
            throw new CstlServiceException("Invalid Identifier", INVALID_REQUEST, identifier);
        }

        ProcessDescriptor processDesc = null;
        try {
            processDesc = ProcessFinder.getProcessDescriptor(processFactory, processName);
        } catch (IllegalArgumentException ex) {
            throw new CstlServiceException(ex, INVALID_REQUEST, VERSION_PARAMETER.toLowerCase());
        } catch (NoSuchIdentifierException ex) {
            throw new CstlServiceException(ex, INVALID_REQUEST, VERSION_PARAMETER.toLowerCase());
        }
        return processDesc;
    }

    /**
     * Return a brief description of the process associate to the process identifier
     * @param identifier like "vector.buffer"
     * @return ProcessBriefType
     * @throws CstlServiceException in case of an unknown process identifier
     */
    private static ProcessBriefType processBrief(final String identifier) throws CstlServiceException {

        final ProcessDescriptor processDesc = getProcessDescriptor(identifier);

        return processBrief(processDesc);
    }

    /**
     * Return a brief description of the process from his process descriptor
     * @param processDesc
     * @return ProcessBriefType
     */
    private static ProcessBriefType processBrief(final ProcessDescriptor processDesc) {

        final ProcessBriefType brief = new ProcessBriefType();
        brief.setIdentifier(new CodeType(processDesc.getIdentifier().getAuthority().getTitle().toString() + "." + processDesc.getIdentifier().getCode()));
        brief.setTitle(new LanguageStringType(processDesc.getIdentifier().getCode()));
        brief.setAbstract(new LanguageStringType(processDesc.getProcedureDescription().toString()));
        brief.setProcessVersion(null);
        brief.setWSDL(null);

        return brief;
    }

    /**
     * Convert a string to a binding class. If the binding class isn't a primitive like Integer, Double, ..
     * we search into the converter list if found a match.
     * @param data string to convert
     * @param binding wanted class
     * @return  converted object
     * @throws CstlServiceException if there is no match found
     */
    private static <T> Object convertFromString(final String data, final Class binding) throws CstlServiceException {

        Object convertedData = null; //resulting Object
        try {
            
            ObjectConverter<String, T> converter = null;//converter
            try {
                //try to convert into a primitive type
                converter = ConverterRegistry.system().converter(String.class, binding);
            } catch (NonconvertibleObjectException ex) {
                //try to convert with some specified converter
                for (ObjectConverter conv : (List<ObjectConverter>) LITERAL_CONVERTERS) {
                    
                    if (conv.getTargetClass().equals(binding)) {
                        converter = conv;
                    }
                }
                if (converter == null) {
                    throw new CstlServiceException(ex);
                }
            }
            convertedData = converter.convert(data);
        } catch (NonconvertibleObjectException ex) {
            throw new CstlServiceException(ex);
        }
        return convertedData;
    }

    /**
     * Create the supported CRS list
     */
    private void initSupportedCRS() {
        final Set<String> allAuth = CRS.getSupportedAuthorities(true);
        for (String auth : allAuth) {
            final Set<String> allCodes = CRS.getSupportedCodes(auth);
            for (String code : allCodes) {
                SUPPORTED_CRS.add(auth + ":" + code);
            }
        }
    }

    /**
     * Get an convert data from a reference for an expected binding
     * 
     * @param expectedClass
     * @param inputObject
     * @param schema
     * @param mime
     * @param encoding
     * @param inputID
     * @return
     * @throws CstlServiceException 
     */
    private Object extractComplexInput(final Class expectedClass,final List<Object> inputObject,
            final String schema,final String mime,final String encoding, final String inputID) throws CstlServiceException{

        final Map<String,Object> parameters = new HashMap<String, Object> ();
        parameters.put("data", inputObject);
        parameters.put("mime", mime);
        parameters.put("schema", schema);
        parameters.put("encoding", encoding);
        
        ObjectConverter converter = null;
        
        for (ObjectConverter conv : (List<ObjectConverter>) COMPLEX_CONVERTERS) {
                    
            if (conv.getTargetClass().isAssignableFrom(expectedClass)) {
                converter = conv;
            }
        }   
        
        if (converter == null) {
            throw new CstlServiceException("Input complex not supported, no converter found.",OPERATION_NOT_SUPPORTED,inputID);
        }

        try{
            return converter.convert(parameters);

        } catch (NonconvertibleObjectException ex) {
           throw new CstlServiceException(ex, INVALID_PARAMETER_VALUE, inputID);
        }
        
    }
    
    /**
     * Get an convert data from a reference for an expected binding
     *
     * @param href
     * @param method
     * @param mime
     * @param encoding
     * @param schema
     * @param expectedClass
     * @return an object
     * @throws CstlServiceException if something went wrong
     */
    private Object reachReferencedData(String href, final String method, final String mime,
            final String encoding, final String schema, final Class expectedClass, final String inputID) throws CstlServiceException {
      
        
        if(href == null){
            throw new CstlServiceException("Invalid reference input : href can't be null.", INVALID_PARAMETER_VALUE, inputID);
        }

        try {
            href = URLDecoder.decode(href, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new CstlServiceException("Invalid reference href.",ex, INVALID_PARAMETER_VALUE, inputID);
        }
        
        final Map<String,String> parameters = new HashMap<String, String> ();
        parameters.put("href", href);
        parameters.put("mime", mime);
        parameters.put("schema", schema);
        parameters.put("method", method);
        parameters.put("encoding", encoding);
        
        ObjectConverter converter = null;
        
        for (ObjectConverter conv : (List<ObjectConverter>) REFERENCE_CONVERTERS) {
                    
            if (conv.getTargetClass().isAssignableFrom(expectedClass)) {
                converter = conv;
            }
        }   
        
        if (converter == null) {
            throw new CstlServiceException("Input reference not supported, no converter found.",OPERATION_NOT_SUPPORTED,inputID);
        }

        try{
            return converter.convert(parameters);

        } catch (NonconvertibleObjectException ex) {
           throw new CstlServiceException(ex, INVALID_PARAMETER_VALUE, inputID);
        }
        
    }

    /**
     * Test if a process is supported by the WPS
     * @param descriptor
     * @return true if process is supported, false if is not.
     */
    private boolean isSupportedProcess(ProcessDescriptor descriptor) {
       
        //Inputs
        final List<GeneralParameterDescriptor> inputDesc = descriptor.getInputDescriptor().descriptors();
        for (GeneralParameterDescriptor input : inputDesc) {
            if(!(input instanceof ParameterDescriptor)){
                return false;
            }else{
                final ParameterDescriptor inputParam = (ParameterDescriptor)input;
                final Class inputClass = inputParam.getValueClass();
                if(!isSupportedClass(null, inputClass)){
                    return false;
                }
            }
        }
        
        //Outputs
        final List<GeneralParameterDescriptor> outputDesc = descriptor.getOutputDescriptor().descriptors();
        for (GeneralParameterDescriptor output : outputDesc) {
            if(!(output instanceof ParameterDescriptor)){
                return false;
            }else{
                final ParameterDescriptor outputParam = (ParameterDescriptor)output;
                final Class outputClass = outputParam.getValueClass();
                if(!isSupportedClassOutput(null, outputClass)){
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Check if an input class is supported by the WPS
     * @param type like "literal", "complex" or "reference". If <code>type</code> is <code>null</code> 
     * the function test for all types.
     * @param expectedClass binding
     * @return true if supported, false else.
     */
    private boolean isSupportedClass(String type, Class expectedClass ){
        boolean testLiteral = false;
        boolean testComplex = false;
        boolean testReference = false;
        boolean testBbox = false;
        
        if(type == null){
            testLiteral = isSupportedClass("literal",expectedClass);
            testComplex = isSupportedClass("complex",expectedClass);
            testReference = isSupportedClass("reference",expectedClass);
            testBbox = isSupportedClass("boundingbox", expectedClass);
            LOGGER.info("DEBUG -> Is Supported INPUT : ExpectedClass="+expectedClass.getSimpleName()+" "
                    + "Literal="+testLiteral+" Complex="+testComplex+" Reference="+testReference+" Bbox="+testBbox);
            if(testLiteral || testComplex || testReference || testBbox){
                return true;
            }
        }else if("literal".equals(type)){
            for(Object obj : LITERAL_INPUT_TYPE_LIST){
                Class clazz = (Class)obj;
                if(clazz.isAssignableFrom(expectedClass)){
                   return true;
                }
            }
        }else if(type.equals("complex")){
            for(Object obj : COMPLEX_INPUT_TYPE_LIST){
                Class clazz = (Class)obj;
                if(clazz.isAssignableFrom(expectedClass)){
                    return true;
                }
            }
        }else if(type.equals("reference")){
            for(Object obj : REFERENCE_INPUT_TYPE_LIST){
                Class clazz = (Class)obj;
                if(clazz.isAssignableFrom(expectedClass)){
                    return true;
                }
            }
        }else if(type.equals("boundingbox")){
            if(Envelope.class.isAssignableFrom(expectedClass)){
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if an output class is supported by the WPS
     * @param type like "literal", "complex" or "reference". If <code>type</code> is <code>null</code> 
     * the function test for all types.
     * @param expectedClass binding
     * @return true if supported, false else.
     */
    private boolean isSupportedClassOutput(String type, Class expectedClass ){
        boolean testComplex = false;
        boolean testLiteral = false;
        boolean testReference = false;
        boolean testBbox = false;
        
        if(type == null){
            testComplex = isSupportedClassOutput("complex", expectedClass);
            testLiteral = isSupportedClassOutput("literal", expectedClass);
            testReference = isSupportedClassOutput("reference", expectedClass);
            testReference = isSupportedClassOutput("boundingbox", expectedClass);
            
            LOGGER.info("DEBUG -> Is Supported OUTPUT : ExpectedClass="+expectedClass.getSimpleName()+" "
                    + "Literal="+testLiteral+" Complex="+testComplex+" Reference="+testReference+" Bbox="+testBbox);

            if(testComplex || testLiteral || testReference || testBbox){
                return true;
            }
        }else if("complex".equals(type)){
            for(Object obj : COMPLEX_OUTPUT_TYPE_LIST){
                Class clazz = (Class)obj;
                if(clazz.isAssignableFrom(expectedClass)){
                    return true;
                }
            }
        }else if("literal".equals(type)){
            for(Object obj : LITERAL_OUPUT_TYPE_LIST){
                Class clazz = (Class)obj;
                if(clazz.isAssignableFrom(expectedClass)){
                    return true;
                }
            }
        }else if("reference".equals(type)){
            for(Object obj : REFERENCE_OUTPUT_TYPE_LIST){
                Class clazz = (Class)obj;
                if(clazz.isAssignableFrom(expectedClass)){
                    return true;
                }
            }
        }else if("boundingbox".equals(type)){
            if(Envelope.class.isAssignableFrom(expectedClass)){
                return true;
            }
        }    
        return false;
    }
    
    /**
     * Create the DomaineMetaData object for a literal
     * @param clazz
     * @return
     * @throws CstlServiceException
     */
    private DomainMetadataType createDataType(final Class clazz) throws CstlServiceException {

        
        if(clazz.equals(Double.class)){
            return new DomainMetadataType("Double", "http://www.w3.org/TR/xmlschema-2/#double");

        }else if(clazz.equals(Float.class)){
            return new DomainMetadataType("Float", "http://www.w3.org/TR/xmlschema-2/#float");
        
        }else if(clazz.equals(Boolean.class)){
            return new DomainMetadataType("Boolean", "http://www.w3.org/TR/xmlschema-2/#boolean");

        }else if(clazz.equals(Integer.class)){
            return new DomainMetadataType("Integer", "http://www.w3.org/TR/xmlschema-2/#integer");

        }else if(clazz.equals(Long.class)){
            return new DomainMetadataType("Long", "http://www.w3.org/TR/xmlschema-2/#long");
            
        }else if(clazz.equals(String.class) || isSupportedClass("literal", clazz)){
            return new DomainMetadataType("String", "http://www.w3.org/TR/xmlschema-2/#string");

        }else {
            throw new CstlServiceException("No supported literal type");
        }
    }

    /**
     * Fix the CRS problem for a Feature or a FeatureCollection
     * @param dataValue a Feature or a FeatureCollection
     * @return the sale Feature/FeatureCollection fixed
     * @throws CstlServiceException 
     */
    public static Object fixFeature(final Object dataValue) throws CstlServiceException {
        
        if(dataValue instanceof  Feature){
            
            final Feature featureIN = (Feature)dataValue;
            DefaultFeatureType ft = (DefaultFeatureType) featureIN.getType();
            fixFeatureType(featureIN, ft);
           
            return featureIN;
        }
        
        if(dataValue instanceof FeatureCollection){
            final FeatureCollection featureColl = (FeatureCollection)dataValue;
            
            DefaultFeatureType ft = (DefaultFeatureType) featureColl.getFeatureType();
            final FeatureIterator featureIter = featureColl.iterator();
            if(featureIter.hasNext()){
                final Feature feature = featureIter.next();
                fixFeatureType(feature, ft);
            }
            featureIter.close();
            return featureColl;
        }
        
        throw new CstlServiceException("Invalid Feature");
    }

    /**
     * Fix a FeatureType in spread the geometry CRS from a feature to the geometry descriptor CRS
     * @param featureIN feature with geometry used to fix the geometry descriptor
     * @param type the featureType to fix
     * @throws CstlServiceException 
     */
    public static void fixFeatureType(final Feature featureIN, DefaultFeatureType type) throws CstlServiceException{
        
        CoordinateReferenceSystem extractCRS = null;

        final FeatureTypeBuilder ftb = new FeatureTypeBuilder();
        ftb.copy(type);

        //Fetch each geometry, get his CRS and 
        for(Property property : featureIN.getProperties()){
            if(property.getDescriptor() instanceof GeometryDescriptor){
                try {
                    final String propertyName = property.getName().getLocalPart();
                    final Geometry propertyGeom = (Geometry) property.getValue();
                    extractCRS = JTS.findCoordinateReferenceSystem(propertyGeom);

                    final Iterator<PropertyDescriptor> ite = type.getDescriptors().iterator();

                    while(ite.hasNext()){
                        final DefaultPropertyDescriptor propertyDesc = (DefaultPropertyDescriptor)ite.next();
                        
                        if(propertyDesc.getName().getLocalPart().equals(propertyName)){
                            final DefaultGeometryType geomType = (DefaultGeometryType) propertyDesc.getType();
                            geomType.setCoordinateReferenceSystem(extractCRS);
                            break;
                        }
                    }
                } catch (NoSuchAuthorityCodeException ex) {
                    throw new CstlServiceException("Can't find feature geometry CRS",ex,NO_APPLICABLE_CODE);
                } catch (FactoryException ex) {
                    throw new CstlServiceException("Can't find feature geometry CRS",ex,NO_APPLICABLE_CODE);
                }
            }
        }
    }

    /**
     * 
     * @param attributeClass
     * @return 
     */
    private SupportedComplexDataInputType describeComplex(final Class attributeClass) {
        
        final SupportedComplexDataInputType complex = new SupportedComplexDataInputType();
        final ComplexDataCombinationsType complexCombs = new ComplexDataCombinationsType();
        final ComplexDataCombinationType complexComb = new ComplexDataCombinationType();
        ComplexDataDescriptionType complexDesc = null;

        for(WPSIO.InputClass inputClass : WPSIO.USEDCLASS){
            if(attributeClass.equals(inputClass.getClazz())){

                complexDesc = new ComplexDataDescriptionType();
                complexDesc.setEncoding(inputClass.getEncoding());   //Encoding
                complexDesc.setMimeType(inputClass.getMime());       //Mime
                complexDesc.setSchema(inputClass.getSchema());       //URL to xsd schema

                if(inputClass.isDefault()){
                     complexComb.setFormat(complexDesc);
                }
                complexCombs.getFormat().add(complexDesc);
            }
        }

        complex.setDefault(complexComb);
        complex.setSupported(complexCombs);
        complex.setMaximumMegabytes(BigInteger.valueOf(MAX_MB_INPUT_COMPLEX));
        return complex;
    }

    /**
     * Supported UOM for literal
     * @return SupportedUOMsType
     */
    private SupportedUOMsType getSupportedUOM() {
        
        final SupportedUOMsType uom = new SupportedUOMsType();
        final SupportedUOMsType.Default defaultUOM = new SupportedUOMsType.Default();
        final UOMsType supportedUOM = new UOMsType(); 

        defaultUOM.setUOM(new DomainMetadataType("m", null));

        supportedUOM.getUOM().add(new DomainMetadataType("m", null));
        supportedUOM.getUOM().add(new DomainMetadataType("km", null));
        supportedUOM.getUOM().add(new DomainMetadataType("cm", null));
        supportedUOM.getUOM().add(new DomainMetadataType("mm", null));
        
        uom.setDefault(defaultUOM);
        uom.setSupported(supportedUOM);
        
        return uom;
    }

    /**
     * Return Supported Coordinate Reference System
     * @return SupportedCRSsType
     */
    private SupportedCRSsType getSupportedCRS() {
        final SupportedCRSsType crsList = new SupportedCRSsType();
        final SupportedCRSsType.Default defaultCRS = new SupportedCRSsType.Default();
        defaultCRS.setCRS(DEFAULT_CRS);                 
        crsList.setDefault(defaultCRS);
        final CRSsType supportedCRS = new CRSsType();
        supportedCRS.getCRS().addAll(SUPPORTED_CRS);
        crsList.setSupported(supportedCRS);
        return crsList;
    }
    
}
