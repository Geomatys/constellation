/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010, Geomatys
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

package org.constellation.ws.embedded;

// JUnit dependencies
import org.geotoolkit.ows.xml.v110.Operation;
import org.geotoolkit.sos.xml.v100.GetFeatureOfInterest;
import org.geotoolkit.sos.xml.v100.ResponseModeType;
import java.util.Arrays;
import org.geotoolkit.sos.xml.v100.GetObservation;
import org.geotoolkit.sos.xml.v100.Capabilities;
import org.geotoolkit.sos.xml.v100.GetCapabilities;
import java.net.URLConnection;
import java.net.URL;
import java.io.File;
import org.geotoolkit.sos.xml.SOSMarshallerPool;
import javax.xml.bind.JAXBException;
import org.geotoolkit.observation.xml.v100.ObservationCollectionEntry;
import org.geotoolkit.ows.xml.v110.ExceptionReport;
import org.geotoolkit.sampling.xml.v100.SamplingPointEntry;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.geotoolkit.sos.xml.v100.DescribeSensor;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class SOSRequestTest extends AbstractTestRequest {

    private static final String SOS_POST_URL = "http://localhost:9090/sos/default?";

    private static final String SOS_GETCAPABILITIES_URL = "http://localhost:9090/sos/default?request=GetCapabilities&service=SOS&version=1.0.0";

    /**
     * Initialize the list of layers from the defined providers in Constellation's configuration.
     */
    @BeforeClass
    public static void initPool() throws JAXBException {
        // Get the list of layers
        pool = SOSMarshallerPool.getInstance();
    }

    @AfterClass
    public static void finish() {
        File f = new File("derby.log");
        if (f.exists()) {
            f.delete();
        }
    }

    @Test
    public void testSOSInvalidRequest() throws Exception {
        // Creates a valid GetCapabilities url.
        final URL getCapsUrl = new URL(SOS_POST_URL);


        // for a POST request
        URLConnection conec = getCapsUrl.openConnection();


        postRequestPlain(conec, "test");
        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof ExceptionReport);

        ExceptionReport report = (ExceptionReport) obj;

        assertEquals("InvalidRequest", report.getException().get(0).getExceptionCode());
    }

    @Test
    public void testSOSGetCapabilities() throws Exception {
        // Creates a valid GetCapabilities url.
        URL getCapsUrl = new URL(SOS_POST_URL);


        // for a POST request
        URLConnection conec = getCapsUrl.openConnection();

        final GetCapabilities request = new GetCapabilities("1.0.0", "text/xml");

        postRequestObject(conec, request);
        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof Capabilities);

        Capabilities c = (Capabilities) obj;

        assertTrue(c.getOperationsMetadata() != null);

        Operation op = c.getOperationsMetadata().getOperation("GetObservation");

        assertTrue(op != null);
        assertTrue(op.getDCP().size() > 0);

        assertEquals(op.getDCP().get(0).getHTTP().getGetOrPost().get(0).getHref(), SOS_POST_URL);

        // Creates a valid GetCapabilties url.
        getCapsUrl = new URL(SOS_GETCAPABILITIES_URL);


        // Try to marshall something from the response returned by the server.
        // The response should be a WMT_MS_Capabilities.
        obj = unmarshallResponse(getCapsUrl);
        assertTrue(obj instanceof Capabilities);
    }

    @Test
    public void testSOSDescribeSensor() throws Exception {
        // Creates a valid DescribeSensor url.
        final URL getCapsUrl = new URL(SOS_POST_URL);


        // for a POST request
        URLConnection conec = getCapsUrl.openConnection();

        final DescribeSensor request = new DescribeSensor("1.0.0","SOS","urn:ogc:object:sensor:SunSpot:0014.4F01.0000.261A", "text/xml;subtype=\"SensorML/1.0.1\"");

        postRequestObject(conec, request);
        Object obj = unmarshallResponse(conec);

        String type = "null";
        if (obj != null) {
            type = obj.getClass().getName();
        }
        assertTrue("expecting AbstractSensorML but was: " + type, obj instanceof AbstractSensorML);
    }

    @Test
    public void testSOSGetObservation() throws Exception {
        // Creates a valid GetObservation url.
        final URL getCapsUrl = new URL(SOS_POST_URL);

        // for a POST request
        URLConnection conec = getCapsUrl.openConnection();

        GetObservation request  = new GetObservation("1.0.0",
                                      "offering-allSensor",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:SunSpot:0014.4F01.0000.261A"),
                                      Arrays.asList("urn:phenomenon:acceleration"),
                                      null,
                                      null,
                                      "text/xml; subtype=\"om/1.0.0\"",
                                      null,
                                      ResponseModeType.INLINE,
                                      null);

        postRequestObject(conec, request);
        Object obj = unmarshallResponse(conec);

        String type = "null";
        if (obj != null) {
            type = obj.getClass().getName();
        }
        assertTrue("expecting ObservationCollectionEntry but was: " + type, obj instanceof ObservationCollectionEntry);
    }

    @Test
    public void testSOSGetFeatureOfInterest() throws Exception {
        // Creates a valid GetObservation url.
        final URL getCapsUrl = new URL(SOS_POST_URL);

        // for a POST request
        URLConnection conec = getCapsUrl.openConnection();

        GetFeatureOfInterest request = new GetFeatureOfInterest("1.0.0", "SOS", "sampling-point-001");

        postRequestObject(conec, request);
        Object obj = unmarshallResponse(conec);

        String type = "null";
        if (obj != null) {
            type = obj.getClass().getName();
        }
        assertTrue("expecting SamplingPointEntry but was: " + type, obj instanceof SamplingPointEntry);
    }
}
