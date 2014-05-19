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
