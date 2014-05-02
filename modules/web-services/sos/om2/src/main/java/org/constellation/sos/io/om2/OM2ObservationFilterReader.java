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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.apache.sis.storage.DataStoreException;
import org.constellation.generic.database.Automatic;
import org.geotoolkit.observation.ObservationFilterReader;

import static org.constellation.sos.ws.SOSConstants.*;
import static org.constellation.sos.ws.SOSUtils.getTimeValue;
import org.geotoolkit.gml.xml.Envelope;
import org.geotoolkit.gml.xml.FeatureProperty;
import org.geotoolkit.observation.ObservationStoreException;
import org.geotoolkit.observation.xml.AbstractObservation;
import org.geotoolkit.observation.xml.OMXmlFactory;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import org.geotoolkit.referencing.CRS;
import static org.geotoolkit.sos.xml.SOSXmlFactory.*;
import org.geotoolkit.swe.xml.AbstractDataComponent;
import org.geotoolkit.swe.xml.AbstractDataRecord;
import org.geotoolkit.swe.xml.AnyScalar;
import org.geotoolkit.swe.xml.DataArrayProperty;
import org.geotoolkit.swe.xml.TextBlock;
import org.geotoolkit.swe.xml.UomProperty;
import org.opengis.observation.Observation;
import org.opengis.observation.Phenomenon;
import org.opengis.observation.sampling.SamplingFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.temporal.Instant;
import org.opengis.temporal.Period;
import org.opengis.temporal.TemporalGeometricPrimitive;
import org.opengis.util.FactoryException;


/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class OM2ObservationFilterReader extends OM2ObservationFilter implements ObservationFilterReader {

    private String responseFormat;
    
    public OM2ObservationFilterReader(final OM2ObservationFilter omFilter) {
        super(omFilter);
    }

    
    public OM2ObservationFilterReader(final Automatic configuration, final Map<String, Object> properties) throws DataStoreException {
        super(configuration, properties);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimeEquals(final Object time) throws DataStoreException {
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
            throw new ObservationStoreException("TM_Equals operation require timeInstant or TimePeriod!",
                    INVALID_PARAMETER_VALUE, EVENT_TIME);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimeBefore(final Object time) throws DataStoreException  {
        // for the operation before the temporal object must be an timeInstant
        if (time instanceof Instant) {
            final Instant ti      = (Instant) time;
            final String position = getTimeValue(ti.getPosition());
            sqlRequest.append("AND (\"time\"<='").append(position).append("')");

        } else {
            throw new ObservationStoreException("TM_Before operation require timeInstant!",
                    INVALID_PARAMETER_VALUE, EVENT_TIME);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimeAfter(final Object time) throws DataStoreException {
        // for the operation after the temporal object must be an timeInstant
        if (time instanceof Instant) {
            final Instant ti      = (Instant) time;
            final String position = getTimeValue(ti.getPosition());
            sqlRequest.append("AND (\"time\">='").append(position).append("')");
        } else {
            throw new ObservationStoreException("TM_After operation require timeInstant!",
                    INVALID_PARAMETER_VALUE, EVENT_TIME);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimeDuring(final Object time) throws DataStoreException {
        if (time instanceof Period) {
            final Period tp    = (Period) time;
            final String begin = getTimeValue(tp.getBeginning().getPosition());
            final String end   = getTimeValue(tp.getEnding().getPosition());
            
            sqlRequest.append("AND (\"time\">='").append(begin).append("' AND \"time\"<= '").append(end).append("')");
        } else {
            throw new ObservationStoreException("TM_During operation require TimePeriod!",
                    INVALID_PARAMETER_VALUE, EVENT_TIME);
        }
    }
    
    @Override
    public List<Observation> getObservationTemplates(final String version) throws DataStoreException {
        if (resultModel.equals(MEASUREMENT_QNAME)) {
            return getMesurementTemplates(version);
        }
        try {
            final Map<String, Observation> observations = new HashMap<>();
            final Connection c                          = source.getConnection();
            c.setReadOnly(true);
            final Statement currentStatement            = c.createStatement();
            final ResultSet rs                          = currentStatement.executeQuery(sqlRequest.toString());
            final TextBlock encoding                    = getDefaultTextEncoding(version);
            final Map<String, AnyScalar> fields         = new HashMap<>();
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
            return new ArrayList<>(observations.values());
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "SQLException while executing the query: {0}", sqlRequest.toString());
            throw new DataStoreException("the service has throw a SQL Exception:" + ex.getMessage());
        } catch (DataStoreException ex) {
            throw new DataStoreException("the service has throw a Datastore Exception:" + ex.getMessage());
        }
    }
    
    public List<Observation> getMesurementTemplates(final String version) throws DataStoreException {
        try {
            final Map<String, Observation> observations = new HashMap<>();
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
            return new ArrayList<>(observations.values());
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "SQLException while executing the query: {0}", sqlRequest.toString());
            throw new DataStoreException("the service has throw a SQL Exception:" + ex.getMessage());
        } catch (DataStoreException ex) {
            throw new DataStoreException("the service has throw a Datastore Exception:" + ex.getMessage());
        }
    }

    @Override
    public List<Observation> getObservations(final String version) throws DataStoreException {
        if (resultModel.equals(MEASUREMENT_QNAME)) {
            return getMesurements(version);
        }
        try {
            // add orderby to the query
            sqlRequest.append(" ORDER BY o.\"id\", m.\"id\"");
            final Map<String, Observation> observations = new HashMap<>();
            final Connection c                          = source.getConnection();
            final Statement currentStatement            = c.createStatement();
            c.setReadOnly(true);
            final ResultSet rs                          = currentStatement.executeQuery(sqlRequest.toString());
            final TextBlock encoding                    = getDefaultTextEncoding(version);
            Timestamp oldTime                           = null;
            StringBuilder values                        = new StringBuilder();
            int nbValue                                 = 0;
            final Map<String, AnyScalar> fields         = new LinkedHashMap<>();
            Observation currentObservation              = null;
            int nbFieldWritten                          = 0;
            int totalField                              = 0;
            
            while (rs.next()) {
                final String procedure        = rs.getString("procedure");
                final String featureID        = rs.getString("foi");
                final Timestamp currentTime   = rs.getTimestamp("time");
                final String value            = rs.getString("value");
                final Observation observation = observations.get(procedure + '-' + featureID);
                
                if (observation == null) {
                    
                    // update Last observation result
                    if (currentObservation != null) {
                        
                        values.append(encoding.getBlockSeparator());
                        final Object result = buildComplexResult(version, fields.values(), nbValue, encoding, values.toString(), observations.size() -1);
                        ((AbstractObservation)currentObservation).setResult(result);
                        ((AbstractObservation)currentObservation).extendSamplingTime(format.format(oldTime));
                        values         = new StringBuilder();
                        nbValue        = 0;
                        totalField     = 0;
                        nbFieldWritten = 0;
                        fields.clear();
                    }
                    
                    final int oid                 = rs.getInt("id");
                    final String obsID            = "obs-"  + oid;
                    final String timeID           = "time-" + oid;
                    final String name             = rs.getString("identifier");
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
                    totalField++;
                        
                    values.append(format.format(currentTime)).append(encoding.getTokenSeparator()).append(value);
                    nbValue++;
                    nbFieldWritten++;
                    
                    currentObservation = OMXmlFactory.buildObservation(version, obsID, name, null, prop, phen, procedure, null, time);
                    observations.put(procedure + '-' + featureID, currentObservation);
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
                        totalField++;
                    }
                    
                    /*
                     *  UPDATE RESULT
                     */
                    if (!currentTime.equals(oldTime) || nbFieldWritten == totalField) {
                        values.append(encoding.getBlockSeparator()).append(format.format(currentTime)).append(encoding.getTokenSeparator()).append(value);
                        nbValue++;
                        nbFieldWritten = 0;
                    } else {
                        values.append(encoding.getTokenSeparator()).append(value);
                    }
                    nbFieldWritten++;
                    
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
            return new ArrayList<>(observations.values());
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "SQLException while executing the query: {0}", sqlRequest.toString());
            throw new DataStoreException("the service has throw a SQL Exception:" + ex.getMessage());
        } catch (DataStoreException ex) {
            throw new DataStoreException("the service has throw a Datastore Exception:" + ex.getMessage());
        }
    }
    
    private AnyScalar buildField(final String version, final String fieldName, final String fieldType, final String uom, final String fieldDef) throws DataStoreException {
        if ("Quantity".equals(fieldType)) {
            final UomProperty uomCode     = buildUomProperty(version, uom, null);
            final AbstractDataComponent c = buildQuantity(version, fieldDef, uomCode, null);
            return buildAnyScalar(version, null, fieldName, c);
        } else if ("Boolean".equals(fieldType)) {
            final AbstractDataComponent c = buildBoolean(version, fieldDef, null);
            return buildAnyScalar(version, null, fieldName, c);
        } else if ("Text".equals(fieldType)) {
            final AbstractDataComponent c = buildText(version, fieldDef, null);
            return buildAnyScalar(version, null, fieldName, c);
        } else {
            throw new DataStoreException("Unsupported field Type:" + fieldType);
        }
    }
    
    private DataArrayProperty buildComplexResult(final String version, final Collection<AnyScalar> fields, final int nbValue, 
            final TextBlock encoding, final String values, final int cpt) {
        final String arrayID     = "dataArray-" + cpt;
        final String recordID    = "datarecord-" + cpt;
        final AbstractDataRecord record = buildSimpleDatarecord(version, null, recordID, null, false, new ArrayList<>(fields));
        return buildDataArrayProperty(version, arrayID, nbValue, arrayID, record, encoding, values);
    }
    
    public List<Observation> getMesurements(final String version) throws DataStoreException {
        try {
            // add orderby to the query
            sqlRequest.append(" ORDER BY o.\"id\", m.\"id\"");
            
            final List<Observation> observations        = new ArrayList<>();
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
                final String name             = rs.getString("identifier");
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
                    throw new DataStoreException("Unable ta parse the result value as a double");
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
            throw new DataStoreException("the service has throw a SQL Exception:" + ex.getMessage());
        } catch (DataStoreException ex) {
            throw new DataStoreException("the service has throw a Datastore Exception:" + ex.getMessage());
        }
    }
    
    private Object buildMeasureResult(final String version, final double value, final String uom, final String resultId) {
        final String name   = "measure-00" + resultId;
        return buildMeasure(version, name, uom, value);
    }
    
    @Override
    public String getResults() throws DataStoreException {
        try {
            // add orderby to the query
            final String fieldRequest = sqlRequest.toString();
            sqlRequest.append(" ORDER BY  o.\"id\", m.\"id\"");
            
            final Connection c                          = source.getConnection();
            c.setReadOnly(true);
            final Statement currentStatement            = c.createStatement();
            LOGGER.info(sqlRequest.toString());
            final ResultSet rs                          = currentStatement.executeQuery(sqlRequest.toString());
            Timestamp oldTime                           = null;
            final StringBuilder values                  = new StringBuilder();
            boolean first                               = true;
            final TextBlock encoding;
            if ("text/csv".equals(responseFormat)) {
                encoding = getCsvTextEncoding("2.0.0");
                // Add the header
                final List<String> fieldNames = getFieldsForGetResult(fieldRequest, c);
                values.append("date,");
                for (String pheno : fieldNames) {
                    values.append(pheno).append(',');
                }
                values.setCharAt(values.length() - 1, '\n');
            } else {
                encoding = getDefaultTextEncoding("2.0.0");
            }

            final List<String> writtenFields = new ArrayList<>();
            while (rs.next()) {
                final Timestamp currentTime   = rs.getTimestamp("time");
                final String value            = rs.getString("value");
                final String fieldName        = rs.getString("field_name");

                boolean newRound = false;
                if (writtenFields.contains(fieldName)) {
                    newRound = true;
                    writtenFields.clear();
                }
                writtenFields.add(fieldName);
                
                if (!currentTime.equals(oldTime) || newRound) {
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
            // empty result 
            if (!first) {
                values.append(encoding.getBlockSeparator());
            }
            rs.close();
            currentStatement.close();
            c.close();
            return values.toString();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "SQLException while executing the query: {0}", sqlRequest.toString());
            throw new DataStoreException("the service has throw a SQL Exception:" + ex.getMessage());
        }
    }
    
    private List<String> getFieldsForGetResult(String request, final Connection c) throws SQLException {
        request = request.replace("SELECT \"time\", \"value\", \"field_name\"", "SELECT DISTINCT \"field_name\"");
        final Statement stmt = c.createStatement();
        final ResultSet rs = stmt.executeQuery(request);
        final List<String> results = new ArrayList<>();
        while (rs.next()) {
            results.add(rs.getString(1));
        }
        rs.close();
        stmt.close();
        return results;
    }
    
    @Override
    public List<SamplingFeature> getFeatureOfInterests(final String version) throws DataStoreException {
        try {
            final List<SamplingFeature> features = new ArrayList<>();
            final Connection c = source.getConnection();
            c.setReadOnly(true);
            final Statement currentStatement = c.createStatement();
            System.out.println(sqlRequest.toString());
            final ResultSet rs = currentStatement.executeQuery(sqlRequest.toString());
            while (rs.next()) {
                final String id   = rs.getString("id");
                final String name = rs.getString("name");
                final String desc = rs.getString("description");
                final String sf   = rs.getString("sampledfeature");
                final int srid    = rs.getInt("crs");
                final byte[] b    = rs.getBytes("shape");
                final CoordinateReferenceSystem crs;
                if (srid != 0) {
                    crs = CRS.decode("urn:ogc:def:crs:EPSG:" + srid);
                } else {
                    crs = defaultCRS;
                }
                final Geometry geom;
                if (b != null) {
                    WKBReader reader = new WKBReader();
                    geom             = reader.read(b);
                } else {
                    geom = null;
                }
                features.add(buildFoi(version, id, name, desc, sf, geom, crs));
            }
            rs.close();
            currentStatement.close();
            c.close();
            return features;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "SQLException while executing the query: {0}", sqlRequest.toString());
            throw new DataStoreException("the service has throw a SQL Exception:" + ex.getMessage(), ex);
        }catch (FactoryException ex) {
            LOGGER.log(Level.SEVERE, "FactoryException while executing the query: {0}", sqlRequest.toString());
            throw new DataStoreException("the service has throw a Factory Exception:" + ex.getMessage(), ex);
        }catch (ParseException ex) {
            LOGGER.log(Level.SEVERE, "ParseException while executing the query: {0}", sqlRequest.toString());
            throw new DataStoreException("the service has throw a Parse Exception:" + ex.getMessage(), ex);
        }
    }

    @Override
    public String getOutOfBandResults() throws DataStoreException {
        throw new ObservationStoreException("Out of band response mode has not been implemented yet", NO_APPLICABLE_CODE, RESPONSE_MODE);
    }

    @Override
    public void setResponseFormat(final String responseFormat) {
        this.responseFormat = responseFormat;
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
