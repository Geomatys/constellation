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

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Logger;
import javax.sql.DataSource;

// JTS dependencies
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.WKBWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.StringTokenizer;

// constellation dependencies
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.sos.factory.OMFactory;
import org.constellation.sos.io.ObservationWriter;
import org.constellation.ws.CstlServiceException;

// Geotoolkit dependencies
import org.geotoolkit.util.logging.Logging;
import org.geotoolkit.gml.xml.DirectPosition;
import org.geotoolkit.gml.GeometrytoJTS;
import org.geotoolkit.sos.xml.ObservationOffering;
import org.geotoolkit.sos.xml.SOSXmlFactory;
import org.geotoolkit.swe.xml.Phenomenon;
import org.geotoolkit.swes.xml.ObservationTemplate;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import org.geotoolkit.swe.xml.AnyScalar;
import org.geotoolkit.swe.xml.DataArray;
import org.geotoolkit.swe.xml.DataArrayProperty;
import org.geotoolkit.swe.xml.DataComponentProperty;
import org.geotoolkit.swe.xml.DataRecord;
import org.geotoolkit.swe.xml.Quantity;
import org.geotoolkit.swe.xml.SimpleDataRecord;
import org.geotoolkit.swe.xml.TextBlock;
import org.opengis.observation.AnyFeature;
import org.opengis.observation.Measure;

// GeoAPI dependencies
import org.opengis.observation.Observation;
import org.opengis.temporal.Instant;
import org.opengis.temporal.Period;
import org.opengis.util.FactoryException;



/**
 * Default Observation reader for Postgrid O&M database.
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
    
    protected String observationIdBase;

    /**
     * Build a new Observation writer for postgrid dataSource.
     *
     * @param configuration
     *
     * @throws org.constellation.ws.CstlServiceException
     */
    public OM2ObservationWriter(final Automatic configuration, final Map<String, Object> properties) throws CstlServiceException {
        this.observationIdBase = (String) properties.get(OMFactory.OBSERVATION_ID_BASE);
        
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
            return writeObservation(template.getObservation());
        }
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String writeObservation(final Observation observation) throws CstlServiceException {
        try {
            final Connection c           = source.getConnection();
            final PreparedStatement stmt = c.prepareStatement("INSERT INTO \"om\".\"observations\" VALUES(?,?,?,?,?,?)");
            final int oid = Integer.parseInt(observation.getName().substring(observationIdBase.length()));
            stmt.setInt(1, oid);
            
            if (observation.getSamplingTime() instanceof Period) {
                final Period period = (Period)observation.getSamplingTime();
                if (period.getBeginning().getPosition().getDate() != null) {
                    stmt.setTimestamp(2, new Timestamp(period.getBeginning().getPosition().getDate().getTime()));
                } else {
                    stmt.setNull(2, java.sql.Types.TIMESTAMP);
                }
                if (period.getEnding().getPosition().getDate() != null) {
                    stmt.setTimestamp(3, new Timestamp(period.getEnding().getPosition().getDate().getTime()));
                } else {
                    stmt.setNull(3, java.sql.Types.TIMESTAMP);
                }
            } else if (observation.getSamplingTime() instanceof Instant) {
                final Instant instant = (Instant)observation.getSamplingTime();
                if (instant.getPosition().getDate() != null) {
                    stmt.setTimestamp(2, new Timestamp(instant.getPosition().getDate().getTime()));
                } else {
                    stmt.setNull(2, java.sql.Types.TIMESTAMP);
                }
                stmt.setNull(3, java.sql.Types.TIMESTAMP);
            } else {
                stmt.setNull(2, java.sql.Types.TIMESTAMP);
                stmt.setNull(3, java.sql.Types.TIMESTAMP);
            }
            
            stmt.setString(4, ((Phenomenon)observation.getObservedProperty()).getName());
            stmt.setString(5, ((org.geotoolkit.observation.xml.Process)observation.getProcedure()).getHref());
            if (observation.getFeatureOfInterest() != null) {
                stmt.setString(6, ((org.geotoolkit.sampling.xml.SamplingFeature)observation.getFeatureOfInterest()).getId());
            } else {
                stmt.setNull(6, java.sql.Types.VARCHAR);
            }
            writeFeatureOfInterest(observation.getFeatureOfInterest(), c);
            
            stmt.executeUpdate();
            stmt.close();
            
            writeResult(oid, observation.getResult(), c);
            
            c.close();
            return observation.getName();
        } catch (SQLException ex) {
            throw new CstlServiceException("Error while inserting observation.", ex, NO_APPLICABLE_CODE);
        }
    }
    
    private void writeFeatureOfInterest(final AnyFeature foi, final Connection c) throws CstlServiceException {
        
    }
    
    private void writeResult(final int oid, final Object result, final Connection c) throws SQLException, CstlServiceException {
        final PreparedStatement stmt = c.prepareStatement("INSERT INTO \"om\".\"mesures\" VALUES(?,?,?,?,?,?,?,?)");
        if (result instanceof Measure) {
            final Measure measure = (Measure) result;
            stmt.setInt(1, oid);
            stmt.setInt(2, 1);
            stmt.setNull(3, java.sql.Types.TIMESTAMP);
            stmt.setDouble(4, measure.getValue());
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
            final List<Field> fields = new ArrayList<Field>();
            if (array.getPropertyElementType().getAbstractRecord() instanceof DataRecord) {
                final DataRecord record = (DataRecord)array.getPropertyElementType().getAbstractRecord();
                for (DataComponentProperty field : record.getField()) {
                    if (field.getName().equalsIgnoreCase("Time")) {
                        continue;
                    } else {
                        final String uom;
                        final String desc;
                        if (field.getValue() instanceof Quantity) {
                            final Quantity q = (Quantity)field.getValue();
                            uom  = q.getUom().getCode();
                            desc = q.getDefinition();
                        } else {
                            throw new CstlServiceException("Only Quantity is supported for now");
                        }
                        fields.add(new Field("Quantity", field.getName(), desc, uom));
                    }
                }
            } else {
                final SimpleDataRecord record = (SimpleDataRecord)array.getPropertyElementType().getAbstractRecord();
                for (AnyScalar field : record.getField()) {
                    if (field.getName().equalsIgnoreCase("Time")) {
                        continue;
                    } else {
                        final String uom;
                        final String desc;
                        if (field.getValue() instanceof Quantity) {
                            final Quantity q = (Quantity)field.getValue();
                            uom  = q.getUom().getCode();
                            desc = q.getDefinition();
                        } else {
                            throw new CstlServiceException("Only Quantity is supported for now");
                        }
                        fields.add(new Field("Quantity", field.getName(), desc, uom));
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
                final String time = block.substring(0, tokenIndex).replace('T', ' ');
                final Timestamp realTime;
                try {
                    realTime = Timestamp.valueOf(time);
                } catch (IllegalArgumentException ex) {
                    throw new CstlServiceException("Bad format of timestamp for:" + time);
                }
                block = block.substring(tokenIndex + 1);
                for (int i = 0; i < fields.size(); i++) {
                    Double value;
                    if (i == fields.size() - 1) {
                        try {
                            value = Double.parseDouble(block);
                        } catch (NumberFormatException ex) {
                            throw new CstlServiceException("Bad format of double for:" + block);
                        }
                        lastDate  = realTime;
                    } else {
                        final String tmp = block.substring(0, block.indexOf(encoding.getTokenSeparator()));
                        try {
                            value = Double.parseDouble(tmp);
                        } catch (NumberFormatException ex) {
                            throw new CstlServiceException("Bad format of double for:" + tmp);
                        }
                    }
                    stmt.setInt(1, oid);
                    stmt.setInt(2, n);
                    stmt.setTimestamp(3, realTime);
                    stmt.setDouble(4, value);
                    stmt.setString(5, fields.get(i).fieldUom);
                    stmt.setString(6, fields.get(i).fieldType);
                    stmt.setString(7, fields.get(i).fieldName);
                    stmt.setString(8, fields.get(i).fieldDesc);
                    stmt.executeUpdate();
                }
                n++;
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
                stmt.setTimestamp(4, new Timestamp(period.getBeginning().getPosition().getDate().getTime()));
                stmt.setTimestamp(5, new Timestamp(period.getEnding().getPosition().getDate().getTime()));
            } else if (offering.getTime() instanceof Instant) {
                final Instant instant = (Instant)offering.getTime();
                stmt.setTimestamp(4, new Timestamp(instant.getPosition().getDate().getTime()));
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
            throw new CstlServiceException("Error while retrieving offering names.", ex, NO_APPLICABLE_CODE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateOffering(final String offeringId, final String offProc, final List<String> offPheno, final String offSF) throws CstlServiceException {
        try {
            final Connection c           = source.getConnection();
            if (offProc != null) {
                // single pricedure in v2.0.0
            }
            if (offPheno != null) {
                final PreparedStatement opstmt = c.prepareStatement("INSERT INTO \"om\".\"offering_observed_properties\" VALUES(?,?)");
                for (String op : offPheno) {
                    opstmt.setString(1, offeringId);
                    opstmt.setString(2, op);
                    opstmt.executeUpdate();
                }
                opstmt.close();
            }
            if (offSF != null) {
                final PreparedStatement foistmt = c.prepareStatement("INSERT INTO \"om\".\"offering_foi\" VALUES(?,?)");
                foistmt.setString(1, offeringId);
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
        } catch (SQLException e) {
            throw new CstlServiceException(e.getMessage(), e, NO_APPLICABLE_CODE);
        } catch (FactoryException e) {
            throw new CstlServiceException(e.getMessage(), e, NO_APPLICABLE_CODE);
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
