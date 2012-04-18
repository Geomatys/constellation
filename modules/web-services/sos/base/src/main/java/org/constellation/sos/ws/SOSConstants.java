/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2009, Geomatys
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

package org.constellation.sos.ws;

import org.opengis.filter.capability.Operator;
import java.util.Arrays;
import javax.xml.namespace.QName;
import net.jcip.annotations.Immutable;
import org.geotoolkit.ogc.xml.v110.ComparisonOperatorType;
import org.geotoolkit.ogc.xml.v110.ComparisonOperatorsType;
import org.geotoolkit.ogc.xml.v110.FunctionNameType;
import org.geotoolkit.ogc.xml.v110.GeometryOperandsType;
import org.geotoolkit.ogc.xml.v110.IdCapabilitiesType;
import org.geotoolkit.ogc.xml.v110.ScalarCapabilitiesType;
import org.geotoolkit.ogc.xml.v110.SpatialCapabilitiesType;
import org.geotoolkit.ogc.xml.v110.SpatialOperatorType;
import org.geotoolkit.ogc.xml.v110.SpatialOperatorsType;
import org.geotoolkit.ogc.xml.v110.TemporalCapabilitiesType;
import org.geotoolkit.ogc.xml.v110.TemporalOperandsType;
import org.geotoolkit.ogc.xml.v110.TemporalOperatorNameType;
import org.geotoolkit.ogc.xml.v110.TemporalOperatorType;
import org.geotoolkit.ogc.xml.v110.TemporalOperatorsType;
import org.geotoolkit.sos.xml.v100.FilterCapabilities;
import org.opengis.filter.capability.SpatialOperator;

import static org.geotoolkit.gml.xml.v311.ObjectFactory.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@Immutable
public final class SOSConstants {

    private SOSConstants() {}

    public static final String SOS = "SOS";
    public static final String VERSION = "1.0.0";
    public static final String ALL = "All";
    public static final String OFFERING = "offering";
    public static final String EVENT_TIME = "eventTime";
    public static final String PROCEDURE = "procedure";
    public static final String OUTPUT_FORMAT = "OUTPUTFORMAT";
    public static final String OBSERVATION_TEMPLATE = "observationTemplate";
    public static final String RESPONSE_MODE = "responseMode";
    public static final String RESPONSE_FORMAT = "responseFormat";
    public static final String NOT_SUPPORTED = "This operation is not take in charge by the Web Service";
    public static final String SENSORML_100_FORMAT = "text/xml;subtype=\"sensorML/1.0.0\"";
    public static final String SENSORML_101_FORMAT = "text/xml;subtype=\"sensorML/1.0.1\"";

    /**
     * The base Qname for complex observation.
     */
    public static final QName OBSERVATION_QNAME = new QName("http://www.opengis.net/om/1.0", "Observation", "om");

    /**
     * The base Qname for measurement observation.
     */
    public static final QName MEASUREMENT_QNAME = new QName("http://www.opengis.net/om/1.0", "Measurement", "om");
    
    public static final FilterCapabilities SOS_FILTER_CAPABILITIES = new FilterCapabilities();
    
    static {
        final GeometryOperandsType geom = new GeometryOperandsType(Arrays.asList(_Envelope_QNAME));
        final SpatialOperator[] spaOps = new SpatialOperator[1];
        spaOps[0] = new SpatialOperatorType("BBOX", null);
        final SpatialOperatorsType spaOp = new SpatialOperatorsType(spaOps);
        final SpatialCapabilitiesType  spatial = new SpatialCapabilitiesType(geom, spaOp);
        SOS_FILTER_CAPABILITIES.setSpatialCapabilities(spatial);
        
        final TemporalCapabilitiesType temporal = new TemporalCapabilitiesType();
        final TemporalOperandsType temp = new TemporalOperandsType();
        temp.getTemporalOperand().add(_TimeInstant_QNAME);
        temp.getTemporalOperand().add(_TimePeriod_QNAME);
        temporal.setTemporalOperands(temp);
        final TemporalOperatorsType tempOp = new TemporalOperatorsType();
        final TemporalOperatorType td = new TemporalOperatorType();
        td.setName(TemporalOperatorNameType.TM_DURING);
        final TemporalOperatorType te = new TemporalOperatorType();
        te.setName(TemporalOperatorNameType.TM_EQUALS);
        final TemporalOperatorType ta = new TemporalOperatorType();
        ta.setName(TemporalOperatorNameType.TM_AFTER);
        final TemporalOperatorType tb = new TemporalOperatorType();
        tb.setName(TemporalOperatorNameType.TM_BEFORE);
        
        tempOp.getTemporalOperator().add(td);
        tempOp.getTemporalOperator().add(te);
        tempOp.getTemporalOperator().add(ta);
        tempOp.getTemporalOperator().add(tb);
        
        temporal.setTemporalOperators(tempOp);
        
        SOS_FILTER_CAPABILITIES.setTemporalCapabilities(temporal);
        
        final Operator[] compOps = new Operator[8];
        compOps[0] = ComparisonOperatorType.BETWEEN;
        compOps[1] = ComparisonOperatorType.EQUAL_TO;
        compOps[2] = ComparisonOperatorType.NOT_EQUAL_TO;
        compOps[3] = ComparisonOperatorType.LESS_THAN;
        compOps[4] = ComparisonOperatorType.LESS_THAN_EQUAL_TO;
        compOps[5] = ComparisonOperatorType.GREATER_THAN;
        compOps[6] = ComparisonOperatorType.GREATER_THAN_EQUAL_TO;
        compOps[7] = ComparisonOperatorType.LIKE;
        final ComparisonOperatorsType compOp = new ComparisonOperatorsType(compOps);
        final ScalarCapabilitiesType scalar = new ScalarCapabilitiesType(compOp, null, false);
        SOS_FILTER_CAPABILITIES.setScalarCapabilities(scalar);
        
        final IdCapabilitiesType id = new IdCapabilitiesType(true, true);
        SOS_FILTER_CAPABILITIES.setIdCapabilities(id);
    }
}

