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

package org.constellation.map.ws.rs;

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
import org.constellation.writer.CapabilitiesFilterWriter;
import org.geotoolkit.util.logging.Logging;
import org.geotoolkit.wms.xml.WMSResponse;
import org.geotoolkit.wms.xml.WMSMarshallerPool;
import org.geotoolkit.wms.xml.v111.WMT_MS_Capabilities;
import org.geotoolkit.wms.xml.v130.WMSCapabilities;
import org.geotoolkit.xml.MarshallerPool;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@Provider
@Produces("application/xml,text/xml,*/*")
public class WMSResponseWriter<T extends WMSResponse> implements MessageBodyWriter<T>  {

    private static final Logger LOGGER = Logging.getLogger("org.constellation.map.ws.rs");

    @Override
    public boolean isWriteable(final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt) {
        return WMSResponse.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(final T t, final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt) {
        return -1;
    }

    @Override
    public void writeTo(final T t, Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt, final MultivaluedMap<String, Object> mm, final OutputStream out) throws IOException, WebApplicationException {
        try {
            //workaround because 1.1.1 is defined with a DTD rather than an XSD
            final MarshallerPool pool;
            final Marshaller m;
            if (t instanceof WMT_MS_Capabilities) {
                final String enc = "UTF8";
                final CapabilitiesFilterWriter swCaps = new CapabilitiesFilterWriter(out, enc);
                final String header;
                if (WMSService.writeDTD) {
                    header = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                             "<!DOCTYPE WMT_MS_Capabilities SYSTEM \"http://schemas.opengis.net/wms/1.1.1/WMS_MS_Capabilities.dtd\">\n";
                } else {
                    header =  "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
                }
                try {
                    swCaps.write(header);
                } catch (IOException ex) {
                    throw new JAXBException(ex);
                }
                pool = WMSMarshallerPool.getInstance();
                m = pool.acquireMarshaller();
                m.setProperty(Marshaller.JAXB_FRAGMENT, true);
                m.marshal(t, swCaps);
                
            } else if (t instanceof WMSCapabilities){
                pool = WMSMarshallerPool.getInstance130();
                m = pool.acquireMarshaller();
                m.marshal(t, out);
                
            } else {
                pool = WMSMarshallerPool.getInstance();
                m = pool.acquireMarshaller();
                m.marshal(t, out);
            } 
            pool.release(m);
        } catch (JAXBException ex) {
            LOGGER.log(Level.SEVERE, "JAXB exception while writing the WMS response", ex);
        }
    }
}
