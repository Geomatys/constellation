/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
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
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;
import com.sun.jersey.api.json.JSONMarshaller;
import com.sun.jersey.json.impl.JSONMarshallerImpl;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.LayerList;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;

import org.apache.sis.util.logging.Logging;


/**
 * Write a layer context response in the stream.
 *
 * @author Guilhem Legal (Geomatys)
 * @author Benjamin Garcia (Geomatys)
 *
 * @version 0.9
 *
 * @see Provider
 * @see Produces
 */
@Provider
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public final class LayerContextWriter implements MessageBodyWriter {

    private static final Logger LOGGER = Logging.getLogger(LayerContextWriter.class);

    @Override
    public boolean isWriteable(Class type, Type type1, Annotation[] antns, MediaType mt) {
        return LayerContext.class.isAssignableFrom(type) || LayerList.class.isAssignableFrom(type);
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
                Map<String, String> nSMap = new HashMap<String, String>(0);
                nSMap.put("http://www.constellation.org/config", "constellation-config");

                // create json marshaller configuration and context
                JSONConfiguration config = JSONConfiguration.mappedJettison().xml2JsonNs(nSMap).build();
                JAXBContext cxtx = new JSONJAXBContext("org.constellation.configuration:" +
                        "org.constellation.generic.database:" +
                        "org.geotoolkit.ogc.xml.v110:" +
                        "org.apache.sis.internal.jaxb.geometry:" +
                        "org.geotoolkit.gml.xml.v311");

                // create marshaller
                JSONMarshaller jsonMarshaller = new JSONMarshallerImpl(cxtx, config);

                // Marshall object
                jsonMarshaller.marshallToJSON(r, out);
            } else {
                // Default : use xml marshaller
                final Marshaller m = GenericDatabaseMarshallerPool.getInstance().acquireMarshaller();
                m.marshal(r, out);
                GenericDatabaseMarshallerPool.getInstance().recycle(m);
            }

        } catch (JAXBException ex) {
            LOGGER.log(Level.SEVERE, "JAXB exception while writing the layerContext", ex);
        }
    }


}
