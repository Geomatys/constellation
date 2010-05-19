/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
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

package org.constellation.wfs.ws.rs;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.logging.Logger;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import org.geotoolkit.data.DataStoreRuntimeException;
import org.geotoolkit.feature.xml.XmlFeatureWriter;
import org.geotoolkit.feature.xml.jaxp.JAXPStreamFeatureWriter;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.storage.DataStoreException;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@Provider
public class FeatureCollectionWriter<T extends FeatureCollection> implements MessageBodyWriter<T> {

    private static final Logger LOGGER = Logger.getLogger("org.constellation.wfs.ws.rs");

    @Override
    public boolean isWriteable(Class<?> type, Type type1, Annotation[] antns, MediaType mt) {
        return FeatureCollection.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(T t, Class<?> type, Type type1, Annotation[] antns, MediaType mt) {
        return -1;
    }

    @Override
    public void writeTo(T t, Class<?> type, Type type1, Annotation[] antns, MediaType mt, MultivaluedMap<String, Object> mm, OutputStream out) throws IOException, WebApplicationException {
        try {
            final XmlFeatureWriter featureWriter = new JAXPStreamFeatureWriter(WFSService.getSchemaLocations());
            featureWriter.write(t, out);
        } catch (JAXBException ex) {
            LOGGER.severe("JAXB exception while writing the feature collection");
        } catch (XMLStreamException ex) {
            LOGGER.severe("Stax exception while writing the feature collection");
        } catch (DataStoreException ex) {
            LOGGER.severe("DataStore exception while writing the feature collection");
        } catch (DataStoreRuntimeException ex) {
            LOGGER.severe("DataStoreRuntimeException exception while writing the feature collection");
        }
    }

}
