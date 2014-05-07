/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
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
package org.constellation.ws.rs.provider;

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

import org.apache.sis.metadata.iso.DefaultMetadata;
import org.geotoolkit.csw.xml.CSWClassesContext;
import org.glassfish.jersey.jettison.JettisonConfig;
import org.glassfish.jersey.jettison.JettisonJaxbContext;
import org.glassfish.jersey.jettison.JettisonMarshaller;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;

import org.apache.sis.util.logging.Logging;
import org.constellation.utils.ISOMarshallerPool;


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

    private static final Logger LOGGER = Logging.getLogger(MetadataBodyWriter.class);

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
