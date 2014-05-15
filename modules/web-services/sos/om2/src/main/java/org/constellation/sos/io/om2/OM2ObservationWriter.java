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
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.logging.Logging;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.sos.factory.OMFactory;
import org.geotoolkit.gml.GeometrytoJTS;
import org.geotoolkit.gml.xml.AbstractGeometry;
import org.geotoolkit.observation.ObservationWriter;
import org.geotoolkit.observation.xml.AbstractObservation;
import org.geotoolkit.observation.xml.v200.OMObservationType;

import org.geotoolkit.observation.xml.v200.OMObservationType.InternalPhenomenon;
import org.geotoolkit.sampling.xml.SamplingFeature;
import org.geotoolkit.sos.xml.ObservationOffering;
import org.geotoolkit.sos.xml.SOSXmlFactory;
import org.geotoolkit.swe.xml.AbstractBoolean;
import org.geotoolkit.swe.xml.AbstractText;
import org.geotoolkit.swe.xml.AnyScalar;
import org.geotoolkit.swe.xml.CompositePhenomenon;
import org.geotoolkit.swe.xml.DataArray;
import org.geotoolkit.swe.xml.DataArrayProperty;
import org.geotoolkit.swe.xml.DataComponentProperty;
import org.geotoolkit.swe.xml.DataRecord;
import org.geotoolkit.swe.xml.PhenomenonProperty;
import org.geotoolkit.swe.xml.Quantity;
import org.geotoolkit.swe.xml.SimpleDataRecord;
import org.geotoolkit.swe.xml.TextBlock;
import org.geotoolkit.swe.xml.v101.PhenomenonType;
import org.geotoolkit.swes.xml.ObservationTemplate;
import org.geotoolkit.temporal.object.ISODateParser;
import org.opengis.observation.Measure;
import org.opengis.observation.Observation;
import org.opengis.observation.Phenomenon;
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
     * @throws org.apache.sis.storage.DataStoreException
     */
    public OM2ObservationWriter(final Automatic configuration, final Map<String, Object> properties) throws DataStoreException {
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
            throw new DataStoreException("The configuration object is null");
        }
        // we get the database informations
        final BDD db = configuration.getBdd();
        if (db == null) {
            throw new DataStoreException("The configuration file does not contains a BDD object");
        }
        isPostgres = db.getClassName() != null && db.getClassName().equals("org.postgresql.Driver");
        try {
            this.source = db.getDataSource();
        } catch (SQLException ex) {
            throw new DataStoreException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String writeObservationTemplate(final ObservationTemplate template) throws DataStoreException {
        if (template.getObservation() != null) {
            return writeObservation((AbstractObservation)template.getObservation());
        }
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String writeObservation(final Observation observation) throws DataStoreException {
        try {
            final Connection c      = source.getConnection();
            c.setAutoCommit(false);
            final int generatedID   = getNewObservationId();
            final String oid        = writeObservation(observation, c, generatedID);
            c.commit();
            c.close();
            return oid;
        } catch (SQLException ex) {
            throw new DataStoreException("Error while inserting observations.", ex);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> writeObservations(final List<Observation> observations) throws DataStoreException {
        final List<String> results = new ArrayList<>();
        try {
            final Connection c           = source.getConnection();
            c.setAutoCommit(false);
            int generatedID = getNewObservationId();
            for (Observation observation : observations) {
                final String oid = writeObservation(observation, c, generatedID);
                c.commit();
                results.add(oid);
                generatedID++;
            }
            c.close();
        } catch (SQLException ex) {
            throw new DataStoreException("Error while inserting observations.", ex);
        }
        return results;
    }
    
    
    
    
    private String writeObservation(final Observation observation, final Connection c, final int generatedID) throws DataStoreException {
        try {
            final PreparedStatement stmt = c.prepareStatement("INSERT INTO \"om\".\"observations\" VALUES(?,?,?,?,?,?,?)");
            final String observationName;
            int oid;
            if (observation.getName() == null) {
                oid = generatedID;
                observationName = observationIdBase + oid;
            } else {
                observationName = observation.getName();
                if (observationName.startsWith(observationIdBase)) {
                    try {
                        oid = Integer.parseInt(observationName.substring(observationIdBase.length()));
                    } catch (NumberFormatException ex) {
                        oid = generatedID;
                    }
                } else {
                    oid = generatedID;
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
            final PhenomenonProperty phenomenon = (PhenomenonProperty)((AbstractObservation)observation).getPropertyObservedProperty();
            final String phenRef = writePhenomenon(phenomenon, c);
            stmt.setString(5, phenRef);
            
            final org.geotoolkit.observation.xml.Process procedure = (org.geotoolkit.observation.xml.Process)observation.getProcedure();
            writeProcedure(procedure.getHref(), c);
            stmt.setString(6, procedure.getHref());
            final org.geotoolkit.sampling.xml.SamplingFeature foi = (org.geotoolkit.sampling.xml.SamplingFeature)observation.getFeatureOfInterest();
            final String foiID;
            if (foi != null) {
                foiID = foi.getId();
                stmt.setString(7, foiID);
                writeFeatureOfInterest(foi, c);
            } else {
                foiID = null;
                stmt.setNull(7, java.sql.Types.VARCHAR);
            }
            
            stmt.executeUpdate();
            stmt.close();
            
            writeResult(oid, observation.getResult(), samplingTime, c);
            
            updateOrCreateOffering(procedure.getHref(),samplingTime, phenRef, foiID, c);
            return observation.getName();
        } catch (SQLException ex) {
            throw new DataStoreException("Error while inserting observation.", ex);
        }
    }
    
    @Override
    public void writePhenomenons(final List<Phenomenon> phenomenons) throws DataStoreException {
        try {
            final Connection c           = source.getConnection();
            c.setAutoCommit(false);
            for (Phenomenon phenomenon : phenomenons) {
                if (phenomenon instanceof InternalPhenomenon) {
                    final InternalPhenomenon internal = (InternalPhenomenon)phenomenon;
                    phenomenon = new PhenomenonType(internal.getName(), internal.getName());
                }
                final PhenomenonProperty phenomenonP = SOSXmlFactory.buildPhenomenonProperty("1.0.0", (org.geotoolkit.swe.xml.Phenomenon) phenomenon);
                writePhenomenon(phenomenonP, c);
            }
            c.commit();
            c.close();
        } catch (SQLException ex) {
            throw new DataStoreException("Error while inserting phenomenons.", ex);
        }
    }
    
    private String writePhenomenon(final PhenomenonProperty phenomenonP, final Connection c) throws SQLException {
        final String phenomenonId = getPhenomenonId(phenomenonP);
        final PreparedStatement stmtExist = c.prepareStatement("SELECT \"id\" FROM  \"om\".\"observed_properties\" WHERE \"id\"=?");
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
        final PreparedStatement stmtExist = c.prepareStatement("SELECT \"id\" FROM  \"om\".\"procedures\" WHERE \"id\"=?");
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
    
    private void writeFeatureOfInterest(final SamplingFeature foi, final Connection c) throws SQLException {
        final PreparedStatement stmtExist = c.prepareStatement("SELECT \"id\" FROM  \"om\".\"sampling_features\" WHERE \"id\"=?");
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
    
    private void writeResult(final int oid, final Object result, final TemporalObject samplingTime, final Connection c) throws SQLException, DataStoreException {
        if (result instanceof Measure) {
            final PreparedStatement stmt = c.prepareStatement("INSERT INTO \"om\".\"mesures\" VALUES(?,?,?,?,?,?,?,?)");
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
            stmt.close();
        } else if (result instanceof DataArrayProperty) {
            final DataArray array = ((DataArrayProperty) result).getDataArray();
            if (!(array.getEncoding() instanceof TextBlock)) {
                throw new DataStoreException("Only TextEncoding is supported");
            }
            final TextBlock encoding = (TextBlock) array.getEncoding();
            
            if (!(array.getPropertyElementType().getAbstractRecord() instanceof DataRecord) &&
                !(array.getPropertyElementType().getAbstractRecord() instanceof SimpleDataRecord)) {
                throw new DataStoreException("Only DataRecord is supported");
            }
            final List<Field> fields = new ArrayList<>();
            boolean hasTime = false;
            if (array.getPropertyElementType().getAbstractRecord() instanceof DataRecord) {
                final DataRecord record = (DataRecord)array.getPropertyElementType().getAbstractRecord();
                for (DataComponentProperty field : record.getField()) {
                    if (field.getName().equalsIgnoreCase("Time")) {
                        hasTime = true;
                    } else {
                        if (field.getValue() instanceof Quantity) {
                            final Quantity q = (Quantity)field.getValue();
                            final String uom;
                            if (q.getUom() != null) {
                                uom = q.getUom().getCode();
                            } else {
                                uom = null;
                            }
                            final String desc = q.getDefinition();
                            fields.add(new Field("Quantity", field.getName(), desc, uom));
                        } else if (field.getValue() instanceof AbstractText) {
                            final AbstractText q = (AbstractText)field.getValue();
                            final String desc = q.getDefinition();
                            fields.add(new Field("Text", field.getName(), desc, null));
                        } else {
                            throw new DataStoreException("Only Quantity and Text is supported for now");
                        }
                    }
                }
            } else {
                final SimpleDataRecord record = (SimpleDataRecord)array.getPropertyElementType().getAbstractRecord();
                for (AnyScalar field : record.getField()) {
                    if (field.getName().equalsIgnoreCase("Time")) {
                        hasTime = true;
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
                            throw new DataStoreException("Only Quantity is supported for now");
                        }
                        
                    }
                }
            }
            final String values = array.getValues();
            final StringTokenizer tokenizer = new StringTokenizer(values, encoding.getBlockSeparator());
            int n = 1;
            int sqlCpt = 0;
            Timestamp lastDate = null;
            final Statement stmtSQL = c.createStatement();
            StringBuilder sql = new StringBuilder("INSERT INTO \"om\".\"mesures\" VALUES ");
            while (tokenizer.hasMoreTokens()) {
                String block       = tokenizer.nextToken();
                
                // time field
                final Timestamp realTime;
                if (hasTime) {
                    final int next;
                    int tokenIndex     = block.indexOf(encoding.getTokenSeparator());
                    if (tokenIndex == -1) {
                        tokenIndex     = block.length();
                        next = tokenIndex;
                    } else {
                        next = tokenIndex + 1;
                    }
                    final String first = block.substring(0, tokenIndex);
                    try {
                        final long millis = new ISODateParser().parseToMillis(first);
                        realTime = new Timestamp(millis);
                    } catch (IllegalArgumentException ex) {
                        throw new DataStoreException("Bad format of timestamp for:" + first);
                    }
                    block = block.substring(next);
                } else {
                    if (samplingTime instanceof Period) {
                        LOGGER.warning("expecting a timeInstant for observation with no time field.");
                        final Period period = (Period) samplingTime;
                        final Date beginDate = period.getBeginning().getPosition().getDate();
                        final Date endDate = period.getEnding().getPosition().getDate();
                        if (beginDate != null) {
                            realTime = new Timestamp(beginDate.getTime());
                        } else if (endDate != null) {
                            realTime = new Timestamp(endDate.getTime());
                        } else {
                            realTime = null;
                        }
                    } else if (samplingTime instanceof Instant) {
                        final Instant instant = (Instant) samplingTime;
                        final Date date = instant.getPosition().getDate();
                        if (date != null) {
                            realTime = new Timestamp(date.getTime());
                        } else {
                            realTime = null;
                        }
                    } else {
                        realTime = null;
                    }
                }
                
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

                    sql.append('(').append(oid).append(',').append(n).append(',');
                    if (realTime != null) {
                        sql.append("'").append(realTime.toString()).append("',");
                    } else {
                        sql.append("NULL,");
                    }
                    
                    sql.append("'").append(value).append("',");
                    sql.append("'").append(fields.get(i).fieldUom).append("',");
                    sql.append("'").append(fields.get(i).fieldType).append("',");
                    sql.append("'").append(fields.get(i).fieldName).append("',");
                    sql.append("'").append(fields.get(i).fieldDesc).append("'),\n");
                    n++;
                    sqlCpt++;
                }
                if (sqlCpt > 99) {
                    sql.setCharAt(sql.length() - 2, ' ');
                    if (isPostgres){
                        sql.setCharAt(sql.length() - 1, ';');
                    } else {
                        sql.setCharAt(sql.length() - 1, ' ');
                    }
                    
                    stmtSQL.addBatch(sql.toString());
                    sqlCpt = 0;
                    sql = new StringBuilder("INSERT INTO \"om\".\"mesures\" VALUES ");
                }
            }
            if (sqlCpt > 0) {
                sql.setCharAt(sql.length() - 2, ' ');
               if (isPostgres){
                    sql.setCharAt(sql.length() - 1, ';');
                } else {
                    sql.setCharAt(sql.length() - 1, ' ');
                }
                stmtSQL.addBatch(sql.toString());
            }
            stmtSQL.executeBatch();
            stmtSQL.close();
            
            if (lastDate != null) {
                final PreparedStatement stmt2 = c.prepareStatement("UPDATE \"om\".\"observations\" SET \"time_end\"=? WHERE \"id\"=?");
                stmt2.setTimestamp(1, lastDate);
                stmt2.setInt(2, oid);
                stmt2.executeUpdate();
            }
        }
    }
    
    private void updateOrCreateOffering(final String procedureID, final TemporalObject samplingTime, final String phenoID, final String foiID, final Connection c) throws SQLException {
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
            
            if (phenoID != null) {
                final PreparedStatement stmtInsertOP = c.prepareStatement("INSERT INTO \"om\".\"offering_observed_properties\" VALUES(?,?)");
                stmtInsertOP.setString(1, offeringID);
                stmtInsertOP.setString(2, phenoID);
                stmtInsertOP.executeUpdate();
                stmtInsertOP.close();
            }
            
            if (foiID != null) {
                final PreparedStatement stmtInsertFOI = c.prepareStatement("INSERT INTO \"om\".\"offering_foi\" VALUES(?,?)");
                stmtInsertFOI.setString(1, offeringID);
                stmtInsertFOI.setString(2, foiID);
                stmtInsertFOI.executeUpdate();
                stmtInsertFOI.close();
            }
            
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
            if (phenoID != null) {
                final PreparedStatement phenoStmt = c.prepareStatement("SELECT \"phenomenon\" FROM  \"om\".\"offering_observed_properties\" WHERE \"id_offering\"=? AND \"phenomenon\"=?");
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
            }
            
            /*
             * Feature Of interest
             */
            if (foiID != null) {
                final PreparedStatement foiStmt = c.prepareStatement("SELECT \"foi\" FROM  \"om\".\"offering_foi\" WHERE \"id_offering\"=? AND \"foi\"=?");
                foiStmt.setString(1, offeringID);
                foiStmt.setString(2, foiID);
                final ResultSet rsf = foiStmt.executeQuery();
                if (!rsf.next()) {
                    final PreparedStatement stmtInsert = c.prepareStatement("INSERT INTO \"om\".\"offering_foi\" VALUES(?,?)");
                    stmtInsert.setString(1, offeringID);
                    stmtInsert.setString(2, foiID);
                    stmtInsert.executeUpdate();
                    stmtInsert.close();
                }
                rsf.close();
                foiStmt.close();
            }
        }
        rs.close();
        stmtExist.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String writeOffering(final ObservationOffering offering) throws DataStoreException {
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
            throw new DataStoreException("Error while inserting offering.", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateOffering(final String offeringID, final String offProc, final List<String> offPheno, final String offSF) throws DataStoreException {
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
            throw new DataStoreException(e.getMessage(), e);
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
    public void recordProcedureLocation(final String physicalID, final AbstractGeometry position) throws DataStoreException {
        if (position != null) {
            try {
                final Connection c     = source.getConnection();
                final WKBWriter writer = new WKBWriter();
                PreparedStatement ps   = c.prepareStatement("UPDATE \"om\".\"procedures\" SET \"shape\"=?, \"crs\"=? WHERE id=?");
                ps.setString(3, physicalID);
                final Geometry pt = GeometrytoJTS.toJTS(position);
                ps.setBytes(1, writer.write(pt));
                int srid = pt.getSRID();
                if (srid == 0) {
                    srid = 4326;
                }
                ps.setInt(2, srid);
                ps.execute();
                c.close();
            } catch (SQLException | FactoryException e) {
                throw new DataStoreException(e.getMessage(), e);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    private int getNewObservationId() throws DataStoreException {
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
            throw new DataStoreException("Error while looking for available observation id.", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeObservationForProcedure(final String procedureID) throws DataStoreException {
        try {
            final Connection c              = source.getConnection();
            c.setAutoCommit(false);
            final PreparedStatement stmtMes = c.prepareStatement("DELETE FROM \"om\".\"mesures\" WHERE \"id_observation\" IN (SELECT \"id\" FROM \"om\".\"observations\" WHERE \"procedure\"=?)");
            final PreparedStatement stmtObs = c.prepareStatement("DELETE FROM \"om\".\"observations\" WHERE \"procedure\"=?");
            
            stmtMes.setString(1, procedureID);
            stmtMes.executeUpdate();
            stmtMes.close();
            stmtObs.setString(1, procedureID);
            stmtObs.executeUpdate();
            stmtObs.close();
            
            c.commit();
            c.close();
        } catch (SQLException ex) {
            throw new DataStoreException("Error while inserting observation.", ex);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void removeProcedure(final String procedureID) throws DataStoreException {
        try {
            removeObservationForProcedure(procedureID);
            final Connection c              = source.getConnection();
            c.setAutoCommit(false);
            final PreparedStatement stmtObsP = c.prepareStatement("DELETE FROM \"om\".\"offering_observed_properties\" "
                                                                + "WHERE \"id_offering\" IN(SELECT \"identifier\" FROM \"om\".\"offerings\" WHERE \"procedure\"=?)");
            final PreparedStatement stmtFoi  = c.prepareStatement("DELETE FROM \"om\".\"offering_foi\" "
                                                                + "WHERE \"id_offering\" IN(SELECT \"identifier\" FROM \"om\".\"offerings\" WHERE \"procedure\"=?)");
            final PreparedStatement stmtMes = c.prepareStatement("DELETE FROM \"om\".\"offerings\" WHERE \"procedure\"=?");
            final PreparedStatement stmtObs = c.prepareStatement("DELETE FROM \"om\".\"procedures\" WHERE \"id\"=?");
            
            stmtObsP.setString(1, procedureID);
            stmtObsP.executeUpdate();
            stmtObsP.close();
            stmtFoi.setString(1, procedureID);
            stmtFoi.executeUpdate();
            stmtFoi.close();
            stmtMes.setString(1, procedureID);
            stmtMes.executeUpdate();
            stmtMes.close();
            stmtObs.setString(1, procedureID);
            stmtObs.executeUpdate();
            stmtObs.close();
            
            //look for unused observed properties (execute the statement 2 times for remaining components)
            final Statement stmtOP = c.createStatement();
            for (int i = 0; i < 2; i++) {
                final ResultSet rs = stmtOP.executeQuery(" SELECT \"id\" FROM \"om\".\"observed_properties\""
                                                       + " WHERE  \"id\" NOT IN (SELECT DISTINCT \"observed_property\" FROM \"om\".\"observations\") " 
                                                       + " AND    \"id\" NOT IN (SELECT DISTINCT \"phenomenon\"        FROM \"om\".\"offering_observed_properties\")"
                                                       + " AND    \"id\" NOT IN (SELECT DISTINCT \"component\"         FROM \"om\".\"components\")");

                while (rs.next()) {
                    stmtOP.addBatch("DELETE FROM \"om\".\"components\" WHERE \"phenomenon\"='" + rs.getString(1) + "';");
                    stmtOP.addBatch("DELETE FROM \"om\".\"observed_properties\" WHERE \"id\"='" + rs.getString(1) + "';");
                }
                rs.close();
                stmtOP.executeBatch();
            }
            stmtOP.close();
            
            //look for unused foi
            final Statement stmtFOI = c.createStatement();
            final ResultSet rs2 = stmtFOI.executeQuery(" SELECT \"id\" FROM \"om\".\"sampling_features\""
                                                     + " WHERE  \"id\" NOT IN (SELECT DISTINCT \"foi\" FROM \"om\".\"observations\") " +
                                                       " AND    \"id\" NOT IN (SELECT DISTINCT \"foi\" FROM \"om\".\"offering_foi\")");
            
            while (rs2.next()) {
                stmtFOI.addBatch("DELETE FROM \"om\".\"sampling_features\" WHERE \"id\"='" + rs2.getString(1) + "';");
            }
            rs2.close();
            stmtFOI.executeBatch();
            stmtFOI.close();
            
            c.commit();
            c.close();
        } catch (SQLException ex) {
            throw new DataStoreException("Error while removeing procedure observation.", ex);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void removeObservation(final String observationID) throws DataStoreException {
        try {
            final Connection c              = source.getConnection();
            c.setAutoCommit(false);
            final PreparedStatement stmtMes = c.prepareStatement("DELETE FROM \"om\".\"mesures\" WHERE id_observation IN (SELECT \"id\" FROM \"om\".\"observations\" WHERE identifier=?)");
            final PreparedStatement stmtObs = c.prepareStatement("DELETE FROM \"om\".\"observations\" WHERE identifier=?");
            
            stmtMes.setString(1, observationID);
            stmtMes.executeUpdate();
            stmtMes.close();
            stmtObs.setString(1, observationID);
            stmtObs.executeUpdate();
            stmtObs.close();
            
            c.commit();
            c.close();
        } catch (SQLException ex) {
            throw new DataStoreException("Error while inserting observation.", ex);
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
