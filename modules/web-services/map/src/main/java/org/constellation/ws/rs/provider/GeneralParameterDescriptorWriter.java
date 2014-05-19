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
package org.constellation.ws.rs.provider;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.stream.XMLStreamException;
import org.apache.sis.util.logging.Logging;
import org.geotoolkit.xml.parameter.ParameterDescriptorWriter;
import org.opengis.parameter.GeneralParameterDescriptor;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@Provider
public class GeneralParameterDescriptorWriter implements MessageBodyWriter<GeneralParameterDescriptor> {
    
    private static final Logger LOGGER = Logging.getLogger(GeneralParameterValueWriter.class);

    @Override
    public long getSize(final GeneralParameterDescriptor r, final Class<?> c, final Type t, final Annotation[] as, final MediaType mt) {
        return -1;
    }

    @Override
    public void writeTo(final GeneralParameterDescriptor r, final Class<?> c, final Type t, final Annotation[] as, final MediaType mt,
            final MultivaluedMap<String, Object> h, final OutputStream out) throws IOException, WebApplicationException {
        try {
            final ParameterDescriptorWriter writer = new ParameterDescriptorWriter();
            writer.setOutput(out);
            writer.write(r);
        } catch (XMLStreamException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
        return GeneralParameterDescriptor.class.isAssignableFrom(type);
    }
}
