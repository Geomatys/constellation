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

import org.geotoolkit.feature.xml.jaxb.JAXBFeatureTypeWriter;
import org.opengis.feature.type.FeatureType;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Write a FeatureType response in the stream.
 *
 * @author Bernard Fabien (Geomatys).
 */
@Provider
public final class FeatureTypeResponseWriter implements MessageBodyWriter {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isWriteable(final Class clazz, final Type type, final Annotation[] annotations,
                               final MediaType mediaType) {
        return FeatureType.class.isAssignableFrom(clazz);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getSize(final Object obj, final Class clazz, final Type type, final Annotation[] annotations,
                        final MediaType mediaType) {
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeTo(final Object obj, final Class clazz, final Type type, final Annotation[] annotations,
                        final MediaType mediaType, final MultivaluedMap parameters,
                        final OutputStream outputStream) throws IOException, WebApplicationException {
        try {
            final JAXBFeatureTypeWriter writer = new JAXBFeatureTypeWriter();
            writer.write((FeatureType) obj, outputStream);
        } catch (JAXBException ex) {
            throw new IOException(ex);
        }
    }
}
