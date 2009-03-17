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
package org.constellation.query;

import org.geotools.util.Version;


/**
 * Interface for web queries.
 * Thoses are containers for real java objects, that means this should hold only
 * functionnal objects, and the minimum strings possible.
 *
 * @version $Id$
 * @author Johann Sorel (Geomayts)
 * @author Cédric Briançon (Geomatys)
 */
public abstract class Query {
    /**
     * Parameters for all requests.
     */
    public static final String KEY_SERVICE = "SERVICE";
    public static final String KEY_VERSION = "VERSION";
    public static final String KEY_REQUEST = "REQUEST";

    /**
     * Exception handling parameters.
     */
    public static final String KEY_EXCEPTIONS = "EXCEPTIONS";
    public static final String EXCEPTIONS_INIMAGE = "INIMAGE";

    /**
     * Undefined CRS constant.
     */
    public static final String UNDEFINED_CRS = "UNDEFINEDCRS";

    /**
     * Formats values.
     */
    public static final String APP_GML = "application/vnd.ogc.gml";
    public static final String APP_XML = "application/vnd.ogc.xml";
    public static final String APP_WMS_XML = "application/vnd.ogc.wms_xml";
    public static final String APP_INIMAGE = "application/vnd.ogc.se_inimage";
    public static final String IMAGE_PNG = "image/png";
    public static final String IMAGE_GIF = "image/gif";
    public static final String IMAGE_JPEG = "image/jpeg";
    public static final String GML = "gml";
    public static final String XML = "xml";
    public static final String TEXT_XML = "text/xml";
    public static final String TEXT_PLAIN = "text/plain";
    public static final String TEXT_HTML = "text/html";

    /**
     * Returns the request type specified for this query.
     */
    public abstract QueryRequest getRequest();

    /**
     * Returns the service name. Never {@code null}.
     */
    public abstract QueryService getService();

    /**
     * Returns the version of the service chosen, or {@code null} if not specified.
     */
    public abstract Version getVersion();

    /**
     * Returns the exception format. Never {@code null}.
     */
    public abstract String getExceptionFormat();
}
