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

package org.constellation.sos.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;

// constellation dependencies
import org.constellation.generic.database.Automatic;
import org.constellation.gml.v311.DirectPositionType;
import org.constellation.gml.v311.PointType;
import org.constellation.gml.v311.ReferenceEntry;
import org.constellation.gml.v311.TimePeriodType;
import org.constellation.observation.ObservationEntry;
import org.constellation.observation.ProcessEntry;
import org.constellation.sampling.SamplingFeatureEntry;
import org.constellation.sampling.SamplingPointEntry;
import org.constellation.sos.v100.ObservationOfferingEntry;
import org.constellation.sos.v100.ResponseModeType;
import org.constellation.swe.v101.AbstractDataComponentEntry;
import org.constellation.swe.v101.AnyResultEntry;
import org.constellation.swe.v101.AnyScalarPropertyType;
import org.constellation.swe.v101.BooleanType;
import org.constellation.swe.v101.CompositePhenomenonEntry;
import org.constellation.swe.v101.DataArrayEntry;
import org.constellation.swe.v101.DataArrayPropertyType;
import org.constellation.swe.v101.PhenomenonEntry;
import org.constellation.swe.v101.QuantityType;
import org.constellation.swe.v101.SimpleDataRecordEntry;
import org.constellation.swe.v101.TextBlockEntry;
import org.constellation.swe.v101.TimeType;
import org.constellation.ws.CstlServiceException;
import static org.constellation.sos.ws.SOSworker.*;
import static org.constellation.ows.OWSExceptionCode.*;

/**
 *
 * @author Guilhem Legal
 */
public class DefaultGenericObservationReader extends GenericObservationReader {

    public DefaultGenericObservationReader(String observationIdBase, Automatic configuration) throws CstlServiceException {
        super(observationIdBase, configuration);
    }

    @Override
    public List<String> getOfferingNames() throws CstlServiceException {
        Values values = loadData(Arrays.asList("var01"), null);
        return getVariables("var01", values);
    }

    @Override
    public List<String> getProcedureNames() throws CstlServiceException {
        Values values = loadData(Arrays.asList("var02"), null);
        return getVariables("var02", values);
    }

    @Override
    public List<String> getPhenomenonNames() throws CstlServiceException {
        Values values = loadData(Arrays.asList("var03"), null);
        return getVariables("var03", values);
    }

    @Override
    public List<String> getFeatureOfInterestNames() throws CstlServiceException {
        Values values = loadData(Arrays.asList("var04"), null);
        return getVariables("var04", values);
    }

    @Override
    public String getNewObservationId() throws CstlServiceException {
        Values values = loadData(Arrays.asList("var05"), null);
        int id = Integer.parseInt(getVariable("var05", values));

        values = loadData(Arrays.asList("var44"), observationIdBase + id);
        String _continue = null;
        do {
            id++;
            _continue = getVariable("var44", values);

        } while (_continue != null);
        return observationIdBase + id;
    }

    @Override
    public String getMinimalEventTime() throws CstlServiceException {
         Values values = loadData(Arrays.asList("var06"), null);
        return getVariable("var06", values);
    }

    @Override
    public ObservationOfferingEntry getObservationOffering(String offeringName) throws CstlServiceException {
        Values values = loadData(Arrays.asList("var07", "var08", "var09", "var10", "var11", "var12", "var18"), offeringName);
        List<String> srsName = getVariables("var07", values);

        // event time
        TimePeriodType time;
        String offeringBegin = getVariable("var08", values);
        if (offeringBegin != null)
            offeringBegin        = offeringBegin.replace(' ', 'T');
        String offeringEnd   = getVariable("var09", values);
        if (offeringEnd != null) {
            offeringEnd          = offeringEnd.replace(' ', 'T');
            time  = new TimePeriodType(offeringBegin, offeringEnd);
        } else {
            time  = new TimePeriodType(offeringBegin);
        }


        // procedure
        List<ReferenceEntry> procedures = new ArrayList<ReferenceEntry>();
        for (String procedureName : getVariables("var10", values)) {
            procedures.add(new ReferenceEntry(null, procedureName));
        }

        // phenomenon
        List<PhenomenonEntry> observedProperties = new ArrayList<PhenomenonEntry>();
        for (String phenomenonId : getVariables("var12", values)) {
            if (phenomenonId!= null && !phenomenonId.equals("")) {
                Values compositeValues = loadData(Arrays.asList("var17"), phenomenonId);
                List<PhenomenonEntry> components = new ArrayList<PhenomenonEntry>();
                for (String componentID : getVariables("var17", compositeValues)) {
                    components.add(getPhenomenon(componentID));
                }
                compositeValues = loadData(Arrays.asList("var15", "var16"), phenomenonId);
                CompositePhenomenonEntry phenomenon = new CompositePhenomenonEntry(phenomenonId,
                                                                                   getVariable("var15", compositeValues),
                                                                                   getVariable("var16", compositeValues),
                                                                                   null,
                                                                                   components);
                observedProperties.add(phenomenon);
            }
        }
        for (String phenomenonId : getVariables("var11", values)) {
            if (phenomenonId != null && !phenomenonId.equals("")) {
                PhenomenonEntry phenomenon = getPhenomenon(phenomenonId);
                observedProperties.add(phenomenon);
            }
        }

        // feature of interest
        List<ReferenceEntry> fois = new ArrayList<ReferenceEntry>();
        for (String foiID : getVariables("var18", values)) {
            fois.add(new ReferenceEntry(null, foiID));
        }

        //static part
        List<String> responseFormat = Arrays.asList("application/xml");
        List<QName> resultModel     = Arrays.asList(observation_QNAME);
        List<ResponseModeType> responseMode = Arrays.asList(ResponseModeType.INLINE, ResponseModeType.RESULT_TEMPLATE);
        ObservationOfferingEntry offering = new ObservationOfferingEntry(offeringName,
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
        return offering;
    }

    @Override
    public List<ObservationOfferingEntry> getObservationOfferings() throws CstlServiceException {
        Values values = loadData(Arrays.asList("var01"), null);
        List<ObservationOfferingEntry> offerings = new ArrayList<ObservationOfferingEntry>();
        List<String> offeringNames = getVariables("var01", values);
        for (String offeringName : offeringNames) {
            offerings.add(getObservationOffering(offeringName));
        }
        return offerings;
    }

    /**
     *  TODO return composite phenomenon
     */
    @Override
    public PhenomenonEntry getPhenomenon(String phenomenonName) throws CstlServiceException {
        Values values = loadData(Arrays.asList("var13", "var14"), phenomenonName);
        PhenomenonEntry phenomenon = new PhenomenonEntry(phenomenonName, getVariable("var13", values), getVariable("var14", values));
        return phenomenon;
    }

    @Override
    public SamplingFeatureEntry getFeatureOfInterest(String samplingFeatureId) throws CstlServiceException {
        Values values = loadData(Arrays.asList("var19", "var20", "var21", "var22", "var23", "var24"), samplingFeatureId);
        String name            = getVariable("var19", values);
        String description     = getVariable("var20", values);
        String sampledFeature  = getVariable("var21", values);

        String pointID         = getVariable("var22", values);
        String SRSname         = getVariable("var23", values);

        String dimension       = getVariable("var24", values);
        int srsDimension       = 0;
        try {
            srsDimension       = Integer.parseInt(dimension);
        } catch (NumberFormatException ex) {
            logger.severe("unable to parse the srs dimension: " + dimension);
        }
        List<Double> coordinates = getCoordinates(samplingFeatureId);
        DirectPositionType pos = new DirectPositionType(SRSname, srsDimension, coordinates);
        PointType location     = new PointType(pointID, pos);

        SamplingPointEntry foi = new SamplingPointEntry(samplingFeatureId, name, description, sampledFeature, location);
        return foi;
    }

    private List<Double> getCoordinates(String samplingFeatureId) throws CstlServiceException {
        Values values = loadData(Arrays.asList("var25"), samplingFeatureId);
        List<Double> result = new ArrayList<Double>();
        List<String> coordinates = getVariables("var25", values);
        for (String coordinate : coordinates) {
            try {
                result.add(Double.parseDouble(coordinate));
            } catch (NumberFormatException ex) {
                throw new CstlServiceException(ex, NO_APPLICABLE_CODE);
            }
        }
        return result;
    }

    @Override
    public ObservationEntry getObservation(String identifier) throws CstlServiceException {
        Values values = loadData(Arrays.asList("var26", "var27", "var28", "var29", "var30", "var31"), identifier);
        SamplingFeatureEntry featureOfInterest = getFeatureOfInterest(getVariable("var26", values));
        PhenomenonEntry observedProperty = getPhenomenon(getVariable("var27", values));
        ProcessEntry procedure = new ProcessEntry(getVariable("var28", values));

        TimePeriodType samplingTime = new TimePeriodType(getVariable("var29", values), getVariable("var30", values));
        AnyResultEntry anyResult = getResult(getVariable("var31", values));
        DataArrayEntry dataArray = anyResult.getArray();
        DataArrayPropertyType result = new DataArrayPropertyType(dataArray);
        ObservationEntry observation = new ObservationEntry(identifier,
                                                            null,
                                                            featureOfInterest,
                                                            observedProperty,
                                                            procedure,
                                                            result,
                                                            samplingTime);
        return observation;
    }

    @Override
    public AnyResultEntry getResult(String identifier) throws CstlServiceException {
        Values values = loadData(Arrays.asList("var32", "var33", "var34", "var35", "var36", "var37", "var38", "var39",
                "var40", "var41", "var42", "var43"), identifier);
        int count = Integer.parseInt(getVariable("var32", values));

        // encoding
        String encodingID       = getVariable("var34", values);
        String tokenSeparator   = getVariable("var35", values);
        String decimalSeparator = getVariable("var36", values);
        String blockSeparator   = getVariable("var37", values);
        TextBlockEntry encoding = new TextBlockEntry(encodingID, tokenSeparator, blockSeparator, decimalSeparator);

        //data block description
        String blockId          = getVariable("var38", values);
        String dataRecordId     = getVariable("var39", values);
        Set<AnyScalarPropertyType> fields = new HashSet<AnyScalarPropertyType>();
        List<String> fieldNames = getVariables("var40", values);
        List<String> fieldDef   = getVariables("var41", values);
        List<String> type       = getVariables("var42", values);
        List<String> uomCodes   = getVariables("var43", values);
        for(int i = 0; i < fieldNames.size(); i++) {
            AbstractDataComponentEntry component = null;
            String typeName   = type.get(i);
            String definition = fieldDef.get(i);
            String uomCode    = uomCodes.get(i);
            if (typeName != null) {
                if (typeName.equals("Quantity")) {
                    component = new QuantityType(definition, uomCode, null);
                } else if (typeName.equals("Time")) {
                    component = new TimeType(definition, uomCode, null);
                } else if (typeName.equals("Boolean")) {
                    component = new BooleanType(definition, null);
                } else {
                    logger.severe("unexpected field type");
                }
            }
            AnyScalarPropertyType field = new AnyScalarPropertyType(dataRecordId, blockId, component);
            fields.add(field);
        }

        SimpleDataRecordEntry elementType = new SimpleDataRecordEntry(blockId, dataRecordId, null, false, fields);

        String dataValues = getVariable("var33", values);
        DataArrayEntry result = new DataArrayEntry(blockId, count, elementType, encoding, dataValues);
        return new AnyResultEntry(identifier, result);
    }

    @Override
    public ReferenceEntry getReference(String href) throws CstlServiceException {
        //TODO
        return new ReferenceEntry(null, href);
    }


}
