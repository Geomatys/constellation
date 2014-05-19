/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.query;

import org.apache.sis.util.Version;


/**
 * Interface for web queries.
 * Thoses are containers for real java objects, that means this should hold only
 * functionnal objects, and the minimum strings possible.
 *
 * @version $Id$
 * @author Johann Sorel (Geomayts)
 * @author Cédric Briançon (Geomatys)
 */
public interface Query {
    /**
     * Parameters for all requests.
     */
     String KEY_SERVICE = "SERVICE";
     String KEY_VERSION = "VERSION";
     String KEY_REQUEST = "REQUEST";

    /**
     * Exception handling parameters.
     */
     String KEY_EXCEPTIONS = "EXCEPTIONS";
     String EXCEPTIONS_INIMAGE = "INIMAGE";

    /**
     * Undefined CRS constant.
     */
     String UNDEFINED_CRS = "UNDEFINEDCRS";

    /**
     * Formats values.
     */
     String GML  = "gml";
     String GML3 = "gml3";
     String XML  = "xml";

    /**
     * Returns the request type specified for this query.
     */
    QueryRequest getRequest();

    /**
     * Returns the service name. Never {@code null}.
     */
    String getService();

    /**
     * Returns the version of the service chosen, or {@code null} if not specified.
     */
    Version getVersion();

    /**
     * Returns the exception format. Never {@code null}.
     */
    String getExceptionFormat();
}
