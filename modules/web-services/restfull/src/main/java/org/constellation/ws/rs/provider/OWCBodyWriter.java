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
package org.constellation.ws.rs.provider;

import org.apache.sis.util.logging.Logging;
import org.apache.sis.xml.MarshallerPool;
import org.glassfish.jersey.jettison.JettisonConfig;
import org.glassfish.jersey.jettison.JettisonJaxbContext;
import org.glassfish.jersey.jettison.JettisonMarshaller;
import org.w3._2005.atom.FeedType;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Write a {@linkplain org.w3._2005.atom.FeedType feed} response in the stream.
 *
 * @author Cédric Briançon (Geomatys)
 * @version 0.9
 */
@Provider
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class OWCBodyWriter implements MessageBodyWriter {
    private static final Map<String, String> XML_TO_JSON_NAMESPACES = new HashMap<>();
    static {
        XML_TO_JSON_NAMESPACES.put("http://www.w3.org/2005/Atom",    "atom");
        XML_TO_JSON_NAMESPACES.put("http://www.opengis.net/owc/1.0", "owc");
        XML_TO_JSON_NAMESPACES.put("http://www.georss.org/georss",   "georss");
        XML_TO_JSON_NAMESPACES.put("http://www.opengis.net/gml",     "gml");
    }
    private static final Logger LOGGER = Logging.getLogger("org.constellation.ws.rs.provider");

    @Override
    public boolean isWriteable(Class aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return FeedType.class.isAssignableFrom(aClass);
    }

    @Override
    public long getSize(Object o, Class aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(Object o, Class aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap multivaluedMap, OutputStream out) throws IOException, WebApplicationException {
        try {
            // if it's a json POST, create a JSonMarshaller.
            if (mediaType.equals(MediaType.APPLICATION_JSON_TYPE)) {
                // create json marshaller configuration and context

                JettisonConfig config = JettisonConfig.mappedJettison().xml2JsonNs(XML_TO_JSON_NAMESPACES).build();
                JettisonJaxbContext cxtx = new JettisonJaxbContext(config, "org.geotoolkit.owc.xml.v10:org.w3._2005.atom:org.geotoolkit.georss.xml.v100:org.geotoolkit.gml.xml.v311");

                // create marshaller
                JettisonMarshaller jsonMarshaller = cxtx.createJsonMarshaller();

                // Marshall object
                jsonMarshaller.marshallToJSON(o, out);
            } else {
                // Default : use xml marshaller
                final JAXBContext jaxbCtxt = JAXBContext.newInstance("org.geotoolkit.owc.xml.v10:org.w3._2005.atom:org.geotoolkit.georss.xml.v100:org.geotoolkit.gml.xml.v311");
                final MarshallerPool pool = new MarshallerPool(jaxbCtxt, null);
                final Marshaller marsh = pool.acquireMarshaller();
                marsh.marshal(o, out);
                pool.recycle(marsh);
            }

        } catch (JAXBException ex) {
            LOGGER.log(Level.SEVERE, "JAXB exception while writing the layerContext", ex);
        }
    }
}
