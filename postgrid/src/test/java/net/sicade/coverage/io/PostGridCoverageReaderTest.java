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

import java.io.IOException;
import junit.framework.TestCase;
import org.geotools.coverage.grid.GridCoverage2D;


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
    public void testGridCoverage() throws IOException {
        final PostGridReader obs = new PostGridReader(null, null, null, "SST (Monde - Coriolis)");
        GridCoverage2D gridCoverage2D = (GridCoverage2D) obs.read(null);
        assertNotNull(gridCoverage2D);        
    }
}
