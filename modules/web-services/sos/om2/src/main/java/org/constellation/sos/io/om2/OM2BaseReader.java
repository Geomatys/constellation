/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.constellation.sos.io.om2;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
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
import org.geotoolkit.util.logging.Logging;

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
}
