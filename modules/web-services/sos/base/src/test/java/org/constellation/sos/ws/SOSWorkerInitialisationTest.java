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

import java.io.StringWriter;
import java.sql.Statement;
import javax.xml.bind.Marshaller;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.admin.EmbeddedDatabase;
import org.constellation.admin.util.SQLExecuter;
import org.constellation.configuration.SOSConfiguration;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.geotoolkit.sos.xml.v100.GetCapabilities;
import static org.geotoolkit.ows.xml.OWSExceptionCode.*;

// JUnit dependencies
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.sos.xml.SOSMarshallerPool;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * Test some erroned initialisation of SOS Worker
 *
 * @author Guilhem Legal (Geomatys)
 */
public class SOSWorkerInitialisationTest {

    private static MarshallerPool pool;

    @BeforeClass
    public static void setUpClass() throws Exception {
        ConfigurationEngine.setupTestEnvironement("SOSWorkerInitialisationTest");
        pool = SOSMarshallerPool.getInstance();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        ConfigurationEngine.shutdownTestEnvironement("SOSWorkerInitialisationTest");
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
        ConfigurationEngine.storeConfiguration("SOS", "default", null);
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
        final SQLExecuter executer = EmbeddedDatabase.createSQLExecuter();
        final Statement stmt = executer.createStatement();
        stmt.executeUpdate("UPDATE \"admin\".\"service\" SET \"config\"=''");
        
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
        StringWriter sw = new StringWriter();
        final Marshaller m = pool.acquireMarshaller();
        m.marshal(request, sw);
        pool.recycle(m);
        stmt.executeUpdate("UPDATE \"admin\".\"service\" SET \"config\"='" + sw.toString() + "'");

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

        Marshaller marshaller = GenericDatabaseMarshallerPool.getInstance().acquireMarshaller();
        /**
         * Test 4: A malformed configuration file (bad unrecognized type).
         */
        sw = new StringWriter();
        marshaller.marshal(new BDD(), sw);
        stmt.executeUpdate("UPDATE \"admin\".\"service\" SET \"config\"='" + sw.toString() + "'");

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
        SOSConfiguration configuration = new SOSConfiguration();
        sw = new StringWriter();
        marshaller.marshal(configuration, sw);
        stmt.executeUpdate("UPDATE \"admin\".\"service\" SET \"config\"='" + sw.toString() + "'");
        
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
        configuration = new SOSConfiguration(new Automatic(), null);
        sw = new StringWriter();
        marshaller.marshal(configuration, sw);
        stmt.executeUpdate("UPDATE \"admin\".\"service\" SET \"config\"='" + sw.toString() + "'");

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
        configuration = new SOSConfiguration(new Automatic(), new Automatic());
        sw = new StringWriter();
        marshaller.marshal(configuration, sw);
        stmt.executeUpdate("UPDATE \"admin\".\"service\" SET \"config\"='" + sw.toString() + "'");

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
        configuration = new SOSConfiguration(new Automatic(), new Automatic());
        configuration.setProfile("transactional");
        configuration.setTemplateValidTime("ff:oo");

        sw = new StringWriter();
        marshaller.marshal(configuration, sw);
        stmt.executeUpdate("UPDATE \"admin\".\"service\" SET \"config\"='" + sw.toString() + "'");

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
