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
package org.constellation.ws.rs.jackson;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.codehaus.jackson.jaxrs.Annotations;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Complete the {@link org.constellation.ws.rs.jackson.JacksonJaxbJsonProvider} with some
 * untouchables classes, in order to let other providers handle them.
 *
 * @deprecated No more usefull, switching to jackson version > 2 fix the problem of writing metadata
 *
 * @author Guilhem Legal (Geomatys)
 * @author Cédric Briançon (Geomatys)
 * @since 0.9
 */
@Deprecated
@Provider
@Consumes({MediaType.APPLICATION_JSON, "text/json"})
@Produces({MediaType.APPLICATION_JSON, "text/json"})
public class JacksonJaxbJsonProvider extends org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider {
    

    /**
     * Default constructor, usually used when provider is automatically
     * configured to be used with JAX-RS implementation.
     */
    public JacksonJaxbJsonProvider() {
        super();
        addUntouchable(DefaultMetadata.class);
    }

    /**
     * @param annotationsToUse Annotation set(s) to use for configuring
     *    data binding
     */
    public JacksonJaxbJsonProvider(Annotations... annotationsToUse) {
        super(null, annotationsToUse);
        addUntouchable(DefaultMetadata.class);
    }
    
    /**
     * Constructor to use when a custom mapper (usually components
     * like serializer/deserializer factories that have been configured)
     * is to be used.
     */
    public JacksonJaxbJsonProvider(ObjectMapper mapper, Annotations[] annotationsToUse){
        super(mapper, annotationsToUse);
        addUntouchable(DefaultMetadata.class);
    }
}
  