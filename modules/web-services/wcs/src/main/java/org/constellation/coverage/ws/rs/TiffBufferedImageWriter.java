/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2016 Geomatys.
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
package org.constellation.coverage.ws.rs;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.apache.sis.util.logging.Logging;

/**
 *
 * @author guilhem
 */
@Provider
@Produces("image/tiff")
public class TiffBufferedImageWriter<T extends BufferedImage> implements MessageBodyWriter<T> {

    private static final Logger LOGGER = Logging.getLogger("org.constellation.coverage.ws.rs");

    @Override
    public boolean isWriteable(final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt) {
        return BufferedImage.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(final T t, final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt) {
        return -1;
    }

    @Override
    public void writeTo(final T img, final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt, final MultivaluedMap<String, Object> mm, final OutputStream out) throws IOException, WebApplicationException {
        final Iterator<ImageWriter> imageWriterIT = ImageIO.getImageWritersByFormatName("tiff");
        ImageWriter iowriter = null;
        while (imageWriterIT.hasNext()) {
            ImageWriter candidate = imageWriterIT.next();
            if (candidate.getClass().getName().startsWith("com.sun")) {
                iowriter = candidate;
            }
        }
        if (iowriter == null) {
            throw new IOException("No JAI Tiff Writer implementation found");
        }

        ImageOutputStream imgOut = ImageIO.createImageOutputStream(out);
        iowriter.setOutput(imgOut);
        iowriter.write(img);
        iowriter.dispose();
        imgOut.flush();
    }
}
