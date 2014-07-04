/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


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
