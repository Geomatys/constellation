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
import java.util.Collections;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

// Constellation dependencies
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.LayerProviderService;
import org.constellation.provider.StyleProviderProxy;
import org.constellation.provider.StyleProviderService;
import org.constellation.provider.configuration.ProviderConfig;
import org.constellation.provider.configuration.ProviderLayer;
import org.constellation.provider.configuration.ProviderSource;
import org.constellation.provider.shapefile.ShapeFileProvider;
import org.constellation.provider.shapefile.ShapeFileProviderService;
import org.constellation.provider.sld.SLDProviderService;


/**
 * Launches a Grizzly server in a thread at the beginning of the testing process
 * and kill it when it is done.
 *
 * @version $Id$
 *
 * @author Cédric Briançon (Geomatys)
 * @since 0.4
 */
public class GrizzlyServer {
    /**
     * The grizzly server that will received some HTTP requests.
     */
    protected static GrizzlyThread grizzly = null;

    /**
     * Initialize the Grizzly server, on which WCS and WMS requests will be sent,
     * and defines a PostGrid data provider.
     */
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

        // Defines a Styles data provider
        final ProviderSource sourceStyle = new ProviderSource();
        sourceStyle.loadAll = true;
        sourceStyle.parameters.put(ShapeFileProvider.KEY_FOLDER_PATH,
                GrizzlyServer.class.getResource("wms111/styles").getFile());

        final ProviderConfig configStyle = new ProviderConfig();
        configStyle.sources.add(sourceStyle);

        for (StyleProviderService service : StyleProviderProxy.getInstance().getServices()) {
            // Here we should have the styles data provider defined previously
            if (service instanceof SLDProviderService) {
                service.init(configStyle);
                if (service.getProviders().isEmpty()) {
                    return;
                }
                break;
            }
        }

        // Defines a ShapeFile data provider
        final ProviderSource sourceShape = new ProviderSource();
        sourceShape.loadAll = true;
        sourceShape.parameters.put(ShapeFileProvider.KEY_FOLDER_PATH,
                GrizzlyServer.class.getResource("wms111/shapefiles").getFile());
        sourceShape.layers.add(new ProviderLayer("BasicPolygons", Collections.singletonList("cite_style_BasicPolygons"),
                               null, null, null, null, false, null));
        sourceShape.layers.add(new ProviderLayer("Bridges", Collections.singletonList("cite_style_Bridges"),
                               null, null, null, null, false, null));
        sourceShape.layers.add(new ProviderLayer("BuildingCenters", Collections.singletonList("cite_style_BuildingCenters"),
                               null, null, null, null, false, null));
        sourceShape.layers.add(new ProviderLayer("Buildings", Collections.singletonList("cite_style_Buildings"),
                               null, null, null, null, false, null));
        sourceShape.layers.add(new ProviderLayer("DividedRoutes", Collections.singletonList("cite_style_DividedRoutes"),
                               null, null, null, null, false, null));
        sourceShape.layers.add(new ProviderLayer("Forests", Collections.singletonList("cite_style_Forests"),
                               null, null, null, null, false, null));
        sourceShape.layers.add(new ProviderLayer("Lakes", Collections.singletonList("cite_style_Lakes"),
                               null, null, null, null, false, null));
        sourceShape.layers.add(new ProviderLayer("MapNeatline", Collections.singletonList("cite_style_MapNeatLine"),
                               null, null, null, null, false, null));
        sourceShape.layers.add(new ProviderLayer("NamedPlaces", Collections.singletonList("cite_style_NamedPlaces"),
                               null, null, null, null, false, null));
        sourceShape.layers.add(new ProviderLayer("Ponds", Collections.singletonList("cite_style_Ponds"),
                               null, null, null, null, false, null));
        sourceShape.layers.add(new ProviderLayer("RoadSegments", Collections.singletonList("cite_style_RoadSegments"),
                               null, null, null, null, false, null));
        sourceShape.layers.add(new ProviderLayer("Streams", Collections.singletonList("cite_style_Streams"),
                               null, null, null, null, false, null));


        final ProviderConfig configShape = new ProviderConfig();
        configShape.sources.add(sourceShape);

        for (LayerProviderService service : LayerProviderProxy.getInstance().getServices()) {
            // Here we should have the shapefile data provider defined previously
            if (service instanceof ShapeFileProviderService) {
                service.init(configShape);
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
            // That case should not occur.
            throw new AssertionError(ex);
        }
    }

    /**
     * Stop the grizzly server, if it is still alive.
     */
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
            cstlServer.duration = 20*60*1000;
            cstlServer.runREST();
        }
    }

    public static void main(String[] args) {
        initServer();
    }
}
