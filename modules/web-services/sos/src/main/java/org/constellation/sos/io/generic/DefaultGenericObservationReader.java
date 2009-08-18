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
import org.constellation.sos.io.ObservationReader;
import org.constellation.sos.ws.Parameters;
import org.geotoolkit.sos.xml.v100.ObservationOfferingEntry;
import org.geotoolkit.sos.xml.v100.ResponseModeType;
import org.geotoolkit.swe.xml.v101.AbstractDataComponentEntry;
import org.geotoolkit.swe.xml.v101.AnyResultEntry;
import org.geotoolkit.swe.xml.v101.AnyScalarPropertyType;
import org.geotoolkit.swe.xml.v101.BooleanType;
import org.geotoolkit.swe.xml.v101.CompositePhenomenonEntry;
import org.geotoolkit.swe.xml.v101.DataArrayEntry;
import org.geotoolkit.swe.xml.v101.DataArrayPropertyType;
import org.geotoolkit.swe.xml.v101.PhenomenonEntry;
import org.geotoolkit.swe.xml.v101.PhenomenonPropertyType;
import org.geotoolkit.swe.xml.v101.QuantityType;
import org.geotoolkit.swe.xml.v101.SimpleDataRecordEntry;
import org.geotoolkit.swe.xml.v101.TextBlockEntry;
import org.geotoolkit.swe.xml.v101.TimeType;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.MimeType;
import org.geotoolkit.gml.xml.v311.DirectPositionType;
import org.geotoolkit.gml.xml.v311.PointType;
import org.geotoolkit.gml.xml.v311.ReferenceEntry;
import org.geotoolkit.gml.xml.v311.TimePeriodType;
import org.geotoolkit.observation.xml.v100.ObservationEntry;
import org.geotoolkit.observation.xml.v100.ProcessEntry;
import org.geotoolkit.sampling.xml.v100.SamplingFeatureEntry;
import org.geotoolkit.sampling.xml.v100.SamplingPointEntry;
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
    
    public DefaultGenericObservationReader(String observationIdBase, Automatic configuration) throws CstlServiceException {
        super(configuration);
        this.observationIdBase = observationIdBase;
    }

    @Override
    public List<String> getOfferingNames() throws CstlServiceException {
        final Values values = loadData(Arrays.asList(VAR01));
        return values.getVariables(VAR01);
    }

    @Override
    public List<String> getProcedureNames() throws CstlServiceException {
        final Values values = loadData(Arrays.asList("var02"));
        return values.getVariables("var02");
    }

    @Override
    public List<String> getPhenomenonNames() throws CstlServiceException {
        final Values values = loadData(Arrays.asList("var03"));
        return values.getVariables("var03");
    }

    @Override
    public List<String> getFeatureOfInterestNames() throws CstlServiceException {
        final Values values = loadData(Arrays.asList("var04"));
        return values.getVariables("var04");
    }

    @Override
    public String getNewObservationId() throws CstlServiceException {
        Values values = loadData(Arrays.asList("var05"));
        int id = Integer.parseInt(values.getVariable("var05"));

        values = loadData(Arrays.asList("var44"), observationIdBase + id);
        String continues = null;
        do {
            id++;
            continues = values.getVariable("var44");

        } while (continues != null);
        return observationIdBase + id;
    }

    @Override
    public List<String> getEventTime() throws CstlServiceException {
         final Values values = loadData(Arrays.asList("var06"));
         return Arrays.asList(values.getVariable("var06"));
    }

    @Override
    public ObservationOfferingEntry getObservationOffering(String offeringName) throws CstlServiceException {
        final Values values = loadData(Arrays.asList("var07", "var08", "var09", "var10", "var11", "var12", "var18"), offeringName);
        final List<String> srsName = values.getVariables("var07");

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
        final List<ReferenceEntry> procedures = new ArrayList<ReferenceEntry>();
        for (String procedureName : values.getVariables("var10")) {
            procedures.add(new ReferenceEntry(null, procedureName));
        }

        // phenomenon
        final List<PhenomenonPropertyType> observedProperties = new ArrayList<PhenomenonPropertyType>();
        for (String phenomenonId : values.getVariables("var12")) {
            if (phenomenonId!= null && !phenomenonId.equals("")) {
                Values compositeValues = loadData(Arrays.asList("var17"), phenomenonId);
                final List<PhenomenonEntry> components = new ArrayList<PhenomenonEntry>();
                for (String componentID : compositeValues.getVariables("var17")) {
                    components.add(getPhenomenon(componentID));
                }
                compositeValues = loadData(Arrays.asList("var15", "var16"), phenomenonId);
                final CompositePhenomenonEntry phenomenon = new CompositePhenomenonEntry(phenomenonId,
                                                                                   compositeValues.getVariable("var15"),
                                                                                   compositeValues.getVariable("var16"),
                                                                                   null,
                                                                                   components);
                observedProperties.add(new PhenomenonPropertyType(phenomenon));
            }
        }
        for (String phenomenonId : values.getVariables("var11")) {
            if (phenomenonId != null && !phenomenonId.equals("")) {
                final PhenomenonEntry phenomenon = getPhenomenon(phenomenonId);
                observedProperties.add(new PhenomenonPropertyType(phenomenon));
            }
        }

        // feature of interest
        final List<ReferenceEntry> fois = new ArrayList<ReferenceEntry>();
        for (String foiID : values.getVariables("var18")) {
            fois.add(new ReferenceEntry(null, foiID));
        }

        //static part
        final List<String> responseFormat = Arrays.asList(MimeType.APP_XML);
        final List<QName> resultModel     = Arrays.asList(Parameters.OBSERVATION_QNAME);
        final List<ResponseModeType> responseMode = Arrays.asList(ResponseModeType.INLINE, ResponseModeType.RESULT_TEMPLATE);
        return new ObservationOfferingEntry(offeringName,
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
    }

    @Override
    public List<ObservationOfferingEntry> getObservationOfferings() throws CstlServiceException {
        final Values values = loadData(Arrays.asList(VAR01));
        final List<ObservationOfferingEntry> offerings = new ArrayList<ObservationOfferingEntry>();
        final List<String> offeringNames = values.getVariables(VAR01);
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
        final Values values = loadData(Arrays.asList("var13", "var14"), phenomenonName);
        return new PhenomenonEntry(phenomenonName, values.getVariable("var13"), values.getVariable("var14"));
    }

    @Override
    public SamplingFeatureEntry getFeatureOfInterest(String samplingFeatureId) throws CstlServiceException {
        final Values values = loadData(Arrays.asList("var19", "var20", "var21", "var22", "var23", "var24"), samplingFeatureId);
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
            LOGGER.severe("unable to parse the srs dimension: " + dimension);
        }
        final List<Double> coordinates = getCoordinates(samplingFeatureId);
        final DirectPositionType pos = new DirectPositionType(srsName, srsDimension, coordinates);
        final PointType location     = new PointType(pointID, pos);

        return  new SamplingPointEntry(samplingFeatureId, name, description, sampledFeature, location);
    }

    private List<Double> getCoordinates(String samplingFeatureId) throws CstlServiceException {
        final Values values = loadData(Arrays.asList("var25"), samplingFeatureId);
        final List<Double> result = new ArrayList<Double>();
        final List<String> coordinates = values.getVariables("var25");
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
    public ObservationEntry getObservation(String identifier, QName resultModel) throws CstlServiceException {
        final Values values = loadData(Arrays.asList("var26", "var27", "var28", "var29", "var30", "var31"), identifier);
        final SamplingFeatureEntry featureOfInterest = getFeatureOfInterest(values.getVariable("var26"));
        final PhenomenonEntry observedProperty = getPhenomenon(values.getVariable("var27"));
        final ProcessEntry procedure = new ProcessEntry(values.getVariable("var28"));

        final TimePeriodType samplingTime = new TimePeriodType(values.getVariable("var29"), values.getVariable("var30"));
        final AnyResultEntry anyResult = getResult(values.getVariable("var31"), resultModel);
        final DataArrayEntry dataArray = anyResult.getArray();
        final DataArrayPropertyType result = new DataArrayPropertyType(dataArray);
        return new ObservationEntry(identifier,
                                    null,
                                    featureOfInterest,
                                    observedProperty,
                                    procedure,
                                    result,
                                    samplingTime);
    }

    @Override
    public AnyResultEntry getResult(String identifier, QName resutModel) throws CstlServiceException {
        final Values values = loadData(Arrays.asList("var32", "var33", "var34", "var35", "var36", "var37", "var38", "var39",
                "var40", "var41", "var42", "var43"), identifier);
        final int count = Integer.parseInt(values.getVariable("var32"));

        // encoding
        final String encodingID       = values.getVariable("var34");
        final String tokenSeparator   = values.getVariable("var35");
        final String decimalSeparator = values.getVariable("var36");
        final String blockSeparator   = values.getVariable("var37");
        final TextBlockEntry encoding = new TextBlockEntry(encodingID, tokenSeparator, blockSeparator, decimalSeparator);

        //data block description
        final String blockId          = values.getVariable("var38");
        final String dataRecordId     = values.getVariable("var39");
        final Set<AnyScalarPropertyType> fields = new HashSet<AnyScalarPropertyType>();
        final List<String> fieldNames = values.getVariables("var40");
        final List<String> fieldDef   = values.getVariables("var41");
        final List<String> type       = values.getVariables("var42");
        final List<String> uomCodes   = values.getVariables("var43");
        for(int i = 0; i < fieldNames.size(); i++) {
            AbstractDataComponentEntry component = null;
            final String typeName   = type.get(i);
            final String definition = fieldDef.get(i);
            final String uomCode    = uomCodes.get(i);
            if (typeName != null) {
                if (typeName.equals("Quantity")) {
                    component = new QuantityType(definition, uomCode, null);
                } else if (typeName.equals("Time")) {
                    component = new TimeType(definition, uomCode, null);
                } else if (typeName.equals("Boolean")) {
                    component = new BooleanType(definition, null);
                } else {
                    LOGGER.severe("unexpected field type");
                }
            }
            final AnyScalarPropertyType field = new AnyScalarPropertyType(dataRecordId, blockId, component);
            fields.add(field);
        }

        final SimpleDataRecordEntry elementType = new SimpleDataRecordEntry(blockId, dataRecordId, null, false, fields);

        final String dataValues     = values.getVariable("var33");
        final DataArrayEntry result = new DataArrayEntry(blockId, count, elementType, encoding, dataValues);
        return new AnyResultEntry(identifier, result);
    }

    @Override
    public ReferenceEntry getReference(String href) throws CstlServiceException {
        //TODO
        return new ReferenceEntry(null, href);
    }

    public String getInfos() {
        return "Constellation Postgrid Generic O&M Reader 0.4";
    }

    public List<ResponseModeType> getResponseModes() throws CstlServiceException {
        return Arrays.asList(ResponseModeType.INLINE, ResponseModeType.RESULT_TEMPLATE);
    }

    public List<String> getResponseFormats() throws CstlServiceException {
        return Arrays.asList("text/xml; subtype=\"om/1.0.0\"");
    }
}
