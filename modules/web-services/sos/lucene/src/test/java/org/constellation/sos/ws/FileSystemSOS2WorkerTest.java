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
import org.constellation.util.Util;
import org.geotoolkit.util.FileUtilities;
import org.geotoolkit.xml.MarshallerPool;


import org.junit.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
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
            writeDataFile(offeringV200Directory, "v200/offering-1.xml", "offering-allSensor");


            File phenomenonDirectory = new File(configDir, "phenomenons");
            phenomenonDirectory.mkdir();
            writeDataFile(phenomenonDirectory, "phenomenon-depth.xml", "depth");
            writeDataFile(phenomenonDirectory, "phenomenon-temp.xml",  "temperature");
            writeDataFile(phenomenonDirectory, "phenomenon-depth-temp.xml",  "aggregatePhenomenon");

            File featureDirectory = new File(configDir, "features");
            featureDirectory.mkdir();
            writeDataFile(featureDirectory, "feature1.xml", "station-001");
            writeDataFile(featureDirectory, "feature2.xml", "station-002");
            writeDataFile(featureDirectory, "feature3.xml", "station-006");

            File observationsDirectory = new File(configDir, "observations");
            observationsDirectory.mkdir();
            writeDataFile(observationsDirectory, "observation1.xml", "urn:ogc:object:observation:GEOM:304");
            writeDataFile(observationsDirectory, "observation2.xml", "urn:ogc:object:observation:GEOM:305");
            writeDataFile(observationsDirectory, "observation3.xml", "urn:ogc:object:observation:GEOM:406");
            writeDataFile(observationsDirectory, "observation4.xml", "urn:ogc:object:observation:GEOM:307");
            writeDataFile(observationsDirectory, "observation5.xml", "urn:ogc:object:observation:GEOM:507");
            writeDataFile(observationsDirectory, "observation6.xml", "urn:ogc:object:observation:GEOM:801");

            File observationTemplatesDirectory = new File(configDir, "observationTemplates");
            observationTemplatesDirectory.mkdir();
            writeDataFile(observationTemplatesDirectory, "observationTemplate-3.xml", "urn:ogc:object:observation:template:GEOM:3");
            writeDataFile(observationTemplatesDirectory, "observationTemplate-4.xml", "urn:ogc:object:observation:template:GEOM:4");
            writeDataFile(observationTemplatesDirectory, "observationTemplate-5.xml", "urn:ogc:object:observation:template:GEOM:5");
            writeDataFile(observationTemplatesDirectory, "observationTemplate-7.xml", "urn:ogc:object:observation:template:GEOM:7");
            writeDataFile(observationTemplatesDirectory, "observationTemplate-8.xml", "urn:ogc:object:observation:template:GEOM:8");

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
        pool.release(marshaller);
        init();
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
    public void getCapabilitiesTest() throws Exception {
        super.getCapabilitiesTest();

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
