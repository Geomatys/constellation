/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010, Geomatys
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

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
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
import org.constellation.portrayal.internal.CstlPortrayalService;
import org.constellation.portrayal.internal.PortrayalResponse;
import org.geotoolkit.display.exception.PortrayalException;
import org.geotoolkit.display2d.service.*;
import org.apache.sis.util.logging.Logging;

/**
 * Write a portrayal response in the stream.
 * 
 * @author Johann Sorel (Geomatys)
 */
@Provider
public final class PortrayalResponseWriter implements MessageBodyWriter<PortrayalResponse> {

    private static final Logger LOGGER = Logging.getLogger(PortrayalResponseWriter.class);

    private int prepare(final PortrayalResponse r, final Class<?> c, final Type t, final Annotation[] as, final MediaType mt) throws IOException{
        
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        OutputDef outdef = r.getOutputDef();
        if(outdef == null){
            outdef = new OutputDef(mt.toString(), out);
        }
        outdef.setOutput(out);
        
        BufferedImage img = r.getImage();
        if(img != null){
            DefaultPortrayalService.writeImage(img, outdef);
        } else {
            final CanvasDef cdef = r.getCanvasDef();
            final SceneDef sdef = r.getSceneDef();
            final ViewDef vdef = r.getViewDef();
            
            if(LOGGER.isLoggable(Level.FINE)){
                final long before = System.nanoTime();
                try {
                    CstlPortrayalService.getInstance().portray(sdef, vdef, cdef, outdef);
                } catch (PortrayalException ex) {
                    //should not happen normally since we asked to never fail.
                    throw new IOException(ex);
                }
                final long after = System.nanoTime();
                LOGGER.log(Level.FINE, "Portraying+Response ({0},Compression:{1}) time = {2} ms",
                        new Object[]{outdef.getMime(),outdef.getCompression(),Math.round( (after - before) / 1000000d)});
            }else{
                try {
                    CstlPortrayalService.getInstance().portray(sdef, vdef, cdef, outdef);
                } catch (PortrayalException ex) {
                    //should not happen normally since we asked to never fail.
                    throw new IOException(ex);
                }
            }
        }
        
        final byte[] result = out.toByteArray();
        r.setBuffer(result);
        return result.length;
    }
    
    @Override
    public long getSize(final PortrayalResponse r, final Class<?> c, final Type t, final Annotation[] as, final MediaType mt) {
        try {
            return prepare(r, c, t, as, mt);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }
        return -1;
    }

    @Override
    public void writeTo(final PortrayalResponse r, final Class<?> c, final Type t, final Annotation[] as, final MediaType mt,
            final MultivaluedMap<String, Object> h, OutputStream out) throws IOException, WebApplicationException {
        out.write(r.getBuffer());
    }

    @Override
    public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
        return PortrayalResponse.class.isAssignableFrom(type);
    }
        
}
