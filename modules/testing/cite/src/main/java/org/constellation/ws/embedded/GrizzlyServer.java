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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

// Constellation dependencies
import java.util.logging.Logger;
import org.constellation.data.PostgridTestCase;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.LayerProviderService;
import org.constellation.provider.StyleProviderProxy;
import org.constellation.provider.StyleProviderService;
import org.constellation.provider.configuration.ProviderConfig;
import org.constellation.provider.configuration.ProviderLayer;
import org.constellation.provider.configuration.ProviderSource;
import org.constellation.provider.postgis.PostGisProvider;
import org.constellation.provider.postgis.PostGisProviderService;
import org.constellation.provider.postgrid.PostGridProvider;
import org.constellation.provider.postgrid.PostGridProviderService;
import org.constellation.provider.shapefile.ShapeFileProvider;
import org.constellation.provider.shapefile.ShapeFileProviderService;
import org.constellation.provider.sld.SLDProvider;
import org.constellation.provider.sld.SLDProviderService;
import org.geotoolkit.internal.io.IOUtilities;
import org.geotoolkit.resources.NIOUtilities;
import org.geotoolkit.util.logging.Logging;


/**
 * Launches a Grizzly server in a thread at the beginning of the testing process
 * and kill it when it is done.
 *
 * @version $Id$
 *
 * @author Cédric Briançon (Geomatys)
 * @since 0.4
 */
public final class GrizzlyServer {
    /**
     * The default logger for this server.
     */
    private static final Logger LOGGER = Logging.getLogger(GrizzlyServer.class);

    /**
     * The grizzly server that will received some HTTP requests.
     */
    static GrizzlyThread grizzly = null;

    /**
     * Prevents instanciation.
     */
    private GrizzlyServer() {}

    /**
     * Initialize the Grizzly server, on which WCS and WMS requests will be sent,
     * and defines a PostGrid data provider.
     */
    public static synchronized void initServer() throws IOException {
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

        // Initialises the postgrid testing raster.
        PostgridTestCase.init();

        // Defines a PostGrid data provider
        final ProviderSource sourcePostGrid = new ProviderSource();
        sourcePostGrid.parameters.put(PostGridProvider.KEY_DATABASE, "jdbc:postgresql://db.geomatys.com/coverages-test");
        sourcePostGrid.parameters.put(PostGridProvider.KEY_DRIVER,   "org.postgresql.Driver");
        sourcePostGrid.parameters.put(PostGridProvider.KEY_PASSWORD, "test");
        sourcePostGrid.parameters.put(PostGridProvider.KEY_READONLY, "true");
        final String rootDir = System.getProperty("java.io.tmpdir") + "/Constellation/images";
        sourcePostGrid.parameters.put(PostGridProvider.KEY_ROOT_DIRECTORY, rootDir);
        sourcePostGrid.parameters.put(PostGridProvider.KEY_USER,     "test");

        final ProviderConfig configPostGrid = new ProviderConfig();
        configPostGrid.sources.add(sourcePostGrid);

        for (LayerProviderService service : LayerProviderProxy.getInstance().getServices()) {
            // Here we should have the postgrid data provider defined previously
            if (service instanceof PostGridProviderService) {
                service.setConfiguration(configPostGrid);
                if (service.getProviders().isEmpty()) {
                    return;
                }
                break;
            }
        }

        // Extracts the zip data into a temporary folder
        final File outputDir = initDataDirectory();

        // Defines a Styles data provider
        final ProviderSource sourceStyle = new ProviderSource();
        sourceStyle.loadAll = true;
        sourceStyle.parameters.put(SLDProvider.KEY_FOLDER_PATH, outputDir.getAbsolutePath() +
                "/org/constellation/ws/embedded/wms111/styles");

        final ProviderConfig configStyle = new ProviderConfig();
        configStyle.sources.add(sourceStyle);

        for (StyleProviderService service : StyleProviderProxy.getInstance().getServices()) {
            // Here we should have the styles data provider defined previously
            if (service instanceof SLDProviderService) {
                service.setConfiguration(configStyle);
                if (service.getProviders().isEmpty()) {
                    return;
                }
                break;
            }
        }

        // Defines a ShapeFile data provider
        final ProviderSource sourceShape = new ProviderSource();
        sourceShape.loadAll = true;
        sourceShape.parameters.put(ShapeFileProvider.KEY_FOLDER_PATH, outputDir.getAbsolutePath() +
                "/org/constellation/ws/embedded/wms111/shapefiles");
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
                service.setConfiguration(configShape);
                if (service.getProviders().isEmpty()) {
                    return;
                }
                break;
            }
        }

        // Defines a PostGis data provider
        final ProviderSource sourcePostGis = new ProviderSource();
        sourcePostGis.parameters.put(PostGisProvider.KEY_DATABASE, "cite-wfs");
        sourcePostGis.parameters.put(PostGisProvider.KEY_HOST,     "db.geomatys.com");
        sourcePostGis.parameters.put(PostGisProvider.KEY_SCHEMA,   "public");
        sourcePostGis.parameters.put(PostGisProvider.KEY_USER,     "test");
        sourcePostGis.parameters.put(PostGisProvider.KEY_PASSWD,   "test");
        sourcePostGis.parameters.put(PostGisProvider.KEY_NAMESPACE,"http://cite.opengeospatial.org/gmlsf");

        final ProviderConfig configPostGis = new ProviderConfig();
        configPostGis.sources.add(sourcePostGis);
        sourcePostGis.loadAll = true;

        for (LayerProviderService service : LayerProviderProxy.getInstance().getServices()) {
            // Here we should have the postgis data provider defined previously
            if (service instanceof PostGisProviderService) {
                service.setConfiguration(configPostGis);
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
     * Initialises the data directory in unzipping the jar containing the resources
     * into a temporary directory.
     *
     * @return The root output directory where the data are unzipped.
     * @throws IOException
     */
    private static File initDataDirectory() throws IOException {
        String styleResource = GrizzlyServer.class.getResource("wms111/styles").getFile();
        styleResource = styleResource.substring(0, styleResource.indexOf('!'));
        if (styleResource.startsWith("file:")) {
            styleResource = styleResource.substring(5);
        }
        final File styleJar = new File(styleResource);
        if (styleJar == null || !styleJar.exists()) {
            throw new IOException("Unable to find the style folder: "+ styleJar);
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

    /**
     * Delete the data directory at the end of the process.
     */
    private static void deleteDataDirectory() throws IOException {
        final File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        final File outputDir = new File(tmpDir, "Constellation");
        if (outputDir != null && outputDir.exists()) {
            if (outputDir.canWrite()) {
                if (!NIOUtilities.deleteDirectory(outputDir)) {
                    LOGGER.info("Unable to delete folder "+ outputDir.getAbsolutePath());
                }
            } else {
                LOGGER.info("No write permission for "+ outputDir.getAbsolutePath());
            }
        }
    }

    /**
     * Stop the grizzly server if it is still alive and delete the temporary data directory.
     */
    public static synchronized void finish() throws IOException {
        if (grizzly != null && grizzly.isAlive()) {
            grizzly.interrupt();
        }
        deleteDataDirectory();
    }

    /**
     * Thread that launches a Grizzly server in a separate thread.
     * Requests will be done on this working server.
     */
    private static class GrizzlyThread extends Thread {
        /**
         * Runs a Grizzly server for two hours. Of course this value is far too high,
         * and the process will be killed as soon as the last test suite finishes.
         */
        @Override
        public void run() {
            final CstlEmbeddedService cstlServer = new CstlEmbeddedService(new String[]{}, new String[] {
                "org.constellation.map.ws.rs",
                "org.constellation.coverage.ws.rs",
                "org.constellation.sos.ws.rs",
                "org.constellation.metadata.ws.rs",
                "org.constellation.wfs.ws.rs",
                "org.constellation.ws.rs.provider"
            });
            cstlServer.duration = 2*60*60*1000;
            cstlServer.runREST();
        }
    }
}
