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
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessFinder;
import org.opengis.util.NoSuchIdentifierException;

import org.geotoolkit.ows.xml.v110.CodeType;
import org.geotoolkit.ows.xml.v110.LanguageStringType;
import org.geotoolkit.wps.xml.v100.ProcessBriefType;
import org.geotoolkit.util.ArgumentChecks;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import static org.constellation.api.QueryConstants.*;
import static org.constellation.wps.ws.WPSConstant.*;
import org.constellation.wps.ws.WPSIO;
import org.constellation.wps.ws.WPSWorker;
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
import org.geotoolkit.wps.xml.v100.*;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.FactoryException;

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
     * @param identifier like "urn:geomatys:wps:math:add"
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
        brief.setTitle(new LanguageStringType(processDesc.getIdentifier().getAuthority().getTitle().toString() + " : " + processDesc.getIdentifier().getCode()));
        brief.setAbstract(new LanguageStringType(processDesc.getProcedureDescription().toString()));
        brief.setProcessVersion(null);
        brief.setWSDL(null);

        return brief;
    }

    /**
     * Build OGC URN unique identifier for a process from his process
     * descriptor.
     *
     * @param processDesc
     * @return
     */
    public static String buildProcessIdentifier(final ProcessDescriptor processDesc) {
        ArgumentChecks.ensureNonNull("processDesc", processDesc);
        return PROCESS_PREFIX + processDesc.getIdentifier().getAuthority().getTitle().toString() + ":" + processDesc.getIdentifier().getCode();
    }

    /**
     * Extract the factory name from a process identifier. e.g :
     * urn:geomatys:wps:math:add return math.
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
        throw new CstlServiceException("Invalid process identifier. Must be an URN code like " + PROCESS_PREFIX + ":factory:process",
                INVALID_PARAMETER_VALUE, IDENTIFER_PARAMETER.toLowerCase());
    }

    /**
     * Extract the process name from a process identifier. 
     * e.g : urn:geomatys:wps:math:add return add.
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
     * Extract the factory name and the process name from a process identifier.
     * e.g : urn:geomatys:wps:math:add return math:add.
     *
     * @param identifier
     * @return factoryName:processName.
     */
    private static String extractFactoryAndProcessNameFromIdentifier(final String identifier) throws CstlServiceException {
        if (identifier.contains(PROCESS_PREFIX)) {
            return identifier.replace(PROCESS_PREFIX, "");
        }
        throw new CstlServiceException("Invalid process identifier. Must be an URN code like " + PROCESS_PREFIX + ":factory:process",
                INVALID_PARAMETER_VALUE, IDENTIFER_PARAMETER.toLowerCase());
    }

    /**
     * Generate process INPUT/OUPTUT identifiers based on process identifier.
     * e.g : urn:geomatys:wps:math:add:input:number1,
     * urn:geomatys:wps:math:add:ouput:result
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
                return buildProcessIdentifier(procDesc) + ":ouptut:" + input.getName().getCode();
            }
        }
        return null;
    }

    /**
     * Return the given String with the first letter to upper case.
     * 
     * @param value
     * @return String
     */
    public static String capitalizeFirstLetter(final String value) {
        if (value != null && !value.isEmpty()) {
          
            final StringBuilder result = new StringBuilder(value);
            result.replace(0, 1, result.substring(0, 1).toUpperCase());
            return result.toString();
        }
        return value;
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
     * Convert a string to a binding class. If the binding class isn't a
     * primitive like Integer, Double, .. we search into the converter list if
     * found a match.
     *
     * @param data string to convert
     * @param binding wanted class
     * @return converted object
     * @throws CstlServiceException if there is no match found
     */
    public static <T> Object convertFromString(final String data, final Class binding) throws CstlServiceException {

        Object convertedData = null; //resulting Object
        try {

            ObjectConverter<String, T> converter = null;//converter
            try {
                //try to convert into a primitive type
                converter = ConverterRegistry.system().converter(String.class, binding);
            } catch (NonconvertibleObjectException ex) {
                //try to convert with some specified converter
                converter = WPSIO.getConverter(binding, WPSIO.IOType.INPUT, WPSIO.DataType.LITERAL, null);

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
        parameters.put("data", inputObject);
        parameters.put("mime", mime);
        parameters.put("schema", schema);
        parameters.put("encoding", encoding);

        final ObjectConverter converter = WPSIO.getConverter(expectedClass, WPSIO.IOType.INPUT, WPSIO.DataType.COMPLEX, mime);

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
        parameters.put("href", href);
        parameters.put("mime", mime);
        parameters.put("schema", schema);
        parameters.put("method", method);
        parameters.put("encoding", encoding);

        final ObjectConverter converter = WPSIO.getConverter(expectedClass, WPSIO.IOType.INPUT, WPSIO.DataType.REFERENCE, mime);

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
     * Fix a FeatureType in spread the geometry CRS from a feature to the
     * geometry descriptor CRS
     *
     * @param featureIN feature with geometry used to fix the geometry
     * descriptor
     * @param type the featureType to fix
     * @throws CstlServiceException
     */
    public static void fixFeatureType(final Feature featureIN, DefaultFeatureType type) throws CstlServiceException {

        CoordinateReferenceSystem extractCRS = null;

        final FeatureTypeBuilder ftb = new FeatureTypeBuilder();
        ftb.copy(type);

        //Fetch each geometry, get his CRS and 
        for (Property property : featureIN.getProperties()) {
            if (property.getDescriptor() instanceof GeometryDescriptor) {
                try {
                    final String propertyName = property.getName().getLocalPart();
                    final Geometry propertyGeom = (Geometry) property.getValue();
                    extractCRS = JTS.findCoordinateReferenceSystem(propertyGeom);

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
        ComplexDataDescriptionType complexDesc = null;

        final List<WPSIO.DataInfo> infos = WPSIO.IOCLASSMAP.get(new WPSIO.KeyTuple(attributeClass, ioType, WPSIO.DataType.COMPLEX));

        for (WPSIO.DataInfo inputClass : infos) {

            complexDesc = new ComplexDataDescriptionType();
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
        complex.setMaximumMegabytes(BigInteger.valueOf(MAX_MB_INPUT_COMPLEX));
        return complex;
    }
}
