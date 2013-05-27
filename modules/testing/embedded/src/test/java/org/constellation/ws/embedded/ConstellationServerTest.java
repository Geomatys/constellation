/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
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
package org.constellation.ws.embedded;

import org.constellation.admin.service.ConstellationServer;
import org.geotoolkit.xml.MarshallerPool;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.constellation.sos.ws.soap.SOService;
import org.constellation.test.utils.Order;
import org.constellation.test.utils.TestRunner;
import org.geotoolkit.util.FileUtilities;
import org.junit.*;
import org.opengis.parameter.GeneralParameterDescriptor;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@RunWith(TestRunner.class)
public class ConstellationServerTest extends AbstractGrizzlyServer {

    @BeforeClass
    public static void initPool() throws Exception {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("sos", new SOService());
        initServer(null, map);
        // Get the list of layers
        pool = new MarshallerPool("org.constellation.configuration:"
                                + "org.constellation.generic.database:"
                                + "org.geotoolkit.ows.xml.v110:"
                                + "org.geotoolkit.csw.xml.v202:"
                                + "org.geotoolkit.internal.jaxb.geometry:"
                                + "org.geotoolkit.ows.xml.v100");
    }

    @AfterClass
    public static void shutDown() {
        File f = new File("derby.log");
        if (f.exists()) {
            f.delete();
        }
        //finish();
    }

    @Test
    @Order(order=1)
    public void testgetDescriptor() throws Exception {

        waitForStart();

        final ConstellationServer administrator = ConstellationServer.login("http://localhost:" + grizzly.getCurrentPort(), "", "");
        assertNotNull(administrator);
        GeneralParameterDescriptor desc = administrator.providers.getServiceDescriptor("feature-store");
        assertNotNull(desc);
    }

    @Test
    @Order(order=2)
    public void testImportFile() throws Exception {

        final ConstellationServer administrator = ConstellationServer.login("http://localhost:" + grizzly.getCurrentPort(), "", "");
        assertNotNull(administrator);
        final File f = FileUtilities.getFileFromResource("constellation.CSW.csw2.data.urn-uuid-e8df05c2-d923-4a05-acce-2b20a27c0e58.xml");

        final boolean inserted = administrator.csws.importFile("default", f, "urn-uuid-e8df05c2-d923-4a05-acce-2b20a27c0e58.xml");
        assertTrue(inserted);

        boolean exist = administrator.csws.metadataExist("default", "urn:uuid:e8df05c2-d923-4a05-acce-2b20a27c0e58");
        assertTrue(exist);

        final boolean deleted = administrator.csws.deleteMetadata("default", "urn:uuid:e8df05c2-d923-4a05-acce-2b20a27c0e58");
        assertTrue(deleted);

        exist = administrator.csws.metadataExist("default", "urn:uuid:e8df05c2-d923-4a05-acce-2b20a27c0e58");
        assertFalse(exist);

    }

}
