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

import java.util.List;
import org.constellation.query.QueryRequest;
import org.constellation.util.StringUtilities;
import org.geotools.util.Version;


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
    public DescribeLayer(final List<String> layers, final Version version) {
        super(version);
        this.layers = layers;
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
        return WMSQueryRequest.DESCRIBE_LAYER;
    }

    /**
     * Returns a list of layers.
     */
    public List<String> getLayers() {
        return layers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toKvp() {
        final StringBuilder kvp = new StringBuilder();
        //Obligatory Parameters
        kvp            .append(KEY_REQUEST).append('=').append(DESCRIBELAYER)
           .append('&').append(KEY_LAYERS ).append('=').append(StringUtilities.toCommaSeparatedValues(layers));

        //Optional Parameters
        if (version != null) {
            kvp.append('&').append(KEY_VERSION).append('=').append(version);
        }
        return kvp.toString();
    }
}
