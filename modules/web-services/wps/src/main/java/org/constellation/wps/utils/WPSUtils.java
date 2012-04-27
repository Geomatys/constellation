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
package org.constellation.wps.utils;

import com.vividsolutions.jts.geom.Geometry;
import java.io.File;
import java.io.IOException;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.constellation.util.Util;
import org.constellation.wps.converters.inputs.AbstractInputConverter;

import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.ows.xml.v110.CodeType;
import org.geotoolkit.ows.xml.v110.LanguageStringType;
import org.geotoolkit.wps.xml.v100.ProcessBriefType;
import org.geotoolkit.util.ArgumentChecks;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.FeatureIterator;
import org.geotoolkit.feature.FeatureTypeBuilder;
import org.geotoolkit.feature.type.DefaultFeatureType;
import org.geotoolkit.feature.type.DefaultGeometryType;
import org.geotoolkit.feature.type.DefaultPropertyDescriptor;
import org.geotoolkit.geometry.jts.JTS;
import org.geotoolkit.ows.xml.v110.DomainMetadataType;
import org.geotoolkit.util.converter.ConverterRegistry;
import org.geotoolkit.util.converter.NonconvertibleObjectException;
import org.geotoolkit.util.converter.ObjectConverter;
import org.geotoolkit.util.logging.Logging;
import org.geotoolkit.wps.xml.v100.SupportedUOMsType;
import org.geotoolkit.wps.xml.v100.UOMsType;
import org.geotoolkit.wps.xml.v100.SupportedComplexDataInputType;
import org.geotoolkit.wps.xml.v100.ComplexDataCombinationsType;
import org.geotoolkit.wps.xml.v100.ComplexDataCombinationType;
import org.geotoolkit.wps.xml.v100.ComplexDataDescriptionType;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.NoSuchIdentifierException;

import org.opengis.util.FactoryException;
import org.constellation.ws.CstlServiceException;
import org.constellation.wps.ws.WPSIO;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import static org.constellation.wps.ws.WPSConstant.*;
import org.geotoolkit.wps.xml.WPSMarshallerPool;
import org.geotoolkit.wps.xml.v100.*;
import org.geotoolkit.xml.MarshallerPool;
import org.opengis.parameter.ParameterDescriptorGroup;

/**
 * Set of utilities method used by WPS worker.
 *
 * @author Quentin Boileau (Geomatys).
 */
public class WPSUtils {

    private static final Logger LOGGER = Logging.getLogger(WPSUtils.class);

    private WPSUtils() {
    }

    /**
     * Return the process descriptor from a process identifier
     *
     * @param identifier like "urn:ogc:geomatys:wps:math:add"
     * @return ProcessDescriptor
     * @throws CstlServiceException in case of an unknown process identifier.
     */
    public static ProcessDescriptor getProcessDescriptor(final String identifier) throws CstlServiceException {
      
        final String processFactory = extractFactoryFromIdentifier(identifier);
        final String processName = extractProcessNameFromIdentifier(identifier);

        ProcessDescriptor processDesc = null;
        try {
            processDesc = ProcessFinder.getProcessDescriptor(processFactory, processName);

        } catch (NoSuchIdentifierException ex) {
            throw new CstlServiceException("The process " + IDENTIFER_PARAMETER.toLowerCase() + " : " + identifier + " does not exist.",
                    INVALID_PARAMETER_VALUE, IDENTIFER_PARAMETER.toLowerCase());
        }
        return processDesc;
    }

    /**
     * Return a brief description of the process from his process descriptor.
     *
     * @param processDesc
     * @return ProcessBriefType
     */
    public static ProcessBriefType generateProcessBrief(final ProcessDescriptor processDesc) {

        final ProcessBriefType brief = new ProcessBriefType();
        brief.setIdentifier(new CodeType(buildProcessIdentifier(processDesc)));
        brief.setTitle(buildProcessTitle(processDesc));
        brief.setAbstract(capitalizeFirstLetter(processDesc.getProcedureDescription().toString()));
        brief.setProcessVersion(WPS_1_0_0);
        brief.setWSDL(null);

        return brief;
    }

    /**
     * Build OGC URN unique identifier for a process from his process descriptor.
     *
     * @param processDesc
     * @return
     */
    public static LanguageStringType buildProcessTitle(final ProcessDescriptor processDesc) {
        ArgumentChecks.ensureNonNull("processDesc", processDesc);
        final String title = capitalizeFirstLetter(processDesc.getIdentifier().getAuthority().getTitle().toString()).getValue() + " : "
                + capitalizeFirstLetter(processDesc.getIdentifier().getCode()).getValue();
        return new LanguageStringType(title);
    }

    /**
     * Build OGC URN unique identifier for a process from his process descriptor.
     *
     * @param processDesc
     * @return
     */
    public static String buildProcessIdentifier(final ProcessDescriptor processDesc) {
        ArgumentChecks.ensureNonNull("processDesc", processDesc);
        return PROCESS_PREFIX + processDesc.getIdentifier().getAuthority().getTitle().toString() + ":" + processDesc.getIdentifier().getCode();
    }

    /**
     * Extract the factory name from a process identifier. e.g : urn:ogc:geomatys:wps:math:add return math.
     *
     * @param identifier
     * @return factory name.
     */
    public static String extractFactoryFromIdentifier(final String identifier) throws CstlServiceException {
        ArgumentChecks.ensureNonNull("identifier", identifier);
        final String factAndProcess = extractFactoryAndProcessNameFromIdentifier(identifier);
        if (factAndProcess.contains(":")) {
            return factAndProcess.split(":")[0];
        }
        throw new CstlServiceException("Invalid process identifier. Must be an URN code like " + PROCESS_PREFIX + "factory:process",
                INVALID_PARAMETER_VALUE, IDENTIFER_PARAMETER.toLowerCase());
    }

    /**
     * Extract the process name from a process identifier. e.g : urn:ogc:geomatys:wps:math:add return add.
     *
     * @param identifier
     * @return process name.
     */
    public static String extractProcessNameFromIdentifier(final String identifier) throws CstlServiceException {
        ArgumentChecks.ensureNonNull("identifier", identifier);
        final String factAndProcess = extractFactoryAndProcessNameFromIdentifier(identifier);
        if (factAndProcess.contains(":")) {
            return factAndProcess.split(":")[1];
        }
        throw new CstlServiceException("Invalid process identifier. Must be an URN code like " + PROCESS_PREFIX + "factory:process",
                INVALID_PARAMETER_VALUE, IDENTIFER_PARAMETER.toLowerCase());
    }

    /**
     * Extract the factory name and the process name from a process identifier. e.g : urn:ogc:geomatys:wps:math:add
     * return math:add.
     *
     * @param identifier
     * @return factoryName:processName.
     */
    private static String extractFactoryAndProcessNameFromIdentifier(final String identifier) throws CstlServiceException {
        if (identifier.contains(PROCESS_PREFIX)) {
            return identifier.replace(PROCESS_PREFIX, "");
        }
        throw new CstlServiceException("Invalid process identifier. Must be an URN code like " + PROCESS_PREFIX + "factory:process",
                INVALID_PARAMETER_VALUE, IDENTIFER_PARAMETER.toLowerCase());
    }

    /**
     * Generate process INPUT/OUPTUT identifiers based on process identifier. e.g :
     * urn:ogc:geomatys:wps:math:add:input:number1, urn:ogc:geomatys:wps:math:add:ouput:result
     *
     * @param procDesc
     * @param input
     * @param ioType
     * @return processIdentifier:ioType:paramName
     */
    public static String buildProcessIOIdentifiers(final ProcessDescriptor procDesc, final ParameterDescriptor input,
            final WPSIO.IOType ioType) {
        if (input != null) {
            if (ioType.equals(WPSIO.IOType.INPUT)) {
                return buildProcessIdentifier(procDesc) + ":input:" + input.getName().getCode();
            } else {
                return buildProcessIdentifier(procDesc) + ":output:" + input.getName().getCode();
            }
        }
        return null;
    }

    /**
     * Extract the process INPUT/OUPTUT code. e.g : urn:ogc:geomatys:wps:math:add:input:number1 will return number1
     *
     * @param identifier Input/Output identifier.
     * @return string code.
     */
    public static String extractProcessIOCode(final String identifier) {
        ArgumentChecks.ensureNonNull("identifier", identifier);

        return identifier.substring(identifier.lastIndexOf(":") + 1, identifier.length());
    }

    /**
     * Return the given String with the first letter to upper case.
     *
     * @param value
     * @return LanguageStringType
     */
    public static LanguageStringType capitalizeFirstLetter(final String value) {
        if (value != null && !value.isEmpty()) {

            final StringBuilder result = new StringBuilder(value);
            result.replace(0, 1, result.substring(0, 1).toUpperCase());
            return new LanguageStringType(result.toString());
        }
        return new LanguageStringType(value);
    }

    /**
     * Generate supported UOM (Units) for a given ParameterDescriptor. If this descriptor have default unit, supported
     * UOM returned will be all the compatible untis to the default one.
     *
     * @param param
     * @return SupportedUOMsType or null if the parameter does'nt have any default unit.
     */
    static public SupportedUOMsType generateUOMs(final ParameterDescriptor param) {
        if (param != null && param.getUnit() != null) {
            final Unit unit = param.getUnit();
            final Set<Unit<?>> siUnits = SI.getInstance().getUnits();
            final Set<Unit<?>> nonisUnits = NonSI.getInstance().getUnits();

            final SupportedUOMsType supportedUOMsType = new SupportedUOMsType();
            final SupportedUOMsType.Default defaultUOM = new SupportedUOMsType.Default();
            final UOMsType supportedUOM = new UOMsType();

            defaultUOM.setUOM(new DomainMetadataType(unit.toString(), null));
            for (Unit u : siUnits) {
                if (unit.isCompatible(u)) {
                    supportedUOM.getUOM().add(new DomainMetadataType(u.toString(), null));
                }
            }
            for (Unit u : nonisUnits) {
                if (unit.isCompatible(u)) {
                    supportedUOM.getUOM().add(new DomainMetadataType(u.toString(), null));
                }
            }
            supportedUOMsType.setDefault(defaultUOM);
            supportedUOMsType.setSupported(supportedUOM);
            return supportedUOMsType;
        }
        return null;
    }

    /**
     * Test if a process is supported by the WPS.
     *
     * @param descriptor
     * @return true if process is supported, false if is not.
     */
    public static boolean isSupportedProcess(ProcessDescriptor descriptor) {

        //Inputs
        final List<GeneralParameterDescriptor> inputDesc = descriptor.getInputDescriptor().descriptors();
        for (GeneralParameterDescriptor input : inputDesc) {
            if (!(input instanceof ParameterDescriptor)) {
                return false;
            } else {
                final ParameterDescriptor inputParam = (ParameterDescriptor) input;
                final Class inputClass = inputParam.getValueClass();
                if (!WPSIO.isSupportedInputClass(inputClass)) {
                    return false;
                }
            }
        }

        //Outputs
        final List<GeneralParameterDescriptor> outputDesc = descriptor.getOutputDescriptor().descriptors();
        for (GeneralParameterDescriptor output : outputDesc) {
            if (!(output instanceof ParameterDescriptor)) {
                return false;
            } else {
                final ParameterDescriptor outputParam = (ParameterDescriptor) output;
                final Class outputClass = outputParam.getValueClass();
                if (!WPSIO.isSupportedOutputClass(outputClass)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Create the DomaineMetaData object for a literal
     *
     * @param clazz
     * @return
     * @throws CstlServiceException
     */
    public static DomainMetadataType createDataType(final Class clazz) throws CstlServiceException {


        if (clazz.equals(Double.class)) {
            return new DomainMetadataType("Double", "http://www.w3.org/TR/xmlschema-2/#double");

        } else if (clazz.equals(Float.class)) {
            return new DomainMetadataType("Float", "http://www.w3.org/TR/xmlschema-2/#float");

        } else if (clazz.equals(Boolean.class)) {
            return new DomainMetadataType("Boolean", "http://www.w3.org/TR/xmlschema-2/#boolean");

        } else if (clazz.equals(Integer.class)) {
            return new DomainMetadataType("Integer", "http://www.w3.org/TR/xmlschema-2/#integer");

        } else if (clazz.equals(Long.class)) {
            return new DomainMetadataType("Long", "http://www.w3.org/TR/xmlschema-2/#long");

        } else if (clazz.equals(String.class) || WPSIO.isSupportedInputClass(clazz) || WPSIO.isSupportedOutputClass(clazz)) {
            return new DomainMetadataType("String", "http://www.w3.org/TR/xmlschema-2/#string");

        } else {
            throw new CstlServiceException("No supported literal type");
        }
    }

    /**
     * Convert a string to a binding class. If the binding class isn't a primitive like Integer, Double, .. we search
     * into the converter list if found a match.
     *
     * @param data string to convert
     * @param binding wanted class
     * @return converted object
     * @throws CstlServiceException if there is no match found
     */
    public static <T> Object convertFromString(final String data, final Class binding) throws CstlServiceException {

        Object convertedData = null; //resulting Object
        try {

            ObjectConverter<String, T> converter;//converter
            try {
                //try to convert into a primitive type
                converter = ConverterRegistry.system().converter(String.class, binding);
            } catch (NonconvertibleObjectException ex) {
                //try to convert with some specified converter
                converter = WPSIO.getConverter(binding, WPSIO.IOType.INPUT, WPSIO.DataType.LITERAL, null, null, null);

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
    public static Object extractComplexInput(final Class expectedClass, final List<Object> inputObject,
            final String schema, final String mime, final String encoding, final String inputID) throws CstlServiceException {

        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(AbstractInputConverter.IN_DATA, inputObject);
        parameters.put(AbstractInputConverter.IN_MIME, mime);
        parameters.put(AbstractInputConverter.IN_SCHEMA, schema);
        parameters.put(AbstractInputConverter.IN_ENCODING, encoding);

        final ObjectConverter converter = WPSIO.getConverter(expectedClass, WPSIO.IOType.INPUT, WPSIO.DataType.COMPLEX, mime, encoding, schema);

        if (converter == null) {
            throw new CstlServiceException("Input complex not supported, no converter found.", OPERATION_NOT_SUPPORTED, inputID);
        }

        try {
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
    public static Object reachReferencedData(String href, final String method, final String mime,
            final String encoding, final String schema, final Class expectedClass, final String inputID) throws CstlServiceException {


        if (href == null) {
            throw new CstlServiceException("Invalid reference input : href can't be null.", INVALID_PARAMETER_VALUE, inputID);
        }

        try {
            href = URLDecoder.decode(href, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new CstlServiceException("Invalid reference href.", ex, INVALID_PARAMETER_VALUE, inputID);
        }

        final Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(AbstractInputConverter.IN_HREF, href);
        parameters.put(AbstractInputConverter.IN_MIME, mime);
        parameters.put(AbstractInputConverter.IN_SCHEMA, schema);
        parameters.put(AbstractInputConverter.IN_ENCODING, encoding);

        final ObjectConverter converter = WPSIO.getConverter(expectedClass, WPSIO.IOType.INPUT, WPSIO.DataType.REFERENCE, mime, encoding, schema);

        if (converter == null) {
            throw new CstlServiceException("Input reference not supported, no converter found.", OPERATION_NOT_SUPPORTED, inputID);
        }

        try {
            return converter.convert(parameters);

        } catch (NonconvertibleObjectException ex) {
            throw new CstlServiceException(ex, INVALID_PARAMETER_VALUE, inputID);
        }

    }

    /**
     * Fix the CRS problem for a Feature or a FeatureCollection
     *
     * @param dataValue a Feature or a FeatureCollection
     * @return the sale Feature/FeatureCollection fixed
     * @throws CstlServiceException
     */
    public static Object fixFeature(final Object dataValue) throws CstlServiceException {

        if (dataValue instanceof Feature) {

            final Feature featureIN = (Feature) dataValue;
            DefaultFeatureType ft = (DefaultFeatureType) featureIN.getType();
            fixFeatureType(featureIN, ft);

            return featureIN;
        }

        if (dataValue instanceof FeatureCollection) {
            final FeatureCollection featureColl = (FeatureCollection) dataValue;

            DefaultFeatureType ft = (DefaultFeatureType) featureColl.getFeatureType();
            final FeatureIterator featureIter = featureColl.iterator();
            if (featureIter.hasNext()) {
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
     *
     * @param featureIN feature with geometry used to fix the geometry descriptor
     * @param type the featureType to fix
     * @throws CstlServiceException
     */
    public static void fixFeatureType(final Feature featureIN, DefaultFeatureType type) throws CstlServiceException {

        final FeatureTypeBuilder ftb = new FeatureTypeBuilder();
        ftb.copy(type);

        //Fetch each geometry, get his CRS and 
        for (Property property : featureIN.getProperties()) {
            if (property.getDescriptor() instanceof GeometryDescriptor) {
                try {
                    final String propertyName = property.getName().getLocalPart();
                    final Geometry propertyGeom = (Geometry) property.getValue();
                    final CoordinateReferenceSystem extractCRS = JTS.findCoordinateReferenceSystem(propertyGeom);

                    final Iterator<PropertyDescriptor> ite = type.getDescriptors().iterator();

                    while (ite.hasNext()) {
                        final DefaultPropertyDescriptor propertyDesc = (DefaultPropertyDescriptor) ite.next();

                        if (propertyDesc.getName().getLocalPart().equals(propertyName)) {
                            final DefaultGeometryType geomType = (DefaultGeometryType) propertyDesc.getType();
                            geomType.setCoordinateReferenceSystem(extractCRS);
                            break;
                        }
                    }
                } catch (NoSuchAuthorityCodeException ex) {
                    throw new CstlServiceException("Can't find feature geometry CRS", ex, NO_APPLICABLE_CODE);
                } catch (FactoryException ex) {
                    throw new CstlServiceException("Can't find feature geometry CRS", ex, NO_APPLICABLE_CODE);
                }
            }
        }
    }

    /**
     * Return the SupportedComplexDataInputType for the given class.
     *
     * @param attributeClass
     * @return SupportedComplexDataInputType
     */
    public static SupportedComplexDataInputType describeComplex(final Class attributeClass, final WPSIO.IOType ioType) {

        final SupportedComplexDataInputType complex = new SupportedComplexDataInputType();
        final ComplexDataCombinationsType complexCombs = new ComplexDataCombinationsType();
        final ComplexDataCombinationType complexComb = new ComplexDataCombinationType();
        final List<WPSIO.DataInfo> infos = WPSIO.IOCLASSMAP.get(new WPSIO.KeyTuple(attributeClass, ioType, WPSIO.DataType.COMPLEX));

        for (WPSIO.DataInfo inputClass : infos) {

            final ComplexDataDescriptionType complexDesc = new ComplexDataDescriptionType();
            complexDesc.setEncoding(inputClass.getEncoding().getValue());   //Encoding
            complexDesc.setMimeType(inputClass.getMime().getValue());       //Mime
            complexDesc.setSchema(inputClass.getSchema().getValue());       //URL to xsd schema

            if (inputClass.isDefaultIO()) {
                complexComb.setFormat(complexDesc);
            }
            complexCombs.getFormat().add(complexDesc);
        }

        complex.setDefault(complexComb);
        complex.setSupported(complexCombs);

        //Set MaximumMegabyte only for the complex input descritpion
        if (ioType == WPSIO.IOType.INPUT) {
            complex.setMaximumMegabytes(BigInteger.valueOf(MAX_MB_INPUT_COMPLEX));
        }
        return complex;
    }

    /**
     * Check if all requested inputs/outputs are presente in the process descriptor. Check also if all mandatory
     * inputs/outputs are specified. If an non allowed input/output is requested or if a mandatory input/output is
     * missing, an {@link CstlServiceException CstlServiceException} will be throw.
     *
     * @param processDesc
     * @param request
     * @throws CstlServiceException if an non allowed input/output is requested or if a mandatory input/output is
     * missing.
     */
    public static void checkValidInputOuputRequest(final ProcessDescriptor processDesc, final Execute request) throws CstlServiceException {

        //check inputs
        final List<String> inputIdentifiers = extractRequestInputIdentifiers(request);
        final ParameterDescriptorGroup inputDescriptorGroup = processDesc.getInputDescriptor();
        final Map<String, Boolean> inputDescMap = desciptorsAsMap(inputDescriptorGroup, processDesc, WPSIO.IOType.INPUT);
        checkIOIdentifiers(inputDescMap, inputIdentifiers, WPSIO.IOType.INPUT);

        //check outputs
        final List<String> outputIdentifiers = extractRequestOutputIdentifiers(request);
        final ParameterDescriptorGroup outputDescriptorGroup = processDesc.getOutputDescriptor();
        final Map<String, Boolean> outputDescMap = desciptorsAsMap(outputDescriptorGroup, processDesc, WPSIO.IOType.OUTPUT);
        checkIOIdentifiers(outputDescMap, outputIdentifiers, WPSIO.IOType.OUTPUT);

    }

    /**
     * Extract the list of identifiers {@code String} requested in input.
     *
     * @param request
     * @return a list of identifiers
     */
    public static List<String> extractRequestInputIdentifiers(final Execute request) {

        final List<String> identifiers = new ArrayList<String>();
        if (request != null && request.getDataInputs() != null) {
            final DataInputsType dataInput = request.getDataInputs();

            final List<InputType> inputs = dataInput.getInput();
            for (final InputType in : inputs) {
                identifiers.add(in.getIdentifier().getValue());
            }
        }
        return identifiers;
    }

    /**
     * Extract the list of identifiers {@code String} requested in output.
     *
     * @param request
     * @return a list of identifiers
     */
    public static List<String> extractRequestOutputIdentifiers(final Execute request) {

        final List<String> identifiers = new ArrayList<String>();
        if (request != null && request.getResponseForm() != null) {
            final ResponseFormType responseForm = request.getResponseForm();

            if (responseForm.getRawDataOutput() != null) {
                identifiers.add(responseForm.getRawDataOutput().getIdentifier().getValue());

            } else if (responseForm.getResponseDocument() != null && responseForm.getResponseDocument().getOutput() != null) {

                final List<DocumentOutputDefinitionType> outputs = responseForm.getResponseDocument().getOutput();
                for (final DocumentOutputDefinitionType out : outputs) {
                    identifiers.add(out.getIdentifier().getValue());
                }
            }
        }
        return identifiers;
    }

    /**
     * Build a {@code Map} from a {@link ParameterDescriptorGroup ParameterDescriptorGroup}. The map keys are the
     * parameter identifier as code and the boolean value the mandatory of the parameter.
     *
     * @param descGroup
     * @return all parameters code and there mandatory value as map.
     */
    private static Map<String, Boolean> desciptorsAsMap(final ParameterDescriptorGroup descGroup, final ProcessDescriptor procDesc,
            final WPSIO.IOType iOType) {

        final Map<String, Boolean> map = new HashMap<String, Boolean>();
        if (descGroup != null && descGroup.descriptors() != null) {
            final List<GeneralParameterDescriptor> descriptors = descGroup.descriptors();

            for (final GeneralParameterDescriptor geneDesc : descriptors) {
                if (geneDesc instanceof ParameterDescriptor) {
                    final ParameterDescriptor desc = (ParameterDescriptor) geneDesc;
                    final String id = buildProcessIOIdentifiers(procDesc, desc, iOType);
                    map.put(id, desc.getMinimumOccurs() > 0);
                }
            }
        }
        return map;
    }

    /**
     * Confronts the process parameters {@code Map} to the list of requested identifiers for an type of IO
     * (Input,Output). If there is a missing mandatory parameter in the list of requested identifiers, an {@link CstlServiceException CstlServiceException}
     * will be throw. If an unknow parameter is requested, it also throw an {@link CstlServiceException CstlServiceException}
     *
     * @param descMap - {@code Map} contain all parameters with their mandatory attributes for INPUT or OUTPUT.
     * @param requestIdentifiers - {@code List} of requested identifiers in INPUT or OUTPUT.
     * @param iotype - {@link WPSIO.IOType type}.
     * @throws CstlServiceException for missing or unknow parmeter.
     */
    private static void checkIOIdentifiers(final Map<String, Boolean> descMap, final List<String> requestIdentifiers, final WPSIO.IOType iotype)
            throws CstlServiceException {

        final String type = iotype == WPSIO.IOType.INPUT ? "INPUT" : "OUTPUT";

        if (descMap.isEmpty() && !requestIdentifiers.isEmpty()) {
            throw new CstlServiceException("This process have no inputs.", INVALID_PARAMETER_VALUE, "input"); //process have no inputs
        } else {
            //check for Unknow parameter.
            for (final String identifier : requestIdentifiers) {
                if (!descMap.containsKey(identifier)) {
                    throw new CstlServiceException("Unknow " + type + " parameter : " + identifier + ".", INVALID_PARAMETER_VALUE, identifier);
                }
            }
            //check for missing parameters.
            if (descMap.containsValue(Boolean.TRUE)) {
                for (Map.Entry<String, Boolean> entry : descMap.entrySet()) {
                    if (entry.getValue() == Boolean.TRUE) {
                        if (!requestIdentifiers.contains(entry.getKey())) {
                            throw new CstlServiceException("Mandatory " + type + " parameter " + entry.getKey() + " is missing.", MISSING_PARAMETER_VALUE, entry.getKey());
                        }
                    }
                }
            }
        }
    }

    /**
     * Create the temporary directory used for storage.
     *
     * @return {@code true} if success, {@code false} if failed.
     */
    public static boolean createTempDirectory() {
        final String tmpDir = getTempDirectoryPath();
        boolean success = (new File(tmpDir)).mkdirs();
        if (success) {
            LOGGER.log(Level.INFO, "Temporary storage directory created at : " + tmpDir);
        } else {
            LOGGER.log(Level.WARNING, "Temporary storage directory can't be created at : " + tmpDir);
        }
        return success;
    }

    /**
     * Delete a file. If the file is a directory, the method will recursivly delete all files before.
     *
     * @param file
     * @return {@code true} if success, {@code false} if failed.
     */
    public static boolean deleteTempFileOrDirectory(final File file) {
        //directory case.
        if (file.isDirectory()) {
            String[] children = file.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteTempFileOrDirectory(new File(file, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        boolean success = file.delete();
        if (success) {
            LOGGER.log(Level.INFO, "Temporary file " + file.getAbsolutePath() + " deleted.");
        } else {
            LOGGER.log(Level.WARNING, "Temporary file " + file.getAbsolutePath() + " can't be deleted.");
        }
        return success;
    }

    /**
     * Store the given object into a temorary file specified by the given fileName into the temporary folder.
     * The object to store is marshalled by the {@link WPSMarshallerPool}. If the temporary file already exist
     * he will be overwrited.
     * 
     * @param obj object to marshalle and store to a temporary file.
     * @param fileName temporary file name.
     * @return 
     */
    public static boolean storeResponse(final Object obj, final String fileName) {
        ArgumentChecks.ensureNonNull("obj", obj);
        
        final MarshallerPool marshallerPool = WPSMarshallerPool.getInstance();
        boolean success = false;

        Marshaller marshaller = null;
        try {

            final File outputFile = new File(getTempDirectoryPath(), fileName);
            marshaller = marshallerPool.acquireMarshaller();
            marshaller.marshal(obj, outputFile);

            success = outputFile.exists();

        } catch (JAXBException ex) {
            LOGGER.log(Level.WARNING, "Error during unmarshalling", ex);
        } finally {
            marshallerPool.release(marshaller);
        }
        return success;
    }

    /**
     * Retrurn the absolute path to the temporary directory.
     *
     * @return absolut path String.
     */
    public static String getTempDirectoryPath() {
        return Util.getWebappDiretory().getAbsolutePath() + TEMP_FOLDER;
    }

    /**
     * Return the temporary folder URL. e.g : http://server:port/domain/tempDoc/
     * 
     * @param workerURL
     * @return temporary folder path URL.
     */
    public static String getTempDirectoryURL(final String workerURL) {
        String path = null;
        try {
            final URL url = new URL(workerURL);
            path = url.getProtocol() + "://" + url.getAuthority() + "/" + url.getPath().split("/")[1] + TEMP_FOLDER;
        } catch (MalformedURLException ex) {
            LOGGER.log(Level.WARNING, "Error during temporary folder URL.", ex);
        }
        return path;
    }

    /**
     * Clean temporary file used as process inputs.
     * 
     * @param files 
     */
    public static void cleanTempFiles(List<File> files) {
        if (files != null) {
            for (final File f : files) {
                f.delete();
            }
        }
    }
}
