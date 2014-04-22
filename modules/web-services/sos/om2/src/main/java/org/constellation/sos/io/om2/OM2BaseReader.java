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
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
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
import org.constellation.sos.factory.OMFactory;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.gml.JTStoGeometry;
import org.geotoolkit.gml.xml.Envelope;
import org.geotoolkit.gml.xml.FeatureProperty;
import org.geotoolkit.referencing.CRS;
import org.apache.sis.util.logging.Logging;

import static org.geotoolkit.sos.xml.SOSXmlFactory.*;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

import org.opengis.observation.Phenomenon;
import org.opengis.observation.sampling.SamplingFeature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.FactoryException;

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
        this.phenomenonIdBase          = (String) properties.get(OMFactory.PHENOMENON_ID_BASE);
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
    
    protected SamplingFeature getFeatureOfInterest(final String id, final String version, final Connection c) throws SQLException, CstlServiceException {
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
            throw new CstlServiceException(ex.getMessage(), ex, NO_APPLICABLE_CODE);
        } catch (FactoryException ex) {
            throw new CstlServiceException(ex.getMessage(), ex, NO_APPLICABLE_CODE);
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

    /*****************************************************************************************************
     *
     * DELETE this methode when cstl point on trunk version of geotk-pending
     *
     ****************************************************************************************************
     */
    public static SamplingFeature buildSamplingPolygon(final String version, final String id, final String name, final String description, final FeatureProperty sampledFeature,
                              final org.geotoolkit.gml.xml.Polygon location, final Double areaValue, final String uom, final Envelope env) {
        if ("1.0.0".equals(version)) {
            if (sampledFeature != null && !(sampledFeature instanceof org.geotoolkit.gml.xml.v311.FeaturePropertyType)) {
                throw new IllegalArgumentException("unexpected object version for sampled feature element");
            }
            if (location != null && !(location instanceof org.geotoolkit.gml.xml.v311.PolygonType)) {
                throw new IllegalArgumentException("unexpected object version for location element");
            }
            if (env != null && !(env instanceof org.geotoolkit.gml.xml.v311.EnvelopeType)) {
                throw new IllegalArgumentException("unexpected object version for env element");
            }
            final org.geotoolkit.gml.xml.v311.MeasureType area;
            if (areaValue != null) {
                area = new org.geotoolkit.gml.xml.v311.MeasureType(areaValue, uom);
            } else {
                area = new org.geotoolkit.gml.xml.v311.MeasureType(0.0, uom);
            }
            final org.geotoolkit.gml.xml.v311.SurfacePropertyType sp =  new org.geotoolkit.gml.xml.v311.SurfacePropertyType();
            sp.setAbstractSurface((org.geotoolkit.gml.xml.v311.PolygonType)location);
            final org.geotoolkit.sampling.xml.v100.SamplingSurfaceType sst =  new org.geotoolkit.sampling.xml.v100.SamplingSurfaceType();
            sst.setId(id);
            sst.setName(name);
            sst.setDescription(description);
            // pas de setter corrigé ds le trunk avec le constructeur (org.geotoolkit.gml.xml.v311.FeaturePropertyType)sampledFeature,
            sst.setShape(sp);
            sst.setArea(area);
            sst.setBoundedBy((org.geotoolkit.gml.xml.v311.EnvelopeType)env);

            return sst;
        } else if ("2.0.0".equals(version)) {
            if (sampledFeature != null && !(sampledFeature instanceof org.geotoolkit.gml.xml.v321.FeaturePropertyType)) {
                throw new IllegalArgumentException("unexpected object version for sampled feature element");
            }
            if (location != null && !(location instanceof org.geotoolkit.gml.xml.v321.PolygonType)) {
                throw new IllegalArgumentException("unexpected object version for location element");
            }
            return new org.geotoolkit.samplingspatial.xml.v200.SFSpatialSamplingFeatureType(id, name, description, "http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingCurve",
                                                                          (org.geotoolkit.gml.xml.v321.FeaturePropertyType)sampledFeature,
                                                                          (org.geotoolkit.gml.xml.v321.PolygonType)location,
                                                                          (org.geotoolkit.gml.xml.v321.EnvelopeType)env);
        } else {
            throw new IllegalArgumentException("unexpected sos version number:" + version);
        }
    }
    
    protected Phenomenon getPhenomenon(final String version, final String observedProperty, final Connection c) throws CstlServiceException {
        final String id;
        if (observedProperty.startsWith(phenomenonIdBase)) {
            id = observedProperty.substring(phenomenonIdBase.length());
        } else {
            id = null;
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
            throw new CstlServiceException(ex.getMessage(), ex, NO_APPLICABLE_CODE);
        }
    }
}
