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
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.logging.Logging;
import org.constellation.sos.factory.OMFactory;
import org.geotoolkit.gml.JTStoGeometry;
import org.geotoolkit.gml.xml.Envelope;
import org.geotoolkit.gml.xml.FeatureProperty;
import org.geotoolkit.referencing.CRS;
import org.opengis.observation.Phenomenon;
import org.opengis.observation.sampling.SamplingFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.FactoryException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.geotoolkit.sos.xml.SOSXmlFactory.buildCompositePhenomenon;
import static org.geotoolkit.sos.xml.SOSXmlFactory.buildFeatureProperty;
import static org.geotoolkit.sos.xml.SOSXmlFactory.buildPhenomenon;
import static org.geotoolkit.sos.xml.SOSXmlFactory.buildSamplingCurve;
import static org.geotoolkit.sos.xml.SOSXmlFactory.buildSamplingFeature;
import static org.geotoolkit.sos.xml.SOSXmlFactory.buildSamplingPoint;
import static org.geotoolkit.sos.xml.SOSXmlFactory.buildSamplingPolygon;
import static org.geotoolkit.sos.xml.SOSXmlFactory.getGMLVersion;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class OM2BaseReader {

    protected boolean isPostgres;
    
    /**
     * The base for observation id.
     */
    protected final String observationIdBase;
    
    protected final String phenomenonIdBase;
    
    protected final String sensorIdBase;
    
    protected final String observationTemplateIdBase;
    
    protected static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    protected static final SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S");
    
    public OM2BaseReader(final Map<String, Object> properties) {
        final String phenID = (String) properties.get(OMFactory.PHENOMENON_ID_BASE);
        if (phenID == null) {
            this.phenomenonIdBase      = "";
        } else {
            this.phenomenonIdBase      = phenID;
        }
        this.sensorIdBase              = (String) properties.get(OMFactory.SENSOR_ID_BASE);
        this.observationTemplateIdBase = (String) properties.get(OMFactory.OBSERVATION_TEMPLATE_ID_BASE);
        this.observationIdBase         = (String) properties.get(OMFactory.OBSERVATION_ID_BASE);
    }
    
    public OM2BaseReader(final OM2BaseReader that) {
        this.phenomenonIdBase          = that.phenomenonIdBase;
        this.observationTemplateIdBase = that.observationTemplateIdBase;
        this.sensorIdBase              = that.sensorIdBase;
        this.isPostgres                = that.isPostgres;
        this.observationIdBase         = that.observationIdBase;
    }
            
    /**
     * use for debugging purpose
     */
    protected static final Logger LOGGER = Logging.getLogger("org.constellation.sos");
    
    protected static final CoordinateReferenceSystem defaultCRS;
    static {
        CoordinateReferenceSystem candidate = null;
        try {
            candidate = CRS.decode("EPSG:4326");
        } catch (FactoryException ex) {
            LOGGER.log(Level.SEVERE, "Error while retrieving default CRS 4326", ex);
        }
        defaultCRS = candidate;
    }
    
    protected SamplingFeature getFeatureOfInterest(final String id, final String version, final Connection c) throws SQLException, DataStoreException {
        try {
            final String name;
            final String description;
            final String sampledFeature;
            final byte[] b;
            final int srid;
            final PreparedStatement stmt;
            if (isPostgres) {
                stmt  = c.prepareStatement("SELECT \"id\", \"name\", \"description\", \"sampledfeature\", \"postgis\".st_asBinary(\"shape\"), \"crs\" FROM \"om\".\"sampling_features\" WHERE \"id\"=?");
            } else {
                stmt  = c.prepareStatement("SELECT * FROM \"om\".\"sampling_features\" WHERE \"id\"=?");
            }
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
            return buildFoi(version, id, name, description, sampledFeature, geom, crs);
            
        } catch (ParseException ex) {
            throw new DataStoreException(ex.getMessage(), ex);
        } catch (FactoryException ex) {
            throw new DataStoreException(ex.getMessage(), ex);
        }
    }
    
    protected SamplingFeature buildFoi(final String version, final String id, final String name, final String description, final String sampledFeature, 
            final Geometry geom, final CoordinateReferenceSystem crs) throws FactoryException {
        
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
            point.setId("pt-" + id);
            return buildSamplingPoint(version, id, name, description, prop, point);
        } else if (geom instanceof LineString) {
            final org.geotoolkit.gml.xml.LineString line = JTStoGeometry.toGML(gmlVersion, (LineString)geom, crs);
            line.emptySrsNameOnChild();
            line.setId("line-" + id);
            final Envelope bound = line.getBounds();
            return buildSamplingCurve(version, id, name, description, prop, line, null, null, bound);
        } else if (geom instanceof Polygon) {
            final org.geotoolkit.gml.xml.Polygon poly = JTStoGeometry.toGML(gmlVersion, (Polygon)geom, crs);
            poly.setId("polygon-" + id);
            return buildSamplingPolygon(version, id, name, description, prop, poly, null, null, null);
        } else if (geom != null) {
            LOGGER.log(Level.WARNING, "Unexpected geometry type:{0}", geom.getClass());
        } 
        return buildSamplingFeature(version, id, name, description, prop);
    }

    protected Phenomenon getPhenomenon(final String version, final String observedProperty, final Connection c) throws DataStoreException {
        final String id;
        if (observedProperty.startsWith(phenomenonIdBase)) {
            id = observedProperty.substring(phenomenonIdBase.length());
        } else {
            id = observedProperty;
        }
        try {
            if (version.equals("2.0.0")) {
                return buildPhenomenon(version, id, observedProperty);
            } else {
                // look for composite phenomenon
                final PreparedStatement stmt = c.prepareStatement("SELECT \"component\" FROM \"om\".\"components\" WHERE \"phenomenon\"=?");
                stmt.setString(1, observedProperty);
                final ResultSet rs = stmt.executeQuery();
                final List<Phenomenon> phenomenons = new ArrayList<>();
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
            throw new DataStoreException(ex.getMessage(), ex);
        }
    }
}
