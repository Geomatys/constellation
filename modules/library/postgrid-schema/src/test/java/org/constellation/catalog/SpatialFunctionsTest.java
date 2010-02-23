/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
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
package org.constellation.catalog;

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
@Deprecated
public class SpatialFunctionsTest extends TestCase {
    /**
     * Tests the parsing and formating of a BOX3D element.
     */
    @Test
    public void testBoxFormat() {
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

    /**
     * Tests some CRS definitions.
     */
    @Test
    public void testCRS() {
        assertEquals(2, CRS.XY  .getCoordinateReferenceSystem().getCoordinateSystem().getDimension());
        assertEquals(3, CRS.XYT .getCoordinateReferenceSystem().getCoordinateSystem().getDimension());
        assertEquals(4, CRS.XYZT.getCoordinateReferenceSystem().getCoordinateSystem().getDimension());
    }
}
