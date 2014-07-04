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

import org.apache.sis.util.logging.Logging;
import org.constellation.portrayal.internal.CstlPortrayalService;
import org.constellation.portrayal.internal.PortrayalResponse;
import org.geotoolkit.display.PortrayalException;
import org.geotoolkit.display2d.service.CanvasDef;
import org.geotoolkit.display2d.service.DefaultPortrayalService;
import org.geotoolkit.display2d.service.OutputDef;
import org.geotoolkit.display2d.service.SceneDef;
import org.geotoolkit.display2d.service.ViewDef;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    /**
     * This method is ignored by new API
     */
    @Override
    public long getSize(final PortrayalResponse r, final Class<?> c, final Type t, final Annotation[] as, final MediaType mt) {
        /*try {
            return prepare(r, c, t, as, mt);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        }*/
        return -1;
    }

    @Override
    public void writeTo(final PortrayalResponse r, final Class<?> c, final Type t, final Annotation[] as, final MediaType mt,
            final MultivaluedMap<String, Object> h, OutputStream out) throws IOException, WebApplicationException {
        prepare(r, c, t, as, mt);
        out.write(r.getBuffer());
    }

    @Override
    public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
        return PortrayalResponse.class.isAssignableFrom(type);
    }
        
}
