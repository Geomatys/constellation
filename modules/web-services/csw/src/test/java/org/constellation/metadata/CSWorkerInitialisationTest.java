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
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.util.Util;
import org.constellation.ws.CstlServiceException;

import org.geotoolkit.csw.xml.CSWMarshallerPool;
import org.geotoolkit.csw.xml.v202.Capabilities;
import org.geotoolkit.csw.xml.v202.GetCapabilitiesType;
import org.geotoolkit.xml.MarshallerPool;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

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

    private static File configurationDirectory = new File("CSWorkerInitialisationTest");

    private static Capabilities skeletonCapabilities;

    @BeforeClass
    public static void setUpClass() throws Exception {
        deleteTemporaryFile();
        if (!configurationDirectory.exists()) {
            configurationDirectory.mkdir();
        }
        pool = CSWMarshallerPool.getInstance();
        Unmarshaller u = pool.acquireUnmarshaller();

        skeletonCapabilities = (Capabilities) u.unmarshal(Util.getResourceAsStream("org/constellation/metadata/CSWCapabilities2.0.2.xml"));
        pool.release(u);
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
     * Tests the initialisation of the CSW worker with different configuration mistake
     *
     * @throws java.lang.Exception
     */
    @Test
    public void initialisationTest() throws Exception {

        /**
         * Test 1: No configuration file.
         */
        CSWworker worker = new CSWworker("", configurationDirectory);
        worker.setSkeletonCapabilities(skeletonCapabilities);
        worker.setLogLevel(Level.FINER);

        boolean exceptionLaunched = false;
        GetCapabilitiesType request = new GetCapabilitiesType("CSW");
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
        
        worker = new CSWworker("",  configurationDirectory);
        worker.setSkeletonCapabilities(skeletonCapabilities);
        worker.setLogLevel(Level.FINER);

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
         * Test 3: A malformed configuration file (bad recognized type).
         */
        configFile = new File(configurationDirectory, "config.xml");
        configFile.createNewFile();

        Marshaller m = null;
        try {
             m = pool.acquireMarshaller();
             m.marshal(request, configFile);
        } finally {
            pool.release(m);
        }


        worker = new CSWworker("",  configurationDirectory);
        worker.setSkeletonCapabilities(skeletonCapabilities);
        worker.setLogLevel(Level.FINER);

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
         * Test 4: A malformed configuration file (bad not recognized type).
         */
        configFile = new File(configurationDirectory, "config.xml");
        configFile.createNewFile();

        Marshaller tempMarshaller = JAXBContext.newInstance(UnknowObject.class, Automatic.class).createMarshaller();
        tempMarshaller.marshal(new UnknowObject(), configFile);

        worker = new CSWworker("",  configurationDirectory);
        worker.setSkeletonCapabilities(skeletonCapabilities);
        worker.setLogLevel(Level.FINER);

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
         * Test 5: A configuration file with missing part.
         */
        configFile = new File(configurationDirectory, "config.xml");
        configFile.createNewFile();

        String s = null;
        Automatic configuration = new Automatic(null, s);
        tempMarshaller.marshal(configuration, configFile);

        worker = new CSWworker("",  configurationDirectory);
        worker.setSkeletonCapabilities(skeletonCapabilities);
        worker.setLogLevel(Level.FINER);

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
         * Test 6: A configuration file with missing part and wrong part.
         */
        configFile = new File(configurationDirectory, "config.xml");
        configFile.createNewFile();

        s = null;
        configuration = new Automatic("whatever", s);
        tempMarshaller.marshal(configuration, configFile);

        worker = new CSWworker("",  configurationDirectory);
        worker.setSkeletonCapabilities(skeletonCapabilities);
        worker.setLogLevel(Level.FINER);

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
         * Test 7: A configuration file with mdweb mdweb database and wrong database config.
         */
        configFile = new File(configurationDirectory, "config.xml");
        configFile.createNewFile();

        s = null;
        configuration = new Automatic("mdweb", s);
        tempMarshaller.marshal(configuration, configFile);

        worker = new CSWworker("",  configurationDirectory);
        worker.setSkeletonCapabilities(skeletonCapabilities);
        worker.setLogLevel(Level.FINER);

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
         * Test 8:  A configuration file with mdweb mode and wrong database config.
         */
        configFile = new File(configurationDirectory, "config.xml");
        configFile.createNewFile();

        configuration = new Automatic("mdweb", new BDD());
        tempMarshaller.marshal(configuration, configFile);

        worker = new CSWworker("",  configurationDirectory);
        worker.setSkeletonCapabilities(skeletonCapabilities);
        worker.setLogLevel(Level.FINER);

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
         * Test 9:  A configuration file with mdweb mode and wrong database config.
         */
        configFile = new File(configurationDirectory, "config.xml");
        configFile.createNewFile();

        configuration = new Automatic("mdweb", new BDD(null, null, null, null));
        tempMarshaller.marshal(configuration, configFile);

        worker = new CSWworker("",  configurationDirectory);
        worker.setSkeletonCapabilities(skeletonCapabilities);
        worker.setLogLevel(Level.FINER);

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
         * Test 10:  A configuration file with mdweb mode and wrong database config.
         */
        configFile = new File(configurationDirectory, "config.xml");
        configFile.createNewFile();

        configuration = new Automatic("mdweb", new BDD(null, "whatever", null, null));
        tempMarshaller.marshal(configuration, configFile);

        worker = new CSWworker("",  configurationDirectory);
        worker.setSkeletonCapabilities(skeletonCapabilities);
        worker.setLogLevel(Level.FINER);

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
         * Test 11:  A configuration file with mdweb mode and wrong database config.
         */
        configFile = new File(configurationDirectory, "config.xml");
        configFile.createNewFile();

        configuration = new Automatic("mdweb", new BDD("org.postgresql.Driver", "whatever", null, null));
        tempMarshaller.marshal(configuration, configFile);

        worker = new CSWworker("",  configurationDirectory);
        worker.setSkeletonCapabilities(skeletonCapabilities);
        worker.setLogLevel(Level.FINER);

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

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "Unknow")
    private static class UnknowObject {

        private String field1 = "something";

        private String field2 = "other thing";

    }

}
