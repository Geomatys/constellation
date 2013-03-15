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
import org.geotoolkit.swes.xml.SOSResponse;
import org.geotoolkit.sos.xml.SOSResponseWrapper;
import org.geotoolkit.util.logging.Logging;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@Provider
@Produces("application/xml,text/xml,*/*")
public class SOSResponseWriter<T extends SOSResponse> implements MessageBodyWriter<T> {

    private static final Logger LOGGER = Logging.getLogger("org.constellation.sos.ws.rs");

    private static final String SCHEMA_LOCATION_V100 =  "http://www.opengis.net/sos/1.0 http://schemas.opengis.net/sos/1.0.0/sosAll.xsd http://www.opengis.net/sampling/1.0 http://schemas.opengis.net/sampling/1.0.0/sampling.xsd";
    
    private static final String SCHEMA_LOCATION_V200 =  "http://www.opengis.net/sos/2.0 http://schemas.opengis.net/sos/2.0/sos.xsd";
   

    @Override
    public boolean isWriteable(final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt) {
        return SOSResponse.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(final T t, Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt) {
        return -1;
    }

    @Override
    public void writeTo(final T t, Class<?> type, final Type type1, Annotation[] antns, final MediaType mt, final MultivaluedMap<String, Object> mm, final OutputStream out) throws IOException, WebApplicationException {
        try {
            Marshaller m = SOSMarshallerPool.getInstance().acquireMarshaller();
            if ("2.0.0".equals(t.getSpecificationVersion())) {
                m.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, SCHEMA_LOCATION_V200);
            } else {
                m.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, SCHEMA_LOCATION_V100);
            }
            if (t instanceof SOSResponseWrapper) {
                m.marshal(((SOSResponseWrapper)t).getCollection(),  out);
            } else {
                m.marshal(t, out);
            }
             SOSMarshallerPool.getInstance().release(m);
        } catch (JAXBException ex) {
            LOGGER.log(Level.SEVERE, "JAXB exception while writing the SOSResponse File", ex);
        }
    }

}

