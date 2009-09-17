/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2008, Geomatys
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

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.geotoolkit.util.ImageIOUtilities;

@Provider
public class RenderedImageWriter<T extends RenderedImage> implements MessageBodyWriter<T> {

    @Override
    public long getSize(T r, Class<?> c, Type t, Annotation[] as, MediaType mt) {
        return -1;
    }

    @Override
    public void writeTo(T r, Class<?> c, Type t, Annotation[] as, MediaType mt,
            MultivaluedMap<String, Object> h, OutputStream out) throws IOException, WebApplicationException {
        ImageIOUtilities.writeImage(r, mt.toString(), out);
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return RenderedImage.class.isAssignableFrom(type);
    }
}
