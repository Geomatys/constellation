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
package org.constellation.test;

import java.awt.geom.AffineTransform;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.util.zip.CRC32;
import javax.media.jai.operator.AffineDescriptor;

import static org.junit.Assert.*;


/**
 * Regroups static methods from <strong>Geotidy</strong>, developped by Martin Desruisseaux.
 * Those methods will be used in the Constellation tests.
 *
 * TODO: delete methods copied from Geotidy when moving to this one for Constellation.
 *
 * @version $Id$
 *
 * @author Cédric Briançon (Geomatys)
 * @author Martin Desruisseaux (Geomatys)
 *
 * @since 0.3
 */
public final class Commons {
    /**
     * Computes the checksum on pixels of the given image. Current implementation assumes that
     * the data type are {@link DataBuffer#TYPE_BYTE}. Note that this computation is sensitive
     * to image tiling, if there is any.
     *
     * TODO: this method comes from Geotidy (module build/tools/gt-test), and has to be removed
     *       when moving to the Geotidy source code for Constellation.
     *
     * @param  image The image for which to compute the checksum.
     * @return The checksum of the given image.
     */
    public static long checksum(final RenderedImage image) {
        assertEquals("Current implementation requires byte data type.",
                DataBuffer.TYPE_BYTE, image.getSampleModel().getDataType());
        final CRC32 sum = new CRC32();
        int ty = image.getMinTileY();
        for (int ny=image.getNumYTiles(); --ny>=0; ty++) {
            int tx = image.getMinTileX();
            for (int nx=image.getNumXTiles(); --nx>=0; tx++) {
                final Raster raster = image.getTile(tx, ty);
                final DataBufferByte buffer = (DataBufferByte) raster.getDataBuffer();
                final int[] offsets = buffer.getOffsets();
                final int size = buffer.getSize();
                for (int i=0; i<offsets.length; i++) {
                    sum.update(buffer.getData(i), offsets[i], size);
                }
            }
        }
        return sum.getValue();
    }

    /**
     * Flip a {@link RenderedImage}. The given image should not be {@code null}.
     *
     * @param image An image to flip. Should not be {@code null}.
     * @return The flipped image.
     */
    public static RenderedImage flip(final RenderedImage image) {
        final AffineTransform flip = new AffineTransform(0, 1, 1, 0, 0, 0);
        final RenderedImage imageFlipped = AffineDescriptor.create(image, flip, null, null, null);
        return imageFlipped;
    }
}
