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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.ImageIO;

// Constellation dependencies
import org.constellation.data.CoverageSQLTestCase;
import org.constellation.configuration.WMSPortrayal;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.StyleProviderProxy;
import org.constellation.provider.configuration.Configurator;
import org.constellation.provider.sld.SLDProviderService;
import org.constellation.provider.shapefile.ShapeFileProviderService;

import org.geotoolkit.image.io.plugin.WorldFileImageReader;
import org.geotoolkit.internal.io.IOUtilities;
import org.geotoolkit.util.FileUtilities;
import org.geotoolkit.util.logging.Logging;
import org.geotoolkit.image.jai.Registry;

import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

import static org.constellation.provider.configuration.ProviderParameters.*;
import static org.geotoolkit.data.postgis.PostgisNGDataStoreFactory.*;
import static org.constellation.provider.coveragesql.CoverageSQLProviderService.*;


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
        CoverageSQLTestCase.init();

        WorldFileImageReader.Spi.registerDefaults(null);
        WMSPortrayal.setEmptyExtension(true);

        // Extracts the zip data into a temporary folder
        final File outputDir = initDataDirectory();

        final Configurator layerConfig = new Configurator() {
            @Override
            public ParameterValueGroup getConfiguration(String serviceName, ParameterDescriptorGroup desc) {
                final ParameterValueGroup config = desc.createValue();

                if("coverage-sql".equals(serviceName)){
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
                    source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("postgridSrc");

                }else if("shapefile".equals(serviceName)){
                    // Defines a ShapeFile data provider
                    final ParameterValueGroup source = config.addGroup(SOURCE_DESCRIPTOR_NAME);
                    final ParameterValueGroup srcconfig = getOrCreate(ShapeFileProviderService.SOURCE_CONFIG_DESCRIPTOR,source);
                    source.parameter(SOURCE_LOADALL_DESCRIPTOR.getName().getCode()).setValue(Boolean.FALSE);
                    source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("shapeSrc");
                    srcconfig.parameter(ShapeFileProviderService.FOLDER_DESCRIPTOR.getName().getCode())
                            .setValue(outputDir.getAbsolutePath() + "/org/constellation/ws/embedded/wms111/shapefiles");
                    srcconfig.parameter(ShapeFileProviderService.NAMESPACE_DESCRIPTOR.getName().getCode())
                            .setValue("cite");


                    ParameterValueGroup layer = source.addGroup(LAYER_DESCRIPTOR.getName().getCode());
                    layer.parameter(LAYER_NAME_DESCRIPTOR.getName().getCode()).setValue("BasicPolygons");
                    layer.parameter(LAYER_STYLE_DESCRIPTOR.getName().getCode()).setValue("cite_style_BasicPolygons");

                    layer = source.addGroup(LAYER_DESCRIPTOR.getName().getCode());
                    layer.parameter(LAYER_NAME_DESCRIPTOR.getName().getCode()).setValue("Bridges");
                    layer.parameter(LAYER_STYLE_DESCRIPTOR.getName().getCode()).setValue("cite_style_Bridges");

                    layer = source.addGroup(LAYER_DESCRIPTOR.getName().getCode());
                    layer.parameter(LAYER_NAME_DESCRIPTOR.getName().getCode()).setValue("BuildingCenters");
                    layer.parameter(LAYER_STYLE_DESCRIPTOR.getName().getCode()).setValue("cite_style_BuildingCenters");

                    layer = source.addGroup(LAYER_DESCRIPTOR.getName().getCode());
                    layer.parameter(LAYER_NAME_DESCRIPTOR.getName().getCode()).setValue("Buildings");
                    layer.parameter(LAYER_STYLE_DESCRIPTOR.getName().getCode()).setValue("cite_style_Buildings");

                    layer = source.addGroup(LAYER_DESCRIPTOR.getName().getCode());
                    layer.parameter(LAYER_NAME_DESCRIPTOR.getName().getCode()).setValue("DividedRoutes");
                    layer.parameter(LAYER_STYLE_DESCRIPTOR.getName().getCode()).setValue("cite_style_DividedRoutes");

                    layer = source.addGroup(LAYER_DESCRIPTOR.getName().getCode());
                    layer.parameter(LAYER_NAME_DESCRIPTOR.getName().getCode()).setValue("Forests");
                    layer.parameter(LAYER_STYLE_DESCRIPTOR.getName().getCode()).setValue("cite_style_Forests");

                    layer = source.addGroup(LAYER_DESCRIPTOR.getName().getCode());
                    layer.parameter(LAYER_NAME_DESCRIPTOR.getName().getCode()).setValue("Lakes");
                    layer.parameter(LAYER_STYLE_DESCRIPTOR.getName().getCode()).setValue("cite_style_Lakes");

                    layer = source.addGroup(LAYER_DESCRIPTOR.getName().getCode());
                    layer.parameter(LAYER_NAME_DESCRIPTOR.getName().getCode()).setValue("MapNeatline");
                    layer.parameter(LAYER_STYLE_DESCRIPTOR.getName().getCode()).setValue("cite_style_MapNeatLine");

                    layer = source.addGroup(LAYER_DESCRIPTOR.getName().getCode());
                    layer.parameter(LAYER_NAME_DESCRIPTOR.getName().getCode()).setValue("NamedPlaces");
                    layer.parameter(LAYER_STYLE_DESCRIPTOR.getName().getCode()).setValue("cite_style_NamedPlaces");

                    layer = source.addGroup(LAYER_DESCRIPTOR.getName().getCode());
                    layer.parameter(LAYER_NAME_DESCRIPTOR.getName().getCode()).setValue("Ponds");
                    layer.parameter(LAYER_STYLE_DESCRIPTOR.getName().getCode()).setValue("cite_style_Ponds");

                    layer = source.addGroup(LAYER_DESCRIPTOR.getName().getCode());
                    layer.parameter(LAYER_NAME_DESCRIPTOR.getName().getCode()).setValue("RoadSegments");
                    layer.parameter(LAYER_STYLE_DESCRIPTOR.getName().getCode()).setValue("cite_style_RoadSegments");

                    layer = source.addGroup(LAYER_DESCRIPTOR.getName().getCode());
                    layer.parameter(LAYER_NAME_DESCRIPTOR.getName().getCode()).setValue("Streams");
                    layer.parameter(LAYER_STYLE_DESCRIPTOR.getName().getCode()).setValue("cite_style_Streams");

                }else if("postgis".equals(serviceName)){
                    // Defines a PostGis data provider
                    final ParameterValueGroup source = config.addGroup(SOURCE_DESCRIPTOR_NAME);
                    final ParameterValueGroup srcconfig = getOrCreate(PARAMETERS_DESCRIPTOR,source);
                    srcconfig.parameter(DATABASE.getName().getCode()).setValue("cite-wfs-2");
                    srcconfig.parameter(HOST.getName().getCode()).setValue("flupke.geomatys.com");
                    srcconfig.parameter(SCHEMA.getName().getCode()).setValue("public");
                    srcconfig.parameter(USER.getName().getCode()).setValue("test");
                    srcconfig.parameter(PASSWD.getName().getCode()).setValue("test");
                    srcconfig.parameter(NAMESPACE.getName().getCode()).setValue("http://cite.opengeospatial.org/gmlsf");
                    source.parameter(SOURCE_LOADALL_DESCRIPTOR.getName().getCode()).setValue(Boolean.TRUE);
                    source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("postgisSrc");
                }

                return config;
            }

            @Override
            public void saveConfiguration(String serviceName, ParameterValueGroup params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        LayerProviderProxy.getInstance().setConfigurator(layerConfig);

        final Configurator styleconfig = new Configurator() {
            @Override
            public ParameterValueGroup getConfiguration(final String serviceName, final ParameterDescriptorGroup desc) {
                final ParameterValueGroup config = desc.createValue();

                if("sld".equals(serviceName)){

                    final ParameterValueGroup source = config.addGroup(
                            SLDProviderService.SOURCE_DESCRIPTOR.getName().getCode());
                    source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("sldSrc");
                    final ParameterValueGroup sourceConfig = source.groups(SLDProviderService.SOURCE_CONFIG_DESCRIPTOR.getName().getCode()).get(0);
                    sourceConfig.parameter(SLDProviderService.FOLDER_DESCRIPTOR.getName().getCode()).setValue(
                            outputDir.getAbsolutePath() + "/org/constellation/ws/embedded/wms111/styles");
                }

                return config;
            }

            @Override
            public void saveConfiguration(String serviceName, ParameterValueGroup params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        StyleProviderProxy.getInstance().setConfigurator(styleconfig);


        //reset values, only allow pure java readers
        for(String jn : ImageIO.getReaderFormatNames()){
            Registry.setNativeCodecAllowed(jn, ImageReaderSpi.class, false);
        }

        //reset values, only allow pure java writers
        for(String jn : ImageIO.getWriterFormatNames()){
            Registry.setNativeCodecAllowed(jn, ImageWriterSpi.class, false);
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
        if (styleResource.contains("!")) {
            styleResource = styleResource.substring(0, styleResource.indexOf('!'));
        }
        if (styleResource.startsWith("file:")) {
            styleResource = styleResource.substring(5);
        }
        File styles = new File(styleResource);
        if (styles == null || !styles.exists()) {
            throw new IOException("Unable to find the style folder: "+ styles);
        }
        final File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        File outputDir = new File(tmpDir, "Constellation");
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }
        if (styles.isDirectory()) {
            FileUtilities.copy(styles, outputDir);
        } else {
            final InputStream in = new FileInputStream(styles);
            IOUtilities.unzip(in, outputDir);
            in.close();
        }
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
                if (!FileUtilities.deleteDirectory(outputDir)) {
                    LOGGER.log(Level.INFO, "Unable to delete folder {0}", outputDir.getAbsolutePath());
                }
            } else {
                LOGGER.log(Level.INFO, "No write permission for {0}", outputDir.getAbsolutePath());
            }
        }
        File f = new File("derby.log");
        if (f.exists()) {
            f.delete();
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
        final CstlEmbeddedService cstlServer = new CstlEmbeddedService(9091, new String[]{}, new String[] {
            "org.constellation.map.ws.rs",
            "org.constellation.coverage.ws.rs",
            "org.constellation.sos.ws.rs",
            "org.constellation.metadata.ws.rs",
            "org.constellation.wfs.ws.rs",
            "org.constellation.wps.ws.rs",
            "org.constellation.ws.rs.provider"
        });

        public int getCurrentPort() {
            return cstlServer.currentPort;
        }

        /**
         * Runs a Grizzly server for five minutes.
         */
        @Override
        public void run() {
            cstlServer.duration = 2*60*60*1000;
            cstlServer.findAvailablePort = true;
            cstlServer.runREST();
        }
    }
}
