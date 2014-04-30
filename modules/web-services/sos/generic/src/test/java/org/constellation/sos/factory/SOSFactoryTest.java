/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2011, Geomatys
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

package org.constellation.sos.factory;

import javax.imageio.spi.ServiceRegistry;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import org.apache.sis.storage.DataStoreException;
import org.constellation.configuration.DataSourceType;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.sos.io.ObservationFilter;
import org.geotoolkit.observation.ObservationReader;
import org.constellation.ws.CstlServiceException;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class SOSFactoryTest {

    private static OMFactory omFactory;

    @BeforeClass
    public static void setUpClass() throws Exception {
        final Iterator<OMFactory> ite = ServiceRegistry.lookupProviders(OMFactory.class);
        while (ite.hasNext()) {
            OMFactory currentFactory = ite.next();
            if (currentFactory.factoryMatchType(DataSourceType.GENERIC)) {
                omFactory = currentFactory;
            }
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }


     /**
     * Tests the initialisation of the SOS worker with different configuration mistake
     *
     * @throws java.lang.Exception
     */
    @Test
    public void defaultTypeTest() throws Exception {

        final BDD bdd            = new BDD("org.postgresql.driver", "SomeUrl", "boby", "gary");
        final Automatic config   = new Automatic("postgrid", bdd);
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put(OMFactory.OBSERVATION_ID_BASE, "idbase");
        parameters.put(OMFactory.OBSERVATION_TEMPLATE_ID_BASE, "templateIdBase");
        parameters.put(OMFactory.SENSOR_ID_BASE, "sensorBase");

        boolean exLaunched = false;
        try  {
            ObservationFilter of = omFactory.getObservationFilter(DataSourceType.GENERIC, config, parameters);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getMessage(), "Unable to find the filter queries part");
        }
        assertTrue(exLaunched);

        exLaunched = false;
        try  {
            ObservationReader or = omFactory.getObservationReader(DataSourceType.GENERIC, config, parameters);
        } catch (DataStoreException ex) {
            exLaunched = true;
            assertTrue("was:" + ex.getMessage(), ex.getMessage().contains("No suitable driver found for SomeUrl"));
        }
        assertTrue(exLaunched);
    }

}
