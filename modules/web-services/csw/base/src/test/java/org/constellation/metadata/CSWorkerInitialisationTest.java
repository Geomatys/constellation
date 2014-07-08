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

package org.constellation.metadata;

// JAXB dependencies

import org.apache.sis.xml.MarshallerPool;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.admin.SpringHelper;
import org.constellation.engine.register.Service;
import org.constellation.engine.register.repository.ServiceRepository;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.test.utils.SpringTestRunner;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.csw.xml.CSWMarshallerPool;
import org.geotoolkit.csw.xml.v202.GetCapabilitiesType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.StringWriter;
import java.util.logging.Level;

import static org.geotoolkit.ows.xml.OWSExceptionCode.NO_APPLICABLE_CODE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

// JUnit dependencies

/**
 * Test some erroned initialisation of CSW Worker
 *
 * @author Guilhem Legal (Geomatys)
 */
@RunWith(SpringTestRunner.class)
@ContextConfiguration("classpath:/cstl/spring/test-derby.xml")
public class CSWorkerInitialisationTest implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private static MarshallerPool pool;

    @Inject
    private ServiceRepository serviceRepository;

    @PostConstruct
    public void setUp() {
        SpringHelper.setApplicationContext(applicationContext);
    }

    
    @BeforeClass
    public static void setUpClass() throws Exception {
        ConfigDirectory.setupTestEnvironement("CSWorkerInitialisationTest");
        pool = CSWMarshallerPool.getInstance();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        ConfigDirectory.shutdownTestEnvironement("CSWorkerInitialisationTest");
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
        Service service = new Service("default", "csw", System.currentTimeMillis(), null, null, null, null, "NOT_STARTED", "1.0.0");
        int id =  serviceRepository.create(service);
        assertTrue(id > 0);
        
        CSWworker worker = new CSWworker("default");
        worker.setLogLevel(Level.FINER);

        boolean exceptionLaunched = false;
        GetCapabilitiesType request = new GetCapabilitiesType("CSW");
        try {

            worker.getCapabilities(request);

        } catch(CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), NO_APPLICABLE_CODE);
            assertEquals(ex.getMessage(), "The service is not running!\nCause: Configuration Object is not an Automatic Object");
            exceptionLaunched = true;
        }

        assertTrue(exceptionLaunched);

        /**
         * Test 2: An empty configuration file.
         */
        service = serviceRepository.findByIdentifierAndType("default", "csw");
        service.setConfig("");
        serviceRepository.update(service);
        
        worker = new CSWworker("default");
        worker.setLogLevel(Level.FINER);

        exceptionLaunched = false;
        try {

            worker.getCapabilities(request);

        } catch(CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), NO_APPLICABLE_CODE);
            assertEquals(ex.getMessage(), "The service is not running!\nCause:The configuration file has not been found");
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
        
        service = serviceRepository.findByIdentifierAndType("default", "csw");
        service.setConfig(sw.toString());
        serviceRepository.update(service);
        

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
        
        service = serviceRepository.findByIdentifierAndType("default", "csw");
        service.setConfig(sw.toString());
        serviceRepository.update(service);

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
        
        service = serviceRepository.findByIdentifierAndType("default", "csw");
        service.setConfig(sw.toString());
        serviceRepository.update(service);

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
        
        service = serviceRepository.findByIdentifierAndType("default", "csw");
        service.setConfig(sw.toString());
        serviceRepository.update(service);
        
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
        
        service = serviceRepository.findByIdentifierAndType("default", "csw");
        service.setConfig(sw.toString());
        serviceRepository.update(service);

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
        
        service = serviceRepository.findByIdentifierAndType("default", "csw");
        service.setConfig(sw.toString());
        serviceRepository.update(service);
        
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
        
        service = serviceRepository.findByIdentifierAndType("default", "csw");
        service.setConfig(sw.toString());
        serviceRepository.update(service);

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
        
        service = serviceRepository.findByIdentifierAndType("default", "csw");
        service.setConfig(sw.toString());
        serviceRepository.update(service);

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
        
        service = serviceRepository.findByIdentifierAndType("default", "csw");
        service.setConfig(sw.toString());
        serviceRepository.update(service);

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
