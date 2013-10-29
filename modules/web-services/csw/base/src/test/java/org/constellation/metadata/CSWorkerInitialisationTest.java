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
import java.io.StringWriter;
import java.sql.Statement;
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
import org.constellation.admin.ConfigurationEngine;
import org.constellation.admin.EmbeddedDatabase;
import org.constellation.admin.util.SQLExecuter;
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

    private static final File constellationDirectory = new File("CSWorkerInitialisationTest");
    

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
        ConfigurationEngine.storeConfiguration("CSW", "default", null);
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
        final SQLExecuter executer = EmbeddedDatabase.createSQLExecuter();
        final Statement stmt = executer.createStatement();
        stmt.executeUpdate("UPDATE \"admin\".\"service\" SET \"config\"=''");
        
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
        StringWriter sw = new StringWriter();
        final Marshaller m = pool.acquireMarshaller();
        m.marshal(request, sw);
        pool.recycle(m);
        stmt.executeUpdate("UPDATE \"admin\".\"service\" SET \"config\"='" + sw.toString() + "'");

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
        sw = new StringWriter();
        Marshaller tempMarshaller = JAXBContext.newInstance(UnknowObject.class, Automatic.class).createMarshaller();
        tempMarshaller.marshal(new UnknowObject(), sw);
        stmt.executeUpdate("UPDATE \"admin\".\"service\" SET \"config\"='" + sw.toString() + "'");

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
        sw = new StringWriter();
        String s = null;
        Automatic configuration = new Automatic(null, s);
        tempMarshaller.marshal(configuration, sw);
        stmt.executeUpdate("UPDATE \"admin\".\"service\" SET \"config\"='" + sw.toString() + "'");

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
        sw = new StringWriter();
        s = null;
        configuration = new Automatic("whatever", s);
        tempMarshaller.marshal(configuration, sw);
        stmt.executeUpdate("UPDATE \"admin\".\"service\" SET \"config\"='" + sw.toString() + "'");
        
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
        sw = new StringWriter();
        s = null;
        configuration = new Automatic("mdweb", s);
        tempMarshaller.marshal(configuration, sw);
        stmt.executeUpdate("UPDATE \"admin\".\"service\" SET \"config\"='" + sw.toString() + "'");

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
        sw = new StringWriter();
        configuration = new Automatic("mdweb", new BDD());
        tempMarshaller.marshal(configuration, sw);
        stmt.executeUpdate("UPDATE \"admin\".\"service\" SET \"config\"='" + sw.toString() + "'");
        
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
        sw = new StringWriter();
        configuration = new Automatic("mdweb", new BDD(null, null, null, null));
        tempMarshaller.marshal(configuration, sw);
        stmt.executeUpdate("UPDATE \"admin\".\"service\" SET \"config\"='" + sw.toString() + "'");

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
        sw = new StringWriter();
        configuration = new Automatic("mdweb", new BDD(null, "whatever", null, null));
        tempMarshaller.marshal(configuration, sw);
        stmt.executeUpdate("UPDATE \"admin\".\"service\" SET \"config\"='" + sw.toString() + "'");

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
        sw = new StringWriter();
        configuration = new Automatic("mdweb", new BDD("org.postgresql.Driver", "whatever", null, null));
        tempMarshaller.marshal(configuration, sw);
        stmt.executeUpdate("UPDATE \"admin\".\"service\" SET \"config\"='" + sw.toString() + "'");

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

        private final String field1 = "something";

        private final String field2 = "other thing";

    }

}
