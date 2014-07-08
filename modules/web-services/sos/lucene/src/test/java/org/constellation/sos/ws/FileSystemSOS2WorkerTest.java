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
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.test.utils.Order;
import org.constellation.test.utils.SpringTestRunner;
import org.constellation.util.Util;
import org.junit.AfterClass;
import org.junit.BeforeClass;
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
public class FileSystemSOS2WorkerTest extends SOS2WorkerTest {

    @Inject
    private ServiceBusiness serviceBusiness;

    private static File instDirectory; 
    
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        MarshallerPool pool   = GenericDatabaseMarshallerPool.getInstance();
        Marshaller marshaller =  pool.acquireMarshaller();

        final File configDir = ConfigDirectory.setupTestEnvironement("LUCSOSWorkerTest");

        File SOSDirectory  = new File(configDir, "SOS");
        SOSDirectory.mkdir();
        instDirectory = new File(SOSDirectory, "default");
        instDirectory.mkdir();

        //we write the data files
        File offeringDirectory = new File(instDirectory, "offerings");
        offeringDirectory.mkdir();

        File offeringV100Directory = new File(offeringDirectory, "1.0.0");
        offeringV100Directory.mkdir();
        writeDataFile(offeringV100Directory, "v100/offering-1.xml", "offering-allSensor");

        File offeringV200Directory = new File(offeringDirectory, "2.0.0");
        offeringV200Directory.mkdir();
        writeDataFile(offeringV200Directory, "v200/offering-1.xml", "offering-1");
        writeDataFile(offeringV200Directory, "v200/offering-2.xml", "offering-4");
        writeDataFile(offeringV200Directory, "v200/offering-3.xml", "offering-3");
        writeDataFile(offeringV200Directory, "v200/offering-4.xml", "offering-5");
        writeDataFile(offeringV200Directory, "v200/offering-5.xml", "offering-2");
        writeDataFile(offeringV200Directory, "v200/offering-6.xml", "offering-6");
        writeDataFile(offeringV200Directory, "v200/offering-7.xml", "offering-7");
        writeDataFile(offeringV200Directory, "v200/offering-8.xml", "offering-8");
        writeDataFile(offeringV200Directory, "v200/offering-9.xml", "offering-9");
        writeDataFile(offeringV200Directory, "v200/offering-10.xml", "offering-10");


        File phenomenonDirectory = new File(instDirectory, "phenomenons");
        phenomenonDirectory.mkdir();
        writeDataFile(phenomenonDirectory, "phenomenon-depth.xml", "depth");
        writeDataFile(phenomenonDirectory, "phenomenon-temp.xml",  "temperature");
        writeDataFile(phenomenonDirectory, "phenomenon-depth-temp.xml",  "aggregatePhenomenon");

        File featureDirectory = new File(instDirectory, "features");
        featureDirectory.mkdir();
        writeDataFile(featureDirectory, "v200/feature1.xml", "station-001");
        writeDataFile(featureDirectory, "v200/feature2.xml", "station-002");
        writeDataFile(featureDirectory, "v200/feature3.xml", "station-006");

        File observationsDirectory = new File(instDirectory, "observations");
        observationsDirectory.mkdir();
        writeDataFile(observationsDirectory, "v200/observation1.xml", "urn:ogc:object:observation:GEOM:304");
        writeDataFile(observationsDirectory, "v200/observation2.xml", "urn:ogc:object:observation:GEOM:305");
        writeDataFile(observationsDirectory, "v200/observation3.xml", "urn:ogc:object:observation:GEOM:406");
        writeDataFile(observationsDirectory, "v200/observation4.xml", "urn:ogc:object:observation:GEOM:307");
        writeDataFile(observationsDirectory, "v200/observation5.xml", "urn:ogc:object:observation:GEOM:507");
        writeDataFile(observationsDirectory, "v200/observation6.xml", "urn:ogc:object:observation:GEOM:801");

        File observationTemplatesDirectory = new File(instDirectory, "observationTemplates");
        observationTemplatesDirectory.mkdir();
        writeDataFile(observationTemplatesDirectory, "v200/observationTemplate-3.xml", "urn:ogc:object:observation:template:GEOM:3");
        writeDataFile(observationTemplatesDirectory, "v200/observationTemplate-4.xml", "urn:ogc:object:observation:template:GEOM:4");
        writeDataFile(observationTemplatesDirectory, "observationTemplate-5.xml", "urn:ogc:object:observation:template:GEOM:5");
        writeDataFile(observationTemplatesDirectory, "observationTemplate-7.xml", "urn:ogc:object:observation:template:GEOM:7");
        writeDataFile(observationTemplatesDirectory, "observationTemplate-8.xml", "urn:ogc:object:observation:template:GEOM:8");

        File sensorDirectory = new File(instDirectory, "sensors");
        sensorDirectory.mkdir();
        File sensor1         = new File(sensorDirectory, "urn:ogc:object:sensor:GEOM:1.xml");
        sensor1.createNewFile();
        File sensor2         = new File(sensorDirectory, "urn:ogc:object:sensor:GEOM:2.xml");
        sensor2.createNewFile();
        File sensor3         = new File(sensorDirectory, "urn:ogc:object:sensor:GEOM:3.xml");
        sensor3.createNewFile();
        File sensor4         = new File(sensorDirectory, "urn:ogc:object:sensor:GEOM:4.xml");
        sensor4.createNewFile();
        File sensor5         = new File(sensorDirectory, "urn:ogc:object:sensor:GEOM:5.xml");
        sensor5.createNewFile();
        File sensor6         = new File(sensorDirectory, "urn:ogc:object:sensor:GEOM:6.xml");
        sensor6.createNewFile();
        File sensor7         = new File(sensorDirectory, "urn:ogc:object:sensor:GEOM:7.xml");
        sensor7.createNewFile();
        File sensor8         = new File(sensorDirectory, "urn:ogc:object:sensor:GEOM:8.xml");
        sensor8.createNewFile();
        File sensor9         = new File(sensorDirectory, "urn:ogc:object:sensor:GEOM:9.xml");
        sensor9.createNewFile();
        File sensor10        = new File(sensorDirectory, "urn:ogc:object:sensor:GEOM:10.xml");
        sensor10.createNewFile();
        pool.recycle(marshaller);
    }
    
    @PostConstruct
    public void setUp() {
        SpringHelper.setApplicationContext(applicationContext);
        try {
            
            if (!serviceBusiness.getServiceIdentifiers("sos").contains("default")) {
                //we write the configuration file
                Automatic SMLConfiguration = new Automatic();

                Automatic OMConfiguration  = new Automatic();
                OMConfiguration.setDataDirectory(instDirectory.getPath());
                SOSConfiguration configuration = new SOSConfiguration(SMLConfiguration, OMConfiguration);
                configuration.setObservationReaderType(DataSourceType.FILESYSTEM);
                configuration.setObservationWriterType(DataSourceType.FILESYSTEM);
                configuration.setSMLType(DataSourceType.NONE);
                configuration.setObservationFilterType(DataSourceType.LUCENE);
                configuration.setPhenomenonIdBase("urn:ogc:def:phenomenon:GEOM:");
                configuration.setProfile("transactional");
                configuration.setObservationIdBase("urn:ogc:object:observation:GEOM:");
                configuration.setObservationTemplateIdBase("urn:ogc:object:observation:template:GEOM:");
                configuration.setSensorIdBase("urn:ogc:object:sensor:GEOM:");
                configuration.getParameters().put("transactionSecurized", "false");

                serviceBusiness.create("sos", "default", configuration, null, null);


                init();
                worker = new SOSworker("default");
                worker.setServiceUrl(URL);
                worker.setLogLevel(Level.FINER);
            } else if (worker == null) {
                
                serviceBusiness.delete("sos", "default");
                
                //we write the configuration file
                Automatic SMLConfiguration = new Automatic();

                Automatic OMConfiguration  = new Automatic();
                OMConfiguration.setDataDirectory(instDirectory.getPath());
                SOSConfiguration configuration = new SOSConfiguration(SMLConfiguration, OMConfiguration);
                configuration.setObservationReaderType(DataSourceType.FILESYSTEM);
                configuration.setObservationWriterType(DataSourceType.FILESYSTEM);
                configuration.setSMLType(DataSourceType.NONE);
                configuration.setObservationFilterType(DataSourceType.LUCENE);
                configuration.setPhenomenonIdBase("urn:ogc:def:phenomenon:GEOM:");
                configuration.setProfile("transactional");
                configuration.setObservationIdBase("urn:ogc:object:observation:GEOM:");
                configuration.setObservationTemplateIdBase("urn:ogc:object:observation:template:GEOM:");
                configuration.setSensorIdBase("urn:ogc:object:sensor:GEOM:");
                configuration.getParameters().put("transactionSecurized", "false");

                serviceBusiness.create("sos", "default", configuration, null, null);


                init();
                worker = new SOSworker("default");
                worker.setServiceUrl(URL);
                worker.setLogLevel(Level.FINER);
            }
        } catch (Exception ex) {
            Logger.getLogger(FileSystemSOS2WorkerTest.class.getName()).log(Level.SEVERE, null, ex);
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
        ConfigDirectory.shutdownTestEnvironement("LUCSOSWorkerTest");
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

    public static void writeDataFile(File dataDirectory, String resourceName, String identifier) throws IOException {

        File dataFile = new File(dataDirectory, identifier + ".xml");
        FileWriter fw = new FileWriter(dataFile);
        InputStream in = Util.getResourceAsStream("org/constellation/sos/" + resourceName);

        byte[] buffer = new byte[1024];
        int size;

        while ((size = in.read(buffer, 0, 1024)) > 0) {
            fw.write(new String(buffer, 0, size));
        }
        in.close();
        fw.close();
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
     * Tests the GetFeatureOfInterest method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=7)
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
    @Order(order=8)
    public void GetFeatureOfInterestTest() throws Exception {
        super.GetFeatureOfInterestTest();
    }
    
    /**
     * Tests the GetResultTemplate method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=9)
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
    @Order(order=10)
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
    @Order(order=11)
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
    @Order(order=12)
    public void insertObservationTest() throws Exception {
        super.insertObservationTest();
    }

    @Test
    @Override
    @Order(order=13)
    public void insertResultTest() throws Exception {
        super.insertResultTest();
    }
    
    /**
     * Tests the destroy method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    @Order(order=14)
    public void destroyTest() throws Exception {
        super.destroyTest();
    }

}
