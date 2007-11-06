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
package net.seagis.catalog;

import java.sql.SQLException;
import org.opengis.geometry.Envelope;

import org.junit.*;
import junit.framework.TestCase;


/**
 * Tests the {@code "spatialExtent"} column in the {@code "GridGeometries"} table.
 * 
 * @version $Id$
 * @author Cédric Briançon
 * @author Martin Desruisseaux
 */
public class SpatialFunctionsTest extends TestCase {
    /**
     * Tests the parsing and formating of a BOX3D element.
     */
    @Test
    public void testBoxFormat() throws SQLException {
        String wkt;
        Envelope envelope;

        // 2D case
        wkt = "BOX(-180 -90,180 90)";
        envelope = SpatialFunctions.parse(wkt);
        assertEquals(2, envelope.getDimension());
        assertEquals(-180, envelope.getMinimum(0), 0);
        assertEquals( 180, envelope.getMaximum(0), 0);
        assertEquals( -90, envelope.getMinimum(1), 0);
        assertEquals(  90, envelope.getMaximum(1), 0);
        assertEquals("BOX2D(-180.0 -90.0,180.0 90.0)", SpatialFunctions.formatBox(envelope));

        final String polygon = SpatialFunctions.formatPolygon(envelope);
        assertEquals("POLYGON((-180.0 -90.0,-180.0 90.0,180.0 90.0,180.0 -90.0,-180.0 -90.0))", polygon);
        assertEquals(envelope, SpatialFunctions.parse(polygon));

        // 3D case
        wkt = "BOX3D(-180 -90 10,180 90 30)";
        envelope = SpatialFunctions.parse(wkt);
        assertEquals(3, envelope.getDimension());
        assertEquals(-180, envelope.getMinimum(0), 0);
        assertEquals( 180, envelope.getMaximum(0), 0);
        assertEquals( -90, envelope.getMinimum(1), 0);
        assertEquals(  90, envelope.getMaximum(1), 0);
        assertEquals(  10, envelope.getMinimum(2), 0);
        assertEquals(  30, envelope.getMaximum(2), 0);
        assertEquals("BOX3D(-180.0 -90.0 10.0,180.0 90.0 30.0)", SpatialFunctions.formatBox(envelope));
    }
}
