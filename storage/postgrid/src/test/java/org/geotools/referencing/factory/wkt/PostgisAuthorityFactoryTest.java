/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2007, Geotools Project Managment Committee (PMC)
 *    (C) 2007, Geomatys
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
package org.geotools.referencing.factory.wkt;

import java.util.Set;
import java.util.Collections;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.crs.VerticalCRS;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.metadata.iso.citation.Citations;
import org.constellation.catalog.DatabaseTest;

import org.junit.*;


/**
 * Tests {@link PostgisAuthorityFactoryTest}.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class PostgisAuthorityFactoryTest extends DatabaseTest {
    /**
     * Every tests grouped in a single method in order to get the connection only once.
     */
    @Test
    public void testConnected() throws Exception {
        final PostgisAuthorityFactory factory = new PostgisAuthorityFactory(null, database.getConnection());
        assertEquals(Citations.EPSG, factory.getAuthority());
        assertEquals(4326, factory.getPrimaryKey("4326"));
        assertEquals(4326, factory.getPrimaryKey("EPSG:4326"));
        try {
            assertEquals(4326, factory.getPrimaryKey("DUMMY:4326"));
            fail("Should not find a non-existing authority.");
        } catch (NoSuchAuthorityCodeException e) {
            // This is the expected exception.
        }

        final Set<String> all        = factory.getAuthorityCodes(null);
        final Set<String> objects    = factory.getAuthorityCodes(IdentifiedObject.class);
        final Set<String> geographic = factory.getAuthorityCodes(GeographicCRS.class);
        final Set<String> projected  = factory.getAuthorityCodes(ProjectedCRS.class);
        final Set<String> vertical   = factory.getAuthorityCodes(VerticalCRS.class);

        assertFalse(all       .isEmpty());
        assertFalse(objects   .isEmpty());
        assertFalse(geographic.isEmpty());
        assertFalse(projected .isEmpty());
        assertFalse(vertical  .isEmpty());

        assertEquals(all, objects);
        assertFalse(objects.equals(geographic));
        assertFalse(objects.equals(projected));
        assertFalse(objects.equals(vertical));

        assertTrue(objects.containsAll(geographic));
        assertTrue(objects.containsAll(projected));
        assertTrue(objects.containsAll(vertical));

        assertTrue(Collections.disjoint(geographic, projected));
        assertTrue(Collections.disjoint(geographic, vertical));
        assertTrue(Collections.disjoint(projected,  vertical));

        assertTrue(geographic.contains("4326"));
        final CoordinateReferenceSystem crs = factory.createCoordinateReferenceSystem("EPSG:4326");
        assertTrue(crs instanceof GeographicCRS);
        assertTrue(CRS.equalsIgnoreMetadata(DefaultGeographicCRS.WGS84, crs));

        factory.dispose();
    }
}
