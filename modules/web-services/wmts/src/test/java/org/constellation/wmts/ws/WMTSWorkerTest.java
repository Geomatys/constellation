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
package org.constellation.wmts.ws;

import org.apache.sis.test.XMLComparator;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.admin.ServiceBusiness;
import org.constellation.admin.SpringHelper;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.LayerContext;
import org.constellation.test.utils.SpringTestRunner;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.ows.xml.v110.AcceptVersionsType;
import org.geotoolkit.ows.xml.v110.SectionsType;
import org.geotoolkit.util.FileUtilities;
import org.geotoolkit.wmts.xml.WMTSMarshallerPool;
import org.geotoolkit.wmts.xml.v100.Capabilities;
import org.geotoolkit.wmts.xml.v100.GetCapabilities;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_PARAMETER_VALUE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.MISSING_PARAMETER_VALUE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.VERSION_NEGOTIATION_FAILED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@RunWith(SpringTestRunner.class)
@ContextConfiguration("classpath:/cstl/spring/test-derby.xml")
public class WMTSWorkerTest implements ApplicationContextAware {

    protected ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Inject
    private ServiceBusiness serviceBusiness;
    
    private static MarshallerPool pool;
    private static WMTSWorker worker ;

    @PostConstruct
    public void setUpClass(){
        SpringHelper.setApplicationContext(applicationContext);
        try {
            ConfigDirectory.setupTestEnvironement("WMTSWorkerTest");
            pool = WMTSMarshallerPool.getInstance();
            


            serviceBusiness.create("wmts", "default", new LayerContext(), null, null);


            worker = new DefaultWMTSWorker("default");
            worker.setLogLevel(Level.FINER);
            worker.setServiceUrl("http://geomatys.com/constellation/WS/");
            worker.setShiroAccessible(false);
        } catch (ConfigurationException ex) {
            Logger.getLogger(WMTSWorkerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        ConfigDirectory.shutdownTestEnvironement("WMTSWorkerTest");
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
    
    /**
     * test the feature marshall
     *
     */
    @Test
    public void getCapabilitiesTest() throws Exception {
        final Marshaller marshaller = pool.acquireMarshaller();

        AcceptVersionsType acceptVersion = new AcceptVersionsType("1.0.0");
        SectionsType sections   = new SectionsType("Contents");
        GetCapabilities request = new GetCapabilities(acceptVersion, sections, null, null, "WMTS");

        Capabilities result = worker.getCapabilities(request);

        StringWriter sw = new StringWriter();
        marshaller.marshal(result, sw);
        XMLComparator comparator = new XMLComparator(FileUtilities.getStringFromFile(
                FileUtilities.getFileFromResource("org.constellation.wmts.xml.WMTSCapabilities1-0-0-cont.xml")), sw.toString());
        comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
        comparator.compare();
        
        
        request = new GetCapabilities("WMTS");
        result = worker.getCapabilities(request);

        sw = new StringWriter();
        marshaller.marshal(result, sw);
        comparator = new XMLComparator(FileUtilities.getStringFromFile(
                FileUtilities.getFileFromResource("org.constellation.wmts.xml.WMTSCapabilities1-0-0.xml")), sw.toString());
        comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
        comparator.compare();

        acceptVersion = new AcceptVersionsType("2.3.0");
        request = new GetCapabilities(acceptVersion, null, null, null, "WMTS");

        try {
            worker.getCapabilities(request);
            fail("Should have raised an error.");
        } catch (CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), VERSION_NEGOTIATION_FAILED);
            assertEquals(ex.getLocator(), "acceptVersion");
        }

         acceptVersion = new AcceptVersionsType("1.0.0");
        request = new GetCapabilities(acceptVersion, null, null, null, "WPS");

        try {
            worker.getCapabilities(request);
            fail("Should have raised an error.");
        } catch (CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "service");
        }

        request = new GetCapabilities(null);

        try {
            worker.getCapabilities(request);
            fail("Should have raised an error.");
        } catch (CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), MISSING_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "service");
        }

        acceptVersion = new AcceptVersionsType("1.0.0");
        sections      = new SectionsType("operationsMetadata");
        request       = new GetCapabilities(acceptVersion, sections, null, null, "WMTS");

        result = worker.getCapabilities(request);

        sw = new StringWriter();
        marshaller.marshal(result, sw);
        comparator = new XMLComparator(FileUtilities.getStringFromFile(
                FileUtilities.getFileFromResource("org.constellation.wmts.xml.WMTSCapabilities1-0-0-om.xml")), sw.toString());
        comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
        comparator.compare();

        acceptVersion = new AcceptVersionsType("1.0.0");
        sections      = new SectionsType("serviceIdentification");
        request       = new GetCapabilities(acceptVersion, sections, null, null, "WMTS");

        result = worker.getCapabilities(request);

        sw = new StringWriter();
        marshaller.marshal(result, sw);
        comparator = new XMLComparator(FileUtilities.getStringFromFile(
                FileUtilities.getFileFromResource("org.constellation.wmts.xml.WMTSCapabilities1-0-0-si.xml")), sw.toString());
        comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
        comparator.compare();

        acceptVersion = new AcceptVersionsType("1.0.0");
        sections      = new SectionsType("serviceProvider");
        request       = new GetCapabilities(acceptVersion, sections, null, null, "WMTS");

        result = worker.getCapabilities(request);

        sw = new StringWriter();
        marshaller.marshal(result, sw);
        comparator = new XMLComparator(FileUtilities.getStringFromFile(
                FileUtilities.getFileFromResource("org.constellation.wmts.xml.WMTSCapabilities1-0-0-sp.xml")), sw.toString());
        comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
        comparator.compare();

        pool.recycle(marshaller);
    }

}
