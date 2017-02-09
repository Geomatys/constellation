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

package org.constellation.sos.io.generic;

import org.apache.sis.storage.DataStoreException;
import org.constellation.generic.GenericReader;
import org.constellation.generic.Values;
import org.constellation.generic.database.Automatic;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.sos.factory.OMFactory;
import org.constellation.ws.MimeType;
import org.geotoolkit.gml.xml.AbstractGeometry;
import org.geotoolkit.gml.xml.Envelope;
import org.geotoolkit.gml.xml.FeatureProperty;
import org.geotoolkit.gml.xml.GMLXmlFactory;
import org.geotoolkit.gml.xml.LineString;
import org.geotoolkit.gml.xml.Point;
import org.geotoolkit.gml.xml.v311.UnitOfMeasureEntry;
import org.constellation.sos.io.ObservationReader;
import org.geotoolkit.observation.xml.OMXmlFactory;
import org.geotoolkit.observation.xml.v100.MeasureType;
import org.geotoolkit.sos.xml.ObservationOffering;
import org.geotoolkit.sos.xml.ResponseModeType;
import org.geotoolkit.sos.xml.SOSXmlFactory;
import org.geotoolkit.swe.xml.AbstractDataComponent;
import org.geotoolkit.swe.xml.AbstractDataRecord;
import org.geotoolkit.swe.xml.AnyScalar;
import org.geotoolkit.swe.xml.Phenomenon;
import org.geotoolkit.swe.xml.PhenomenonProperty;
import org.geotoolkit.swe.xml.TextBlock;
import org.geotoolkit.swe.xml.UomProperty;
import org.geotoolkit.swe.xml.v101.CompositePhenomenonType;
import org.geotoolkit.swe.xml.v101.PhenomenonPropertyType;
import org.geotoolkit.swe.xml.v101.PhenomenonType;
import org.opengis.geometry.DirectPosition;
import org.opengis.observation.Measure;
import org.opengis.observation.Observation;
import org.opengis.observation.sampling.SamplingFeature;
import org.opengis.temporal.Period;
import org.opengis.temporal.TemporalGeometricPrimitive;
import org.opengis.temporal.TemporalPrimitive;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static org.constellation.sos.ws.SOSConstants.MEASUREMENT_QNAME;
import static org.constellation.sos.ws.SOSConstants.OBSERVATION_MODEL;
import static org.constellation.sos.ws.SOSConstants.OBSERVATION_QNAME;
import static org.constellation.sos.ws.SOSConstants.SENSORML_100_FORMAT_V200;
import static org.constellation.sos.ws.SOSConstants.SENSORML_101_FORMAT_V200;
import static org.geotoolkit.sos.xml.SOSXmlFactory.buildDirectPosition;
import static org.geotoolkit.sos.xml.SOSXmlFactory.buildFeatureProperty;
import static org.geotoolkit.sos.xml.SOSXmlFactory.buildLineString;
import static org.geotoolkit.sos.xml.SOSXmlFactory.buildOffering;
import static org.geotoolkit.sos.xml.SOSXmlFactory.buildPoint;
import static org.geotoolkit.sos.xml.SOSXmlFactory.buildSamplingCurve;
import static org.geotoolkit.sos.xml.SOSXmlFactory.buildSamplingPoint;


/**
 *
 * @author Guilhem Legal
 */
public class DefaultGenericObservationReader extends GenericReader implements ObservationReader {

    /**
     * The base for observation id.
     */
    protected final String observationIdBase;
    
    protected final String observationIdTemplateBase;
    
    protected final String phenomenonIdBase;
    
    protected final String sensorIdBase;

    public DefaultGenericObservationReader(Automatic configuration, Map<String, Object> properties) throws DataStoreException, MetadataIoException {
        super(configuration);
        this.observationIdBase = (String) properties.get(OMFactory.OBSERVATION_ID_BASE);
        this.phenomenonIdBase  = (String) properties.get(OMFactory.PHENOMENON_ID_BASE);
        this.sensorIdBase      = (String) properties.get(OMFactory.SENSOR_ID_BASE);
        this.observationIdTemplateBase = (String) properties.get(OMFactory.OBSERVATION_TEMPLATE_ID_BASE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getOfferingNames(final String version) throws DataStoreException {
        try {
            if (version.equals("1.0.0")) {
                final Values values = loadData("var01");
                return values.getVariables("var01");
                
            // for 2.0 we adapt the offering with one by procedure   
            } else if (version.equals("2.0.0")) {
                final Values values = loadData("var02");
                final List<String> result = new ArrayList<>();
                for (String procedure : values.getVariables("var02")) {
                    if (procedure.startsWith(sensorIdBase)) {
                        procedure = procedure.replace(sensorIdBase, "");
                    }
                    result.add("offering-" + procedure);
                }
                return result;
            } else {
                throw new IllegalArgumentException("unexpected SOS version:" + version);
            }
        } catch (MetadataIoException ex) {
            throw new DataStoreException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getProcedureNames() throws DataStoreException {
        try {
            final Values values = loadData(Arrays.asList("var02"));
            return values.getVariables("var02");
        } catch (MetadataIoException ex) {
            throw new DataStoreException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getPhenomenonNames() throws DataStoreException {
        try {
            final Values values = loadData(Arrays.asList("var03", "var83"));
            final List<String> results = values.getVariables("var03");
            results.addAll(values.getVariables("var83"));
            return results;
        } catch (MetadataIoException ex) {
            throw new DataStoreException(ex);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existPhenomenon(String phenomenonName) throws DataStoreException {
        return getPhenomenonNames().contains(phenomenonName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getFeatureOfInterestNames() throws DataStoreException {
        try {
            final Values values = loadData(Arrays.asList("var04", "var67"));
            final List<String> result = values.getVariables("var04");
            final List<String> curves = values.getVariables("var67");
            if (!curves.isEmpty()) {
                result.addAll(curves);
            }
            return result;
        } catch (MetadataIoException ex) {
            throw new DataStoreException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNewObservationId() throws DataStoreException {
        try {
            Values values = loadData(Arrays.asList("var05"));
            int id = Integer.parseInt(values.getVariable("var05"));

            values = loadData(Arrays.asList("var44"), observationIdBase + id);
            String continues;
            do {
                id++;
                continues = values.getVariable("var44");

            } while (continues != null);
            return observationIdBase + id;
        } catch (MetadataIoException ex) {
            throw new DataStoreException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getEventTime() throws DataStoreException {
         try {
            final Values values = loadData(Arrays.asList("var06"));
            return Arrays.asList(values.getVariable("var06"));
         } catch (MetadataIoException ex) {
            throw new DataStoreException(ex);
         }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ObservationOffering> getObservationOfferings(final List<String> offeringNames, final String version) throws DataStoreException {
        final List<ObservationOffering> offerings = new ArrayList<>();
        for (String offeringName : offeringNames) {
            offerings.add(getObservationOffering(offeringName, version));
        }
        return offerings;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ObservationOffering getObservationOffering(final String offeringName, final String version) throws DataStoreException {
        try {
            final Values values = loadData(Arrays.asList("var07", "var08", "var09", "var10", "var11", "var12", "var18", "var46"), offeringName);

            final boolean exist = values.getVariable("var46") != null;
            if (!exist) {
                return null;
            }
            
            final List<String> srsName = values.getVariables("var07");

            final String gmlVersion;
            if (version.equals("2.0.0")) {
                gmlVersion = "3.2.1";
            } else {
                gmlVersion = "3.1.1";
            }
            // event time
            Period time;
            String offeringBegin = values.getVariable("var08");
            if (offeringBegin != null) {
                offeringBegin    = offeringBegin.replace(' ', 'T');
            }
            String offeringEnd   = values.getVariable("var09");
            if (offeringEnd != null) {
                offeringEnd          = offeringEnd.replace(' ', 'T');
            }
            time  = GMLXmlFactory.createTimePeriod(gmlVersion, null, offeringBegin, offeringEnd);
            
            // procedure
            final List<String> procedures;
            if (version.equals("2.0.0")) {
                procedures = Arrays.asList(sensorIdBase + offeringName.substring(9));
            } else {
                procedures = values.getVariables("var10");
            }
            
            // phenomenon
            final List<String> observedPropertiesv200         = new ArrayList<>();
            final List<PhenomenonProperty> observedProperties = new ArrayList<>();
            for (String phenomenonId : values.getVariables("var12")) {
                if (phenomenonId!= null && !phenomenonId.isEmpty()) {
                    Values compositeValues = loadData(Arrays.asList("var17"), phenomenonId);
                    final List<PhenomenonType> components = new ArrayList<>();
                    for (String componentID : compositeValues.getVariables("var17")) {
                        components.add(getPhenomenon(componentID));
                    }
                    compositeValues = loadData(Arrays.asList("var15", "var16"), phenomenonId);
                    final CompositePhenomenonType phenomenon = new CompositePhenomenonType(phenomenonId,
                                                                                       compositeValues.getVariable("var15"),
                                                                                       compositeValues.getVariable("var16"),
                                                                                       null,
                                                                                       components);
                    observedProperties.add(new PhenomenonPropertyType(phenomenon));
                    observedPropertiesv200.add(phenomenonId);
                }
            }
            for (String phenomenonId : values.getVariables("var11")) {
                if (phenomenonId != null && !phenomenonId.isEmpty()) {
                    final PhenomenonType phenomenon = getPhenomenon(phenomenonId);
                    observedProperties.add(new PhenomenonPropertyType(phenomenon));
                    observedPropertiesv200.add(phenomenonId);
                }
            }

            // feature of interest
            final List<String> foisV200    = new ArrayList<>();
            for (String foiID : values.getVariables("var18")) {
                foisV200.add(foiID);
            }

            //static part
            final List<String> responseFormat         = Arrays.asList(MimeType.APPLICATION_XML);
            final List<QName> resultModel             = Arrays.asList(OBSERVATION_QNAME, MEASUREMENT_QNAME);
            final List<String> resultModelV200        = Arrays.asList(OBSERVATION_MODEL);
            final List<ResponseModeType> responseMode = Arrays.asList(ResponseModeType.INLINE, ResponseModeType.RESULT_TEMPLATE);
            final List<String> procedureDescription   = Arrays.asList(SENSORML_100_FORMAT_V200, SENSORML_101_FORMAT_V200);
            return buildOffering(version, 
                                 offeringName, 
                                 offeringName, 
                                 null, 
                                 srsName, 
                                 time, 
                                 procedures, 
                                 observedProperties, 
                                 observedPropertiesv200, 
                                 foisV200, 
                                 responseFormat, 
                                 resultModel, 
                                 resultModelV200, 
                                 responseMode,
                                 procedureDescription);
                    
            
        } catch (MetadataIoException ex) {
            throw new DataStoreException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ObservationOffering> getObservationOfferings(final String version) throws DataStoreException {
        final List<ObservationOffering> offerings = new ArrayList<>();
        final List<String> offeringNames = getOfferingNames(version);
        for (String offeringName : offeringNames) {
            offerings.add(getObservationOffering(offeringName, version));
        }
        return offerings;
    }

    private PhenomenonType getPhenomenon(String phenomenonName) throws DataStoreException {
        // we remove the phenomenon id base
        if (phenomenonName.contains(phenomenonIdBase)) {
            phenomenonName = phenomenonName.replace(phenomenonIdBase, "");
        }
        try {
            final Values values = loadData(Arrays.asList("var13", "var14", "var47"), phenomenonName);
            final boolean exist = values.getVariable("var47") != null;
            if (!exist) {
                return getCompositePhenomenon(phenomenonName);
            }
            return new PhenomenonType(phenomenonName, values.getVariable("var13"), values.getVariable("var14"));
        } catch (MetadataIoException ex) {
            throw new DataStoreException(ex);
        }
    }
    
    private PhenomenonType getCompositePhenomenon(String phenomenonName) throws DataStoreException {
        // we remove the phenomenon id base
        if (phenomenonName.contains(phenomenonIdBase)) {
            phenomenonName = phenomenonName.replace(phenomenonIdBase, "");
        }
        try {
            Values compositeValues = loadData("var68", phenomenonName);
            final boolean exist = compositeValues.getVariable("var68") != null;
            if (!exist) {
                return null;
            }
            compositeValues = loadData(Arrays.asList("var17"), phenomenonName);
            final List<PhenomenonType> components = new ArrayList<>();
            for (String componentID : compositeValues.getVariables("var17")) {
                components.add(getPhenomenon(componentID));
            }
            compositeValues = loadData(Arrays.asList("var15", "var16"), phenomenonName);
            final CompositePhenomenonType phenomenon = new CompositePhenomenonType(phenomenonName,
                                                                               compositeValues.getVariable("var15"),
                                                                               compositeValues.getVariable("var16"),
                                                                               null,
                                                                               components);
            return phenomenon;
        } catch (MetadataIoException ex) {
            throw new DataStoreException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SamplingFeature getFeatureOfInterest(final String samplingFeatureName, final String version) throws DataStoreException {
        try {
            final Values values = loadData(Arrays.asList("var19", "var20", "var21", "var22", "var23", "var24", "var48"), samplingFeatureName);

            final boolean exist = values.getVariable("var48") != null;
            if (!exist) {
                return getFeatureOfInterestCurve(samplingFeatureName, version);
            }

            final String name            = values.getVariable("var19");
            final String description     = values.getVariable("var20");
            final String sampledFeature  = values.getVariable("var21");

            final String pointID         = values.getVariable("var22");
            final String srsName         = values.getVariable("var23");

            final String dimension       = values.getVariable("var24");
            int srsDimension       = 0;
            try {
                srsDimension       = Integer.parseInt(dimension);
            } catch (NumberFormatException ex) {
                LOGGER.log(Level.SEVERE, "unable to parse the srs dimension: {0}", dimension);
            }
            final List<Double> coordinates = getCoordinates(samplingFeatureName);
            final DirectPosition pos = buildDirectPosition(version, srsName, srsDimension, coordinates);
            final Point location     = buildPoint(version, pointID, pos);

            final FeatureProperty sampleFeatureProperty;
            if (sampledFeature != null) {
                sampleFeatureProperty = buildFeatureProperty(version, sampledFeature);
            } else {
                sampleFeatureProperty = null;
            }
            return buildSamplingPoint(version, samplingFeatureName, name, description, sampleFeatureProperty, location);
        } catch (MetadataIoException ex) {
            throw new DataStoreException(ex);
        }
    }
    
    public SamplingFeature getFeatureOfInterestCurve(final String samplingFeatureId, final String version) throws DataStoreException {
        try {
            final Values values = loadData(Arrays.asList("var51", "var52", "var53", "var54", "var55", "var56", "var56", "var57",
                                                         "var58", "var59", "var60", "var61", "var62", "var63", "var82"), samplingFeatureId);

            final boolean exist = values.getVariable("var51") != null;
            if (!exist) {
                return null;
            }

            final String name            = values.getVariable("var53");
            final String description     = values.getVariable("var52");
            final String sampledFeature  = values.getVariable("var59");
            
            final String boundSrsName    = values.getVariable("var54");
            final String envID           = values.getVariable("var82");
            final Double lcx             = (Double) values.getTypedVariable("var55");
            final Double lcy             = (Double) values.getTypedVariable("var56");
            final Double ucx             = (Double) values.getTypedVariable("var57");
            final Double ucy             = (Double) values.getTypedVariable("var58");
            final Envelope env           = SOSXmlFactory.buildEnvelope(version, envID, lcx, lcy, ucx, ucy, boundSrsName);

            final String lengthUom       = values.getVariable("var60");
            final Double lengthValue     = (Double) values.getTypedVariable("var61");
            
            final String shapeID         = values.getVariable("var62");
            final String shapeSrsName    = values.getVariable("var63");

            final LineString location    = buildShape(version, shapeID);

            final FeatureProperty sampleFeatureProperty;
            if (sampledFeature != null) {
                sampleFeatureProperty = buildFeatureProperty(version, sampledFeature);
            } else {
                sampleFeatureProperty = null;
            }
            return buildSamplingCurve(version, samplingFeatureId, name, description, sampleFeatureProperty, location, lengthValue, lengthUom, env);
        } catch (MetadataIoException ex) {
            throw new DataStoreException(ex);
        }
    }
    
    private LineString buildShape(final String version, final String shapeID) throws MetadataIoException {
        final Values values = loadData(Arrays.asList("var64", "var65", "var66"), shapeID);
        final List<Object> xValues     = values.getTypedVariables("var64");
        final List<Object> yValues     = values.getTypedVariables("var65");
        final List<Object> zValues     = values.getTypedVariables("var66");
        final List<DirectPosition> pos = new ArrayList<>();
        for (int i = 0; i < xValues.size(); i++) {
            final List<Double> coord = new ArrayList<>();
            final Double x = (Double) xValues.get(i);
            final Double y = (Double) yValues.get(i);
            coord.add(x);
            coord.add(y);
            if (zValues.size() < i) {
                final Double z = (Double) zValues.get(i);
                coord.add(z);
            }
            pos.add(buildDirectPosition(version, null, null, coord));
        }
        return buildLineString(version, null, null, pos);
        
    }

    private List<Double> getCoordinates(String samplingFeatureId) throws DataStoreException {
        try {
            final Values values = loadData(Arrays.asList("var25", "var45"), samplingFeatureId);
            final List<Double> result = new ArrayList<>();
            String coordinate = values.getVariable("var25");
            if (coordinate != null) {
                try {
                    result.add(Double.parseDouble(coordinate));
                } catch (NumberFormatException ex) {
                    throw new DataStoreException(ex);
                }
            }
            coordinate = values.getVariable("var45");
            if (coordinate != null) {
                try {
                    result.add(Double.parseDouble(coordinate));
                } catch (NumberFormatException ex) {
                    throw new DataStoreException(ex);
                }
            }
            return result;
        } catch (MetadataIoException ex) {
            throw new DataStoreException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observation getObservation(final String identifier, final QName resultModel, final ResponseModeType mode, final String version) throws DataStoreException {
        try {
            final List<String> variables;
            if (resultModel.equals(OBSERVATION_QNAME))  {
                variables = Arrays.asList("var26", "var50", "var27", "var49", "var28", "var29", "var30", "var31");
            } else if (resultModel.equals(MEASUREMENT_QNAME))  {
                variables = Arrays.asList("var69", "var70", "var71", "var72", "var73", "var74", "var75", "var76");
            } else {
                throw new IllegalArgumentException("unexpected resultModel:" + resultModel);
            }
            final Values values = loadData(variables, identifier);
            final String foiPoint = values.getVariable(variables.get(0));
            final String foiCurve = values.getVariable(variables.get(1));
            final SamplingFeature featureOfInterest;
            if (foiPoint != null) {
                featureOfInterest = getFeatureOfInterest(foiPoint, version);
            } else if (foiCurve != null){
                featureOfInterest = getFeatureOfInterestCurve(foiCurve, version);
            } else {
               featureOfInterest = null;
               LOGGER.log(Level.INFO, "no featureOfInterest for result:{0}", identifier);
            }
            String begin = values.getVariable(variables.get(5));
            if (begin != null) {
                begin = begin.replace(' ', 'T');
            }
            String end   = values.getVariable(variables.get(6));
            if (end != null) {
                end = end.replace(' ', 'T');
            }
            final Period samplingTime = SOSXmlFactory.buildTimePeriod(version, null, begin, end);
            final String proc             = values.getVariable(variables.get(4));
            final String phenomenon       = values.getVariable(variables.get(2));
            final String phenomenonComp   = values.getVariable(variables.get(3));
            final String resultID         = values.getVariable(variables.get(7));
            final String obsID;
            if (identifier.startsWith(observationIdBase)) {
                obsID = "obs-" + identifier.substring(observationIdBase.length());
            } else if (identifier.startsWith(observationIdTemplateBase)) {
                obsID = "obs-" + identifier.substring(observationIdTemplateBase.length());
            } else {
                obsID = "obs-?";
            }
            
            final Phenomenon observedProperty;
            if (phenomenon != null) {
                observedProperty = getPhenomenon(phenomenon);
            } else if (phenomenonComp != null) {
                observedProperty = getCompositePhenomenon(phenomenonComp);
            } else {
                observedProperty = null;
                LOGGER.log(Level.INFO, "no phenomenon for result:{0}", identifier);
            }
            final FeatureProperty foi = SOSXmlFactory.buildFeatureProperty(version, featureOfInterest);
            final Object result = getResult(resultID, resultModel, version);
            if (version.equals("1.0.0")) {
                if (resultModel.equals(OBSERVATION_QNAME)) {
                    
                    return OMXmlFactory.buildObservation(version,
                                                         obsID,
                                                         identifier,
                                                         null,
                                                         foi,
                                                         observedProperty,
                                                         proc,
                                                         result,
                                                         samplingTime);
                } else if (resultModel.equals(MEASUREMENT_QNAME)) {
                    return OMXmlFactory.buildMeasurement(version,
                                                         obsID,
                                                         identifier,
                                                         null,
                                                         foi,
                                                         observedProperty,
                                                         proc,
                                                         (Measure)result,
                                                         (org.geotoolkit.gml.xml.v311.TimePeriodType)samplingTime);
                } else {
                    throw new IllegalArgumentException("unexpected resultModel:" + resultModel);
                }
            } else if (version.equals("2.0.0")) {
                return OMXmlFactory.buildObservation(version,
                                                     obsID,
                                                     identifier,
                                                     null,
                                                     foi,
                                                     observedProperty,
                                                     proc,
                                                     result,
                                                     samplingTime);
            } else {
                throw new IllegalArgumentException("Unexpected version:" + version);
            }
        } catch (MetadataIoException ex) {
            throw new DataStoreException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getResult(final String identifier, final QName resultModel, final String version) throws DataStoreException {
        try {
            if (resultModel.equals(OBSERVATION_QNAME)) {
                final Values values = loadData(Arrays.asList("var32", "var33", "var34", "var35", "var36", "var37", "var38", "var39",
                        "var40", "var41", "var42", "var43"), identifier);
                final int count = Integer.parseInt(values.getVariable("var32"));

                // encoding
                final String encodingID       = values.getVariable("var34");
                final String tokenSeparator   = values.getVariable("var35");
                final String decimalSeparator = values.getVariable("var36");
                final String blockSeparator   = values.getVariable("var37");
                final TextBlock encoding      = SOSXmlFactory.buildTextBlock(version, encodingID, tokenSeparator, blockSeparator, decimalSeparator);

                //data block description
                final String blockId          = values.getVariable("var38");
                final String dataRecordId     = values.getVariable("var39");
                final List<AnyScalar> fields  = new ArrayList<>();
                final List<String> fieldNames = values.getVariables("var40");
                final List<String> fieldDef   = values.getVariables("var41");
                final List<String> type       = values.getVariables("var42");
                final List<String> uomCodes   = values.getVariables("var43", true);
                for(int i = 0; i < fieldNames.size(); i++) {
                    AbstractDataComponent component = null;
                    final String typeName   = type.get(i);
                    final String fieldName  = fieldNames.get(i);
                    final String definition = fieldDef.get(i);
                    final UomProperty uomCode;
                    if (uomCodes.get(i) != null) {
                        uomCode = SOSXmlFactory.buildUomProperty(version, uomCodes.get(i), null);
                    } else {
                        uomCode = null;
                    }
                    if (typeName != null) {
                        if ("Quantity".equals(typeName)) {
                            component = SOSXmlFactory.buildQuantity(version,definition, uomCode, null);
                        } else if ("Time".equals(typeName)) {
                            component = SOSXmlFactory.buildTime(version, definition, uomCode);
                        } else if ("Boolean".equals(typeName)) {
                            component = SOSXmlFactory.buildBoolean(version, definition, null);
                        } else {
                            LOGGER.severe("unexpected field type");
                        }
                    }
                    final AnyScalar field = SOSXmlFactory.buildAnyScalar(version, dataRecordId, fieldName, component);
                    fields.add(field);
                }

                final AbstractDataRecord elementType = SOSXmlFactory.buildSimpleDatarecord(version, blockId, dataRecordId, null, false, fields);

                final String dataValues = values.getVariable("var33");
                return SOSXmlFactory.buildDataArrayProperty(version, blockId, count, blockId, elementType, encoding, dataValues);
                
            } else if (resultModel.equals(MEASUREMENT_QNAME)) {
                final Values values    = loadData(Arrays.asList("var77", "var78"), identifier);
                final String uomValue = values.getVariable("var78");
                final float val;
                if (uomValue != null) {
                    val = Float.parseFloat(uomValue);
                } else {
                    val = 0;
                }
                final String uomId     = values.getVariable("var77");
                final Values uomvalues = loadData(Arrays.asList("var79", "var80", "var81"), uomId);
                final UnitOfMeasureEntry uom = new UnitOfMeasureEntry(uomId,
                                                                      uomvalues.getVariable("var79"),
                                                                      uomvalues.getVariable("var80"),
                                                                      uomvalues.getVariable("var81"));
                return new MeasureType(uom, val);
            } else {
                throw new IllegalArgumentException("unexpected resultModel:" + resultModel);
            }
        } catch (MetadataIoException ex) {
            throw new DataStoreException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TemporalPrimitive getFeatureOfInterestTime(final String samplingFeatureName, final String version) throws DataStoreException {
        throw new DataStoreException("The Default generic implementation of SOS does not support GetFeatureofInterestTime");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existProcedure(final String href) throws DataStoreException {
        try {
            final Values values = loadData(Arrays.asList("var02"));
            final List<String>  procedureNames = values.getVariables("var02");
            return procedureNames.contains(href);
        } catch (MetadataIoException ex) {
            throw new DataStoreException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInfos() {
        return "Constellation Postgrid Generic O&M Reader 0.9";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ResponseModeType> getResponseModes() throws DataStoreException {
        return Arrays.asList(ResponseModeType.INLINE, ResponseModeType.RESULT_TEMPLATE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getResponseFormats() throws DataStoreException {
        return Arrays.asList("text/xml; subtype=\"om/1.0.0\"");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getProceduresForPhenomenon(String observedProperty) throws DataStoreException {
        throw new UnsupportedOperationException("Not supported yet in this implementation.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<String> getPhenomenonsForProcedure(String sensorID) throws DataStoreException {
        throw new UnsupportedOperationException("Not supported yet in this implementation."); 
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractGeometry getSensorLocation(String sensorID, String version) throws DataStoreException {
        throw new UnsupportedOperationException("Not supported yet in this implementation.");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public TemporalGeometricPrimitive getTimeForProcedure(final String version, final String sensorID) throws DataStoreException {
        throw new UnsupportedOperationException("Not supported yet in this implementation.");
    }

    @Override
    public Observation getTemplateForProcedure(String procedure, String version) throws DataStoreException {
        throw new UnsupportedOperationException("Not supported yet in this implementation.");
    }

    @Override
    public Collection<String> getOfferingNames(String version, String sensorType) throws DataStoreException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<ObservationOffering> getObservationOfferings(String version, String sensorType) throws DataStoreException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<String> getProcedureNames(String sensorType) throws DataStoreException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
