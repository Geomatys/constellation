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
import java.util.logging.Logger;
import javax.sql.DataSource;
import javax.xml.namespace.QName;

// JTS dependencies
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.logging.Level;

// Constellation dependencies
import org.constellation.sos.factory.OMFactory;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.sos.io.ObservationReader;
import org.constellation.ws.CstlServiceException;

import static org.constellation.sos.ws.SOSConstants.*;

// Geotk dependencies
import org.geotoolkit.gml.JTStoGeometry;
import org.geotoolkit.gml.xml.Envelope;
import org.geotoolkit.gml.xml.FeatureProperty;
import org.geotoolkit.util.logging.Logging;
import org.geotoolkit.observation.xml.OMXmlFactory;
import org.geotoolkit.sos.xml.ResponseModeType;
import org.geotoolkit.sos.xml.ObservationOffering;
import org.geotoolkit.swe.xml.PhenomenonProperty;
import org.geotoolkit.swe.xml.v101.PhenomenonPropertyType;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.swe.xml.AbstractDataComponent;
import org.geotoolkit.swe.xml.AbstractDataRecord;
import org.geotoolkit.swe.xml.AnyScalar;
import org.geotoolkit.swe.xml.TextBlock;
import org.geotoolkit.swe.xml.UomProperty;

import static org.geotoolkit.sos.xml.SOSXmlFactory.*;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import org.geotoolkit.swe.xml.DataArrayProperty;

// GeoAPI dependencies
import org.opengis.observation.Observation;
import org.opengis.observation.Phenomenon;
import org.opengis.observation.sampling.SamplingFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.temporal.TemporalGeometricPrimitive;
import org.opengis.temporal.TemporalPrimitive;
import org.opengis.util.FactoryException;


/**
 * Default Observation reader for Postgrid O&M database.
 *
 * @author Guilhem Legal
 */
public class OM2ObservationReader implements ObservationReader {

    /**
     * use for debugging purpose
     */
    protected static final Logger LOGGER = Logging.getLogger("org.constellation.sos");

    /**
     * The base for observation id.
     */
    protected final String observationIdBase;
    
    protected final String observationTemplateIdBase;

    protected final String phenomenonIdBase;
    
    protected final String sensorIdBase;
    
    protected final DataSource source;
    
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    
    private static final CoordinateReferenceSystem defaultCRS;
    static {
        CoordinateReferenceSystem candidate = null;
        try {
            candidate = CRS.decode("EPSG:4326");
        } catch (FactoryException ex) {
            LOGGER.log(Level.SEVERE, "Error while retrieving default CRS 4326", ex);
        }
        defaultCRS = candidate;
    }
    
    /**
     *
     * @param dataSourceOM
     * @param observationIdBase
     */
    public OM2ObservationReader(final Automatic configuration, final Map<String, Object> properties) throws CstlServiceException {
        this.observationIdBase         = (String) properties.get(OMFactory.OBSERVATION_ID_BASE);
        this.observationTemplateIdBase = (String) properties.get(OMFactory.OBSERVATION_TEMPLATE_ID_BASE);
        this.phenomenonIdBase          = (String) properties.get(OMFactory.PHENOMENON_ID_BASE);
        this.sensorIdBase              = (String) properties.get(OMFactory.SENSOR_ID_BASE);
        if (configuration == null) {
            throw new CstlServiceException("The configuration object is null", NO_APPLICABLE_CODE);
        }
        // we get the database informations
        final BDD db = configuration.getBdd();
        if (db == null) {
            throw new CstlServiceException("The configuration file does not contains a BDD object (DefaultObservationReader)", NO_APPLICABLE_CODE);
        }
        try {
            this.source = db.getDataSource();
            // try if the connection is valid
            final Connection c = this.source.getConnection();
            c.close();
        } catch (SQLException ex) {
            throw new CstlServiceException(ex);
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
            final List<String> results = new ArrayList<String>();
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
        final List<ObservationOffering> offerings = new ArrayList<ObservationOffering>();
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
            final List<String> phen200             = new ArrayList<String>();
            final List<PhenomenonProperty> phen100 = new ArrayList<PhenomenonProperty>();
            final List<String> foi                 = new ArrayList<String>();
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
                            time = buildTimeInstant(version, b);
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
            final List<String> responseFormat         = Arrays.asList(RESPONSE_FORMAT_V100, RESPONSE_FORMAT_V200);
            final List<QName> resultModel             = Arrays.asList(OBSERVATION_QNAME, MEASUREMENT_QNAME);
            final List<String> resultModelV200        = Arrays.asList(OBSERVATION_MODEL);
            final List<ResponseModeType> responseMode = Arrays.asList(ResponseModeType.INLINE, ResponseModeType.RESULT_TEMPLATE);
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
                                 responseMode);
            
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
        final List<ObservationOffering> loo = new ArrayList<ObservationOffering>();
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
            final List<String> results = new ArrayList<String>();
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
            final List<String> results = new ArrayList<String>();
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
            final List<String> results = new ArrayList<String>();
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
    public SamplingFeature getFeatureOfInterest(final String id, final String version) throws CstlServiceException {
        try {
            final Connection c = source.getConnection();
            c.setReadOnly(true);
            try {
                return getFeatureOfInterest(id, version, c);
            } finally {
                c.close();
            }
        } catch (SQLException ex) {
            throw new CstlServiceException(ex.getMessage(), ex, NO_APPLICABLE_CODE);
        }
    }
    
    private SamplingFeature getFeatureOfInterest(final String id, final String version, final Connection c) throws SQLException, CstlServiceException {
        try {
            final String name;
            final String description;
            final String sampledFeature;
            final byte[] b;
            final int srid;

            final PreparedStatement stmt  = c.prepareStatement("SELECT * FROM \"om\".\"sampling_features\" WHERE \"id\"=?");
            stmt.setString(1, id);
            final ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                name           = rs.getString(2);
                description    = rs.getString(3);
                sampledFeature = rs.getString(4);
                b              = rs.getBytes(5);
                srid           = rs.getInt(6);
            } else {
                return null;
            }
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

            final String gmlVersion = getGMLVersion(version);
            final FeatureProperty prop;
            if (sampledFeature != null) {
                prop = buildFeatureProperty(version, sampledFeature);
            } else {
                prop = null;
            }
            if (geom instanceof Point) {
                final org.geotoolkit.gml.xml.Point point = JTStoGeometry.toGML(gmlVersion, (Point)geom, crs);
                // little hack fo unit test
                point.setSrsName(null);
                return buildSamplingPoint(version, id, name, description, prop, point);
            } else if (geom instanceof LineString) {
                final org.geotoolkit.gml.xml.LineString line = JTStoGeometry.toGML(gmlVersion, (LineString)geom, crs);
                line.emptySrsNameOnChild();
                final Envelope bound = line.getBounds();
                return buildSamplingCurve(version, id, name, description, prop, line, null, null, bound);
            } else if (geom != null) {
                return buildSamplingFeature(version, id, name, description, prop);   
            } else {
                throw new IllegalArgumentException("Unexpected geometry type:" + geom.getClass());
            }
            
        } catch (ParseException ex) {
            throw new CstlServiceException(ex.getMessage(), ex, NO_APPLICABLE_CODE);
        } catch (FactoryException ex) {
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
                final String idToParse;
                if (identifier.startsWith(observationIdBase)) {
                    idToParse = identifier.substring(observationIdBase.length());
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
                    idToParse = oid;
                } else {
                    idToParse = identifier;
                }
                final int id;
                try {
                    id = Integer.parseInt(idToParse);
                } catch (NumberFormatException ex) {
                    throw new CstlServiceException("observation id can't be parsed as an integer:" + idToParse);
                }
                final String observedProperty;
                final String procedure;
                final String foi;
                final TemporalGeometricPrimitive time;
                
                final PreparedStatement stmt  = c.prepareStatement("SELECT * FROM \"om\".\"observations\" WHERE \"id\"=?");
                stmt.setInt(1, id);
                final ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    final String b  = rs.getString(2);
                    final String e  = rs.getString(3);
                    if (b != null && e == null) {
                        time = buildTimeInstant(version, b.replace(' ', 'T'));
                    } else if (b != null && e != null) {
                        time = buildTimePeriod(version, b.replace(' ', 'T'), e.replace(' ', 'T'));
                    } else {
                        time = null;
                    }
                    observedProperty = rs.getString(4);
                    procedure        = rs.getString(5);
                    foi              = rs.getString(6);
                    
                } else {
                    return null;
                }
                
                final SamplingFeature feature = getFeatureOfInterest(foi, version, c);
                final FeatureProperty prop    = buildFeatureProperty(version, feature);
                final String phenID;
                if (observedProperty.startsWith(phenomenonIdBase)) {
                    phenID = observedProperty.substring(phenomenonIdBase.length());
                } else {
                    phenID = null;
                }
                final Phenomenon phen  = getPhenomenon(version, phenID, observedProperty, c);
                
                final String name;
                if (identifier.startsWith(observationIdBase)) {
                    name = identifier;
                } else if (ResponseModeType.RESULT_TEMPLATE.equals(mode)) {
                    final String procedureID = procedure.substring(sensorIdBase.length());
                    name = observationTemplateIdBase + procedureID;
                } else {
                    name = observationIdBase + idToParse;
                }

                if (resultModel.equals(MEASUREMENT_QNAME)) {
                    final Object result = getResult(identifier, resultModel, version); 
                    return OMXmlFactory.buildMeasurement(version, name, null, prop, phen, procedure, result, time);
                } else {
                    final Object result = getResult(idToParse, resultModel, version);
                    return OMXmlFactory.buildObservation(version, name, null, prop, phen, procedure, result, time);
                }
            } finally {
                c.close();
            }
            
        } catch (SQLException ex) {
            throw new CstlServiceException(ex.getMessage(), ex, NO_APPLICABLE_CODE);
        }
    }
    
    private Phenomenon getPhenomenon(final String version, final String id, final String observedProperty, final Connection c) throws CstlServiceException {
        try {
            if (version.equals("2.0.0")) {
                return buildPhenomenon(version, id, observedProperty);
            } else {
                // look for composite phenomenon
                final PreparedStatement stmt = c.prepareStatement("SELECT \"component\" FROM \"om\".\"components\" WHERE \"phenomenon\"=?");
                stmt.setString(1, observedProperty);
                final ResultSet rs = stmt.executeQuery();
                final List<Phenomenon> phenomenons = new ArrayList<Phenomenon>();
                while (rs.next()) {
                    final String name = rs.getString(1);
                    final String phenID;
                    if (name.startsWith(phenomenonIdBase)) {
                        phenID = name.substring(phenomenonIdBase.length());
                    } else {
                        phenID = null;
                    }
                    phenomenons.add(buildPhenomenon(version, phenID, name));
                }
                rs.close();
                stmt.close();
                if (phenomenons.isEmpty()) {
                    return buildPhenomenon(version, id, observedProperty);
                } else {
                    return buildCompositePhenomenon(version, id, observedProperty, phenomenons);
                }
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
        final List<Double> value      = new ArrayList<Double>();
        final List<String> uom        = new ArrayList<String>();
        final List<String> fieldType  = new ArrayList<String>();
        final List<Timestamp> time    = new ArrayList<Timestamp>();
        final List<String> fieldDef   = new ArrayList<String>();
        final List<String> fieldNames = new ArrayList<String>();
        
        final PreparedStatement stmt  = c.prepareStatement("SELECT \"value\", \"field_type\", \"uom\", \"time\" ,\"field_definition\", \"field_name\""
                                                         + "FROM \"om\".\"mesures\" "
                                                         + "WHERE \"id_observation\"=?");
        final int id;
        try {
            id = Integer.parseInt(identifier);
        } catch (NumberFormatException ex) {
            throw new CstlServiceException("Unable to parse result ID:" + identifier);
        }
        stmt.setInt(1, id);
        final ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            value.add(rs.getDouble(1));
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
        final Map<String, AnyScalar> fields = new HashMap<String, AnyScalar>();
        fields.put("Time", getDefaultTimeField(version));
        final StringBuilder values = new StringBuilder();
        int nbValue = 0;
        Timestamp oldTime = null;
        for (int i = 0; i < uom.size(); i++) {
            final String fieldName = fieldNames.get(i);
            if (!fields.containsKey(fieldName)) {
                final AbstractDataComponent compo;
                if ("Quantity".equals(fieldType.get(i))) {
                    final UomProperty uomCode = buildUomProperty(version, uom.get(i), null);
                    compo = buildQuantity(version, fieldDef.get(i), uomCode, null);
                } else {
                    throw new IllegalArgumentException("Unexpected field Type:" + fieldType);
                }
                final AnyScalar scalar = buildAnyScalar(version, null, fieldName, compo);
                fields.put(fieldName, scalar);
            }
            final Timestamp currentTime = time.get(i);
            if (currentTime.equals(oldTime)) {
                values.append(encoding.getTokenSeparator()).append(value.get(i));
            } else {
                nbValue++;
                if (oldTime != null) {
                    values.append(encoding.getBlockSeparator());
                }
                values.append(format.format(currentTime)).append(encoding.getTokenSeparator()).append(value.get(i));
            }
            oldTime = currentTime;
        }
        values.append(encoding.getBlockSeparator());
        final AbstractDataRecord record = buildSimpleDatarecord(version, null, recordID, null, false, new ArrayList<AnyScalar>(fields.values()));

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
                value  = rs.getDouble(2);
                uom    = rs.getString(3);
            } else {
                return null;
            }
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
            final List<String> results = new ArrayList<String>();
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
