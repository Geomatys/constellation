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

package org.constellation.utils;

import org.apache.sis.util.Static;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Utility class for RESTful services.
 *
 * @author Bernard Fabien (Geomatys)
 * @version 0.9
 * @since 0.9
 */
public final class RESTfulUtilities extends Static {

    // Success.
    public static Response ok(final Object entity) {
        return Response.status(200).entity(entity).build();
    }
    public static Response created(final Object entity) {
        return Response.status(201).entity(entity).build();
    }
    public static Response noContent() {
        return Response.status(204).build();
    }

    // Failure.
    public static Response badRequest(final Object entity) {
        return Response.status(400).entity(entity).build();
    }
    public static Response notFound(final Object entity) {
        return Response.status(404).entity(entity).build();
    }
    public static Response internalError(final Object entity) {
        return Response.status(500).entity(entity).build();
    }
}
