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

package org.constellation.data.om2;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKBWriter;
import java.io.IOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotoolkit.data.AbstractFeatureStore;
import org.geotoolkit.data.FeatureStoreFactory;
import org.geotoolkit.data.FeatureStoreFinder;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.logging.Logging;
import static org.constellation.data.om2.OM2FeatureStoreFactory.SGBDTYPE;
import org.geotoolkit.data.FeatureStoreRuntimeException;
import org.geotoolkit.data.FeatureReader;
import org.geotoolkit.data.FeatureWriter;
import org.geotoolkit.data.query.DefaultQueryCapabilities;
import org.geotoolkit.data.query.Query;
import org.geotoolkit.data.query.QueryCapabilities;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.feature.FeatureTypeBuilder;
import org.geotoolkit.feature.LenientFeatureFactory;
import org.geotoolkit.feature.simple.DefaultSimpleFeatureType;
import org.geotoolkit.feature.type.DefaultGeometryDescriptor;
import org.geotoolkit.filter.identity.DefaultFeatureId;
import org.geotoolkit.jdbc.ManageableDataSource;
import org.geotoolkit.parameter.Parameters;
import org.geotoolkit.referencing.CRS;

import org.opengis.feature.Feature;
import org.opengis.feature.FeatureFactory;
import org.opengis.feature.Property;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.identity.FeatureId;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.FactoryException;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class OM2FeatureStore extends AbstractFeatureStore {
    /** the feature factory */
    private static final FeatureFactory FF = FactoryFinder.getFeatureFactory(
                        new Hints(Hints.FEATURE_FACTORY,LenientFeatureFactory.class));

    private static final String CSTL_NAMESPACE = "http://constellation.org/om2";
    private static final Name CSTL_TN_SENSOR = new DefaultName(CSTL_NAMESPACE, "Sensor");
    protected static final Name ATT_POSITION = new DefaultName(CSTL_NAMESPACE,  "position");

    private static final QueryCapabilities capabilities = new DefaultQueryCapabilities(false);

    private final Map<Name, FeatureType> types = new HashMap<>();

    private final ManageableDataSource source;

    private static final String SQL_ALL_PROCEDURE = "SELECT * FROM \"om\".\"procedures\"";
    private static final String SQL_ALL_PROCEDURE_PG = "SELECT  \"id\", \"postgis\".st_asBinary(\"shape\"), \"crs\" FROM \"om\".\"procedures\"";
    private static final String SQL_ALL_ID = "SELECT \"id\" FROM \"om\".\"procedures\" ORDER BY \"id\" ASC";
    private static final String SQL_WRITE_PROCEDURE = "INSERT INTO \"om\".\"procedures\" VALUES(?,?,?)";
    private static final String SQL_DELETE_PROCEDURE = "DELETE FROM \"om\".\"procedures\" WHERE \"id\" = ?";

    private final String sensorIdBase = "urn:ogc:object:sensor:GEOM:"; // TODO

    private final boolean isPostgres;

    public OM2FeatureStore(final ParameterValueGroup params, final ManageableDataSource source) {
        super(params);
        this.source = source;
        Object sgbdtype = Parameters.value(SGBDTYPE, params);
        isPostgres = !("derby".equals(sgbdtype));
        initTypes();
    }

    @Override
    public FeatureStoreFactory getFactory() {
        return FeatureStoreFinder.getFactoryById(OM2FeatureStoreFactory.NAME);
    }

    private Connection getConnection() throws SQLException{
        return source.getConnection();
    }

    private void initTypes() {
        final FeatureTypeBuilder featureTypeBuilder = new FeatureTypeBuilder();
        featureTypeBuilder.setName(CSTL_TN_SENSOR);
        featureTypeBuilder.add(ATT_POSITION,Geometry.class,1,1,false,null);
        featureTypeBuilder.setDefaultGeometry(ATT_POSITION);
        types.put(CSTL_TN_SENSOR, featureTypeBuilder.buildFeatureType());
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public FeatureReader<FeatureType, Feature> getFeatureReader(final Query query) throws DataStoreException {
        final FeatureType sft = getFeatureType(query.getTypeName());
        try {
            return handleRemaining(new OMReader(sft), query);
        } catch (SQLException ex) {
            throw new DataStoreException(ex);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public FeatureWriter getFeatureWriterAppend(final Name typeName, final Hints hints) throws DataStoreException {
        return handleWriterAppend(typeName,hints);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public FeatureWriter getFeatureWriter(final Name typeName, final Filter filter, final Hints hints) throws DataStoreException {
        final FeatureType sft = getFeatureType(typeName);
        try {
            return handleRemaining(new OMWriter(sft), filter);
        } catch (SQLException ex) {
            throw new DataStoreException(ex);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void close() throws DataStoreException {
        super.close();
        try {
            source.close();
        } catch (SQLException ex) {
            getLogger().info("SQL Exception while closing O&M2 datastore");
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Set<Name> getNames() throws DataStoreException {
        return types.keySet();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public FeatureType getFeatureType(final Name typeName) throws DataStoreException {
        typeCheck(typeName);
        return types.get(typeName);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public QueryCapabilities getQueryCapabilities() {
        return capabilities;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public List<FeatureId> addFeatures(final Name groupName, final Collection<? extends Feature> newFeatures, 
            final Hints hints) throws DataStoreException {
        final FeatureType featureType = getFeatureType(groupName); //raise an error if type doesn't exist
        final List<FeatureId> result = new ArrayList<>();


        Connection cnx = null;
        PreparedStatement stmtWrite = null;
        try {
            cnx = getConnection();
            stmtWrite = cnx.prepareStatement(SQL_WRITE_PROCEDURE);

            for(final Feature feature : newFeatures) {
                FeatureId identifier = feature.getIdentifier();
                if (identifier == null || identifier.getID().isEmpty()) {
                    identifier = getNewFeatureId();
                }


                stmtWrite.setString(1, identifier.getID());
                final Object geometry = feature.getDefaultGeometryProperty().getValue();
                if (geometry instanceof Geometry) {
                    final Geometry geom = (Geometry) geometry;
                    final WKBWriter writer = new WKBWriter();
                    final int SRID = geom.getSRID();
                    stmtWrite.setBytes(2, writer.write(geom));
                    stmtWrite.setInt(3, SRID);
                    
                } else {
                    stmtWrite.setNull(2, Types.VARCHAR);
                    stmtWrite.setNull(3, Types.INTEGER);
                    
                }
                stmtWrite.executeUpdate();
                result.add(identifier);
            }
        } catch (SQLException ex) {
            getLogger().log(Level.WARNING, SQL_WRITE_PROCEDURE, ex);
        }finally{
            if(stmtWrite != null){
                try {
                    stmtWrite.close();
                } catch (SQLException ex) {
                    getLogger().log(Level.WARNING, null, ex);
                }
            }

            if(cnx != null){
                try {
                    cnx.close();
                } catch (SQLException ex) {
                    getLogger().log(Level.WARNING, null, ex);
                }
            }
        }

        return result;
    }

    public FeatureId getNewFeatureId() {
        Connection cnx = null;
        PreparedStatement stmtLastId = null;
        try {
            cnx = getConnection();
            stmtLastId = cnx.prepareStatement(SQL_ALL_ID);
            final ResultSet result = stmtLastId.executeQuery();
            // keep the last
            String id = null;
            while (result.next()) {
                id = result.getString(1);
            }
            if (id != null) {
                try {
                    final int i = Integer.parseInt(id.substring(sensorIdBase.length()));
                    return new DefaultFeatureId(sensorIdBase + i);
                } catch (NumberFormatException ex) {
                    getLogger().warning("a snesor ID is malformed in procedures tables");
                }
            } else {
                return new DefaultFeatureId(sensorIdBase + 1);
            }

        } catch (SQLException ex) {
            getLogger().log(Level.WARNING, null, ex);
        }finally{
            if(stmtLastId != null){
                try {
                    stmtLastId.close();
                } catch (SQLException ex) {
                    getLogger().log(Level.WARNING, null, ex);
                }
            }

            if(cnx != null){
                try {
                    cnx.close();
                } catch (SQLException ex) {
                    getLogger().log(Level.WARNING, null, ex);
                }
            }
        }
        return null;
    }


    ////////////////////////////////////////////////////////////////////////////
    // No supported stuffs /////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////

    /**
     * {@inheritDoc }
     */
    @Override
    public void createFeatureType(final Name typeName, final FeatureType featureType) throws DataStoreException {
        throw new DataStoreException("Not Supported.");
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void updateFeatureType(final Name typeName, final FeatureType featureType) throws DataStoreException {
        throw new DataStoreException("Not Supported.");
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void deleteFeatureType(final Name typeName) throws DataStoreException {
        throw new DataStoreException("Not Supported.");
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void updateFeatures(final Name groupName, final Filter filter, final Map<? extends PropertyDescriptor, ? extends Object> values) throws DataStoreException {
        throw new DataStoreException("Not supported.");
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void removeFeatures(final Name groupName, final Filter filter) throws DataStoreException {
        handleRemoveWithFeatureWriter(groupName, filter);
    }


    ////////////////////////////////////////////////////////////////////////////
    // Feature Reader //////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////

    private class OMReader implements FeatureReader {

        protected final Connection cnx;
        private boolean firstCRS = true;
        protected final FeatureType type;
        private final ResultSet result;
        protected Feature current = null;

        private OMReader(final FeatureType type) throws SQLException{
            this.type = type;
            cnx = getConnection();
            final PreparedStatement stmtAll;
            if (isPostgres) {
                stmtAll = cnx.prepareStatement(SQL_ALL_PROCEDURE_PG);
            } else {
                stmtAll = cnx.prepareStatement(SQL_ALL_PROCEDURE);
            }
            result = stmtAll.executeQuery();
        }

        @Override
        public FeatureType getFeatureType() {
            return type;
        }

        @Override
        public Feature next() throws FeatureStoreRuntimeException {
            try {
                read();
            } catch (Exception ex) {
                throw new FeatureStoreRuntimeException(ex);
            }
            Feature candidate = current;
            current = null;
            return candidate;
        }

        @Override
        public boolean hasNext() throws FeatureStoreRuntimeException {
            try {
                read();
            } catch (Exception ex) {
                throw new FeatureStoreRuntimeException(ex);
            }
            return current != null;
        }

        protected void read() throws Exception{
            if(current != null) return;

            if(!result.next()){
                return;
            }


            final String crsStr = result.getString(3);
            Geometry geom = null;

            if (crsStr != null && !crsStr.isEmpty()) {
                if (firstCRS) {
                    try {
                        CoordinateReferenceSystem crs = CRS.decode("EPSG:" + crsStr);
                        if (type instanceof DefaultSimpleFeatureType) {
                            ((DefaultSimpleFeatureType) type).setCoordinateReferenceSystem(crs);
                        }
                        if (type.getGeometryDescriptor() instanceof DefaultGeometryDescriptor) {
                            ((DefaultGeometryDescriptor) type.getGeometryDescriptor()).setCoordinateReferenceSystem(crs);
                        }
                        firstCRS = false;
                    } catch (NoSuchAuthorityCodeException ex) {
                        throw new IOException(ex);
                    } catch (FactoryException ex) {
                        throw new IOException(ex);
                    }
                }

                final byte[] b = result.getBytes(2);
                if (b != null) {
                    WKBReader reader = new WKBReader();
                    geom = reader.read(b);
                }
            }

            final Collection<Property> props = new ArrayList<>();
            props.add(FF.createAttribute(geom, (AttributeDescriptor) type.getDescriptor(ATT_POSITION), null));
            //props.add(FF.createAttribute(result.getString("description"), (AttributeDescriptor) type.getDescriptor(ATT_DESC), null));
            //props.add(FF.createAttribute(result.getString("name"), (AttributeDescriptor) type.getDescriptor(ATT_NAME), null));

            final String id = result.getString(1);
            current = FF.createFeature(props, type, id);
        }

        @Override
        public void close() {
            try {
                result.close();
                cnx.close();
            } catch (SQLException ex) {
                throw new FeatureStoreRuntimeException(ex);
            }
        }

        @Override
        public void remove() throws FeatureStoreRuntimeException{
            throw new FeatureStoreRuntimeException("Not supported.");
        }

    }

    private class OMWriter extends OMReader implements FeatureWriter {

        protected Feature candidate = null;

        private OMWriter(final FeatureType type) throws SQLException{
            super(type);
        }

        @Override
        public Feature next() throws FeatureStoreRuntimeException {
            try {
                read();
            } catch (Exception ex) {
                throw new FeatureStoreRuntimeException(ex);
            }
            candidate = current;
            current = null;
            return candidate;
        }

        @Override
        public void remove() throws FeatureStoreRuntimeException{

            if (candidate == null) {
                return;
            }

            PreparedStatement stmtDelete = null;
            try {
                stmtDelete = cnx.prepareStatement(SQL_DELETE_PROCEDURE);
                stmtDelete.setString(1, candidate.getIdentifier().getID());
                stmtDelete.executeUpdate();

            } catch (SQLException ex) {
                getLogger().log(Level.WARNING, SQL_WRITE_PROCEDURE, ex);
            } finally {
                if (stmtDelete != null) {
                    try {
                        stmtDelete.close();
                    } catch (SQLException ex) {
                        getLogger().log(Level.WARNING, null, ex);
                    }
                }
            }

        }

        @Override
        public void write() throws FeatureStoreRuntimeException {
            throw new FeatureStoreRuntimeException("Not supported.");
        }
    }

	@Override
	public void refreshMetaModel() {
		
	}
}
