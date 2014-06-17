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

package org.constellation.ws.embedded;

// JUnit dependencies
import org.geotoolkit.ows.xml.v110.Operation;
import org.geotoolkit.sos.xml.v100.GetFeatureOfInterest;
import org.geotoolkit.sos.xml.ResponseModeType;
import java.util.Arrays;
import org.geotoolkit.sos.xml.v100.GetObservation;
import org.geotoolkit.sos.xml.v100.Capabilities;
import org.geotoolkit.sos.xml.v100.GetCapabilities;
import java.net.URLConnection;
import java.net.URL;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.admin.ServiceBusiness;
import org.constellation.configuration.DataSourceType;
import org.constellation.configuration.SOSConfiguration;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.geotoolkit.sos.xml.SOSMarshallerPool;
import org.constellation.sos.ws.soap.SOService;
import org.constellation.test.utils.Order;
import org.constellation.test.utils.SpringTestRunner;
import org.constellation.util.Util;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.internal.sql.ScriptRunner;
import org.geotoolkit.observation.xml.v100.ObservationCollectionType;
import org.geotoolkit.ows.xml.v110.ExceptionReport;
import org.geotoolkit.sampling.xml.v100.SamplingPointType;
import org.geotoolkit.sml.xml.AbstractSensorML;
import org.geotoolkit.sos.xml.v100.DescribeSensor;
import org.geotoolkit.sos.xml.v200.CapabilitiesType;
import org.geotoolkit.sos.xml.v200.GetCapabilitiesType;
import org.junit.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@RunWith(SpringTestRunner.class)
public class SOSRequestTest extends AbstractGrizzlyServer {

    @Inject
    private ServiceBusiness serviceBusiness;
    
    private static String getDefaultURL() {
        return "http://localhost:" +  grizzly.getCurrentPort() + "/sos/default?";
    }

    private static String getTestURL() {
        return "http://localhost:" +  grizzly.getCurrentPort() + "/sos/test?";
    }


    /**
     * Initialize the list of layers from the defined providers in Constellation's configuration.
     */
    @PostConstruct
    public void initPool() {
        try {
            final File configDirectory = ConfigurationEngine.setupTestEnvironement("SOSRequestTest");
            
            final String url = "jdbc:derby:memory:TestOM2;create=true";
            final DefaultDataSource ds = new DefaultDataSource(url);
            Connection con = ds.getConnection();
            
            final ScriptRunner exec = new ScriptRunner(con);
            exec.run(Util.getResourceAsStream("org/constellation/data/om2/structure_observations.sql"));
            exec.run(Util.getResourceAsStream("org/constellation/sql/sos-data-om2.sql"));
            con.close();
            
            
            final File dataDirectory = new File(configDirectory, "dataSos");
            dataDirectory.mkdir();
            
            writeDataFile(dataDirectory, "urn-ogc-object-sensor-SunSpot-0014.4F01.0000.261A");
            writeDataFile(dataDirectory, "urn-ogc-object-sensor-SunSpot-0014.4F01.0000.2626");
            writeDataFile(dataDirectory, "urn-ogc-object-sensor-SunSpot-2");
            
            final Automatic smlConfig = new Automatic(null, dataDirectory.getPath());
            final Automatic omCOnfig = new Automatic(null, new BDD("derby", url, null, null));
            final SOSConfiguration sosconf = new SOSConfiguration(smlConfig, omCOnfig);
            sosconf.setObservationFilterType(DataSourceType.OM2);
            sosconf.setObservationReaderType(DataSourceType.OM2);
            sosconf.setObservationWriterType(DataSourceType.OM2);
            sosconf.setSMLType(DataSourceType.FILESYSTEM);
            sosconf.setProfile("transactional");
            sosconf.setObservationIdBase("urn:ogc:object:observation:SunSpot:");
            sosconf.setSensorIdBase("urn:ogc:object:sensor:SunSpot:");
            sosconf.setPhenomenonIdBase("urn:phenomenon:");
            sosconf.setObservationTemplateIdBase("urn:ogc:object:observationTemplate:SunSpot:");
            sosconf.setVerifySynchronization(false);
            
            serviceBusiness.create("SOS", "default", sosconf, null);
            serviceBusiness.create("SOS", "test", sosconf, null);
            
            final Map<String, Object> map = new HashMap<>();
            map.put("sos", new SOService());
            initServer(null, map);
            // Get the list of layers
            pool = SOSMarshallerPool.getInstance();
        } catch (Exception ex) {
            Logger.getLogger(SOSRequestTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @AfterClass
    public static void shutDown() {
        File f = new File("derby.log");
        if (f.exists()) {
            f.delete();
        }
        ConfigurationEngine.shutdownTestEnvironement("SOSRequestTest");
        finish();
    }

    @Test
    @Order(order=1)
    public void testSOSInvalidRequest() throws Exception {

        waitForStart();

        // Creates a valid GetCapabilities url.
        final URL getCapsUrl = new URL(getDefaultURL());


        // for a POST request
        URLConnection conec = getCapsUrl.openConnection();


        postRequestPlain(conec, "test");
        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof ExceptionReport);

        ExceptionReport report = (ExceptionReport) obj;

        assertEquals("InvalidRequest", report.getException().get(0).getExceptionCode());
    }

    @Test
    @Order(order=2)
    public void testSOSGetCapabilities() throws Exception {
        // Creates a valid GetCapabilities url.
        URL getCapsUrl = new URL(getDefaultURL());


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

        assertEquals(op.getDCP().get(0).getHTTP().getGetOrPost().get(0).getHref(), getDefaultURL());

        // Creates a valid GetCapabilties url.
        getCapsUrl = new URL(getTestURL() + "request=GetCapabilities&service=SOS&version=1.0.0");

        // Try to marshall something from the response returned by the server.
        // The response should be a Capabilities.
        obj = unmarshallResponse(getCapsUrl);
        assertTrue("was:" + obj, obj instanceof Capabilities);

        c = (Capabilities) obj;

        op = c.getOperationsMetadata().getOperation("GetObservation");

        assertEquals(op.getDCP().get(0).getHTTP().getGetOrPost().get(0).getHref(), getTestURL());

        // Creates a valid GetCapabilties url.
        getCapsUrl = new URL(getDefaultURL()+ "request=GetCapabilities&service=SOS&version=1.0.0");

        // Try to marshall something from the response returned by the server.
        // The response should be a Capabilities.
        obj = unmarshallResponse(getCapsUrl);
        assertTrue(obj instanceof Capabilities);

        c = (Capabilities) obj;

        op = c.getOperationsMetadata().getOperation("GetObservation");

        assertEquals(op.getDCP().get(0).getHTTP().getGetOrPost().get(0).getHref(), getDefaultURL());
    }
    
    @Test
    @Order(order=3)
    public void testSOSGetCapabilitiesv2() throws Exception {
        // Creates a valid GetCapabilities url.
        URL getCapsUrl = new URL(getDefaultURL());


        // for a POST request
        URLConnection conec = getCapsUrl.openConnection();

        final GetCapabilitiesType request = new GetCapabilitiesType("2.0.0", "text/xml");

        postRequestObject(conec, request);
        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof CapabilitiesType);

        CapabilitiesType c = (CapabilitiesType) obj;

        assertTrue(c.getOperationsMetadata() != null);

        Operation op = c.getOperationsMetadata().getOperation("GetObservation");

        assertTrue(op != null);
        assertTrue(op.getDCP().size() > 0);

        assertEquals(op.getDCP().get(0).getHTTP().getGetOrPost().get(0).getHref(), getDefaultURL());

        // Creates a valid GetCapabilties url.
        getCapsUrl = new URL(getTestURL() + "request=GetCapabilities&service=SOS&version=2.0.0");

        // Try to marshall something from the response returned by the server.
        // The response should be a Capabilities.
        obj = unmarshallResponse(getCapsUrl);
        assertTrue("was:" + obj, obj instanceof CapabilitiesType);

        c = (CapabilitiesType) obj;

        op = c.getOperationsMetadata().getOperation("GetObservation");

        assertEquals(op.getDCP().get(0).getHTTP().getGetOrPost().get(0).getHref(), getTestURL());

        // Creates a valid GetCapabilties url.
        getCapsUrl = new URL(getDefaultURL()+ "request=GetCapabilities&service=SOS&version=2.0.0");

        // Try to marshall something from the response returned by the server.
        // The response should be a Capabilities.
        obj = unmarshallResponse(getCapsUrl);
        assertTrue(obj instanceof CapabilitiesType);

        c = (CapabilitiesType) obj;

        op = c.getOperationsMetadata().getOperation("GetObservation");

        assertEquals(op.getDCP().get(0).getHTTP().getGetOrPost().get(0).getHref(), getDefaultURL());
    }

    @Test
    @Order(order=4)
    public void testSOSDescribeSensor() throws Exception {
        // Creates a valid DescribeSensor url.
        final URL getCapsUrl = new URL(getDefaultURL());


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
    @Order(order=5)
    public void testSOSGetObservation() throws Exception {
        // Creates a valid GetObservation url.
        final URL getCapsUrl = new URL(getDefaultURL());

        // for a POST request
        URLConnection conec = getCapsUrl.openConnection();

        GetObservation request  = new GetObservation("1.0.0",
                                      "offering-3",
                                      null,
                                      Arrays.asList("urn:ogc:object:sensor:GEOM:3"),
                                      Arrays.asList("urn:ogc:def:phenomenon:GEOM:depth"),
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
        assertTrue("expecting ObservationCollectionType but was: " + type, obj instanceof ObservationCollectionType);
    }

    @Test
    @Order(order=6)
    public void testSOSGetFeatureOfInterest() throws Exception {
        // Creates a valid GetObservation url.
        final URL getCapsUrl = new URL(getDefaultURL());

        // for a POST request
        URLConnection conec = getCapsUrl.openConnection();

        GetFeatureOfInterest request = new GetFeatureOfInterest("1.0.0", "SOS", "station-001");

        postRequestObject(conec, request);
        Object obj = unmarshallResponse(conec);

        String type = "null";
        if (obj != null) {
            type = obj.getClass().getName();
        }
        assertTrue("expecting SamplingPointType but was: " + type, obj instanceof SamplingPointType);

        // Creates a valid GetFeatureFInterest url.
        final URL getFoiUrl = new URL(getDefaultURL() + "request=GetFeatureOfInterest&service=SOS&version=1.0.0&FeatureOfInterestId=station-001");


        // Try to marshall something from the response returned by the server.
        // The response should be a Capabilities.
        obj = unmarshallResponse(getFoiUrl);

        assertTrue("expecting SamplingPointType but was: " + obj, obj instanceof SamplingPointType);
    }

    public static void writeDataFile(File dataDirectory, String resourceName) throws IOException {

        final File dataFile;
        if (System.getProperty("os.name", "").startsWith("Windows")) {
            final String windowsIdentifier = resourceName.replace(':', '-');
            dataFile = new File(dataDirectory, windowsIdentifier + ".xml");
        } else {
            dataFile = new File(dataDirectory, resourceName + ".xml");
        }
        FileWriter fw = new FileWriter(dataFile);
        InputStream in = Util.getResourceAsStream("org/constellation/embedded/test/" + resourceName + ".xml");

        byte[] buffer = new byte[1024];
        int size;

        while ((size = in.read(buffer, 0, 1024)) > 0) {
            fw.write(new String(buffer, 0, size));
        }
        in.close();
        fw.close();
    }
}
