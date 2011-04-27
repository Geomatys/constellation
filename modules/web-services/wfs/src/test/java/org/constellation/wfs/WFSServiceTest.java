/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2011, Geomatys
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

package org.constellation.wfs;

import org.constellation.configuration.Layers;
import java.util.Arrays;
import org.constellation.configuration.Source;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.internal.io.IOUtilities;
import java.io.IOException;
import org.constellation.provider.LayerProviderProxy;
import org.geotoolkit.data.sml.SMLDataStoreFactory;
import org.constellation.util.Util;
import java.sql.Connection;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.io.InputStream;
import javax.xml.bind.Marshaller;

import javax.ws.rs.core.MultivaluedMap;

import org.constellation.ws.rs.WebService;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.generic.database.GenericDatabaseMarshallerPool;
import org.constellation.wfs.ws.rs.WFSService;
import org.constellation.test.utils.BasicMultiValueMap;
import org.constellation.test.utils.BasicUriInfo;
import org.constellation.provider.shapefile.ShapeFileProviderService;
import org.constellation.provider.configuration.Configurator;
import org.geotoolkit.data.FeatureCollection;

import static org.constellation.provider.configuration.ProviderParameters.*;

import org.geotoolkit.data.FeatureIterator;
import org.geotoolkit.data.om.OMDataStoreFactory;
import org.geotoolkit.util.sql.DerbySqlScriptRunner;
import org.geotoolkit.util.FileUtilities;

import org.opengis.feature.Feature;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.parameter.ParameterDescriptorGroup;

// JUnit dependencies
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class WFSServiceTest {

    private static WFSService service;

    private static File configDirectory;

    private static DefaultDataSource ds = null;

    private static DefaultDataSource ds2 = null;

    private static final BasicUriInfo info = new BasicUriInfo(null, null);

    private static final MultivaluedMap<String,String> queryParameters = new BasicMultiValueMap<String, String>();
    private static final MultivaluedMap<String,String> pathParameters = new BasicMultiValueMap<String, String>();

    @BeforeClass
    public static void setUpClass() throws Exception {
        initFeatureSource();
        configDirectory = new File("WFSServiceTest");

        if (configDirectory.exists()) {
            FileUtilities.deleteDirectory(configDirectory);
        }
        
        configDirectory.mkdir();
        File serviceDirectory = new File(configDirectory, "WFS");
        serviceDirectory.mkdir();
        File intanceDirectory = new File(serviceDirectory, "default");
        intanceDirectory.mkdir();
        File LayerContext = new File(intanceDirectory, "layerContext.xml");
        Source s1 = new Source("shapeSrc", Boolean.TRUE, null, null);
        Source s2 = new Source("omSrc", Boolean.TRUE, null, null);
        Source s3 = new Source("smlSrc", Boolean.TRUE, null, null);
        LayerContext context = new LayerContext(new Layers(Arrays.asList(s1, s2, s3)));
        Marshaller m = GenericDatabaseMarshallerPool.getInstance().acquireMarshaller();
        m.marshal(context, LayerContext);
        GenericDatabaseMarshallerPool.getInstance().release(m);
        
        ConfigDirectory.setConfigDirectory(configDirectory);
        service = new WFSService();

        Field privateStringField = WebService.class.getDeclaredField("uriContext");
        privateStringField.setAccessible(true);
        privateStringField.set(service, info);

        pathParameters.add("serviceId", "default");
        queryParameters.add("serviceId", "default");
        info.setPathParameters(pathParameters);
        info.setQueryParameters(queryParameters);
        
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        if (ds != null) {
            ds.shutdown();
        }
        if (ds2 != null) {
            ds2.shutdown();
        }

        if (service != null) {
            service.destroy();
        }
        File derbyLog = new File("derby.log");
        if (derbyLog.exists()) {
            derbyLog.delete();
        }
        FileUtilities.deleteDirectory(configDirectory);
        ConfigDirectory.setConfigDirectory(null);
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void transactionInsertTest() throws Exception {

        /*
         * we verify that the number of features before insert
         */
        InputStream is = new FileInputStream(FileUtilities.getFileFromResource("org.constellation.wfs.request.xml.GetFeature.xml"));
        Response result = service.doPOSTXml(is);

        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());

        assertTrue(result.getEntity() instanceof FeatureCollection);
        FeatureCollection collection = (FeatureCollection) result.getEntity();
        assertEquals(2, collection.size());
        
        /*
         * we insert the feature
         */
        is = new FileInputStream(FileUtilities.getFileFromResource("org.constellation.wfs.request.xml.InsertFeature.xml"));
        result = service.doPOSTXml(is);

        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());

        /*
         * we verify that the features has been inserted
         */
        is = new FileInputStream(FileUtilities.getFileFromResource("org.constellation.wfs.request.xml.GetFeature.xml"));
        result = service.doPOSTXml(is);

        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());

        assertTrue(result.getEntity() instanceof FeatureCollection);
        collection = (FeatureCollection) result.getEntity();
        assertEquals(4, collection.size());
        
        /*
         * we delete the features
         */
        is = new FileInputStream(FileUtilities.getFileFromResource("org.constellation.wfs.request.xml.DeleteFeature.xml"));
        result = service.doPOSTXml(is);

        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());

        /*
         * we verify that the features has been deleted
         */
        is = new FileInputStream(FileUtilities.getFileFromResource("org.constellation.wfs.request.xml.GetFeature.xml"));
        result = service.doPOSTXml(is);

        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());

        assertTrue(result.getEntity() instanceof FeatureCollection);
        collection = (FeatureCollection) result.getEntity();
        assertEquals(2, collection.size());
        
        /*
         * we insert the feature with another request
         */
        is = new FileInputStream(FileUtilities.getFileFromResource("org.constellation.wfs.request.xml.InsertFeature2.xml"));
        result = service.doPOSTXml(is);

        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());
        
        /*
         * we verify that the features has been inserted
         */
        is = new FileInputStream(FileUtilities.getFileFromResource("org.constellation.wfs.request.xml.GetFeature.xml"));
        result = service.doPOSTXml(is);

        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());

        assertTrue(result.getEntity() instanceof FeatureCollection);
        collection = (FeatureCollection) result.getEntity();
        assertEquals(4, collection.size());
        
    }

    private static void initFeatureSource() throws Exception {
         final File outputDir = initDataDirectory();

         final Configurator config = new Configurator() {

            @Override
            public ParameterValueGroup getConfiguration(String serviceName, ParameterDescriptorGroup desc) {
                final ParameterValueGroup config = desc.createValue();

                if("shapefile".equals(serviceName)){

                    final ParameterValueGroup source = config.addGroup(SOURCE_DESCRIPTOR_NAME);
                    final ParameterValueGroup srcconfig = getOrCreate(ShapeFileProviderService.SOURCE_CONFIG_DESCRIPTOR,source);
                    source.parameter(SOURCE_LOADALL_DESCRIPTOR.getName().getCode()).setValue(Boolean.TRUE);
                    source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("shapeSrc");
                    srcconfig.parameter(ShapeFileProviderService.FOLDER_DESCRIPTOR.getName().getCode())
                            .setValue(outputDir.getAbsolutePath() + "/org/constellation/ws/embedded/wms111/shapefiles");
                    srcconfig.parameter(ShapeFileProviderService.NAMESPACE_DESCRIPTOR.getName().getCode())
                            .setValue("http://www.opengis.net/gml");


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

                }else if("observation".equals(serviceName)){
                    try{
                        final String url = "jdbc:derby:memory:TestWFSServiceOM";
                        ds = new DefaultDataSource(url + ";create=true");
                        Connection con = ds.getConnection();
                        DerbySqlScriptRunner sr = new DerbySqlScriptRunner(con);
                        sr.run(Util.getResourceAsStream("org/constellation/sql/structure-observations.sql"));
                        sr.run(Util.getResourceAsStream("org/constellation/sql/sos-data.sql"));
                        con.close();

                        final ParameterValueGroup source = config.addGroup(SOURCE_DESCRIPTOR_NAME);
                        final ParameterValueGroup srcconfig = getOrCreate(OMDataStoreFactory.PARAMETERS_DESCRIPTOR,source);
                        srcconfig.parameter(OMDataStoreFactory.SGBDTYPE.getName().getCode()).setValue("derby");
                        srcconfig.parameter(OMDataStoreFactory.DERBYURL.getName().getCode()).setValue(url);
                        source.parameter(SOURCE_LOADALL_DESCRIPTOR.getName().getCode()).setValue(Boolean.TRUE);
                        source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("omSrc");
                    }catch(Exception ex){
                        throw new RuntimeException(ex.getLocalizedMessage(), ex);
                    }
                }else if("sensorML".equals(serviceName)){
                    try{
                        final String url2 = "jdbc:derby:memory:TestWFSServiceSMl";
                        ds2 = new DefaultDataSource(url2 + ";create=true");
                        Connection con = ds2.getConnection();
                        DerbySqlScriptRunner sr = new DerbySqlScriptRunner(con);
                        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/model/mdw_schema_2.1(derby).sql"));
                        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/ISO19115.sql"));
                        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/ISO19119.sql"));
                        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/ISO19108.sql"));
                        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/data/defaultRecordSets.sql"));
                        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/users/creation_user.sql"));
                        sr.run(Util.getResourceAsStream("org/mdweb/sql/v21/metadata/schemas/SensorML.sql"));
                        sr.run(Util.getResourceAsStream("org/constellation/sql/sml-data.sql"));
                        con.close();

                        final ParameterValueGroup source = config.addGroup(SOURCE_DESCRIPTOR_NAME);
                        final ParameterValueGroup srcconfig = getOrCreate(SMLDataStoreFactory.PARAMETERS_DESCRIPTOR,source);
                        source.parameter(SOURCE_LOADALL_DESCRIPTOR.getName().getCode()).setValue(Boolean.TRUE);
                        source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("smlSrc");
                        srcconfig.parameter(SMLDataStoreFactory.SGBDTYPE.getName().getCode()).setValue("derby");
                        srcconfig.parameter(SMLDataStoreFactory.DERBYURL.getName().getCode()).setValue(url2);
                    }catch(Exception ex){
                        throw new RuntimeException(ex.getLocalizedMessage(), ex);
                    }
                }

                return config;
            }

            @Override
            public void saveConfiguration(String serviceName, ParameterValueGroup params) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };

        LayerProviderProxy.getInstance().setConfigurator(config);
    }

    /**
     * Initialises the data directory in unzipping the jar containing the resources
     * into a temporary directory.
     *
     * @return The root output directory where the data are unzipped.
     * @throws IOException
     */
    private static File initDataDirectory() throws IOException {
        final ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        final String stylePath = "org/constellation/ws/embedded/wms111/styles";
        String styleResource = classloader.getResource(stylePath).getFile();

        if (styleResource.indexOf('!') != -1) {
            styleResource = styleResource.substring(0, styleResource.indexOf('!'));
        }
        if (styleResource.startsWith("file:")) {
            styleResource = styleResource.substring(5);
        }

        File styleJar = new File(styleResource);
        if (styleJar == null || !styleJar.exists()) {
            throw new IOException("Unable to find the style folder: "+ styleJar);
        }
        if (styleJar.isDirectory()) {
            styleJar = new File(styleJar.getPath().replaceAll(stylePath, ""));
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
