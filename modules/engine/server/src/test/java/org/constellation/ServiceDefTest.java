/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
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
package org.constellation;

import org.junit.*;
import static org.junit.Assert.*;


/**
 * Testing class for the {@link ServiceDef} methods of comparaison.
 *
 * @version $Id$
 *
 * @author Cédric Briançon (Geomatys)
 * @since 0.3
 */
public class ServiceDefTest {
    /**
     * Compare versions of two {@link ServiceDef}, and ensures the result is the one
     * waited.
     */
    @Test
    public void testCompareTo() {
        assertTrue(ServiceDef.WMS_1_3_0_SLD.compareTo(ServiceDef.WMS_1_1_1_SLD) > 0);
        assertTrue(ServiceDef.CSW_2_0_2.compareTo(ServiceDef.CSW_2_0_2) == 0);
        assertTrue(ServiceDef.WCS_1_0_0.compareTo("1.1.1") < 0);
    }

    @Test
    public void testGetServiceDefinition() {
        // Ensures that a wrong version number for a service returns null
        final ServiceDef wrongSD = ServiceDef.getServiceDefinition("SOS", "2.4.8");
        assertNull(wrongSD);

        // Ensures a right service definition has been found.
        final ServiceDef sd = ServiceDef.getServiceDefinition("SOS", "1.0.0");
        assertNotNull(sd);
        assertTrue   (sd.version.toString().equals("1.0.0"));
        assertEquals (sd.organization, ServiceDef.Organization.OGC);
        assertEquals (sd.specification, ServiceDef.Specification.SOS);
    }
}
