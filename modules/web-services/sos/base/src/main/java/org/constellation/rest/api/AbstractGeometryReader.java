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

import org.geotoolkit.gml.xml.AbstractGeometry;
import org.geotoolkit.gml.xml.GMLMarshallerPool;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * {@link javax.ws.rs.ext.MessageBodyReader} implementation when POST operation send a {@link org.geotoolkit.gml.xml.AbstractGeometry}
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @see MessageBodyReader
 * @see Provider
 * @see Consumes
 * @since 0.9
 */
@Provider
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class AbstractGeometryReader implements MessageBodyReader<AbstractGeometry> {

    private static final Logger LOGGER = Logger.getLogger(AbstractGeometryReader.class.getName());

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return AbstractGeometry.class.isAssignableFrom(type);
    }

    @Override
    public AbstractGeometry readFrom(Class<AbstractGeometry> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        AbstractGeometry context = null;
        try {

            if (mediaType.equals(MediaType.APPLICATION_JSON_TYPE)) {
                /*
                
                TODO
                
                Map<String, String> nSMap = new HashMap<String, String>(0);
                nSMap.put("http://www.constellation.org/config", "constellation-config");
                JettisonConfig config = JettisonConfig.mappedJettison().xml2JsonNs(nSMap).build();
                JettisonJaxbContext cxtx = new JettisonJaxbContext(config, "org.constellation.configuration:" +
                        "org.constellation.generic.database:" +
                        "org.geotoolkit.ogc.xml.v110:" +
                        "org.apache.sis.internal.jaxb.geometry:" +
                        "org.geotoolkit.gml.xml.v311");
                JettisonUnmarshaller jsonUnmarshaller = cxtx.createJsonUnmarshaller();
                context = jsonUnmarshaller.unmarshalFromJSON(entityStream, LayerContext.class);*/
                
            } else {
                final Unmarshaller m = GMLMarshallerPool.getInstance().acquireUnmarshaller();
                Object obj = m.unmarshal(entityStream);
                if (obj instanceof JAXBElement) {
                    obj = ((JAXBElement)obj).getValue();
                }
                context = (AbstractGeometry) obj;
                GMLMarshallerPool.getInstance().recycle(m);
            }

        } catch (JAXBException ex) {
            LOGGER.log(Level.SEVERE, "JAXB exception while writing the layerContext", ex);
        }
        return context;
    }
}
