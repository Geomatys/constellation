/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2015 Geomatys.
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

package org.constellation.wfs.ws.rs;

import com.fasterxml.jackson.core.JsonEncoding;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.logging.Logging;
import org.geotoolkit.data.FeatureStoreRuntimeException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotoolkit.data.FeatureStoreUtilities;
import org.geotoolkit.data.FeatureWriter;
import org.geotoolkit.data.geojson.GeoJSONStreamWriter;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@Provider
public class FeatureCollectionGJSWriter<T extends FeatureCollectionWrapper> implements MessageBodyWriter<T> {

    private static final Logger LOGGER = Logging.getLogger("org.constellation.wfs.ws.rs");

    @Override
    public boolean isWriteable(final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt) {
        return FeatureCollectionWrapper.class.isAssignableFrom(type) && mt.toString().equals("application/json");
    }

    @Override
    public long getSize(final T t, final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt) {
        return -1;
    }

    @Override
    public void writeTo(final T t, final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt,
            final MultivaluedMap<String, Object> mm, final OutputStream out) throws IOException, WebApplicationException {
        try {
            FeatureWriter featureWriter = new GeoJSONStreamWriter(out, t.getFeatureCollection().getFeatureType(), JsonEncoding.UTF8, 4, true);
            FeatureStoreUtilities.write(featureWriter, t.getFeatureCollection());
        } catch (DataStoreException ex) {
            LOGGER.log(Level.SEVERE, "DataStore exception while writing the feature collection", ex);
        } catch (FeatureStoreRuntimeException ex) {
            LOGGER.log(Level.SEVERE, "DataStoreRuntimeException exception while writing the feature collection", ex);
        }
    }

}
