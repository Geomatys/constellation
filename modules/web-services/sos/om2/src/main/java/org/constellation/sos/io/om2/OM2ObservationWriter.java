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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.WKBWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.apache.sis.util.logging.Logging;

// Constellation dependencies
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.sos.factory.OMFactory;
import org.constellation.sos.io.ObservationWriter;
import org.constellation.ws.CstlServiceException;

// Geotk dependencies
import org.geotoolkit.gml.GeometrytoJTS;
import org.geotoolkit.gml.xml.AbstractGeometry;
import org.geotoolkit.gml.xml.DirectPosition;
import org.geotoolkit.observation.xml.AbstractObservation;
import org.geotoolkit.sampling.xml.SamplingFeature;
import org.geotoolkit.sos.xml.ObservationOffering;
import org.geotoolkit.sos.xml.SOSXmlFactory;
import org.geotoolkit.swe.xml.AnyScalar;
import org.geotoolkit.swe.xml.DataArray;
import org.geotoolkit.swe.xml.DataArrayProperty;
import org.geotoolkit.swe.xml.DataComponentProperty;
import org.geotoolkit.swe.xml.DataRecord;
import org.geotoolkit.swe.xml.CompositePhenomenon;
import org.geotoolkit.swe.xml.PhenomenonProperty;
import org.geotoolkit.swe.xml.Quantity;
import org.geotoolkit.swe.xml.AbstractText;
import org.geotoolkit.swe.xml.AbstractBoolean;
import org.geotoolkit.swe.xml.SimpleDataRecord;
import org.geotoolkit.swe.xml.TextBlock;
import org.geotoolkit.swes.xml.ObservationTemplate;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import org.geotoolkit.temporal.object.ISODateParser;

// GeoAPI dependencies
import org.opengis.observation.Measure;
import org.opengis.temporal.Instant;
import org.opengis.temporal.Period;
import org.opengis.temporal.TemporalObject;
import org.opengis.util.FactoryException;



/**
 * Default Observation reader for Postgis O&M2 database.
 *
 * @author Guilhem Legal (Geomatys)
 */
public class OM2ObservationWriter implements ObservationWriter {

    /**
     * use for debugging purpose
     */
    protected static final Logger LOGGER = Logging.getLogger("org.constellation.sos");

    /**
     * A flag indicating if the dataSource is a postgreSQL SGBD
     */
    private final boolean isPostgres;

    protected final DataSource source;
    
    protected final String observationIdBase;
    
    protected final String sensorIdBase;

    /**
     * Build a new Observation writer for postgrid dataSource.
     *
     * @param configuration
     * @param properties
     *
     * @throws org.constellation.ws.CstlServiceException
     */
    public OM2ObservationWriter(final Automatic configuration, final Map<String, Object> properties) throws CstlServiceException {
        final String oidBase = (String) properties.get(OMFactory.OBSERVATION_ID_BASE);
        if (oidBase == null) {
            this.observationIdBase = "";
        } else {
            this.observationIdBase = oidBase;
        }
        final String sidBase = (String) properties.get(OMFactory.SENSOR_ID_BASE);
        if (sidBase == null) {
            this.sensorIdBase = "";
        } else {
            this.sensorIdBase = sidBase;
        }
        if (configuration == null) {
            throw new CstlServiceException("The configuration object is null", NO_APPLICABLE_CODE);
        }
        // we get the database informations
        final BDD db = configuration.getBdd();
        if (db == null) {
            throw new CstlServiceException("The configuration file does not contains a BDD object", NO_APPLICABLE_CODE);
        }
        isPostgres = db.getClassName() != null && db.getClassName().equals("org.postgresql.Driver");
        try {
            this.source = db.getDataSource();
        } catch (SQLException ex) {
            throw new CstlServiceException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String writeObservationTemplate(final ObservationTemplate template) throws CstlServiceException {
        if (template.getObservation() != null) {
            return writeObservation((AbstractObservation)template.getObservation());
        }
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String writeObservation(final AbstractObservation observation) throws CstlServiceException {
        try {
            final Connection c           = source.getConnection();
            c.setAutoCommit(false);
            final PreparedStatement stmt = c.prepareStatement("INSERT INTO \"om\".\"observations\" VALUES(?,?,?,?,?,?,?)");
            final String observationName;
            int oid;
            if (observation.getName() == null) {
                oid = getNewObservationId();
                observationName = observationIdBase + oid;
            } else {
                observationName = observation.getName();
                if (observationName.startsWith(observationIdBase)) {
                    try {
                        oid = Integer.parseInt(observationName.substring(observationIdBase.length()));
                    } catch (NumberFormatException ex) {
                        oid = getNewObservationId();
                    }
                } else {
                    oid = getNewObservationId();
                }
            }
            
            stmt.setString(1, observationName);
            stmt.setInt(2, oid);
            
            final TemporalObject samplingTime = observation.getSamplingTime();
            if (samplingTime instanceof Period) {
                final Period period  = (Period) samplingTime;
                final Date beginDate = period.getBeginning().getPosition().getDate();
                final Date endDate   = period.getEnding().getPosition().getDate();
                if (beginDate != null) {
                    stmt.setTimestamp(3, new Timestamp(beginDate.getTime()));
                } else {
                    stmt.setNull(3, java.sql.Types.TIMESTAMP);
                }
                if (endDate != null) {
                    stmt.setTimestamp(4, new Timestamp(endDate.getTime()));
                } else {
                    stmt.setNull(4, java.sql.Types.TIMESTAMP);
                }
            } else if (samplingTime instanceof Instant) {
                final Instant instant = (Instant) samplingTime;
                final Date date       = instant.getPosition().getDate();
                if (date != null) {
                    stmt.setTimestamp(3, new Timestamp(date.getTime()));
                } else {
                    stmt.setNull(3, java.sql.Types.TIMESTAMP);
                }
                stmt.setNull(4, java.sql.Types.TIMESTAMP);
            } else {
                stmt.setNull(3, java.sql.Types.TIMESTAMP);
                stmt.setNull(4, java.sql.Types.TIMESTAMP);
            }
            final PhenomenonProperty phenomenon = (PhenomenonProperty)observation.getPropertyObservedProperty();
            final String phenRef = writePhenomenon(phenomenon, c);
            stmt.setString(5, phenRef);
            
            final org.geotoolkit.observation.xml.Process procedure = (org.geotoolkit.observation.xml.Process)observation.getProcedure();
            writeProcedure(procedure.getHref(), c);
            stmt.setString(6, procedure.getHref());
            final org.geotoolkit.sampling.xml.SamplingFeature foi = (org.geotoolkit.sampling.xml.SamplingFeature)observation.getFeatureOfInterest();
            if (foi != null) {
                stmt.setString(7, foi.getId());
                writeFeatureOfInterest(foi, c);
            } else {
                stmt.setNull(7, java.sql.Types.VARCHAR);
            }
            
            stmt.executeUpdate();
            stmt.close();
            
            writeResult(oid, observation.getResult(), c);
            
            updateOrCreateOffering(procedure.getHref(),samplingTime, phenRef, c);
            
            c.commit();
            c.close();
            return observation.getName();
        } catch (SQLException ex) {
            throw new CstlServiceException("Error while inserting observation.", ex, NO_APPLICABLE_CODE);
        }
    }
    
    private String writePhenomenon(final PhenomenonProperty phenomenonP, final Connection c) throws SQLException {
        final String phenomenonId = getPhenomenonId(phenomenonP);
        final PreparedStatement stmtExist = c.prepareStatement("SELECT * FROM  \"om\".\"observed_properties\" WHERE \"id\"=?");
        stmtExist.setString(1, phenomenonId);
        final ResultSet rs = stmtExist.executeQuery();
        if (!rs.next()) {
            final PreparedStatement stmtInsert = c.prepareStatement("INSERT INTO \"om\".\"observed_properties\" VALUES(?)");
            stmtInsert.setString(1, phenomenonId);
            stmtInsert.executeUpdate();
            stmtInsert.close();
            if (phenomenonP.getPhenomenon() instanceof CompositePhenomenon) {
                final CompositePhenomenon composite = (CompositePhenomenon) phenomenonP.getPhenomenon();
                final PreparedStatement stmtInsertCompo = c.prepareStatement("INSERT INTO \"om\".\"components\" VALUES(?,?)");
                for (PhenomenonProperty child : composite.getRealComponent()) {
                    final String childID = getPhenomenonId(child);
                    writePhenomenon(child, c);
                    stmtInsertCompo.setString(1, phenomenonId);
                    stmtInsertCompo.setString(2, childID);
                    stmtInsertCompo.executeUpdate();
                }
                stmtInsertCompo.close();
            }
        } 
        rs.close();
        stmtExist.close();
        return phenomenonId;
    }
    
    private String getPhenomenonId(final PhenomenonProperty phenomenonP) {
        if (phenomenonP.getHref() != null) {
            return phenomenonP.getHref();
        } else if (phenomenonP.getPhenomenon() != null) {
            return phenomenonP.getPhenomenon().getName();
        } else {
            return null;
        }
    }
    
    private void writeProcedure(final String procedureID, final Connection c) throws SQLException {
        final PreparedStatement stmtExist = c.prepareStatement("SELECT * FROM  \"om\".\"procedures\" WHERE \"id\"=?");
        stmtExist.setString(1, procedureID);
        final ResultSet rs = stmtExist.executeQuery();
        if (!rs.next()) {
            final PreparedStatement stmtInsert = c.prepareStatement("INSERT INTO \"om\".\"procedures\" VALUES(?)");
            stmtInsert.setString(1, procedureID);
            stmtInsert.executeUpdate();
            stmtInsert.close();
        } 
        rs.close();
        stmtExist.close();
    }
    
    // TODO
    private void writeFeatureOfInterest(final SamplingFeature foi, final Connection c) throws SQLException {
        final PreparedStatement stmtExist = c.prepareStatement("SELECT * FROM  \"om\".\"sampling_features\" WHERE \"id\"=?");
        stmtExist.setString(1, foi.getId());
        final ResultSet rs = stmtExist.executeQuery();
        if (!rs.next()) {
            final PreparedStatement stmtInsert = c.prepareStatement("INSERT INTO \"om\".\"sampling_features\" VALUES(?,?,?,?,?,?)");
            stmtInsert.setString(1, foi.getId());
            stmtInsert.setString(2, foi.getName());
            stmtInsert.setString(3, foi.getDescription());
            stmtInsert.setNull(4, java.sql.Types.VARCHAR); // TODO
            
            if (foi.getGeometry() != null) {
                try {
                    WKBWriter writer = new WKBWriter();
                    final Geometry geom = GeometrytoJTS.toJTS((AbstractGeometry)foi.getGeometry());
                    final int SRID = geom.getSRID();
                    stmtInsert.setBytes(5, writer.write(geom));
                    stmtInsert.setInt(6, SRID);
                } catch (FactoryException ex) {
                    LOGGER.log(Level.WARNING, "unable to transform the geometry to JTS", ex);
                    stmtInsert.setNull(5, java.sql.Types.VARBINARY);
                    stmtInsert.setNull(6, java.sql.Types.INTEGER);
                }
            } else {
                stmtInsert.setNull(5, java.sql.Types.VARBINARY);
                stmtInsert.setNull(6, java.sql.Types.INTEGER);
            }
            
            stmtInsert.executeUpdate();
            stmtInsert.close();
        } 
        rs.close();
        stmtExist.close();
    }
    
    private void writeResult(final int oid, final Object result, final Connection c) throws SQLException, CstlServiceException {
        final PreparedStatement stmt = c.prepareStatement("INSERT INTO \"om\".\"mesures\" VALUES(?,?,?,?,?,?,?,?)");
        if (result instanceof Measure) {
            final Measure measure = (Measure) result;
            stmt.setInt(1, oid);
            stmt.setInt(2, 1);
            stmt.setNull(3, java.sql.Types.TIMESTAMP);
            stmt.setString(4, Double.toString(measure.getValue()));
            stmt.setString(5, measure.getUom().getUnitsSystem());
            stmt.setNull(6, java.sql.Types.VARCHAR);
            stmt.setNull(7, java.sql.Types.VARCHAR);
            stmt.setNull(8, java.sql.Types.VARCHAR);
            stmt.executeUpdate();
        } else if (result instanceof DataArrayProperty) {
            final DataArray array = ((DataArrayProperty) result).getDataArray();
            if (!(array.getEncoding() instanceof TextBlock)) {
                throw new CstlServiceException("Only TextEncoding is supported");
            }
            final TextBlock encoding = (TextBlock) array.getEncoding();
            
            if (!(array.getPropertyElementType().getAbstractRecord() instanceof DataRecord) &&
                !(array.getPropertyElementType().getAbstractRecord() instanceof SimpleDataRecord)) {
                throw new CstlServiceException("Only DataRecord is supported");
            }
            final List<Field> fields = new ArrayList<>();
            if (array.getPropertyElementType().getAbstractRecord() instanceof DataRecord) {
                final DataRecord record = (DataRecord)array.getPropertyElementType().getAbstractRecord();
                for (DataComponentProperty field : record.getField()) {
                    if (field.getName().equalsIgnoreCase("Time")) {
                        continue;
                    } else {
                        if (field.getValue() instanceof Quantity) {
                            final Quantity q = (Quantity)field.getValue();
                            final String uom  = q.getUom().getCode();
                            final String desc = q.getDefinition();
                            fields.add(new Field("Quantity", field.getName(), desc, uom));
                        } else if (field.getValue() instanceof AbstractText) {
                            final AbstractText q = (AbstractText)field.getValue();
                            final String desc = q.getDefinition();
                            fields.add(new Field("Text", field.getName(), desc, null));
                        } else {
                            throw new CstlServiceException("Only Quantity and Text is supported for now");
                        }
                    }
                }
            } else {
                final SimpleDataRecord record = (SimpleDataRecord)array.getPropertyElementType().getAbstractRecord();
                for (AnyScalar field : record.getField()) {
                    if (field.getName().equalsIgnoreCase("Time")) {
                        continue;
                    } else {
                        if (field.getValue() instanceof Quantity) {
                            final Quantity q = (Quantity)field.getValue();
                            final String uom  = q.getUom().getCode();
                            final String desc = q.getDefinition();
                            fields.add(new Field("Quantity", field.getName(), desc, uom));
                        } else if (field.getValue() instanceof AbstractText) {
                            final AbstractText q = (AbstractText)field.getValue();
                            final String desc = q.getDefinition();
                            fields.add(new Field("Text", field.getName(), desc, null));
                        } else if (field.getValue() instanceof AbstractBoolean) {
                            final AbstractBoolean q = (AbstractBoolean)field.getValue();
                            final String desc = q.getDefinition();
                            fields.add(new Field("Boolean", field.getName(), desc, null));
                        } else {
                            throw new CstlServiceException("Only Quantity is supported for now");
                        }
                        
                    }
                }
            }
            final String values = array.getValues();
            final StringTokenizer tokenizer = new StringTokenizer(values, encoding.getBlockSeparator());
            int n = 1;
            Timestamp lastDate = null;
            while (tokenizer.hasMoreTokens()) {
                String block = tokenizer.nextToken();
                // time field
                int tokenIndex = block.indexOf(encoding.getTokenSeparator());
                final String time = block.substring(0, tokenIndex);
                final Timestamp realTime;
                try {
                    final long millis = new ISODateParser().parseToMillis(time);
                    realTime = new Timestamp(millis);
                } catch (IllegalArgumentException ex) {
                    throw new CstlServiceException("Bad format of timestamp for:" + time);
                }
                block = block.substring(tokenIndex + 1);
                for (int i = 0; i < fields.size(); i++) {
                    final String value;
                    if (i == fields.size() - 1) {
                        value     = block;
                        lastDate  = realTime;
                    } else {
                        int separator = block.indexOf(encoding.getTokenSeparator());
                        value = block.substring(0, separator);
                        block = block.substring(separator + 1);
                    }
                    stmt.setInt(1, oid);
                    stmt.setInt(2, n);
                    stmt.setTimestamp(3, realTime);
                    stmt.setString(4, value);
                    stmt.setString(5, fields.get(i).fieldUom);
                    stmt.setString(6, fields.get(i).fieldType);
                    stmt.setString(7, fields.get(i).fieldName);
                    stmt.setString(8, fields.get(i).fieldDesc);
                    stmt.executeUpdate();
                    n++;
                }
            }
            if (lastDate != null) {
                final PreparedStatement stmt2 = c.prepareStatement("UPDATE \"om\".\"observations\" SET \"time_end\"=? WHERE \"id\"=?");
                stmt2.setTimestamp(1, lastDate);
                stmt2.setInt(2, oid);
                stmt2.executeUpdate();
            }
        }
        stmt.close();
    }
    
    private void updateOrCreateOffering(final String procedureID, final TemporalObject samplingTime, final String phenoID, final Connection c) throws SQLException {
        final String offeringID;
        if (procedureID.startsWith(sensorIdBase)) {
            offeringID  = "offering-" + procedureID.substring(sensorIdBase.length());
        } else {
            offeringID  = "offering-" + procedureID;
        }
       
        final PreparedStatement stmtExist = c.prepareStatement("SELECT * FROM  \"om\".\"offerings\" WHERE \"identifier\"=?");
        stmtExist.setString(1, offeringID);
        final ResultSet rs = stmtExist.executeQuery();
        
        // INSERT
        if (!rs.next()) {
            final PreparedStatement stmtInsert = c.prepareStatement("INSERT INTO \"om\".\"offerings\" VALUES(?,?,?,?,?,?)");
            stmtInsert.setString(1, offeringID);
            stmtInsert.setString(2, "Offering for procedure:" + procedureID);
            stmtInsert.setString(3, offeringID);
            if (samplingTime instanceof Period) {
                final Period period  = (Period)samplingTime;
                final Date beginDate = period.getBeginning().getPosition().getDate();
                final Date endDate   = period.getEnding().getPosition().getDate();
                if (beginDate != null) {
                    stmtInsert.setTimestamp(4, new Timestamp(beginDate.getTime()));
                } else {
                    stmtInsert.setNull(4, java.sql.Types.TIMESTAMP);
                }
                if (endDate != null) {
                    stmtInsert.setTimestamp(5, new Timestamp(endDate.getTime()));
                } else {
                    stmtInsert.setNull(5, java.sql.Types.TIMESTAMP);
                }
            } else if (samplingTime instanceof Instant) {
                final Instant instant = (Instant)samplingTime;
                final Date date       = instant.getPosition().getDate();
                if (date != null) {
                    stmtInsert.setTimestamp(4, new Timestamp(date.getTime()));
                } else {
                    stmtInsert.setNull(4, java.sql.Types.TIMESTAMP);
                }
                stmtInsert.setNull(5, java.sql.Types.TIMESTAMP);
            } else {
                stmtInsert.setNull(4, java.sql.Types.TIMESTAMP);
                stmtInsert.setNull(5, java.sql.Types.TIMESTAMP);
            }
            stmtInsert.setString(6, procedureID);
            stmtInsert.executeUpdate();
            stmtInsert.close();
            
            final PreparedStatement stmtInsertOP = c.prepareStatement("INSERT INTO \"om\".\"offering_observed_properties\" VALUES(?,?)");
            stmtInsertOP.setString(1, offeringID);
            stmtInsertOP.setString(2, phenoID);
            stmtInsertOP.executeUpdate();
            stmtInsertOP.close();
            
        // UPDATE
        } else {
            
            /*
             * update time bound
             */ 
            final Timestamp timeBegin = rs.getTimestamp(4);
            final long offBegin;
            if (timeBegin != null) {
                offBegin = timeBegin.getTime();
            } else {
                offBegin = Long.MAX_VALUE;
            }
            final Timestamp timeEnd   = rs.getTimestamp(5);
            final long offEnd;
            if (timeEnd != null) {
                offEnd = timeEnd.getTime();
            } else {
                offEnd = -Long.MAX_VALUE;
            }
            
            if (samplingTime instanceof Period) {
                final Period period  = (Period) samplingTime;
                final Date beginDate = period.getBeginning().getPosition().getDate();
                final Date endDate   = period.getEnding().getPosition().getDate();
                if (beginDate != null) {
                    final long obsBeginTime = beginDate.getTime();
                    if (obsBeginTime < offBegin) {
                        final PreparedStatement beginStmt = c.prepareStatement("UPDATE \"om\".\"offerings\" SET \"time_begin\"=?");
                        beginStmt.setTimestamp(1, new Timestamp(obsBeginTime));
                        beginStmt.executeUpdate();
                        beginStmt.close();
                    }
                }
                if (endDate != null) {
                    final long obsEndTime = endDate.getTime();
                    if (obsEndTime > offEnd) {
                        final PreparedStatement endStmt = c.prepareStatement("UPDATE \"om\".\"offerings\" SET \"time_end\"=?");
                        endStmt.setTimestamp(1, new Timestamp(obsEndTime));
                        endStmt.executeUpdate();
                        endStmt.close();
                    }
                }
            } else if (samplingTime instanceof Instant) {
                final Instant instant = (Instant) samplingTime;
                final Date date       = instant.getPosition().getDate();
                if (date != null) {
                    final long obsTime = date.getTime();
                    if (obsTime < offBegin) {
                        final PreparedStatement beginStmt = c.prepareStatement("UPDATE \"om\".\"offerings\" SET \"time_begin\"=?");
                        beginStmt.setTimestamp(1, new Timestamp(obsTime));
                        beginStmt.executeUpdate();
                        beginStmt.close();
                    }
                    if (obsTime > offEnd) {
                        final PreparedStatement endStmt = c.prepareStatement("UPDATE \"om\".\"offerings\" SET \"time_end\"=?");
                        endStmt.setTimestamp(1, new Timestamp(obsTime));
                        endStmt.executeUpdate();
                        endStmt.close();
                    }
                }
            }
            
            /*
             * Phenomenon
             */
            final PreparedStatement phenoStmt = c.prepareStatement("SELECT * FROM  \"om\".\"offering_observed_properties\" WHERE \"id_offering\"=? AND \"phenomenon\"=?");
            phenoStmt.setString(1, offeringID);
            phenoStmt.setString(2, phenoID);
            final ResultSet rsp = phenoStmt.executeQuery();
            if (!rsp.next()) {
                final PreparedStatement stmtInsert = c.prepareStatement("INSERT INTO \"om\".\"offering_observed_properties\" VALUES(?,?)");
                stmtInsert.setString(1, offeringID);
                stmtInsert.setString(2, phenoID);
                stmtInsert.executeUpdate();
                stmtInsert.close();
            }
            rsp.close();
            phenoStmt.close();
            
            /*
             * Feature Of interest TODO
             */
        }
        rs.close();
        stmtExist.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String writeOffering(final ObservationOffering offering) throws CstlServiceException {
        try {
            final Connection c           = source.getConnection();
            final PreparedStatement stmt = c.prepareStatement("INSERT INTO \"om\".\"offerings\" VALUES(?,?,?,?,?,?)");
            stmt.setString(1, offering.getId());
            stmt.setString(2, offering.getDescription());
            stmt.setString(3, offering.getName());
            if (offering.getTime() instanceof Period) {
                final Period period = (Period)offering.getTime();
                if (period.getBeginning() != null && period.getBeginning().getPosition() != null && period.getBeginning().getPosition().getDate() != null) {
                    stmt.setTimestamp(4, new Timestamp(period.getBeginning().getPosition().getDate().getTime()));
                } else {
                    stmt.setNull(4, java.sql.Types.TIMESTAMP);
                }
                if (period.getEnding() != null && period.getEnding().getPosition() != null && period.getEnding().getPosition().getDate() != null) {
                    stmt.setTimestamp(5, new Timestamp(period.getEnding().getPosition().getDate().getTime()));
                } else {
                    stmt.setNull(5, java.sql.Types.TIMESTAMP);
                }
            } else if (offering.getTime() instanceof Instant) {
                final Instant instant = (Instant)offering.getTime();
                if (instant.getPosition() != null && instant.getPosition().getDate() != null) {
                    stmt.setTimestamp(4, new Timestamp(instant.getPosition().getDate().getTime()));
                } else {
                    stmt.setNull(4, java.sql.Types.TIMESTAMP);
                }
                stmt.setNull(5, java.sql.Types.TIMESTAMP);
            } else {
                stmt.setNull(4, java.sql.Types.TIMESTAMP);
                stmt.setNull(5, java.sql.Types.TIMESTAMP);
            }
            stmt.setString(6, offering.getProcedures().get(0));
            stmt.executeUpdate();
            stmt.close();
            final PreparedStatement opstmt = c.prepareStatement("INSERT INTO \"om\".\"offering_observed_properties\" VALUES(?,?)");
            for (String op : offering.getObservedProperties()) {
                opstmt.setString(1, offering.getId());
                opstmt.setString(2, op);
                opstmt.executeUpdate();
            }
            opstmt.close();
            
            final PreparedStatement foistmt = c.prepareStatement("INSERT INTO \"om\".\"offering_foi\" VALUES(?,?)");
            for (String foi : offering.getFeatureOfInterestIds()) {
                foistmt.setString(1, offering.getId());
                foistmt.setString(2, foi);
                foistmt.executeUpdate();
            }
            foistmt.close();
            
            c.close();
            return offering.getId();
        } catch (SQLException ex) {
            throw new CstlServiceException("Error while inserting offering.", ex, NO_APPLICABLE_CODE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateOffering(final String offeringID, final String offProc, final List<String> offPheno, final String offSF) throws CstlServiceException {
        try {
            final Connection c           = source.getConnection();
            if (offProc != null) {
                // single pricedure in v2.0.0
            }
            if (offPheno != null) {
                final PreparedStatement opstmt = c.prepareStatement("INSERT INTO \"om\".\"offering_observed_properties\" VALUES(?,?)");
                for (String op : offPheno) {
                    opstmt.setString(1, offeringID);
                    opstmt.setString(2, op);
                    opstmt.executeUpdate();
                }
                opstmt.close();
            }
            if (offSF != null) {
                final PreparedStatement foistmt = c.prepareStatement("INSERT INTO \"om\".\"offering_foi\" VALUES(?,?)");
                foistmt.setString(1, offeringID);
                foistmt.setString(2, offSF);
                foistmt.executeUpdate();
                foistmt.close();
            }
            c.close();
        } catch (SQLException e) {
            throw new CstlServiceException(e.getMessage(), e, NO_APPLICABLE_CODE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateOfferings() {
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void recordProcedureLocation(final String physicalID, final DirectPosition position) throws CstlServiceException {
        if (position != null) {
            try {
                final Connection c     = source.getConnection();
                final WKBWriter writer = new WKBWriter();
                PreparedStatement ps   = c.prepareStatement("INSERT INTO \"om\".\"procedures\" VALUES (?,?)");
                ps.setString(1, physicalID);
                final org.geotoolkit.gml.xml.Point gmlPt = SOSXmlFactory.buildPoint("2.0.0", null, (org.geotoolkit.gml.xml.DirectPosition)position);
                final Point pt = (Point) GeometrytoJTS.toJTS(gmlPt);
                ps.setBytes(3, writer.write(pt));
                ps.execute();
                c.close();
            } catch (SQLException | FactoryException e) {
                throw new CstlServiceException(e.getMessage(), e, NO_APPLICABLE_CODE);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    private int getNewObservationId() throws CstlServiceException {
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
            return resultNum;
        } catch (SQLException ex) {
            throw new CstlServiceException("Error while looking for available observation id.", ex, NO_APPLICABLE_CODE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getInfos() {
        return "Constellation O&M 2 Writer 0.9";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
       
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeObservation(final String observationID) throws CstlServiceException {
        try {
            final Connection c              = source.getConnection();
            c.setAutoCommit(false);
            final PreparedStatement stmtMes = c.prepareStatement("DELETE FROM \"om\".\"mesures\" WHERE id_observation=?");
            final PreparedStatement stmtObs = c.prepareStatement("DELETE FROM \"om\".\"observations\" WHERE id=?");
            
            final int oid = Integer.parseInt(observationID.substring(observationIdBase.length()));
            stmtMes.setInt(1, oid);
            stmtMes.executeUpdate();
            stmtMes.close();
            stmtObs.setInt(1, oid);
            stmtObs.executeUpdate();
            stmtObs.close();
            
            c.commit();
            c.close();
        } catch (SQLException ex) {
            throw new CstlServiceException("Error while inserting observation.", ex, NO_APPLICABLE_CODE);
        }
    }

    private static class Field {
        
        public String fieldType;
        public String fieldName;
        public String fieldDesc;
        public String fieldUom;
        
        public Field(final String fieldType, final String fieldName, final String fieldDesc, final String fieldUom) {
            this.fieldDesc = fieldDesc;
            this.fieldName = fieldName;
            this.fieldType = fieldType;
            this.fieldUom  = fieldUom;
        }
    }
}
