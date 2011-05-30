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

package org.constellation.wps.ws.rs;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.stream.XMLStreamException;
import org.geotoolkit.data.DataStoreRuntimeException;
import org.geotoolkit.feature.xml.XmlFeatureWriter;
import org.geotoolkit.feature.xml.jaxp.JAXPStreamFeatureWriter;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.storage.DataStoreException;
import org.geotoolkit.util.logging.Logging;

/**
 *
 * @author Quentin Boileau
 */
@Provider
public class FeatureCollectionWriter<T extends FeatureCollection> implements MessageBodyWriter<T> {

    private static final Logger LOGGER = Logging.getLogger("org.constellation.wps.ws.rs");

    @Override
    public boolean isWriteable(final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt) {
        return FeatureCollection.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(final T t, final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt) {
        return -1;
    }

    @Override
    public void writeTo(final T t, final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt,
            final MultivaluedMap<String, Object> mm, final OutputStream out) throws IOException, WebApplicationException {
        try {
            final XmlFeatureWriter featureWriter = new JAXPStreamFeatureWriter();
            featureWriter.write(t, out);
        
        } catch (XMLStreamException ex) {
            LOGGER.log(Level.SEVERE, "Stax exception while writing the feature collection", ex);
        } catch (DataStoreException ex) {
            LOGGER.log(Level.SEVERE, "DataStore exception while writing the feature collection", ex);
        } catch (DataStoreRuntimeException ex) {
            LOGGER.log(Level.SEVERE, "DataStoreRuntimeException exception while writing the feature collection", ex);
        }
    }

}
