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
import org.constellation.business.IServiceBusiness;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.admin.SpringHelper;
import org.constellation.configuration.DataSourceType;
import org.constellation.configuration.SOSConfiguration;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.test.utils.Order;
import org.constellation.test.utils.SpringTestRunner;
import org.constellation.util.Util;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.xml.bind.Marshaller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@RunWith(SpringTestRunner.class)
public class FileSystemSOSWorkerTest extends SOSWorkerTest {

    @Inject
    private IServiceBusiness serviceBusiness;

    private static File instDirectory; 
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        MarshallerPool pool   = GenericDatabaseMarshallerPool.getInstance();
        Marshaller marshaller =  pool.acquireMarshaller();

        final File configDir = ConfigDirectory.setupTestEnvironement("FSSOSWorkerTest");

        File CSWDirectory  = new File(configDir, "SOS");
        CSWDirectory.mkdirs();
        instDirectory = new File(CSWDirectory, "default");
        instDirectory.mkdirs();

        File sensorDirectory = new File(instDirectory, "sensors");
        sensorDirectory.mkdirs();
        writeCommonDataFile(sensorDirectory, "system.xml",    "urn:ogc:object:sensor:GEOM:1");
        writeCommonDataFile(sensorDirectory, "component.xml", "urn:ogc:object:sensor:GEOM:2");
        pool.recycle(marshaller);
    }
    
    @PostConstruct
    public void setUp() {
        SpringHelper.setApplicationContext(applicationContext);
        try {
            
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
            
            if (!serviceBusiness.getServiceIdentifiers("sos").contains("default")) {
                //we write the configuration file
                
                serviceBusiness.create("sos", "default", configuration, null, null);

                init();
                worker = new SOSworker("default");
                worker.setServiceUrl(URL);
                worker.setLogLevel(Level.FINER);
                
            } else if (worker == null) {
                serviceBusiness.delete("sos", "default");
                
                setUpClass();
                
                serviceBusiness.create("sos", "default", configuration, null, null);

                init();
                worker = new SOSworker("default");
                worker.setServiceUrl(URL);
                worker.setLogLevel(Level.FINER);
                 
            }
        } catch (Exception ex) {
            Logger.getLogger(FileSystemSOSWorkerTest.class.getName()).log(Level.SEVERE, null, ex);
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
        ConfigDirectory.shutdownTestEnvironement("FSSOSWorkerTest");
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
