/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
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
import org.geotoolkit.util.logging.Logging;
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
