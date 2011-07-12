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

import java.io.File;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import javax.imageio.spi.ServiceRegistry;
import java.util.Iterator;

import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.configuration.DataSourceType;
import org.constellation.sos.io.SensorReader;
import org.constellation.sos.io.SensorWriter;
import org.constellation.sos.io.filesystem.FileSensorReader;
import org.constellation.metadata.io.MetadataIoException;

import org.geotoolkit.util.FileUtilities;

import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class SOSFactoryTest {

    private static SMLFactory smlFactory;

    private static final File configurationDirectory = new File("LuceneSOSTest");
    
    private static final File dataDirectory = new File(configurationDirectory, "data");
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        
        final Iterator<SMLFactory> ite = ServiceRegistry.lookupProviders(SMLFactory.class);
        while (ite.hasNext()) {
            SMLFactory currentFactory = ite.next();
            if (currentFactory.factoryMatchType(DataSourceType.FILE_SYSTEM)) {
                smlFactory = currentFactory;
            }
        }
        if (!configurationDirectory.exists()) {
            configurationDirectory.mkdir();
            dataDirectory.mkdir();
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        if (configurationDirectory.exists()) {
            FileUtilities.deleteDirectory(configurationDirectory);
        }
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
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(OMFactory.OBSERVATION_ID_BASE, "idbase");
        parameters.put(OMFactory.OBSERVATION_TEMPLATE_ID_BASE, "templateIdBase");
        parameters.put(OMFactory.SENSOR_ID_BASE, "sensorBase");
        parameters.put(OMFactory.IDENTIFIER_MAPPING, new Properties());

        boolean exLaunched = false;
        try  {
            SensorReader sr = smlFactory.getSensorReader(DataSourceType.MDWEB, config, parameters);
        } catch (MetadataIoException ex) {
            exLaunched = true;
            assertEquals("The sensor data directory is null", ex.getMessage());
        }
        assertTrue(exLaunched);
        
        try  {
            SensorWriter sw = smlFactory.getSensorWriter(DataSourceType.MDWEB, config, parameters);
        } catch (MetadataIoException ex) {
            exLaunched = true;
            assertEquals("The sensor data directory is null", ex.getMessage());
        }
        assertTrue(exLaunched);
        
        config.setConfigurationDirectory(configurationDirectory);
        config.setDataDirectory(dataDirectory.getPath());

        SensorReader sr = smlFactory.getSensorReader(DataSourceType.MDWEB, config, parameters);
        SensorWriter sw = smlFactory.getSensorWriter(DataSourceType.MDWEB, config, parameters);
        
    }

}
