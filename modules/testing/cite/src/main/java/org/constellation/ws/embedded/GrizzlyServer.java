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

// J2SE dependencies

import org.apache.sis.util.logging.Logging;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.configuration.DataSourceType;
import org.constellation.configuration.Layer;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.Layers;
import org.constellation.configuration.ProcessContext;
import org.constellation.configuration.ProcessFactory;
import org.constellation.configuration.Processes;
import org.constellation.configuration.SOSConfiguration;
import org.constellation.configuration.Source;
import org.constellation.configuration.WMSPortrayal;
import org.constellation.data.CoverageSQLTestCase;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.provider.DataProviders;
import org.constellation.provider.Provider;
import org.constellation.provider.ProviderService;
import org.constellation.provider.StyleProviders;
import org.constellation.provider.configuration.Configurator;
import org.constellation.provider.sld.SLDProviderService;
import org.constellation.util.DataReference;
import org.constellation.util.Util;
import org.geotoolkit.image.io.plugin.WorldFileImageReader;
import org.geotoolkit.image.jai.Registry;
import org.geotoolkit.internal.io.IOUtilities;
import org.geotoolkit.util.FileUtilities;
import org.opengis.parameter.ParameterValueGroup;

import javax.imageio.ImageIO;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.constellation.provider.configuration.ProviderParameters.*;
import static org.constellation.provider.coveragesql.CoverageSQLProviderService.*;
import static org.geotoolkit.parameter.ParametersExt.*;

// Constellation dependencies


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
    public static synchronized void initServer() throws Exception {
        // Protective test in order not to launch a new instance of the grizzly server for
        // each sub classes.
        if (grizzly != null) {
            return;
        }

        // Initialises the postgrid testing raster.
        CoverageSQLTestCase.init();

        WorldFileImageReader.Spi.registerDefaults(null);
        WMSPortrayal.setEmptyExtension(true);

        // setup configuration database

        final File configDir = ConfigurationEngine.setupTestEnvironement("CITE_CONFIGURATION");

        /*---------------------------------------------------------------*/
        /*------------------------- CSW ---------------------------------*/
        /*---------------------------------------------------------------*/
        
        final File dataDirectory = new File(configDir, "dataCSW");
        dataDirectory.mkdir();

        writeDataFile(dataDirectory, "urn-uuid-19887a8a-f6b0-4a63-ae56-7fba0e17801f");
        writeDataFile(dataDirectory, "urn-uuid-1ef30a8b-876d-4828-9246-c37ab4510bbd");
        writeDataFile(dataDirectory, "urn-uuid-66ae76b7-54ba-489b-a582-0f0633d96493");
        writeDataFile(dataDirectory, "urn-uuid-6a3de50b-fa66-4b58-a0e6-ca146fdd18d4");
        writeDataFile(dataDirectory, "urn-uuid-784e2afd-a9fd-44a6-9a92-a3848371c8ec");
        writeDataFile(dataDirectory, "urn-uuid-829babb0-b2f1-49e1-8cd5-7b489fe71a1e");
        writeDataFile(dataDirectory, "urn-uuid-88247b56-4cbc-4df9-9860-db3f8042e357");
        writeDataFile(dataDirectory, "urn-uuid-94bc9c83-97f6-4b40-9eb8-a8e8787a5c63");
        writeDataFile(dataDirectory, "urn-uuid-9a669547-b69b-469f-a11f-2d875366bbdc");
        writeDataFile(dataDirectory, "urn-uuid-a06af396-3105-442d-8b40-22b57a90d2f2");
        writeDataFile(dataDirectory, "urn-uuid-ab42a8c4-95e8-4630-bf79-33e59241605a");
        writeDataFile(dataDirectory, "urn-uuid-e9330592-0932-474b-be34-c3a3bb67c7db");

        final Automatic config = new Automatic("filesystem", dataDirectory.getPath());
        config.putParameter("transactionSecurized", "false");
        config.putParameter("shiroAccessible", "false");
        ConfigurationEngine.storeConfiguration("CSW", "default", config);

        /*---------------------------------------------------------------*/
        /*------------------------- SOS ---------------------------------*/
        /*---------------------------------------------------------------*/

        final File sensorDirectory = new File(configDir, "dataSOS");
        sensorDirectory.mkdir();

        writeDataFile(sensorDirectory, "urn-ogc-object-sensor-SunSpot-0014.4F01.0000.261A");
        writeDataFile(sensorDirectory, "urn-ogc-object-sensor-SunSpot-0014.4F01.0000.2626");
        writeDataFile(sensorDirectory, "urn-ogc-object-sensor-SunSpot-2");

        final Automatic smlConfig = new Automatic(null, sensorDirectory.getPath());
        final Automatic omCOnfig = new Automatic(null, new BDD("org.postgresql.Driver", "jdbc:postgresql://flupke.geomatys.com:5432/observation", "test", "test"));
        final SOSConfiguration sosconf = new SOSConfiguration(smlConfig, omCOnfig);
        sosconf.setObservationFilterType(DataSourceType.POSTGRID);
        sosconf.setObservationReaderType(DataSourceType.POSTGRID);
        sosconf.setObservationWriterType(DataSourceType.POSTGRID);
        sosconf.setSMLType(DataSourceType.FILESYSTEM);
        sosconf.setProfile("transactional");
        sosconf.setObservationIdBase("urn:ogc:object:observation:SunSpot:");
        sosconf.setSensorIdBase("urn:ogc:object:sensor:SunSpot:");
        sosconf.setPhenomenonIdBase("urn:phenomenon:");
        sosconf.setObservationTemplateIdBase("urn:ogc:object:observationTemplate:SunSpot:");
        sosconf.setVerifySynchronization(false);
        sosconf.getParameters().put("transactionSecurized", "false");
        sosconf.getParameters().put("multipleVersion", "false");
        sosconf.getParameters().put("singleVersion", "1.0.0");

        ConfigurationEngine.storeConfiguration("SOS", "default", sosconf);

        /*---------------------------------------------------------------*/
        /*------------------------- WCS ---------------------------------*/
        /*---------------------------------------------------------------*/

        final List<Source> sources = Arrays.asList(new Source("postgisSrc", true, null, null),
                                                   new Source("postgridSrc", true, null, null));
        final Layers layers = new Layers(sources);
        final LayerContext wcsConfig = new LayerContext(layers);
        wcsConfig.getCustomParameters().put("shiroAccessible", "false");

        ConfigurationEngine.storeConfiguration("WCS", "default", wcsConfig);

        /*---------------------------------------------------------------*/
        /*------------------------- WFS ---------------------------------*/
        /*---------------------------------------------------------------*/

        final List<Source> sourcesWFS = Arrays.asList(new Source("postgisSrc", true, null, null));
        final Layers layersWFS = new Layers(sourcesWFS);
        final LayerContext wfsConfig = new LayerContext(layersWFS);
        wfsConfig.getCustomParameters().put("shiroAccessible", "false");
        wfsConfig.getCustomParameters().put("multipleVersion", "false");
        wfsConfig.getCustomParameters().put("transactionSecurized", "false");
        wfsConfig.getCustomParameters().put("transactionnal", "true");

        //wfsConfig.getCustomParameters().put("requestValidationActivated", "true");
        //wfsConfig.getCustomParameters().put("requestValidationSchema", "http://schemas.opengis.net/wfs/1.1.0/wfs.xsd");

        ConfigurationEngine.storeConfiguration("WFS", "default", wfsConfig);

        /*---------------------------------------------------------------*/
        /*------------------------- WMS ---------------------------------*/
        /*---------------------------------------------------------------*/
        final List<Layer> includes = new ArrayList<>();
        includes.add(new Layer(new QName("cite", "BasicPolygons"),   Arrays.asList(new DataReference("${providerStyleType|sldSrc|cite_style_BasicPolygons}"))));
        includes.add(new Layer(new QName("cite", "Bridges"),         Arrays.asList(new DataReference("${providerStyleType|sldSrc|cite_style_Bridges}"))));
        includes.add(new Layer(new QName("cite", "BuildingCenters"), Arrays.asList(new DataReference("${providerStyleType|sldSrc|cite_style_BuildingCenters}"))));
        includes.add(new Layer(new QName("cite", "Buildings"),       Arrays.asList(new DataReference("${providerStyleType|sldSrc|cite_style_Buildings}"))));
        includes.add(new Layer(new QName("cite", "DividedRoutes"),   Arrays.asList(new DataReference("${providerStyleType|sldSrc|cite_style_DividedRoutes}"))));
        includes.add(new Layer(new QName("cite", "Forests"),         Arrays.asList(new DataReference("${providerStyleType|sldSrc|cite_style_Forests}"))));
        includes.add(new Layer(new QName("cite", "Lakes"),           Arrays.asList(new DataReference("${providerStyleType|sldSrc|cite_style_Lakes}"))));
        includes.add(new Layer(new QName("cite", "MapNeatline"),     Arrays.asList(new DataReference("${providerStyleType|sldSrc|cite_style_MapNeatLine}"))));
        includes.add(new Layer(new QName("cite", "NamedPlaces"),     Arrays.asList(new DataReference("${providerStyleType|sldSrc|cite_style_NamedPlaces}"))));
        includes.add(new Layer(new QName("cite", "Ponds"),           Arrays.asList(new DataReference("${providerStyleType|sldSrc|cite_style_Ponds}"))));
        includes.add(new Layer(new QName("cite", "RoadSegments"),    Arrays.asList(new DataReference("${providerStyleType|sldSrc|cite_style_RoadSegments}"))));
        includes.add(new Layer(new QName("cite", "Streams"),         Arrays.asList(new DataReference("${providerStyleType|sldSrc|cite_style_Streams}"))));

        final Source shapeSrc = new Source("shapeSrc", true, includes, null);
        final List<Source> sourcesWMS = Arrays.asList(shapeSrc);
        final Layers layersWMS = new Layers(sourcesWMS);
        final LayerContext wmsConfig = new LayerContext(layersWMS);
        wmsConfig.getCustomParameters().put("shiroAccessible", "false");

        ConfigurationEngine.storeConfiguration("WMS", "default", wmsConfig);

        /*---------------------------------------------------------------*/
        /*------------------------- WPS ---------------------------------*/
        /*---------------------------------------------------------------*/

        final Processes processes = new Processes(Arrays.asList(new ProcessFactory("jts", true)));
        final ProcessContext wpsConfig = new ProcessContext(processes);

        ConfigurationEngine.storeConfiguration("WPS", "default", wpsConfig);

        // Extracts the zip data into a temporary folder
        final File outputDir = initDataDirectory();

        final Configurator layerConfig = new Configurator() {
            @Override
            public ParameterValueGroup getConfiguration(final ProviderService service) {
                final ParameterValueGroup config = service.getServiceDescriptor().createValue();
                final String serviceName = service.getName();

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

                }else if("feature-store".equals(serviceName)){
                    try{ 
                                                
                        {//SHAPEFILE
                        final File outputDir = initDataDirectory();
                        final ParameterValueGroup source = createGroup(config,SOURCE_DESCRIPTOR_NAME);
                        getOrCreateValue(source, "id").setValue("shapeSrc");
                        getOrCreateValue(source, "load_all").setValue(false);    
                        
                        final ParameterValueGroup choice = getOrCreateGroup(source, "choice");
                        final ParameterValueGroup shpconfig = createGroup(choice, "ShapefileParametersFolder");
                        getOrCreateValue(shpconfig, "url").setValue(new URL("file:"+outputDir.getAbsolutePath() + "/org/constellation/ws/embedded/wms111/shapefiles"));
                        getOrCreateValue(shpconfig, "namespace").setValue("cite");        
                        
                        ParameterValueGroup layer = createGroup(source, "Layer");
                        getOrCreateValue(layer, "name").setValue("BasicPolygons");
                        getOrCreateValue(layer, "style").setValue("cite_style_BasicPolygons");     
                        layer = createGroup(source, "Layer");
                        getOrCreateValue(layer, "name").setValue("Bridges");
                        getOrCreateValue(layer, "style").setValue("cite_style_Bridges");
                        layer = createGroup(source, "Layer");
                        getOrCreateValue(layer, "name").setValue("BuildingCenters");
                        getOrCreateValue(layer, "style").setValue("cite_style_BuildingCenters");
                        layer = createGroup(source, "Layer");
                        getOrCreateValue(layer, "name").setValue("Buildings");
                        getOrCreateValue(layer, "style").setValue("cite_style_Buildings");
                        layer = createGroup(source, "Layer");
                        getOrCreateValue(layer, "name").setValue("DividedRoutes");
                        getOrCreateValue(layer, "style").setValue("cite_style_DividedRoutes");
                        layer = createGroup(source, "Layer");
                        getOrCreateValue(layer, "name").setValue("Forests");
                        getOrCreateValue(layer, "style").setValue("cite_style_Forests");
                        layer = createGroup(source, "Layer");
                        getOrCreateValue(layer, "name").setValue("Lakes");
                        getOrCreateValue(layer, "style").setValue("cite_style_Lakes");
                        layer = createGroup(source, "Layer");
                        getOrCreateValue(layer, "name").setValue("MapNeatline");
                        getOrCreateValue(layer, "style").setValue("cite_style_MapNeatLine");
                        layer = createGroup(source, "Layer");
                        getOrCreateValue(layer, "name").setValue("NamedPlaces");
                        getOrCreateValue(layer, "style").setValue("cite_style_NamedPlaces");
                        layer = createGroup(source, "Layer");
                        getOrCreateValue(layer, "name").setValue("Ponds");
                        getOrCreateValue(layer, "style").setValue("cite_style_Ponds");
                        layer = createGroup(source, "Layer");
                        getOrCreateValue(layer, "name").setValue("RoadSegments");
                        getOrCreateValue(layer, "style").setValue("cite_style_RoadSegments");
                        layer = createGroup(source, "Layer");
                        getOrCreateValue(layer, "name").setValue("Streams");
                        getOrCreateValue(layer, "style").setValue("cite_style_Streams");
                        }
                        
                        {//POSTGIS
                        final ParameterValueGroup source = createGroup(config,SOURCE_DESCRIPTOR_NAME);
                        getOrCreateValue(source, "id").setValue("postgisSrc");
                        getOrCreateValue(source, "load_all").setValue(true);                        
                        
                        final ParameterValueGroup choice = getOrCreateGroup(source, "choice");
                        final ParameterValueGroup pgconfig = createGroup(choice, "PostgresParameters");
                        getOrCreateValue(pgconfig,"host").setValue("flupke.geomatys.com");
                        getOrCreateValue(pgconfig,"port").setValue(5432);
                        getOrCreateValue(pgconfig,"database").setValue("cite-wfs-2");
                        getOrCreateValue(pgconfig,"schema").setValue("public");
                        getOrCreateValue(pgconfig,"user").setValue("test");
                        getOrCreateValue(pgconfig,"password").setValue("test");
                        getOrCreateValue(pgconfig,"namespace").setValue("http://cite.opengeospatial.org/gmlsf");                     
                        }
                                                
                    }catch(Exception ex){
                        throw new RuntimeException(ex.getLocalizedMessage(),ex);
                    }
                }
                

                return config;
            }

            @Override
            public void saveConfiguration(ProviderService service, List<Provider> providers) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        DataProviders.getInstance().setConfigurator(layerConfig);

        final Configurator styleconfig = new Configurator() {
            @Override
            public ParameterValueGroup getConfiguration(final ProviderService service) {
                final ParameterValueGroup config = service.getServiceDescriptor().createValue();
                final String serviceName = service.getName();

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
            public void saveConfiguration(ProviderService service, List<Provider> providers) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        StyleProviders.getInstance().setConfigurator(styleconfig);


        //reset values, only allow pure java readers
        for(String jn : ImageIO.getReaderFormatNames()){
            Registry.setNativeCodecAllowed(jn, ImageReaderSpi.class, false);
        }

        //reset values, only allow pure java writers
        for(String jn : ImageIO.getWriterFormatNames()){
            Registry.setNativeCodecAllowed(jn, ImageWriterSpi.class, false);
        }

        LOGGER.info("Configuration context set");
        
        /* Instanciates the Grizzly server, but not start it at this moment.
         * The implementation waits for the data provider to be defined for
         * starting the server.
         */
        grizzly = new GrizzlyThread();
        
        // Starting the grizzly server
        grizzly.start();

        // Waiting for grizzly server to be completely started
        System.out.println("wait for services start ...");
        try {
            while (!grizzly.isReady()) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException ex) {
            // That case should not occur.
            throw new AssertionError(ex);
        }
        System.out.println("all services should been started ...");
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
        if (!styles.exists()) {
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
        if (outputDir.exists()) {
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

    public static void writeDataFile(File dataDirectory, String resourceName) throws IOException {

        final File dataFile;
        if (System.getProperty("os.name", "").startsWith("Windows")) {
            final String windowsIdentifier = resourceName.replace(':', '-');
            dataFile = new File(dataDirectory, windowsIdentifier + ".xml");
        } else {
            dataFile = new File(dataDirectory, resourceName + ".xml");
        }
        FileWriter fw = new FileWriter(dataFile);
        InputStream in = Util.getResourceAsStream("org/constellation/cite/data/" + resourceName + ".xml");

        byte[] buffer = new byte[1024];
        int size;

        while ((size = in.read(buffer, 0, 1024)) > 0) {
            fw.write(new String(buffer, 0, size));
        }
        in.close();
        fw.close();
    }

    /**
     * Stop the grizzly server if it is still alive and delete the temporary data directory.
     */
    public static synchronized void finish() throws IOException {
        if (grizzly != null && grizzly.isAlive()) {
            grizzly.interrupt();
        }
        deleteDataDirectory();
        ConfigurationEngine.shutdownTestEnvironement("CITE_CONFIGURATION");
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

        public boolean isReady() {
            return cstlServer.ready;
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
