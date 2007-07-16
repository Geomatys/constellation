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
package net.sicade.observation.coverage.sql;

import java.sql.SQLException;
import net.sicade.observation.CatalogException;
import net.sicade.observation.sql.CRS;
import net.sicade.observation.sql.DatabaseTest;

import org.junit.*;
import static org.junit.Assert.*;


/**
 * Tests {@link CoordinateReferenceSystemTable}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class CoordinateReferenceSystemTableTest extends DatabaseTest {
    /**
     * Tests the {@link CoordinateReferenceSystemTable#getEntry} method.
     */
    @Test
    public void testGetEntry() throws SQLException, CatalogException {
        final CoordinateReferenceSystemTable table = new CoordinateReferenceSystemTable(database);
        assertEquals(CRS.XY  .getCoordinateReferenceSystem(), table.getEntry("IRD:WGS84(xy)"));
        assertEquals(CRS.XYT .getCoordinateReferenceSystem(), table.getEntry("IRD:WGS84(xyt)"));
        assertEquals(CRS.XYZT.getCoordinateReferenceSystem(), table.getEntry("IRD:WGS84(xyzt)"));
        assertEquals(2, CRS.XY  .getCoordinateReferenceSystem().getCoordinateSystem().getDimension());
        assertEquals(3, CRS.XYT .getCoordinateReferenceSystem().getCoordinateSystem().getDimension());
        assertEquals(4, CRS.XYZT.getCoordinateReferenceSystem().getCoordinateSystem().getDimension());
    }
}
