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

import org.geotoolkit.sld.xml.StyleXmlIO;
import org.opengis.sld.StyledLayerDescriptor;
import org.opengis.style.Style;

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
        
        
        final StyleXmlIO utils = new StyleXmlIO();
        
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
