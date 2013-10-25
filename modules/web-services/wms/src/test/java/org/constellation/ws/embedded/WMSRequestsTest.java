/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009-2010, Geomatys
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

// J2SE dependencies
import java.util.Arrays;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.xml.bind.JAXBException;
import org.constellation.configuration.WMSPortrayal;

// Constellation dependencies
import org.constellation.test.ImageTesting;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.Provider;
import org.constellation.provider.ProviderService;
import org.constellation.provider.configuration.Configurator;

import static org.constellation.provider.coveragesql.CoverageSQLProviderService.*;
import static org.constellation.provider.configuration.ProviderParameters.*;
import static org.constellation.ws.embedded.AbstractGrizzlyServer.initDataDirectory;

// Geotoolkit dependencies
import org.geotoolkit.wms.xml.WMSMarshallerPool;
import org.geotoolkit.sld.xml.v110.DescribeLayerResponseType;
import org.geotoolkit.sld.xml.v110.LayerDescriptionType;
import org.geotoolkit.sld.xml.v110.TypeNameType;
import org.geotoolkit.wms.xml.v111.LatLonBoundingBox;
import org.geotoolkit.wms.xml.v111.Layer;
import org.geotoolkit.wms.xml.v111.WMT_MS_Capabilities;
import org.geotoolkit.wms.xml.v130.WMSCapabilities;
import org.geotoolkit.inspire.xml.vs.ExtendedCapabilitiesType;
import org.geotoolkit.inspire.xml.vs.LanguagesType;
import org.geotoolkit.inspire.xml.vs.LanguageType;
import org.geotoolkit.ogc.xml.exception.ServiceExceptionReport;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.image.io.plugin.WorldFileImageReader;
import org.geotoolkit.image.jai.Registry;
import static org.geotoolkit.parameter.ParametersExt.createGroup;
import static org.geotoolkit.parameter.ParametersExt.getOrCreateGroup;
import static org.geotoolkit.parameter.ParametersExt.getOrCreateValue;

// JUnit dependencies

import org.junit.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;

import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;


/**
 * A set of methods that request a Grizzly server which embeds a WMS service.
 *
 * @version $Id$
 *
 * @author Cédric Briançon (Geomatys)
 * @since 0.3
 */
public class WMSRequestsTest extends AbstractGrizzlyServer {

    /**
     * The layer to test.
     */
    private static final DefaultName LAYER_TEST = new DefaultName("SST_tests");

    /**
     * URLs which will be tested on the server.
     */
    private static final String WMS_GETCAPABILITIES ="request=GetCapabilities&service=WMS&version=1.1.1";

    private static final String WMS_GETCAPABILITIES_WMS1_111 ="request=GetCapabilities&service=WMS&version=1.1.1";

    private static final String WMS_GETCAPABILITIES_WMS1 ="request=GetCapabilities&service=WMS&version=1.3.0";

    private static final String WMS_GETCAPABILITIES_WMS1_FRE ="request=GetCapabilities&service=WMS&version=1.3.0&language=fre";

    private static final String WMS_GETCAPABILITIES_WMS1_ENG ="request=GetCapabilities&service=WMS&version=1.3.0&language=eng";

    private static final String WMS_FALSE_REQUEST ="request=SomethingElse";

    private static final String WMS_GETMAP ="request=GetMap&service=WMS&version=1.1.1&" +
                                      "format=image/png&width=1024&height=512&" +
                                      "srs=EPSG:4326&bbox=-180,-90,180,90&" +
                                      "layers="+ LAYER_TEST +"&styles=";

    private static final String WMS_GETFEATUREINFO ="request=GetFeatureInfo&service=WMS&version=1.1.1&" +
                                      "format=image/png&width=1024&height=512&" +
                                      "srs=EPSG:4326&bbox=-180,-90,180,90&" +
                                      "layers="+ LAYER_TEST +"&styles=&" +
                                      "query_layers="+ LAYER_TEST +"&" + "info_format=text/plain&" +
                                      "X=300&Y=200";

    private static final String WMS_GETLEGENDGRAPHIC = "request=GetLegendGraphic&service=wms&" +
            "width=200&height=40&layer="+ LAYER_TEST +"&format=image/png&version=1.1.0";

    private static final String WMS_DESCRIBELAYER ="request=DescribeLayer&service=WMS&" +
            "version=1.1.1&layers="+ LAYER_TEST;

    private static final String WMS_GETMAP2 =
    "HeIgHt=100&LaYeRs=Lakes&FoRmAt=image/png&ReQuEsT=GetMap&StYlEs=&CrS=CRS:84&BbOx=-0.0025,-0.0025,0.0025,0.0025&VeRsIoN=1.3.0&WiDtH=100";

    private static final String WMS_GETMAP_BMP =
    "HeIgHt=100&LaYeRs=Lakes&FoRmAt=image/bmp&ReQuEsT=GetMap&StYlEs=&CrS=CRS:84&BbOx=-0.0025,-0.0025,0.0025,0.0025&VeRsIoN=1.3.0&WiDtH=100";

    private static final String WMS_GETMAP_PPM =
    "HeIgHt=100&LaYeRs=Lakes&FoRmAt=image/x-portable-pixmap&ReQuEsT=GetMap&StYlEs=&CrS=CRS:84&BbOx=-0.0025,-0.0025,0.0025,0.0025&VeRsIoN=1.3.0&WiDtH=100";

    private static final String WMS_GETMAP_GIF =
    "HeIgHt=100&LaYeRs=Lakes&FoRmAt=image/gif&ReQuEsT=GetMap&StYlEs=&CrS=CRS:84&BbOx=-0.0025,-0.0025,0.0025,0.0025&VeRsIoN=1.3.0&WiDtH=100";

    private static final String WMS_GETMAP_GIF_TRANSPARENT =
    "TrAnSpArEnT=TRUE&CrS=CRS:84&FoRmAt=image%2Fgif&VeRsIoN=1.3.0&HeIgHt=100&WiDtH=200&StYlEs=&LaYeRs=cite%3ALakes&ReQuEsT=GetMap&BbOx=0,-0.0020,0.0040,0";
    /**
     * Initialize the list of layers from the defined providers in Constellation's configuration.
     */
    @BeforeClass
    public static void initLayerList() throws JAXBException {
        initServer(new String[] {
            "org.constellation.map.ws.rs",
            "org.constellation.configuration.ws.rs",
            "org.constellation.ws.rs.provider"
        }, null);

        pool = WMSMarshallerPool.getInstance();

        final Configurator config = new Configurator() {
            @Override
            public ParameterValueGroup getConfiguration(final ProviderService service) {

                final ParameterValueGroup config = service.getServiceDescriptor().createValue();

                if("coverage-sql".equals(service.getName())){
                    // Defines a PostGrid data provider
                    final ParameterValueGroup source = config.addGroup(SOURCE_DESCRIPTOR_NAME);
                    final ParameterValueGroup srcconfig = getOrCreate(COVERAGESQL_DESCRIPTOR,source);
                    srcconfig.parameter(URL_DESCRIPTOR.getName().getCode()).setValue("jdbc:postgresql://flupke.geomatys.com/coverages-test");
                    srcconfig.parameter(PASSWORD_DESCRIPTOR.getName().getCode()).setValue("test");
                    final String rootDir = System.getProperty("java.io.tmpdir") + "/Constellation/images";
                    srcconfig.parameter(ROOT_DIRECTORY_DESCRIPTOR.getName().getCode()).setValue(rootDir);
                    srcconfig.parameter(USER_DESCRIPTOR.getName().getCode()).setValue("test");
                    srcconfig.parameter(SCHEMA_DESCRIPTOR.getName().getCode()).setValue("coverages");
                    srcconfig.parameter(NAMESPACE_DESCRIPTOR.getName().getCode()).setValue("no namespace");
                    source.parameter(SOURCE_LOADALL_DESCRIPTOR.getName().getCode()).setValue(Boolean.TRUE);
                    source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("coverageTestSrc");

                }else if("feature-store".equals(service.getName())){
                    try{
                        final File outputDir = initDataDirectory();
                        final ParameterValueGroup source = createGroup(config,SOURCE_DESCRIPTOR_NAME);
                        getOrCreateValue(source, "id").setValue("shapeSrc");
                        getOrCreateValue(source, "load_all").setValue(true);    
                        
                        final ParameterValueGroup choice = getOrCreateGroup(source, "choice");
                        final ParameterValueGroup shpconfig = createGroup(choice, "ShapefileParametersFolder");
                        getOrCreateValue(shpconfig, "url").setValue(new URL("file:"+outputDir.getAbsolutePath() + "/org/constellation/ws/embedded/wms111/shapefiles"));
                        getOrCreateValue(shpconfig, "namespace").setValue("http://www.opengis.net/gml");        
                        
                        final ParameterValueGroup layer = getOrCreateGroup(source, "Layer");
                        getOrCreateValue(layer, "name").setValue("NamedPlaces");
                        getOrCreateValue(layer, "style").setValue("cite_style_NamedPlaces");     
                        
                    }catch(Exception ex){
                        throw new RuntimeException(ex.getLocalizedMessage(),ex);
                    }
                }
                //empty configuration for others
                return config;
            }

            @Override
            public void saveConfiguration(ProviderService service, List<Provider> providers) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };

        LayerProviderProxy.getInstance().setConfigurator(config);

        WorldFileImageReader.Spi.registerDefaults(null);
        WMSPortrayal.setEmptyExtension(true);

        //reset values, only allow pure java readers
        for(String jn : ImageIO.getReaderFormatNames()){
            Registry.setNativeCodecAllowed(jn, ImageReaderSpi.class, false);
        }

        //reset values, only allow pure java writers
        for(String jn : ImageIO.getWriterFormatNames()){
            Registry.setNativeCodecAllowed(jn, ImageWriterSpi.class, false);
        }
    }

    @AfterClass
    public static void shutDown() throws JAXBException {
        LayerProviderProxy.getInstance().setConfigurator(Configurator.DEFAULT);
        //finish();
    }

    /**
     * Ensure that a wrong value given in the request parameter for the WMS server
     * returned an error report for the user.
     */
    @Test
    public void testWMSWrongRequest() throws Exception {

        waitForStart();

        // Creates an intentional wrong url, regarding the WMS version 1.1.1 standard
        final URL wrongUrl;
        try {
            wrongUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/default?" + WMS_FALSE_REQUEST);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to marshall something from the response returned by the server.
        // The response should be a ServiceExceptionReport.
        final Object obj = unmarshallResponse(wrongUrl);
        assertTrue(obj instanceof ServiceExceptionReport);
    }

    /**
     * Ensures that a valid GetMap request returns indeed a {@link BufferedImage}.
     */
    @Test
    public void testWMSGetMap() throws IOException {
        // Creates a valid GetMap url.
        final URL getMapUrl;
        try {
            getMapUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/default?" + WMS_GETMAP);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to get a map from the url. The test is skipped in this method if it fails.
        final BufferedImage image = getImageFromURL(getMapUrl, "image/png");

        // Test on the returned image.
        assertTrue  (!(ImageTesting.isImageEmpty(image)));
        assertEquals(1024, image.getWidth());
        assertEquals(512,  image.getHeight());
        assertTrue  (ImageTesting.getNumColors(image) > 8);
    }

    /**
     * Ensures that a valid GetMap request returns indeed a {@link BufferedImage}.
     */
    @Test
    public void testWMSGetMapLakeGif() throws IOException {
                // Creates a valid GetMap url.
        final URL getMapUrl;
        try {
            getMapUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/default?" + WMS_GETMAP_GIF);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to get a map from the url. The test is skipped in this method if it fails.
        final BufferedImage image = getImageFromURL(getMapUrl, "image/gif");

        // Test on the returned image.
        assertTrue  (!(ImageTesting.isImageEmpty(image)));
        assertEquals(100, image.getWidth());
        assertEquals(100,  image.getHeight());
        assertTrue  (ImageTesting.getNumColors(image) > 2);
    }

    /**
     * Ensures that a valid GetMap request returns indeed a {@link BufferedImage}.
     */
    @Test
    public void testWMSGetMapLakeGifransparent() throws IOException {
                // Creates a valid GetMap url.
        final URL getMapUrl;
        try {
            getMapUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/default?" + WMS_GETMAP_GIF_TRANSPARENT);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to get a map from the url. The test is skipped in this method if it fails.
        final BufferedImage image = getImageFromURL(getMapUrl, "image/gif");

        // Test on the returned image.
        assertEquals(200, image.getWidth());
        assertEquals(100,  image.getHeight());
    }

    /**
     * Ensures that a valid GetMap request returns indeed a {@link BufferedImage}.
     */
    @Test
    public void testWMSGetMapLakePng() throws IOException {
        // Creates a valid GetMap url.
        final URL getMapUrl;
        try {
            getMapUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/default?" + WMS_GETMAP2);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to get a map from the url. The test is skipped in this method if it fails.
        final BufferedImage image = getImageFromURL(getMapUrl, "image/png");

        // Test on the returned image.
        assertTrue  (!(ImageTesting.isImageEmpty(image)));
        assertEquals(100, image.getWidth());
        assertEquals(100,  image.getHeight());
        assertTrue  (ImageTesting.getNumColors(image) > 2);
    }

    /**
     * Ensures that a valid GetMap request returns indeed a {@link BufferedImage}.
     */
    @Test
    public void testWMSGetMapLakeBmp() throws IOException {
        // Creates a valid GetMap url.
        final URL getMapUrl;
        try {
            getMapUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/default?" + WMS_GETMAP_BMP);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to get a map from the url. The test is skipped in this method if it fails.
        final BufferedImage image = getImageFromURL(getMapUrl, "image/bmp");

        // Test on the returned image.
        assertTrue  (!(ImageTesting.isImageEmpty(image)));
        assertEquals(100, image.getWidth());
        assertEquals(100,  image.getHeight());
        assertTrue  (ImageTesting.getNumColors(image) > 2);
    }

    /**
     * Ensures that a valid GetMap request returns indeed a {@link BufferedImage}.
     */
    @Test
    public void testWMSGetMapLakePpm() throws IOException {
        // Creates a valid GetMap url.
        final URL getMapUrl;
        try {
            getMapUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/default?" + WMS_GETMAP_PPM);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to get a map from the url. The test is skipped in this method if it fails.
        final BufferedImage image = getImageFromURL(getMapUrl, "image/x-portable-pixmap");

        // Test on the returned image.
        assertTrue  (!(ImageTesting.isImageEmpty(image)));
        assertEquals(100, image.getWidth());
        assertEquals(100,  image.getHeight());
        assertTrue  (ImageTesting.getNumColors(image) > 2);
    }

    /**
     * Ensures that a valid GetCapabilities request returns indeed a valid GetCapabilities
     * document representing the server capabilities in the WMS version 1.1.1/ 1.3.0 standard.
     */
    @Test
    public void testWMSGetCapabilities() throws JAXBException, IOException {
        // Creates a valid GetCapabilities url.
        URL getCapsUrl;
        try {
            getCapsUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/default?" + WMS_GETCAPABILITIES);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to marshall something from the response returned by the server.
        // The response should be a WMT_MS_Capabilities.
        Object obj = unmarshallResponse(getCapsUrl);
        assertTrue("was:" + obj, obj instanceof WMT_MS_Capabilities);

        WMT_MS_Capabilities responseCaps = (WMT_MS_Capabilities)obj;
        Layer layer = (Layer) responseCaps.getLayerFromName(LAYER_TEST.getLocalPart());

        assertNotNull(layer);
        assertEquals("EPSG:4326", layer.getSRS().get(0));
        final LatLonBoundingBox bboxGeo = (LatLonBoundingBox) layer.getLatLonBoundingBox();
        assertTrue(bboxGeo.getWestBoundLongitude() == -180d);
        assertTrue(bboxGeo.getSouthBoundLatitude() ==  -90d);
        assertTrue(bboxGeo.getEastBoundLongitude() ==  180d);
        assertTrue(bboxGeo.getNorthBoundLatitude() ==   90d);

        String currentUrl = responseCaps.getCapability().getRequest().getGetMap().getDCPType().get(0).getHTTP().getGet().getOnlineResource().getHref();

        assertEquals("http://localhost:" + grizzly.getCurrentPort() + "/wms/default?", currentUrl);

        // Creates a valid GetCapabilities url.
        try {
            getCapsUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/wms1?" + WMS_GETCAPABILITIES_WMS1_111);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }
        // Try to marshall something from the response returned by the server.
        // The response should be a WMT_MS_Capabilities.
        obj = unmarshallResponse(getCapsUrl);
        assertTrue(obj instanceof WMT_MS_Capabilities);

        responseCaps = (WMT_MS_Capabilities) obj;

        // The layer test must be excluded
        layer = (Layer) responseCaps.getLayerFromName(LAYER_TEST.getLocalPart());
        assertNull(layer);

        // The layer lake must be included
        layer = (Layer) responseCaps.getLayerFromName("http://www.opengis.net/gml:Lakes");
        assertNotNull(layer);

        currentUrl = responseCaps.getCapability().getRequest().getGetMap().getDCPType().get(0).getHTTP().getGet().getOnlineResource().getHref();

        assertEquals("http://localhost:" + grizzly.getCurrentPort() + "/wms/wms1?", currentUrl);

        try {
            getCapsUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/default?" + WMS_GETCAPABILITIES);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to marshall something from the response returned by the server.
        // The response should be a WMT_MS_Capabilities.
        obj = unmarshallResponse(getCapsUrl);
        assertTrue(obj instanceof WMT_MS_Capabilities);
        responseCaps = (WMT_MS_Capabilities) obj;

        currentUrl = responseCaps.getCapability().getRequest().getGetMap().getDCPType().get(0).getHTTP().getGet().getOnlineResource().getHref();

        assertEquals("http://localhost:" + grizzly.getCurrentPort() + "/wms/default?", currentUrl);

    }

    @Test
    public void testWMSGetCapabilitiesLanguage() throws JAXBException, IOException {
         // Creates a valid GetMap url.
        URL getCapsUrl;
        try {
            getCapsUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/wms1?" +WMS_GETCAPABILITIES_WMS1);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }
        // Try to marshall something from the response returned by the server.
        // The response should be a WMT_MS_Capabilities.
        Object obj = unmarshallResponse(getCapsUrl);
        assertTrue(obj instanceof WMSCapabilities);

        WMSCapabilities responseCaps130 = (WMSCapabilities)obj;
        ExtendedCapabilitiesType  ext = responseCaps130.getCapability().getInspireExtendedCapabilities();
        assertEquals("eng", ext.getCurrentLanguage());

        LanguageType l1 = new LanguageType("fre", false);
        LanguageType l2 = new LanguageType("eng", true);
        LanguagesType languages = new LanguagesType(Arrays.asList(l1, l2));
        assertEquals(ext.getLanguages(), languages);

        assertEquals("this is the default english capabilities", responseCaps130.getService().getName());

        try {
            getCapsUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/wms1?" +WMS_GETCAPABILITIES_WMS1_ENG);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }
        // Try to marshall something from the response returned by the server.
        // The response should be a WMT_MS_Capabilities.
        obj = unmarshallResponse(getCapsUrl);
        assertTrue(obj instanceof WMSCapabilities);

        responseCaps130 = (WMSCapabilities)obj;
        ext = responseCaps130.getCapability().getInspireExtendedCapabilities();
        assertEquals("eng", ext.getCurrentLanguage());
        assertEquals(ext.getLanguages(), languages);

        assertEquals("this is the default english capabilities", responseCaps130.getService().getName());

        try {
            getCapsUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/wms1?" +WMS_GETCAPABILITIES_WMS1_FRE);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }
        // Try to marshall something from the response returned by the server.
        // The response should be a WMT_MS_Capabilities.
        obj = unmarshallResponse(getCapsUrl);
        assertTrue(obj instanceof WMSCapabilities);

        responseCaps130 = (WMSCapabilities)obj;
        ext = responseCaps130.getCapability().getInspireExtendedCapabilities();
        assertEquals("fre", ext.getCurrentLanguage());
        assertEquals(ext.getLanguages(), languages);

        assertEquals("Ceci est le document capabilities français", responseCaps130.getService().getName());

    }

    /**
     * Ensures that the {@code WMS GetFeatureInfo} request on a particular point of the
     * testing layer produces the wanted result.
     */
    @Test
    public void testWMSGetFeatureInfo() throws IOException {
        // Creates a valid GetFeatureInfo url.
        final URL gfi;
        try {
            gfi = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/default?" + WMS_GETFEATUREINFO);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        String value = null;

        final InputStream inGfi = gfi.openStream();
        final InputStreamReader isr = new InputStreamReader(inGfi);
        final BufferedReader reader = new BufferedReader(isr);
        String fullResponse = "";
        String line;
        while ((line = reader.readLine()) != null) {
            // Verify that the line starts with a number, only the one with the value
            // should begin like this.
            if (line.matches("[0-9]+.*")) {
                // keep the line with the value
                value = line;
            }
            fullResponse = fullResponse + line + '\n';
        }
        reader.close();

        // Tests on the returned value
        assertNotNull(fullResponse, value);
        assertTrue   (value.startsWith("28.5"));
    }

    /**
     * Ensures that a valid GetLegendGraphic request returns indeed a {@link BufferedImage}.
     *
     * TODO : ignore until the getlegendgraphic method is done into the new
     *        postgrid implementation.
     */
    @Test
    @Ignore
    public void testWMSGetLegendGraphic() throws IOException {
        // Creates a valid GetLegendGraphic url.
        final URL getLegendUrl;
        try {
            getLegendUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/default?" + WMS_GETLEGENDGRAPHIC);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to get a map from the url. The test is skipped in this method if it fails.
        final BufferedImage image = getImageFromURL(getLegendUrl, "image/png");

        // Test on the returned image.
        assertTrue  (!(ImageTesting.isImageEmpty(image)));
        assertEquals(200, image.getWidth());
        assertEquals(40,  image.getHeight());
    }

    /**
     * Ensures that a valid DescribeLayer request produces a valid document.
     */
    @Test
    public void testWMSDescribeLayer() throws JAXBException, IOException {
        // Creates a valid DescribeLayer url.
        final URL describeUrl;
        try {
            describeUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/default?" + WMS_DESCRIBELAYER);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to marshall something from the response returned by the server.
        // The response should be a WMT_MS_Capabilities.
        final Object obj = unmarshallResponse(describeUrl);
        assertTrue(obj instanceof DescribeLayerResponseType);

        // Tests on the response
        final DescribeLayerResponseType desc = (DescribeLayerResponseType)obj;
        final List<LayerDescriptionType> layerDescs = desc.getLayerDescription();
        assertFalse(layerDescs.isEmpty());
        final List<TypeNameType> typeNames = layerDescs.get(0).getTypeName();
        assertFalse(typeNames.isEmpty());
        final DefaultName name = new DefaultName(typeNames.get(0).getCoverageName());
        assertEquals(LAYER_TEST, name);
    }

    /**
     * Free some resources.
     */
    @AfterClass
    public static void finish() {
    }
}
