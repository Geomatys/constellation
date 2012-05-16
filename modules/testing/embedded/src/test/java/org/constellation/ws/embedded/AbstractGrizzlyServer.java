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

// JAI dependencies
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.ImageIO;

// J2SE dependencies
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

// Constellation dependencies
import org.constellation.data.CoverageSQLTestCase;
import org.constellation.map.ws.WMSMapDecoration;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.configuration.Configurator;
import org.constellation.provider.shapefile.ShapeFileProviderService;
import org.constellation.util.Util;
import org.constellation.sos.ws.soap.SOService;
import org.constellation.wps.ws.soap.WPSService;
import org.constellation.ws.CstlServiceException;

import static org.constellation.provider.coveragesql.CoverageSQLProviderService.*;
import static org.constellation.provider.configuration.ProviderParameters.*;

// Geotoolkit dependencies
import org.geotoolkit.data.postgis.PostgisNGDataStoreFactory;
import org.geotoolkit.data.om.OMDataStoreFactory;
import org.geotoolkit.image.jai.Registry;
import org.geotoolkit.image.io.plugin.WorldFileImageReader;
import org.geotoolkit.internal.io.IOUtilities;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.util.logging.Logging;
import org.geotoolkit.util.sql.DerbySqlScriptRunner;

import static org.geotoolkit.data.postgis.PostgisNGDataStoreFactory.*;

// JUnit dependencies
import org.junit.*;

// GeoAPI dependencies
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

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
     * The grizzly server that will received some HTTP requests.
     */
    protected static GrizzlyThread grizzly = null;

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
            public ParameterValueGroup getConfiguration(String serviceName, ParameterDescriptorGroup desc) {

                final ParameterValueGroup config = desc.createValue();
                
                if("coverage-sql".equals(serviceName)){
                    // Defines a PostGrid data provider
                    final ParameterValueGroup source = config.addGroup(SOURCE_DESCRIPTOR_NAME);
                    final ParameterValueGroup srcconfig = getOrCreate(COVERAGESQL_DESCRIPTOR,source);
                    srcconfig.parameter(URL_DESCRIPTOR.getName().getCode()).setValue("jdbc:postgresql://db.geomatys.com/coverages-test");
                    srcconfig.parameter(PASSWORD_DESCRIPTOR.getName().getCode()).setValue("test");
                    final String rootDir = System.getProperty("java.io.tmpdir") + "/Constellation/images";
                    srcconfig.parameter(ROOT_DIRECTORY_DESCRIPTOR.getName().getCode()).setValue(rootDir);
                    srcconfig.parameter(USER_DESCRIPTOR.getName().getCode()).setValue("test");
                    srcconfig.parameter(SCHEMA_DESCRIPTOR.getName().getCode()).setValue("coverages");
                    srcconfig.parameter(NAMESPACE_DESCRIPTOR.getName().getCode()).setValue("no namespace");
                    source.parameter(SOURCE_LOADALL_DESCRIPTOR.getName().getCode()).setValue(Boolean.TRUE);
                    source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("coverageTestSrc");

                }else if("observation".equals(serviceName)){
                    try{
                        final String url = "jdbc:derby:memory:TestWFSWorker";
                        final DefaultDataSource ds = new DefaultDataSource(url + ";create=true");
                        if (!datasourceCreated) {
                            Connection con = ds.getConnection();
                            DerbySqlScriptRunner sr = new DerbySqlScriptRunner(con);
                            sr.run(Util.getResourceAsStream("org/constellation/observation/structure_observations.sql"));
                            sr.run(Util.getResourceAsStream("org/constellation/sql/sos-data.sql"));
                            con.close();
                            datasourceCreated = true;
                        }
                        ds.shutdown();

                        final ParameterValueGroup source = config.addGroup(SOURCE_DESCRIPTOR_NAME);
                        final ParameterValueGroup srcconfig = getOrCreate(OMDataStoreFactory.PARAMETERS_DESCRIPTOR,source);
                        srcconfig.parameter(OMDataStoreFactory.SGBDTYPE.getName().getCode()).setValue("derby");
                        srcconfig.parameter(OMDataStoreFactory.DERBYURL.getName().getCode()).setValue(url);
                        source.parameter(SOURCE_LOADALL_DESCRIPTOR.getName().getCode()).setValue(Boolean.TRUE);
                        source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("omSrc");
                    }catch(Exception ex){
                        throw new RuntimeException(ex.getLocalizedMessage(),ex);
                    }
                }else if("shapefile".equals(serviceName)){
                    try{
                        final File outputDir = initDataDirectory();
                        
                        final ParameterValueGroup source = config.addGroup(SOURCE_DESCRIPTOR_NAME);
                        final ParameterValueGroup srcconfig = getOrCreate(ShapeFileProviderService.SOURCE_CONFIG_DESCRIPTOR,source);
                        source.parameter(SOURCE_LOADALL_DESCRIPTOR.getName().getCode()).setValue(Boolean.TRUE);
                        source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("shapeSrc");
                        srcconfig.parameter(ShapeFileProviderService.FOLDER_DESCRIPTOR.getName().getCode())
                                .setValue(outputDir.getAbsolutePath() + "/org/constellation/ws/embedded/wms111/shapefiles");
                        srcconfig.parameter(ShapeFileProviderService.NAMESPACE_DESCRIPTOR.getName().getCode())
                                .setValue("http://www.opengis.net/gml");
                        
                        ParameterValueGroup layer = source.addGroup(LAYER_DESCRIPTOR.getName().getCode());
                        layer.parameter(LAYER_NAME_DESCRIPTOR.getName().getCode()).setValue("NamedPlaces");
                        layer.parameter(LAYER_STYLE_DESCRIPTOR.getName().getCode()).setValue("cite_style_NamedPlaces");

                    }catch(Exception ex){
                        throw new RuntimeException(ex.getLocalizedMessage(),ex);
                    }
                }else if("postgis".equals(serviceName)){
                    // Defines a PostGis data provider
                    final ParameterValueGroup source = config.addGroup(SOURCE_DESCRIPTOR_NAME);
                    final ParameterValueGroup srcconfig = getOrCreate(PostgisNGDataStoreFactory.PARAMETERS_DESCRIPTOR,source);
                    
                    srcconfig.parameter(HOST.getName().getCode()).setValue("db.geomatys.com");
                    srcconfig.parameter(PORT.getName().getCode()).setValue(5432);
                    srcconfig.parameter(DATABASE.getName().getCode()).setValue("cite-wfs");
                    srcconfig.parameter(SCHEMA.getName().getCode()).setValue("public");
                    srcconfig.parameter(USER.getName().getCode()).setValue("test");
                    srcconfig.parameter(PASSWD.getName().getCode()).setValue("test");                    
                    srcconfig.parameter(NAMESPACE_DESCRIPTOR.getName().getCode()).setValue("no namespace");
                    
                    source.parameter(SOURCE_LOADALL_DESCRIPTOR.getName().getCode()).setValue(Boolean.TRUE);
                    source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("postgisSrc");
                    
                    //add a custom sql query layer                    
                    ParameterValueGroup layer = source.addGroup(LAYER_DESCRIPTOR.getName().getCode());
                    layer.parameter(LAYER_NAME_DESCRIPTOR.getName().getCode()).setValue("CustomSQLQuery");
                    layer.parameter(LAYER_QUERY_LANGUAGE.getName().getCode()).setValue("CUSTOM-SQL");
                    layer.parameter(LAYER_QUERY_STATEMENT.getName().getCode()).setValue(
                            "SELECT name as nom, \"pointProperty\" as geom FROM \"PrimitiveGeoFeature\" ");
                }

                //empty configuration for others
                return config;
            }

            @Override
            public void saveConfiguration(String serviceName, ParameterValueGroup params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };

        LayerProviderProxy.getInstance().setConfigurator(config);


        WorldFileImageReader.Spi.registerDefaults(null);
        WMSMapDecoration.setEmptyExtension(true);
        
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

    }

    /**
     * Stop the grizzly server, if it is still alive.
     */
    @AfterClass
    public static void finish() {
        if (grizzly.isAlive()) {
            grizzly.interrupt();
        }
        File f = new File("derby.log");
        if (f.exists()) {
            f.delete();
        }
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
            try {
                cstlServer.serviceInstanceSOAP.put("wps", new WPSService());
                cstlServer.serviceInstanceSOAP.put("sos", new SOService());
            } catch (CstlServiceException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
            cstlServer.runAll();
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
