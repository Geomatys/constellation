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
import org.constellation.swe.v101.PhenomenonPropertyType;
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
        Values values = loadData(Arrays.asList("var01"));
        return values.getVariables("var01");
    }

    @Override
    public List<String> getProcedureNames() throws CstlServiceException {
        Values values = loadData(Arrays.asList("var02"));
        return values.getVariables("var02");
    }

    @Override
    public List<String> getPhenomenonNames() throws CstlServiceException {
        Values values = loadData(Arrays.asList("var03"));
        return values.getVariables("var03");
    }

    @Override
    public List<String> getFeatureOfInterestNames() throws CstlServiceException {
        Values values = loadData(Arrays.asList("var04"));
        return values.getVariables("var04");
    }

    @Override
    public String getNewObservationId() throws CstlServiceException {
        Values values = loadData(Arrays.asList("var05"));
        int id = Integer.parseInt(values.getVariable("var05"));

        values = loadData(Arrays.asList("var44"), observationIdBase + id);
        String _continue = null;
        do {
            id++;
            _continue = values.getVariable("var44");

        } while (_continue != null);
        return observationIdBase + id;
    }

    @Override
    public List<String> getEventTime() throws CstlServiceException {
         Values values = loadData(Arrays.asList("var06"));
         return Arrays.asList(values.getVariable("var06"));
    }

    @Override
    public ObservationOfferingEntry getObservationOffering(String offeringName) throws CstlServiceException {
        Values values = loadData(Arrays.asList("var07", "var08", "var09", "var10", "var11", "var12", "var18"), offeringName);
        List<String> srsName = values.getVariables("var07");

        // event time
        TimePeriodType time;
        String offeringBegin = values.getVariable("var08");
        if (offeringBegin != null)
            offeringBegin    = offeringBegin.replace(' ', 'T');
        String offeringEnd   = values.getVariable("var09");
        if (offeringEnd != null) {
            offeringEnd          = offeringEnd.replace(' ', 'T');
            time  = new TimePeriodType(offeringBegin, offeringEnd);
        } else {
            time  = new TimePeriodType(offeringBegin);
        }


        // procedure
        List<ReferenceEntry> procedures = new ArrayList<ReferenceEntry>();
        for (String procedureName : values.getVariables("var10")) {
            procedures.add(new ReferenceEntry(null, procedureName));
        }

        // phenomenon
        List<PhenomenonPropertyType> observedProperties = new ArrayList<PhenomenonPropertyType>();
        for (String phenomenonId : values.getVariables("var12")) {
            if (phenomenonId!= null && !phenomenonId.equals("")) {
                Values compositeValues = loadData(Arrays.asList("var17"), phenomenonId);
                List<PhenomenonEntry> components = new ArrayList<PhenomenonEntry>();
                for (String componentID : compositeValues.getVariables("var17")) {
                    components.add(getPhenomenon(componentID));
                }
                compositeValues = loadData(Arrays.asList("var15", "var16"), phenomenonId);
                CompositePhenomenonEntry phenomenon = new CompositePhenomenonEntry(phenomenonId,
                                                                                   compositeValues.getVariable("var15"),
                                                                                   compositeValues.getVariable("var16"),
                                                                                   null,
                                                                                   components);
                observedProperties.add(new PhenomenonPropertyType(phenomenon));
            }
        }
        for (String phenomenonId : values.getVariables("var11")) {
            if (phenomenonId != null && !phenomenonId.equals("")) {
                PhenomenonEntry phenomenon = getPhenomenon(phenomenonId);
                observedProperties.add(new PhenomenonPropertyType(phenomenon));
            }
        }

        // feature of interest
        List<ReferenceEntry> fois = new ArrayList<ReferenceEntry>();
        for (String foiID : values.getVariables("var18")) {
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
        Values values = loadData(Arrays.asList("var01"));
        List<ObservationOfferingEntry> offerings = new ArrayList<ObservationOfferingEntry>();
        List<String> offeringNames = values.getVariables("var01");
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
        PhenomenonEntry phenomenon = new PhenomenonEntry(phenomenonName, values.getVariable("var13"), values.getVariable("var14"));
        return phenomenon;
    }

    @Override
    public SamplingFeatureEntry getFeatureOfInterest(String samplingFeatureId) throws CstlServiceException {
        Values values = loadData(Arrays.asList("var19", "var20", "var21", "var22", "var23", "var24"), samplingFeatureId);
        String name            = values.getVariable("var19");
        String description     = values.getVariable("var20");
        String sampledFeature  = values.getVariable("var21");

        String pointID         = values.getVariable("var22");
        String SRSname         = values.getVariable("var23");

        String dimension       = values.getVariable("var24");
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
        List<String> coordinates = values.getVariables("var25");
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
        SamplingFeatureEntry featureOfInterest = getFeatureOfInterest(values.getVariable("var26"));
        PhenomenonEntry observedProperty = getPhenomenon(values.getVariable("var27"));
        ProcessEntry procedure = new ProcessEntry(values.getVariable("var28"));

        TimePeriodType samplingTime = new TimePeriodType(values.getVariable("var29"), values.getVariable("var30"));
        AnyResultEntry anyResult = getResult(values.getVariable("var31"));
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
        int count = Integer.parseInt(values.getVariable("var32"));

        // encoding
        String encodingID       = values.getVariable("var34");
        String tokenSeparator   = values.getVariable("var35");
        String decimalSeparator = values.getVariable("var36");
        String blockSeparator   = values.getVariable("var37");
        TextBlockEntry encoding = new TextBlockEntry(encodingID, tokenSeparator, blockSeparator, decimalSeparator);

        //data block description
        String blockId          = values.getVariable("var38");
        String dataRecordId     = values.getVariable("var39");
        Set<AnyScalarPropertyType> fields = new HashSet<AnyScalarPropertyType>();
        List<String> fieldNames = values.getVariables("var40");
        List<String> fieldDef   = values.getVariables("var41");
        List<String> type       = values.getVariables("var42");
        List<String> uomCodes   = values.getVariables("var43");
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

        String dataValues = values.getVariable("var33");
        DataArrayEntry result = new DataArrayEntry(blockId, count, elementType, encoding, dataValues);
        return new AnyResultEntry(identifier, result);
    }

    @Override
    public ReferenceEntry getReference(String href) throws CstlServiceException {
        //TODO
        return new ReferenceEntry(null, href);
    }

    public String getInfos() {
        return "Constellation Postgrid Generic O&M Reader 0.3";
    }

}
