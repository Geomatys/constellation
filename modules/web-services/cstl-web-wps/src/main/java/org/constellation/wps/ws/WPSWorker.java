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
import javax.xml.parsers.ParserConfigurationException;
import org.geotoolkit.coverage.io.CoverageStoreException;
import org.geotoolkit.storage.DataStoreException;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import com.vividsolutions.jts.geom.Geometry;

import java.awt.geom.AffineTransform;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.measure.unit.Unit;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;

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
import org.geotoolkit.process.ProcessFactory;
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
import org.geotoolkit.feature.xml.XmlFeatureReader;
import org.geotoolkit.feature.xml.XmlFeatureTypeReader;
import org.geotoolkit.feature.xml.jaxb.JAXBFeatureTypeReader;
import org.geotoolkit.feature.xml.jaxp.JAXPStreamFeatureReader;
import org.geotoolkit.gml.GeometrytoJTS;
import org.geotoolkit.gml.xml.v311.AbstractGeometryType;
import org.geotoolkit.wps.xml.v100.DocumentOutputDefinitionType;
import org.geotoolkit.wps.xml.v100.OutputDefinitionType;
import org.geotoolkit.wps.xml.v100.OutputDefinitionsType;
import org.geotoolkit.wps.xml.v100.ResponseDocumentType;
import org.geotoolkit.wps.xml.v100.ResponseFormType;
import org.geotoolkit.wps.xml.v100.StatusType;
import org.geotoolkit.wps.xml.v100.ProcessStartedType;
import org.geotoolkit.data.FeatureIterator;
import org.geotoolkit.feature.AttributeDescriptorBuilder;
import org.geotoolkit.feature.AttributeTypeBuilder;
import org.geotoolkit.feature.FeatureTypeBuilder;
import org.geotoolkit.feature.type.DefaultFeatureType;
import org.geotoolkit.feature.type.DefaultGeometryType;
import org.geotoolkit.feature.type.DefaultPropertyDescriptor;
import org.geotoolkit.feature.xml.jaxp.ElementFeatureWriter;
import org.geotoolkit.geometry.isoonjts.GeometryUtils;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.geometry.jts.JTSEnvelope2D;

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
import org.constellation.ws.AbstractWorker;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.MimeType;
import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.coverage.io.GridCoverageReaders;
import org.geotoolkit.data.DataStore;
import org.geotoolkit.data.DataStoreFinder;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.gml.JTStoGeometry;
import org.geotoolkit.wps.xml.WPSMarshallerPool;
import org.geotoolkit.xsd.xml.v2001.Schema;

import static org.constellation.query.Query.*;
import static org.constellation.wps.ws.WPSConstant.*;

/**
 * @author Quentin Boileau
 */
public class WPSWorker extends AbstractWorker {

    /**
     * List of converter
     */
    private static final List CONVERTERS_LIST = UnmodifiableArrayList.wrap(
            StringToFeatureCollectionConverter.getInstance(),
            StringToUnitConverter.getInstance(),
            StringToGeometryConverter.getInstance(),
            StringToCRSConverter.getInstance(),
            StringToAffineTransformConverter.getInstance(),
            StringToFilterConverter.getInstance(),
            StringToSortByConverter.getInstance());

  
     /**
     * List of Complex input Class
     */
    private static final List COMPLEX_INPUT_TYPE_LIST = UnmodifiableArrayList.wrap(
            Feature.class,
            FeatureCollection.class,
            Feature[].class,
            FeatureCollection[].class,
            FeatureType.class,
            Geometry.class);

    /**
     * List of Complex output Class
     */
    private static final List COMPLEX_OUTPUT_TYPE_LIST = UnmodifiableArrayList.wrap(
            Feature.class,
            FeatureCollection.class,
            Geometry.class);
    
    /**
     * List of literal input Class which need a conversion from String
     */
    private static final List LITERALTYPE_LIST = UnmodifiableArrayList.wrap(
            Number.class,Boolean.class,String.class,
            Unit.class,
            AffineTransform.class,
            org.opengis.filter.Filter.class,
            CoordinateReferenceSystem.class,
            SortBy[].class);

    /*
     * List of Reference input Class 
     */
    private static final List REFERENCETYPE_LIST = UnmodifiableArrayList.wrap(
            Feature.class,
            FeatureCollection.class,
            Geometry.class,
            File.class,
            FeatureType.class,
            FeatureExtend.class,
            GridCoverage2D.class,
            GridCoverageReader.class);
    
    /*
     * A list supported CRS
     *
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

    /*
     * GetCapabilities request
     */
    /**
     * 
     * @param request
     * @return
     * @throws CstlServiceException
     */
    public WPSCapabilitiesType getCapabilities(GetCapabilities request) throws CstlServiceException {
        isWorking();

        final String service = request.getService();

        if (!(service.equalsIgnoreCase(WPS_SERVICE))) {
            throw new CstlServiceException("The version number specified for this request "
                    + "is not handled.", VERSION_NEGOTIATION_FAILED, KEY_VERSION.toLowerCase());
        }
        System.out.println("Request : "+request);
        System.out.println("Versions : "+request.getAcceptVersions().getVersion());
        final List<String> versionsAccepted = request.getAcceptVersions().getVersion();
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
                    + "is not handled.", VERSION_NEGOTIATION_FAILED, KEY_VERSION.toLowerCase());
        }
        
    }

    /**
     *
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

        final Iterator<ProcessFactory> factoryIte = ProcessFinder.getProcessFactories();

        while (factoryIte.hasNext()) {

            final ProcessFactory factory = factoryIte.next();
            ProcessBriefType brief = new ProcessBriefType();

            for (ProcessDescriptor descriptor : factory.getDescriptors()) {
                brief = processBrief(descriptor);
                offering.getProcess().add(brief);
            }
        }

        capabilities.setProcessOfferings(offering);
        return capabilities;
    }

    /*
     *  DiscribeProcess request
     */
    
    /**
     *
     * @param request
     * @return
     * @throws CstlServiceException
     */
    public ProcessDescriptions describeProcess(DescribeProcess request) throws CstlServiceException {
        isWorking();

        final String service = request.getService();
        if (!(service.equalsIgnoreCase(WPS_SERVICE))) {
            throw new CstlServiceException("The version number specified for this request "
                    + "is not handled.", VERSION_NEGOTIATION_FAILED, KEY_VERSION.toLowerCase());
        }

        final String version = request.getVersion().toString();

        if (version.isEmpty()) {
            throw new CstlServiceException("The parameter VERSION must be specified.",
                    MISSING_PARAMETER_VALUE, KEY_VERSION.toLowerCase());
        }

        if (version.equals(ServiceDef.WPS_1_0_0.version.toString())) {
            return describeProcess100((org.geotoolkit.wps.xml.v100.DescribeProcess) request);
        } else {
            throw new CstlServiceException("The version number specified for this discribeProcess request "
                    + "is not handled.", NO_APPLICABLE_CODE, KEY_VERSION.toLowerCase());
        }
    }

    /**
     *
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

        //final List<ProcessDescriptionType> processDescritionList = new ArrayList<ProcessDescriptionType>();
        for (CodeType identifier : request.getIdentifier()) {

            final ProcessDescriptionType descriptionType = new ProcessDescriptionType();
            descriptionType.setIdentifier(identifier);          //Process Identifier

            descriptionType.setProcessVersion("1.0.0");         //TODO set a version to the process
            descriptionType.setWSDL(null);                      //TODO WSDL
            descriptionType.setStatusSupported(false);          //TODO support process status
            descriptionType.setStoreSupported(false);           //TODO support process storage

            //find the process
            final ProcessDescriptor processDesc = getProcessDescriptor(identifier.getValue());
            descriptionType.setTitle(new LanguageStringType(processDesc.getName().getCode()));          //Process Title
            descriptionType.setAbstract(new LanguageStringType(processDesc.getAbstract().toString()));  //Process abstract

            //get process input and output descriptors
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
                    
                    //parameter informations
                    in.setIdentifier(new CodeType(paramDesc.getName().getCode()));
                    in.setTitle(new LanguageStringType(paramDesc.getName().getCode()));
                    in.setAbstract(new LanguageStringType(paramDesc.getRemarks().toString()));

                    //set occurs
                    in.setMaxOccurs(BigInteger.valueOf(paramDesc.getMaximumOccurs()));
                    in.setMinOccurs(BigInteger.valueOf(paramDesc.getMinimumOccurs()));
                    //input class
                    final Class clazz = paramDesc.getValueClass();

                    //BoundingBox type
                    if (clazz.equals(Envelope.class)) {
                        final SupportedCRSsType crsList = new SupportedCRSsType();
                        final SupportedCRSsType.Default defaultCRS = new SupportedCRSsType.Default();
                        defaultCRS.setCRS("EPSG:4326");                                             //TODO confirm the default CRS
                        crsList.setDefault(defaultCRS);
                        final CRSsType supportedCRS = new CRSsType();
                        supportedCRS.getCRS().addAll(SUPPORTED_CRS);
                        crsList.setSupported(supportedCRS); 
                        in.setBoundingBoxData(crsList);

                    } else //Complex type (XML, raster, ...)
                    if (COMPLEX_INPUT_TYPE_LIST.contains(clazz)) {
                        final SupportedComplexDataInputType complex = new SupportedComplexDataInputType();
                        final ComplexDataCombinationsType complexCombs = new ComplexDataCombinationsType();
                        final ComplexDataCombinationType complexComb = new ComplexDataCombinationType();
                        ComplexDataDescriptionType complexDesc = null;

                        for(WPSIO.InputClass inputClass : WPSIO.USEDCLASS){
                            if(clazz.equals(inputClass.getClazz())){
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
                        complex.setMaximumMegabytes(BigInteger.valueOf(2));        // TODO define a maximum size by default 2Mb
                        in.setComplexData(complex);
                        
                    } else {
                        //Simple object (Integer, double, ...) and Object which need a conversion from String like affineTransform or Geometry
                        final LiteralInputType literal = new LiteralInputType();
                        
                        literal.setAnyValue(new AnyValue());
                        if(paramDesc.getDefaultValue() != null){
                            literal.setDefaultValue(paramDesc.getDefaultValue().toString()); //default value if enable
                        }
                        literal.setDataType(createDataType(clazz));

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

                        final SupportedCRSsType crsList = new SupportedCRSsType();
                        final SupportedCRSsType.Default defaultCRS = new SupportedCRSsType.Default();
                        defaultCRS.setCRS("EPSG:4326");                 //TODO confirm the default CRS
                        crsList.setDefault(defaultCRS);
                        final CRSsType supportedCRS = new CRSsType();
                        supportedCRS.getCRS().addAll(SUPPORTED_CRS);
                        crsList.setSupported(supportedCRS);
                        out.setBoundingBoxOutput(crsList);

                    //Complex type (XML, raster, ...)
                    } else if (COMPLEX_INPUT_TYPE_LIST.contains(clazz)) {
                        final SupportedComplexDataType complex = new SupportedComplexDataType();
                        final ComplexDataCombinationsType complexCombs = new ComplexDataCombinationsType();
                        final ComplexDataCombinationType complexComb = new ComplexDataCombinationType();
                        ComplexDataDescriptionType complexDesc = null;

                        for(WPSIO.InputClass inputClass : WPSIO.USEDCLASS){
                            if(clazz.equals(inputClass.getClazz())){
                               
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
                        out.setComplexOutput(complex);

                    } else if(LITERALTYPE_LIST.contains(clazz)){
                        //Simple object (Integer, double) and Object which need a conversion from String like affineTransform or Geometry
                        final LiteralInputType literal = new LiteralInputType();
                        
                        literal.setAnyValue(new AnyValue());
                        if(paramDesc.getDefaultValue() != null){
                            literal.setDefaultValue(paramDesc.getDefaultValue().toString()); //default value if enable
                        }

                        literal.setDataType(createDataType(clazz));

                        out.setLiteralOutput(literal);
                    } else{
                    
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
                    + "is not handled.", VERSION_NEGOTIATION_FAILED, KEY_VERSION.toLowerCase());
        }

        final String version = request.getVersion().toString();
        if (version.isEmpty()) {
            throw new CstlServiceException("The parameter VERSION must be specified.",
                    MISSING_PARAMETER_VALUE, KEY_VERSION.toLowerCase());
        }

        if (version.equals(ServiceDef.WPS_1_0_0.version.toString())) {
            return execute100((org.geotoolkit.wps.xml.v100.Execute) request);
        } else {
            throw new CstlServiceException("The version number specified for this discribeProcess request "
                    + "is not handled.", NO_APPLICABLE_CODE, KEY_VERSION.toLowerCase());
        }
    }

    /**
     *
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
        System.out.println("PROCESS : "+request.getIdentifier().getValue());
        
        //Find the process
        final ProcessDescriptor processDesc = getProcessDescriptor(request.getIdentifier().getValue());

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
            
            if(isLineage){
                System.out.println("LOG : Lineage True");
                //Inputs
                response.setDataInputs(request.getDataInputs());
                final OutputDefinitionsType outputsDef = new OutputDefinitionsType();
                outputsDef.getOutput().addAll(respDoc.getOutput());
                //Outputs
                response.setOutputDefinitions(outputsDef);
            }

            if(useStorage){
                System.out.println("LOG : Storage True");
                response.setStatusLocation(null); //Output data URL
            }

            if(useStatus){
                System.out.println("LOG : Status True");
                response.setStatus(status);
            }
        }else{
            
        }
        //Input temporary files used by the process. In order to delete them at the end of the process.
        List<File> files = null;
        
        //Create Process and Inputs
        final org.geotoolkit.process.Process proc = processDesc.createProcess();
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
            System.out.println("Expected Class = "+expectedClass.getCanonicalName());
            
            /*
             * A referenced input data
             */
            if (inputRequest.getReference() != null) {
                
                //Check if the expected class is supproted for literal using
                if(!isSupportedClass("reference",expectedClass)){
                    throw new CstlServiceException("Reference value expected",INVALID_PARAMETER_VALUE,inputIdentifier); 
                }
                
                System.out.println("LOG : Input Reference");
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
                //System.out.println("Reference data value : "+dataValue);
                
            /*
             * Encapsulated data into the Execute Request
             */
            } else if (inputRequest.getData() != null) {
                
                
                /* BoundingBox data */
                if (inputRequest.getData().getBoundingBoxData() != null) {
                    System.out.println("LOG : Input Boundingbox");
                    final BoundingBoxType bBox = inputRequest.getData().getBoundingBoxData();
                    final List<Double> lower = bBox.getLowerCorner();
                    final List<Double> upper = bBox.getUpperCorner();
                    final String crs = bBox.getCrs();
                    final int dimension = bBox.getDimensions();
                    
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
                    System.out.println("BBOX : "+envelop);
                    dataValue = envelop;

                /* Complex data (XML, raster, ...) */
                } else if (inputRequest.getData().getComplexData() != null) {
                    
                    //Check if the expected class is supproted for literal using
                    if(!isSupportedClass("complex",expectedClass)){
                        throw new CstlServiceException("Complex value expected",INVALID_PARAMETER_VALUE,inputIdentifier); 
                    }
                    
                    System.out.println("LOG : Complex Input");

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
                    
                    
                    System.out.println("Literal");
                    final LiteralDataType literal = inputRequest.getData().getLiteralData();
                    final String data = literal.getValue();
                    
                    //convert String into expected type
                    dataValue = convertFromString(data, expectedClass);
                    System.out.println("Value : "+dataValue);

                } else {
                    throw new CstlServiceException("Invalid input data type.", INVALID_REQUEST, inputIdentifier);
                }
            } else {
                throw new CstlServiceException("Invalid input data format.", INVALID_REQUEST,inputIdentifier);
            }

            //System.out.println("Identifier = "+inputIdentifier+" data = "+dataValue);
                        
            try{
                in.parameter(inputIdentifier).setValue(dataValue);
            }catch(InvalidParameterValueException ex ){
                throw new CstlServiceException("Invalid data input value.",ex, INVALID_PARAMETER_VALUE,inputIdentifier);
            }

        }

        //Give input parameter to the process
        proc.setInput(in);
        
        //Status
        ProcessStartedType started = new ProcessStartedType();
        started.setValue("Process "+request.getIdentifier().getValue()+" is started");
        started.setPercentCompleted(0);
        //status.setProcessStarted(started);
        
        //Run the process
        proc.run();
        
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
                System.out.println("LOG : Raw output");
                final Object outputValue = proc.getOutput().parameter(rawOutputID).getValue();
                System.out.println("OUPUT RAW : "+outputValue);
                
                if(outputValue instanceof Geometry){
                     org.opengis.geometry.Geometry isoGeom;
                    try {
                        final Geometry jtsGeom = (Geometry)outputValue;
                        System.out.println("JTS GEOM "+jtsGeom);
                        final AbstractGeometryType gmlGeom = JTStoGeometry.toGML(jtsGeom);
                        System.out.println("GML GEOM "+gmlGeom);
                        return gmlGeom;
                    } catch (NoSuchAuthorityCodeException ex) {
                        Logger.getLogger(WPSWorker.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (FactoryException ex) {
                        Logger.getLogger(WPSWorker.class.getName()).log(Level.SEVERE, null, ex);
                    }
                   
                }
                
                if(outputValue instanceof Envelope){
                    return new BoundingBoxType((Envelope)outputValue);
                }
                return outputValue;
               
            /* DocumentResponse returned */
            }else{
                System.out.println("LOG : Document output");
                final ExecuteResponse.ProcessOutputs outputs = new ExecuteResponse.ProcessOutputs();
                //Process Outputs
                for (GeneralParameterDescriptor outputDescriptor : processDesc.getOutputDescriptor().descriptors()) {

                    final OutputDataType outData = new OutputDataType();

                    //set Ouput informations
                    final String outputIdentifier = outputDescriptor.getName().getCode();
                    outData.setIdentifier(new CodeType(outputIdentifier));
                    outData.setTitle(new LanguageStringType(outputIdentifier));
                    outData.setAbstract(new LanguageStringType(outputDescriptor.getRemarks().toString()));

                    //get output value from process
                    final Object outputValue = proc.getOutput().parameter(outputIdentifier).getValue();
                    System.out.println("######### OUPUT : "+outputValue);
                    final DataType data = new DataType();
                    if (outputDescriptor instanceof ParameterDescriptor) {

                        final ParameterDescriptor outParamDesc = (ParameterDescriptor) outputDescriptor;
                        final Class outClass = outParamDesc.getValueClass();

                        /* Bounding Box */
                        if (outClass.equals(Envelope.class)) {
                            System.out.println("BoundingBox output");
                            org.opengis.geometry.Envelope envelop = (org.opengis.geometry.Envelope) outputValue;
                            
                            data.setBoundingBoxData(new BoundingBoxType(envelop));

                        /* Complex */
                        } else if (isSupportedClassOutput("complex", outClass)) {
                            System.out.println("Complex output");
                            final ComplexDataType complex = new ComplexDataType();

                            for(DocumentOutputDefinitionType wO : wantedOutputs){
                                if(wO.getIdentifier().getValue().equals(outputIdentifier)){
                                    complex.setEncoding(wO.getEncoding());
                                    complex.setMimeType(wO.getMimeType());
                                    complex.setSchema(wO.getSchema());
                                }
                            }

                            //convert Geometry from JTS to GML
                            if(outputValue instanceof Geometry){
                                System.out.println("Output Complex Geometry");
                                org.opengis.geometry.Geometry isoGeom;
                                AbstractGeometryType gmlGeom = null;
                                try {
                                    final Geometry jtsGeom = (Geometry)outputValue;
                                    System.out.println("JTS GEOM "+jtsGeom);
                                    gmlGeom = JTStoGeometry.toGML(jtsGeom);
                                    System.out.println("GML GEOM "+gmlGeom);
                                } catch (NoSuchAuthorityCodeException ex) {
                                    Logger.getLogger(WPSWorker.class.getName()).log(Level.SEVERE, null, ex);
                                } catch (FactoryException ex) {
                                    Logger.getLogger(WPSWorker.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                
                                complex.getContent().add(gmlGeom);
                                
                            //FeatureCollection    
                            }else if(outputValue instanceof FeatureCollection){
                                System.out.println("LOG : Output Complex FeatureCollection");
                                FeatureCollection fc = (FeatureCollection) outputValue;
                                FeatureType ft = fc.getFeatureType();
                                System.out.println("DEBUG FeatureType: "+ft);
                                System.out.println("DEBUG FeatureCollection: ");
                                //for (Object feat : fc) {
                                    //System.out.println("Feature : "+feat);
                                //}
                                Element elem = null;
                                try {
                                   
                                    final ElementFeatureWriter efw = new ElementFeatureWriter();
                                    elem = efw.writeFeatureCollection(fc, true, false);
                                        
                                } catch (DataStoreException ex) {
                                    throw new CstlServiceException("Can't write FeatureCollection into ResponseDocument",ex, 
                                            NO_APPLICABLE_CODE, outputIdentifier);
                                } catch (ParserConfigurationException ex) {
                                     throw new CstlServiceException("Can't write FeatureCollection into ResponseDocument",ex, 
                                            NO_APPLICABLE_CODE, outputIdentifier);
                                }
                               
                                complex.getContent().add(elem);
                                
                            //Feature
                            }else if(outputValue instanceof Feature){
                                System.out.println("Output Complex Feature");
                                Feature feat = (Feature) outputValue;
                                FeatureType ft = feat.getType();
                                
                                Element elem = null;
                                try {
                                   
                                    final ElementFeatureWriter efw = new ElementFeatureWriter();
                                    elem = efw.writeFeature(feat, null, true);
                                        
                                } catch (ParserConfigurationException ex) {
                                     throw new CstlServiceException("Can't write FeatureCollection into ResponseDocument",ex, 
                                            NO_APPLICABLE_CODE, outputIdentifier);
                                }
                                
                                complex.getContent().add(elem);
                                                                
                            }else{
                                System.out.println("Output Complex "+outputValue.getClass().getName());
                                complex.getContent().add(outputValue);
                            }
                            System.out.println("Output Complex enco: "+complex.getEncoding());
                            System.out.println("Output Complex mime: "+complex.getMimeType());
                            System.out.println("Output Complex schema: "+complex.getSchema());
                            System.out.println("Output Complex value: "+complex.getContent().get(0));
                            
                            data.setComplexData(complex);

                        /* Literal */
                        } else {
                            System.out.println("Literal output : "+outputIdentifier);
                            final LiteralDataType literal = new LiteralDataType();
                            literal.setDataType(outClass.getCanonicalName());
                            if(outputValue == null){
                                literal.setValue(null);
                            }else{
                                literal.setValue(outputValue.toString());
                            }
                            data.setLiteralData(literal);
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
                //Delete input files 
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
            throw new CstlServiceException(ex, INVALID_REQUEST, KEY_VERSION.toLowerCase());
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

        final ProcessBriefType brief = new ProcessBriefType();
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
        brief.setIdentifier(new CodeType(processDesc.getName().getAuthority().getTitle().toString() + "." + processDesc.getName().getCode()));
        brief.setTitle(new LanguageStringType(processDesc.getName().getCode()));
        brief.setAbstract(new LanguageStringType(processDesc.getAbstract().toString()));
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
                System.out.println("Binding "+binding);
                for (ObjectConverter conv : (List<ObjectConverter>) CONVERTERS_LIST) {
                    
                    System.out.println("Convert from String to "+conv.getTargetClass().getSimpleName());
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

    
    private Object extractComplexInput(final Class expectedClass,final List<Object> inputObject,
            final String schema,final String mime,final String encoding, final String inputID) throws CstlServiceException{
        
        Object extractData = null;
        System.out.println("LOG Extract Complex :");
        System.out.println("    LOG Expected Class : "+expectedClass.getSimpleName());
        System.out.println("    LOG Input Object :"+inputObject);
        
        
        /* 
         * Geometry and Geometry[]
         */
        if(expectedClass.equals(Geometry.class) || expectedClass.equals(Geometry[].class)){
            System.out.println("LOG : Input Geometry");
            System.out.println("LOG : Geometry TYpe : "+inputObject.getClass().getName());
            
            try {                
                if(expectedClass.equals(Geometry.class)){
                    if(inputObject.size() == 1){
                        extractData = GeometrytoJTS.toJTS((AbstractGeometryType) inputObject.get(0));
                    }else{
                        throw new CstlServiceException("Invalid data input : Only one geometry expected.", INVALID_PARAMETER_VALUE, inputID);
                    }
                }else{ //Geometry Array
                    List<Geometry> geoms = new ArrayList<Geometry>();
                    for(int i = 0; i<inputObject.size(); i++){
                        geoms.add(GeometrytoJTS.toJTS((AbstractGeometryType) inputObject.get(i)));
                    }
                    extractData = geoms.toArray(new Geometry[geoms.size()]);
                }
                
            }catch(ClassCastException ex){
                throw new CstlServiceException("Invalid data input : empty GML geometry.",ex, INVALID_PARAMETER_VALUE, inputID);
            }catch (FactoryException ex) {
                throw new CstlServiceException("Invalid data input : Cannot convert GML geometry.",ex, INVALID_PARAMETER_VALUE, inputID);
            }

        /* 
         * Feature/FeatureCollection
         */    
        }else if(expectedClass.equals(Feature.class) || expectedClass.equals(FeatureCollection.class)){
            System.out.println("LOG : Extract Complexe Input Feature/FeatureCollection");
            if(inputObject.size() > 1){
               throw new CstlServiceException("Invalid data input : Only one Feature/FeatureCollection expected.", INVALID_PARAMETER_VALUE, inputID);
            }
            
            //Get FeatureType
            List<FeatureType> ft = null;
            if (schema != null) {
                try {
                    final XmlFeatureTypeReader xsdReader = new JAXBFeatureTypeReader();
                    final URL schemaURL = new URL(schema);
                    ft = xsdReader.read(schemaURL.openStream());
                } catch (IOException ex) {
                    throw new CstlServiceException("Unable to read feature type from xsd.", ex, NO_APPLICABLE_CODE);
                } catch (JAXBException ex) {
                    throw new CstlServiceException("Unable to read feature type from xsd.", ex, NO_APPLICABLE_CODE);
                }
            }
            System.out.println("FeatureType : "+ft);
            System.out.println("Content size : "+inputObject.size());
           
            try {
                final XmlFeatureReader fcollReader = new JAXPStreamFeatureReader(ft);
                extractData = fcollReader.read(inputObject.get(0));
               // System.out.println("DATAVALUE : "+dataValue);
            } catch (IOException ex) {
                throw new CstlServiceException("Unable to read feature from nodes.", ex, NO_APPLICABLE_CODE);
            } catch (XMLStreamException ex) {
                throw new CstlServiceException("Unable to read feature from nodes.", ex, NO_APPLICABLE_CODE);
            }
                
            extractData = fixFeature(extractData);
            FeatureCollection feat = (FeatureCollection)extractData;
            System.out.println("FIXED FEATURECOLLECTION :"+feat.getFeatureType()+" FEATURECOLLECTION : "+feat);
            
        /*
         * Feature Array/Collection and FeatureCollection Array/Collection
         */
        }else if(expectedClass.equals(Feature[].class) || expectedClass.equals(FeatureCollection[].class)){
            
            System.out.println("Content size : "+inputObject.size());
            
            try {
                JAXPStreamFeatureReader fcollReader = null;
                
                if(expectedClass.equals(Feature[].class)){
                    
                    final List<Feature> features = new ArrayList<Feature>();
                    for(int i = 0; i<inputObject.size(); i++){
                        
                        fcollReader = new JAXPStreamFeatureReader();
                        //enable to read the FeatureType into the FeatureCollection schema
                        fcollReader.setReadEmbeddedFeatureType(true); 
                        Feature f = (Feature)fcollReader.read(inputObject.get(i));
                        f = (Feature)fixFeature(f);
                        features.add(f);
                    }
                    extractData = features.toArray(new Feature[features.size()]);
                }else{
                    
                    final List<FeatureCollection> features = new ArrayList<FeatureCollection>();
                    for(int i = 0; i<inputObject.size(); i++){
                        
                        fcollReader = new JAXPStreamFeatureReader();
                        //enable to read the FeatureType into the FeatureCollection schema
                        fcollReader.setReadEmbeddedFeatureType(true); 
                        FeatureCollection f = (FeatureCollection)fcollReader.read(inputObject.get(i));
                        f = (FeatureCollection) fixFeature(f);
                        features.add(f);
                    }
                    System.out.println("DEBUG FeatureCollection Array Size :"+features.size());
                    for (FeatureCollection fc : features) {
                        System.out.println("DEBUG : FeatureType : "+fc.getFeatureType());
                        System.out.println("DEBUG : FeatureCollection : "+fc);
                    }
                    extractData = features.toArray(new FeatureCollection[features.size()]);
                }
                
               // System.out.println("DATAVALUE : "+dataValue);
            } catch (IOException ex) {
                throw new CstlServiceException("Unable to read feature from nodes.", ex, NO_APPLICABLE_CODE);
            } catch (XMLStreamException ex) {
                throw new CstlServiceException("Unable to read feature from nodes.", ex, NO_APPLICABLE_CODE);
            }
            
        /*
         * FeatureType
         */
        }else if(expectedClass.equals(FeatureType.class)){
            System.out.println("LOG : FeatureType Input");
            
            if(inputObject.size() > 1){
               throw new CstlServiceException("Invalid data input : Only one FeatureType expected.", INVALID_PARAMETER_VALUE, inputID);
            }
            
            //Get FeatureType
            List<FeatureType> ft = null;
            try {
                System.out.println("LOG Input Object : "+inputObject.get(0));
                final JAXBFeatureTypeReader xsdReader = new JAXBFeatureTypeReader();
                ft = xsdReader.read((Node)inputObject.get(0));
                extractData = ft.get(0);

            } catch (JAXBException ex) {
                throw new CstlServiceException("Unable to read feature type from xsd.", ex, NO_APPLICABLE_CODE); 
            }
            System.out.println("FeatureType : "+ft);
           
        }else{
            throw new CstlServiceException("Requested format is not supported", VERSION_NEGOTIATION_FAILED, inputID);
        }
        
        return extractData;
    }
    
    /**
     * Get an convert data from a reference for an expected binding
     * Supported binding and mime :
     * <ul>
     *      <li>Feature :
     *          <ul>
     *              <li>text/xml</li>
     *              <li>application/octec-stream (Shp file) TODO fix midding CRS</li>
     *          </ul>
     *      </li>
     *      <li>FeatureCollection :
     *          <ul>
     *              <li>text/xml</li>
     *              <li>application/octec-stream (Shp file) TODO fix midding CRS</li>
     *          </ul>
     *      </li>
     *      <li>Geometry :
     *          <ul>
     *              <li>text/xml</li>
     *          </ul>
     *      </li>
     *      <li>File :
     *          <ul>
     *              <li>all text/binary (download and store in a tempFile)</li>
     *          </ul>
     *      </li>
     *      <li>GridCoverageReader :
     *          <ul>
     *              <li>all text/binary (download and store in a tempFile)</li>
     *          </ul>
     *      </li>
     * </ul>
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
        System.out.println(" HREF :"+href);
        System.out.println(" Schema : "+schema);
        System.out.println(" mime : "+mime);

        try {
            href = URLDecoder.decode(href, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new CstlServiceException("Invalid reference href.",ex, INVALID_PARAMETER_VALUE, inputID);
        }
        
       /*
        * Feature/FeatureCollection
        */
        if(expectedClass.equals(Feature.class) || expectedClass.equals(FeatureCollection.class)){
            System.out.println("LOG Extract Reference Input Feature/FeatureCollection");
            if (mime == null) {
                throw new CstlServiceException("Invalid reference input : typeMime can't be null.", INVALID_PARAMETER_VALUE, inputID);
            }
            //XML
            if(mime.equalsIgnoreCase(MimeType.TEXT_XML)){
                 try {
                    final XmlFeatureTypeReader xsdReader = new JAXBFeatureTypeReader();
                    JAXPStreamFeatureReader fcollReader;
                    
                    if(schema != null){
                        System.out.println(" Schema  URL : "+schema);
                        final URL schemaURL = new URL(schema);
                        fcollReader = new JAXPStreamFeatureReader(xsdReader.read(schemaURL.openStream()));
                    }else{
                         fcollReader = new JAXPStreamFeatureReader();
                         fcollReader.setReadEmbeddedFeatureType(true);
                    }
                    
                    FeatureCollection fcoll = (FeatureCollection)fcollReader.read(new URL(href));
                     System.out.println("FeatureCollection Type : "+fcoll.getFeatureType());
                     fcoll = (FeatureCollection) fixFeature(fcoll);
                     System.out.println("Fixed FeatureType : "+fcoll.getFeatureType());
                     System.out.println("FeatureCollec : "+fcoll);
                    return fcoll;

                } catch (JAXBException ex) {
                    throw new CstlServiceException("Invalid reference input : can't read reference schema.",ex, NO_APPLICABLE_CODE, inputID);
                }catch (MalformedURLException ex){
                    throw new CstlServiceException("Invalid reference input : Malformed schema or resource.",ex, NO_APPLICABLE_CODE, inputID);
                }catch (IOException ex){
                    throw new CstlServiceException("Invalid reference input : IO.",ex, NO_APPLICABLE_CODE, inputID);
                }catch (XMLStreamException ex) {
                    throw new CstlServiceException("Invalid reference input.",ex, NO_APPLICABLE_CODE, inputID);
                }
            // SHP
            }else if(mime.equalsIgnoreCase("application/octec-stream")){

                try {
                    Hints.putSystemDefault(Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE);
                    final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
                    parameters.put("url", new URL(href));
                    System.out.println("URL :"+new URL(href).toString());
                    final DataStore store = DataStoreFinder.getDataStore(parameters);

                    if(store == null){
                        throw new CstlServiceException("Invalid URL", NO_APPLICABLE_CODE, inputID);
                    }

                    if(store.getNames().size() != 1){
                        throw new CstlServiceException("More than one FeatureCollection in the file", NO_APPLICABLE_CODE, inputID);
                    }

                    final FeatureCollection collection = store.createSession(true).getFeatureCollection(QueryBuilder.all(store.getNames().iterator().next()));
                    System.out.println("FEATURE TYPE : "+collection.getFeatureType());
                    if(collection != null){
                        return collection;
                    }else{
                        throw new CstlServiceException("Collection not found", NO_APPLICABLE_CODE, inputID);
                    }
                    
                } catch (DataStoreException ex) {
                    throw new CstlServiceException("Invalid reference input : Malformed schema or resource.",ex, NO_APPLICABLE_CODE, inputID);
                }catch (MalformedURLException ex){
                    throw new CstlServiceException("Invalid reference input : Malformed schema or resource.",ex, NO_APPLICABLE_CODE, inputID);
                }
                
            }else {
                 throw new CstlServiceException("Reference data mime is not supported", VERSION_NEGOTIATION_FAILED, inputID);
            }
            
       /*
        * Geometry
        */   
        }else if(expectedClass.equals(Geometry.class)){
            if (mime == null) {
                throw new CstlServiceException("Invalid reference input : typeMime can't be null.", INVALID_PARAMETER_VALUE, inputID);
            }
            if(mime.equalsIgnoreCase(MimeType.TEXT_XML)){
                try {
                    final Unmarshaller unmarsh = getMarshallerPool().acquireUnmarshaller();
                    Object value = unmarsh.unmarshal(new URL(href));
                    if(value instanceof JAXBElement){
                        value = ((JAXBElement)value).getValue();
                    }
                    return GeometrytoJTS.toJTS((AbstractGeometryType) value);
                    
                } catch (NoSuchAuthorityCodeException ex) {
                    throw new CstlServiceException("Reference geometry invalid input", INVALID_PARAMETER_VALUE, inputID);
                } catch (FactoryException ex) {
                    throw new CstlServiceException("Reference geometry invalid input", INVALID_PARAMETER_VALUE, inputID);
                } catch (MalformedURLException ex) {
                    throw new CstlServiceException("Reference geometry invalid input : Malformed url", INVALID_PARAMETER_VALUE, inputID);
                } catch (JAXBException ex) {
                    throw new CstlServiceException("Reference geometry invalid input : Unmarshallable geometry", INVALID_PARAMETER_VALUE, inputID);
                }
            }else{
                 throw new CstlServiceException("Reference data mime is not supported", INVALID_PARAMETER_VALUE, inputID);
            }
            
        /*
         * File
         */
        }else if(expectedClass.equals(File.class)){
            try {
                final URL u = new URL(href);
                final URLConnection uc = u.openConnection();
                final String contentType = uc.getContentType();
                final int contentLength = uc.getContentLength();
              
                
                final InputStream raw = uc.getInputStream();
                final InputStream in = new BufferedInputStream(raw);
                
                
                // get filename from the path
                String filename = u.getFile();
                filename = filename.substring(filename.lastIndexOf('/') + 1);
                int dotPos = filename.lastIndexOf(".");
                int len = filename.length();
                String name = filename.substring(0, dotPos);
                String ext = filename.substring(dotPos+1,len) ;
                
                //Create a temp file
                File file = File.createTempFile(name, ext);
                file.deleteOnExit();
                final FileOutputStream out = new FileOutputStream(file);
                
                final byte[] data = new byte[contentLength];
                byte[] readData = new byte[1024];
                int i = in.read(readData);

                while (i != -1) {
                    out.write(readData, 0, i);
                    i = in.read(readData);
                }
               
                in.close();

                out.write(data);
                out.flush();
                out.close();
                
                return file;
            }catch (MalformedURLException ex) {
                throw new CstlServiceException("Reference file invalid input : Malformed url", INVALID_PARAMETER_VALUE, inputID);
            } catch (IOException ex) {
                throw new CstlServiceException("Reference file invalid input : IO", INVALID_PARAMETER_VALUE, inputID);
            } 
           
         
        /*
         * GridCoverageReader
         */          
        }else if(expectedClass.equals(GridCoverageReader.class)){
            try {
                return GridCoverageReaders.createMosaicReader(new URL(href));
            } catch (MalformedURLException ex) {
                throw new CstlServiceException("Reference grid coverage invalid input : Malformed url",ex, VERSION_NEGOTIATION_FAILED, inputID);
            } catch (CoverageStoreException ex) {
                throw new CstlServiceException("Reference grid coverage invalid input : Can't read coverage",ex, VERSION_NEGOTIATION_FAILED, inputID);
            } catch (IOException ex) {
                throw new CstlServiceException("Reference grid coverage invalid input : IO",ex, VERSION_NEGOTIATION_FAILED, inputID);
            }
        
        /*
         * GridCoverage2D
         */          
        }else if(expectedClass.equals(GridCoverage2D.class)){
            try {
                final GridCoverageReader reader = GridCoverageReaders.createMosaicReader(new URL(href));
                return (GridCoverage2D)reader.read(0, null);
            } catch (MalformedURLException ex) {
                throw new CstlServiceException("Reference grid coverage invalid input : Malformed url",ex, VERSION_NEGOTIATION_FAILED, inputID);
            } catch (CoverageStoreException ex) {
                throw new CstlServiceException("Reference grid coverage invalid input : Can't read coverage",ex, VERSION_NEGOTIATION_FAILED, inputID);
            } catch (IOException ex) {
                throw new CstlServiceException("Reference grid coverage invalid input : IO",ex, VERSION_NEGOTIATION_FAILED, inputID);
            }
            
        /*
         * FeatureType
         */
        }else if(expectedClass.equals(FeatureType.class)){
            if (mime == null) {
                throw new CstlServiceException("Invalid reference input : typeMime can't be null.", INVALID_PARAMETER_VALUE, inputID);
            }
            //XML
            if(mime.equalsIgnoreCase(MimeType.TEXT_XML)){
                 try {
                    final XmlFeatureTypeReader xsdReader = new JAXBFeatureTypeReader();
                    final URL schemaURL = new URL(href);
                    final List<FeatureType> ft = xsdReader.read(schemaURL.openStream());
                    
                    if(ft.size() != 1){
                        throw new CstlServiceException("Invalid reference input : More than one FeatureType in schema.", INVALID_PARAMETER_VALUE, inputID);
                    }
                    return ft.get(0);
                } catch (JAXBException ex) {
                    throw new CstlServiceException("Invalid reference input : can't read reference schema.",ex, NO_APPLICABLE_CODE, inputID);
                }catch (MalformedURLException ex){
                    throw new CstlServiceException("Invalid reference input : Malformed schema or resource.",ex, NO_APPLICABLE_CODE, inputID);
                }catch (IOException ex){
                    throw new CstlServiceException("Invalid reference input : IO.",ex, NO_APPLICABLE_CODE, inputID);
                }
            }else {
                 throw new CstlServiceException("Reference data mime is not supported", VERSION_NEGOTIATION_FAILED, inputID);
            }
        }else{
            throw new CstlServiceException("Requested format is not supported", VERSION_NEGOTIATION_FAILED, inputID);
        }
    }

    private boolean isSupportedClass(String type, Class expectedClass ){
        boolean supportedClass = false;
        if(type.equals("literal")){
            for(Object obj : LITERALTYPE_LIST){
                Class clazz = (Class)obj;
                if(clazz.isAssignableFrom(expectedClass)){
                    supportedClass = true;
                    break;
                }
            }
        }else if(type.equals("complex")){
            for(Object obj : COMPLEX_INPUT_TYPE_LIST){
                Class clazz = (Class)obj;
                if(clazz.isAssignableFrom(expectedClass)){
                    supportedClass = true;
                    break;
                }
            }
        }else if(type.equals("reference")){
            for(Object obj : REFERENCETYPE_LIST){
                Class clazz = (Class)obj;
                if(clazz.isAssignableFrom(expectedClass)){
                    supportedClass = true;
                    break;
                }
            }
        }
        return supportedClass;
    }
    
    private boolean isSupportedClassOutput(String type, Class expectedClass ){
        boolean supportedClass = false;
        if(type.equals("complex")){
            for(Object obj : COMPLEX_OUTPUT_TYPE_LIST){
                Class clazz = (Class)obj;
                if(clazz.isAssignableFrom(expectedClass)){
                    supportedClass = true;
                    break;
                }
            }
        }
        return supportedClass;
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

    private Object fixFeature(final Object dataValue) throws CstlServiceException {
        
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

    private void fixFeatureType(final Feature featureIN, DefaultFeatureType type) throws CstlServiceException{
        AttributeDescriptorBuilder descBuilder;
        AttributeTypeBuilder typeBuilder;
        
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
                    throw new CstlServiceException("Can't find feature geometry CRS");
                } catch (FactoryException ex) {
                    throw new CstlServiceException("Can't find feature geometry CRS");
                }
            }
        }
    } 
    
}
