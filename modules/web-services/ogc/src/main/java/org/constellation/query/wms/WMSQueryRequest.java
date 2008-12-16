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

import static org.constellation.query.wms.WMSQuery.*;


/**
 * Stores the kind of request available for this webservice.
 *
 * @version $Id$
 * @author Cédric Briançon
 */
public class WMSQueryRequest extends QueryRequest {
    /**
     * Key for the {@code DescribeLayer} request.
     */
    public static final WMSQueryRequest DESCRIBE_LAYER = new WMSQueryRequest(DESCRIBELAYER);

    /**
     * Key for the {@code GetCapabilities} request.
     */
    public static final WMSQueryRequest GET_CAPABILITIES = new WMSQueryRequest(GETCAPABILITIES);

    /**
     * Key for the {@code GetFeatureInfo} request.
     */
    public static final WMSQueryRequest GET_FEATURE_INFO = new WMSQueryRequest(GETFEATUREINFO);

    /**
     * Key for the {@code GetLegendGraphic} request.
     */
    public static final WMSQueryRequest GET_LEGEND_GRAPHIC = new WMSQueryRequest(GETLEGENDGRAPHIC);

    /**
     * Key for the {@code GetMap} request.
     */
    public static final WMSQueryRequest GET_MAP = new WMSQueryRequest(GETMAP);

    /**
     * Key for the {@code GetOrigFile} request.
     */
    public static final WMSQueryRequest GET_ORIG_FILE = new WMSQueryRequest(GETORIGFILE);

    private WMSQueryRequest(final String key) {
        super(key);
    }
}
