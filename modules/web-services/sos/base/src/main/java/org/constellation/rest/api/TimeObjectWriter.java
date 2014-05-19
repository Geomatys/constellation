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

package org.constellation.rest.api;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
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
import org.apache.sis.util.logging.Logging;
import org.geotoolkit.gml.xml.GMLMarshallerPool;
import org.glassfish.jersey.jettison.JettisonConfig;
import org.glassfish.jersey.jettison.JettisonJaxbContext;
import org.glassfish.jersey.jettison.JettisonMarshaller;
import org.opengis.temporal.TemporalGeometricPrimitive;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@Provider
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public final class TimeObjectWriter implements MessageBodyWriter {

    private static final Logger LOGGER = Logging.getLogger(TimeObjectWriter.class);

    @Override
    public boolean isWriteable(Class type, Type type1, Annotation[] antns, MediaType mt) {
        return TemporalGeometricPrimitive.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(Object t, Class type, Type type1, Annotation[] antns, MediaType mt) {
        return -1;
    }

    @Override
    public void writeTo(Object r, Class type, Type type1, Annotation[] antns, MediaType mt,
            MultivaluedMap mm, OutputStream out) throws IOException, WebApplicationException {
        try {
            // if it's a json POST, create a JSonMarshaller.
            if (mt.equals(MediaType.APPLICATION_JSON_TYPE)) {
                //transform xlm namespace to json namespace
                Map<String, String> nSMap = new HashMap<>(0);
                nSMap.put("http://www.opengis.net/gml/3.2", "gml32");
                nSMap.put("http://www.opengis.net/gml", "gml");

                // create json marshaller configuration and context
                JettisonConfig config = JettisonConfig.mappedJettison().xml2JsonNs(nSMap).build();
                JettisonJaxbContext cxtx = new JettisonJaxbContext(config, "org.geotoolkit.gml.xml.v311:org.geotoolkit.gml.xml.v321");

                // create marshaller
                JettisonMarshaller jsonMarshaller = cxtx.createJsonMarshaller();

                // Marshall object
                jsonMarshaller.marshallToJSON(r, out);
            } else {
                // Default : use xml marshaller
                final Marshaller m = GMLMarshallerPool.getInstance().acquireMarshaller();
                m.marshal(r, out);
                GMLMarshallerPool.getInstance().recycle(m);
            }

        } catch (JAXBException ex) {
            LOGGER.log(Level.SEVERE, "JAXB exception while writing the layerContext", ex);
        }
    }
}
