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
package org.constellation.query.wms;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Date;
import java.util.List;
import org.geotools.sld.MutableStyledLayerDescriptor;
import org.geotools.util.MeasurementRange;
import org.opengis.geometry.Envelope;


/**
 * Representation of a {@code WMS GetFeatureInfo} request, with its parameters. It
 * is an extension of the {@link GetMap} request.
 *
 * @version $Id$
 * @author Cédric Briançon (Geomatys)
 *
 * @see GetMap
 */
public class GetFeatureInfo extends GetMap {
    /**
     * X coordinate to request.
     */
    private final int x;

    /**
     * Y coordinate to request.
     */
    private final int y;

    /**
     * Layers to request.
     */
    private final List<String> queryLayers;

    /**
     * Format of the returned information.
     */
    private final String infoFormat;

    /**
     * Number of maximal features that the request has to handle.
     */
    private final int featureCount;

    public GetFeatureInfo(final GetMap getMap, final int x, final int y,
                          final List<String> queryLayers, final String infoFormat,
                          final int featureCount)
    {
        super(getMap);
        this.x = x;     this.queryLayers = queryLayers;
        this.y = y;     this.infoFormat  = infoFormat;
        this.featureCount = featureCount;
    }

    public GetFeatureInfo(final Envelope envelope, final WMSQueryVersion version,
                  final String format, final List<String> layers, final List<String> styles,
                  final MutableStyledLayerDescriptor sld, final Double elevation, final Date date,
                  final MeasurementRange dimRange, final Dimension size, final Color background,
                  final Boolean transparent, final String exceptions, final int x, final int y,
                  final List<String> queryLayers, final String infoFormat, final int featureCount)
    {
        super(envelope, version, format, layers, styles, sld, elevation, date, dimRange, size,
                background, transparent, 0,exceptions);
        this.x = x;     this.queryLayers = queryLayers;
        this.y = y;     this.infoFormat  = infoFormat;
        this.featureCount = featureCount;
    }

    /**
     * Returns the X coordinate to request value.
     */
    public int getX() {
        return x;
    }

    /**
     * Returns the Y coordinate to request value.
     */
    public int getY() {
        return y;
    }

    /**
     * Returns a list of layers to request.
     */
    public List<String> getQueryLayers() {
        return queryLayers;
    }

    /**
     * Returns the format of the information to returned.
     */
    public String getInfoFormat() {
        return infoFormat;
    }

    /**
     * Returns the number of features to request. If a negative or {@code 0} is defined,
     * then it returns the default value {@code 1}.
     */
    public int getFeatureCount() {
        return (featureCount < 1) ? 1 : featureCount;
    }
}
