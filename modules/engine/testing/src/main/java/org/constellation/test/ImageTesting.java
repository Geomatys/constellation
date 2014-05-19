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
package org.constellation.test;

// J2SE dependencies
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.HashSet;
import java.util.Set;
import javax.media.jai.operator.AffineDescriptor;


/**
 * Regroups static methods for image verification in JUnit tests.
 *
 * @author Cédric Briançon (Geomatys)
 * @author Martin Desruisseaux (Geomatys)
 *
 * @since 0.3
 */
public final class ImageTesting {

    private ImageTesting() {}

    /**
     * Verifies that a {@link BufferedImage} is empty or not.
     *
     * @param image The image for which to test the emptiness.
     * @return {@code true} if the image is empty, {@code false} if not.
     */
    public static boolean isImageEmpty(final BufferedImage image) {
        final WritableRaster wr = image.getRaster();
        for (int b=wr.getNumBands(); --b >= 0;) {
            for (int y=image.getHeight(); --y >= 0;) {
                for (int x=image.getWidth(); --x >= 0;) {
                    if (wr.getSample(x, y, b) != 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Returns the number of different colors present in a {@link BufferedImage}.
     * The current implementation only do additions on the different values composing
     * an RGB image, consequently
     *
     * @param image The input image.
     * @return The number of different colors composing an image.
     */
    public static int getNumColors(final BufferedImage image) {
        final Set<Integer> colors = new HashSet<Integer>();
        final int nbBands = image.getSampleModel().getNumBands();
        final int[] pixelValue = new int[nbBands];
        final Raster raster = image.getData();
        for (int x=0, nw=image.getWidth(); x<nw; x++) {
            for (int y=0, nh=image.getHeight(); y<nh; y++) {
                raster.getPixel(x, y, pixelValue);
                int result = 0;
                for (int b=0; b<nbBands; b++) {
                    result += pixelValue[b];
                }
                colors.add(result);
            }
        }
        return colors.size();
    }

    /**
     * Flip a {@link RenderedImage}. The given image should not be {@code null}.
     *
     * @param image An image to flip. Should not be {@code null}.
     * @return The flipped image.
     */
    public static RenderedImage flip(final RenderedImage image) {
        final AffineTransform flip = new AffineTransform(0, 1, 1, 0, 0, 0);
        return  AffineDescriptor.create(image, flip, null, null, null);
    }
}
