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

package org.constellation.ws.rs.provider;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.logging.Logger;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.constellation.writer.ExceptionFilterWriter;
import org.geotoolkit.ows.xml.ExceptionResponse;
import org.geotoolkit.ows.xml.ExceptionReportMarshallerPool;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@Provider
@Produces("application/xml,text/xml,*/*")
public class ExceptionReportWriter<T extends ExceptionResponse> implements MessageBodyWriter<T>  {

    private static final Logger LOGGER = Logger.getLogger("org.constellation.ws.rs.provider");

    private static final String OWS_110_XSD = "http://www.opengis.net/ows/1.1 http://schemas.opengis.net/ows/1.1.0/owsExceptionReport.xsd";

    private static final String OWS_100_XSD = "http://www.opengis.net/ows http://schemas.opengis.net/ows/1.0.0/owsExceptionReport.xsd";

    @Override
    public boolean isWriteable(Class<?> type, Type type1, Annotation[] antns, MediaType mt) {
        return ExceptionResponse.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(T t, Class<?> type, Type type1, Annotation[] antns, MediaType mt) {
        return -1;
    }

    @Override
    public void writeTo(T t, Class<?> type, Type type1, Annotation[] antns, MediaType mt, MultivaluedMap<String, Object> mm, OutputStream out) throws IOException, WebApplicationException {
        Marshaller m = null;
        try {
            m = ExceptionReportMarshallerPool.getInstance().acquireMarshaller();
            if (t instanceof org.geotoolkit.ows.xml.v100.ExceptionReport) {
                m.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, OWS_100_XSD);
            } else if (t instanceof org.geotoolkit.ows.xml.v110.ExceptionReport) {
                m.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, OWS_110_XSD);
            } else if (t instanceof SchemaLocatedExceptionResponse) {
                SchemaLocatedExceptionResponse response = (SchemaLocatedExceptionResponse) t;
                t = (T)response.getResponse();

                /*
                 * For WMS 1.1.1, we need to define another marshalling pool, with just the service exception
                 * packages. Actually that package does not contain any reference to namespace, consequently
                 * the service exception marshalled file will not contain namespaces definitions.
                 * This is what we want since the service exception report already owns a DTD.
                 */
                if (response.getSchemaLocation().equals("http://schemas.opengis.net/wms/1.1.1/exception_1_1_1.dtd")) {
                    final String enc = "UTF-8";
                    final ExceptionFilterWriter swException = new ExceptionFilterWriter(out, enc);
                    try {
                        swException.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
                        swException.write("<!DOCTYPE ServiceExceptionReport SYSTEM \"http://schemas.opengis.net/wms/1.1.1/exception_1_1_1.dtd\">\n");
                    } catch (IOException io) {
                        throw new JAXBException(io);
                    }
                    m.setProperty(Marshaller.JAXB_FRAGMENT, true);
                    m.marshal(t, swException);
                    return;
                } else {
                    m.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, response.getSchemaLocation());
                }
                
            } else if (!(t instanceof org.geotoolkit.ogc.xml.exception.ServiceExceptionReport)) {
                throw new IllegalArgumentException("unexpected type:" + t.getClass().getName());
            }
            m.marshal(t, out);
            
        } catch (JAXBException ex) {
            LOGGER.severe("JAXB exception while writing the describeLayer response");
        } finally {
            if (m != null) {
                 ExceptionReportMarshallerPool.getInstance().release(m);
            }
        }
    }
}
