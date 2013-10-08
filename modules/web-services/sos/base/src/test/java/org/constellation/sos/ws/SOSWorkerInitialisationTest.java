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
import javax.xml.bind.Marshaller;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.configuration.SOSConfiguration;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.geotoolkit.sos.xml.v100.GetCapabilities;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

// JUnit dependencies
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.sos.xml.SOSMarshallerPool;
import org.geotoolkit.util.FileUtilities;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * Test some erroned initialisation of SOS Worker
 *
 * @author Guilhem Legal (Geomatys)
 */
public class SOSWorkerInitialisationTest {

    private static File constellationDirectory = new File("SOSWorkerInitialisationTest");
    private static File instDirectory;

    @BeforeClass
    public static void setUpClass() throws Exception {
        FileUtilities.deleteDirectory(constellationDirectory);
        constellationDirectory.mkdir();
        ConfigDirectory.setConfigDirectory(constellationDirectory);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        FileUtilities.deleteDirectory(constellationDirectory);
    }


    @Before
    public void setUp() throws Exception {
        if (constellationDirectory.exists()) {

            File SOSDirectory  = new File(constellationDirectory, "SOS");
            SOSDirectory.mkdir();
            instDirectory = new File(SOSDirectory, "default");
            instDirectory.mkdir();

            File dataDirectory = new File(instDirectory, "data");
            if (dataDirectory.exists()) {
                for (File f : dataDirectory.listFiles()) {
                    f.delete();
                }
                dataDirectory.delete();
            }
            File indexDirectory = new File(instDirectory, "index");
            if (indexDirectory.exists()) {
                for (File f : indexDirectory.listFiles()) {
                    f.delete();
                }
                indexDirectory.delete();
            }
            File conf = new File(instDirectory, "config.xml");
            conf.delete();

            File map = new File(instDirectory, "mapping.properties");
            map.delete();
        }
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
        SOSworker worker = new SOSworker("default");

        boolean exceptionLaunched = false;
        GetCapabilities request = new GetCapabilities();
        try {

            worker.getCapabilities(request);

        } catch(CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), NO_APPLICABLE_CODE);
            assertEquals(ex.getMessage(), "The service is not running!\nCause:The configuration file can't be found.");
            exceptionLaunched = true;
        }

        assertTrue(exceptionLaunched);

        /**
         * Test 2: An empty configuration file.
         */
        File configFile = new File(instDirectory, "config.xml");
        configFile.createNewFile();

        worker = new SOSworker("default");

        exceptionLaunched = false;
        try {

            worker.getCapabilities(request);

        } catch(CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), NO_APPLICABLE_CODE);
            assertTrue(ex.getMessage().equals("The service is not running!\nCause:JAXBException:Premature end of file.") || ex.getMessage().equals("The service is not running!\nCause:JAXBException:Fin prématurée du fichier.") );

            exceptionLaunched = true;
        }

        assertTrue(exceptionLaunched);

        /**
         * Test 3: A malformed configuration file (bad unrecognized type).
         */
        configFile = new File(instDirectory, "config.xml");
        configFile.createNewFile();

        Marshaller marshaller = SOSMarshallerPool.getInstance().acquireMarshaller();
        marshaller.marshal(request, configFile);
        SOSMarshallerPool.getInstance().recycle(marshaller);
        
        worker = new SOSworker("default");

        exceptionLaunched = false;
        try {

            worker.getCapabilities(request);

        } catch(CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), NO_APPLICABLE_CODE);
            assertTrue(ex.getMessage().startsWith("The service is not running!"));
            exceptionLaunched = true;
        }
        assertTrue(exceptionLaunched);

        marshaller = GenericDatabaseMarshallerPool.getInstance().acquireMarshaller();

        /**
         * Test 4: A malformed configuration file (bad unrecognized type).
         */
        configFile = new File(instDirectory, "config.xml");
        configFile.createNewFile();


        marshaller.marshal(new BDD(), configFile);

        worker = new SOSworker("default");

        exceptionLaunched = false;
        try {

            worker.getCapabilities(request);

        } catch(CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), NO_APPLICABLE_CODE);
            assertTrue(ex.getMessage().startsWith("The service is not running!"));
            exceptionLaunched = true;
        }
        assertTrue(exceptionLaunched);


        /**
         * Test 5: A configuration file with missing part.
         */
        configFile = new File(instDirectory, "config.xml");
        configFile.createNewFile();

        SOSConfiguration configuration = new SOSConfiguration();
        marshaller.marshal(configuration, configFile);

        worker = new SOSworker("default");

        exceptionLaunched = false;
        try {

            worker.getCapabilities(request);

        } catch(CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), NO_APPLICABLE_CODE);
            assertEquals(ex.getMessage(), "The service is not running!\nCause:The configuration file does not contains a SML configuration.");
            exceptionLaunched = true;
        }

        assertTrue(exceptionLaunched);

        /**
         * Test 6: A configuration file with missing part.
         */
        configFile = new File(instDirectory, "config.xml");
        configFile.createNewFile();

        configuration = new SOSConfiguration(new Automatic(), null);
        marshaller.marshal(configuration, configFile);

        worker = new SOSworker("default");

        exceptionLaunched = false;
        try {

            worker.getCapabilities(request);

        } catch(CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), NO_APPLICABLE_CODE);
            assertEquals(ex.getMessage(), "The service is not running!\nCause:The configuration file does not contains a O&M configuration.");
            exceptionLaunched = true;
        }

        assertTrue(exceptionLaunched);

        /**
         * Test 7: A configuration file with two empty configuration object.
         */
        configFile = new File(instDirectory, "config.xml");
        configFile.createNewFile();

        configuration = new SOSConfiguration(new Automatic(), new Automatic());
        marshaller.marshal(configuration, configFile);

        worker = new SOSworker("default");

        exceptionLaunched = false;
        try {

            worker.getCapabilities(request);

        } catch(CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), NO_APPLICABLE_CODE);
            assertEquals(ex.getMessage(), "The service is not running!\nCause:Unable to find a SOS Factory.No SML factory has been found for type:mdweb");
            exceptionLaunched = true;
        }

        assertTrue(exceptionLaunched);

        /**
         * Test 8: A configuration file with two empty configuration object and a malformed template valid time.
         */
        configFile = new File(instDirectory, "config.xml");
        configFile.createNewFile();

        configuration = new SOSConfiguration(new Automatic(), new Automatic());
        configuration.setProfile("transactional");
        configuration.setTemplateValidTime("ff:oo");
        marshaller.marshal(configuration, configFile);

        worker = new SOSworker("default");


        exceptionLaunched = false;
        try {

            worker.getCapabilities(request);

        } catch(CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), NO_APPLICABLE_CODE);
            assertEquals(ex.getMessage(), "The service is not running!\nCause:Unable to find a SOS Factory.No SML factory has been found for type:mdweb");
            exceptionLaunched = true;
        }

        assertTrue(exceptionLaunched);
        
        GenericDatabaseMarshallerPool.getInstance().recycle(marshaller);
    }

}
