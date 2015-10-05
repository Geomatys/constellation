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
package org.constellation.ws.rs;

import java.util.logging.Logger;
import org.apache.sis.util.logging.Logging;
import org.constellation.ws.rs.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

public class RestApplication extends ResourceConfig {
    private static final Logger LOGGER = Logging.getLogger("org.constellation.ws.rs");

    public RestApplication() {
         super(JacksonFeature.class, MultiPartFeature.class, RolesAllowedDynamicFeature.class);
         LOGGER.info("Starting Rest API Application");
         packages("org.constellation.rest.api;org.constellation.ws.rest;org.constellation.metadata.ws.rs.provider;org.constellation.ws.rs.provider");
    }

}
