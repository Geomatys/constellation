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

package org.constellation.tile.ws.rs;

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
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.geotoolkit.wmts.xml.WMTSMarshallerPool;
import org.geotoolkit.wmts.xml.WMTSResponse;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@Provider
@Produces("application/xml,text/xml,*/*")
public class WMTSResponseWriter<T extends WMTSResponse> implements MessageBodyWriter<T>  {

    private static final Logger LOGGER = Logger.getLogger("org.constellation.map.ws.rs");

    @Override
    public boolean isWriteable(Class<?> type, Type type1, Annotation[] antns, MediaType mt) {
        return WMTSResponse.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(T t, Class<?> type, Type type1, Annotation[] antns, MediaType mt) {
        return -1;
    }

    @Override
    public void writeTo(T t, Class<?> type, Type type1, Annotation[] antns, MediaType mt, MultivaluedMap<String, Object> mm, OutputStream out) throws IOException, WebApplicationException {
        Marshaller m = null;
        try {
            m = WMTSMarshallerPool.getInstance().acquireMarshaller();
            m.marshal(t, out);

        } catch (JAXBException ex) {
            LOGGER.log(Level.SEVERE, "JAXB exception while writing the wmts response", ex);
        } finally {
            if (m != null) {
                 WMTSMarshallerPool.getInstance().release(m);
            }
        }
    }
}
