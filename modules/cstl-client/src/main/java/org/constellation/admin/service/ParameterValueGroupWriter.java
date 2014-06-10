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

package org.constellation.admin.service;

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
import javax.xml.stream.XMLStreamException;
import org.geotoolkit.xml.parameter.ParameterValueWriter;
import org.opengis.parameter.ParameterValueGroup;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class ParameterValueGroupWriter<T extends ParameterValueGroup> implements MessageBodyWriter<T> {

    @Override
    public boolean isWriteable(Class<?> type, Type type1, Annotation[] antns, MediaType mt) {
        return ParameterValueGroup.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(T t, Class<?> type, Type type1, Annotation[] antns, MediaType mt) {
        return -1;
    }

    @Override
    public void writeTo(T t, Class<?> type, Type type1, Annotation[] antns, MediaType mt, MultivaluedMap<String, Object> mm, OutputStream out) throws IOException, WebApplicationException {
        try {
            ParameterValueWriter writer = new ParameterValueWriter();
            writer.setOutput(out);
            writer.write(t);
        } catch (XMLStreamException ex) {
            Logger.getLogger(ParameterValueGroupWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
