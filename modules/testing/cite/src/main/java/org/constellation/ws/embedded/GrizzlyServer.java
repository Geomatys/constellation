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
import org.constellation.configuration.DataSourceType;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.ProcessContext;
import org.constellation.configuration.ProcessFactory;
import org.constellation.configuration.Processes;
import org.constellation.configuration.SOSConfiguration;
import org.constellation.configuration.WMSPortrayal;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.provider.DataProviders;
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
import java.sql.Connection;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.constellation.api.ProviderType;
import org.constellation.business.IDataBusiness;
import org.constellation.business.ILayerBusiness;
import org.constellation.business.IProviderBusiness;
import org.constellation.business.IServiceBusiness;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.map.featureinfo.FeatureInfoUtilities;
import org.constellation.provider.ProviderFactory;

import static org.constellation.provider.configuration.ProviderParameters.*;
import static org.constellation.provider.coveragesql.CoverageSQLProviderService.*;
import static org.constellation.provider.featurestore.FeatureStoreProviderService.SOURCE_CONFIG_DESCRIPTOR;
import org.constellation.test.utils.TestDatabaseHandler;
import static org.geotoolkit.data.AbstractFeatureStoreFactory.NAMESPACE;
import static org.geotoolkit.db.AbstractJDBCFeatureStoreFactory.DATABASE;
import static org.geotoolkit.db.AbstractJDBCFeatureStoreFactory.HOST;
import static org.geotoolkit.db.AbstractJDBCFeatureStoreFactory.PASSWORD;
import static org.geotoolkit.db.AbstractJDBCFeatureStoreFactory.SCHEMA;
import static org.geotoolkit.db.AbstractJDBCFeatureStoreFactory.USER;
import org.geotoolkit.internal.sql.DefaultDataSource;
import static org.geotoolkit.utility.parameter.ParametersExt.*;
import org.geotoolkit.util.sql.DerbySqlScriptRunner;

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

    @Inject
    private IServiceBusiness serviceBusiness;
    
    @Inject
    protected IProviderBusiness providerBusiness;
    
    @Inject
    protected IDataBusiness dataBusiness;
    
    @Inject
    protected ILayerBusiness layerBusiness;
    
    /**
     * Prevents instanciation.
     */
    public GrizzlyServer() {
    }

    /**
     * Initialize the Grizzly server, on which WCS and WMS requests will be sent,
     * and defines a PostGrid data provider.
     */
    public synchronized void initServer() throws Exception {
        // Protective test in order not to launch a new instance of the grizzly server for
        // each sub classes.
        if (grizzly != null) {
            return;
        }
        
        final File outputDir = initDataDirectory();
        WorldFileImageReader.Spi.registerDefaults(null);
        WMSPortrayal.setEmptyExtension(true);

        // setup configuration database
        TestDatabaseHandler.hasLocalDatabase();
        final File configDir = ConfigDirectory.setupTestEnvironement("CITE_CONFIGURATION");
        
        //SHAPEFILE
        final ProviderFactory featfactory = DataProviders.getInstance().getFactory("feature-store");
        final ParameterValueGroup sourcef = featfactory.getProviderDescriptor().createValue();
        getOrCreateValue(sourcef, "id").setValue("shapeSrc");
        getOrCreateValue(sourcef, "load_all").setValue(true);

        final ParameterValueGroup choice2 = getOrCreateGroup(sourcef, "choice");
        final ParameterValueGroup shpconfig = getOrCreateGroup(choice2, "ShapefileParametersFolder");
        getOrCreateValue(shpconfig, "url").setValue(new URL("file:" + outputDir.getAbsolutePath() + "/org/constellation/ws/embedded/wms111/shapefiles"));
        getOrCreateValue(shpconfig, "namespace").setValue("cite");

        final ParameterValueGroup layer2 = getOrCreateGroup(sourcef, "Layer");
        getOrCreateValue(layer2, "name").setValue("NamedPlaces");
        getOrCreateValue(layer2, "style").setValue("cite_style_NamedPlaces");

        providerBusiness.storeProvider("shapeSrc", null, ProviderType.LAYER, "feature-store", sourcef);

        dataBusiness.create(new QName("cite", "BuildingCenters"), "shapeSrc", "VECTOR", false, true, null, null);
        dataBusiness.create(new QName("cite", "BasicPolygons"),   "shapeSrc", "VECTOR", false, true, null, null);
        dataBusiness.create(new QName("cite", "Bridges"),         "shapeSrc", "VECTOR", false, true, null, null);
        dataBusiness.create(new QName("cite", "Streams"),         "shapeSrc", "VECTOR", false, true, null, null);
        dataBusiness.create(new QName("cite", "Lakes"),           "shapeSrc", "VECTOR", false, true, null, null);
        dataBusiness.create(new QName("cite", "NamedPlaces"),     "shapeSrc", "VECTOR", false, true, null, null);
        dataBusiness.create(new QName("cite", "Buildings"),       "shapeSrc", "VECTOR", false, true, null, null);
        dataBusiness.create(new QName("cite", "RoadSegments"),    "shapeSrc", "VECTOR", false, true, null, null);
        dataBusiness.create(new QName("cite", "DividedRoutes"),   "shapeSrc", "VECTOR", false, true, null, null);
        dataBusiness.create(new QName("cite", "Forests"),         "shapeSrc", "VECTOR", false, true, null, null);
        dataBusiness.create(new QName("cite", "MapNeatline"),     "shapeSrc", "VECTOR", false, true, null, null);
        dataBusiness.create(new QName("cite", "Ponds"),           "shapeSrc", "VECTOR", false, true, null, null);

        final ProviderFactory factory = DataProviders.getInstance().getFactory("feature-store");

        // Defines a GML data provider
        ParameterValueGroup source = factory.getProviderDescriptor().createValue();
        source.parameter(SOURCE_LOADALL_DESCRIPTOR.getName().getCode()).setValue(Boolean.TRUE);
        source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("primGMLSrc");

        ParameterValueGroup choice = getOrCreate(SOURCE_CONFIG_DESCRIPTOR,source);
        ParameterValueGroup pgconfig = getOrCreateGroup(choice, "GMLParameters");
        pgconfig.parameter("identifier").setValue("gml");
        pgconfig.parameter("url").setValue(new URL("file:"+outputDir.getAbsolutePath() + "/org/constellation/ws/embedded/wfs110/primitive"));
        pgconfig.parameter("sparse").setValue(Boolean.TRUE);
        pgconfig.parameter("xsd").setValue("file:"+outputDir.getAbsolutePath() + "/org/constellation/ws/embedded/wfs110/cite-gmlsf0.xsd");
        pgconfig.parameter("xsdtypename").setValue("PrimitiveGeoFeature");
        pgconfig.parameter("longitudeFirst").setValue(Boolean.TRUE);
        pgconfig.parameter("namespace").setValue("http://cite.opengeospatial.org/gmlsf");

        providerBusiness.storeProvider("primGMLSrc", null, ProviderType.LAYER, "feature-store", source);
        dataBusiness.create(new QName("http://cite.opengeospatial.org/gmlsf", "PrimitiveGeoFeature"), "primGMLSrc", "VECTOR", false, true, null, null);


        source = factory.getProviderDescriptor().createValue();
        source.parameter(SOURCE_LOADALL_DESCRIPTOR.getName().getCode()).setValue(Boolean.TRUE);
        source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("entGMLSrc");

        choice = getOrCreate(SOURCE_CONFIG_DESCRIPTOR,source);
        pgconfig = getOrCreateGroup(choice, "GMLParameters");
        pgconfig.parameter("identifier").setValue("gml");
        pgconfig.parameter("url").setValue(new URL("file:"+outputDir.getAbsolutePath() + "/org/constellation/ws/embedded/wfs110/entity"));
        pgconfig.parameter("sparse").setValue(Boolean.TRUE);
        pgconfig.parameter("xsd").setValue("file:"+outputDir.getAbsolutePath() + "/org/constellation/ws/embedded/wfs110/cite-gmlsf0.xsd");
        pgconfig.parameter("xsdtypename").setValue("EntitéGénérique");
        pgconfig.parameter("longitudeFirst").setValue(Boolean.TRUE);
        pgconfig.parameter("namespace").setValue("http://cite.opengeospatial.org/gmlsf");
        providerBusiness.storeProvider("entGMLSrc", null, ProviderType.LAYER, "feature-store", source);
        dataBusiness.create(new QName("http://cite.opengeospatial.org/gmlsf", "EntitéGénérique"),     "entGMLSrc", "VECTOR", false, true, null, null);


        source = factory.getProviderDescriptor().createValue();
        source.parameter(SOURCE_LOADALL_DESCRIPTOR.getName().getCode()).setValue(Boolean.TRUE);
        source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("aggGMLSrc");

        choice = getOrCreate(SOURCE_CONFIG_DESCRIPTOR,source);
        pgconfig = getOrCreateGroup(choice, "GMLParameters");
        pgconfig.parameter("identifier").setValue("gml");
        pgconfig.parameter("url").setValue(new URL("file:"+outputDir.getAbsolutePath() + "/org/constellation/ws/embedded/wfs110/aggregate"));
        pgconfig.parameter("sparse").setValue(Boolean.TRUE);
        pgconfig.parameter("xsd").setValue("file:"+outputDir.getAbsolutePath() + "/org/constellation/ws/embedded/wfs110/cite-gmlsf0.xsd");
        pgconfig.parameter("xsdtypename").setValue("AggregateGeoFeature");
        pgconfig.parameter("longitudeFirst").setValue(Boolean.TRUE);
        pgconfig.parameter("namespace").setValue("http://cite.opengeospatial.org/gmlsf");
        providerBusiness.storeProvider("aggGMLSrc", null, ProviderType.LAYER, "feature-store", source);
        dataBusiness.create(new QName("http://cite.opengeospatial.org/gmlsf", "AggregateGeoFeature"), "aggGMLSrc", "VECTOR", false, true, null, null);
          
        
        final ProviderFactory covFilefactory = DataProviders.getInstance().getFactory("coverage-store");
        final ParameterValueGroup sourceCF = covFilefactory.getProviderDescriptor().createValue();
        getOrCreateValue(sourceCF, "id").setValue("postgridSrc");
        getOrCreateValue(sourceCF, "load_all").setValue(true);
        final ParameterValueGroup choice3 = getOrCreateGroup(sourceCF, "choice");

        final ParameterValueGroup srcCFConfig = getOrCreateGroup(choice3, "FileCoverageStoreParameters");

        getOrCreateValue(srcCFConfig, "path").setValue(new URL("file:" + outputDir.getAbsolutePath() + "/org/constellation/data/SSTMDE200305.png"));
        getOrCreateValue(srcCFConfig, "type").setValue("AUTO");
        getOrCreateValue(srcCFConfig, NAMESPACE_DESCRIPTOR.getName().getCode()).setValue("no namespace");

        providerBusiness.storeProvider("postgridSrc", null, ProviderType.LAYER, "coverage-store", sourceCF);

        dataBusiness.create(new QName("SSTMDE200305"), "postgridSrc", "COVERAGE", false, true, null, null);
                
        
        DataProviders.getInstance().reload();

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
        
        serviceBusiness.create("csw", "default", config, null);

        /*---------------------------------------------------------------*/
        /*------------------------- SOS ---------------------------------*/
        /*---------------------------------------------------------------*/

        final File sensorDirectory = new File(configDir, "dataSOS");
        sensorDirectory.mkdir();

        writeDataFile(sensorDirectory, "urn-ogc-object-sensor-GEOM-2");
        writeDataFile(sensorDirectory, "urn-ogc-object-sensor-GEOM-3");
        writeDataFile(sensorDirectory, "urn-ogc-object-sensor-GEOM-4");
        writeDataFile(sensorDirectory, "urn-ogc-object-sensor-GEOM-5");
        writeDataFile(sensorDirectory, "urn-ogc-object-sensor-GEOM-7");
        writeDataFile(sensorDirectory, "urn-ogc-object-sensor-GEOM-8");
        writeDataFile(sensorDirectory, "urn-ogc-object-sensor-GEOM-9");

        final Automatic smlConfig = new Automatic(null, sensorDirectory.getPath());
        
        String sosurl = "jdbc:derby:memory:CITEOM2Test2;create=true";
        DefaultDataSource ds = new DefaultDataSource(sosurl);

        Connection con = ds.getConnection();

        DerbySqlScriptRunner sr = new DerbySqlScriptRunner(con);
        sr.setEncoding("UTF-8");
        sr.run(Util.getResourceAsStream("org/constellation/om2/structure_observations.sql"));
        sr.run(Util.getResourceAsStream("org/constellation/sql/sos-data-om2-cite.sql"));
        
        Automatic OMConfiguration  = new Automatic();
        BDD bdd = new BDD("org.apache.derby.jdbc.EmbeddedDriver", sosurl, "", "");
        OMConfiguration.setBdd(bdd);
        SOSConfiguration sosconf = new SOSConfiguration(smlConfig, OMConfiguration);
        sosconf.setObservationReaderType(DataSourceType.OM2);
        sosconf.setObservationWriterType(DataSourceType.OM2);
        sosconf.setObservationFilterType(DataSourceType.OM2);
        sosconf.setSMLType(DataSourceType.FILESYSTEM);
        sosconf.setPhenomenonIdBase("urn:ogc:def:phenomenon:GEOM:");
        sosconf.setProfile("transactional");
        sosconf.setObservationTemplateIdBase("urn:ogc:object:observation:template:GEOM:");
        sosconf.setObservationIdBase("urn:ogc:object:observation:GEOM:");
        sosconf.setSensorIdBase("urn:ogc:object:sensor:GEOM:");
        sosconf.getParameters().put("transactionSecurized", "false");
        sosconf.getParameters().put("multipleVersion", "false");
        sosconf.getParameters().put("singleVersion", "1.0.0");

        serviceBusiness.create("sos", "default", sosconf, null);

        /*---------------------------------------------------------------*/
        /*------------------------- WCS ---------------------------------*/
        /*---------------------------------------------------------------*/

        final LayerContext wcsConfig = new LayerContext();
        wcsConfig.getCustomParameters().put("shiroAccessible", "false");

        serviceBusiness.create("wcs", "default", wcsConfig, null);
        
        layerBusiness.add("SSTMDE200305", null, "postgridSrc", null, "default", "wcs", null);
        
        /*---------------------------------------------------------------*/
        /*------------------------- WFS ---------------------------------*/
        /*---------------------------------------------------------------*/

        final LayerContext wfsConfig = new LayerContext();
        wfsConfig.getCustomParameters().put("shiroAccessible", "false");
        wfsConfig.getCustomParameters().put("multipleVersion", "false");
        wfsConfig.getCustomParameters().put("transactionSecurized", "false");
        wfsConfig.getCustomParameters().put("transactionnal", "true");
        
        //wfsConfig.getCustomParameters().put("requestValidationActivated", "true");
        //wfsConfig.getCustomParameters().put("requestValidationSchema", "http://schemas.opengis.net/wfs/1.1.0/wfs.xsd");
        
        serviceBusiness.create("wfs", "default", wfsConfig, null);
        
        layerBusiness.add("AggregateGeoFeature", "http://cite.opengeospatial.org/gmlsf", "aggGMLSrc", null, "default", "wfs", null);
        layerBusiness.add("PrimitiveGeoFeature", "http://cite.opengeospatial.org/gmlsf", "primGMLSrc", null, "default", "wfs", null);
        layerBusiness.add("EntitéGénérique",     "http://cite.opengeospatial.org/gmlsf", "entGMLSrc", null, "default", "wfs", null);
        

        /*---------------------------------------------------------------*/
        /*------------------------- WMS ---------------------------------*/
        /*---------------------------------------------------------------*/

        final LayerContext wmsConfig = new LayerContext();
        wmsConfig.getCustomParameters().put("shiroAccessible", "false");
        wmsConfig.setGetFeatureInfoCfgs(FeatureInfoUtilities.createGenericConfiguration());
        serviceBusiness.create("wms", "default", wmsConfig, null);
        
        layerBusiness.add("BuildingCenters",     "cite",           "shapeSrc",   null, "default", "wms", null);
        layerBusiness.add("BasicPolygons",       "cite",           "shapeSrc",   null, "default", "wms", null);
        layerBusiness.add("Bridges",             "cite",           "shapeSrc",   null, "default", "wms", null);
        layerBusiness.add("Streams",             "cite",           "shapeSrc",   null, "default", "wms", null);
        layerBusiness.add("Lakes",               "cite",           "shapeSrc",   null, "default", "wms", null);
        layerBusiness.add("NamedPlaces",         "cite",           "shapeSrc",   null, "default", "wms", null);
        layerBusiness.add("Buildings",           "cite",           "shapeSrc",   null, "default", "wms", null);
        layerBusiness.add("RoadSegments",        "cite",           "shapeSrc",   null, "default", "wms", null);
        layerBusiness.add("DividedRoutes",       "cite",           "shapeSrc",   null, "default", "wms", null);
        layerBusiness.add("Forests",             "cite",           "shapeSrc",   null, "default", "wms", null);
        layerBusiness.add("MapNeatline",         "cite",           "shapeSrc",   null, "default", "wms", null);
        layerBusiness.add("Ponds",               "cite",           "shapeSrc",   null, "default", "wms", null);

        /*---------------------------------------------------------------*/
        /*------------------------- WPS ---------------------------------*/
        /*---------------------------------------------------------------*/

        final Processes processes = new Processes(Arrays.asList(new ProcessFactory("jts", true)));
        final ProcessContext wpsConfig = new ProcessContext(processes);

        serviceBusiness.create("wps", "default", wpsConfig, null);

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
        ConfigDirectory.shutdownTestEnvironement("CITE_CONFIGURATION");
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
