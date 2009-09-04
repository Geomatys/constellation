/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2009, Geomatys
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
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.constellation.configuration.DataSourceType;
import org.constellation.configuration.ObservationFilterType;
import org.constellation.configuration.ObservationReaderType;
import org.constellation.configuration.ObservationWriterType;
import org.constellation.configuration.SOSConfiguration;
import org.constellation.generic.database.Automatic;
import org.constellation.util.Util;
import org.geotoolkit.sos.xml.v100.Capabilities;
import org.geotoolkit.xml.MarshallerPool;


import org.junit.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class FileSystemSOSWorkerTest extends SOSWorkerTest {


    @BeforeClass
    public static void setUpClass() throws Exception {
        deleteTemporaryFile();

        MarshallerPool pool   = new MarshallerPool(org.constellation.configuration.ObjectFactory.class);
        Marshaller marshaller =  pool.acquireMarshaller();


        File configDir = new File("SOSWorkerTest");
        if (!configDir.exists()) {
            configDir.mkdir();

            //we write the data files
            File offeringDirectory = new File(configDir, "offerings");
            offeringDirectory.mkdir();
            writeDataFile(offeringDirectory, "offering-1.xml", "offering-allSensor");

            File phenomenonDirectory = new File(configDir, "phenomenons");
            phenomenonDirectory.mkdir();
            writeDataFile(phenomenonDirectory, "phenomenon-depth.xml", "depth");
            writeDataFile(phenomenonDirectory, "phenomenon-temp.xml",  "temperature");
            writeDataFile(phenomenonDirectory, "phenomenon-depth-temp.xml",  "aggregatePhenomenon");

            File sensorDirectory = new File(configDir, "sensors");
            sensorDirectory.mkdir();
            writeDataFile(sensorDirectory, "system.xml",    "urn:ogc:object:sensor:GEOM:1");
            writeDataFile(sensorDirectory, "component.xml", "urn:ogc:object:sensor:GEOM:2");

            File featureDirectory = new File(configDir, "features");
            featureDirectory.mkdir();
            writeDataFile(featureDirectory, "feature1.xml", "station-001");
            writeDataFile(featureDirectory, "feature2.xml", "station-002");

            File observationsDirectory = new File(configDir, "observations");
            observationsDirectory.mkdir();
            writeDataFile(observationsDirectory, "observation1.xml", "urn:ogc:object:observation:GEOM:304");
            writeDataFile(observationsDirectory, "observation2.xml", "urn:ogc:object:observation:GEOM:305");
            writeDataFile(observationsDirectory, "observation3.xml", "urn:ogc:object:observation:GEOM:406");
            writeDataFile(observationsDirectory, "observation4.xml", "urn:ogc:object:observation:GEOM:307");
            writeDataFile(observationsDirectory, "observation5.xml", "urn:ogc:object:observation:GEOM:507");

            File observationTemplatesDirectory = new File(configDir, "observationTemplates");
            observationTemplatesDirectory.mkdir();
            writeDataFile(observationTemplatesDirectory, "observationTemplate-3.xml", "urn:ogc:object:observation:template:GEOM:3");
            writeDataFile(observationTemplatesDirectory, "observationTemplate-4.xml", "urn:ogc:object:observation:template:GEOM:4");
            writeDataFile(observationTemplatesDirectory, "observationTemplate-5.xml", "urn:ogc:object:observation:template:GEOM:5");

            //we write the configuration file
            File configFile = new File(configDir, "config.xml");
            Automatic SMLConfiguration = new Automatic();
            SMLConfiguration.setDataDirectory("SOSWorkerTest/sensors");
            Automatic OMConfiguration  = new Automatic();
            OMConfiguration.setDataDirectory("SOSWorkerTest");
            SOSConfiguration configuration = new SOSConfiguration(SMLConfiguration, OMConfiguration);
            configuration.setObservationReaderType(ObservationReaderType.FILESYSTEM);
            configuration.setObservationWriterType(ObservationWriterType.FILESYSTEM);
            configuration.setSMLType(DataSourceType.FILE_SYSTEM);
            configuration.setObservationFilterType(ObservationFilterType.LUCENE);
            configuration.setPhenomenonIdBase("urn:ogc:def:phenomenon:GEOM:");
            configuration.setProfile("transactional");
            configuration.setObservationTemplateIdBase("urn:ogc:object:observation:template:GEOM:");
            configuration.setSensorIdBase("urn:ogc:object:sensor:GEOM:");
            marshaller.marshal(configuration, configFile);

        }
        pool.release(marshaller);
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

    @AfterClass
    public static void tearDownClass() throws Exception {
        deleteTemporaryFile();
    }

    public static void deleteTemporaryFile() {
        File configDirectory = new File("SOSWorkerTest");
        if (configDirectory.exists()) {
            File dataDirectory = new File(configDirectory, "sensors");
            if (dataDirectory.exists()) {
                for (File f : dataDirectory.listFiles()) {
                    f.delete();
                }
                dataDirectory.delete();
            }
            File indexDirectory = new File(configDirectory, "index");
            if (indexDirectory.exists()) {
                for (File f : indexDirectory.listFiles()) {
                    f.delete();
                }
                indexDirectory.delete();
            }
            dataDirectory = new File(configDirectory, "offerings");
            if (dataDirectory.exists()) {
                for (File f : dataDirectory.listFiles()) {
                    f.delete();
                }
                dataDirectory.delete();
            }
            dataDirectory = new File(configDirectory, "observations");
            if (dataDirectory.exists()) {
                for (File f : dataDirectory.listFiles()) {
                    f.delete();
                }
                dataDirectory.delete();
            }
            dataDirectory = new File(configDirectory, "observationTemplates");
            if (dataDirectory.exists()) {
                for (File f : dataDirectory.listFiles()) {
                    f.delete();
                }
                dataDirectory.delete();
            }
            dataDirectory = new File(configDirectory, "features");
            if (dataDirectory.exists()) {
                for (File f : dataDirectory.listFiles()) {
                    f.delete();
                }
                dataDirectory.delete();
            }
            dataDirectory = new File(configDirectory, "phenomenons");
            if (dataDirectory.exists()) {
                for (File f : dataDirectory.listFiles()) {
                    f.delete();
                }
                dataDirectory.delete();
            }
            File conf = new File(configDirectory, "config.xml");
            conf.delete();
            File map = new File(configDirectory, "mapping.properties");
            map.delete();
            configDirectory.delete();
        }
    }

    @Before
    public void setUp() throws Exception {

        marshallerPool = new MarshallerPool("org.geotoolkit.sos.xml.v100:org.geotoolkit.observation.xml.v100:org.geotoolkit.sml.xml.v100:org.geotoolkit.sampling.xml.v100:org.geotoolkit.swe.xml.v101");

        File configDir = new File("SOSWorkerTest");
        worker = new SOSworker(configDir);
        Unmarshaller unmarshaller = marshallerPool.acquireUnmarshaller();
        Capabilities stcapa = (Capabilities) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/SOSCapabilities1.0.0.xml"));
        worker.setSkeletonCapabilities(stcapa);
        worker.setServiceURL(URL);
        marshallerPool.release(unmarshaller);
    }

    @After
    public void tearDown() throws Exception {
        worker.destroy();
    }

    /**
     * Tests the getcapabilities method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    public void getCapabilitiesTest() throws Exception {
        super.getCapabilitiesTest();

    }



    /**
     * Tests the DescribeSensor method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    public void DescribeSensorTest() throws Exception {
       super.DescribeSensorTest();
    }

    /**
     * Tests the GetObservation method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    public void GetObservationTest() throws Exception {
        super.GetObservationTest();
    }

    /**
     * Tests the GetResult method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    public void GetResultTest() throws Exception {
        super.GetResultTest();
    }

    /**
     * Tests the RegisterSensor method
     *
     * @throws java.lang.Exception
     */
    @Test
    @Override
    public void RegisterSensorTest() throws Exception {
        super.RegisterSensorTest();
    }
    
    /**
     * Tests the destroy method
     *
     * @throws java.lang.Exception
     */
    @Ignore
    @Override
    public void destroyTest() throws Exception {
        super.destroyTest();
    }

}
