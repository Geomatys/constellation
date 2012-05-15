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
import javax.xml.bind.JAXBException;
import java.io.File;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;
import org.junit.*;
import org.opengis.parameter.GeneralParameterDescriptor;
import static org.junit.Assert.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class ConstellationServerTest extends AbstractTestRequest {
    
    @BeforeClass
    public static void initPool() throws JAXBException {
        // Get the list of layers
        pool = new MarshallerPool("org.constellation.configuration:"
                                + "org.constellation.generic.database:"
                                + "org.geotoolkit.ows.xml.v110:"
                                + "org.geotoolkit.csw.xml.v202:"
                                + "org.geotoolkit.internal.jaxb.geometry:"
                                + "org.geotoolkit.ows.xml.v100");
    }
    
    @AfterClass
    public static void finish() {
        File f = new File("derby.log");
        if (f.exists()) {
            f.delete();
        }
    }
    
    @Test
    public void testgetDescriptor() throws Exception {
        
        waitForStart();
        
        final ConstellationServer administrator = ConstellationServer.login("http://localhost:9090/", "", "");
        assertNotNull(administrator);
        GeneralParameterDescriptor desc = administrator.providers.getServiceDescriptor("shapefile");
        assertNotNull(desc);
    }
    
}
