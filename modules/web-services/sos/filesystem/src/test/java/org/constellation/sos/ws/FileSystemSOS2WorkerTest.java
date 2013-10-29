/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2010, Geomatys
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

package org.constellation.sos.ws;

// JUnit dependencies
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import javax.xml.bind.Marshaller;
import org.constellation.configuration.DataSourceType;
import org.constellation.configuration.SOSConfiguration;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.test.utils.Order;
import org.constellation.test.utils.TestRunner;
import org.constellation.util.Util;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.admin.ConfigurationEngine;


import org.junit.*;
import org.junit.runner.RunWith;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@RunWith(TestRunner.class)
public class FileSystemSOS2WorkerTest extends SOS2WorkerTest {


    @BeforeClass
    public static void setUpClass() throws Exception {
        MarshallerPool pool   = GenericDatabaseMarshallerPool.getInstance();
        Marshaller marshaller =  pool.acquireMarshaller();

        final File configDir = ConfigurationEngine.setupTestEnvironement("FSSOSWorkerTest");

        File SOSDirectory  = new File(configDir, "SOS");
        SOSDirectory.mkdir();
        final File instDirectory = new File(SOSDirectory, "default");
        instDirectory.mkdir();


        File sensorDirectory = new File(instDirectory, "sensors");
        sensorDirectory.mkdir();
        writeCommonDataFile(sensorDirectory, "system.xml",     "urn:ogc:object:sensor:GEOM:1");
        writeCommonDataFile(sensorDirectory, "component.xml",  "urn:ogc:object:sensor:GEOM:2");
        writeCommonDataFile(sensorDirectory, "component2.xml", "urn:ogc:object:sensor:GEOM:3");

        //we write the configuration file
        Automatic SMLConfiguration = new Automatic();
        SMLConfiguration.setDataDirectory(instDirectory.getPath() + "/sensors");

        Automatic OMConfiguration  = new Automatic();
        SOSConfiguration configuration = new SOSConfiguration(SMLConfiguration, OMConfiguration);
        configuration.setObservationReaderType(DataSourceType.NONE);
        configuration.setObservationWriterType(DataSourceType.NONE);
        configuration.setObservationFilterType(DataSourceType.NONE);

        configuration.setSMLType(DataSourceType.FILESYSTEM);

        configuration.setPhenomenonIdBase("urn:ogc:def:phenomenon:GEOM:");
        configuration.setProfile("transactional");
        configuration.setObservationIdBase("urn:ogc:object:observation:GEOM:");
        configuration.setObservationTemplateIdBase("urn:ogc:object:observation:template:GEOM:");
        configuration.setSensorIdBase("urn:ogc:object:sensor:GEOM:");
        configuration.getParameters().put("transactionSecurized", "false");

        ConfigurationEngine.storeConfiguration("SOS", "default", configuration);

        pool.recycle(marshaller);
        init();
        worker = new SOSworker("default");
        worker.setServiceUrl(URL);
        worker.setLogLevel(Level.FINER);
    }

    @Override
    public void initWorker() {
        worker = new SOSworker("default");
        worker.setServiceUrl(URL);
        worker.setLogLevel(Level.FINER);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        if (worker != null) {
            worker.destroy();
        }
        ConfigurationEngine.shutdownTestEnvironement("FSSOSWorkerTest");
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {

    }

    public static void writeCommonDataFile(File dataDirectory, String resourceName, String identifier) throws IOException {

        File dataFile = new File(dataDirectory, identifier + ".xml");
        FileWriter fw = new FileWriter(dataFile);
        InputStream in = Util.getResourceAsStream("org/constellation/xml/sml/" + resourceName);

        byte[] buffer = new byte[1024];
        int size;

        while ((size = in.read(buffer, 0, 1024)) > 0) {
            fw.write(new String(buffer, 0, size));
        }
        in.close();
        fw.close();
    }


    /**
     * Tests the DescribeSensor method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=1)
    public void DescribeSensorErrorTest() throws Exception {
       super.DescribeSensorErrorTest();
    }

    /**
     * Tests the DescribeSensor method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=2)
    public void DescribeSensorTest() throws Exception {
       super.DescribeSensorTest();
    }


    /**
     * Tests the RegisterSensor method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=3)
    public void RegisterSensorErrorTest() throws Exception {
        super.RegisterSensorErrorTest();
    }

    /**
     * Tests the RegisterSensor method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=4)
    public void RegisterSensorTest() throws Exception {
        super.RegisterSensorTest();
    }

    /**
     * Tests the RegisterSensor method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=5)
    public void DeleteSensorTest() throws Exception {
        super.DeleteSensorTest();
    }
    
    /**
     * Tests the destroy method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=6)
    public void destroyTest() throws Exception {
        super.destroyTest();
    }

}
