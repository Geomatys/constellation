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

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKBWriter;
import org.apache.sis.storage.DataStoreException;
import org.constellation.admin.SpringHelper;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.sos.io.om2.OM2BaseReader.Field;
import org.constellation.sos.ws.GeometrytoJTS;
import org.geotoolkit.gml.xml.AbstractGeometry;
import org.constellation.sos.io.ObservationWriter;
import org.geotoolkit.observation.xml.AbstractObservation;
import org.geotoolkit.observation.xml.v200.OMObservationType.InternalPhenomenon;
import org.geotoolkit.sampling.xml.SamplingFeature;
import org.geotoolkit.sos.xml.ObservationOffering;
import org.geotoolkit.sos.xml.SOSXmlFactory;
import org.geotoolkit.swe.xml.AnyScalar;
import org.geotoolkit.swe.xml.CompositePhenomenon;
import org.geotoolkit.swe.xml.DataArray;
import org.geotoolkit.swe.xml.DataArrayProperty;
import org.geotoolkit.swe.xml.DataComponentProperty;
import org.geotoolkit.swe.xml.DataRecord;
import org.geotoolkit.swe.xml.PhenomenonProperty;
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

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import org.geotoolkit.geometry.jts.transform.AbstractGeometryTransformer;
import org.geotoolkit.geometry.jts.transform.GeometryCSTransformer;
import org.geotoolkit.swe.xml.AbstractDataComponent;
import org.geotoolkit.swe.xml.AbstractDataRecord;
import org.opengis.referencing.operation.TransformException;



/**
 * Default Observation reader for Postgis O&M2 database.
 *
 * @author Guilhem Legal (Geomatys)
 */
public class OM2ObservationWriter extends OM2BaseReader implements ObservationWriter {

    protected final DataSource source;
    
    private boolean allowSensorStructureUpdate = true;
    
    /**
     * Build a new Observation writer for postgrid dataSource.
     *
     * @param configuration
     * @param properties
     *
     * @throws org.apache.sis.storage.DataStoreException
     */
    @Deprecated
    public OM2ObservationWriter(final Automatic configuration, final String schemaPrefix, final Map<String, Object> properties) throws DataStoreException {
        super(properties, schemaPrefix);
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
     * Build a new Observation writer for the given data source.
     *
     * @param source Data source on the database.
     * @param isPostgres {@code True} if the database is a postgresql db, {@code false} otherwise.
     * @param properties
     *
     * @throws org.apache.sis.storage.DataStoreException
     */
    public OM2ObservationWriter(final DataSource source, final boolean isPostgres, final String schemaPrefix, final Map<String, Object> properties) throws DataStoreException {
        super(properties, schemaPrefix);
        if (source == null) {
            throw new DataStoreException("The source object is null");
        }
        this.isPostgres = isPostgres;
        this.source = source;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized String writeObservationTemplate(final ObservationTemplate template) throws DataStoreException {
        if (template.getObservation() != null) {
            return writeObservation(template.getObservation());
        } else  {
            try(final Connection c = source.getConnection()) {
                writeProcedure(template.getProcedure(), null, null, null, c);
                for (PhenomenonProperty phen : template.getFullObservedProperties()) {
                    writePhenomenon(phen, c, true);
                }
                return null;
            } catch (SQLException | FactoryException ex) {
                throw new DataStoreException("Error while inserting observations.", ex);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized String writeObservation(final Observation observation) throws DataStoreException {
        try(final Connection c = source.getConnection()) {
            final int generatedID   = getNewObservationId(c);
            final String oid        = writeObservation(observation, c, generatedID);
            return oid;
        } catch (SQLException ex) {
            throw new DataStoreException("Error while inserting observations.", ex);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized List<String> writeObservations(final List<Observation> observations) throws DataStoreException {
        final List<String> results = new ArrayList<>();
        try(final Connection c = source.getConnection()) {
            int generatedID = getNewObservationId(c);
            for (Observation observation : observations) {
                final String oid = writeObservation(observation, c, generatedID);
                results.add(oid);
                generatedID++;
            }
        } catch (SQLException ex) {
            throw new DataStoreException("Error while inserting observations.", ex);
        }
        return results;
    }
    
    private String writeObservation(final Observation observation, final Connection c, final int generatedID) throws DataStoreException {
        try(final PreparedStatement stmt = c.prepareStatement("INSERT INTO \"" + schemaPrefix + "om\".\"observations\" VALUES(?,?,?,?,?,?,?)")) {
            final String observationName;
            int oid;
            if (observation.getName() == null || observation.getName().getCode() == null) {
                oid = generatedID;
                observationName = observationIdBase + oid;
            } else {
                observationName = observation.getName().getCode();
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
                final Date beginDate = period.getBeginning().getDate();
                final Date endDate   = period.getEnding().getDate();
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
                Date date = instant.getDate();
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
            final String phenRef = writePhenomenon(phenomenon, c, false);
            stmt.setString(5, phenRef);
            
            final org.geotoolkit.observation.xml.Process procedure = (org.geotoolkit.observation.xml.Process)observation.getProcedure();
            final String procedureID = procedure.getHref();
            final int pid = writeProcedure(procedureID, null, null, null, c);
            stmt.setString(6, procedureID);
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

            writeResult(oid, pid, procedureID, observation.getResult(), samplingTime, c);
            emitResultOnBus(procedureID, observation.getResult());
            String parent = getProcedureParent(procedureID, c);
            if (parent != null) {        
                updateOrCreateOffering(parent,samplingTime, phenRef, foiID, c);
            }
            updateOrCreateOffering(procedureID,samplingTime, phenRef, foiID, c);
            
            return observationName;
        } catch (SQLException | FactoryException ex) {
            throw new DataStoreException("Error while inserting observation:" + ex.getMessage(), ex);
        }
    }

    private void emitResultOnBus(String procedureID, Object result) {
        if (result instanceof DataArrayProperty){
            OM2ResultEventDTO resultEvent = new OM2ResultEventDTO();
            final DataArray array = ((DataArrayProperty) result).getDataArray();
            final TextBlock encoding = (TextBlock) array.getEncoding();
            resultEvent.setBlockSeparator(encoding.getBlockSeparator());
            resultEvent.setDecimalSeparator(encoding.getDecimalSeparator());
            resultEvent.setTokenSeparator(encoding.getTokenSeparator());
            resultEvent.setValues(array.getValues());
            List<String> headers = new ArrayList<>();
            if (array.getPropertyElementType().getAbstractRecord() instanceof DataRecord) {
                final DataRecord record = (DataRecord)array.getPropertyElementType().getAbstractRecord();
                for (DataComponentProperty field : record.getField()) {
                    headers.add(field.getName());
                }
            } else if (array.getPropertyElementType().getAbstractRecord() instanceof SimpleDataRecord) {
                final SimpleDataRecord record = (SimpleDataRecord)array.getPropertyElementType().getAbstractRecord();
                for (AnyScalar field : record.getField()) {
                    headers.add(field.getName());
                }
            }
            
            resultEvent.setHeaders(headers);
            resultEvent.setProcedureID(procedureID);
            SpringHelper.sendEvent(resultEvent);
        }
    }

    @Override
    public synchronized void writePhenomenons(final List<Phenomenon> phenomenons) throws DataStoreException {
        try(final Connection c = source.getConnection()) {
            for (Phenomenon phenomenon : phenomenons) {
                if (phenomenon instanceof InternalPhenomenon) {
                    final InternalPhenomenon internal = (InternalPhenomenon)phenomenon;
                    phenomenon = new PhenomenonType(internal.getName().getCode(), internal.getName().getCode());
                }
                final PhenomenonProperty phenomenonP = SOSXmlFactory.buildPhenomenonProperty("1.0.0", (org.geotoolkit.swe.xml.Phenomenon) phenomenon);
                writePhenomenon(phenomenonP, c, false);
            }
        } catch (SQLException ex) {
            throw new DataStoreException("Error while inserting phenomenons.", ex);
        }
    }
    
    private String writePhenomenon(final PhenomenonProperty phenomenonP, final Connection c, final boolean partial) throws SQLException {
        final String phenomenonId = getPhenomenonId(phenomenonP);
        if (phenomenonId == null) return null;
        
        try(final PreparedStatement stmtExist = c.prepareStatement("SELECT \"id\", \"partial\" FROM  \"" + schemaPrefix + "om\".\"observed_properties\" WHERE \"id\"=?")) {
            stmtExist.setString(1, phenomenonId);
            boolean exist = false;
            boolean isPartial = false;
            try(final ResultSet rs = stmtExist.executeQuery()) {
                if (rs.next()) {
                    isPartial = rs.getBoolean("partial");
                    exist = true;
                }
            }
            if (!exist) {
                try(final PreparedStatement stmtInsert = c.prepareStatement("INSERT INTO \"" + schemaPrefix + "om\".\"observed_properties\" VALUES(?,?)")) {
                    stmtInsert.setString(1, phenomenonId);
                    stmtInsert.setBoolean(2, partial);
                    stmtInsert.executeUpdate();
                }    
                if (phenomenonP.getPhenomenon() instanceof CompositePhenomenon) {
                    final CompositePhenomenon composite = (CompositePhenomenon) phenomenonP.getPhenomenon();
                    try(final PreparedStatement stmtInsertCompo = c.prepareStatement("INSERT INTO \"" + schemaPrefix + "om\".\"components\" VALUES(?,?)")) {
                        for (PhenomenonProperty child : composite.getRealComponent()) {
                            final String childID = getPhenomenonId(child);
                            writePhenomenon(child, c, false);
                            stmtInsertCompo.setString(1, phenomenonId);
                            stmtInsertCompo.setString(2, childID);
                            stmtInsertCompo.executeUpdate();
                        }
                    }
                }
            } else if (exist && isPartial) {
                try(final PreparedStatement stmtUpdate = c.prepareStatement("UPDATE \"" + schemaPrefix + "om\".\"observed_properties\" SET \"partial\" = ? WHERE \"id\"= ?")) {
                    stmtUpdate.setBoolean(1, false);
                    stmtUpdate.setString(2, phenomenonId);
                    stmtUpdate.executeUpdate();
                }
                if (phenomenonP.getPhenomenon() instanceof CompositePhenomenon) {
                    final CompositePhenomenon composite = (CompositePhenomenon) phenomenonP.getPhenomenon();
                    try(final PreparedStatement stmtInsertCompo = c.prepareStatement("INSERT INTO \"" + schemaPrefix + "om\".\"components\" VALUES(?,?)")) {
                        for (PhenomenonProperty child : composite.getRealComponent()) {
                            final String childID = getPhenomenonId(child);
                            writePhenomenon(child, c, false);
                            stmtInsertCompo.setString(1, phenomenonId);
                            stmtInsertCompo.setString(2, childID);
                            stmtInsertCompo.executeUpdate();
                        }
                    }
                }
            }
        }
        return phenomenonId;
    }
    
    private String getPhenomenonId(final PhenomenonProperty phenomenonP) {
        if (phenomenonP.getHref() != null) {
            return phenomenonP.getHref();
        }
        if (phenomenonP.getPhenomenon() != null) {
            org.geotoolkit.swe.xml.Phenomenon phen = phenomenonP.getPhenomenon();
            // TODO remove when the interface Phenomenon will have a getId() method
            if (phen instanceof  org.geotoolkit.swe.xml.v101.PhenomenonType) {
                String id = ((org.geotoolkit.swe.xml.v101.PhenomenonType)phen).getId();
                if (id != null) {
                    return id;
                }
            }
            if (phen.getName() != null) {
                return phen.getName().getCode();
            }
        }
        return null;
    }
    
    @Override
    public void writeProcedure(final String procedureID, final AbstractGeometry position, final String parent, final String type) throws DataStoreException {
        try(final Connection c = source.getConnection()) {
            writeProcedure(procedureID, position, parent, type, c);
        } catch (SQLException | FactoryException ex) {
            throw new DataStoreException("Error while inserting procedure.", ex);
        }
    }
    
    private int writeProcedure(final String procedureID,  final AbstractGeometry position, final String parent, final String type, final Connection c) throws SQLException, FactoryException, DataStoreException {
        int pid;
        try(final PreparedStatement stmtExist = c.prepareStatement("SELECT \"pid\" FROM \"" + schemaPrefix + "om\".\"procedures\" WHERE \"id\"=?")) {
            stmtExist.setString(1, procedureID);
            try(final ResultSet rs = stmtExist.executeQuery()) {
                if (!rs.next()) {
                    try(final Statement stmt = c.createStatement();
                        final ResultSet rs2 = stmt.executeQuery("SELECT max(\"pid\") FROM \"" + schemaPrefix + "om\".\"procedures\"")) {
                        pid = 0;
                        if (rs2.next()) {
                            pid = rs2.getInt(1) + 1;
                        }
                    }

                    try(final PreparedStatement stmtInsert = c.prepareStatement("INSERT INTO \"" + schemaPrefix + "om\".\"procedures\" VALUES(?,?,?,?,?,?)")) {
                        stmtInsert.setString(1, procedureID);
                         if (position != null) {
                            Geometry pt = GeometrytoJTS.toJTS(position, false);
                            int srid = pt.getSRID();
                            if (srid == 0) {
                                srid = 4326;
                            }
                            stmtInsert.setBytes(2, getGeometryBytes(pt));
                            stmtInsert.setInt(3, srid);
                        } else {
                            stmtInsert.setNull(2, java.sql.Types.BINARY);
                            stmtInsert.setNull(3, java.sql.Types.INTEGER);
                        }
                        stmtInsert.setInt(4, pid);
                        if (parent != null) {
                            stmtInsert.setString(5, parent);
                        } else {
                            stmtInsert.setNull(5, java.sql.Types.VARCHAR);
                        }
                        if (type != null) {
                            stmtInsert.setString(6, type);
                        } else {
                            stmtInsert.setNull(6, java.sql.Types.VARCHAR);
                        }
                        stmtInsert.executeUpdate();
                    }
                } else {
                    pid = rs.getInt(1);
                }
            }
        }
        return pid;
    }
    
    private byte[] getGeometryBytes(Geometry pt) throws DataStoreException {
        try {
            final WKBWriter writer = new WKBWriter();
            GeometryCSTransformer ts = new GeometryCSTransformer(new AbstractGeometryTransformer() {
                @Override
                public CoordinateSequence transform(CoordinateSequence cs, int i) throws TransformException {
                    for (int j = 0; j < cs.size(); j++) {
                        double x = cs.getX(j);
                        double y = cs.getY(j);
                        cs.setOrdinate(j, 0, y);
                        cs.setOrdinate(j, 1, x);
                    }
                    return cs;
                }
            });

            int srid = pt.getSRID();
            if (srid == 0) {
                srid = 4326;
            }                
            if (srid == 4326) {
                pt = ts.transform(pt);
            }
            return writer.write(pt);
        } catch (TransformException ex) {
            throw new DataStoreException(ex);
        }
    }
    
    
    private void writeFeatureOfInterest(final SamplingFeature foi, final Connection c) throws SQLException {
        try(final PreparedStatement stmtExist = c.prepareStatement("SELECT \"id\" FROM  \"" + schemaPrefix + "om\".\"sampling_features\" WHERE \"id\"=?")) {
            stmtExist.setString(1, foi.getId());
            try(final ResultSet rs = stmtExist.executeQuery()) {
                if (!rs.next()) {
                    try (final PreparedStatement stmtInsert = c.prepareStatement("INSERT INTO \"" + schemaPrefix + "om\".\"sampling_features\" VALUES(?,?,?,?,?,?)")) {
                        stmtInsert.setString(1, foi.getId());
                        stmtInsert.setString(2, (foi.getName() != null) ? foi.getName().getCode() : null);
                        stmtInsert.setString(3, foi.getDescription());
                        stmtInsert.setNull(4, java.sql.Types.VARCHAR); // TODO

                        if (foi.getGeometry() != null) {
                            try {
                                WKBWriter writer = new WKBWriter();
                                final Geometry geom = GeometrytoJTS.toJTS((AbstractGeometry) foi.getGeometry());
                                final int SRID = geom.getSRID();
                                stmtInsert.setBytes(5, writer.write(geom));
                                stmtInsert.setInt(6, SRID);
                            } catch (FactoryException | IllegalArgumentException ex) {
                                LOGGER.log(Level.WARNING, "unable to transform the geometry to JTS", ex);
                                stmtInsert.setNull(5, java.sql.Types.VARBINARY);
                                stmtInsert.setNull(6, java.sql.Types.INTEGER);
                            }
                        } else {
                            stmtInsert.setNull(5, java.sql.Types.VARBINARY);
                            stmtInsert.setNull(6, java.sql.Types.INTEGER);
                        }
                        stmtInsert.executeUpdate();
                    }
                }
            }
        }
    }
    
    private void writeResult(final int oid, final int pid, final String procedureID, final Object result, final TemporalObject samplingTime, final Connection c) throws SQLException, DataStoreException {
        if (result instanceof Measure || result instanceof org.apache.sis.internal.jaxb.gml.Measure) {
            Field singleField = new Field("Quantity", "value", null, null);
            buildMeasureTable(procedureID, pid, Arrays.asList(singleField), c);
            try(final PreparedStatement stmt = c.prepareStatement("INSERT INTO \"" + schemaPrefix + "mesures\".\"mesure" + pid + "\" VALUES(?,?,?)")) {
                double value;
                if (result instanceof org.apache.sis.internal.jaxb.gml.Measure) {
                    value = ((org.apache.sis.internal.jaxb.gml.Measure)result).value;
                } else {
                    value = ((Measure) result).getValue();
                }
                stmt.setInt(1, oid);
                stmt.setInt(2, 1);
                stmt.setDouble(3, value);
                stmt.executeUpdate();
            }
        } else if (result instanceof DataArrayProperty) {
            final DataArray array = ((DataArrayProperty) result).getDataArray();
            if (!(array.getEncoding() instanceof TextBlock)) {
                throw new DataStoreException("Only TextEncoding is supported");
            }
            final TextBlock encoding = (TextBlock) array.getEncoding();
            
            if (!(array.getPropertyElementType().getAbstractRecord() instanceof DataRecord) &&
                !(array.getPropertyElementType().getAbstractRecord() instanceof SimpleDataRecord)) {
                throw new DataStoreException("Only DataRecord/SimpleDataRecord is supported");
            }
            final List<Field> fields = getFieldList(array.getPropertyElementType().getAbstractRecord());
            buildMeasureTable(procedureID, pid, fields, c);
            final String values       = array.getValues();
            fillMesureTable(c, oid, pid, fields, values, encoding);
            
            /*if (lastDate != null) {
                final PreparedStatement stmt2 = c.prepareStatement("UPDATE \"om\".\"observations\" SET \"time_end\"=? WHERE \"id\"=?");
                stmt2.setTimestamp(1, lastDate);
                stmt2.setInt(2, oid);
                stmt2.executeUpdate();
            }*/
        } else if (result != null) {
            throw new DataStoreException("This type of resultat is not supported :" + result.getClass().getName());
        }
    }
    
    private void updateOrCreateOffering(final String procedureID, final TemporalObject samplingTime, final String phenoID, final String foiID, final Connection c) throws SQLException {
        final String offeringID;
        try(final PreparedStatement stmtExist = c.prepareStatement("SELECT * FROM  \"" + schemaPrefix + "om\".\"offerings\" WHERE \"procedure\"=?")) {
            stmtExist.setString(1, procedureID);
            try(final ResultSet rs = stmtExist.executeQuery()) {
                // INSERT
                if (!rs.next()) {
                    if (procedureID.startsWith(sensorIdBase)) {
                        offeringID  = "offering-" + procedureID.substring(sensorIdBase.length());
                    } else {
                        offeringID  = "offering-" + procedureID;
                    }
                    try(final PreparedStatement stmtInsert = c.prepareStatement("INSERT INTO \"" + schemaPrefix + "om\".\"offerings\" VALUES(?,?,?,?,?,?)")) {
                        stmtInsert.setString(1, offeringID);
                        stmtInsert.setString(2, "Offering for procedure:" + procedureID);
                        stmtInsert.setString(3, offeringID);
                        if (samplingTime instanceof Period) {
                            final Period period = (Period) samplingTime;
                            final Date beginDate = period.getBeginning().getDate();
                            final Date endDate = period.getEnding().getDate();
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
                            final Instant instant = (Instant) samplingTime;
                            final Date date = instant.getDate();
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
                    }

                    if (phenoID != null) {
                        try(final PreparedStatement stmtInsertOP = c.prepareStatement("INSERT INTO \"" + schemaPrefix + "om\".\"offering_observed_properties\" VALUES(?,?)")) {
                            stmtInsertOP.setString(1, offeringID);
                            stmtInsertOP.setString(2, phenoID);
                            stmtInsertOP.executeUpdate();
                        }
                    }

                    if (foiID != null) {
                        try(final PreparedStatement stmtInsertFOI = c.prepareStatement("INSERT INTO \"" + schemaPrefix + "om\".\"offering_foi\" VALUES(?,?)")) {
                            stmtInsertFOI.setString(1, offeringID);
                            stmtInsertFOI.setString(2, foiID);
                            stmtInsertFOI.executeUpdate();
                        }
                    }

                    // UPDATE
                } else {
                    offeringID = rs.getString(1);
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
                    final Timestamp timeEnd = rs.getTimestamp(5);
                    final long offEnd;
                    if (timeEnd != null) {
                        offEnd = timeEnd.getTime();
                    } else {
                        offEnd = -Long.MAX_VALUE;
                    }

                    if (samplingTime instanceof Period) {
                        final Period period = (Period) samplingTime;
                        final Date beginDate = period.getBeginning().getDate();
                        final Date endDate = period.getEnding().getDate();
                        if (beginDate != null) {
                            final long obsBeginTime = beginDate.getTime();
                            if (obsBeginTime < offBegin) {
                                try(final PreparedStatement beginStmt = c.prepareStatement("UPDATE \"" + schemaPrefix + "om\".\"offerings\" SET \"time_begin\"=?")) {
                                    beginStmt.setTimestamp(1, new Timestamp(obsBeginTime));
                                    beginStmt.executeUpdate();
                                }
                            }
                        }
                        if (endDate != null) {
                            final long obsEndTime = endDate.getTime();
                            if (obsEndTime > offEnd) {
                                try(final PreparedStatement endStmt = c.prepareStatement("UPDATE \"" + schemaPrefix + "om\".\"offerings\" SET \"time_end\"=?")) {
                                    endStmt.setTimestamp(1, new Timestamp(obsEndTime));
                                    endStmt.executeUpdate();
                                }
                            }
                        }
                    } else if (samplingTime instanceof Instant) {
                        final Instant instant = (Instant) samplingTime;
                        final Date date = instant.getDate();
                        if (date != null) {
                            final long obsTime = date.getTime();
                            if (obsTime < offBegin) {
                                try(final PreparedStatement beginStmt = c.prepareStatement("UPDATE \"" + schemaPrefix + "om\".\"offerings\" SET \"time_begin\"=?")) {
                                    beginStmt.setTimestamp(1, new Timestamp(obsTime));
                                    beginStmt.executeUpdate();
                                }
                            }
                            if (obsTime > offEnd) {
                                try(final PreparedStatement endStmt = c.prepareStatement("UPDATE \"" + schemaPrefix + "om\".\"offerings\" SET \"time_end\"=?")) {
                                    endStmt.setTimestamp(1, new Timestamp(obsTime));
                                    endStmt.executeUpdate();
                                }
                            }
                        }
                    }
            
                    /*
                     * Phenomenon
                     */
                    if (phenoID != null) {
                        try(final PreparedStatement phenoStmt = c.prepareStatement("SELECT \"phenomenon\" FROM  \"" + schemaPrefix + "om\".\"offering_observed_properties\" WHERE \"id_offering\"=? AND \"phenomenon\"=?")) {
                            phenoStmt.setString(1, offeringID);
                            phenoStmt.setString(2, phenoID);
                            try(final ResultSet rsp = phenoStmt.executeQuery()) {
                                if (!rsp.next()) {
                                    try(final PreparedStatement stmtInsert = c.prepareStatement("INSERT INTO \"" + schemaPrefix + "om\".\"offering_observed_properties\" VALUES(?,?)")) {
                                        stmtInsert.setString(1, offeringID);
                                        stmtInsert.setString(2, phenoID);
                                        stmtInsert.executeUpdate();
                                    }
                                }
                            }
                        }
                    }
                }
                /*
                 * Feature Of interest
                 */
                if (foiID != null) {
                    try(final PreparedStatement foiStmt = c.prepareStatement("SELECT \"foi\" FROM  \"" + schemaPrefix + "om\".\"offering_foi\" WHERE \"id_offering\"=? AND \"foi\"=?")) {
                        foiStmt.setString(1, offeringID);
                        foiStmt.setString(2, foiID);
                        try(final ResultSet rsf = foiStmt.executeQuery()) {
                            if (!rsf.next()) {
                                try(final PreparedStatement stmtInsert = c.prepareStatement("INSERT INTO \"" + schemaPrefix + "om\".\"offering_foi\" VALUES(?,?)")) {
                                    stmtInsert.setString(1, offeringID);
                                    stmtInsert.setString(2, foiID);
                                    stmtInsert.executeUpdate();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized String writeOffering(final ObservationOffering offering) throws DataStoreException {
        try(final Connection c           = source.getConnection();
             final PreparedStatement stmt = c.prepareStatement("INSERT INTO \"" + schemaPrefix + "om\".\"offerings\" VALUES(?,?,?,?,?,?)")) {
            stmt.setString(1, offering.getId());
            stmt.setString(2, offering.getDescription());
            stmt.setString(3, (offering.getName() != null) ? offering.getName().getCode() : null);
            if (offering.getTime() instanceof Period) {
                final Period period = (Period)offering.getTime();
                if (period.getBeginning() != null && period.getBeginning().getDate() != null) {
                    stmt.setTimestamp(4, new Timestamp(period.getBeginning().getDate().getTime()));
                } else {
                    stmt.setNull(4, java.sql.Types.TIMESTAMP);
                }
                if (period.getEnding() != null && period.getEnding().getDate() != null) {
                    stmt.setTimestamp(5, new Timestamp(period.getEnding().getDate().getTime()));
                } else {
                    stmt.setNull(5, java.sql.Types.TIMESTAMP);
                }
            } else if (offering.getTime() instanceof Instant) {
                final Instant instant = (Instant)offering.getTime();
                if (instant != null && instant.getDate() != null) {
                    stmt.setTimestamp(4, new Timestamp(instant.getDate().getTime()));
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

            try(final PreparedStatement opstmt = c.prepareStatement("INSERT INTO \"" + schemaPrefix + "om\".\"offering_observed_properties\" VALUES(?,?)")) {
                for (String op : offering.getObservedProperties()) {
                    if (op != null) {
                        opstmt.setString(1, offering.getId());
                        opstmt.setString(2, op);
                        opstmt.executeUpdate();
                    }
                }
            }

            try(final PreparedStatement foistmt = c.prepareStatement("INSERT INTO \"" + schemaPrefix + "om\".\"offering_foi\" VALUES(?,?)")) {
                for (String foi : offering.getFeatureOfInterestIds()) {
                    if (foi != null) {
                        foistmt.setString(1, offering.getId());
                        foistmt.setString(2, foi);
                        foistmt.executeUpdate();
                    }
                }
            }

            return offering.getId();
        } catch (SQLException ex) {
            throw new DataStoreException("Error while inserting offering.", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void updateOffering(final String offeringID, final String offProc, final List<String> offPheno, final String offSF) throws DataStoreException {
        try(final Connection c = source.getConnection()) {
            if (offProc != null) {
                // single pricedure in v2.0.0
            }
            if (offPheno != null) {
                try(final PreparedStatement opstmt = c.prepareStatement("INSERT INTO \"" + schemaPrefix + "om\".\"offering_observed_properties\" VALUES(?,?)")) {
                    for (String op : offPheno) {
                        opstmt.setString(1, offeringID);
                        opstmt.setString(2, op);
                        opstmt.executeUpdate();
                    }
                }
            }
            if (offSF != null) {
                try(final PreparedStatement foistmt = c.prepareStatement("INSERT INTO \"" + schemaPrefix + "om\".\"offering_foi\" VALUES(?,?)")) {
                    foistmt.setString(1, offeringID);
                    foistmt.setString(2, offSF);
                    foistmt.executeUpdate();
                }
            }
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
    public synchronized void recordProcedureLocation(final String physicalID, final AbstractGeometry position) throws DataStoreException {
        if (position != null) {
            try(final Connection c     = source.getConnection();
                PreparedStatement ps   = c.prepareStatement("UPDATE \"" + schemaPrefix + "om\".\"procedures\" SET \"shape\"=?, \"crs\"=? WHERE id=?")) {
                ps.setString(3, physicalID);
                Geometry pt = GeometrytoJTS.toJTS(position, false);

                int srid = pt.getSRID();
                if (srid == 0) {
                    srid = 4326;
                }                
                ps.setBytes(1, getGeometryBytes(pt));
                ps.setInt(2, srid);
                ps.execute();
            } catch (SQLException | FactoryException e) {
                throw new DataStoreException(e.getMessage(), e);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    private int getNewObservationId(Connection c) throws DataStoreException {
        try(final Statement stmt       = c.createStatement();
            final ResultSet rs         = stmt.executeQuery("SELECT max(\"id\") FROM \"" + schemaPrefix + "om\".\"observations\"")) {
            int resultNum;
            if (rs.next()) {
                resultNum = rs.getInt(1) + 1;
            } else {
                resultNum = 1;
            }
            return resultNum;
        } catch (SQLException ex) {
            throw new DataStoreException("Error while looking for available observation id.", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void removeObservationForProcedure(final String procedureID) throws DataStoreException {
        try(final Connection c = source.getConnection()) {
            final int pid = getPIDFromProcedure(procedureID, c);
            if (pid == -1) {
                LOGGER.log(Level.FINE, "Unable to find a procedure:{0}", procedureID);
                return;
            }
            
            boolean mesureTableExist = true;
            try(final PreparedStatement stmtExist  = c.prepareStatement("SELECT COUNT(id) FROM \"" + schemaPrefix + "mesures\".\"mesure" + pid + "\"")) {
                stmtExist.executeQuery();
            } catch (SQLException ex) {
                LOGGER.log(Level.WARNING, "no measure table mesure{0} exist.", pid);
                mesureTableExist = false;
            }
            
            //NEW
            try(final PreparedStatement stmtMes  = c.prepareStatement("DELETE FROM \"" + schemaPrefix + "mesures\".\"mesure" + pid + "\" WHERE \"id_observation\" IN (SELECT \"id\" FROM \"" + schemaPrefix + "om\".\"observations\" WHERE \"procedure\"=?)");
                final PreparedStatement stmtObs  = c.prepareStatement("DELETE FROM \"" + schemaPrefix + "om\".\"observations\" WHERE \"procedure\"=?")) {
                
                if (mesureTableExist) {
                    stmtMes.setString(1, procedureID);
                    stmtMes.executeUpdate();
                }

                stmtObs.setString(1, procedureID);
                stmtObs.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new DataStoreException("Error while removing observation for procedure.", ex);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void removeProcedure(final String procedureID) throws DataStoreException {
        try {
            removeObservationForProcedure(procedureID);
            try(final Connection c = source.getConnection()) {
                final int pid = getPIDFromProcedure(procedureID, c);

                try (final PreparedStatement stmtObsP = c.prepareStatement("DELETE FROM \"" + schemaPrefix + "om\".\"offering_observed_properties\" "
                        + "WHERE \"id_offering\" IN(SELECT \"identifier\" FROM \"" + schemaPrefix + "om\".\"offerings\" WHERE \"procedure\"=?)");
                     final PreparedStatement stmtFoi = c.prepareStatement("DELETE FROM \"" + schemaPrefix + "om\".\"offering_foi\" "
                             + "WHERE \"id_offering\" IN(SELECT \"identifier\" FROM \"" + schemaPrefix + "om\".\"offerings\" WHERE \"procedure\"=?)");
                     final PreparedStatement stmtMes = c.prepareStatement("DELETE FROM \"" + schemaPrefix + "om\".\"offerings\" WHERE \"procedure\"=?");
                     final PreparedStatement stmtObs = c.prepareStatement("DELETE FROM \"" + schemaPrefix + "om\".\"procedures\" WHERE \"id\"=?");
                     final PreparedStatement stmtProcDesc = c.prepareStatement("DELETE FROM \"" + schemaPrefix + "om\".\"procedure_descriptions\" WHERE \"procedure\"=?")) {

                    stmtObsP.setString(1, procedureID);
                    stmtObsP.executeUpdate();

                    stmtFoi.setString(1, procedureID);
                    stmtFoi.executeUpdate();

                    stmtMes.setString(1, procedureID);
                    stmtMes.executeUpdate();

                    stmtProcDesc.setString(1, procedureID);
                    stmtProcDesc.executeUpdate();

                    stmtObs.setString(1, procedureID);
                    stmtObs.executeUpdate();
                }

                // remove measure table
                if (pid == -1) {
                    LOGGER.log(Level.FINE, "Unable to find a procedure:{0}", procedureID);
                    return;
                }

                try (final Statement stmtDrop = c.createStatement()) {
                    stmtDrop.executeUpdate("DROP TABLE \"mesures\".\"mesure" + pid + "\"");
                }  catch (SQLException ex) {
                    // it happen that the table does not exist
                    LOGGER.log(Level.WARNING, "Unable to remove measure table.{0}", ex.getMessage());
                }

                //look for unused observed properties (execute the statement 2 times for remaining components)
                try (final Statement stmtOP = c.createStatement()) {
                    for (int i = 0; i < 2; i++) {
                        try (final ResultSet rs = stmtOP.executeQuery(" SELECT \"id\" FROM \"" + schemaPrefix + "om\".\"observed_properties\""
                                + " WHERE  \"id\" NOT IN (SELECT DISTINCT \"observed_property\" FROM \"" + schemaPrefix + "om\".\"observations\") "
                                + " AND    \"id\" NOT IN (SELECT DISTINCT \"phenomenon\"        FROM \"" + schemaPrefix + "om\".\"offering_observed_properties\")"
                                + " AND    \"id\" NOT IN (SELECT DISTINCT \"component\"         FROM \"" + schemaPrefix + "om\".\"components\")")) {
                            while (rs.next()) {
                                final String key = encodeQuote(rs.getString(1));
                                stmtOP.addBatch("DELETE FROM \"" + schemaPrefix + "om\".\"components\" WHERE \"phenomenon\"='" + key + "';");
                                stmtOP.addBatch("DELETE FROM \"" + schemaPrefix + "om\".\"observed_properties\" WHERE \"id\"='" + key + "';");
                            }
                        }
                        stmtOP.executeBatch();
                    }

                    //look for unused foi
                    try(final Statement stmtFOI = c.createStatement();
                    final ResultSet rs2 = stmtFOI.executeQuery(" SELECT \"id\" FROM \"" + schemaPrefix + "om\".\"sampling_features\""
                            + " WHERE  \"id\" NOT IN (SELECT DISTINCT \"foi\" FROM \"" + schemaPrefix + "om\".\"observations\") " +
                            " AND    \"id\" NOT IN (SELECT DISTINCT \"foi\" FROM \"" + schemaPrefix + "om\".\"offering_foi\")")) {

                        while (rs2.next()) {
                            stmtFOI.addBatch("DELETE FROM \"" + schemaPrefix + "om\".\"sampling_features\" WHERE \"id\"='" + encodeQuote(rs2.getString(1)) + "';");
                        }
                        stmtFOI.executeBatch();
                    }
                }
            }
        } catch (SQLException ex) {
            throw new DataStoreException("Error while removing procedure.", ex);
        }
    }

    /**
     * Encode quotes in the given string, to make it work with SQL syntax.
     *
     * @param original Original string. May contain simple quote. Should not be {@code null}.
     * @return The original string with quotes encoded. Never {@code null}.
     */
    private String encodeQuote(final String original) {
        return original.replace("'", "''");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void removeObservation(final String observationID) throws DataStoreException {
        try(final Connection c              = source.getConnection()) {
            final int pid                   = getPIDFromObservation(observationID, c);
            try(final PreparedStatement stmtMes = c.prepareStatement("DELETE FROM \"" + schemaPrefix + "mesures\".\"mesure" + pid + "\" WHERE id_observation IN (SELECT \"id\" FROM \"" + schemaPrefix + "om\".\"observations\" WHERE identifier=?)");
            final PreparedStatement stmtObs = c.prepareStatement("DELETE FROM \"" + schemaPrefix + "om\".\"observations\" WHERE identifier=?")) {
                stmtMes.setString(1, observationID);
                stmtMes.executeUpdate();

                stmtObs.setString(1, observationID);
                stmtObs.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new DataStoreException("Error while inserting observation.", ex);
        }
    }
    
    private boolean measureTableExist(final int pid) throws SQLException {
        final String tableName = "mesure" + pid;
        boolean exist = false;
        try(final Connection c = source.getConnection()) {
            try(final Statement stmt = c.createStatement()) {
                try(final ResultSet rs = stmt.executeQuery("SELECT \"id\" FROM \"" + schemaPrefix + "mesures\".\"" + tableName + "\"")) {
                    exist = true;
                }
            } catch (SQLException ex) {
                LOGGER.log(Level.FINER, "Error while looking for measure table existence (normal error if table does not exist).", ex);
            }
        }
        return exist;
    }
    
    private void buildMeasureTable(final String procedureID, final int pid, final List<Field> fields, final Connection c) throws SQLException {
        
        
        final String tableName = "mesure" + pid;
        //look for existence
        final boolean exist = measureTableExist(pid);
        if (!exist) {

            // Build measure table
            final StringBuilder sb = new StringBuilder("CREATE TABLE \"" + schemaPrefix + "mesures\".\"" + tableName + "\"("
                                                     + "\"id_observation\" integer NOT NULL,"
                                                     + "\"id\"             integer NOT NULL,");
            for (Field field : fields) {
                sb.append('"').append(field.fieldName).append("\" ").append(field.getSQLType(isPostgres)).append(",");
            }
            sb.setCharAt(sb.length() - 1, ' ');
            sb.append(");");
            try(final Statement stmt = c.createStatement()) {
                stmt.executeUpdate(sb.toString());
                stmt.executeUpdate("ALTER TABLE \"" + schemaPrefix + "mesures\".\"" + tableName + "\" ADD CONSTRAINT " + tableName + "_pk PRIMARY KEY (\"id_observation\", \"id\")");
                stmt.executeUpdate("ALTER TABLE \"" + schemaPrefix + "mesures\".\"" + tableName + "\" ADD CONSTRAINT " + tableName + "_obs_fk FOREIGN KEY (\"id_observation\") REFERENCES \"" + schemaPrefix + "om\".\"observations\"(\"id\")");
            }

            //fill procedure_descriptions table
            insertField(procedureID, fields, 1, c);
            
        } else if (allowSensorStructureUpdate) {
            final List<Field> oldfields = readFields(procedureID, c);
            final List<Field> newfields = new ArrayList<>();
            for (Field field : fields) {
                if (!oldfields.contains(field)) {
                    newfields.add(field);
                }
            }
            // Update measure table
            try (Statement addColumnStmt = c.createStatement()) {
                for (Field newField : newfields) {
                    StringBuilder sb = new StringBuilder("ALTER TABLE \"" + schemaPrefix + "mesures\".\"" + tableName + "\" ADD \"" + newField.fieldName + "\" ");
                    sb.append(newField.getSQLType(isPostgres)).append(';');
                    addColumnStmt.execute(sb.toString());
                }
            }
            
            //fill procedure_descriptions table
            insertField(procedureID, newfields, oldfields.size() + 1, c);
        }
    }
    
    private void insertField(String procedureID, List<Field> fields, int offset, final Connection c) throws SQLException {
        
        try (final PreparedStatement insertFieldStmt = c.prepareStatement("INSERT INTO \"" + schemaPrefix + "om\".\"procedure_descriptions\" VALUES (?,?,?,?,?,?)")) {
            for (Field field : fields) {
                insertFieldStmt.setString(1, procedureID);
                insertFieldStmt.setInt(2, offset);
                insertFieldStmt.setString(3, field.fieldName);
                insertFieldStmt.setString(4, field.fieldType);
                if (field.fieldDesc != null) {
                    insertFieldStmt.setString(5, field.fieldDesc);
                } else {
                    insertFieldStmt.setNull(5, java.sql.Types.VARCHAR);
                }
                if (field.fieldUom != null) {
                    insertFieldStmt.setString(6, field.fieldUom);
                } else {
                    insertFieldStmt.setNull(6, java.sql.Types.VARCHAR);
                }
                insertFieldStmt.executeUpdate();
                offset++;
            }
        }
    }
    
    private List<Field> getFieldList(AbstractDataRecord abstractRecord) throws SQLException {
        final List<Field> fields = new ArrayList<>();
        final Collection recordField;
        if (abstractRecord instanceof DataRecord) {
            final DataRecord record = (DataRecord) abstractRecord;
            recordField =  record.getField();
        } else {
            final SimpleDataRecord record = (SimpleDataRecord) abstractRecord;
            recordField =  record.getField();
        }
        
        for (Object field : recordField) {
            String name;
            AbstractDataComponent value;
            if (field instanceof AnyScalar) {
                name  = ((AnyScalar)field).getName();
                value = ((AnyScalar)field).getValue();
            } else if (field instanceof DataComponentProperty) {
                name  = ((DataComponentProperty)field).getName();
                value = ((DataComponentProperty)field).getValue();
            } else {
                throw new SQLException("Unexpected field type:" + field.getClass());
            }
            fields.add(new Field(name, value));
        }
        return fields;
    }
    
    private void fillMesureTable(final Connection c, final int oid, final int pid, final List<Field> fields, final String values, final TextBlock encoding ) throws SQLException {
        final String tableName = "mesure" + pid;
        final StringTokenizer tokenizer = new StringTokenizer(values, encoding.getBlockSeparator());
        int n = 1;
        int sqlCpt = 0;
        try(final Statement stmtSQL = c.createStatement()) {
            StringBuilder sql = new StringBuilder("INSERT INTO \"" + schemaPrefix + "mesures\".\"" + tableName + "\" (\"id_observation\", \"id\", ");
            for (Field field : fields) {
                sql.append('"').append(field.fieldName).append("\",");
            }
            sql.setCharAt(sql.length() - 1, ' ');
            sql.append(") VALUES ");
            while (tokenizer.hasMoreTokens()) {
                String block = tokenizer.nextToken();
                block = block.trim();
                if (block.isEmpty()) {
                    continue;
                }

                sql.append('(').append(oid).append(',').append(n).append(',');
                for (int i = 0; i < fields.size(); i++) {
                    final Field field = fields.get(i);
                    String value;
                    if (i == fields.size() - 1) {
                        value = block;
                    } else {
                        int separator = block.indexOf(encoding.getTokenSeparator());
                        if (separator != -1) {
                            value = block.substring(0, separator);
                            block = block.substring(separator + 1);
                        } else {
                            throw new SQLException("Bad encoding for datablock, unable to find the token separator:" + encoding.getTokenSeparator() + "in the block.");
                        }
                    }

                    //format time
                    if (field.fieldType.equals("Time") && value != null && !value.isEmpty()) {
                        try {
                            value = value.trim();
                            final long millis = new ISODateParser().parseToMillis(value);
                            value = "'" + new Timestamp(millis).toString() + "'";
                        } catch (IllegalArgumentException ex) {
                            throw new SQLException("Bad format of timestamp for:" + value);
                        }
                    } else if (field.fieldType.equals("Text")) {
                        value = "'" + value + "'";
                    }

                    if (value != null && !value.isEmpty()) {
                        sql.append(value).append(",");
                    } else {
                        sql.append("NULL,");
                    }
                }
                sql.setCharAt(sql.length() - 1, ' ');
                sql.append("),\n");
                n++;
                sqlCpt++;
                if (sqlCpt > 99) {
                    sql.setCharAt(sql.length() - 2, ' ');
                    if (isPostgres) {
                        sql.setCharAt(sql.length() - 1, ';');
                    } else {
                        sql.setCharAt(sql.length() - 1, ' ');
                    }

                    stmtSQL.addBatch(sql.toString());
                    sqlCpt = 0;
                    sql = new StringBuilder("INSERT INTO \"" + schemaPrefix + "mesures\".\"" + tableName + "\" (\"id_observation\", \"id\", ");
                    for (Field field : fields) {
                        sql.append('"').append(field.fieldName).append("\",");
                    }
                    sql.setCharAt(sql.length() - 1, ' ');
                    sql.append(") VALUES ");
                }
            }
            if (sqlCpt > 0) {
                sql.setCharAt(sql.length() - 2, ' ');
                if (isPostgres) {
                    sql.setCharAt(sql.length() - 1, ';');
                } else {
                    sql.setCharAt(sql.length() - 1, ' ');
                }
                stmtSQL.addBatch(sql.toString());
            }
            stmtSQL.executeBatch();

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
}
