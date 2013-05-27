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
package org.constellation.wmts.ws;

import java.io.File;
import java.io.StringWriter;
import java.util.logging.Level;
import javax.xml.bind.Marshaller;

import org.constellation.ws.CstlServiceException;

import org.geotoolkit.ows.xml.v110.AcceptVersionsType;
import org.geotoolkit.util.FileUtilities;
import org.geotoolkit.wmts.xml.WMTSMarshallerPool;
import org.geotoolkit.wmts.xml.v100.Capabilities;
import org.geotoolkit.wmts.xml.v100.GetCapabilities;
import org.geotoolkit.xml.MarshallerPool;

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
        pool = WMTSMarshallerPool.getInstance();
        
        File configDir = new File("WMTSWorkerTest");
        if (configDir.exists()) {
            FileUtilities.deleteDirectory(new File("WMTSWorkerTest"));
        }
        
        worker = new DefaultWMTSWorker("default", configDir);
        worker.setLogLevel(Level.FINER);
        worker.setServiceUrl("http://geomatys.com/constellation/WS/");
        worker.setShiroAccessible(false);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
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
        assertEquals(FileUtilities.getStringFromFile(
                FileUtilities.getFileFromResource("org.constellation.wmts.xml.WMTSCapabilities1-0-0-cont.xml")),
                sw.toString());
        
        
        request = new GetCapabilities("WMTS");
        result = worker.getCapabilities(request);

        sw = new StringWriter();
        marshaller.marshal(result, sw);
        assertEquals(FileUtilities.getStringFromFile(
                FileUtilities.getFileFromResource("org.constellation.wmts.xml.WMTSCapabilities1-0-0.xml")),
                sw.toString());

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
        assertEquals(FileUtilities.getStringFromFile(
                FileUtilities.getFileFromResource("org.constellation.wmts.xml.WMTSCapabilities1-0-0-om.xml")),
                sw.toString());

        acceptVersion = new AcceptVersionsType("1.0.0");
        sections      = new SectionsType("serviceIdentification");
        request       = new GetCapabilities(acceptVersion, sections, null, null, "WMTS");

        result = worker.getCapabilities(request);

        sw = new StringWriter();
        marshaller.marshal(result, sw);
        assertEquals(FileUtilities.getStringFromFile(
                FileUtilities.getFileFromResource("org.constellation.wmts.xml.WMTSCapabilities1-0-0-si.xml")),
                sw.toString());

        acceptVersion = new AcceptVersionsType("1.0.0");
        sections      = new SectionsType("serviceProvider");
        request       = new GetCapabilities(acceptVersion, sections, null, null, "WMTS");

        result = worker.getCapabilities(request);

        sw = new StringWriter();
        marshaller.marshal(result, sw);
        assertEquals(FileUtilities.getStringFromFile(
                FileUtilities.getFileFromResource("org.constellation.wmts.xml.WMTSCapabilities1-0-0-sp.xml")),
                sw.toString());

        pool.release(marshaller);
    }

}
