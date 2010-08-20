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
package org.constellation.query.wfs;

import org.constellation.query.Query;
import org.constellation.query.DefaultQueryRequest;
import org.constellation.query.QueryRequest;
import org.geotoolkit.util.Version;


/**
 * Handle the service type and the version of a WFS query.
 * Contains constants for WFS requests in version 1.1.0
 *
 * @author Johann Sorel (Geomatys)
 */
public abstract class WFSQuery implements Query {

    /**
     * Request parameters.
     */
    public static final String STR_GETCAPABILITIES      = "GetCapabilities";
    public static final String STR_DESCRIBEFEATURETYPE  = "DescribeFeatureType";
    public static final String STR_GETFEATURE           = "GetFeature";
    public static final String STR_GETGMLOBJECT         = "getGMLObject";
    public static final String STR_LOCKFEATURE          = "lockFeature";
    public static final String STR_TRANSACTION          = "Transaction";

    /**
     * WFS Query service
     */
    public static final String WFS_SERVICE = "WFS";

    /**
     * Key for the {@code GetCapabilities} request.
     */
    public static final QueryRequest GET_CAPABILITIES = new DefaultQueryRequest(STR_GETCAPABILITIES);

    /**
     * Key for the {@code DescribeFeatureType} request.
     */
    public static final QueryRequest DESCRIBE_FEATURE_TYPE = new DefaultQueryRequest(STR_DESCRIBEFEATURETYPE);

    /**
     * Key for the {@code GetFeature} request.
     */
    public static final QueryRequest GET_FEATURE = new DefaultQueryRequest(STR_GETFEATURE);

    /**
     * Key for the {@code getGMLObject} request.
     */
    public static final QueryRequest GET_GML_OBJECT = new DefaultQueryRequest(STR_GETGMLOBJECT);

    /**
     * Key for the {@code lockFeature} request.
     */
    public static final QueryRequest LOCK_FEATURE = new DefaultQueryRequest(STR_LOCKFEATURE);

    /**
     * Key for the {@code Transaction} request.
     */
    public static final QueryRequest TRANSACTION = new DefaultQueryRequest(STR_TRANSACTION);



    private final Version version;

    protected WFSQuery(final Version version) {
        if (version == null) {
            throw new NullPointerException("Version should not be null !");
        }
        this.version = version;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getService() {
        return WFS_SERVICE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Version getVersion() {
        return version;
    }

}
