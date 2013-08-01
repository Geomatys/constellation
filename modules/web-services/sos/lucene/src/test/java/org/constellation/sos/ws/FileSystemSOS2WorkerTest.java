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
import org.geotoolkit.util.FileUtilities;
import org.apache.sis.xml.MarshallerPool;


import org.junit.*;
import org.junit.runner.RunWith;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@RunWith(TestRunner.class)
public class FileSystemSOS2WorkerTest extends SOS2WorkerTest {

    private static final File configDir = new File("LUCSOSWorkerTest");;

    @BeforeClass
    public static void setUpClass() throws Exception {
        MarshallerPool pool   = GenericDatabaseMarshallerPool.getInstance();
        Marshaller marshaller =  pool.acquireMarshaller();

        if (configDir.exists()) {
            FileUtilities.deleteDirectory(configDir);
        }
        if (!configDir.exists()) {
            configDir.mkdir();

            //we write the data files
            File offeringDirectory = new File(configDir, "offerings");
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


            File phenomenonDirectory = new File(configDir, "phenomenons");
            phenomenonDirectory.mkdir();
            writeDataFile(phenomenonDirectory, "phenomenon-depth.xml", "depth");
            writeDataFile(phenomenonDirectory, "phenomenon-temp.xml",  "temperature");
            writeDataFile(phenomenonDirectory, "phenomenon-depth-temp.xml",  "aggregatePhenomenon");

            File featureDirectory = new File(configDir, "features");
            featureDirectory.mkdir();
            writeDataFile(featureDirectory, "v200/feature1.xml", "station-001");
            writeDataFile(featureDirectory, "v200/feature2.xml", "station-002");
            writeDataFile(featureDirectory, "v200/feature3.xml", "station-006");

            File observationsDirectory = new File(configDir, "observations");
            observationsDirectory.mkdir();
            writeDataFile(observationsDirectory, "v200/observation1.xml", "urn:ogc:object:observation:GEOM:304");
            writeDataFile(observationsDirectory, "v200/observation2.xml", "urn:ogc:object:observation:GEOM:305");
            writeDataFile(observationsDirectory, "v200/observation3.xml", "urn:ogc:object:observation:GEOM:406");
            writeDataFile(observationsDirectory, "v200/observation4.xml", "urn:ogc:object:observation:GEOM:307");
            writeDataFile(observationsDirectory, "v200/observation5.xml", "urn:ogc:object:observation:GEOM:507");
            writeDataFile(observationsDirectory, "v200/observation6.xml", "urn:ogc:object:observation:GEOM:801");

            File observationTemplatesDirectory = new File(configDir, "observationTemplates");
            observationTemplatesDirectory.mkdir();
            writeDataFile(observationTemplatesDirectory, "v200/observationTemplate-3.xml", "urn:ogc:object:observation:template:GEOM:3");
            writeDataFile(observationTemplatesDirectory, "v200/observationTemplate-4.xml", "urn:ogc:object:observation:template:GEOM:4");
            writeDataFile(observationTemplatesDirectory, "observationTemplate-5.xml", "urn:ogc:object:observation:template:GEOM:5");
            writeDataFile(observationTemplatesDirectory, "observationTemplate-7.xml", "urn:ogc:object:observation:template:GEOM:7");
            writeDataFile(observationTemplatesDirectory, "observationTemplate-8.xml", "urn:ogc:object:observation:template:GEOM:8");

             File sensorDirectory = new File(configDir, "sensors");
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
            
            //we write the configuration file
            File configFile = new File(configDir, "config.xml");
            Automatic SMLConfiguration = new Automatic();

            Automatic OMConfiguration  = new Automatic();
            OMConfiguration.setDataDirectory(configDir.getName());
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
            marshaller.marshal(configuration, configFile);

        }
        pool.recycle(marshaller);
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
