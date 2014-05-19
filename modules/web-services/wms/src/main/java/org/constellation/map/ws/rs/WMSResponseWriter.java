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
import org.apache.sis.util.logging.Logging;
import org.geotoolkit.wms.xml.WMSResponse;
import org.geotoolkit.wms.xml.WMSMarshallerPool;
import org.geotoolkit.wms.xml.v111.WMT_MS_Capabilities;
import org.geotoolkit.wms.xml.v130.WMSCapabilities;
import org.apache.sis.xml.MarshallerPool;

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
        if (MediaType.APPLICATION_JSON_TYPE.equals(mt)) {
            return false;
        }
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
            pool.recycle(m);
        } catch (JAXBException ex) {
            LOGGER.log(Level.SEVERE, "JAXB exception while writing the WMS response", ex);
        }
    }
}
