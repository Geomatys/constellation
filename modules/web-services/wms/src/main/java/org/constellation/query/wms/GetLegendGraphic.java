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

import org.constellation.query.QueryRequest;
import org.constellation.ws.ServiceType;
import org.constellation.ws.ServiceVersion;


/**
 * Representation of a {@code WMS GetLegendGraphic} request, with its parameters.
 *
 * @version $Id$
 * @author Cédric Briançon
 */
public class GetLegendGraphic extends WMSQuery {
    /**
     * Layer to consider.
     */
    private final String layer;

    /**
     * Format of the legend file returned.
     */
    private final String format;

    /**
     * Width of the generated legend image. Optional.
     */
    private final Integer width;

    /**
     * Height of the generated legend image. Optional.
     */
    private final Integer height;

    /**
     * Builds a {@code GetLegendGraphic} request, using the layer and mime-type specified
     * and width and height for the image.
     */
    public GetLegendGraphic(final String layer, final String format,
                            final Integer width, final Integer height)
    {
        super(new ServiceVersion(ServiceType.WMS, "1.1.1"));
        this.layer  = layer;
        this.format = format;
        this.width  = width;
        this.height = height;
    }

    /**
     * Returns the layer to consider for this request.
     */
    public String getLayer() {
        return layer;
    }

    /**
     * Returns the format for the legend file.
     */
    public String getFormat() {
        return format;
    }

    /**
     * Returns the width of the legend image.
     */
    public Integer getWidth() {
        return width;
    }

    /**
     * Returns the height of the legend image.
     */
    public Integer getHeight() {
        return height;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getExceptionFormat() {
        return "application/vnd.ogc.se_xml";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueryRequest getRequest() {
        return WMSQueryRequest.GET_LEGEND_GRAPHIC;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toKvp() {
        final StringBuilder kvp = new StringBuilder();
        //Obligatory Parameters
        kvp            .append(KEY_REQUEST).append('=').append(GETLEGENDGRAPHIC)
           .append('&').append(KEY_FORMAT ).append('=').append(format)
           .append('&').append(KEY_LAYER  ).append('=').append(layer);

        //Optional Parameters
        if (width != null) {
            kvp.append('&').append(KEY_WIDTH ).append('=').append(width);
        }
        if (height != null) {
            kvp.append('&').append(KEY_HEIGHT).append('=').append(height);
        }
        return kvp.toString();
    }
}
