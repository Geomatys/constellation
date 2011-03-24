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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import javax.imageio.ImageReader;

// Constellation dependencies
import org.constellation.data.CoverageSQLTestCase;
import org.constellation.map.ws.WMSMapDecoration;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.configuration.Configurator;
import org.constellation.provider.configuration.ProviderConfig;
import org.constellation.provider.configuration.ProviderLayer;
import org.constellation.provider.configuration.ProviderSource;
import org.constellation.provider.coveragesql.CoverageSQLProvider;
import org.constellation.provider.om.OMProvider;
import org.constellation.provider.shapefile.ShapeFileProvider;
import org.constellation.util.Util;

// Geotoolkit dependencies
import org.geotoolkit.image.io.XImageIO;
import org.geotoolkit.image.io.plugin.WorldFileImageReader;
import org.geotoolkit.internal.io.IOUtilities;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.internal.sql.ScriptRunner;
import org.geotoolkit.util.logging.Logging;
import org.geotoolkit.feature.DefaultName;

// JUnit dependencies
import org.junit.*;
import static org.junit.Assume.*;


/**
 * Launches a Grizzly server in a thread at the beginning of the testing process
 * and kill it when it is done.
 *
 * @version $Id$
 *
 * @author Cédric Briançon (Geomatys)
 * @since 0.3
 */
public abstract class AbstractGrizzlyServer extends CoverageSQLTestCase {
    /**
     * A list of available layers to be requested in WMS.
     */
    protected static List<LayerDetails> layers;

    /**
     * The grizzly server that will received some HTTP requests.
     */
    protected static GrizzlyThread grizzly = null;

    /**
     * The layer to test.
     */
    protected static final DefaultName LAYER_TEST = new DefaultName("SST_tests");

    private static DefaultDataSource ds;
    
    private static final Logger LOGGER = Logging.getLogger("org.constellation.ws.embedded");

    private static boolean datasourceCreated = false;
    
    /**
     * Initialize the Grizzly server, on which WCS and WMS requests will be sent,
     * and defines a PostGrid data provider.
     */
    @BeforeClass
    public static void initServer() {
        // Protective test in order not to launch a new instance of the grizzly server for
        // each sub classes.
        if (grizzly != null) {
            return;
        }

        /* Instanciates the Grizzly server, but not start it at this moment.
         * The implementation waits for the data provider to be defined for
         * starting the server.
         */
        grizzly = new GrizzlyThread();

        final Configurator config = new Configurator() {
            @Override
            public ProviderConfig getConfiguration(String serviceName) {

                final ProviderConfig config = new ProviderConfig();

                if("coverage-sql".equals(serviceName)){
                    // Defines a PostGrid data provider
                    final ProviderSource source = new ProviderSource();
                    source.parameters.put(CoverageSQLProvider.KEY_DATABASE, "jdbc:postgresql://db.geomatys.com/coverages-test");
                    source.parameters.put(CoverageSQLProvider.KEY_DRIVER,   "org.postgresql.Driver");
                    source.parameters.put(CoverageSQLProvider.KEY_PASSWORD, "test");
                    source.parameters.put(CoverageSQLProvider.KEY_READONLY, "true");
                    final String rootDir = System.getProperty("java.io.tmpdir") + "/Constellation/images";
                    source.parameters.put(CoverageSQLProvider.KEY_ROOT_DIRECTORY, rootDir);
                    source.parameters.put(CoverageSQLProvider.KEY_USER,     "test");
                    source.parameters.put(CoverageSQLProvider.KEY_SCHEMA,   "coverages");
                    source.parameters.put(CoverageSQLProvider.KEY_NAMESPACE, "no namespace");
                    source.loadAll = true;
                    source.id      = "coverageTestSrc";
                    config.sources.add(source);
                }else if("observation".equals(serviceName)){
                    try{
                        final String url = "jdbc:derby:memory:TestWFSWorker";
                        ds = new DefaultDataSource(url + ";create=true");
                        if (!datasourceCreated) {
                            Connection con = ds.getConnection();
                            ScriptRunner sr = new ScriptRunner(con);
                            sr.run(Util.getResourceAsStream("org/constellation/sql/structure-observations.sql"));
                            sr.run(Util.getResourceAsStream("org/constellation/sql/sos-data.sql"));
                            con.close();
                            datasourceCreated = true;
                        }
                        final ProviderSource sourceOM = new ProviderSource();
                        sourceOM.loadAll = true;
                        sourceOM.parameters.put(OMProvider.KEY_SGBDTYPE, "derby");
                        sourceOM.parameters.put(OMProvider.KEY_DERBYURL, url);
                        sourceOM.id = "omSrc";
                        config.sources.add(sourceOM);
                    }catch(Exception ex){
                        throw new RuntimeException(ex.getLocalizedMessage(),ex);
                    }
                }else if("shapefile".equals(serviceName)){
                    try{
                        final File outputDir = initDataDirectory();
                        final ProviderSource sourceShape = new ProviderSource();
                        sourceShape.loadAll = true;
                        sourceShape.parameters.put(ShapeFileProvider.KEY_FOLDER_PATH, outputDir.getAbsolutePath() +
                                "/org/constellation/ws/embedded/wms111/shapefiles");
                        sourceShape.parameters.put(ShapeFileProvider.KEY_NAMESPACE, "http://www.opengis.net/gml");
                        sourceShape.layers.add(new ProviderLayer("NamedPlaces", Collections.singletonList("cite_style_NamedPlaces"),
                                               null, null, null, null, false, null));
                        sourceShape.id = "shapeSrc";
                        config.sources.add(sourceShape);
                    }catch(Exception ex){
                        throw new RuntimeException(ex.getLocalizedMessage(),ex);
                    }
                }

                //empty configuration for others
                return config;
            }
        };

        LayerProviderProxy.getInstance().setConfigurator(config);


        WorldFileImageReader.Spi.registerDefaults(null);
        WMSMapDecoration.setEmptyExtension(true);
        
        // Starting the grizzly server
        grizzly.start();

        // Waiting for grizzly server to be completely started
        try {
            Thread.sleep(3 * 1000);
        } catch (InterruptedException ex) {
            assumeNoException(ex);
        }
    }

    /**
     * Stop the grizzly server, if it is still alive.
     */
    @AfterClass
    public static void finish() {
        if (grizzly.isAlive()) {
            grizzly.interrupt();
        }
        if (ds != null) {
            ds.shutdown();
        }
        File f = new File("derby.log");
        if (f.exists()) {
            f.delete();
        }
    }

    /**
     * Returned the {@link BufferedImage} from an URL requesting an image.
     *
     * @param url  The url of a request of an image.
     * @param mime The mime type of the image to return.
     *
     * @return The {@link BufferedImage} or {@code null} if an error occurs.
     * @throws IOException
     */
    protected static BufferedImage getImageFromURL(final URL url, final String mime) throws IOException {
        // Try to get the image from the url.
        final InputStream in = url.openStream();
        final ImageReader reader = XImageIO.getReaderByMIMEType(mime, in, true, true);
        final BufferedImage image = reader.read(0);
        XImageIO.close(reader);
        reader.dispose();
        // For debugging, uncomment the JFrame creation and the Thread.sleep further,
        // in order to see the image in a popup.
//        javax.swing.JFrame frame = new javax.swing.JFrame();
//        frame.setContentPane(new javax.swing.JLabel(new javax.swing.ImageIcon(image)));
//        frame.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
//        frame.pack();
//        frame.setVisible(true);
//        try {
//            Thread.sleep(5 * 1000);
//            frame.dispose();
//        } catch (InterruptedException ex) {
//            assumeNoException(ex);
//        }
        return image;
    }

    /**
     * Returns {@code true} if the {@code SST_tests} layer is found in the list of
     * available layers. It means the postgrid database, pointed by the postgrid.xml
     * file in the configuration directory, contains this layer and can then be requested
     * in WMS.
     */
    protected static boolean containsTestLayer() {
        for (LayerDetails layer : layers) {
            if (layer.getName().equals(LAYER_TEST)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Thread that launches a Grizzly server in a separate thread.
     * Requests will be done on this working server.
     */
    protected static class GrizzlyThread extends Thread {
        /**
         * Runs a Grizzly server for five minutes.
         */
        @Override
        public void run() {
            final CstlEmbeddedService cstlServer = new CstlEmbeddedService(new String[]{});
            cstlServer.duration = 5*60*1000;
            cstlServer.runREST();
        }
    }

    /**
     * Initializes the data directory in unzipping the jar containing the resources
     * into a temporary directory.
     *
     * @return The root output directory where the data are unzipped.
     * @throws IOException
     */
    private static File initDataDirectory() throws IOException {
        final ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        String styleResource = classloader.getResource("org/constellation/ws/embedded/wms111/styles").getFile();
        if (styleResource.indexOf('!') != -1) {
            styleResource = styleResource.substring(0, styleResource.indexOf('!'));
        }
        if (styleResource.startsWith("file:")) {
            styleResource = styleResource.substring(5);
        }
        final File styleJar = new File(styleResource);
        if (styleJar == null || !styleJar.exists()) {
            throw new IOException("Unable to find the style folder: "+ styleJar);
        }
        if (styleJar.isDirectory()) {
            return styleJar;
        }
        final InputStream in = new FileInputStream(styleJar);
        final File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        final File outputDir = new File(tmpDir, "Constellation");
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }
        IOUtilities.unzip(in, outputDir);
        in.close();
        return outputDir;
    }
}
