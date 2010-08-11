/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010, Geomatys
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

import java.net.URLConnection;
import java.net.URL;
import java.io.File;
import javax.xml.bind.JAXBException;
import org.geotoolkit.csw.xml.v202.Capabilities;
import org.geotoolkit.csw.xml.v202.GetCapabilitiesType;
import org.geotoolkit.ebrim.xml.EBRIMMarshallerPool;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class CSWRequestTest extends AbstractTestRequest {

    private static final String CSW_POST_URL = "http://localhost:9090/csw?";

    /**
     * Initialize the list of layers from the defined providers in Constellation's configuration.
     */
    @BeforeClass
    public static void initPool() throws JAXBException {
        // Get the list of layers
        pool = EBRIMMarshallerPool.getInstance();
    }

    @AfterClass
    public static void finish() {
        File f = new File("derby.log");
        if (f.exists()) {
            f.delete();
        }
    }
    /**
     * TODO
     */
    @Test
    public void testCSWGetCapabilities() throws Exception {

        /* Creates a valid GetCapabilities url.
        final URL getCapsUrl = new URL(CSW_POST_URL);


        // for a POST request
        URLConnection conec = getCapsUrl.openConnection();

        final GetCapabilitiesType request = new GetCapabilitiesType("CSW");
        
        postRequestObject(conec, request);
        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof Capabilities);*/

    }
}
