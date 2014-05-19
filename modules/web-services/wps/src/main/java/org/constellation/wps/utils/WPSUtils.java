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
package org.constellation.wps.utils;

import java.io.File;
import java.math.BigInteger;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import static org.constellation.wps.ws.WPSConstant.*;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.feature.xml.jaxb.JAXBFeatureTypeWriter;

import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_PARAMETER_VALUE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.MISSING_PARAMETER_VALUE;
import org.geotoolkit.ows.xml.v110.CodeType;
import org.geotoolkit.ows.xml.v110.DomainMetadataType;
import org.geotoolkit.ows.xml.v110.LanguageStringType;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessFinder;
import org.apache.sis.util.ArgumentChecks;
import org.apache.sis.util.logging.Logging;
import org.geotoolkit.wps.io.WPSIO;
import org.geotoolkit.wps.xml.WPSMarshallerPool;
import org.geotoolkit.wps.xml.v100.*;
import org.apache.sis.xml.MarshallerPool;
import org.geotoolkit.xsd.xml.v2001.Schema;
import org.geotoolkit.xsd.xml.v2001.XSDMarshallerPool;
import org.opengis.feature.type.FeatureType;

import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.util.NoSuchIdentifierException;

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
     * @param identifier like "urn:ogc:cstl:wps:math:add"
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
     * Build layerName
     *
     * @param processDesc
     * @return authority.code
     */
    public static String buildLayerName(final ProcessDescriptor processDesc) {
        ArgumentChecks.ensureNonNull("processDesc", processDesc);
        return  processDesc.getIdentifier().getAuthority().getTitle().toString() + "." + processDesc.getIdentifier().getCode();
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
     * urn:ogc:cstl:wps:math:add:input:number1, urn:ogc:cstl:wps:math:add:ouput:result
     *
     * @param procDesc
     * @param input
     * @param ioType
     * @return processIdentifier:ioType:paramName
     */
    public static String buildProcessIOIdentifiers(final ProcessDescriptor procDesc, final GeneralParameterDescriptor input,
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
    public static boolean isSupportedProcess(final ProcessDescriptor descriptor) {
        
        //Inputs
        final GeneralParameterDescriptor inputDesc = descriptor.getInputDescriptor();
        if(!isSupportedParameter(inputDesc, WPSIO.IOType.INPUT)) {
            return false;
        }

        //Outputs
        GeneralParameterDescriptor outputDesc = descriptor.getOutputDescriptor();        
        if(!isSupportedParameter(outputDesc, WPSIO.IOType.OUTPUT)) {
            return false;
        }        
        return true;
    }
    
    /**
     * A function which test if the given parameter can be proceed by the WPS.
     * @param toTest The descriptor of the parameter to test.
     * @param type The parameter type (input or output).
     * @return true if the WPS can work with this parameter, false otherwise.
     */
    public static boolean isSupportedParameter(GeneralParameterDescriptor toTest, WPSIO.IOType type) {
        boolean isClean = false;
        if (toTest instanceof ParameterDescriptorGroup) {
            final List<GeneralParameterDescriptor> descs = ((ParameterDescriptorGroup) toTest).descriptors();
            if (descs.isEmpty()) {
                isClean = true;
            } else {
                for (GeneralParameterDescriptor desc : descs) {
                    isClean = isSupportedParameter(desc, type);
                    if (!isClean) {
                        break;
                    }
                }
            }
        } else if (toTest instanceof ParameterDescriptor) {
            final ParameterDescriptor param = (ParameterDescriptor) toTest;
            final Class paramClass = param.getValueClass();

            isClean = (type.equals(WPSIO.IOType.INPUT))
                    ? WPSIO.isSupportedInputClass(paramClass)
                    : WPSIO.isSupportedOutputClass(paramClass);
        }
        return isClean;
    }

    /**
     * Return the SupportedComplexDataInputType for the given class.
     *
     * @param attributeClass The java class to get complex type from.
     * @param ioType The type of parameter to describe (input or output).
     * @param type The complex type (complex, reference, etc.).
     * @return SupportedComplexDataInputType 
     */
    public static SupportedComplexDataInputType describeComplex(final Class attributeClass, final WPSIO.IOType ioType, final WPSIO.FormChoice type) {
        return describeComplex(attributeClass, ioType, type, null);
    }

    /**
     * Return the SupportedComplexDataInputType for the given class.
     *
     * @param attributeClass The java class to get complex type from.
     * @param ioType The type of parameter to describe (input or output).
     * @param type The complex type (complex, reference, etc.).
     * @param userData A map containing user's options for type support.
     * @return SupportedComplexDataInputType
     */
    public static SupportedComplexDataInputType describeComplex(final Class attributeClass, final WPSIO.IOType ioType, final WPSIO.FormChoice type, final Map<String, Object> userData) {

        final SupportedComplexDataInputType complex = new SupportedComplexDataInputType();
        final ComplexDataCombinationsType complexCombs = new ComplexDataCombinationsType();
        final ComplexDataCombinationType complexComb = new ComplexDataCombinationType();
        List<WPSIO.FormatSupport> infos = null;
        String schema = null;

        if (userData != null) {
            try {
                infos = (List<WPSIO.FormatSupport>) userData.get(WPSIO.SUPPORTED_FORMATS_KEY);
                schema = (String) userData.get(WPSIO.SCHEMA_KEY);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "A parameter type definition can't be read.", e);
            }
        }

        if (infos == null) {
            infos = WPSIO.getFormats(attributeClass, ioType);
        }

        if (infos != null) {
            for (WPSIO.FormatSupport inputClass : infos) {

                final ComplexDataDescriptionType complexDesc = new ComplexDataDescriptionType();
                complexDesc.setEncoding(inputClass.getEncoding()); //Encoding
                complexDesc.setMimeType(inputClass.getMimeType()); //Mime
                complexDesc.setSchema(schema != null ? schema : inputClass.getSchema()); //URL to xsd schema

                if (inputClass.isDefaultFormat()) {
                    complexComb.setFormat(complexDesc);
                }
                complexCombs.getFormat().add(complexDesc);
            }
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
     * Check if all requested inputs/outputs are present in the process descriptor. Check also if all mandatory
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
        final Map<String, Boolean> inputDescMap = descriptorsAsMap(inputDescriptorGroup, processDesc, WPSIO.IOType.INPUT);
        checkIOIdentifiers(inputDescMap, inputIdentifiers, WPSIO.IOType.INPUT);

        //check outputs
        final List<String> outputIdentifiers = extractRequestOutputIdentifiers(request);
        final ParameterDescriptorGroup outputDescriptorGroup = processDesc.getOutputDescriptor();
        final Map<String, Boolean> outputDescMap = descriptorsAsMap(outputDescriptorGroup, processDesc, WPSIO.IOType.OUTPUT);
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
    private static Map<String, Boolean> descriptorsAsMap(final ParameterDescriptorGroup descGroup, final ProcessDescriptor procDesc,
            final WPSIO.IOType iOType) {

        final Map<String, Boolean> map = new HashMap<String, Boolean>();
        if (descGroup != null && descGroup.descriptors() != null) {
            final List<GeneralParameterDescriptor> descriptors = descGroup.descriptors();

            for (final GeneralParameterDescriptor geneDesc : descriptors) {
                final String id = buildProcessIOIdentifiers(procDesc, geneDesc, iOType);
                final boolean required;
                if (geneDesc instanceof ParameterDescriptor && ((ParameterDescriptor)geneDesc).getDefaultValue() != null) {
                    required = false;
                } else {
                    required = geneDesc.getMinimumOccurs() > 0;
                }
                map.put(id, required);
            }
        }
        return map;
    }

    /**
     * Confronts the process parameters {@code Map} to the list of requested identifiers for an type of IO
     * (Input,Output). If there is a missing mandatory parameter in the list of requested identifiers, an {@link CstlServiceException CstlServiceException}
     * will be throw. If an unknown parameter is requested, it also throw an {@link CstlServiceException CstlServiceException}
     *
     * @param descMap - {@code Map} contain all parameters with their mandatory attributes for INPUT or OUTPUT.
     * @param requestIdentifiers - {@code List} of requested identifiers in INPUT or OUTPUT.
     * @param iotype - {@link WPSIO.IOType type}.
     * @throws CstlServiceException for missing or unknown parameter.
     */
    private static void checkIOIdentifiers(final Map<String, Boolean> descMap, final List<String> requestIdentifiers, final WPSIO.IOType iotype)
            throws CstlServiceException {

        final String type = iotype == WPSIO.IOType.INPUT ? "INPUT" : "OUTPUT";

        if (descMap.isEmpty() && !requestIdentifiers.isEmpty()) {
            throw new CstlServiceException("This process have no inputs.", INVALID_PARAMETER_VALUE, "input"); //process have no input
        } else {
            //check for Unknown parameter.
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
     * Store the given object into a temorary file specified by the given fileName into the temporary folder. The object
     * to store is marshalled by the {@link WPSMarshallerPool}. If the temporary file already exist he will be
     * overwrited.
     *
     * @param obj object to marshalle and store to a temporary file.
     * @param fileName temporary file name.
     * @return
     */
    public static boolean storeResponse(final Object obj, final String folderPath, final String fileName) {
        ArgumentChecks.ensureNonNull("obj", obj);

        final MarshallerPool marshallerPool = WPSMarshallerPool.getInstance();
        boolean success = false;

        try {

            final File outputFile = new File(folderPath, fileName);
            final Marshaller marshaller = marshallerPool.acquireMarshaller();
            marshaller.marshal(obj, outputFile);
            marshallerPool.recycle(marshaller);
            success = outputFile.exists();

        } catch (JAXBException ex) {
            LOGGER.log(Level.WARNING, "Error during unmarshalling", ex);
        }
        return success;
    }

    /**
     * Return tuple toString mime/encoding/schema. "[mimeType, encoding, schema]".
     * @param requestedOuptut DocumentOutputDefinitionType
     * @return tuple string.
     */
    public static String outputDefinitionToString(final DocumentOutputDefinitionType requestedOuptut) {
        final StringBuilder builder = new StringBuilder();
        final String begin = "[";
        final String end = "]";
        final String separator = ", ";

        builder.append(begin);
        
        builder.append("mimeType=");
        builder.append(requestedOuptut.getMimeType());
        builder.append(separator);
        
        builder.append("encoding=");
        builder.append(requestedOuptut.getEncoding());
        builder.append(separator);
        
        builder.append("schema=");
        builder.append(requestedOuptut.getSchema());
       
        builder.append(end);
        return builder.toString();
    }
    
    /**
     * A function to retrieve a Feature schema, and store it into the given file
     * as an xsd.
     *
     * @param source The feature to get schema from.
     * @param destination The file where we want to save our feature schema.
     * @throws JAXBException If we can't parse / write the schema properly.
     */
    public static void storeFeatureSchema(FeatureType source, File destination) throws JAXBException {

        JAXBFeatureTypeWriter writer = new JAXBFeatureTypeWriter();
        Schema s = writer.getSchemaFromFeatureType(source);
        MarshallerPool pool = XSDMarshallerPool.getInstance();
        Marshaller marsh = pool.acquireMarshaller();

        marsh.marshal(s, destination);
    }


    /**
     * @return the current time in an XMLGregorianCalendar.
     */
    public static XMLGregorianCalendar getCurrentXMLGregorianCalendar(){
        XMLGregorianCalendar xcal = null;
        try {
            final GregorianCalendar c = new GregorianCalendar();
            c.setTime(new Date());
            xcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        } catch (DatatypeConfigurationException ex) {
            LOGGER.log(Level.INFO, "Can't create the creation time of the status.");
        }
        return xcal;
    }
}