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

package org.constellation.sos.ws.rs.provider;

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
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamWriter;
import org.codehaus.jettison.mapped.Configuration;
import org.codehaus.jettison.mapped.MappedNamespaceConvention;
import org.codehaus.jettison.mapped.MappedXMLStreamWriter;
import org.geotoolkit.sos.xml.v200.InsertObservationResponseType;
import org.geotoolkit.sos.xml.v200.ObjectFactory;

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
    
    public static final Map<String, String> XML_TO_JSON_NAMESPACES = new HashMap<String,String>();
    static {
        XML_TO_JSON_NAMESPACES.put("http://www.opengis.net/sos/1.0",             "sos");
        XML_TO_JSON_NAMESPACES.put("http://www.opengis.net/sos/2.0",             "sos2");
        XML_TO_JSON_NAMESPACES.put("http://www.opengis.net/swe/1.0",             "swe");
        XML_TO_JSON_NAMESPACES.put("http://www.opengis.net/swe/1.0.1",           "swe1");
        XML_TO_JSON_NAMESPACES.put("http://www.opengis.net/swe/2.0",             "swe2");
        XML_TO_JSON_NAMESPACES.put("http://www.opengis.net/swes/2.0",            "swes");
        XML_TO_JSON_NAMESPACES.put("http://www.opengis.net/gml",                 "gml");
        XML_TO_JSON_NAMESPACES.put("http://www.opengis.net/gml/3.2",             "gml32");
        XML_TO_JSON_NAMESPACES.put("http://www.opengis.net/ows/1.1",             "ows");
        XML_TO_JSON_NAMESPACES.put("http://www.opengis.net/ogc",                 "ogc");
        XML_TO_JSON_NAMESPACES.put("http://www.opengis.net/fes/2.0",             "fes");
        XML_TO_JSON_NAMESPACES.put("http://www.w3.org/1999/xlink",               "xlink");
        XML_TO_JSON_NAMESPACES.put("http://www.opengis.net/sensorML/1.0",        "sml");
        XML_TO_JSON_NAMESPACES.put("http://www.opengis.net/sensorML/1.0.1",      "sml1");
        XML_TO_JSON_NAMESPACES.put("http://www.w3.org/XML/1998/namespace",       "nmsp");
        XML_TO_JSON_NAMESPACES.put("http://www.opengis.net/sampling/1.0",        "sampling");
        XML_TO_JSON_NAMESPACES.put("http://www.opengis.net/sampling/2.0",        "sampling2");
        XML_TO_JSON_NAMESPACES.put("http://www.opengis.net/samplingSpatial/2.0", "samplingSP");
        XML_TO_JSON_NAMESPACES.put("http://www.opengis.net/om/1.0",              "om");
        XML_TO_JSON_NAMESPACES.put("http://www.opengis.net/om/2.0",              "om2");
        XML_TO_JSON_NAMESPACES.put("http://www.cnig.gouv.fr/2005/fra",           "fra");
        XML_TO_JSON_NAMESPACES.put("http://www.isotc211.org/2005/gco",           "gco");
        XML_TO_JSON_NAMESPACES.put("http://www.isotc211.org/2005/gmx",           "gmx");
        XML_TO_JSON_NAMESPACES.put("http://www.isotc211.org/2005/gmi",           "gmi");
        XML_TO_JSON_NAMESPACES.put("http://www.isotc211.org/2005/gmd",           "gmd");
        XML_TO_JSON_NAMESPACES.put("http://www.isotc211.org/2005/gts",           "gts");
        XML_TO_JSON_NAMESPACES.put("urn:us:gov:ic:ism:v2",                       "ism");
        XML_TO_JSON_NAMESPACES.put("http://www.w3.org/2001/XMLSchema-instance",  "xsi");
        XML_TO_JSON_NAMESPACES.put("http://www.w3.org/2005/08/addressing",       "adr");
        XML_TO_JSON_NAMESPACES.put("http://docs.oasis-open.org/wsn/t-1",         "wsn");

    }
   

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
            final Marshaller m = SOSMarshallerPool.getInstance().acquireMarshaller();
            final Object obj;
            if (t instanceof SOSResponseWrapper) {
                obj = ((SOSResponseWrapper)t).getCollection();
            } else if (t instanceof InsertObservationResponseType) {
                final ObjectFactory factory = new ObjectFactory();
                obj = factory.createInsertObservationResponse((InsertObservationResponseType)t);
            } else {
                obj = t;
            }
            
            if (MediaType.APPLICATION_JSON_TYPE.equals(mt)) {
                final Configuration config = new Configuration(XML_TO_JSON_NAMESPACES);
                final MappedNamespaceConvention con = new MappedNamespaceConvention(config);
                final Writer writer = new OutputStreamWriter(out);
                final XMLStreamWriter xmlStreamWriter = new MappedXMLStreamWriter(con, writer);
                m.marshal(obj, xmlStreamWriter);
            } else {
                if ("2.0.0".equals(response.getSpecificationVersion())) {
                    m.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, SCHEMA_LOCATION_V200);
                } else {
                    m.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, SCHEMA_LOCATION_V100);
                }
                m.marshal(obj, out);
            }
            SOSMarshallerPool.getInstance().recycle(m);
        } catch (JAXBException ex) {
            LOGGER.log(Level.SEVERE, "JAXB exception while writing the SOSResponse File", ex);
        }
    }

}

