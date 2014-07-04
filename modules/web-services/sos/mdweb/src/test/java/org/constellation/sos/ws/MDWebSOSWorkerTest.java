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
import org.constellation.admin.ConfigurationEngine;
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
import org.junit.runner.RunWith;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.constellation.sos.ws.SOS2WorkerTest.worker;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@RunWith(SpringTestRunner.class)
public class MDWebSOSWorkerTest extends SOSWorkerTest {

    @Inject
    private ServiceBusiness serviceBusiness;
    
    private static DefaultDataSource ds2 = null;

    private static String url2;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        url2 = "jdbc:derby:memory:MDTest2;create=true";
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

        final File configDir = ConfigurationEngine.setupTestEnvironement("MDSOSWorkerTest");

        File CSWDirectory  = new File(configDir, "SOS");
        CSWDirectory.mkdir();
        final File instDirectory = new File(CSWDirectory, "default");
        instDirectory.mkdir();
        
        pool.recycle(marshaller);
    }
    
    @PostConstruct
    public void setUp() {
        SpringHelper.setApplicationContext(applicationContext);
        try {
            
            //we write the configuration file
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
            
            if (!serviceBusiness.getServiceIdentifiers("sos").contains("default")) {
                serviceBusiness.create("sos", "default", configuration, null, null);
                init();
                worker = new SOSworker("default");
                worker.setServiceUrl(URL);
                worker.setLogLevel(Level.FINER);
            
            } else if (worker == null) {
                serviceBusiness.delete("sos", "default");
                
                serviceBusiness.create("sos", "default", configuration, null, null);

                init();
                worker = new SOSworker("default");
                worker.setServiceUrl(URL);
                worker.setLogLevel(Level.FINER);
            }
            
            
        } catch (Exception ex) {
            Logger.getLogger(MDWebSOSWorkerTest.class.getName()).log(Level.SEVERE, null, ex);
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
        if (ds2 != null) {
            ds2.shutdown();
        }
        ConfigurationEngine.shutdownTestEnvironement("MDSOSWorkerTest");
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
     * Tests the destroy method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=5)
    public void destroyTest() throws Exception {
        super.destroyTest();
    }
}
