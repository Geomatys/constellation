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
import java.util.logging.Logger;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBException;

import org.geotoolkit.sld.xml.XMLUtilities;
import org.geotoolkit.util.logging.Logging;

import org.opengis.sld.StyledLayerDescriptor;
import org.opengis.style.Style;

/**
 * Write a SLD/Style response in the stream.
 * 
 * @author Johann Sorel (Geomatys)
 */
@Provider
public final class StyleResponseWriter implements MessageBodyWriter {

    @Override
    public boolean isWriteable(Class type, Type type1, Annotation[] antns, MediaType mt) {
        return Style.class.isAssignableFrom(type) ||
               StyledLayerDescriptor.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(Object t, Class type, Type type1, Annotation[] antns, MediaType mt) {
        return -1;
    }

    @Override
    public void writeTo(Object r, Class type, Type type1, Annotation[] antns, MediaType mt, 
            MultivaluedMap mm, OutputStream out) throws IOException, WebApplicationException {
        
        
        final XMLUtilities utils = new XMLUtilities();
        
        try{
            if(r instanceof Style){
                utils.writeStyle(out, (Style)r, org.geotoolkit.sld.xml.Specification.StyledLayerDescriptor.V_1_1_0);
            }else if(r instanceof StyledLayerDescriptor){
                utils.writeSLD(out, (StyledLayerDescriptor)r, org.geotoolkit.sld.xml.Specification.StyledLayerDescriptor.V_1_1_0);
            }else{
                throw new IOException("Unhandle object class : " + type);
            }
        }catch(JAXBException ex){
            throw new IOException(ex);
        }
        
    }
}
