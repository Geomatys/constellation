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
import java.util.logging.Logger;
import javax.imageio.ImageWriteParam;
import org.geotoolkit.image.io.plugin.TiffImageWriteParam;
import org.geotoolkit.process.ProcessException;
import org.geotoolkit.process.coverage.resample.ResampleProcess;
import org.opengis.referencing.crs.CompoundCRS;

/**
 *
 * @author guilhem
 */
@Provider
@Produces("image/tiff")
public class GridCoverageWriter<T extends GeotiffResponse> implements MessageBodyWriter<T> {

    private static final Logger LOGGER = Logging.getLogger("org.constellation.coverage.ws.rs");

    @Override
    public boolean isWriteable(final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt) {
        return GeotiffResponse.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(final T t, final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt) {
        return -1;
    }

    @Override
    public void writeTo(final T entry, final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt, final MultivaluedMap<String, Object> mm, final OutputStream out) throws IOException, WebApplicationException {
        final File f = writeInFile(entry);
        byte[] buf = new byte[8192];
        FileInputStream is = new FileInputStream(f);
        int c = 0;
        while ((c = is.read(buf, 0, buf.length)) > 0) {
            out.write(buf, 0, c);
            out.flush();
        }
        out.close();
        is.close();

    }

    public static File writeInFile(final GeotiffResponse entry) throws IOException {
        final GridCoverage2D coverage    = entry.coverage;
        final SpatialMetadata metadata   = entry.metadata;

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

        final IIOImage iioimage    = new IIOImage(outCoverage.getRenderedImage(), null, metadata);
        final ImageWriter iowriter = ImageIO.getImageWritersByFormatName("geotiff").next();

        // TIFF writer do no support writing in output stream currently, we have to write in a file before
        final File f = File.createTempFile(coverage.getName().toString(), ".tiff");
        iowriter.setOutput(f);
        TiffImageWriteParam param = new TiffImageWriteParam(iowriter);
        if (entry.compression != null && !entry.compression.equals("NONE")) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionType(entry.compression);
        }
        if (entry.tiling) {
            param.setTilingMode(ImageWriteParam.MODE_EXPLICIT);
            param.setTiling(entry.tileWidth, entry.tileHeight, 0, 0);
        }
        iowriter.write(null, iioimage, param);
        iowriter.dispose();
        return f;
    }
}