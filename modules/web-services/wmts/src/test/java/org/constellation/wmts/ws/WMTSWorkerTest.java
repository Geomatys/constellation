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

import java.io.StringWriter;
import java.util.logging.Level;
import javax.xml.bind.Marshaller;

import org.apache.sis.test.XMLComparator;
import org.constellation.ws.CstlServiceException;

import org.geotoolkit.ows.xml.v110.AcceptVersionsType;
import org.geotoolkit.util.FileUtilities;
import org.geotoolkit.wmts.xml.WMTSMarshallerPool;
import org.geotoolkit.wmts.xml.v100.Capabilities;
import org.geotoolkit.wmts.xml.v100.GetCapabilities;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.configuration.LayerContext;

import static org.geotoolkit.ows.xml.OWSExceptionCode.*;
import org.geotoolkit.ows.xml.v110.SectionsType;

import org.junit.*;
import static org.junit.Assert.*;


/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class WMTSWorkerTest {

    private static MarshallerPool pool;
    private static WMTSWorker worker ;

    @BeforeClass
    public static void setUpClass() throws Exception {
        ConfigurationEngine.setupTestEnvironement("WMTSWorkerTest");
        pool = WMTSMarshallerPool.getInstance();
        
        ConfigurationEngine.storeConfiguration("WMTS", "default", new LayerContext());

        worker = new DefaultWMTSWorker("default");
        worker.setLogLevel(Level.FINER);
        worker.setServiceUrl("http://geomatys.com/constellation/WS/");
        worker.setShiroAccessible(false);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        ConfigurationEngine.shutdownTestEnvironement("WMTSWorkerTest");
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
