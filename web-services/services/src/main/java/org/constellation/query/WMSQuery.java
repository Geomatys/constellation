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
package org.constellation.query;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.util.Date;
import java.util.List;
import org.geotools.sld.MutableStyledLayerDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * WMS Query in java objects
 * 
 * @author Johann Sorel (Geomatys)
 */
public class WMSQuery implements Query{
    
    public static final String KEY_REQUEST = "REQUEST";
    public static final String REQUEST_MAP = "GetMap";
    public static final String REQUEST_FEATUREINFO = "GetFeatureInfo";
    public static final String REQUEST_CAPABILITIES = "GetCapabilities";
    public static final String REQUEST_DESCRIBELAYER = "DescribeLayer";
    public static final String REQUEST_LEGENDGRAPHIC = "GetLegendGraphic";
    public static final String REQUEST_ORIGFILE = "GetOrigFile";
    
    /** Parameter used in getMap */
    public static final String KEY_EXCEPTIONS = "EXCEPTIONS";
    /** Parameter used in getMap */
    public static final String EXCEPTIONS_INIMAGE = "INIMAGE";
    
    
    /** Parameter used in getMap, getLegendGraphic, getCapabilities */
    public static final String KEY_FORMAT = "FORMAT";
    /** Parameter used in getMap, describeLayer */
    public static final String KEY_LAYERS = "LAYERS";
    /** Parameter used in getOrigFile, getLegendGraphic */
    public static final String KEY_LAYER = "LAYER";
    /** Parameter used in getFeatureInfo */
    public static final String KEY_QUERY_LAYERS = "QUERY_LAYERS";
    /** Parameter used in getMap */
    public static final String KEY_DIM_RANGE = "DIM_RANGE";
    /** Parameter used in getMap, getFeatureInfo */
    public static final String KEY_CRS_v110 = "SRS";
    /** Parameter used in getMap, getFeatureInfo */
    public static final String KEY_CRS_v130 = "CRS";
    /** Parameter used in getMap, getFeatureInfo */
    public static final String KEY_BBOX = "BBOX";
    /** Parameter used in getMap, getFeatureInfo */
    public static final String KEY_ELEVATION = "ELEVATION";
    /** Parameter used in getMap, getOrigFile, getFeatureInfo */
    public static final String KEY_TIME = "TIME";
    /** Parameter used in getMap, getFeatureInfo, getLegendGraphic */
    public static final String KEY_WIDTH = "WIDTH";
    /** Parameter used in getMap, getFeatureInfo, getLegendGraphic */
    public static final String KEY_HEIGHT = "HEIGHT";
    /** Parameter used in getMap */
    public static final String KEY_BGCOLOR = "BGCOLOR";
    /** Parameter used in getMap */
    public static final String KEY_TRANSPARENT = "TRANSPARENT";
    /** Parameter used in getMap */
    public static final String KEY_STYLES = "STYLES";
    /** Parameter used in getLegendGraphic */
    public static final String KEY_STYLE = "STYLE";
    /** Parameter used in getMap,getLegendGraphic */
    public static final String KEY_SLD = "SLD";
    /** Parameter used in getLegendGraphic */
    public static final String KEY_FEATURETYPE = "FEATURETYPE";
    /** Parameter used in getLegendGraphic */
    public static final String KEY_COVERAGE = "COVERAGE";
    /** Parameter used in getLegendGraphic */
    public static final String KEY_RULE = "RULE";
    /** Parameter used in getLegendGraphic */
    public static final String KEY_SCALE = "SCALE";
    /** Parameter used in getLegendGraphic */
    public static final String KEY_SLD_BODY = "SLD_BODY";
    /** Parameter used in getMap,getLegendGraphic */
    public static final String KEY_REMOTE_OWS_TYPE = "REMOTE_OWS_TYPE";
    /** Parameter used in getMap,getLegendGraphic */
    public static final String KEY_REMOTE_OWS_URL = "REMOTE_OWS_URL";
    /** Parameter used in getFeatureInfo */
    public static final String KEY_I_v130 = "I";
    /** Parameter used in getFeatureInfo */
    public static final String KEY_J_v130 = "J";
    /** Parameter used in getFeatureInfo */
    public static final String KEY_I_v110 = "X";
    /** Parameter used in getFeatureInfo */
    public static final String KEY_J_v110 = "Y";
    /** Parameter used in getFeatureInfo */
    public static final String KEY_INFO_FORMAT= "INFO_FORMAT";
    /** Parameter used in getFeatureInfo */
    public static final String KEY_FEATURE_COUNT = "FEATURE_COUNT";
    /** Parameter used in getFeatureInfo */
    public static final String KEY_GETMETADATA = "GetMetadata";
    
    
    
    
    public final Rectangle2D bbox;
    public final CoordinateReferenceSystem crs;
    public final String format;
    public final List<String> layers;
    public final List<String> styles;
    public final Double elevation;
    public final Date date;
    public final Dimension size;
    public final Color background;
    public final boolean transparent;
    public final MutableStyledLayerDescriptor sld;
    
    public WMSQuery(Rectangle2D bbox, CoordinateReferenceSystem crs, String format,
            List<String> layers, List<String> styles, MutableStyledLayerDescriptor sld, Double elevation, Date date,
            Dimension size, Color background, boolean transparent){
        this.bbox = bbox;
        this.crs =  crs;
        this.format = format;
        this.layers = layers;
        this.styles = styles;
        this.sld = sld;
        this.elevation = elevation;
        this.date = date;
        this.size = size;
        this.background = background;
        this.transparent = transparent;
    }
    
}
