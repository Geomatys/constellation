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

package org.constellation.metadata;

// JAXB dependencies
import java.io.File;
import java.util.logging.Level;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.ws.CstlServiceException;

import org.geotoolkit.csw.xml.CSWMarshallerPool;
import org.geotoolkit.csw.xml.v202.GetCapabilitiesType;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.configuration.ConfigDirectory;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import org.geotoolkit.util.FileUtilities;

// JUnit dependencies
import org.junit.*;
import static org.junit.Assert.*;

/**
 * Test some erroned initialisation of CSW Worker
 *
 * @author Guilhem Legal (Geomatys)
 */
public class CSWorkerInitialisationTest {

    private static MarshallerPool pool;

    private static File constellationDirectory = new File("CSWorkerInitialisationTest");
    private static File instDirectory;
    

    @BeforeClass
    public static void setUpClass() throws Exception {
        FileUtilities.deleteDirectory(constellationDirectory);
        constellationDirectory.mkdir();
        ConfigDirectory.setConfigDirectory(constellationDirectory);
        
        
        pool = CSWMarshallerPool.getInstance();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        FileUtilities.deleteDirectory(constellationDirectory);
    }
    

    @Before
    public void setUp() throws Exception {
        if (constellationDirectory.exists()) {

            File CSWDirectory  = new File(constellationDirectory, "CSW");
            CSWDirectory.mkdir();
            instDirectory = new File(CSWDirectory, "default");
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
        }
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * Tests the initialisation of the CSW worker with different configuration mistake
     *
     * @throws java.lang.Exception
     */
    @Test
    public void initialisationTest() throws Exception {

        /**
         * Test 1: No configuration file.
         */
        CSWworker worker = new CSWworker("default");
        worker.setLogLevel(Level.FINER);

        boolean exceptionLaunched = false;
        GetCapabilitiesType request = new GetCapabilitiesType("CSW");
        try {

            worker.getCapabilities(request);

        } catch(CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), NO_APPLICABLE_CODE);
            assertEquals(ex.getMessage(), "The service is not running!\nCause:The configuration file has not been found");
            exceptionLaunched = true;
        }

        assertTrue(exceptionLaunched);

        /**
         * Test 2: An empty configuration file.
         */
        File configFile = new File(instDirectory, "config.xml");
        configFile.createNewFile();
        
        worker = new CSWworker("default");
        worker.setLogLevel(Level.FINER);

        exceptionLaunched = false;
        try {

            worker.getCapabilities(request);

        } catch(CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), NO_APPLICABLE_CODE);
            assertEquals(ex.getMessage(), "The service is not running!\nCause:JAXBException:null");
            exceptionLaunched = true;
        }

        assertTrue(exceptionLaunched);

        /**
         * Test 3: A malformed configuration file (bad recognized type).
         */
        configFile = new File(instDirectory, "config.xml");
        configFile.createNewFile();

        final Marshaller m = pool.acquireMarshaller();
        m.marshal(request, configFile);
        pool.recycle(m);


        worker = new CSWworker("default");
        worker.setLogLevel(Level.FINER);

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
         * Test 4: A malformed configuration file (bad not recognized type).
         */
        configFile = new File(instDirectory, "config.xml");
        configFile.createNewFile();

        Marshaller tempMarshaller = JAXBContext.newInstance(UnknowObject.class, Automatic.class).createMarshaller();
        tempMarshaller.marshal(new UnknowObject(), configFile);

        worker = new CSWworker("default");
        worker.setLogLevel(Level.FINER);

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

        String s = null;
        Automatic configuration = new Automatic(null, s);
        tempMarshaller.marshal(configuration, configFile);

        worker = new CSWworker("default");
        worker.setLogLevel(Level.FINER);

        exceptionLaunched = false;
        try {

            worker.getCapabilities(request);

        } catch(CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), NO_APPLICABLE_CODE);
            assertEquals(ex.getMessage(), "The service is not running!\nCause: Unable to find a CSW Factory");
            exceptionLaunched = true;
        }

        assertTrue(exceptionLaunched);

        /**
         * Test 6: A configuration file with missing part and wrong part.
         */
        configFile = new File(instDirectory, "config.xml");
        configFile.createNewFile();

        s = null;
        configuration = new Automatic("whatever", s);
        tempMarshaller.marshal(configuration, configFile);

        worker = new CSWworker("default");
        worker.setLogLevel(Level.FINER);

        exceptionLaunched = false;
        try {

            worker.getCapabilities(request);

        } catch(CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), NO_APPLICABLE_CODE);
            assertEquals(ex.getMessage(), "The service is not running!\nCause: Unable to find a CSW Factory");
            exceptionLaunched = true;
        }

        assertTrue(exceptionLaunched);

        /**
         * Test 7: A configuration file with mdweb database and wrong database config.
         */
        configFile = new File(instDirectory, "config.xml");
        configFile.createNewFile();

        s = null;
        configuration = new Automatic("mdweb", s);
        tempMarshaller.marshal(configuration, configFile);

        worker = new CSWworker("default");
        worker.setLogLevel(Level.FINER);

        exceptionLaunched = false;
        try {

            worker.getCapabilities(request);

        } catch(CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), NO_APPLICABLE_CODE);
            assertEquals(ex.getMessage(), "The service is not running!\nCause: Unable to find a CSW Factory");
            exceptionLaunched = true;
        }

        assertTrue(exceptionLaunched);

        /**
         * Test 8:  A configuration file with mdweb mode and wrong database config.
         */
        configFile = new File(instDirectory, "config.xml");
        configFile.createNewFile();

        configuration = new Automatic("mdweb", new BDD());
        tempMarshaller.marshal(configuration, configFile);

        worker = new CSWworker("default");
        worker.setLogLevel(Level.FINER);

        exceptionLaunched = false;
        try {

            worker.getCapabilities(request);

        } catch(CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), NO_APPLICABLE_CODE);
            assertEquals(ex.getMessage(), "The service is not running!\nCause: Unable to find a CSW Factory");
            exceptionLaunched = true;
        }

        assertTrue(exceptionLaunched);

         /**
         * Test 9:  A configuration file with mdweb mode and wrong database config.
         */
        configFile = new File(instDirectory, "config.xml");
        configFile.createNewFile();

        configuration = new Automatic("mdweb", new BDD(null, null, null, null));
        tempMarshaller.marshal(configuration, configFile);

        worker = new CSWworker("default");
        worker.setLogLevel(Level.FINER);

        exceptionLaunched = false;
        try {

            worker.getCapabilities(request);

        } catch(CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), NO_APPLICABLE_CODE);
            assertEquals(ex.getMessage(), "The service is not running!\nCause: Unable to find a CSW Factory");
            exceptionLaunched = true;
        }

        assertTrue(exceptionLaunched);

        /**
         * Test 10:  A configuration file with mdweb mode and wrong database config.
         */
        configFile = new File(instDirectory, "config.xml");
        configFile.createNewFile();

        configuration = new Automatic("mdweb", new BDD(null, "whatever", null, null));
        tempMarshaller.marshal(configuration, configFile);

        worker = new CSWworker("default");
        worker.setLogLevel(Level.FINER);

        exceptionLaunched = false;
        try {

            worker.getCapabilities(request);

        } catch(CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), NO_APPLICABLE_CODE);
            assertEquals(ex.getMessage(), "The service is not running!\nCause: Unable to find a CSW Factory");
            exceptionLaunched = true;
        }

        assertTrue(exceptionLaunched);

        /**
         * Test 11:  A configuration file with mdweb mode and wrong database config.
         */
        configFile = new File(instDirectory, "config.xml");
        configFile.createNewFile();

        configuration = new Automatic("mdweb", new BDD("org.postgresql.Driver", "whatever", null, null));
        tempMarshaller.marshal(configuration, configFile);

        worker = new CSWworker("default");
        worker.setLogLevel(Level.FINER);

        exceptionLaunched = false;
        try {

            worker.getCapabilities(request);

        } catch(CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), NO_APPLICABLE_CODE);
            assertEquals(ex.getMessage(), "The service is not running!\nCause: Unable to find a CSW Factory");
            exceptionLaunched = true;
        }

        assertTrue(exceptionLaunched);


    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "Unknow")
    private static class UnknowObject {

        private String field1 = "something";

        private String field2 = "other thing";

    }

}
