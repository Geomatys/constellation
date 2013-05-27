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
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.geotoolkit.sml.xml.SensorMLMarshallerPool;
import org.geotoolkit.util.logging.Logging;

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
            SensorMLMarshallerPool.getInstance().release(m);
        } catch (JAXBException ex) {
            LOGGER.log(Level.SEVERE, "JAXB exception while writing the SensorML response", ex);
        }
    }
}
