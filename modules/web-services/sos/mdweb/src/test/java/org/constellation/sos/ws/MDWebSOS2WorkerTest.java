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
import java.sql.Connection;
import java.util.logging.Level;
import javax.xml.bind.Marshaller;
import org.constellation.configuration.DataSourceType;
import org.constellation.configuration.SOSConfiguration;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.test.utils.Order;
import org.constellation.test.utils.TestRunner;
import org.constellation.util.Util;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.util.FileUtilities;
import org.geotoolkit.util.sql.DerbySqlScriptRunner;
import org.geotoolkit.xml.MarshallerPool;

import org.junit.*;
import org.junit.runner.RunWith;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@RunWith(TestRunner.class)
public class MDWebSOS2WorkerTest extends SOS2WorkerTest {

    private static DefaultDataSource ds2 = null;

    private static final File configDir = new File("MDSOSWorkerTest");

    @BeforeClass
    public static void setUpClass() throws Exception {

        final String url2 = "jdbc:derby:memory:MDTest200;create=true";
        ds2 = new DefaultDataSource(url2);

        Connection con2 = ds2.getConnection();

        DerbySqlScriptRunner sr = new DerbySqlScriptRunner(con2);
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/model/mdw_schema_2.4_derby.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ISO19115.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ISO19119.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ISO19108.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/data/defaultRecordSets.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/users/creation_user.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/SensorML_v2.sql"));
        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/profiles/inputLevels.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/sql/sml-data_v2.sql"));

        MarshallerPool pool   = GenericDatabaseMarshallerPool.getInstance();
        Marshaller marshaller =  pool.acquireMarshaller();

        if (configDir.exists()) {
            FileUtilities.deleteDirectory(configDir);
        }

        if (!configDir.exists()) {
            configDir.mkdir();

            //we write the configuration file
            File configFile = new File(configDir, "config.xml");
            Automatic SMLConfiguration = new Automatic();
            BDD smBdd = new BDD("org.apache.derby.jdbc.EmbeddedDriver", url2, "", "");
            SMLConfiguration.setBdd(smBdd);
            SMLConfiguration.setFormat("mdweb");

            Automatic OMConfiguration  = new Automatic();

            SOSConfiguration configuration = new SOSConfiguration(SMLConfiguration, OMConfiguration);
            configuration.setObservationReaderType(DataSourceType.NONE);
            configuration.setObservationWriterType(DataSourceType.NONE);
            configuration.setObservationFilterType(DataSourceType.NONE);

            configuration.setSMLType(DataSourceType.MDWEB);
            configuration.setPhenomenonIdBase("urn:ogc:def:phenomenon:GEOM:");
            configuration.setProfile("transactional");
            configuration.setObservationTemplateIdBase("urn:ogc:object:observation:template:GEOM:");
            configuration.setObservationIdBase("urn:ogc:object:observation:GEOM:");
            configuration.setSensorIdBase("urn:ogc:object:sensor:GEOM:");
            configuration.getParameters().put("transactionSecurized", "false");
            marshaller.marshal(configuration, configFile);

        }
        pool.release(marshaller);
        init();
        worker = new SOSworker("", configDir);
        worker.setServiceUrl(URL);
        worker.setLogLevel(Level.FINER);
    }
    
    @Override
    public void initWorker() {
        worker = new SOSworker("", configDir);
        worker.setServiceUrl(URL);
        worker.setLogLevel(Level.FINER);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        if (worker != null) {
            worker.destroy();
        }
        FileUtilities.deleteDirectory(configDir);
        File derbyLog = new File("derby.log");
        if (derbyLog.exists()) {
            derbyLog.delete();
        }
        File mappingFile = new File("mapping.properties");
        if (mappingFile.exists()) {
            mappingFile.delete();
        }
        if (ds2 != null) {
            ds2.shutdown();
        }
    }



    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
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
