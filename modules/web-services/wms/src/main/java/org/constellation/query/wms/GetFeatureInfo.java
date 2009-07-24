/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2009, Geomatys
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
import org.constellation.util.StringUtilities;
import org.geotoolkit.sld.MutableStyledLayerDescriptor;
import org.geotoolkit.util.MeasurementRange;
import org.geotoolkit.util.Version;
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
     * Number of maximal features that the request has to handle. Optional.
     */
    private final Integer featureCount;

    public GetFeatureInfo(final GetMap getMap, final int x, final int y,
                          final List<String> queryLayers, final String infoFormat,
                          final Integer featureCount)
    {
        super(getMap);
        this.x = x;     this.queryLayers = queryLayers;
        this.y = y;     this.infoFormat  = infoFormat;
        this.featureCount = featureCount;
    }

    public GetFeatureInfo(final Envelope envelope, final Version version,
                  final String format, final List<String> layers, final List<String> styles,
                  final MutableStyledLayerDescriptor sld, final Double elevation, final Date date,
                  final MeasurementRange dimRange, final Dimension size, final Color background,
                  final Boolean transparent, final String exceptions, final int x, final int y,
                  final List<String> queryLayers, final String infoFormat, final Integer featureCount)
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
     * Returns the number of features to request.
     */
    public Integer getFeatureCount() {
        return featureCount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toKvp() {
        final String getMapKvp = super.toKvp();
        final StringBuilder kvp = new StringBuilder(getMapKvp);
        //Obligatory Parameters
        kvp.append('&').append(KEY_QUERY_LAYERS).append('=').append(StringUtilities.toCommaSeparatedValues(queryLayers))
           .append('&').append(KEY_INFO_FORMAT ).append('=').append(infoFormat)
           .append('&').append((version.toString().equals("1.1.1")) ?
                               KEY_I_V111 :
                               KEY_I_V130 )     .append('=').append(x)
           .append('&').append((version.toString().equals("1.1.1")) ?
                               KEY_J_V111 :
                               KEY_J_V130 )     .append('=').append(y);

        //Optional parameters
        if (featureCount != null) {
            kvp.append('&').append(KEY_FEATURE_COUNT).append('=').append(featureCount);
        }
        return kvp.toString();
    }
}
