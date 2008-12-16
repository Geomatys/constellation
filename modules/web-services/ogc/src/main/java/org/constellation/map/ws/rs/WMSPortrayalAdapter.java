/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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
package org.constellation.map.ws.rs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.constellation.portrayal.CSTLPortrayalService;
import org.constellation.query.wms.GetFeatureInfo;
import org.constellation.query.wms.GetMap;
import org.constellation.query.wms.WMSQuery;
import org.constellation.ws.ServiceVersion;
import org.constellation.ws.WebServiceException;

import org.geotools.display.canvas.GraphicVisitor;
import org.geotools.display.exception.PortrayalException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.sld.MutableStyledLayerDescriptor;
import org.geotools.util.MeasurementRange;

import org.opengis.geometry.Envelope;

/**
 * Adapt WMS queries to generic portrayal parameters.
 *
 * @author Johann Sorel (Geomatys)
 */
public class WMSPortrayalAdapter {
    /**
     * Defines the number of pixels around the requested coordinates (X,Y), in order to
     * search into a {@linkplain Rectangle rectangle}.
     */
    private static final int PIXEL_TOLERANCE = 3;

    /**
     * Makes the portray of a {@code GetMap} request.
     *
     * @param query A {@link GetMap} query. Should not be {@code null}.
     *
     * @throws PortrayalException
     * @throws WebServiceException if an error occurs during the creation of the map context
     */
    public static BufferedImage portray(final GetMap query)
                            throws PortrayalException, WebServiceException {

        if (query == null) {
            throw new NullPointerException("The GetMap query cannot be null. The portray() method" +
                    " is not well used here.");
        }

        final List<String> layers              = query.getLayers();
        final List<String> styles              = query.getStyles();
        final MutableStyledLayerDescriptor sld = query.getSld();
        final Envelope contextEnv              = query.getEnvelope();
        final ReferencedEnvelope refEnv        = new ReferencedEnvelope(contextEnv);
        final String mime                      = query.getFormat();
        final ServiceVersion version           = query.getVersion();
        final Double elevation                 = query.getElevation();
        final Date time                        = query.getTime();
        final MeasurementRange dimRange        = query.getDimRange();
        final Dimension canvasDimension        = query.getSize();
        final double azimuth                   = query.getAzimuth();
        final Map<String, Object> params       = new HashMap<String, Object>();
        params.put(WMSQuery.KEY_ELEVATION, elevation);
        params.put(WMSQuery.KEY_DIM_RANGE, dimRange);
        params.put(WMSQuery.KEY_TIME, time);
        final Color background;
        if (query.getTransparent()) {
            background = null;
        } else {
            final Color color = query.getBackground();
            background = (color == null) ? Color.WHITE : color;
        }

        if (false) {
            //for debug
            final StringBuilder builder = new StringBuilder();
            builder.append("Layers => ");
            for(String layer : layers){
                builder.append(layer +",");
            }
            builder.append("\n");
            builder.append("Styles => ");
            for(String style : styles){
                builder.append(style +",");
            }
            builder.append("\n");
            builder.append("Context env => " + contextEnv.toString() + "\n");
            builder.append("Azimuth => " + azimuth + "\n");
            builder.append("Mime => " + mime.toString() + "\n");
            builder.append("Dimension => " + canvasDimension.toString() + "\n");
            builder.append("BGColor => " + background + "\n");
            builder.append("Transparent => " + query.getTransparent() + "\n");
            System.out.println(builder.toString());
        }

        return CSTLPortrayalService.getInstance().portray(
                refEnv, azimuth, background, canvasDimension,
                layers, styles, sld, params, version);

    }

    public static void hit(final GetFeatureInfo query, final GraphicVisitor visitor)
                            throws PortrayalException, WebServiceException{

        if (query == null) {
            throw new NullPointerException("The GetMap query cannot be null. The portray() method" +
                    " is not well used here.");
        }

        final Map<String, List<String>> values = new HashMap<String, List<String>>();
        final List<String> layers              = query.getQueryLayers();
        final List<String> styles              = query.getStyles();
        final MutableStyledLayerDescriptor sld = query.getSld();
        final Envelope contextEnv              = query.getEnvelope();
        final ReferencedEnvelope refEnv        = new ReferencedEnvelope(contextEnv);
        final String mime                      = query.getFormat();
        final ServiceVersion version           = query.getVersion();
        final Double elevation                 = query.getElevation();
        final Date time                        = query.getTime();
        final MeasurementRange dimRange        = query.getDimRange();
        final Dimension canvasDimension        = query.getSize();
        final double azimuth                   = query.getAzimuth();
        final int infoX                        = query.getX();
        final int infoY                        = query.getY();
        final Map<String, Object> params       = new HashMap<String, Object>();
        params.put(WMSQuery.KEY_ELEVATION, elevation);
        params.put(WMSQuery.KEY_DIM_RANGE, dimRange);
        params.put(WMSQuery.KEY_TIME, time);
        final Color background;
        if (query.getTransparent()) {
            background = null;
        } else {
            final Color color = query.getBackground();
            background = (color == null) ? Color.WHITE : color;
        }
        final Rectangle selectedArea = new Rectangle(infoX-PIXEL_TOLERANCE, infoY-PIXEL_TOLERANCE, PIXEL_TOLERANCE*2, PIXEL_TOLERANCE*2);

        //fill in the values with empty lists
        for(String layer : layers){
            values.put(layer, new ArrayList<String>());
        }

        if (false) {
            //for debug
            final StringBuilder builder = new StringBuilder();
            builder.append("Layers => ");
            for(String layer : layers){
                builder.append(layer +",");
            }
            builder.append("\n");
            builder.append("Styles => ");
            for(String style : styles){
                builder.append(style +",");
            }
            builder.append("\n");
            builder.append(selectedArea);
            builder.append("\n");
            builder.append("Context env => " + refEnv.toString() + "\n");
            builder.append("Context crs => " + refEnv.getCoordinateReferenceSystem().toString() + "\n");
            builder.append("Azimuth => " + azimuth + "\n");
            builder.append("Mime => " + mime.toString() + "\n");
            builder.append("Dimension => " + canvasDimension.toString() + "\n");
            builder.append("BGColor => " + background + "\n");
            builder.append("Transparent => " + query.getTransparent() + "\n");
            builder.append("elevation => " + elevation + "\n");
            builder.append("range => " + dimRange + "\n");
            builder.append("time => " + time + "\n");
            System.out.println(builder.toString());
        }

        CSTLPortrayalService.getInstance().hit(
            refEnv, azimuth, background, canvasDimension,
            layers, styles, sld, params, version,
            selectedArea, visitor);

    }

}
