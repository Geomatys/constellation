/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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

package org.constellation.wmts.ws.rs;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.geotoolkit.coverage.TileReference;
import org.geotoolkit.internal.io.IOUtilities;
import org.geotoolkit.util.logging.Logging;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
@Provider
@Produces("image/*")
public class StreamResponseWriter implements MessageBodyWriter<TileReference>  {

    private static final Logger LOGGER = Logging.getLogger("org.constellation.map.ws.rs");

    @Override
    public boolean isWriteable(final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt) {
        return TileReference.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(final TileReference t, final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt) {
        return -1;
    }

    @Override
    public void writeTo(final TileReference t, final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt,
            final MultivaluedMap<String, Object> mm, final OutputStream out) throws IOException, WebApplicationException {
        
        Object input = t.getInput();
        
        //try to write the content of the tile if it's alredy in a binary form
        final InputStream stream;
        if(input instanceof byte[]){
            stream = new ByteArrayInputStream((byte[])input);
        }else if(input instanceof InputStream){
            stream = (InputStream)input;
        }else if(input instanceof URL){
            stream = ((URL)input).openStream();
        }else if(input instanceof URI){
            stream = ((URI)input).toURL().openStream();
        }else if(input instanceof File){
            stream = new FileInputStream((File)input);
        }else{
            LOGGER.log(Level.WARNING, "Unsupported tyle type : {0}", input.getClass());
            return;
        }
        
        try {
            IOUtilities.copy(stream, out);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        } finally {
            if(stream != null){
                stream.close();
            }
        }
    }
}
