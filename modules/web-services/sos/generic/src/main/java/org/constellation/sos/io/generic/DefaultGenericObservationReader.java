/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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

package org.constellation.sos.io.generic;

import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.xml.namespace.QName;

// constellation dependencies
import org.constellation.sos.factory.OMFactory;
import org.constellation.generic.GenericReader;
import org.constellation.generic.Values;
import org.constellation.generic.database.Automatic;
import org.constellation.metadata.io.MetadataIoException;
import org.constellation.sos.io.ObservationReader;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.MimeType;
import static org.constellation.sos.ws.SOSConstants.*;
import org.geotoolkit.gml.xml.v311.AbstractTimePrimitiveType;

import org.geotoolkit.sos.xml.v100.ObservationOfferingType;
import org.geotoolkit.sos.xml.v100.ResponseModeType;
import org.geotoolkit.swe.xml.v101.AbstractDataComponentType;
import org.geotoolkit.swe.xml.v101.AnyResultType;
import org.geotoolkit.swe.xml.v101.AnyScalarPropertyType;
import org.geotoolkit.swe.xml.v101.BooleanType;
import org.geotoolkit.swe.xml.v101.CompositePhenomenonType;
import org.geotoolkit.swe.xml.v101.DataArrayType;
import org.geotoolkit.swe.xml.v101.DataArrayPropertyType;
import org.geotoolkit.swe.xml.v101.PhenomenonType;
import org.geotoolkit.swe.xml.v101.PhenomenonPropertyType;
import org.geotoolkit.swe.xml.v101.QuantityType;
import org.geotoolkit.swe.xml.v101.SimpleDataRecordType;
import org.geotoolkit.swe.xml.v101.TextBlockType;
import org.geotoolkit.swe.xml.v101.TimeType;
import org.geotoolkit.gml.xml.v311.DirectPositionType;
import org.geotoolkit.gml.xml.v311.FeaturePropertyType;
import org.geotoolkit.gml.xml.v311.PointPropertyType;
import org.geotoolkit.gml.xml.v311.PointType;
import org.geotoolkit.gml.xml.v311.ReferenceType;
import org.geotoolkit.gml.xml.v311.TimePeriodType;
import org.geotoolkit.observation.xml.v100.ObservationType;
import org.geotoolkit.observation.xml.v100.ProcessType;
import org.geotoolkit.sampling.xml.v100.SamplingFeatureType;
import org.geotoolkit.sampling.xml.v100.SamplingPointType;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;


/**
 *
 * @author Guilhem Legal
 */
public class DefaultGenericObservationReader extends GenericReader implements ObservationReader {

    /**
     * The base for observation id.
     */
    protected final String observationIdBase;

    private static final String VAR01 = "var01";
    
    public DefaultGenericObservationReader(Automatic configuration, Map<String, Object> properties) throws CstlServiceException, MetadataIoException {
        super(configuration);
        this.observationIdBase = (String) properties.get(OMFactory.OBSERVATION_ID_BASE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getOfferingNames() throws CstlServiceException {
        try {
            final Values values = loadData(Arrays.asList(VAR01));
            return values.getVariables(VAR01);
        } catch (MetadataIoException ex) {
            throw new CstlServiceException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getProcedureNames() throws CstlServiceException {
        try {
            final Values values = loadData(Arrays.asList("var02"));
            return values.getVariables("var02");
        } catch (MetadataIoException ex) {
            throw new CstlServiceException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getPhenomenonNames() throws CstlServiceException {
        try {
            final Values values = loadData(Arrays.asList("var03"));
            return values.getVariables("var03");
        } catch (MetadataIoException ex) {
            throw new CstlServiceException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getFeatureOfInterestNames() throws CstlServiceException {
        try {
            final Values values = loadData(Arrays.asList("var04"));
            return values.getVariables("var04");
        } catch (MetadataIoException ex) {
            throw new CstlServiceException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNewObservationId() throws CstlServiceException {
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
            throw new CstlServiceException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getEventTime() throws CstlServiceException {
         try {
            final Values values = loadData(Arrays.asList("var06"));
            return Arrays.asList(values.getVariable("var06"));
         } catch (MetadataIoException ex) {
            throw new CstlServiceException(ex);
         }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ObservationOfferingType> getObservationOfferings(final List<String> offeringNames) throws CstlServiceException {
        final List<ObservationOfferingType> offerings = new ArrayList<ObservationOfferingType>();
        for (String offeringName : offeringNames) {
            offerings.add(getObservationOffering(offeringName));
        }
        return offerings;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ObservationOfferingType getObservationOffering(final String offeringName) throws CstlServiceException {
        try {
            final Values values = loadData(Arrays.asList("var07", "var08", "var09", "var10", "var11", "var12", "var18", "var46"), offeringName);

            final boolean exist = values.getVariable("var46") != null;
            if (!exist) {
                return null;
            }
            
            final List<String> srsName = values.getVariables("var07");

            // event time
            TimePeriodType time;
            String offeringBegin = values.getVariable("var08");
            if (offeringBegin != null) {
                offeringBegin    = offeringBegin.replace(' ', 'T');
            }
            String offeringEnd   = values.getVariable("var09");
            if (offeringEnd != null) {
                offeringEnd          = offeringEnd.replace(' ', 'T');
                time  = new TimePeriodType(offeringBegin, offeringEnd);
            } else {
                time  = new TimePeriodType(offeringBegin);
            }


            // procedure
            final List<ReferenceType> procedures = new ArrayList<ReferenceType>();
            for (String procedureName : values.getVariables("var10")) {
                procedures.add(new ReferenceType(null, procedureName));
            }

            // phenomenon
            final List<PhenomenonPropertyType> observedProperties = new ArrayList<PhenomenonPropertyType>();
            for (String phenomenonId : values.getVariables("var12")) {
                if (phenomenonId!= null && !phenomenonId.isEmpty()) {
                    Values compositeValues = loadData(Arrays.asList("var17"), phenomenonId);
                    final List<PhenomenonType> components = new ArrayList<PhenomenonType>();
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
                }
            }
            for (String phenomenonId : values.getVariables("var11")) {
                if (phenomenonId != null && !phenomenonId.isEmpty()) {
                    final PhenomenonType phenomenon = getPhenomenon(phenomenonId);
                    observedProperties.add(new PhenomenonPropertyType(phenomenon));
                }
            }

            // feature of interest
            final List<ReferenceType> fois = new ArrayList<ReferenceType>();
            for (String foiID : values.getVariables("var18")) {
                fois.add(new ReferenceType(null, foiID));
            }

            //static part
            final List<String> responseFormat = Arrays.asList(MimeType.APPLICATION_XML);
            final List<QName> resultModel     = Arrays.asList(OBSERVATION_QNAME);
            final List<ResponseModeType> responseMode = Arrays.asList(ResponseModeType.INLINE, ResponseModeType.RESULT_TEMPLATE);
            return new ObservationOfferingType(offeringName,
                                                offeringName,
                                                null,
                                                null,
                                                null,
                                                srsName,
                                                time,
                                                procedures,
                                                observedProperties,
                                                fois,
                                                responseFormat,
                                                resultModel,
                                                responseMode);
        } catch (MetadataIoException ex) {
            throw new CstlServiceException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ObservationOfferingType> getObservationOfferings() throws CstlServiceException {
        try {
            final Values values = loadData(Arrays.asList(VAR01));
            final List<ObservationOfferingType> offerings = new ArrayList<ObservationOfferingType>();
            final List<String> offeringNames = values.getVariables(VAR01);
            for (String offeringName : offeringNames) {
                offerings.add(getObservationOffering(offeringName));
            }
            return offerings;
        } catch (MetadataIoException ex) {
            throw new CstlServiceException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PhenomenonType getPhenomenon(String phenomenonName) throws CstlServiceException {
        // TODO return composite phenomenon
        try {
            final Values values = loadData(Arrays.asList("var13", "var14", "var47"), phenomenonName);
            final boolean exist = values.getVariable("var47") != null;
            if (!exist) {
                return null;
            }
            return new PhenomenonType(phenomenonName, values.getVariable("var13"), values.getVariable("var14"));
        } catch (MetadataIoException ex) {
            throw new CstlServiceException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SamplingFeatureType getFeatureOfInterest(String samplingFeatureId) throws CstlServiceException {
        try {
            final Values values = loadData(Arrays.asList("var19", "var20", "var21", "var22", "var23", "var24", "var48"), samplingFeatureId);

            final boolean exist = values.getVariable("var48") != null;
            if (!exist) {
                return null;
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
            final List<Double> coordinates = getCoordinates(samplingFeatureId);
            final DirectPositionType pos = new DirectPositionType(srsName, srsDimension, coordinates);
            final PointType location     = new PointType(pointID, pos);

            final FeaturePropertyType sampleFeatureProperty;
            if (sampledFeature != null) {
                sampleFeatureProperty = new FeaturePropertyType(sampledFeature);
            } else {
                sampleFeatureProperty = null;
            }
            return  new SamplingPointType(samplingFeatureId, name, description, sampleFeatureProperty, new PointPropertyType(location));
        } catch (MetadataIoException ex) {
            throw new CstlServiceException(ex);
        }
    }

    private List<Double> getCoordinates(String samplingFeatureId) throws CstlServiceException {
        try {
            final Values values = loadData(Arrays.asList("var25", "var45"), samplingFeatureId);
            final List<Double> result = new ArrayList<Double>();
            String coordinate = values.getVariable("var25");
            if (coordinate != null) {
                try {
                    result.add(Double.parseDouble(coordinate));
                } catch (NumberFormatException ex) {
                    throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
                }
            }
            coordinate = values.getVariable("var45");
            if (coordinate != null) {
                try {
                    result.add(Double.parseDouble(coordinate));
                } catch (NumberFormatException ex) {
                    throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
                }
            }
            return result;
        } catch (MetadataIoException ex) {
            throw new CstlServiceException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ObservationType getObservation(String identifier, QName resultModel) throws CstlServiceException {
        try {
            final Values values = loadData(Arrays.asList("var26", "var27", "var28", "var29", "var30", "var31"), identifier);
            final SamplingFeatureType featureOfInterest = getFeatureOfInterest(values.getVariable("var26"));
            final PhenomenonType observedProperty = getPhenomenon(values.getVariable("var27"));
            final ProcessType procedure = new ProcessType(values.getVariable("var28"));

            String begin = values.getVariable("var29");
            if (begin != null) {
                begin = begin.replace(' ', 'T');
            }
            String end   = values.getVariable("var30");
            if (end != null) {
                end = end.replace(' ', 'T');
            }
            final TimePeriodType samplingTime = new TimePeriodType(begin, end);
            final AnyResultType anyResult = getResult(values.getVariable("var31"), resultModel);
            final DataArrayType dataArray = anyResult.getArray();
            final DataArrayPropertyType result = new DataArrayPropertyType(dataArray);
            return new ObservationType(identifier,
                                        null,
                                        featureOfInterest,
                                        observedProperty,
                                        procedure,
                                        result,
                                        samplingTime);
        } catch (MetadataIoException ex) {
            throw new CstlServiceException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AnyResultType getResult(String identifier, QName resutModel) throws CstlServiceException {
        try {
            final Values values = loadData(Arrays.asList("var32", "var33", "var34", "var35", "var36", "var37", "var38", "var39",
                    "var40", "var41", "var42", "var43"), identifier);
            final int count = Integer.parseInt(values.getVariable("var32"));

            // encoding
            final String encodingID       = values.getVariable("var34");
            final String tokenSeparator   = values.getVariable("var35");
            final String decimalSeparator = values.getVariable("var36");
            final String blockSeparator   = values.getVariable("var37");
            final TextBlockType encoding = new TextBlockType(encodingID, tokenSeparator, blockSeparator, decimalSeparator);

            //data block description
            final String blockId          = values.getVariable("var38");
            final String dataRecordId     = values.getVariable("var39");
            final Set<AnyScalarPropertyType> fields = new HashSet<AnyScalarPropertyType>();
            final List<String> fieldNames = values.getVariables("var40");
            final List<String> fieldDef   = values.getVariables("var41");
            final List<String> type       = values.getVariables("var42");
            final List<String> uomCodes   = values.getVariables("var43");
            for(int i = 0; i < fieldNames.size(); i++) {
                AbstractDataComponentType component = null;
                final String typeName   = type.get(i);
                final String fieldName  = fieldNames.get(i);
                final String definition = fieldDef.get(i);
                final String uomCode    = uomCodes.get(i);
                if (typeName != null) {
                    if ("Quantity".equals(typeName)) {
                        component = new QuantityType(definition, uomCode, null);
                    } else if ("Time".equals(typeName)) {
                        component = new TimeType(definition, uomCode, null);
                    } else if ("Boolean".equals(typeName)) {
                        component = new BooleanType(definition, null);
                    } else {
                        LOGGER.severe("unexpected field type");
                    }
                }
                final AnyScalarPropertyType field = new AnyScalarPropertyType(dataRecordId, fieldName, component);
                fields.add(field);
            }

            final SimpleDataRecordType elementType = new SimpleDataRecordType(blockId, dataRecordId, null, false, fields);

            final String dataValues     = values.getVariable("var33");
            final DataArrayType result = new DataArrayType(blockId, count, elementType, encoding, dataValues);
            return new AnyResultType(identifier, result);
        } catch (MetadataIoException ex) {
            throw new CstlServiceException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractTimePrimitiveType getFeatureOfInterestTime(String samplingFeatureName) throws CstlServiceException {
        throw new CstlServiceException("The Default generic implementation of SOS does not support GetFeatureofInterestTime");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReferenceType getReference(String href) throws CstlServiceException {
        //TODO
        return new ReferenceType(null, href);
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
    public List<ResponseModeType> getResponseModes() throws CstlServiceException {
        return Arrays.asList(ResponseModeType.INLINE, ResponseModeType.RESULT_TEMPLATE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getResponseFormats() throws CstlServiceException {
        return Arrays.asList("text/xml; subtype=\"om/1.0.0\"");
    }
}
