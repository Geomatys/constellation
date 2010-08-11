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
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.constellation.portrayal.internal.CstlPortrayalService;
import org.constellation.portrayal.internal.PortrayalResponse;

import org.geotoolkit.display.exception.PortrayalException;
import org.geotoolkit.display2d.service.CanvasDef;
import org.geotoolkit.display2d.service.OutputDef;
import org.geotoolkit.display2d.service.SceneDef;
import org.geotoolkit.display2d.service.ViewDef;
import org.geotoolkit.util.ImageIOUtilities;

/**
 * Write a portrayal response in the stream.
 * 
 * @author Johann Sorel (Geomatys)
 */
@Provider
public final class PortrayalResponseWriter implements MessageBodyWriter<PortrayalResponse> {

    @Override
    public long getSize(PortrayalResponse r, Class<?> c, Type t, Annotation[] as, MediaType mt) {
        return -1;
    }

    @Override
    public void writeTo(PortrayalResponse r, Class<?> c, Type t, Annotation[] as, MediaType mt,
            MultivaluedMap<String, Object> h, OutputStream out) throws IOException, WebApplicationException {

        BufferedImage img = r.getImage();
        if(img != null){
            ImageIOUtilities.writeImage(img, mt.toString(), out);
        }else{
            final CanvasDef cdef = r.getCanvasDef();
            final SceneDef sdef = r.getSceneDef();
            final ViewDef vdef = r.getViewDef();
            final OutputDef odef = r.getOutputDef();
            odef.setOutput(out);

            System.out.println("Writing Portrayal response.");

            try {
                CstlPortrayalService.getInstance().portray(sdef, vdef, cdef, odef);
            } catch (PortrayalException ex) {
                //should not happen normally since we asked to never fail.
                throw new IOException(ex);
            }
        }

        
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return PortrayalResponse.class.isAssignableFrom(type);
    }
}
