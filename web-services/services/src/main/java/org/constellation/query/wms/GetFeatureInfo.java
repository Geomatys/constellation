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
import org.constellation.query.QueryVersion;
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
    private final double x;

    /**
     * Y coordinate to request.
     */
    private final double y;

    /**
     * Layers to request.
     */
    private final List<String> queryLayers;

    /**
     * Format of the returned information.
     */
    private final String infoFormat;

    public GetFeatureInfo(final GetMap getMap, final double x, final double y,
                          final List<String> queryLayers, final String infoFormat)
    {
        super(getMap);
        this.x = x;     this.queryLayers = queryLayers;
        this.y = y;     this.infoFormat  = infoFormat;
    }

    public GetFeatureInfo(final Envelope envelope, final QueryVersion version,
                  final String format, final List<String> layers, final List<String> styles,
                  final MutableStyledLayerDescriptor sld, final Double elevation, final Date date,
                  final MeasurementRange dimRange, final Dimension size, final Color background,
                  final Boolean transparent, final String exceptions, final double x, final double y,
                  final List<String> queryLayers, final String infoFormat)
    {
        super(envelope, version, format, layers, styles, sld, elevation, date, dimRange, size,
                background, transparent, exceptions);
        this.x = x;     this.queryLayers = queryLayers;
        this.y = y;     this.infoFormat  = infoFormat;
    }

    /**
     * Returns the X coordinate to request value.
     */
    public double getX() {
        return x;
    }

    /**
     * Returns the Y coordinate to request value.
     */
    public double getY() {
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
}
