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
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.constellation.configuration.SOSConfiguration;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.geotoolkit.sos.xml.v100.Capabilities;
import org.geotoolkit.sos.xml.v100.GetCapabilities;
import org.constellation.util.Util;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

// JUnit dependencies
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.xml.MarshallerPool;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * Test some erroned initialisation of SOS Worker
 *
 * @author Guilhem Legal (Geomatys)
 */
public class SOSWorkerInitialisationTest {

    private static File configurationDirectory = new File("SOSWorkerInitialisationTest");

    private static Capabilities skeletonCapabilities;

    @BeforeClass
    public static void setUpClass() throws Exception {
        deleteTemporaryFile();
        if (!configurationDirectory.exists()) {
            configurationDirectory.mkdir();
        }

        MarshallerPool pool       = new MarshallerPool("org.geotoolkit.sos.xml.v100:org.geotoolkit.internal.jaxb.geometry");
        Unmarshaller unmarshaller = pool.acquireUnmarshaller();
        skeletonCapabilities      = (Capabilities) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/sos/SOSCapabilities1.0.0.xml"));
        pool.release(unmarshaller);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        deleteTemporaryFile();
    }

    private static void deleteTemporaryFile() {
        if (configurationDirectory.exists()) {
            emptyConfigurationDirectory();
        }
        configurationDirectory.delete();
    }

    private static void emptyConfigurationDirectory() {

        if (configurationDirectory.exists()) {
            File dataDirectory = new File(configurationDirectory, "data");
            if (dataDirectory.exists()) {
                for (File f : dataDirectory.listFiles()) {
                    f.delete();
                }
                dataDirectory.delete();
            }
            File indexDirectory = new File(configurationDirectory, "index");
            if (indexDirectory.exists()) {
                for (File f : indexDirectory.listFiles()) {
                    f.delete();
                }
                indexDirectory.delete();
            }
            File conf = new File(configurationDirectory, "config.xml");
            conf.delete();

            File map = new File(configurationDirectory, "mapping.properties");
            map.delete();
        }

    }

    @Before
    public void setUp() throws Exception {
        emptyConfigurationDirectory();
    }

    @After
    public void tearDown() throws Exception {
    }


    /**
     * Tests the initialisation of the SOS worker with different configuration mistake
     *
     * @throws java.lang.Exception
     */
    @Test
    public void initialisationTest() throws Exception {

        /**
         * Test 1: No configuration file.
         */
        SOSworker worker = new SOSworker(configurationDirectory);
        worker.setSkeletonCapabilities(skeletonCapabilities);

        boolean exceptionLaunched = false;
        GetCapabilities request = new GetCapabilities();
        try {

            worker.getCapabilities(request);

        } catch(CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), NO_APPLICABLE_CODE);
            assertEquals(ex.getMessage(), "The service is not running!");
            exceptionLaunched = true;
        }

        assertTrue(exceptionLaunched);

        /**
         * Test 2: An empty configuration file.
         */
        File configFile = new File(configurationDirectory, "config.xml");
        configFile.createNewFile();

        worker = new SOSworker(configurationDirectory);
        worker.setSkeletonCapabilities(skeletonCapabilities);

        exceptionLaunched = false;
        try {

            worker.getCapabilities(request);

        } catch(CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), NO_APPLICABLE_CODE);
            assertEquals(ex.getMessage(), "The service is not running!");
            exceptionLaunched = true;
        }

        assertTrue(exceptionLaunched);

        /**
         * Test 3: A malformed configuration file (bad unrecognized type).
         */
        configFile = new File(configurationDirectory, "config.xml");
        configFile.createNewFile();

        Marshaller marshaller = JAXBContext.newInstance("org.geotoolkit.sos.xml.v100:org.geotoolkit.internal.jaxb.geometry").createMarshaller();
        marshaller.marshal(request, configFile);

        worker = new SOSworker(configurationDirectory);
        worker.setSkeletonCapabilities(skeletonCapabilities);

        exceptionLaunched = false;
        try {

            worker.getCapabilities(request);

        } catch(CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), NO_APPLICABLE_CODE);
            assertEquals(ex.getMessage(), "The service is not running!");
            exceptionLaunched = true;
        }

        marshaller = JAXBContext.newInstance(SOSConfiguration.class).createMarshaller();

        /**
         * Test 4: A malformed configuration file (bad unrecognized type).
         */
        configFile = new File(configurationDirectory, "config.xml");
        configFile.createNewFile();


        marshaller.marshal(new BDD(), configFile);

        worker = new SOSworker(configurationDirectory);
        worker.setSkeletonCapabilities(skeletonCapabilities);

        exceptionLaunched = false;
        try {

            worker.getCapabilities(request);

        } catch(CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), NO_APPLICABLE_CODE);
            assertEquals(ex.getMessage(), "The service is not running!");
            exceptionLaunched = true;
        }

        /**
         * Test 5: A configuration file with missing part.
         */
        configFile = new File(configurationDirectory, "config.xml");
        configFile.createNewFile();

        SOSConfiguration configuration = new SOSConfiguration();
        marshaller.marshal(configuration, configFile);

        worker = new SOSworker(configurationDirectory);
        worker.setSkeletonCapabilities(skeletonCapabilities);

        exceptionLaunched = false;
        try {

            worker.getCapabilities(request);

        } catch(CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), NO_APPLICABLE_CODE);
            assertEquals(ex.getMessage(), "The service is not running!");
            exceptionLaunched = true;
        }

        assertTrue(exceptionLaunched);

        /**
         * Test 6: A configuration file with missing part.
         */
        configFile = new File(configurationDirectory, "config.xml");
        configFile.createNewFile();

        configuration = new SOSConfiguration(new Automatic(), null);
        marshaller.marshal(configuration, configFile);

        worker = new SOSworker(configurationDirectory);
        worker.setSkeletonCapabilities(skeletonCapabilities);

        exceptionLaunched = false;
        try {

            worker.getCapabilities(request);

        } catch(CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), NO_APPLICABLE_CODE);
            assertEquals(ex.getMessage(), "The service is not running!");
            exceptionLaunched = true;
        }

        assertTrue(exceptionLaunched);

        /**
         * Test 7: A configuration file with two empty configuration object.
         */
        configFile = new File(configurationDirectory, "config.xml");
        configFile.createNewFile();

        configuration = new SOSConfiguration(new Automatic(), new Automatic());
        marshaller.marshal(configuration, configFile);

        worker = new SOSworker(configurationDirectory);

        worker.setSkeletonCapabilities(skeletonCapabilities);

        exceptionLaunched = false;
        try {

            worker.getCapabilities(request);

        } catch(CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), NO_APPLICABLE_CODE);
            assertEquals(ex.getMessage(), "The service is not running!");
            exceptionLaunched = true;
        }

        assertTrue(exceptionLaunched);

        /**
         * Test 8: A configuration file with two empty configuration object and a malformed template valid time.
         */
        configFile = new File(configurationDirectory, "config.xml");
        configFile.createNewFile();

        configuration = new SOSConfiguration(new Automatic(), new Automatic());
        configuration.setProfile("transactional");
        configuration.setTemplateValidTime("ff:oo");
        marshaller.marshal(configuration, configFile);

        worker = new SOSworker(configurationDirectory);

        worker.setSkeletonCapabilities(skeletonCapabilities);

        exceptionLaunched = false;
        try {

            worker.getCapabilities(request);

        } catch(CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), NO_APPLICABLE_CODE);
            assertEquals(ex.getMessage(), "The service is not running!");
            exceptionLaunched = true;
        }

        assertTrue(exceptionLaunched);
    }

}
