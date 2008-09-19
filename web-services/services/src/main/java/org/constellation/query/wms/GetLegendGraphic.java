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
import org.constellation.query.QueryVersion;


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
     * Builds a {@code GetLegendGraphic} request, using the layer and mime-type specified.
     */
    public GetLegendGraphic(final String layer, final String format) {
        this.layer = layer;
        this.format = format;
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
     * {@inheritDoc}
     */
    public String getExceptionFormat() {
        return "application/vnd.ogc.se_xml";
    }

    /**
     * {@inheritDoc}
     */
    public QueryRequest getRequest() {
        return WMSQueryRequest.GET_LEGEND_GRAPHIC;
    }

    /**
     * {@inheritDoc}
     */
    public QueryVersion getVersion() {
        return WMSQueryVersion.WMS_GETLEGENDGRAPHIC_1_1_0;
    }
}
