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

package org.constellation.metadata.ws.rs.provider;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.logging.Logger;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.xml.bind.JAXBException;
import org.apache.xml.serialize.XMLSerializer;
import org.constellation.jaxb.MarshallWarnings;
import org.constellation.metadata.utils.SerializerResponse;
import org.geotoolkit.csw.xml.CSWMarshallerPool;
import org.geotoolkit.csw.xml.CSWResponse;
import org.geotoolkit.xml.Catching;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@Provider
@Produces("application/xml,text/xml,*/*")
public class CSWResponseWriter<T extends CSWResponse> implements MessageBodyWriter<T>  {

    private static final Logger LOGGER = Logger.getLogger("org.constellation.metadata.ws.rs");

    @Override
    public boolean isWriteable(Class<?> type, Type type1, Annotation[] antns, MediaType mt) {
        return CSWResponse.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(T t, Class<?> type, Type type1, Annotation[] antns, MediaType mt) {
        return -1;
    }

    @Override
    public void writeTo(T t, Class<?> type, Type type1, Annotation[] antns, MediaType mt, MultivaluedMap<String, Object> mm, OutputStream out) throws IOException, WebApplicationException {
        Catching.Marshaller m = null;
        final MarshallWarnings warnings = new MarshallWarnings();
        try {
            m = CSWMarshallerPool.getInstance().acquireMarshaller();
            m.setObjectConverters(warnings);
            if (t instanceof SerializerResponse) {
                final SerializerResponse response = (SerializerResponse) t;
                final XMLSerializer serializer    = response.getSerializer();
                if (serializer != null) {
                    System.out.println("using serializer");
                    serializer.setOutputByteStream(out);
                    m.marshal(response.getResponse(), serializer.asContentHandler());
                } else  {
                    m.marshal(response.getResponse(), out);
                }
            } else {
                m.marshal(t, out);
            }

        } catch (JAXBException ex) {
            LOGGER.severe("JAXB exception while writing the describeLayer response");
        } finally {
            if (m != null) {
                 CSWMarshallerPool.getInstance().release(m);
            }
            if (!warnings.isEmpty()) {
               for (String message : warnings.getMessages()) {
                   LOGGER.warning(message);
               }
            }
        }
    }
}
