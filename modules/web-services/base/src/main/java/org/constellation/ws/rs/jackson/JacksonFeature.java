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

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import org.glassfish.jersey.CommonProperties;


/**
 * The Jackson feature used to register providers message body readers / writers.
 *
 * @author Guilhem Legal (Geomatys)
 * @author Cédric Briançon (Geomatys)
 * @since 0.9
 */
public class JacksonFeature implements Feature {
    /**
     * Configure the {@linkplain javax.ws.rs.core.FeatureContext feature context} to register
     * message body readers / writers for Jackson.
     *
     * @param context The {@linkplain javax.ws.rs.core.FeatureContext} to register.
     * @return Always {@code true}.
     */
    @Override
    public boolean configure(final FeatureContext context) {
        final StringBuilder sb = new StringBuilder();
        sb.append(CommonProperties.MOXY_JSON_FEATURE_DISABLE).append('.')
          .append(context.getConfiguration().getRuntimeType().name().toLowerCase());
        context.property(sb.toString(), true);

        context.register(com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider.class, MessageBodyReader.class, MessageBodyWriter.class);
        return true;
    }
}
