/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2008, Geomatys
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

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

@Provider
public class RenderedImageWriter implements MessageBodyWriter<RenderedImage> {

    public long getSize(RenderedImage r, Class<?> c, Type t, Annotation[] as, MediaType mt) {
        return -1;
    }

    public void writeTo(RenderedImage r, Class<?> c, Type t, Annotation[] as, MediaType mt,
            MultivaluedMap<String, Object> h, OutputStream out) throws IOException, WebApplicationException {
        writeImage(r, mt.toString(), out);
    }

    public long getSize(RenderedImage t) {
        return -1;
    }

    /**
     * Write an {@linkplain BufferedImage image} into an output stream, using
     * the mime type specified.
     * 
     * @param image
     *            The image to write into an output stream.
     * @param mime
     *            Mime-type of the output
     * @param output
     *            Output stream containing the image.
     * @throws java.io.IOException
     *             if a writing error occurs.
     */
    private static void writeImage(final RenderedImage image, final String mime, Object output)
            throws IOException {
        if (image == null) {
            throw new NullPointerException("Image can not be null");
        }
        final Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType(mime);
        while (writers.hasNext()) {
            final ImageWriter writer = writers.next();
            final ImageWriterSpi spi = writer.getOriginatingProvider();
            if (spi.canEncodeImage(image)) {
                ImageOutputStream stream = null;
                if (!isValidType(spi.getOutputTypes(), output)) {
                    stream = ImageIO.createImageOutputStream(output);
                    output = stream;
                }
                writer.setOutput(output);
                writer.write(image);
                writer.dispose();
                if (stream != null) {
                    stream.close();
                }
                return;
            }
        }
        throw new IOException("Unknowed image type");
    }

    private static boolean isValidType(final Class<?>[] validTypes, final Object type) {
        for (final Class<?> t : validTypes) {
            if (t.isInstance(type)) {
                return true;
            }
        }
        return false;
    }

    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return RenderedImage.class.isAssignableFrom(type);
    }
}
