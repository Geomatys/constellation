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

package org.constellation.coverage.ws.rs;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map.Entry;
import java.util.logging.Logger;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.image.io.metadata.SpatialMetadata;

/**
 *
 * @author guilhem
 */
@Provider
@Produces("image/geotiff")
public class GridCoverageWriter<T extends Entry> implements MessageBodyWriter<T> {

    private static final Logger LOGGER = Logger.getLogger("org.constellation.coverage.ws.rs");

    @Override
    public boolean isWriteable(Class<?> type, Type type1, Annotation[] antns, MediaType mt) {
        return Entry.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(T t, Class<?> type, Type type1, Annotation[] antns, MediaType mt) {
        return -1;
    }

    @Override
    public void writeTo(T entry, Class<?> type, Type type1, Annotation[] antns, MediaType mt, MultivaluedMap<String, Object> mm, OutputStream out) throws IOException, WebApplicationException {
        final GridCoverage2D coverage    = (GridCoverage2D) entry.getKey();
        final SpatialMetadata metadata   = (SpatialMetadata) entry.getValue();
        IIOImage iioimage                = new IIOImage(coverage.getRenderedImage(), null, metadata);
        ImageWriter iowriter             = ImageIO.getImageWritersByFormatName("geotiff").next();
        /*System.out.println("iowriter:" + iowriter.getClass().getName());
        System.out.println("out:" + out.getClass().getName());*/
        iowriter.setOutput(ImageIO.createImageOutputStream(out));
        iowriter.write(null, iioimage, null);

        /*final ImageCoverageWriter writer = new ImageCoverageWriter();
        
        try {
            writer.setOutput(out);
            GridCoverageWriteParam param = new GridCoverageWriteParam();
            param.setFormatName("geotiff");
            writer.write(coverage, param);
           
        } catch (CoverageStoreException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            throw new IOException(ex);
        } finally {
            try {
                writer.dispose();
            } catch (CoverageStoreException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }*/

    }
}
