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

package org.constellation.coverage.ws.rs;

import java.io.File;
import java.io.FileInputStream;
import org.apache.sis.util.logging.Logging;
import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.image.io.metadata.SpatialMetadata;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map.Entry;
import java.util.logging.Logger;
import org.constellation.ws.MimeType;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.coverage.resample.ResampleProcess;
import org.opengis.referencing.crs.CompoundCRS;

/**
 *
 * @author guilhem
 */
@Provider
@Produces("application/x-netcdf")
public class GridCoverageNCWriter<T extends Entry> implements MessageBodyWriter<T> {

    private static final Logger LOGGER = Logging.getLogger("org.constellation.coverage.ws.rs");

    @Override
    public boolean isWriteable(final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt) {
        return Entry.class.isAssignableFrom(type) && mt.toString().equals("application/x-netcdf");
    }

    @Override
    public long getSize(final T t, final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt) {
        return -1;
    }

    @Override
    public void writeTo(final T entry, final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt, final MultivaluedMap<String, Object> mm, final OutputStream out) throws IOException, WebApplicationException {
        final GridCoverage2D coverage    = (GridCoverage2D) entry.getKey();
        final SpatialMetadata metadata   = (SpatialMetadata) entry.getValue();
        
        // we don"t support 3D crs writing
        final GridCoverage2D outCoverage;
        if (coverage.getCoordinateReferenceSystem() instanceof CompoundCRS) {
            try {
                outCoverage = new ResampleProcess(coverage, coverage.getCoordinateReferenceSystem2D(), null).executeNow();
            } catch (ProcessException ex) {
                throw new IOException("Erro while reprojecting coverage to 2D projection", ex);
            }
        } else {
            outCoverage = coverage;
        }
        
        IIOImage iioimage                = new IIOImage(outCoverage.getRenderedImage(), null, metadata);
        ImageWriter iowriter             = ImageIO.getImageWritersByFormatName("netcdf").next();
        
        // TIFF writer do no support writing in output stream currently, we have to write in a file before
        File f = File.createTempFile(coverage.getName().toString(), ".nc");
        iowriter.setOutput(f);
        iowriter.write(null, iioimage, null);
        
        byte[] buf = new byte[8192];
        FileInputStream is = new FileInputStream(f);
        int c = 0;
        while ((c = is.read(buf, 0, buf.length)) > 0) {
            out.write(buf, 0, c);
            out.flush();
        }
        out.close();
        is.close();
        

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
