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

package org.constellation.sos.io.om2;

// J2SE dependencies
import java.util.Map;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;
import javax.xml.namespace.QName;
import java.util.HashMap;

// Constellation dependencies
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.sos.io.ObservationReader;
import org.constellation.ws.CstlServiceException;

import static org.constellation.sos.ws.SOSConstants.*;

// Geotk dependencies
import org.geotoolkit.gml.xml.FeatureProperty;
import org.geotoolkit.observation.xml.OMXmlFactory;
import org.geotoolkit.sos.xml.ResponseModeType;
import org.geotoolkit.sos.xml.ObservationOffering;
import org.geotoolkit.swe.xml.PhenomenonProperty;
import org.geotoolkit.swe.xml.v101.PhenomenonPropertyType;
import org.geotoolkit.swe.xml.AbstractDataComponent;
import org.geotoolkit.swe.xml.AbstractDataRecord;
import org.geotoolkit.swe.xml.AnyScalar;
import org.geotoolkit.swe.xml.TextBlock;
import org.geotoolkit.swe.xml.UomProperty;
import org.geotoolkit.swe.xml.DataArrayProperty;

import static org.geotoolkit.sos.xml.SOSXmlFactory.*;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import org.geotoolkit.util.StringUtilities;

// GeoAPI dependencies
import org.opengis.observation.Observation;
import org.opengis.observation.Phenomenon;
import org.opengis.observation.sampling.SamplingFeature;
import org.opengis.temporal.TemporalGeometricPrimitive;
import org.opengis.temporal.TemporalPrimitive;


/**
 * Default Observation reader for Postgrid O&M database.
 *
 * @author Guilhem Legal
 */
public class OM2ObservationReader extends OM2BaseReader implements ObservationReader {

    protected final DataSource source;
    
    private static final Map<String, List<String>> RESPONSE_FORMAT = new HashMap<>();
    static {
        RESPONSE_FORMAT.put("1.0.0", Arrays.asList(RESPONSE_FORMAT_V100));
        RESPONSE_FORMAT.put("2.0.0", Arrays.asList(RESPONSE_FORMAT_V200));
    }
    
    private final Map<String, List<String>> acceptedSensorMLFormats = new HashMap<>();
    
    /**
     *
     * @param configuration
     * @param properties
     * @throws org.constellation.ws.CstlServiceException
     */
    public OM2ObservationReader(final Automatic configuration, final Map<String, Object> properties) throws CstlServiceException {
        super(properties);
        if (configuration == null) {
            throw new CstlServiceException("The configuration object is null", NO_APPLICABLE_CODE);
        }
        // we get the database informations
        final BDD db = configuration.getBdd();
        if (db == null) {
            throw new CstlServiceException("The configuration file does not contains a BDD object (DefaultObservationReader)", NO_APPLICABLE_CODE);
        }
        isPostgres = db.getClassName() != null && db.getClassName().equals("org.postgresql.Driver");
        try {
            this.source = db.getDataSource();
            // try if the connection is valid
            final Connection c = this.source.getConnection();
            c.close();
        } catch (SQLException ex) {
            throw new CstlServiceException(ex);
        }
        final String smlFormats100 = (String) properties.get("smlFormats100");
        if (smlFormats100 != null) {
            acceptedSensorMLFormats.put("1.0.0", StringUtilities.toStringList(smlFormats100));
        } else {
            acceptedSensorMLFormats.put("1.0.0", Arrays.asList(SENSORML_100_FORMAT_V100,
                                                               SENSORML_101_FORMAT_V100));
        }
        
        final String smlFormats200 = (String) properties.get("smlFormats200");
        if (smlFormats200 != null) {
            acceptedSensorMLFormats.put("2.0.0", StringUtilities.toStringList(smlFormats200));
        } else {
            acceptedSensorMLFormats.put("2.0.0", Arrays.asList(SENSORML_100_FORMAT_V200,
                                                               SENSORML_101_FORMAT_V200));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getOfferingNames(final String version) throws CstlServiceException {
        try {
            final Connection c         = source.getConnection();
            final Statement stmt       = c.createStatement();
            final List<String> results = new ArrayList<>();
            final ResultSet rs         = stmt.executeQuery("SELECT \"identifier\" FROM \"om\".\"offerings\"");
            while (rs.next()) {
                results.add(rs.getString(1));
            }
            rs.close();
            stmt.close();
            c.close();
            return results;
        } catch (SQLException ex) {
            throw new CstlServiceException("Error while retrieving offering names.", ex, NO_APPLICABLE_CODE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ObservationOffering> getObservationOfferings(final List<String> offeringNames, final String version) throws CstlServiceException {
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
    public ObservationOffering getObservationOffering(final String offeringName, final String version) throws CstlServiceException {
        try {
            final Connection c           = source.getConnection();
            final String id;
            final String name;
            final String description;
            final TemporalGeometricPrimitive time;
            final String procedure;
            final List<String> phen200             = new ArrayList<>();
            final List<PhenomenonProperty> phen100 = new ArrayList<>();
            final List<String> foi                 = new ArrayList<>();
            try {
                final PreparedStatement stmt = c.prepareStatement("SELECT * FROM \"om\".\"offerings\" WHERE \"identifier\"=?");
                stmt.setString(1, offeringName);
                final ResultSet rs           = stmt.executeQuery();
                try {
                    if (rs.next()) {
                        id                 = rs.getString(1);
                        description        = rs.getString(2);
                        name               = rs.getString(3);
                        final Timestamp b  = rs.getTimestamp(4);
                        final Timestamp e  = rs.getTimestamp(5);
                        procedure          = rs.getString(6);
                        if (b != null && e == null) {
                            time = buildTimePeriod(version, b, null);
                        } else if (b != null && e != null) {
                            time = buildTimePeriod(version, b, e);
                        } else {
                            time = null;
                        }
                    } else {
                        return null;
                    }
                } finally {
                    rs.close();
                    stmt.close();
                }

                final PreparedStatement stmt2 = c.prepareStatement("SELECT \"phenomenon\" FROM \"om\".\"offering_observed_properties\" WHERE \"id_offering\"=?");
                stmt2.setString(1, offeringName);
                final ResultSet rs2           = stmt2.executeQuery();
                while (rs2.next()) {
                    final String href = rs2.getString(1);
                    phen200.add(href);
                    phen100.add(new PhenomenonPropertyType(href));
                } 
                rs2.close();
                stmt2.close();

                final PreparedStatement stmt3 = c.prepareStatement("SELECT \"foi\" FROM \"om\".\"offering_foi\" WHERE \"id_offering\"=?");
                stmt3.setString(1, offeringName);
                final ResultSet rs3           = stmt3.executeQuery();
                while (rs3.next()) {
                    foi.add(rs3.getString(1));
                } 
                rs3.close();
                stmt3.close();

            } finally {
                c.close();
            }
            final List<String> responseFormat         = RESPONSE_FORMAT.get(version);
            final List<QName> resultModel             = Arrays.asList(OBSERVATION_QNAME, MEASUREMENT_QNAME);
            final List<String> resultModelV200        = Arrays.asList(OBSERVATION_MODEL);
            final List<ResponseModeType> responseMode = Arrays.asList(ResponseModeType.INLINE, ResponseModeType.RESULT_TEMPLATE);
            final List<String> procedureDescription   = acceptedSensorMLFormats.get("2.0.0");
            return buildOffering(version, 
                                 id, 
                                 name, 
                                 description, 
                                 null, 
                                 time, 
                                 Arrays.asList(procedure), 
                                 phen100,
                                 phen200,
                                 foi, 
                                 responseFormat, 
                                 resultModel, 
                                 resultModelV200, 
                                 responseMode,
                                 procedureDescription);
            
        } catch (SQLException e) {
            throw new CstlServiceException("Error while retrieving offering names.", e, NO_APPLICABLE_CODE);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<ObservationOffering> getObservationOfferings(final String version) throws CstlServiceException {
        final List<String> offeringNames    = getOfferingNames(version);
        final List<ObservationOffering> loo = new ArrayList<>();
        for (String offeringName : offeringNames) {
            loo.add(getObservationOffering(offeringName, version));
        }
        return loo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getProcedureNames() throws CstlServiceException {
        try {
            final Connection c         = source.getConnection();
            final Statement stmt       = c.createStatement();
            final List<String> results = new ArrayList<>();
            final ResultSet rs         = stmt.executeQuery("SELECT \"id\" FROM \"om\".\"procedures\"");
            while (rs.next()) {
                results.add(rs.getString(1));
            }
            rs.close();
            stmt.close();
            c.close();
            return results;
        } catch (SQLException ex) {
            throw new CstlServiceException("Error while retrieving procedure names.", ex, NO_APPLICABLE_CODE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getPhenomenonNames() throws CstlServiceException {
        try {
            final Connection c         = source.getConnection();
            final Statement stmt       = c.createStatement();
            final List<String> results = new ArrayList<>();
            final ResultSet rs         = stmt.executeQuery("SELECT \"id\" FROM \"om\".\"observed_properties\"");
            while (rs.next()) {
                results.add(rs.getString(1));
            }
            rs.close();
            stmt.close();
            c.close();
            return results;
        } catch (SQLException ex) {
            throw new CstlServiceException("Error while retrieving phenomenon names.", ex, NO_APPLICABLE_CODE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existPhenomenon(final String phenomenonName) throws CstlServiceException {
        return getPhenomenonNames().contains(phenomenonName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getFeatureOfInterestNames() throws CstlServiceException {
        try {
            final Connection c         = source.getConnection();
            final Statement stmt       = c.createStatement();
            final List<String> results = new ArrayList<>();
            final ResultSet rs         = stmt.executeQuery("SELECT \"id\" FROM \"om\".\"sampling_features\"");
            while (rs.next()) {
                results.add(rs.getString(1));
            }
            rs.close();
            stmt.close();
            c.close();
            return results;
        } catch (SQLException ex) {
            throw new CstlServiceException("Error while retrieving phenomenon names.", ex, NO_APPLICABLE_CODE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SamplingFeature getFeatureOfInterest(final String samplingFeatureName, final String version) throws CstlServiceException {
        try {
            final Connection c = source.getConnection();
            c.setReadOnly(true);
            try {
                return getFeatureOfInterest(samplingFeatureName, version, c);
            } finally {
                c.close();
            }
        } catch (SQLException ex) {
            throw new CstlServiceException(ex.getMessage(), ex, NO_APPLICABLE_CODE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observation getObservation(final String identifier, final QName resultModel, final ResponseModeType mode, final String version) throws CstlServiceException {
        try {
            final Connection c         = source.getConnection();
            c.setReadOnly(true);
            try {
                final String observationID;
                if (identifier.startsWith(observationIdBase)) {
                    observationID = identifier.substring(observationIdBase.length());
                } else if (identifier.startsWith(observationTemplateIdBase)) {
                    final String procedureID     = sensorIdBase + identifier.substring(observationTemplateIdBase.length());
                    final PreparedStatement stmt = c.prepareStatement("SELECT \"id\" FROM \"om\".\"observations\" WHERE \"procedure\"=?");
                    stmt.setString(1, procedureID);
                    final ResultSet rs = stmt.executeQuery();
                    final String oid;
                    if (rs.next()) {
                        oid = rs.getString(1);
                    } else {
                        oid = null;
                    }
                    rs.close();
                    stmt.close();
                    if (oid == null) {
                        return null;
                    }
                    observationID = oid;
                } else {
                    observationID = identifier;
                }

                final String timeID = "time-" + observationID;
                final String observedProperty;
                final String procedure;
                final String foi;
                final TemporalGeometricPrimitive time;
                
                final PreparedStatement stmt  = c.prepareStatement("SELECT * FROM \"om\".\"observations\" WHERE \"identifier\"=?");
                stmt.setString(1, identifier);
                final ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    final String b  = rs.getString(3);
                    final String e  = rs.getString(4);
                    if (b != null && e == null) {
                        time = buildTimeInstant(version, timeID, b.replace(' ', 'T'));
                    } else if (b != null && e != null) {
                        time = buildTimePeriod(version, timeID, b.replace(' ', 'T'), e.replace(' ', 'T'));
                    } else {
                        time = null;
                    }
                    observedProperty = rs.getString(5);
                    procedure        = rs.getString(6);
                    foi              = rs.getString(7);
                    
                } else {
                    return null;
                }
                
                final SamplingFeature feature = getFeatureOfInterest(foi, version, c);
                final FeatureProperty prop    = buildFeatureProperty(version, feature);
                final Phenomenon phen         = getPhenomenon(version, observedProperty, c);
                
                final String name;
                if (ResponseModeType.RESULT_TEMPLATE.equals(mode)) {
                    final String procedureID = procedure.substring(sensorIdBase.length());
                    name = observationTemplateIdBase + procedureID;
                } else {
                    name = identifier;
                }

                if (resultModel.equals(MEASUREMENT_QNAME)) {
                    final Object result = getResult(identifier, resultModel, version); 
                    return OMXmlFactory.buildMeasurement(version, identifier, name, null, prop, phen, procedure, result, time);
                } else {
                    final Object result = getResult(identifier, resultModel, version);
                    return OMXmlFactory.buildObservation(version, identifier, name, null, prop, phen, procedure, result, time);
                }
            } finally {
                c.close();
            }
            
        } catch (SQLException ex) {
            throw new CstlServiceException(ex.getMessage(), ex, NO_APPLICABLE_CODE);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object getResult(final String identifier, final QName resultModel, final String version) throws CstlServiceException {
        try {
            final Connection c = source.getConnection();
            c.setReadOnly(true);
            try {
                return getResult(identifier, resultModel, version, c);
            } finally {
                c.close();
            }
        } catch (SQLException ex) {
            throw new CstlServiceException(ex.getMessage(), ex, NO_APPLICABLE_CODE);
        }
    }
    
    private Object getResult(final String identifier, final QName resultModel, final String version, final Connection c) throws CstlServiceException, SQLException {
        if (resultModel.equals(MEASUREMENT_QNAME)) {
            return buildMeasureResult(identifier, version, c);
        } else {
            return buildComplexResult(identifier, version, c);
        }
    }
    
    private DataArrayProperty buildComplexResult(final String identifier, final String version, final Connection c) throws CstlServiceException, SQLException {
        final List<String> value      = new ArrayList<>();
        final List<String> uom        = new ArrayList<>();
        final List<String> fieldType  = new ArrayList<>();
        final List<Timestamp> time    = new ArrayList<>();
        final List<String> fieldDef   = new ArrayList<>();
        final List<String> fieldNames = new ArrayList<>();
        
        final PreparedStatement stmt  = c.prepareStatement("SELECT \"value\", \"field_type\", \"uom\", \"time\" ,\"field_definition\", \"field_name\" "
                                                         + "FROM \"om\".\"mesures\" m, \"om\".\"observations\" o "
                                                         + "WHERE \"id_observation\" = o.\"id\" "
                                                         + "AND o.\"identifier\"=?"
                                                         + "ORDER BY m.\"id\"");
        
        stmt.setString(1, identifier);
        final ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            value.add(rs.getString(1));
            fieldType.add(rs.getString(2));
            uom.add(rs.getString(3));
            time.add(rs.getTimestamp(4));
            fieldDef.add(rs.getString(5));
            fieldNames.add(rs.getString(6));
        }
        rs.close();
        stmt.close();
        
        final TextBlock encoding = getDefaultTextEncoding(version);
        final String arrayID =  "dataArray-1"; // TODO
        final String recordID = "datarecord-0"; // TODO
        final Map<String, AnyScalar> fields = new HashMap<>();
        fields.put("Time", getDefaultTimeField(version));
        final StringBuilder values = new StringBuilder();
        int nbValue = 0;
        Timestamp oldTime = null;
        int nbFieldWritten = 0;
        int totalField = 0;
        for (int i = 0; i < uom.size(); i++) {
            final String fieldName = fieldNames.get(i);
            if (!fields.containsKey(fieldName)) {
                final AbstractDataComponent compo;
                if ("Quantity".equals(fieldType.get(i))) {
                    final UomProperty uomCode = buildUomProperty(version, uom.get(i), null);
                    compo = buildQuantity(version, fieldDef.get(i), uomCode, null);
                } else if ("Text".equals(fieldType.get(i))) {
                    compo = buildText(version, fieldDef.get(i), null);
                } else {
                    throw new IllegalArgumentException("Unexpected field Type:" + fieldType);
                }
                final AnyScalar scalar = buildAnyScalar(version, null, fieldName, compo);
                fields.put(fieldName, scalar);
                totalField++;
            }
            final Timestamp currentTime = time.get(i);
            if (currentTime.equals(oldTime) && nbFieldWritten != totalField) {
                values.append(encoding.getTokenSeparator()).append(value.get(i));
            } else {
                nbValue++;
                if (oldTime != null) {
                    values.append(encoding.getBlockSeparator());
                }
                values.append(format.format(currentTime)).append(encoding.getTokenSeparator()).append(value.get(i));
                nbFieldWritten = 0;
            }
            nbFieldWritten ++;
            oldTime = currentTime;
        }
        values.append(encoding.getBlockSeparator());
        final AbstractDataRecord record = buildSimpleDatarecord(version, null, recordID, null, false, new ArrayList<>(fields.values()));

        return buildDataArrayProperty(version, arrayID, nbValue, arrayID, record, encoding, values.toString());
    }
    
    private Object buildMeasureResult(final String identifier, final String version, final Connection c) throws CstlServiceException, SQLException {
        final double value;
        final String uom;
        final String name;
        try {
            final PreparedStatement stmt  = c.prepareStatement("SELECT \"id\", \"value\", \"uom\", \"time\" ,\"field_definition\", \"field_name\""
                                                             + "FROM \"om\".\"mesures\" "
                                                             + "WHERE \"id_observation\"=?");
            stmt.setString(1, identifier);
            final ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                name   = "measure-00" + rs.getString(1);
                value  = Double.parseDouble(rs.getString(2));
                uom    = rs.getString(3);
            } else {
                return null;
            }
        } catch (NumberFormatException ex) {
            throw new CstlServiceException("Unable ta parse the result value as a double");
        } finally {
            c.close();
        }
        return buildMeasure(version, name, uom, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existProcedure(final String href) throws CstlServiceException {
        return getProcedureNames().contains(href);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNewObservationId() throws CstlServiceException {
        try {
            final Connection c         = source.getConnection();
            final Statement stmt       = c.createStatement();
            final ResultSet rs         = stmt.executeQuery("SELECT max(\"id\") FROM \"om\".\"observations\"");
            int resultNum;
            if (rs.next()) {
                resultNum = rs.getInt(1) + 1;
            } else {
                resultNum = 1;
            }
            rs.close();
            stmt.close();
            c.close();
            return observationIdBase + resultNum;
        } catch (SQLException ex) {
            throw new CstlServiceException("Error while looking for available observation id.", ex, NO_APPLICABLE_CODE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TemporalPrimitive getFeatureOfInterestTime(final String samplingFeatureName, final String version) throws CstlServiceException {
        try {
            final Connection c           = source.getConnection();
            final PreparedStatement stmt = c.prepareStatement("SELECT max(\"time_begin\"), min(\"time_end\") "
                                                            + "FROM \"om\".\"mesures\""
                                                            + "WHERE \"foi\"=?");
            final ResultSet rs           = stmt.executeQuery();
            final TemporalGeometricPrimitive time;
            if (rs.next()) {
                final Timestamp b  = rs.getTimestamp(1);
                final Timestamp e  = rs.getTimestamp(2);
                if (b != null && e == null) {
                    time = buildTimeInstant(version, b);
                } else if (b != null && e != null) {
                    time = buildTimePeriod(version, b, e);
                } else {
                    time = null;
                }
            } else {
                time = null;
            }
            rs.close();
            stmt.close();
            c.close();
            return time;
        } catch (SQLException ex) {
            throw new CstlServiceException("Error while retrieving phenomenon names.", ex, NO_APPLICABLE_CODE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getEventTime() throws CstlServiceException {
        try {
            final Connection c         = source.getConnection();
            final Statement stmt       = c.createStatement();
            final ResultSet rs         = stmt.executeQuery("SELECT max(\"time_begin\"), min(\"time_end\") FROM \"om\".\"offerings\"");
            final List<String> results = new ArrayList<>();
            if (rs.next()) {
                String s = rs.getString(1);
                if (s != null) {
                    results.add(s);
                }
                s = rs.getString(2);
                if (s != null) {
                    results.add(s);
                }
            } 
            rs.close();
            stmt.close();
            c.close();
            return results;
        } catch (SQLException ex) {
            throw new CstlServiceException("Error while retrieving phenomenon names.", ex, NO_APPLICABLE_CODE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInfos() {
        return "Constellation O&M 2 Reader 0.9";
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
        return Arrays.asList(RESPONSE_FORMAT_V100, RESPONSE_FORMAT_V200);
    }
}
