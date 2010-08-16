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

package org.constellation.sos.ws.rs;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.geotoolkit.sos.xml.SOSMarshallerPool;
import org.geotoolkit.sos.xml.SOSResponse;
import org.geotoolkit.sos.xml.SOSResponseWrapper;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@Provider
@Produces("application/xml,text/xml,*/*")
public class SOSResponseWriter<T extends SOSResponse> implements MessageBodyWriter<T> {

    private static final Logger LOGGER = Logger.getLogger("org.constellation.sos.ws.rs");

    private static final String schemaLocation =  "http://www.opengis.net/sos/1.0 http://schemas.opengis.net/sos/1.0.0/sosAll.xsd http://www.opengis.net/sampling/1.0 http://schemas.opengis.net/sampling/1.0.0/sampling.xsd";
   

    @Override
    public boolean isWriteable(Class<?> type, Type type1, Annotation[] antns, MediaType mt) {
        return SOSResponse.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(T t, Class<?> type, Type type1, Annotation[] antns, MediaType mt) {
        return -1;
    }

    @Override
    public void writeTo(T t, Class<?> type, Type type1, Annotation[] antns, MediaType mt, MultivaluedMap<String, Object> mm, OutputStream out) throws IOException, WebApplicationException {
        Marshaller m = null;
        try {
            m = SOSMarshallerPool.getInstance().acquireMarshaller();
            m.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, schemaLocation);
            if (t instanceof SOSResponseWrapper) {
                m.marshal(((SOSResponseWrapper)t).getCollection(),  out);
            } else {
                m.marshal(t, out);
            }
        } catch (JAXBException ex) {
            LOGGER.log(Level.SEVERE, "JAXB exception while writing the SOSResponse File", ex);
        } finally {
            if (m != null) {
                SOSMarshallerPool.getInstance().release(m);
            }
        }
    }

}

