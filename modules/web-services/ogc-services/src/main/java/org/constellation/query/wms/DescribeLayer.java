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

import java.util.List;
import org.constellation.query.QueryRequest;


/**
 * Representation of a {@code WMS DescribeLayer} request, with its parameters.
 *
 * @version $Id$
 * @author Cédric Briançon
 */
public class DescribeLayer extends WMSQuery {
    /**
     * List of layers to request.
     */
    private final List<String> layers;

    /**
     * Builds a {@code DescribeLayer} request, using the layer and mime-type specified.
     */
    public DescribeLayer(final List<String> layers, final WMSQueryVersion version) {
        super(version);
        this.layers = layers;
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
     * Returns a list of layers.
     */
    public List<String> getLayers() {
        return layers;
    }
}
