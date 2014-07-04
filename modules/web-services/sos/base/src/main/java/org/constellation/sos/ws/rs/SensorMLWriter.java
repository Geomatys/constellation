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

package org.constellation.sos.ws.rs;

import org.apache.sis.util.logging.Logging;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.geotoolkit.sml.xml.SensorMLMarshallerPool;

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
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@Provider
@Produces("application/xml,text/xml,*/*")
public class SensorMLWriter<T extends AbstractSensorML> implements MessageBodyWriter<T>  {

    private static final Logger LOGGER = Logging.getLogger("org.constellation.ws.rs.provider");

    private static final String SML_101_XSD = "http://www.opengis.net/sensorML/1.0.1 http://schemas.opengis.net/sensorML/1.0.1/sensorML.xsd";

    private static final String SML_100_XSD = "http://www.opengis.net/sensorML/1.0 http://schemas.opengis.net/sensorML/1.0.0/sensorML.xsd";

    @Override
    public boolean isWriteable(final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt) {
        return AbstractSensorML.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(final T t, Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt) {
        return -1;
    }

    @Override
    public void writeTo(final T t, Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt, final MultivaluedMap<String, Object> mm, final OutputStream out) throws IOException, WebApplicationException {
        try {
            final Marshaller m = SensorMLMarshallerPool.getInstance().acquireMarshaller();
            if (t.getVersion() != null && t.getVersion().equals("1.0.1")) {
                m.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, SML_101_XSD);
            } else {
                if (t.getVersion() == null) {
                    LOGGER.warning("there is no version for sensorML file");
                }
                m.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, SML_100_XSD);
            }
            m.marshal(t, out);
            SensorMLMarshallerPool.getInstance().recycle(m);
        } catch (JAXBException ex) {
            LOGGER.log(Level.SEVERE, "JAXB exception while writing the SensorML response", ex);
        }
    }
}
