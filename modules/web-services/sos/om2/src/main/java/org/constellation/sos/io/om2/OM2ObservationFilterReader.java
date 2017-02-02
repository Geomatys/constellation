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

package org.constellation.sos.io.om2;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.apache.sis.storage.DataStoreException;
import org.constellation.generic.database.Automatic;
import static org.constellation.sos.ws.SOSConstants.EVENT_TIME;
import static org.constellation.sos.ws.SOSConstants.MEASUREMENT_QNAME;
import static org.constellation.sos.ws.SOSConstants.RESPONSE_MODE;
import static org.constellation.sos.ws.SOSUtils.getTimeValue;
import org.geotoolkit.geometry.jts.SRIDGenerator;
import org.geotoolkit.gml.xml.Envelope;
import org.geotoolkit.gml.xml.FeatureProperty;
import org.geotoolkit.observation.ObservationFilterReader;
import org.geotoolkit.observation.ObservationStoreException;
import org.geotoolkit.observation.xml.AbstractObservation;
import org.geotoolkit.observation.xml.OMXmlFactory;
import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_PARAMETER_VALUE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.NO_APPLICABLE_CODE;
import org.geotoolkit.referencing.CRS;

import static org.geotoolkit.sos.xml.SOSXmlFactory.*;
import org.geotoolkit.swe.xml.AbstractDataRecord;
import org.geotoolkit.swe.xml.AnyScalar;
import org.geotoolkit.swe.xml.DataArray;
import org.geotoolkit.swe.xml.DataArrayProperty;
import org.geotoolkit.swe.xml.TextBlock;
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

    
    public OM2ObservationFilterReader(final Automatic configuration, final String schemaPrefix, final Map<String, Object> properties) throws DataStoreException {
        super(configuration, schemaPrefix, properties);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setTimeEquals(final Object time) throws DataStoreException {
        if (time instanceof Period) {
            final Period tp    = (Period) time;
            final String begin = getTimeValue(tp.getBeginning().getDate());
            final String end   = getTimeValue(tp.getEnding().getDate());

            // we request directly a multiple observation or a period observation (one measure during a period)
            sqlRequest.append("AND (");
            sqlRequest.append(" \"time_begin\"='").append(begin).append("' AND ");
            sqlRequest.append(" \"time_end\"='").append(end).append("') ");

        // if the temporal object is a timeInstant
        } else if (time instanceof Instant) {
            final Instant ti      = (Instant) time;
            final String position = getTimeValue(ti.getDate());
            //sqlRequest.append("AND (\"time_begin\"='").append(position).append("' AND \"time_end\"='").append(position).append("') ");
            sqlMeasureRequest.append("AND (\"$time\"='").append(position).append("') ");

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
            final String position = getTimeValue(ti.getDate());
            sqlRequest.append("AND (\"time_begin\"<='").append(position).append("')");
            sqlMeasureRequest.append("AND (\"$time\"<='").append(position).append("')");

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
            final String position = getTimeValue(ti.getDate());
            sqlRequest.append("AND (\"time_end\">='").append(position).append("')");
            sqlMeasureRequest.append("AND (\"$time\">='").append(position).append("')");
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
            final String begin = getTimeValue(tp.getBeginning().getDate());
            final String end   = getTimeValue(tp.getEnding().getDate());
            sqlRequest.append("AND (");

            // the multiple observations included in the period
            sqlRequest.append(" (\"time_begin\">='").append(begin).append("' AND \"time_end\"<='").append(end).append("')");
            sqlRequest.append("OR");
            // the single observations included in the period
            sqlRequest.append(" (\"time_begin\">='").append(begin).append("' AND \"time_begin\">='").append(end).append("' AND \"time_end\" IS NULL)");
            sqlRequest.append("OR");
            // the multiple observations which overlaps the first bound
            sqlRequest.append(" (\"time_begin\"<='").append(begin).append("' AND \"time_end\"<='").append(end).append("' AND \"time_end\">='").append(begin).append("')");
            sqlRequest.append("OR");
            // the multiple observations which overlaps the second bound
            sqlRequest.append(" (\"time_begin\">='").append(begin).append("' AND \"time_end\">='").append(end).append("' AND \"time_begin\"<='").append(end).append("')");
            sqlRequest.append("OR");
            // the multiple observations which overlaps the whole period
            sqlRequest.append(" (\"time_begin\"<='").append(begin).append("' AND \"time_end\">='").append(end).append("'))");
            
            sqlMeasureRequest.append("AND (\"$time\">='").append(begin).append("' AND \"$time\"<= '").append(end).append("')");
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
        try(final Connection c = source.getConnection()) {
            final Map<String, Observation> observations = new HashMap<>();
            try(final Statement currentStatement = c.createStatement();
                final ResultSet rs               = currentStatement.executeQuery(sqlRequest.toString())) {
                final TextBlock encoding = getDefaultTextEncoding(version);

                while (rs.next()) {
                    final String procedure = rs.getString("procedure");
                    Observation observation = observations.get(procedure);

                    if (observation == null) {

                        final String procedureID = procedure.substring(sensorIdBase.length());
                        final String obsID = "obs-" + procedureID;
                        final String name = observationTemplateIdBase + procedureID;
                        final String featureID = rs.getString("foi");
                        final String observedProperty = rs.getString("observed_property");
                        final SamplingFeature feature = getFeatureOfInterest(featureID, version, c);
                        final FeatureProperty prop = buildFeatureProperty(version, feature);
                        final Phenomenon phen = getPhenomenon(version, observedProperty, c);
                        List<Field> fields = readFields(procedure, c);
                        /*
                         *  BUILD RESULT
                         */
                        final List<AnyScalar> scal = new ArrayList<>();
                        for (Field f : fields) {
                            scal.add(f.getScalar(version));
                        }
                        final Object result = buildComplexResult(version, scal, 0, encoding, null, observations.size() - 1);
                        observation = OMXmlFactory.buildObservation(version, obsID, name, null, prop, phen, procedure, result, null);
                        observations.put(procedure, observation);
                    }
                }
            }
            return new ArrayList<>(observations.values());
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "SQLException while executing the query: {0}", sqlRequest.toString());
            throw new DataStoreException("the service has throw a SQL Exception:" + ex.getMessage(), ex);
        } catch (DataStoreException ex) {
            throw new DataStoreException("the service has throw a Datastore Exception:" + ex.getMessage(), ex);
        }
    }
    
    public List<Observation> getMesurementTemplates(final String version) throws DataStoreException {
        try(final Connection c = source.getConnection()) {
            final Map<String, Observation> observations = new HashMap<>();

            try(final Statement currentStatement = c.createStatement();
                final ResultSet rs               = currentStatement.executeQuery(sqlRequest.toString())) {

                while (rs.next()) {
                    final String procedure = rs.getString("procedure");
                    final Observation observation = observations.get(procedure);

                    if (observation == null) {
                        final String procedureID = procedure.substring(sensorIdBase.length());
                        final String obsID = "obs-" + procedureID;
                        final String name = observationTemplateIdBase + procedureID;
                        final String featureID = rs.getString("foi");
                        final String observedProperty = rs.getString("observed_property");
                        final SamplingFeature feature = getFeatureOfInterest(featureID, version, c);
                        final FeatureProperty prop = buildFeatureProperty(version, feature);
                        final Phenomenon phen = getPhenomenon(version, observedProperty, c);
                    
                        /*
                         *  BUILD RESULT
                         */
                        final List<Field> fields = readFields(procedure, c);

                        final Object result = buildMeasureResult(version, 0, fields.get(1).fieldUom, "1");
                        observations.put(procedure, OMXmlFactory.buildMeasurement(version, obsID, name, null, prop, phen, procedure, result, null));
                    } else {
                        LOGGER.log(Level.WARNING, "multiple fields on Mesurement for :{0}", procedure);
                    }
                }
            }
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
        try(final Connection c               = source.getConnection();
            final Statement currentStatement = c.createStatement()) {
            try(final ResultSet rs           = currentStatement.executeQuery(sqlRequest.toString())) {
                // add orderby to the query
                sqlRequest.append(" ORDER BY o.\"id\"");
                final Map<String, Observation> observations = new HashMap<>();
                final TextBlock encoding = getDefaultTextEncoding(version);
                final Map<String, List<Field>> fieldMap = new LinkedHashMap<>();

                while (rs.next()) {
                    int nbValue = 0;
                    StringBuilder values = new StringBuilder();
                    final String procedure = rs.getString("procedure");
                    final String featureID = rs.getString("foi");
                    final int oid = rs.getInt("id");
                    Observation observation = observations.get(procedure + '-' + featureID);
                    final int pid = getPIDFromProcedure(procedure, c);
                    List<Field> fields = fieldMap.get(procedure);
                    if (fields == null) {
                        fields = readFields(procedure, c);
                        fieldMap.put(procedure, fields);
                    }

                    if (observation == null) {
                        final String obsID = "obs-" + oid;
                        final String timeID = "time-" + oid;
                        final String name = rs.getString("identifier");
                        final String observedProperty = rs.getString("observed_property");
                        final SamplingFeature feature = getFeatureOfInterest(featureID, version, c);
                        final FeatureProperty prop = buildFeatureProperty(version, feature);
                        final Phenomenon phen = getPhenomenon(version, observedProperty, c);
                        String firstTime = formatTime(rs.getString("time_begin"));
                        String lastTime = formatTime(rs.getString("time_end"));
                        boolean first = true;
                        final List<AnyScalar> scal = new ArrayList<>();
                        for (Field f : fields) {
                            scal.add(f.getScalar(version));
                        }
                    
                        /*
                         *  BUILD RESULT
                         */
                        final Field timeField = getTimeField(procedure);
                        final String sqlRequest;
                        if (timeField != null) {
                            sqlRequest = "SELECT * FROM \"" + schemaPrefix + "mesures\".\"mesure" + pid + "\" m "
                                    + "WHERE \"id_observation\" = ? " + sqlMeasureRequest.toString().replace("$time", timeField.fieldName)
                                    + "ORDER BY m.\"id\"";
                        } else {
                            sqlRequest = "SELECT * FROM \"" + schemaPrefix + "mesures\".\"mesure" + pid + "\" m WHERE \"id_observation\" = ? ORDER BY m.\"id\"";
                        }

                        try(final PreparedStatement stmt = c.prepareStatement(sqlRequest)) {
                            stmt.setInt(1, oid);
                            try(final ResultSet rs2 = stmt.executeQuery()) {
                                while (rs2.next()) {
                                    for (int i = 0; i < fields.size(); i++) {
                                        String value = rs2.getString(i + 3);
                                        Field field = fields.get(i);
                                        // for time TODO remove when field will be typed
                                        if (field.fieldType.equals("Time")) {
                                            value = value.replace(' ', 'T');
                                            if (first) {
                                                firstTime = value;
                                                first = false;
                                            }
                                            lastTime = value;
                                            value = value.substring(0, value.length() - 2);
                                        }
                                        values.append(value).append(encoding.getTokenSeparator());
                                    }
                                    values.deleteCharAt(values.length() - 1);
                                    values.append(encoding.getBlockSeparator());
                                    nbValue++;
                                }
                            }
                        }

                        final TemporalGeometricPrimitive time = buildTimePeriod(version, timeID, firstTime, lastTime);
                        final Object result = buildComplexResult(version, scal, nbValue, encoding, values.toString(), observations.size() - 1);
                        observation = OMXmlFactory.buildObservation(version, obsID, name, null, prop, phen, procedure, result, time);
                        observations.put(procedure + '-' + featureID, observation);
                    } else {
                        String lastTime = null;
                        final Field timeField = getTimeField(procedure);
                        final String sqlRequest;
                        if (timeField != null) {
                            sqlRequest = "SELECT * FROM \"" + schemaPrefix + "mesures\".\"mesure" + pid + "\" m "
                                    + "WHERE \"id_observation\" = ? " + sqlMeasureRequest.toString().replace("$time", timeField.fieldName)
                                    + "ORDER BY m.\"id\"";
                        } else {
                            sqlRequest = "SELECT * FROM \"" + schemaPrefix + "mesures\".\"mesure" + pid + "\" m WHERE \"id_observation\" = ? ORDER BY m.\"id\"";
                        }

                        try(final PreparedStatement stmt = c.prepareStatement(sqlRequest)) {
                            stmt.setInt(1, oid);
                            try(final ResultSet rs2 = stmt.executeQuery()) {
                                while (rs2.next()) {
                                    for (int i = 0; i < fields.size(); i++) {
                                        String value = rs2.getString(i + 3);
                                        Field field = fields.get(i);
                                        // for time TODO remove when field will be typed
                                        if (field.fieldType.equals("Time")) {
                                            value = value.replace(' ', 'T');
                                            value = value.substring(0, value.length() - 2);
                                            lastTime = value;
                                        }
                                        values.append(value).append(encoding.getTokenSeparator());
                                    }
                                    values.deleteCharAt(values.length() - 1);
                                    values.append(encoding.getBlockSeparator());
                                    nbValue++;
                                }
                            }
                        }

                        // UPDATE RESULTS
                        final DataArrayProperty result = (DataArrayProperty) (observation).getResult();
                        final DataArray array = result.getDataArray();
                        array.setElementCount(array.getElementCount().getCount().getValue() + nbValue);
                        array.setValues(array.getValues() + values.toString());
                        ((AbstractObservation) observation).extendSamplingTime(lastTime);
                    }
                }
                return new ArrayList<>(observations.values());
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "SQLException while executing the query: {0}", sqlRequest.toString());
            throw new DataStoreException("the service has throw a SQL Exception:" + ex.getMessage(), ex);
        } catch (DataStoreException ex) {
            throw new DataStoreException("the service has throw a Datastore Exception:" + ex.getMessage(), ex);
        }
    }
    
    private String formatTime(String s) {
        if (s != null) {
            s = s.replace(' ', 'T'); 
        }
        return s;
    }
    
    private DataArrayProperty buildComplexResult(final String version, final Collection<AnyScalar> fields, final int nbValue, 
            final TextBlock encoding, final String values, final int cpt) {
        final String arrayID     = "dataArray-" + cpt;
        final String recordID    = "datarecord-" + cpt;
        final AbstractDataRecord record = buildSimpleDatarecord(version, null, recordID, null, false, new ArrayList<>(fields));
        return buildDataArrayProperty(version, arrayID, nbValue, arrayID, record, encoding, values);
    }
    
    public List<Observation> getMesurements(final String version) throws DataStoreException {
        // add orderby to the query
        sqlRequest.append(" ORDER BY o.\"id\"");

        try(final Connection c = source.getConnection()) {
            final List<Observation> observations        = new ArrayList<>();

            try(final Statement currentStatement = c.createStatement();
                final ResultSet rs               = currentStatement.executeQuery(sqlRequest.toString())) {
                while (rs.next()) {
                    final String procedure = rs.getString("procedure");
                    final Timestamp startTime = rs.getTimestamp("time_begin");
                    final Timestamp endTime = rs.getTimestamp("time_end");
                    final int oid = rs.getInt("id");
                    final String name = rs.getString("identifier");
                    final String obsID = "obs-" + oid;
                    final String timeID = "time-" + oid;
                    final String featureID = rs.getString("foi");
                    final String observedProperty = rs.getString("observed_property");
                    final SamplingFeature feature = getFeatureOfInterest(featureID, version, c);
                    final FeatureProperty prop = buildFeatureProperty(version, feature);
                    final Phenomenon phen = getPhenomenon(version, observedProperty, c);
                    final int pid = getPIDFromProcedure(procedure, c);
                    final List<Field> fields = readFields(procedure, c);
                    final String uom = fields.get(0).fieldUom;
                    String start = null;
                    if (startTime != null) {
                        start = format2.format(startTime);
                    }
                    String end = null;
                    if (endTime != null) {
                        end = format2.format(endTime);
                    }
                    TemporalGeometricPrimitive time = null;
                    if (start != null || end != null) {
                        time = buildTimePeriod(version, timeID, start, end);
                    }

                    /*
                     *  BUILD RESULT
                     */
                    final Field timeField = getTimeField(procedure);
                    final String sqlRequest;
                    if (timeField != null) {
                        sqlRequest = "SELECT * FROM \"" + schemaPrefix + "mesures\".\"mesure" + pid + "\" m "
                                + "WHERE \"id_observation\" = ? " + sqlMeasureRequest.toString().replace("$time", timeField.fieldName)
                                + "ORDER BY m.\"id\"";
                    } else {
                        sqlRequest = "SELECT * FROM \"" + schemaPrefix + "mesures\".\"mesure" + pid + "\" m WHERE \"id_observation\" = ? ORDER BY m.\"id\"";
                    }

                    try(final PreparedStatement stmt = c.prepareStatement(sqlRequest)) {
                        stmt.setInt(1, oid);
                        try(final ResultSet rs2 = stmt.executeQuery()) {
                            if (rs2.next()) {
                                Double dValue = null;
                                String rid = null;
                                rid = rs2.getString("id");
                                final String value = rs2.getString(3);
                                try {
                                    dValue = Double.parseDouble(value);
                                } catch (NumberFormatException ex) {
                                    throw new DataStoreException("Unable ta parse the result value as a double");
                                }
                                final Object result = buildMeasureResult(version, dValue, uom, rid);
                                observations.add(OMXmlFactory.buildMeasurement(version, obsID, name, null, prop, phen, procedure, result, time));
                            }
                        }
                    }
                }
            }
            return observations;
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "SQLException while executing the query: {0}", sqlRequest.toString());
            throw new DataStoreException("the service has throw a SQL Exception:" + ex.getMessage(), ex);
        } catch (DataStoreException ex) {
            throw new DataStoreException("the service has throw a Datastore Exception:" + ex.getMessage(), ex);
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
            final Field timeField = getTimeField(currentProcedure);
            if (timeField != null) {
                sqlRequest.append(sqlMeasureRequest.toString().replace("$time", timeField.fieldName));
            }
            sqlRequest.append(" ORDER BY  o.\"id\", m.\"id\"");

            final StringBuilder values = new StringBuilder();
            try(final Connection c = source.getConnection()) {
                try(final Statement currentStatement = c.createStatement()) {
                    LOGGER.info(sqlRequest.toString());
                    final ResultSet rs = currentStatement.executeQuery(sqlRequest.toString());
                    final List<Field> fields;
                    if (!currentFields.isEmpty()) {
                        fields = new ArrayList<>();
                        // we add the main field TODO profiles???
                        if (timeField != null) {
                            fields.add(timeField);
                        }
                        for (String f : currentFields) {
                            final Field field = getFieldForPhenomenon(currentProcedure, f, c);
                            if (!fields.contains(field)) {
                                fields.add(field);
                            }
                        }
                    } else {
                        fields = readFields(currentProcedure, c);
                    }
                    final TextBlock encoding;
                    if ("text/csv".equals(responseFormat)) {
                        encoding = getCsvTextEncoding("2.0.0");
                        // Add the header
                        for (Field pheno : fields) {
                            values.append(pheno.fieldName).append(',');
                        }
                        values.setCharAt(values.length() - 1, '\n');
                    } else {
                        encoding = getDefaultTextEncoding("2.0.0");
                    }

                    while (rs.next()) {
                        for (int i = 0; i < fields.size(); i++) {
                            Field field = fields.get(i);
                            String value = rs.getString(field.fieldName);
                            // for time TODO remove when field will be typed
                            if (value != null && field.fieldType.equals("Time")) {
                                value = value.replace(' ', 'T');
                                value = value.substring(0, value.length() - 2);
                            } else if (value == null) {
                                value = "";
                            }
                            values.append(value).append(encoding.getTokenSeparator());
                        }
                        values.deleteCharAt(values.length() - 1);
                        values.append(encoding.getBlockSeparator());
                    }
                }
            }
            return values.toString();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "SQLException while executing the query: {0}", sqlRequest.toString());
            throw new DataStoreException("the service has throw a SQL Exception:" + ex.getMessage(), ex);
        }
    }
    
    @Override
    public String getDecimatedResults(final int width) throws DataStoreException {
        try {
            // add orderby to the query
            final String fieldRequest = sqlRequest.toString();
            sqlRequest.append(" ORDER BY  o.\"id\", m.\"id\"");
            final StringBuilder values = new StringBuilder();
            try(final Connection c = source.getConnection()) {
                try (final Statement currentStatement = c.createStatement()) {
                    LOGGER.info(sqlRequest.toString());
                    try (final ResultSet rs = currentStatement.executeQuery(sqlRequest.toString())) {
                        final TextBlock encoding;
                        final List<Field> fields;
                        if (!currentFields.isEmpty()) {
                            fields = new ArrayList<>();
                            // we add the main field TODO profiles???
                            final Field timeField = getTimeField(currentProcedure, c);
                            if (timeField != null) {
                                fields.add(timeField);
                            }
                            for (String f : currentFields) {
                                final Field field = getFieldForPhenomenon(currentProcedure, f, c);
                                if (field != null && !fields.contains(field)) {
                                    fields.add(field);
                                }
                            }
                        } else {
                            fields = readFields(currentProcedure, c);
                        }
                        if ("text/csv".equals(responseFormat)) {
                            encoding = getCsvTextEncoding("2.0.0");
                            // Add the header
                            for (Field pheno : fields) {
                                // hack for the current graph in cstl you only work when the main field is named "time"
                                if ("Time".equals(pheno.fieldType)) {
                                    values.append("time").append(',');
                                } else {
                                    values.append(pheno.fieldName).append(',');
                                }
                            }
                            values.setCharAt(values.length() - 1, '\n');
                        } else {
                            encoding = getDefaultTextEncoding("2.0.0");
                        }
                        Map<String, Double> minVal = initMapVal(fields, false);
                        Map<String, Double> maxVal = initMapVal(fields, true);
                        final long[] times = getMainFieldStepForGetResult(fieldRequest, fields.get(0), c, width);
                        final long step = times[1];
                        long start = times[0];

                        while (rs.next()) {

                            long currentMainValue = -1;
                            for (int i = 0; i < fields.size(); i++) {
                                Field field = fields.get(i);
                                String value = rs.getString(field.fieldName);

                                if (i == 0) {
                                    if (field.fieldType.equals("Time")) {
                                        final Timestamp currentTime = Timestamp.valueOf(value);
                                        currentMainValue = currentTime.getTime();
                                    } else if (field.fieldType.equals("Quantity")) {
                                        if (value != null && !value.isEmpty()) {
                                            final Double d = Double.parseDouble(value);
                                            currentMainValue = d.longValue();
                                        }
                                    }
                                }
                                addToMapVal(minVal, maxVal, field.fieldName, value);
                            }


                            if (currentMainValue != -1 && currentMainValue > (start + step)) {
                                //min
                                if (fields.get(0).fieldType.equals("Time")) {
                                    values.append(format.format(new Date(start)));
                                } else if (fields.get(0).fieldType.equals("Quantity")) {
                                    values.append(start);
                                } else {
                                    throw new DataStoreException("main field other than Time or Quantity are not yet allowed");
                                }
                                for (Field field : fields) {
                                    if (!field.equals(fields.get(0))) {
                                        values.append(encoding.getTokenSeparator());
                                        final double minValue = minVal.get(field.fieldName);
                                        if (minValue != Double.MAX_VALUE) {
                                            values.append(minValue);
                                        }
                                    }
                                }
                                values.append(encoding.getBlockSeparator());
                                //max
                                if (fields.get(0).fieldType.equals("Time")) {
                                    long maxTime = start + step;
                                    values.append(format.format(new Date(maxTime)));
                                } else if (fields.get(0).fieldType.equals("Quantity")) {
                                    values.append(start + step);
                                } else {
                                    throw new DataStoreException("main field other than Time or Quantity are not yet allowed");
                                }
                                for (Field field : fields) {
                                    if (!field.equals(fields.get(0))) {
                                        values.append(encoding.getTokenSeparator());
                                        final double maxValue = maxVal.get(field.fieldName);
                                        if (maxValue != -Double.MAX_VALUE) {
                                            values.append(maxValue);
                                        }
                                    }
                                }
                                values.append(encoding.getBlockSeparator());
                                start = currentMainValue;
                                minVal = initMapVal(fields, false);
                                maxVal = initMapVal(fields, true);
                            }
                        }
                    }
                }
            }
            return values.toString();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "SQLException while executing the query: {0}", sqlRequest.toString());
            throw new DataStoreException("the service has throw a SQL Exception:" + ex.getMessage(), ex);
        }
    }
    
    private Map<String, Double> initMapVal(final List<Field> fields, final boolean max) {
        final Map<String, Double> result = new HashMap<>();
        final double value;
        if (max) {
            value = -Double.MAX_VALUE;
        } else {
            value = Double.MAX_VALUE;
        }
        for (Field field : fields) {
            result.put(field.fieldName, value);
        }
        return result;
    }
    
    private void addToMapVal(final Map<String, Double> minMap, final Map<String, Double> maxMap, final String field, final String value) {
        if (value == null || value.isEmpty()) return;
        
        final Double minPrevious = minMap.get(field);
        final Double maxPrevious = maxMap.get(field);
        try {
            final Double current = Double.parseDouble(value);
            if (current > maxPrevious) {
                maxMap.put(field, current);
            }
            if (current < minPrevious) {
                minMap.put(field, current);
            }
        } catch (NumberFormatException ex) {
            LOGGER.log(Level.FINER, "unable to parse value:{0}", value);
        }
    }
    
    private long[] getMainFieldStepForGetResult(String request, final Field mainField, final Connection c, final int width) throws SQLException {
        request = request.replace("SELECT m.*", "SELECT MIN(\"" + mainField.fieldName + "\"), MAX(\"" + mainField.fieldName + "\") ");
        try(final Statement stmt = c.createStatement();
            final ResultSet rs = stmt.executeQuery(request)) {
            final long[] result = {-1L, -1L};
            if (rs.next()) {
                if (mainField.fieldType.equals("Time")) {
                    final Timestamp minT = rs.getTimestamp(1);
                    final Timestamp maxT = rs.getTimestamp(2);
                    if (minT != null && maxT != null) {
                        final long min = minT.getTime();
                        final long max = maxT.getTime();
                        result[0] = min;
                        result[1] = (max - min) / width;
                    }
                } else if (mainField.fieldType.equals("Quantity")) {
                    final Double minT = rs.getDouble(1);
                    final Double maxT = rs.getDouble(2);
                    if (minT != null && maxT != null) {
                        final long min = minT.longValue();
                        final long max = maxT.longValue();
                        result[0] = min;
                        result[1] = (max - min) / width;
                    }
                } else {
                    throw new SQLException("unable to extract bound from a " + mainField.fieldType + " main field.");
                }
            }
            return result;
        }
    }
    
    @Override
    public List<SamplingFeature> getFeatureOfInterests(final String version) throws DataStoreException {
        try(final Connection c = source.getConnection()) {
            final List<SamplingFeature> features = new ArrayList<>();
            try(final Statement currentStatement = c.createStatement();
                final ResultSet rs = currentStatement.executeQuery(sqlRequest.toString())) {
                while (rs.next()) {
                    final String id = rs.getString("id");
                    final String name = rs.getString("name");
                    final String desc = rs.getString("description");
                    final String sf = rs.getString("sampledfeature");
                    final int srid = rs.getInt("crs");
                    final byte[] b = rs.getBytes("shape");
                    final CoordinateReferenceSystem crs;
                    if (srid != 0) {
                        crs = CRS.decode(SRIDGenerator.toSRS(srid, SRIDGenerator.Version.V1));
                    } else {
                        crs = defaultCRS;
                    }
                    final Geometry geom;
                    if (b != null) {
                        WKBReader reader = new WKBReader();
                        geom = reader.read(b);
                    } else {
                        geom = null;
                    }
                    features.add(buildFoi(version, id, name, desc, sf, geom, crs));
                }
            }
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
