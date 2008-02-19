/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package net.seagis.coverage.catalog;

import java.util.Locale;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import net.seagis.catalog.Element;
import org.geotools.util.MeasurementRange;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.gui.swing.tree.MutableTreeNode;


/**
 * Information about an image format. The {@linkplain #getName name} should be the MIME type.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface Format extends Element {
    /**
     * Returns the image format name. The returned value should be a name that can be
     * used in a call to {@link javax.imageio.ImageIO#getImageReadersByFormatName}.
     * <p>
     * For compatibility reason, the caller should be prepared to handle MIME type
     * (as understood by {@link javax.imageio.ImageIO#getImageReadersByMIMEType}).
     * as well. As a heuristic rule, we can consider the returned value as a MIME
     * type if it contains the {@code '/'} character.
     */
    String getImageFormat();

    /**
     * Returns the ranges of valid sample values for each band in this format.
     * The range are always expressed in <cite>geophysics</cite> values.
     */
    MeasurementRange[] getSampleValueRanges();

    /**
     * Returns the list of sample dimensions that should be common to every coverage in that format.
     * The array length is equals to the expected number of bands.
     * <p>
     * The sample dimensions specify how to convert pixel values to geophysics values,
     * or conversely. Their type (geophysics or not) is format depedent. For example
     * coverages read from PNG files will typically store their data as integer values
     * (<code>{@linkplain GridSampleDimension#geophysics geophysics}(false)</code>),
     * while coverages read from ASCII files will often store their pixel values as real numbers
     * (<code>{@linkplain GridSampleDimension#geophysics geophysics}(true)</code>).
     */
    GridSampleDimension[] getSampleDimensions();

    /**
     * Returns a legend of the specified dimension. The legend is a
     * {@linkplain org.geotools.gui.swing.image.ColorRamp color ramp}
     * with gratuation on it.
     *
     * @param  dimension The dimension of the image to be returned.
     * @return The color ramp as an image of the specified dimension.
     */
    BufferedImage getLegend(Dimension dimension);

    /**
     * Returns a tree representation of this format, including {@linkplain SampleDimension
     * sample dimensions} and {@linkplain org.geotools.coverage.Category categories}.
     *
     * @param  locale The locale to use for formatting labels in the tree.
     * @return The tree root.
     */
    MutableTreeNode getTree(Locale locale);
}
