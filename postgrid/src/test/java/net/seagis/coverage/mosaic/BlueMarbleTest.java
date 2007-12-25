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
package net.seagis.coverage.mosaic;

import java.util.Set;
import org.junit.*;
import static org.junit.Assert.*;

import net.seagis.coverage.catalog.Layer;
import net.seagis.coverage.catalog.LayerTable;
import net.seagis.coverage.catalog.CoverageReference;
import net.seagis.catalog.DatabaseTest;


/**
 * Tests a mosaic using Blue Marble.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class BlueMarbleTest extends DatabaseTest {
    /**
     * The layer name in the database.
     */
    private static final String LAYER = "BlueMarble";

    /**
     * Tests a mosaic without pyramid.
     */
    @Test
    public void testMosaic() throws Exception {
        final Layer layer = database.getTable(LayerTable.class).getEntry(LAYER);
        assertNotNull(layer);

        final Set<CoverageReference> reference = layer.getCoverageReferences();
        assertEquals(1, reference.size());
    }
}
