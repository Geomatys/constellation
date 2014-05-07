/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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
