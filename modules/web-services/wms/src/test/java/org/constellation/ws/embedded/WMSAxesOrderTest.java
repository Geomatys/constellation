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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

// Constellation dependencies
import org.constellation.admin.ConfigurationEngine;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.Language;
import org.constellation.configuration.Languages;
import org.constellation.configuration.Layer;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.Layers;
import org.constellation.configuration.Source;
import org.constellation.configuration.WMSPortrayal;
import org.constellation.provider.Data;
import org.constellation.provider.DataProviders;
import org.constellation.provider.ProviderFactory;
import org.constellation.provider.Providers;
import org.constellation.provider.configuration.AbstractConfigurator;
import org.constellation.provider.configuration.Configurator;
import static org.constellation.provider.configuration.ProviderParameters.*;
import static org.constellation.provider.coveragesql.CoverageSQLProviderService.*;
import org.constellation.test.ImageTesting;
import static org.constellation.ws.embedded.AbstractGrizzlyServer.finish;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.image.io.plugin.WorldFileImageReader;
import org.geotoolkit.image.jai.Registry;
import org.geotoolkit.test.Commons;

// JUnit dependencies
import org.junit.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;


/**
 * Do some tests on the {@code WMS GetMap} request, in order to ensure that the axes
 * order is well handled by Constellation, for different kinds of
 * {@linkplain CoordinateReferenceSystem CRS}.
 *
 * @version $Id$
 *
 * @author Cédric Briançon (Geomatys)
 * @since 0.3
 */
public class WMSAxesOrderTest extends AbstractGrizzlyServer {

    /**
     * The layer to test.
     */
    private static final DefaultName LAYER_TEST = new DefaultName("SST_tests");

    /**
     * A list of available layers to be requested in WMS.
     */
    private static List<Data> layers;

    /**
     * Checksum value on the returned image expressed in a geographic CRS for the SST_tests layer.
     */
    private Long sstChecksumGeo = null;

    /**
     * URLs which will be tested on the server.
     */
    private static final String WMS_GETMAP_111_PROJ ="request=GetMap&service=WMS&version=1.1.1&" +
                                      "format=image/png&width=1024&height=512&" +
                                      "srs=EPSG:3395&bbox=-19000000,-19000000,19000000,19000000&" +
                                      "layers="+ LAYER_TEST +"&styles=";
    private static final String WMS_GETMAP_130_PROJ ="request=GetMap&service=WMS&version=1.3.0&" +
                                      "format=image/png&width=1024&height=512&" +
                                      "crs=EPSG:3395&bbox=-19000000,-19000000,19000000,19000000&" +
                                      "layers="+ LAYER_TEST +"&styles=";
    private static final String WMS_GETMAP_111_GEO ="request=GetMap&service=WMS&version=1.1.1&" +
                                      "format=image/png&width=1024&height=512&" +
                                      "srs=EPSG:4022&bbox=-90,-180,90,180&" +
                                      "layers="+ LAYER_TEST +"&styles=";
    private static final String WMS_GETMAP_111_EPSG_4326 ="request=GetMap&service=WMS&version=1.1.1&" +
                                      "format=image/png&width=1024&height=512&" +
                                      "srs=EPSG:4326&bbox=-180,-90,180,90&" +
                                      "layers="+ LAYER_TEST +"&styles=";
    private static final String WMS_GETMAP_130_EPSG_4326 ="request=GetMap&service=WMS&version=1.3.0&" +
                                      "format=image/png&width=512&height=1024&" +
                                      "crs=EPSG:4326&bbox=-90,-180,90,180&" +
                                      "layers="+ LAYER_TEST +"&styles=";
    private static final String WMS_GETMAP_111_CRS_84 ="request=GetMap&service=WMS&version=1.1.1&" +
                                      "format=image/png&width=1024&height=512&" +
                                      "srs=CRS:84&bbox=-180,-90,180,90&" +
                                      "layers="+ LAYER_TEST +"&styles=";
    private static final String WMS_GETMAP_130_CRS_84 ="request=GetMap&service=WMS&version=1.3.0&" +
                                      "format=image/png&width=1024&height=512&" +
                                      "crs=CRS:84&bbox=-180,-90,180,90&" +
                                      "layers="+ LAYER_TEST +"&styles=";

    public static boolean hasLocalDatabase() {
        return false; // TODO
    }
    
    /**
     * Initialize the list of layers from the defined providers in Constellation's configuration.
     */
    @BeforeClass
    public static void initLayerList() throws JAXBException {
        ConfigurationEngine.setupTestEnvironement("WMSAxesOrderTest");

        final List<Source> sources = Arrays.asList(new Source("coverageTestSrc", true, null, null));
        final Layers layers1 = new Layers(sources);
        final LayerContext config = new LayerContext(layers1);
        config.getCustomParameters().put("shiroAccessible", "false");

        ConfigurationEngine.storeConfiguration("WMS", "default", config);

        final List<Source> sources2 = Arrays.asList(new Source("coverageTestSrc", true, null, Arrays.asList(new Layer(new QName("SST_tests")))));
        final Layers layers2 = new Layers(sources2);
        final LayerContext config2 = new LayerContext(layers2);
        config2.setSupportedLanguages(new Languages(Arrays.asList(new Language("fre"), new Language("eng", true))));
        config2.getCustomParameters().put("shiroAccessible", "false");

        ConfigurationEngine.storeConfiguration("WMS", "wms1", config2);

        initServer(new String[] {
            "org.constellation.map.ws.rs",
            "org.constellation.configuration.ws.rs",
            "org.constellation.ws.rs.provider"
        }, null);

        final Configurator configurator = new AbstractConfigurator() {
            @Override
            public List<Map.Entry<String, ParameterValueGroup>> getProviderConfigurations() throws ConfigurationException {

                final ArrayList<Map.Entry<String, ParameterValueGroup>> lst = new ArrayList<>();
                final ProviderFactory factory = DataProviders.getInstance().getFactory("coverage-sql");
                
                if (hasLocalDatabase()) {
                    // Defines a PostGrid data provider
                    final ParameterValueGroup source = factory.getProviderDescriptor().createValue();
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
                    
                    lst.add(new AbstractMap.SimpleImmutableEntry<>("coverageTestSrc",source));
                }
                
                return lst;
            }

        };

        DataProviders.getInstance().setConfigurator(configurator);


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

        // Get the list of layers
        layers = DataProviders.getInstance().getAll();
    }

    /**
     * Free some resources.
     */
    @AfterClass
    public static void shutDown() {
        DataProviders.getInstance().setConfigurator(Providers.DEFAULT_CONFIGURATOR);
        layers = null;
        File f = new File("derby.log");
        if (f.exists()) {
            f.delete();
        }
        ConfigurationEngine.shutdownTestEnvironement("WMSAxesOrderTest");
        finish();
    }
    /**
     * Returns {@code true} if the {@code SST_tests} layer is found in the list of
     * available layers. It means the postgrid database, pointed by the postgrid.xml
     * file in the configuration directory, contains this layer and can then be requested
     * in WMS.
     */
    private static boolean containsTestLayer() {
        for (Data layer : layers) {
            if (layer.getName().equals(LAYER_TEST)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Ensures that a GetMap in version 1.1.1 on a projected
     * {@linkplain CoordinateReferenceSystem CRS} provides the same image that a GetMap
     * in version 1.3.0 on the same CRS.
     */
    @Test
    public void testGetMap111And130Projected() throws Exception {
        waitForStart();

        assertNotNull(layers);
        assumeTrue(!(layers.isEmpty()));
        assumeTrue(containsTestLayer());

        // Creates a valid GetMap url.
        final URL getMap111Url, getMap130Url;
        try {
            getMap111Url = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/default?" + WMS_GETMAP_111_PROJ);
            getMap130Url = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/default?" + WMS_GETMAP_130_PROJ);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to get a map from the url. The test is skipped in this method if it fails.
        final BufferedImage image111 = getImageFromURL(getMap111Url, "image/png");
        final BufferedImage image130 = getImageFromURL(getMap130Url, "image/png");

        // Tests on the returned images.
        assertTrue  (!(ImageTesting.isImageEmpty(image111)));
        assertEquals(1024, image111.getWidth());
        assertEquals(512,  image111.getHeight());
        assertTrue  (!(ImageTesting.isImageEmpty(image130)));
        assertEquals(1024, image130.getWidth());
        assertEquals(512,  image130.getHeight());
        assertEquals(Commons.checksum(image111), Commons.checksum(image130));
    }

    /**
     * Test a GetMap request on a WMS version 1.1.1 for a geographical
     * {@linkplain CoordinateReferenceSystem CRS}.
     *
     * TODO: fix the implementation of the GetMap request concerning the handling of
     *       geographical CRS (not WGS84) and do this test then.
     */
    @Ignore
    public void testCRSGeographique111() throws IOException {
        assertNotNull(layers);
        assumeTrue(!(layers.isEmpty()));
        assumeTrue(containsTestLayer());

        // Creates a valid GetMap url.
        final URL getMapUrl;
        try {
            getMapUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/default?" + WMS_GETMAP_111_GEO);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to get a map from the url. The test is skipped in this method if it fails.
        final BufferedImage image = getImageFromURL(getMapUrl, "image/png");

        // Tests on the returned image.
        assertTrue  (!(ImageTesting.isImageEmpty(image)));
        assertEquals(1024, image.getWidth());
        assertEquals(512,  image.getHeight());
    }

    /**
     * Verify the axis order for a GetMap in version 1.1.1 for the {@code WGS84} CRS.
     */
    @Test
    public void testGetMap111Epsg4326() throws IOException {
        assertNotNull(layers);
        assumeTrue(!(layers.isEmpty()));
        assumeTrue(containsTestLayer());

        // Creates a valid GetMap url.
        final URL getMapUrl;
        try {
            getMapUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/default?" + WMS_GETMAP_111_EPSG_4326);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to get a map from the url. The test is skipped in this method if it fails.
        final BufferedImage image = getImageFromURL(getMapUrl, "image/png");

        // Tests on the returned image.
        assertTrue  (!(ImageTesting.isImageEmpty(image)));
        assertEquals(1024, image.getWidth());
        assertEquals(512,  image.getHeight());
        if (sstChecksumGeo == null) {
            sstChecksumGeo = Commons.checksum(image);
            assertTrue(ImageTesting.getNumColors(image) > 8);
        } else {
            assertEquals(sstChecksumGeo.longValue(), Commons.checksum(image));
        }
    }

    /**
     * Verify the axis order for a GetMap in version 1.3.0 for the {@code WGS84} CRS.
     */
    @Test
    public void testGetMap130Epsg4326() throws IOException {
        assertNotNull(layers);
        assumeTrue(!(layers.isEmpty()));
        assumeTrue(containsTestLayer());

        // Creates a valid GetMap url.
        final URL getMapUrl;
        try {
            getMapUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/default?" + WMS_GETMAP_130_EPSG_4326);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to get a map from the url. The test is skipped in this method if it fails.
        final BufferedImage image = getImageFromURL(getMapUrl, "image/png");

        // Tests on the returned image.
        assertTrue  (!(ImageTesting.isImageEmpty(image)));
        assertEquals(512, image.getWidth());
        assertEquals(1024,  image.getHeight());
        if (sstChecksumGeo == null) {
            assertTrue(ImageTesting.getNumColors(image) > 8);
        } else {
            assertTrue(sstChecksumGeo.longValue() != Commons.checksum(image));
        }
    }

    /**
     * Verify the axis order for a GetMap in version 1.1.1 for the {@code WGS84} CRS.
     */
    @Test
    public void testGetMap111Crs84() throws IOException {
        assertNotNull(layers);
        assumeTrue(!(layers.isEmpty()));
        assumeTrue(containsTestLayer());

        // Creates a valid GetMap url.
        final URL getMapUrl;
        try {
            getMapUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/default?" + WMS_GETMAP_111_CRS_84);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to get a map from the url. The test is skipped in this method if it fails.
        final BufferedImage image = getImageFromURL(getMapUrl, "image/png");

        // Tests on the returned image.
        assertTrue  (!(ImageTesting.isImageEmpty(image)));
        assertEquals(1024, image.getWidth());
        assertEquals(512,  image.getHeight());
        if (sstChecksumGeo == null) {
            sstChecksumGeo = Commons.checksum(image);
            assertTrue(ImageTesting.getNumColors(image) > 8);
        } else {
            assertEquals(sstChecksumGeo.longValue(), Commons.checksum(image));
        }
    }

    /**
     * Verify the axis order for a GetMap in version 1.3.0 for the {@code WGS84} CRS.
     *
     * TODO: fix the implementation of the GetMap request concerning the axes order,
     *       and do this test then.
     */
    @Test
    public void testGetMap130Crs84() throws IOException {
        assertNotNull(layers);
        assumeTrue(!(layers.isEmpty()));
        assumeTrue(containsTestLayer());

        // Creates a valid GetMap url.
        final URL getMapUrl;
        try {
            getMapUrl = new URL("http://localhost:" + grizzly.getCurrentPort() + "/wms/default?" + WMS_GETMAP_130_CRS_84);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        // Try to get a map from the url. The test is skipped in this method if it fails.
        final BufferedImage image = getImageFromURL(getMapUrl, "image/png");

        // Tests on the returned image.
        assertTrue  (!(ImageTesting.isImageEmpty(image)));
        assertEquals(1024, image.getWidth());
        assertEquals(512,  image.getHeight());
        if (sstChecksumGeo == null) {
            sstChecksumGeo = Commons.checksum(image);
            assertTrue(ImageTesting.getNumColors(image) > 8);
        } else {
            assertEquals(sstChecksumGeo.longValue(), Commons.checksum(image));
        }
    }
}
