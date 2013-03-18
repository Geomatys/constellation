/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
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

package org.constellation.sos.io.om2;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.constellation.generic.database.Automatic;

import org.constellation.sos.io.ObservationFilterReader;
import org.constellation.ws.CstlServiceException;

import static org.constellation.sos.ws.SOSConstants.*;
import static org.constellation.sos.ws.Utils.getTimeValue;

import org.geotoolkit.gml.xml.Envelope;
import org.geotoolkit.gml.xml.FeatureProperty;
import org.geotoolkit.observation.xml.AbstractObservation;
import org.geotoolkit.observation.xml.OMXmlFactory;
import org.geotoolkit.swe.xml.AbstractDataComponent;
import org.geotoolkit.swe.xml.AbstractDataRecord;
import org.geotoolkit.swe.xml.AnyScalar;
import org.geotoolkit.swe.xml.DataArrayProperty;
import org.geotoolkit.swe.xml.TextBlock;
import org.geotoolkit.swe.xml.UomProperty;

import static org.geotoolkit.sos.xml.SOSXmlFactory.*;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

import org.opengis.observation.Observation;
import org.opengis.observation.Phenomenon;
import org.opengis.observation.sampling.SamplingFeature;
import org.opengis.temporal.Instant;
import org.opengis.temporal.Period;
import org.opengis.temporal.TemporalGeometricPrimitive;


/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class OM2ObservationFilterReader extends OM2ObservationFilter implements ObservationFilterReader {

    public OM2ObservationFilterReader(final OM2ObservationFilter omFilter) {
        super(omFilter);
    }

    
    public OM2ObservationFilterReader(final Automatic configuration, final Map<String, Object> properties) throws CstlServiceException {
        super(configuration, properties);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimeEquals(final Object time) throws CstlServiceException {
        if (time instanceof Period) {
            final Period tp    = (Period) time;
            final String begin = getTimeValue(tp.getBeginning().getPosition());
            final String end   = getTimeValue(tp.getEnding().getPosition());

            // we request directly a multiple observation or a period observation (one measure during a period)
            sqlRequest.append("AND (");
            sqlRequest.append(" \"time_begin\"='").append(begin).append("' AND ");
            sqlRequest.append(" \"time_end\"='").append(end).append("') ");

        // if the temporal object is a timeInstant
        } else if (time instanceof Instant) {
            final Instant ti      = (Instant) time;
            final String position = getTimeValue(ti.getPosition());
            sqlRequest.append("AND (\"time\"='").append(position).append("') ");

        } else {
            throw new CstlServiceException("TM_Equals operation require timeInstant or TimePeriod!",
                    INVALID_PARAMETER_VALUE, EVENT_TIME);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimeBefore(final Object time) throws CstlServiceException  {
        // for the operation before the temporal object must be an timeInstant
        if (time instanceof Instant) {
            final Instant ti      = (Instant) time;
            final String position = getTimeValue(ti.getPosition());
            sqlRequest.append("AND (\"time\"<='").append(position).append("')");

        } else {
            throw new CstlServiceException("TM_Before operation require timeInstant!",
                    INVALID_PARAMETER_VALUE, EVENT_TIME);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimeAfter(final Object time) throws CstlServiceException {
        // for the operation after the temporal object must be an timeInstant
        if (time instanceof Instant) {
            final Instant ti      = (Instant) time;
            final String position = getTimeValue(ti.getPosition());
            sqlRequest.append("AND (\"time\">='").append(position).append("')");
        } else {
            throw new CstlServiceException("TM_After operation require timeInstant!",
                    INVALID_PARAMETER_VALUE, EVENT_TIME);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimeDuring(final Object time) throws CstlServiceException {
        if (time instanceof Period) {
            final Period tp    = (Period) time;
            final String begin = getTimeValue(tp.getBeginning().getPosition());
            final String end   = getTimeValue(tp.getEnding().getPosition());
            
            sqlRequest.append("AND (\"time\">='").append(begin).append("' AND \"time\"<= '").append(end).append("')");
        } else {
            throw new CstlServiceException("TM_During operation require TimePeriod!",
                    INVALID_PARAMETER_VALUE, EVENT_TIME);
        }
    }
    
    @Override
    public List<Observation> getObservationTemplates(final String version) throws CstlServiceException {
        if (resultModel.equals(MEASUREMENT_QNAME)) {
            return getMesurementTemplates(version);
        }
        try {
            final Map<String, Observation> observations = new HashMap<String, Observation>();
            final Connection c                          = source.getConnection();
            c.setReadOnly(true);
            final Statement currentStatement            = c.createStatement();
            final ResultSet rs                          = currentStatement.executeQuery(sqlRequest.toString());
            final TextBlock encoding                    = getDefaultTextEncoding(version);
            final Map<String, AnyScalar> fields         = new HashMap<String, AnyScalar>();
            Observation currentObservation              = null;
            
            while (rs.next()) {
                final String procedure        = rs.getString("procedure");
                final Observation observation = observations.get(procedure);
                
                if (observation == null) {
                    
                    // update Last observation result
                    if (currentObservation != null) {
                        final Object result = buildComplexResult(version, fields.values(), 0, encoding, null, observations.size() -1);
                        ((AbstractObservation)currentObservation).setResult(result);
                        fields.clear();
                    }
                    
                    final String procedureID      = procedure.substring(sensorIdBase.length());
                    final String obsID           = "obs-" + procedureID;
                    final String name             = observationTemplateIdBase + procedureID;
                    final String featureID        = rs.getString("foi");
                    final String observedProperty = rs.getString("observed_property");
                    final SamplingFeature feature = getFeatureOfInterest(featureID, version, c);
                    final FeatureProperty prop    = buildFeatureProperty(version, feature); 
                    final Phenomenon phen         = getPhenomenon(version, observedProperty, c);
                    
                    /*
                     *  BUILD RESULT
                     */
                    final String uom        = rs.getString("uom");
                    final String fieldType  = rs.getString("field_type");
                    final String fieldDef   = rs.getString("field_definition");
                    final String fieldName  = rs.getString("field_name");
                    
                    fields.put("Time", getDefaultTimeField(version));
                    
                    final AnyScalar scalar = buildField(version, fieldName, fieldType, uom, fieldDef);
                    fields.put(fieldName, scalar);
                        
                    currentObservation = OMXmlFactory.buildObservation(version, obsID, name, null, prop, phen, procedure, null, null);
                    observations.put(procedure, currentObservation);
                } else {
                    
                    /*
                     * UPDATE FIELDS 
                     */
                    final String fieldName  = rs.getString("field_name");
                    if (!fields.containsKey(fieldName)) {
                        final String uom        = rs.getString("uom");
                        final String fieldType  = rs.getString("field_type");
                        final String fieldDef   = rs.getString("field_definition");
                        final AnyScalar scalar  = buildField(version, fieldName, fieldType, uom, fieldDef);
                        fields.put(fieldName, scalar);
                    }
                }
            }
            
            // update Last observation result
            if (currentObservation != null) {
                final Object result = buildComplexResult(version, fields.values(), 0, encoding, null, observations.size());
                ((AbstractObservation)currentObservation).setResult(result);
            }
            
            
            rs.close();
            currentStatement.close();
            c.close();
            return new ArrayList<Observation>(observations.values());
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "SQLException while executing the query: {0}", sqlRequest.toString());
            throw new CstlServiceException("the service has throw a SQL Exception:" + ex.getMessage(),
                                          NO_APPLICABLE_CODE);
        }
    }
    
    public List<Observation> getMesurementTemplates(final String version) throws CstlServiceException {
        try {
            final Map<String, Observation> observations = new HashMap<String, Observation>();
            final Connection c                          = source.getConnection();
            c.setReadOnly(true);
            final Statement currentStatement            = c.createStatement();
            final ResultSet rs                          = currentStatement.executeQuery(sqlRequest.toString());
            
            while (rs.next()) {
                final String procedure        = rs.getString("procedure");
                final Observation observation = observations.get(procedure);
                
                if (observation == null) {
                    
                    final String procedureID      = procedure.substring(sensorIdBase.length());
                    final String obsID            = "obs-" + procedureID;
                    final String name             = observationTemplateIdBase + procedureID;
                    final String featureID        = rs.getString("foi");
                    final String observedProperty = rs.getString("observed_property");
                    final SamplingFeature feature = getFeatureOfInterest(featureID, version, c);
                    final FeatureProperty prop    = buildFeatureProperty(version, feature); 
                    final Phenomenon phen         = getPhenomenon(version, observedProperty, c);
                    
                    /*
                     *  BUILD RESULT
                     */
                    final String uom        = rs.getString("uom");
                    
                    final Object result = buildMeasureResult(version, 0, uom, "1");
                    observations.put(procedure, OMXmlFactory.buildMeasurement(version, obsID, name, null, prop, phen, procedure, result, null));
                } else {
                    
                   LOGGER.log(Level.WARNING, "multiple fields on Mesurement for :{0}", procedure);
                }
            }
            
            rs.close();
            currentStatement.close();
            c.close();
            return new ArrayList<Observation>(observations.values());
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "SQLException while executing the query: {0}", sqlRequest.toString());
            throw new CstlServiceException("the service has throw a SQL Exception:" + ex.getMessage(),
                                          NO_APPLICABLE_CODE);
        }
    }

    @Override
    public List<Observation> getObservations(final String version) throws CstlServiceException {
        if (resultModel.equals(MEASUREMENT_QNAME)) {
            return getMesurements(version);
        }
        try {
            final Map<String, Observation> observations = new HashMap<String, Observation>();
            final Connection c                          = source.getConnection();
            final Statement currentStatement            = c.createStatement();
            c.setReadOnly(true);
            final ResultSet rs                          = currentStatement.executeQuery(sqlRequest.toString());
            final TextBlock encoding                    = getDefaultTextEncoding(version);
            Timestamp oldTime                           = null;
            StringBuilder values                        = new StringBuilder();
            int nbValue                                 = 0;
            final Map<String, AnyScalar> fields         = new HashMap<String, AnyScalar>();
            Observation currentObservation              = null;
            
            while (rs.next()) {
                final String procedure        = rs.getString("procedure");
                final Timestamp currentTime   = rs.getTimestamp("time");
                final String value            = rs.getString("value");
                final Observation observation = observations.get(procedure);
                
                if (observation == null) {
                    
                    // update Last observation result
                    if (currentObservation != null) {
                        
                        values.append(encoding.getBlockSeparator());
                        final Object result = buildComplexResult(version, fields.values(), nbValue, encoding, values.toString(), observations.size() -1);
                        ((AbstractObservation)currentObservation).setResult(result);
                        ((AbstractObservation)currentObservation).extendSamplingTime(format.format(oldTime));
                        values  = new StringBuilder();
                        nbValue = 0;
                        fields.clear();
                    }
                    
                    final int oid                 = rs.getInt("id");
                    final String obsID            = "obs-"  + oid;
                    final String timeID           = "time-" + oid;
                    final String name             = observationIdBase + oid;
                    final String featureID        = rs.getString("foi");
                    final String observedProperty = rs.getString("observed_property");
                    final SamplingFeature feature = getFeatureOfInterest(featureID, version, c);
                    final FeatureProperty prop    = buildFeatureProperty(version, feature); 
                    final Phenomenon phen         = getPhenomenon(version, observedProperty, c);
                    final TemporalGeometricPrimitive time = buildTimeInstant(version, timeID, format2.format(currentTime));
                    
                    /*
                     *  BUILD RESULT
                     */
                    final String uom        = rs.getString("uom");
                    final String fieldType  = rs.getString("field_type");
                    final String fieldDef   = rs.getString("field_definition");
                    final String fieldName  = rs.getString("field_name");
                    
                    fields.put("Time", getDefaultTimeField(version));
                    
                    final AnyScalar scalar = buildField(version, fieldName, fieldType, uom, fieldDef);
                    fields.put(fieldName, scalar);
                        
                    values.append(format.format(currentTime)).append(encoding.getTokenSeparator()).append(value);
                    nbValue++;
                    
                    currentObservation = OMXmlFactory.buildObservation(version, obsID, name, null, prop, phen, procedure, null, time);
                    observations.put(procedure, currentObservation);
                } else {
                    
                    /*
                     * UPDATE FIELDS 
                     */
                    final String fieldName  = rs.getString("field_name");
                    if (!fields.containsKey(fieldName)) {
                        final String uom        = rs.getString("uom");
                        final String fieldType  = rs.getString("field_type");
                        final String fieldDef   = rs.getString("field_definition");
                        final AnyScalar scalar  = buildField(version, fieldName, fieldType, uom, fieldDef);
                        fields.put(fieldName, scalar);
                    }
                    
                    /*
                     *  UPDATE RESULT
                     */
                    if (!currentTime.equals(oldTime)) {
                        values.append(encoding.getBlockSeparator()).append(format.format(currentTime)).append(encoding.getTokenSeparator()).append(value);
                        nbValue++;
                    } else {
                        values.append(encoding.getTokenSeparator()).append(value);
                    }
                    
                }
            
                oldTime = currentTime;
            }
            
            // update Last observation result
            if (currentObservation != null) {
                values.append(encoding.getBlockSeparator());
                final Object result = buildComplexResult(version, fields.values(), nbValue, encoding, values.toString(), observations.size());
                ((AbstractObservation)currentObservation).setResult(result);
                ((AbstractObservation)currentObservation).extendSamplingTime(format2.format(oldTime));
            }
            
            
            rs.close();
            currentStatement.close();
            c.close();
            return new ArrayList<Observation>(observations.values());
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "SQLException while executing the query: {0}", sqlRequest.toString());
            throw new CstlServiceException("the service has throw a SQL Exception:" + ex.getMessage(),
                                          NO_APPLICABLE_CODE);
        }
    }
    
    private AnyScalar buildField(final String version, final String fieldName, final String fieldType, final String uom, final String fieldDef) {
        if ("Quantity".equals(fieldType)) {
            final UomProperty uomCode     = buildUomProperty(version, uom, null);
            final AbstractDataComponent c = buildQuantity(version, fieldDef, uomCode, null);
            return buildAnyScalar(version, null, fieldName, c);
        } else {
            throw new IllegalArgumentException("Unexpected field Type:" + fieldType);
        }
    }
    
    private DataArrayProperty buildComplexResult(final String version, final Collection<AnyScalar> fields, final int nbValue, 
            final TextBlock encoding, final String values, final int cpt) {
        final String arrayID     = "dataArray-" + cpt;
        final String recordID    = "datarecord-" + cpt;
        final AbstractDataRecord record = buildSimpleDatarecord(version, null, recordID, null, false, new ArrayList<AnyScalar>(fields));
        return buildDataArrayProperty(version, arrayID, nbValue, arrayID, record, encoding, values);
    }
    
    public List<Observation> getMesurements(final String version) throws CstlServiceException {
        try {
            final List<Observation> observations        = new ArrayList<Observation>();
            final Connection c                          = source.getConnection();
            c.setReadOnly(true);
            final Statement currentStatement            = c.createStatement();
            System.out.println(sqlRequest.toString());
            final ResultSet rs                          = currentStatement.executeQuery(sqlRequest.toString());
            while (rs.next()) {
                final String procedure        = rs.getString("procedure");
                final Timestamp currentTime   = rs.getTimestamp("time");
                final String value            = rs.getString("value");
                final int oid                 = rs.getInt("id");
                final String rid              = rs.getString("resultid");
                final String name             = observationIdBase + oid;
                final String obsID            = "obs-"  + oid;
                final String timeID           = "time-" + oid;
                final String featureID        = rs.getString("foi");
                final String observedProperty = rs.getString("observed_property");
                final SamplingFeature feature = getFeatureOfInterest(featureID, version, c);
                final FeatureProperty prop    = buildFeatureProperty(version, feature); 
                final Phenomenon phen         = getPhenomenon(version, observedProperty, c);
                final TemporalGeometricPrimitive time = buildTimeInstant(version, timeID, format2.format(currentTime));

                /*
                 *  BUILD RESULT
                 */
                final String uom        = rs.getString("uom");
                final Double dValue;
                try {
                    dValue = Double.parseDouble(value);
                } catch (NumberFormatException ex) {
                    throw new CstlServiceException("Unable ta parse the result value as a double");
                }
                final Object result = buildMeasureResult(version, dValue, uom, rid);
                observations.add(OMXmlFactory.buildMeasurement(version, obsID, name, null, prop, phen, procedure, result, time));

            }
            rs.close();
            currentStatement.close();
            c.close();
            return observations;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "SQLException while executing the query: {0}", sqlRequest.toString());
            throw new CstlServiceException("the service has throw a SQL Exception:" + ex.getMessage(),
                                          NO_APPLICABLE_CODE);
        }
    }
    
    private Object buildMeasureResult(final String version, final double value, final String uom, final String resultId) throws CstlServiceException, SQLException {
        final String name   = "measure-00" + resultId;
        return buildMeasure(version, name, uom, value);
    }
    
    @Override
    public String getResults() throws CstlServiceException {
        try {
            final Connection c                          = source.getConnection();
            c.setReadOnly(true);
            final Statement currentStatement            = c.createStatement();
            System.out.println(sqlRequest.toString());
            final ResultSet rs                          = currentStatement.executeQuery(sqlRequest.toString());
            final TextBlock encoding                    = getDefaultTextEncoding("2.0.0");
            Timestamp oldTime                           = null;
            final StringBuilder values                  = new StringBuilder();
            boolean first                               = true;
            while (rs.next()) {
                final Timestamp currentTime   = rs.getTimestamp("time");
                final String value            = rs.getString("value");
                
                if (!currentTime.equals(oldTime)) {
                    if (!first) {
                        values.append(encoding.getBlockSeparator());
                    }
                    values.append(format.format(currentTime)).append(encoding.getTokenSeparator()).append(value);
                } else {
                    values.append(encoding.getTokenSeparator()).append(value);
                }                    
                first = false;
                oldTime = currentTime;
            }
            values.append(encoding.getBlockSeparator());
            rs.close();
            currentStatement.close();
            c.close();
            return  values.toString();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "SQLException while executing the query: {0}", sqlRequest.toString());
            throw new CstlServiceException("the service has throw a SQL Exception:" + ex.getMessage(),
                                          NO_APPLICABLE_CODE);
        }
    }

    @Override
    public String getOutOfBandResults() throws CstlServiceException {
        throw new CstlServiceException("Out of band response mode has not been implemented yet", NO_APPLICABLE_CODE, RESPONSE_MODE);
    }

    @Override
    public void setResponseFormat(final String responseFormat) {
        // TODO
    }

    @Override
    public boolean computeCollectionBound() {
        return false;
    }

    @Override
    public Envelope getCollectionBoundingShape() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
