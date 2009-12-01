/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

// Constellation dependencies
import org.constellation.data.PostgridTestCase;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.LayerProviderService;
import org.constellation.provider.configuration.ProviderConfig;
import org.constellation.provider.configuration.ProviderSource;
import org.constellation.provider.postgrid.PostGridProvider;
import org.constellation.provider.postgrid.PostGridProviderService;

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
public abstract class AbstractGrizzlyServer extends PostgridTestCase {
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
    protected static final String LAYER_TEST = "SST_tests";

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

        // Defines a PostGrid data provider
        final ProviderSource source = new ProviderSource();
        source.parameters.put(PostGridProvider.KEY_DATABASE, "jdbc:postgresql://hyperion.geomatys.com/coverages-test");
        source.parameters.put(PostGridProvider.KEY_DRIVER,   "org.postgresql.Driver");
        source.parameters.put(PostGridProvider.KEY_PASSWORD, "g3ouser");
        source.parameters.put(PostGridProvider.KEY_READONLY, "true");
        final String rootDir = System.getProperty("java.io.tmpdir") + "/Constellation/images";
        source.parameters.put(PostGridProvider.KEY_ROOT_DIRECTORY, rootDir);
        source.parameters.put(PostGridProvider.KEY_USER,     "geouser");

        final ProviderConfig config = new ProviderConfig();
        config.sources.add(source);

        for (LayerProviderService service : LayerProviderProxy.getInstance().getServices()) {
            // Here we should have the postgrid data provider defined previously
            if (service instanceof PostGridProviderService) {
                service.setConfiguration(config);
                assumeTrue(!(service.getProviders().isEmpty()));
                if (service.getProviders().isEmpty()) {
                    return;
                }
                break;
            }
        }

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
        final ImageInputStream iis = ImageIO.createImageInputStream(in);
        final Iterator<ImageReader> irs = ImageIO.getImageReadersByMIMEType(mime);
        if (!irs.hasNext()) {
            return null;
        }
        final ImageReader ir = irs.next();
        ir.setInput(iis, true, true);
        final BufferedImage image = ir.read(0);
        ir.dispose();
        iis.close();
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
}
