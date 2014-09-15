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

package org.constellation.wmts.ws.rs;

import org.apache.commons.io.IOUtils;
import org.apache.sis.util.logging.Logging;
import org.geotoolkit.coverage.TileReference;
import org.geotoolkit.util.ImageIOUtilities;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
@Provider
@Produces("image/*")
public class StreamResponseWriter implements MessageBodyWriter<TileReference>  {

    private static final Logger LOGGER = Logging.getLogger("org.constellation.map.ws.rs");

    @Override
    public boolean isWriteable(final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt) {
        return TileReference.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(final TileReference t, final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt) {
        return -1;
    }

    @Override
    public void writeTo(final TileReference t, final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt,
            final MultivaluedMap<String, Object> mm, final OutputStream out) throws IOException, WebApplicationException {


        InputStream stream = null;
        if (t.getImageReaderSpi() != null) {
            final String[] baseType = t.getImageReaderSpi().getMIMETypes();
            final String mime = mt.getType()+"/"+mt.getSubtype();
            if(Arrays.asList(baseType).contains(mime)) {
                Object input = t.getInput();
                //we can reuse the input directly
                //try to write the content of the tile if it's alredy in a binary form
                if (input instanceof byte[]) {
                    stream = new ByteArrayInputStream((byte[]) input);
                } else if (input instanceof InputStream) {
                    stream = (InputStream) input;
                } else if (input instanceof URL) {
                    stream = ((URL) input).openStream();
                } else if (input instanceof URI) {
                    stream = ((URI) input).toURL().openStream();
                } else if (input instanceof File) {
                    stream = new FileInputStream((File) input);
                } else {
                    LOGGER.log(Level.WARNING, "Unsupported tyle type : {0}", input.getClass());
                    return;
                }
            }
        }
            
        if (stream == null) {

            final BufferedImage image;
            if (t.getInput() instanceof BufferedImage) {
                image = (BufferedImage) t.getInput();
            } else {
                //we need to recode the input
                final ImageReader reader = t.getImageReader();
                image = reader.read(t.getImageIndex());
                //dispose reader and substream
                ImageIOUtilities.releaseReader(reader);
            }
            
            final ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ImageIO.write(image, mt.getSubtype(), bo);
            bo.flush();
            stream = new ByteArrayInputStream(bo.toByteArray());
        }
        
        try {
            IOUtils.copy(stream, out);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        } finally {
            if(stream != null){
                stream.close();
            }
        }
        
    }
}
