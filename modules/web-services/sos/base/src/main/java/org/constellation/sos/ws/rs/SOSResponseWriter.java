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
import org.geotoolkit.sos.xml.SOSMarshallerPool;
import org.geotoolkit.sos.xml.SOSResponseWrapper;
import org.geotoolkit.swes.xml.SOSResponse;

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
@Produces({"application/xml" , "text/xml" , "*/*"})
public class SOSResponseWriter<T extends SOSResponse> implements MessageBodyWriter<T> {

    private static final Logger LOGGER = Logging.getLogger("org.constellation.sos.ws.rs");

    private static final String SCHEMA_LOCATION_V100 =  "http://www.opengis.net/sos/1.0 http://schemas.opengis.net/sos/1.0.0/sosAll.xsd http://www.opengis.net/sampling/1.0 http://schemas.opengis.net/sampling/1.0.0/sampling.xsd";
    
    private static final String SCHEMA_LOCATION_V200 =  "http://www.opengis.net/sos/2.0 http://schemas.opengis.net/sos/2.0/sos.xsd http://www.opengis.net/samplingSpatial/2.0 http://schemas.opengis.net/samplingSpatial/2.0/spatialSamplingFeature.xsd";
   

    @Override
    public boolean isWriteable(final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt) {
        return SOSResponse.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(final T t, Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt) {
        return -1;
    }

    @Override
    public void writeTo(final T t, Class<?> type, final Type type1, Annotation[] antns, final MediaType mt, final MultivaluedMap<String, Object> mm, final OutputStream out) throws IOException, WebApplicationException {
        try {
            final SOSResponse response = (SOSResponse) t;
            Marshaller m = SOSMarshallerPool.getInstance().acquireMarshaller();
            if ("2.0.0".equals(response.getSpecificationVersion())) {
                m.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, SCHEMA_LOCATION_V200);
            } else {
                m.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, SCHEMA_LOCATION_V100);
            }
            if (t instanceof SOSResponseWrapper) {
                m.marshal(((SOSResponseWrapper)t).getCollection(),  out);
                m.marshal(((SOSResponseWrapper)t).getCollection(), System.out);
            } else {
                m.marshal(t, out);
                m.marshal(t, System.out);
            }
             SOSMarshallerPool.getInstance().recycle(m);
        } catch (JAXBException ex) {
            LOGGER.log(Level.SEVERE, "JAXB exception while writing the SOSResponse File", ex);
        }
    }

}

