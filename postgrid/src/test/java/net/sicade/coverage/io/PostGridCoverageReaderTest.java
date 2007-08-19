/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2006, Institut de Recherche pour le Développement
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
package net.sicade.coverage.io;

import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import javax.units.SI;
import junit.framework.TestCase;

import org.opengis.coverage.grid.GridCoverage;
import org.opengis.parameter.GeneralParameterValue;

import org.geotools.util.MeasurementRange;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.processing.ColorMap;
import org.geotools.coverage.processing.Operations;


/**
 * Tests the PostGrid plugin.
 *
 * @author Cédric Briançon
 */
public class PostGridCoverageReaderTest extends TestCase {
    /**
     * Tests whether a {@code GridCoverage} has been found for the layer in 
     * the PostGrid DataBase.
     */
    public void testCoriolis() throws Exception {
        final PostGridReader obs = new PostGridReader(null, null, null, "SST (Monde - Coriolis)");
        final GridCoverage original = obs.read(new GeneralParameterValue[0]);
        assertTrue(original instanceof GridCoverage2D);

        final GridCoverage2D coverage = (GridCoverage2D) original;
        final GridSampleDimension[] bands = coverage.getSampleDimensions();
        assertEquals(1, bands.length);

        final RenderedImage image = coverage.getRenderedImage();
        assertNotNull(image);
        assertTrue(image.getColorModel() instanceof IndexColorModel);
        assertEquals(43001, ((IndexColorModel) image.getColorModel()).getMapSize());
        coverage.show("Original");

        final ColorMap colorMap = new ColorMap();
        colorMap.setGeophysicsRange(ColorMap.ANY_QUANTITATIVE_CATEGORY, new MeasurementRange(3, 20, SI.CELSIUS));
        final GridCoverage recolored = Operations.DEFAULT.recolor(original, new ColorMap[] {colorMap});
        assertNotSame(original, recolored);
        assertTrue(recolored instanceof GridCoverage2D);
        ((GridCoverage2D) recolored).show("Recolor");

        Thread.sleep(10000);
    }
}
