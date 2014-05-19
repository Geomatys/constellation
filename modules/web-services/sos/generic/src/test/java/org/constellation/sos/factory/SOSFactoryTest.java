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

package org.constellation.sos.factory;

import javax.imageio.spi.ServiceRegistry;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import org.apache.sis.storage.DataStoreException;
import org.constellation.configuration.DataSourceType;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.geotoolkit.observation.ObservationFilter;
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
        } catch (DataStoreException ex) {
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
