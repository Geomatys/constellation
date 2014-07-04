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

package org.constellation.metadata.ws.rs.provider;

import com.sun.xml.bind.marshaller.DataWriter;
import org.apache.sis.util.logging.Logging;
import org.apache.sis.xml.XML;
import org.constellation.jaxb.CstlXMLSerializer;
import org.constellation.jaxb.MarshallWarnings;
import org.constellation.metadata.utils.SerializerResponse;
import org.geotoolkit.csw.xml.CSWMarshallerPool;
import org.geotoolkit.csw.xml.CSWResponse;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Note: replace <T extends CSWResponse> by <T extends Object> because an strange bug arrive with DescribeRecordResponse not passing in this Provider.
 *
 * @author Guilhem Legal (Geomatys)
 */
@Provider
@Produces({"application/xml" , "text/xml" , "*/*"})
public class CSWResponseWriter<T extends Object> implements MessageBodyWriter<T>  {

    private static final Logger LOGGER = Logging.getLogger("org.constellation.metadata.ws.rs");

    @Override
    public boolean isWriteable(final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt) {
        return CSWResponse.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(final T t, Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt) {
        return -1;
    }

    @Override
    public void writeTo(final T t, final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt, final MultivaluedMap<String, Object> mm, final OutputStream out) throws IOException, WebApplicationException {
        final MarshallWarnings warnings = new MarshallWarnings();
        try {
            final Marshaller m = CSWMarshallerPool.getInstance().acquireMarshaller();
            m.setProperty(XML.CONVERTER, warnings);
            if (t instanceof SerializerResponse) {
                final SerializerResponse response = (SerializerResponse) t;
                final CstlXMLSerializer serializer    = response.getSerializer();
                if (serializer != null) {
                    DataWriter writer = new DataWriter(new OutputStreamWriter(out), "UTF-8");
                    writer.setIndentStep("   ");
                    serializer.setContentHandler(writer);
                    m.marshal(response.getResponse(), serializer);
                } else  {
                    m.marshal(response.getResponse(), out);
                }
            } else {
                m.marshal(t, out);
            }
            CSWMarshallerPool.getInstance().recycle(m);

        } catch (JAXBException ex) {
            LOGGER.log(Level.SEVERE, "JAXB exception while writing the CSW response", ex);
        } finally {
            if (!warnings.isEmpty()) {
               for (String message : warnings.getMessages()) {
                   LOGGER.warning(message);
               }
            }
        }
    }
}
