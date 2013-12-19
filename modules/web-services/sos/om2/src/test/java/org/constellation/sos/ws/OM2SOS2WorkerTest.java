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
import org.geotoolkit.util.sql.DerbySqlScriptRunner;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.admin.ConfigurationEngine;

import org.junit.*;
import org.junit.runner.RunWith;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@RunWith(TestRunner.class)
public class OM2SOS2WorkerTest extends SOS2WorkerTest {

    private static DefaultDataSource ds = null;
    
    @BeforeClass
    public static void setUpClass() throws Exception {

        final String url = "jdbc:derby:memory:OM2Test2;create=true";
        ds = new DefaultDataSource(url);

        Connection con = ds.getConnection();

        DerbySqlScriptRunner sr = new DerbySqlScriptRunner(con);
        sr.setEncoding("UTF-8");
        sr.run(Util.getResourceAsStream("org/constellation/om2/structure_observations.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/sql/sos-data-om2.sql"));


        MarshallerPool pool   = GenericDatabaseMarshallerPool.getInstance();
        Marshaller marshaller =  pool.acquireMarshaller();

        final File workingDirectory = ConfigurationEngine.setupTestEnvironement("OM2SOSWorkerTest2");
        File CSWDirectory  = new File(workingDirectory, "SOS");
        CSWDirectory.mkdir();
        final File instDirectory = new File(CSWDirectory, "default");
        instDirectory.mkdir();

        //we write the configuration file
        Automatic SMLConfiguration = new Automatic();

        Automatic OMConfiguration  = new Automatic();
        BDD bdd = new BDD("org.apache.derby.jdbc.EmbeddedDriver", url, "", "");
        OMConfiguration.setBdd(bdd);
        SOSConfiguration configuration = new SOSConfiguration(SMLConfiguration, OMConfiguration);
        configuration.setObservationReaderType(DataSourceType.OM2);
        configuration.setObservationWriterType(DataSourceType.OM2);
        configuration.setObservationFilterType(DataSourceType.OM2);
        configuration.setSMLType(DataSourceType.NONE);
        configuration.setPhenomenonIdBase("urn:ogc:def:phenomenon:GEOM:");
        configuration.setProfile("transactional");
        configuration.setObservationTemplateIdBase("urn:ogc:object:observation:template:GEOM:");
        configuration.setObservationIdBase("urn:ogc:object:observation:GEOM:");
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
        File derbyLog = new File("derby.log");
        if (derbyLog.exists()) {
            derbyLog.delete();
        }
        File mappingFile = new File("mapping.properties");
        if (mappingFile.exists()) {
            mappingFile.delete();
        }
        if (ds != null) {
            ds.shutdown();
        }
        ConfigurationEngine.shutdownTestEnvironement("OM2SOSWorkerTest2");
    }



    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }


    /**
     * Tests the getcapabilities method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=1)
    public void getCapabilitiesErrorTest() throws Exception {
        super.getCapabilitiesErrorTest();

    }


    /**
     * Tests the getcapabilities method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=2)
    public void getCapabilitiesTest() throws Exception {
        super.getCapabilitiesTest();
    }

    /**
     * Tests the GetObservation method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=3)
    public void GetObservationErrorTest() throws Exception {
        super.GetObservationErrorTest();
    }

    /**
     * Tests the GetObservation method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=4)
    public void GetObservationTest() throws Exception {
        super.GetObservationTest();
    }

    /**
     * Tests the GetObservation method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=5)
    public void GetObservationSamplingCurveTest() throws Exception {
        super.GetObservationSamplingCurveTest();
    }
    
    /**
     * Tests the GetObservationById method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=6)
    public void GetObservationByIdTest() throws Exception {
        super.GetObservationByIdTest();
    }
    
    /**
     * Tests the GetResultTemplate method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=7)
    public void GetResultTemplateTest() throws Exception {
        super.GetResultTemplateTest();
    }

    /**
     * Tests the GetResult method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=8)
    public void GetResultErrorTest() throws Exception {
        super.GetResultErrorTest();
    }

    /**
     * Tests the GetResult method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=9)
    public void GetResultTest() throws Exception {
        super.GetResultTest();
    }

    /**
     * Tests the InsertObservation method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=10)
    public void insertObservationTest() throws Exception {
        super.insertObservationTest();
    }

    /**
     * Tests the GetFeatureOfInterest method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=11)
    public void GetFeatureOfInterestErrorTest() throws Exception {
        super.GetFeatureOfInterestErrorTest();
    }

    /**
     * Tests the GetFeatureOfInterest method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=12)
    public void GetFeatureOfInterestTest() throws Exception {
        super.GetFeatureOfInterestTest();
    }

    @Test
    @Override
    @Order(order=13)
    public void insertResultTest() throws Exception {
        super.insertResultTest();
    }
    
    @Test
    @Override
    @Order(order=14)
    public void GetFeatureOfInterestObservedPropertiesTest() throws Exception {
        super.GetFeatureOfInterestObservedPropertiesTest();
    }
    
    /**
     * Tests the destroy method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=15)
    public void destroyTest() throws Exception {
        super.destroyTest();
    }
}
