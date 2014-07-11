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

package org.constellation.sos.ws;

// JUnit dependencies

import org.apache.sis.xml.MarshallerPool;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.admin.ServiceBusiness;
import org.constellation.admin.SpringHelper;
import org.constellation.configuration.DataSourceType;
import org.constellation.configuration.SOSConfiguration;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.test.utils.Order;
import org.constellation.test.utils.SpringTestRunner;
import org.constellation.util.Util;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.util.sql.DerbySqlScriptRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@RunWith(SpringTestRunner.class)
public class OM2SOS2WorkerTest extends SOS2WorkerTest {

    private static DefaultDataSource ds = null;
    
    @Inject
    private ServiceBusiness serviceBusiness;
    
    private static String url;
    
    private static boolean initialized = false;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        url = "jdbc:derby:memory:OM2Test2;create=true";
        ds = new DefaultDataSource(url);

        Connection con = ds.getConnection();

        DerbySqlScriptRunner sr = new DerbySqlScriptRunner(con);
        sr.setEncoding("UTF-8");
        sr.run(Util.getResourceAsStream("org/constellation/om2/structure_observations.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/sql/sos-data-om2.sql"));


        MarshallerPool pool   = GenericDatabaseMarshallerPool.getInstance();
        Marshaller marshaller =  pool.acquireMarshaller();

        ConfigDirectory.setupTestEnvironement("OM2SOSWorkerTest2");
        
        pool.recycle(marshaller);
    }
    
    @PostConstruct
    public void setUp() {
        SpringHelper.setApplicationContext(applicationContext);
        try {
            
            if (!initialized) {
                serviceBusiness.deleteAll();

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

                serviceBusiness.create("sos", "default", configuration, null, null);
                init();
                worker = new SOSworker("default");
                worker.setServiceUrl(URL);
                worker.setLogLevel(Level.FINER);
                initialized = true;
            }
        } catch (Exception ex) {
            Logger.getLogger(OM2SOS2WorkerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
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
        ConfigDirectory.shutdownTestEnvironement("OM2SOSWorkerTest2");
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
     * Tests the GetObservation method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=6)
    public void GetObservationProfileTest() throws Exception {
        super.GetObservationProfileTest();
    }
    
    /**
     * Tests the GetObservationById method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=7)
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
    @Order(order=8)
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
    @Order(order=9)
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
    @Order(order=10)
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
    @Order(order=11)
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
    @Order(order=12)
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
    @Order(order=13)
    public void GetFeatureOfInterestTest() throws Exception {
        super.GetFeatureOfInterestTest();
    }

    @Test
    @Override
    @Order(order=14)
    public void insertResultTest() throws Exception {
        super.insertResultTest();
    }
    
    @Test
    @Override
    @Order(order=15)
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
    @Order(order=16)
    public void destroyTest() throws Exception {
        super.destroyTest();
    }
}
