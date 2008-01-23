/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2007, Geomatys
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
package net.seagis.coverage.web;

import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.Transparency;
import java.awt.image.RenderedImage;

import org.geotools.util.logging.Logging;
import org.geotools.coverage.grid.GridCoverage2D;

import net.seagis.coverage.catalog.Layer;
import net.seagis.coverage.catalog.LayerTableTest;
import net.seagis.catalog.DatabaseTest;

import org.junit.Test;


/**
 * Tests {@link WebServiceWorkerTest} with some data from IFREMER.
 *
 * @author Martin Desruisseaux
 */
public class IFREMER_Test extends DatabaseTest {
    /**
     * Tests the MARS3D layer.
     */
    @Test
    public void testMARS() throws WebServiceException, IOException {
        final WebServiceWorker worker = new WebServiceWorker(database);

        worker.setService("WMS", "1.1.1");
        worker.setLayer("Mars3D_Gascogne_(Temp)");
        worker.setCoordinateReferenceSystem("EPSG:3395");
        worker.setTransparency("TRUE");
        worker.setFormat("image/png");
        worker.setTime("2007-05-25T00:00:00Z");
        worker.setElevation("22.5");
        worker.setColormapRange("-2.85,35.25");
        worker.setBoundingBox("-20037508.34,-14066065.457219,20037508.34,14066065.457219");
        worker.setDimension("604","424",null);
        if (false) try {
            // TODO: Need more debugging. We want to avoid creating two consecutive "SampleTranscoder" operations.
            RenderedImage image = worker.getRenderedImage();
            org.geotools.gui.swing.image.OperationTreeBrowser.show(image);
            Thread.sleep(50000);
        } catch (InterruptedException e) {
            // Ignore and go back to work.
        }

        File file = worker.getImageFile();
        RenderedImage image = ImageIO.read(file);
        assertEquals(604, image.getWidth());
        assertEquals(424, image.getHeight());
        assertEquals(Transparency.BITMASK, image.getColorModel().getTransparency());
    }
}
