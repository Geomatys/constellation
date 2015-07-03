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

package org.constellation.wfs.ws.rs;

import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.logging.Logging;
import org.geotoolkit.data.FeatureStoreRuntimeException;
import org.geotoolkit.feature.xml.XmlFeatureWriter;
import org.geotoolkit.feature.xml.jaxp.JAXPStreamFeatureWriter;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotoolkit.data.FeatureIterator;
import org.geotoolkit.data.memory.GenericEmptyFeatureIterator;
import org.geotoolkit.feature.Feature;
import static org.constellation.wfs.ws.WFSConstants.GML_3_1_1;
import static org.constellation.wfs.ws.WFSConstants.GML_3_2_1;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@Provider
public class FeatureCollectionWriter<T extends FeatureCollectionWrapper> implements MessageBodyWriter<T> {

    private static final Logger LOGGER = Logging.getLogger("org.constellation.wfs.ws.rs");

    @Override
    public boolean isWriteable(final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt) {
        return FeatureCollectionWrapper.class.isAssignableFrom(type) && (mt.equals(GML_3_1_1) || mt.equals(GML_3_2_1));
    }

    @Override
    public long getSize(final T t, final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt) {
        return -1;
    }

    @Override
    public void writeTo(final T t, final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt,
            final MultivaluedMap<String, Object> mm, final OutputStream out) throws IOException, WebApplicationException {
        try {
            final XmlFeatureWriter featureWriter = new JAXPStreamFeatureWriter(t.getGmlVersion(), t.getWfsVersion(), t.getSchemaLocations());
            if(t.isWriteSingleFeature()){
                //write a single feature without collection element container
                final FeatureIterator ite = t.getFeatureCollection().iterator();
                try{
                    if(ite.hasNext()){
                        Feature f = ite.next();
                        featureWriter.write(f, out, t.getNbMatched());
                    }else{
                        //write an empty collection
                        featureWriter.write(GenericEmptyFeatureIterator.wrap(t.getFeatureCollection()), out, t.getNbMatched());
                    }
                }finally{
                    ite.close();
                }
            }else{
                featureWriter.write(t.getFeatureCollection(), out, t.getNbMatched());
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Exception while writing the feature collection", ex);
        }
    }

}
