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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.constellation.configuration.DataSourceType;
import org.constellation.configuration.ObservationFilterType;
import org.constellation.configuration.ObservationReaderType;
import org.constellation.configuration.SOSConfiguration;
import org.constellation.generic.database.Automatic;
import org.constellation.ows.v110.AcceptFormatsType;
import org.constellation.ows.v110.AcceptVersionsType;
import org.constellation.ows.v110.SectionsType;
import org.constellation.sml.AbstractSensorML;
import org.constellation.sos.v100.Capabilities;
import org.constellation.sos.v100.DescribeSensor;
import org.constellation.sos.v100.GetCapabilities;
import org.constellation.util.Util;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.rs.NamespacePrefixMapperImpl;
import static org.constellation.ows.OWSExceptionCode.*;

// JUnit dependencies
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class SOSWorkerTest {

    private SOSworker worker;

    private Unmarshaller unmarshaller;

    @BeforeClass
    public static void setUpClass() throws Exception {
        deleteTemporaryFile();

        JAXBContext context = JAXBContext.newInstance(org.constellation.configuration.ObjectFactory.class);
        Marshaller marshaller          = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        NamespacePrefixMapperImpl prefixMapper = new NamespacePrefixMapperImpl("");
        marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", prefixMapper);

        File configDir = new File("SOSWorkerTest");
        if (!configDir.exists()) {
            configDir.mkdir();

            //we write the data files
            File offeringDirectory = new File(configDir, "offerings");
            offeringDirectory.mkdir();
            writeDataFile(offeringDirectory, "offering-1.xml", "offering-allSensor");

            File phenomenonDirectory = new File(configDir, "phenomenons");
            phenomenonDirectory.mkdir();
            writeDataFile(phenomenonDirectory, "phenomenon-depth.xml", "urn:ogc:def:phenomenon:BRGM:depth");
            writeDataFile(phenomenonDirectory, "phenomenon-temp.xml",  "urn:ogc:def:phenomenon:BRGM:temperature");

            File sensorDirectory = new File(configDir, "sensors");
            sensorDirectory.mkdir();
            writeDataFile(sensorDirectory, "system.xml",    "urn:ogc:object:sensor:BRGM:1");
            writeDataFile(sensorDirectory, "component.xml", "urn:ogc:object:sensor:BRGM:2");

            File featureDirectory = new File(configDir, "features");
            featureDirectory.mkdir();
            writeDataFile(featureDirectory, "feature1.xml", "10972X0137-PONT");

            //we write the configuration file
            File configFile = new File(configDir, "config.xml");
            Automatic SMLConfiguration = new Automatic();
            SMLConfiguration.setDataDirectory("SOSWorkerTest/sensors");
            Automatic OMConfiguration  = new Automatic();
            OMConfiguration.setDataDirectory("SOSWorkerTest");
            SOSConfiguration configuration = new SOSConfiguration(SMLConfiguration, OMConfiguration);
            configuration.setObservationReaderType(ObservationReaderType.FILESYSTEM);
            configuration.setSMLType(DataSourceType.FILE_SYSTEM);
            configuration.setObservationFilterType(ObservationFilterType.LUCENE);
            marshaller.marshal(configuration, configFile);

        }

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

        JAXBContext context = JAXBContext.newInstance("org.constellation.sos.v100:org.constellation.observation:org.constellation.sml.v100");
        unmarshaller      = context.createUnmarshaller();
        Marshaller marshaller          = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        NamespacePrefixMapperImpl prefixMapper = new NamespacePrefixMapperImpl("");
        marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", prefixMapper);

        File configDir = new File("SOSWorkerTest");
        worker = new SOSworker(SOSworker.DISCOVERY, configDir);
        Capabilities stcapa = (Capabilities) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/SOSCapabilities1.0.0.xml"));
        worker.setSkeletonCapabilities(stcapa);
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
    public void getCapabilitiesTest() throws Exception {

        /*
         *  TEST 1 : minimal getCapabilities
         */
        GetCapabilities request = new GetCapabilities();
        Capabilities result = worker.getCapabilities(request);

        assertTrue(result != null);
        assertTrue(result.getVersion().equals("1.0.0"));
        assertTrue(result.getFilterCapabilities() != null);
        assertTrue(result.getOperationsMetadata() != null);
        assertTrue(result.getServiceIdentification() != null);
        assertTrue(result.getServiceProvider() != null);

        assertTrue(result.getContents() != null);
        assertTrue(result.getContents().getObservationOfferingList() != null);

        /*
         *  TEST 2 : full get capabilities
         */
        AcceptVersionsType acceptVersions = new AcceptVersionsType("1.0.0");
        SectionsType sections             = new SectionsType("All");
        AcceptFormatsType acceptFormats   = new AcceptFormatsType("application/xml");
        request = new GetCapabilities(acceptVersions, sections, acceptFormats, "", "SOS");

        result = worker.getCapabilities(request);

        assertTrue(result.getVersion().equals("1.0.0"));
        assertTrue(result.getFilterCapabilities() != null);
        assertTrue(result.getOperationsMetadata() != null);
        assertTrue(result.getServiceIdentification() != null);
        assertTrue(result.getServiceProvider() != null);
        assertTrue(result.getContents() != null);
        assertTrue(result != null);

        /*
         *  TEST 3 : get capabilities section Operation metadata
         */
        acceptVersions = new AcceptVersionsType("1.0.0");
        sections       = new SectionsType("OperationsMetadata");
        acceptFormats  = new AcceptFormatsType("application/xml");
        request = new GetCapabilities(acceptVersions, sections, acceptFormats, "", "SOS");

        result = worker.getCapabilities(request);

        assertTrue(result.getVersion().equals("1.0.0"));
        assertTrue(result.getFilterCapabilities() == null);
        assertTrue(result.getOperationsMetadata() != null);
        assertTrue(result.getServiceIdentification() == null);
        assertTrue(result.getServiceProvider() == null);
        assertTrue(result.getContents() == null);
        assertTrue(result != null);

        /*
         *  TEST 4 : get capabilities section Service provider
         */
        acceptVersions = new AcceptVersionsType("1.0.0");
        sections       = new SectionsType("ServiceProvider");
        acceptFormats  = new AcceptFormatsType("application/xml");
        request = new GetCapabilities(acceptVersions, sections, acceptFormats, "", "SOS");

        result = worker.getCapabilities(request);

        assertTrue(result.getVersion().equals("1.0.0"));
        assertTrue(result.getFilterCapabilities() == null);
        assertTrue(result.getOperationsMetadata() == null);
        assertTrue(result.getServiceIdentification() == null);
        assertTrue(result.getServiceProvider() != null);
        assertTrue(result.getContents() == null);
        assertTrue(result != null);

        /*
         *  TEST 5 : get capabilities section Service Identification
         */
        acceptVersions = new AcceptVersionsType("1.0.0");
        sections       = new SectionsType("ServiceIdentification");
        acceptFormats  = new AcceptFormatsType("application/xml");
        request = new GetCapabilities(acceptVersions, sections, acceptFormats, "", "SOS");

        result = worker.getCapabilities(request);

        assertTrue(result.getVersion().equals("1.0.0"));
        assertTrue(result.getFilterCapabilities() == null);
        assertTrue(result.getOperationsMetadata() == null);
        assertTrue(result.getServiceIdentification() != null);
        assertTrue(result.getServiceProvider() == null);
        assertTrue(result.getContents() == null);
        assertTrue(result != null);

        /*
         *  TEST 6 : get capabilities section Service Identification
         */
        acceptVersions = new AcceptVersionsType("1.0.0");
        sections       = new SectionsType("Contents");
        acceptFormats  = new AcceptFormatsType("application/xml");
        request = new GetCapabilities(acceptVersions, sections, acceptFormats, "", "SOS");

        result = worker.getCapabilities(request);

        assertTrue(result.getVersion().equals("1.0.0"));
        assertTrue(result.getFilterCapabilities() == null);
        assertTrue(result.getOperationsMetadata() == null);
        assertTrue(result.getServiceIdentification() == null);
        assertTrue(result.getServiceProvider() == null);
         assertTrue(result.getContents() != null);
        assertTrue(result != null);

        /*
         *  TEST 7 : get capabilities with wrong version (waiting for an exception)
         */
        acceptVersions = new AcceptVersionsType("2.0.0");
        sections       = new SectionsType("All");
        acceptFormats  = new AcceptFormatsType("text/xml");
        request = new GetCapabilities(acceptVersions, sections, acceptFormats, "", "SOS");

        boolean exLaunched = false;
        try {
            worker.getCapabilities(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
            assertEquals(ex.getExceptionCode(), VERSION_NEGOTIATION_FAILED);
            assertEquals(ex.getLocator(), "acceptVersion");
        }

        assertTrue(exLaunched);
    }
    
    
    /**
     * Tests the DescribeSensor method
     *
     * @throws java.lang.Exception
     */
    @Test
    public void DescribeSensorTest() throws Exception {
        DescribeSensor request  = new DescribeSensor("urn:ogc:object:sensor:BRGM:1", "text/xml;subtype=\"SensorML/1.0.0\"");
        AbstractSensorML result = (AbstractSensorML) worker.describeSensor(request);

        AbstractSensorML expResult = (AbstractSensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/system.xml"));

        assertEquals(expResult, result);

        request  = new DescribeSensor("urn:ogc:object:sensor:BRGM:2", "text/xml;subtype=\"SensorML/1.0.0\"");
        result = (AbstractSensorML) worker.describeSensor(request);

        expResult = (AbstractSensorML) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/component.xml"));

        assertEquals(expResult, result);
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
}
