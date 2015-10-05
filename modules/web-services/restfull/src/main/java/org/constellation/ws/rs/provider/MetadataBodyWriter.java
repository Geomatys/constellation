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

import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.util.logging.Logging;
import org.constellation.utils.ISOMarshallerPool;
import org.geotoolkit.csw.xml.CSWClassesContext;
import org.glassfish.jersey.jettison.JettisonConfig;
import org.glassfish.jersey.jettison.JettisonJaxbContext;
import org.glassfish.jersey.jettison.JettisonMarshaller;

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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Write a {@linkplain org.apache.sis.metadata.iso.DefaultMetadata metadata} response in the stream.
 *
 * @author Cédric Briançon (Geomatys)
 *
 * @version 0.9
 *
 * @see Provider
 * @see Produces
 */
@Provider
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public final class MetadataBodyWriter implements MessageBodyWriter {
    private static final Map<String, String> XML_TO_JSON_NAMESPACES = new HashMap<>();
    static {
        XML_TO_JSON_NAMESPACES.put("http://www.opengis.net/gml",                 "gml");
        XML_TO_JSON_NAMESPACES.put("http://www.opengis.net/gml/3.2",             "gml32");
        XML_TO_JSON_NAMESPACES.put("http://www.opengis.net/ows/1.1",             "ows");
        XML_TO_JSON_NAMESPACES.put("http://www.opengis.net/ogc",                 "ogc");
        XML_TO_JSON_NAMESPACES.put("http://www.w3.org/1999/xlink",               "xlink");
        XML_TO_JSON_NAMESPACES.put("http://www.w3.org/XML/1998/namespace",       "nmsp");
        XML_TO_JSON_NAMESPACES.put("http://www.cnig.gouv.fr/2005/fra",           "fra");
        XML_TO_JSON_NAMESPACES.put("http://www.isotc211.org/2005/gco",           "gco");
        XML_TO_JSON_NAMESPACES.put("http://www.isotc211.org/2005/gmx",           "gmx");
        XML_TO_JSON_NAMESPACES.put("http://www.isotc211.org/2005/gmd",           "gmd");
    }

    private static final Logger LOGGER = Logging.getLogger("org.constellation.ws.rs.provider");

    @Override
    public boolean isWriteable(Class type, Type type1, Annotation[] antns, MediaType mt) {
        return DefaultMetadata.class.isAssignableFrom(type);
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
                // create json marshaller configuration and context

                JettisonConfig config = JettisonConfig.mappedJettison().xml2JsonNs(XML_TO_JSON_NAMESPACES).build();
                JettisonJaxbContext cxtx = new JettisonJaxbContext(config, CSWClassesContext.getAllClasses());

                // create marshaller
                JettisonMarshaller jsonMarshaller = cxtx.createJsonMarshaller();

                // Marshall object
                jsonMarshaller.marshallToJSON(r, out);
            } else {
                // Default : use xml marshaller
                final Marshaller m = ISOMarshallerPool.getInstance().acquireMarshaller();
                m.marshal(r, out);
                ISOMarshallerPool.getInstance().recycle(m);
            }

        } catch (JAXBException ex) {
            LOGGER.log(Level.SEVERE, "JAXB exception while writing the layerContext", ex);
        }
    }


}
