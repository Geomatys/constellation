/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
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
  