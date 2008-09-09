/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2006, Institut de Recherche pour le Développement
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
package org.constellation.image.operation;

// J2SE dependencies
import java.awt.Point;
import java.awt.image.Raster;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.Arrays;


/**
 * Construit une image indexé dont chaque valeurs (entre 0 et 255) apparait à la même fréquence.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class Equalizer {
    /**
     * Interdit la création d'instance de cette classe.
     */
    private Equalizer() {
    }

    /**
     * Retourne un nouveau raster qui contient les valeurs distribuées à fréquence égales.
     */
    public static WritableRaster equalize(final RenderedImage source) {
        final int bands, width, height, txMin, tyMin, txMax, tyMax;
        bands  = source.getSampleModel().getNumBands();
        width  = source.getWidth();
        height = source.getHeight();
        txMax  = source.getNumXTiles() + (txMin = source.getMinTileX());
        tyMax  = source.getNumYTiles() + (tyMin = source.getMinTileY());
        final Pixel[] pixels = new Pixel[width * height];
        for (int i=0; i<pixels.length; i++) {
            pixels[i] = new Pixel();
        }
        final WritableRaster target = Raster.createBandedRaster(DataBuffer.TYPE_BYTE,
                width, height, bands, new Point(source.getMinX(), source.getMinY()));
pband:  for (int band=0; band<bands; band++) {
            int c=0;
            for (int ty=tyMin; ty<tyMax; ty++) {
                for (int tx=txMin; tx<txMax; tx++) {
                    final Raster raster = source.getTile(tx, ty);
                    final int xmin, xmax, ymin, ymax;
                    xmax = (xmin = source.getMinX()) + source.getWidth();
                    ymax = (ymin = source.getMinY()) + source.getHeight();
                    for (int y=ymin; y<ymax; y++) {
                        for (int x=xmin; x<xmax; x++) {
                            final Pixel p = pixels[c++];
                            p.x = x;
                            p.y = y;
                            p.value = raster.getSampleDouble(x, y, band);
                        }
                    }
                }
            }
            if (c != pixels.length) {
                throw new AssertionError(c);
            }
            Arrays.sort(pixels);
            int upper = pixels.length;
            do if (--upper < 0) continue pband;
            while (Double.isNaN(pixels[upper].value));
            final double offset = pixels[0].value;
            final double scale  = 255.0 / upper;
            for (int i=0; i<upper; i++) {
                final Pixel p = pixels[i];
                target.setSample(p.x, p.y, band, (int) (i * scale) + 1);
            }
        }
        return target;
    }
}
