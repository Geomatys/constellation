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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.sql.Connection;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.Layers;
import org.constellation.configuration.Source;
import org.constellation.provider.DataProviders;
import org.constellation.provider.Provider;
import org.constellation.provider.ProviderFactory;
import org.constellation.provider.Providers;
import org.constellation.provider.configuration.AbstractConfigurator;
import org.constellation.provider.configuration.Configurator;

import static org.constellation.provider.configuration.ProviderParameters.*;
import org.constellation.test.utils.BasicMultiValueMap;
import org.constellation.test.utils.BasicUriInfo;
import org.constellation.util.Util;
import org.constellation.wfs.ws.rs.FeatureCollectionWrapper;
import org.constellation.wfs.ws.rs.WFSService;
import org.constellation.ws.rs.WebService;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.internal.io.IOUtilities;
import org.geotoolkit.internal.sql.DefaultDataSource;

import static org.geotoolkit.parameter.ParametersExt.*;
import org.geotoolkit.util.FileUtilities;
import org.geotoolkit.util.sql.DerbySqlScriptRunner;

// JUnit dependencies
import org.junit.*;
import static org.junit.Assert.*;
import org.opengis.parameter.ParameterValueGroup;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class WFSServiceTest {

    private static WFSService service;

    private static DefaultDataSource ds = null;

    private static DefaultDataSource ds2 = null;

    private static final BasicUriInfo info = new BasicUriInfo(null, null);

    private static final MultivaluedMap<String,String> queryParameters = new BasicMultiValueMap<>();
    private static final MultivaluedMap<String,String> pathParameters = new BasicMultiValueMap<>();

    @BeforeClass
    public static void setUpClass() throws Exception {
        ConfigurationEngine.setupTestEnvironement("WFSServiceTest");

        final List<Source> sources = Arrays.asList(new Source("coverageTestSrc", true, null, null),
                                                   new Source("omSrc", true, null, null),
                                                   new Source("shapeSrc", true, null, null),
                                                   new Source("postgisSrc", true, null, null));
        final Layers layers = new Layers(sources);
        final LayerContext config = new LayerContext(layers);
        config.getCustomParameters().put("shiroAccessible", "false");
        config.getCustomParameters().put("transactionSecurized", "false");
        config.getCustomParameters().put("transactionnal", "true");

        ConfigurationEngine.storeConfiguration("WFS", "default", config);
        
        initFeatureSource();
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
        ConfigurationEngine.shutdownTestEnvironement("WFSServiceTest");
        
        DataProviders.getInstance().setConfigurator(Providers.DEFAULT_CONFIGURATOR);
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

        assertTrue(result.getEntity() instanceof FeatureCollectionWrapper);
        FeatureCollection collection = ((FeatureCollectionWrapper) result.getEntity()).getFeatureCollection();
        assertEquals(6, collection.size());

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

        assertTrue(result.getEntity() instanceof FeatureCollectionWrapper);
        collection = ((FeatureCollectionWrapper) result.getEntity()).getFeatureCollection();
        assertEquals(8, collection.size());

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

        assertTrue(result.getEntity() instanceof FeatureCollectionWrapper);
        collection = ((FeatureCollectionWrapper) result.getEntity()).getFeatureCollection();
        assertEquals(6, collection.size());

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

        assertTrue(result.getEntity() instanceof FeatureCollectionWrapper);
        collection = ((FeatureCollectionWrapper) result.getEntity()).getFeatureCollection();
        assertEquals(8, collection.size());

    }

    private static void initFeatureSource() throws Exception {
         final File outputDir = initDataDirectory();

         final Configurator config = new AbstractConfigurator() {

             @Override
             public List<Map.Entry<String, ParameterValueGroup>> getProviderConfigurations() throws ConfigurationException {
                 
                 final ArrayList<Map.Entry<String, ParameterValueGroup>> lst = new ArrayList<>();
                
                final ProviderFactory factory = DataProviders.getInstance().getFactory("feature-store");
                
                try{ 
                    
                    {//OBSERVATION
                        final String url = "jdbc:derby:memory:TestWFSServiceOM";
                        final DefaultDataSource ds = new DefaultDataSource(url + ";create=true");
                        Connection con = ds.getConnection();
                        DerbySqlScriptRunner sr = new DerbySqlScriptRunner(con);
                        sr.run(Util.getResourceAsStream("org/constellation/om2/structure_observations.sql"));
                        sr.run(Util.getResourceAsStream("org/constellation/sql/sos-data-om2.sql"));
                        con.close();
                        ds.shutdown();
                        
                        final ParameterValueGroup source = factory.getProviderDescriptor().createValue();
                        getOrCreateValue(source, "id").setValue("omSrc");
                        getOrCreateValue(source, "load_all").setValue(true);    
                        
                        final ParameterValueGroup choice = getOrCreateGroup(source, "choice");
                        final ParameterValueGroup omconfig = createGroup(choice, "OMParameters");
                        getOrCreateValue(omconfig, "sgbdtype").setValue("derby");
                        getOrCreateValue(omconfig, "derbyurl").setValue(url);
                        lst.add(new AbstractMap.SimpleImmutableEntry<>("omSrc",source));
                    }

                    {//SHAPEFILE
                        final File outputDir = initDataDirectory();
                        final ParameterValueGroup source = factory.getProviderDescriptor().createValue();
                        getOrCreateValue(source, "id").setValue("shapeSrc");
                        getOrCreateValue(source, "load_all").setValue(true);    
                        
                        final ParameterValueGroup choice = getOrCreateGroup(source, "choice");
                        final ParameterValueGroup shpconfig = createGroup(choice, "ShapefileParametersFolder");
                        getOrCreateValue(shpconfig, "url").setValue(new URL("file:"+outputDir.getAbsolutePath() + "/org/constellation/ws/embedded/wms111/shapefiles"));
                        getOrCreateValue(shpconfig, "namespace").setValue("http://www.opengis.net/gml");        
                        
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
                        
                        lst.add(new AbstractMap.SimpleImmutableEntry<>("shapeSrc",source));
                    }

                    {//SENSORML
                        final String url2 = "jdbc:derby:memory:TestWFSServiceSMl";
                        ds2 = new DefaultDataSource(url2 + ";create=true");
                        Connection con = ds2.getConnection();
                        DerbySqlScriptRunner sr = new DerbySqlScriptRunner(con);
                        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/model/mdw_schema_2.4_derby.sql"));
                        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ISO19115.sql"));
                        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ISO19119.sql"));
                        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ISO19108.sql"));
                        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/data/defaultRecordSets.sql"));
                        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/users/creation_user.sql"));
                        sr.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/SensorML.sql"));
                        sr.run(Util.getResourceAsStream("org/constellation/sql/sml-data.sql"));
                        con.close();
                            
                        final ParameterValueGroup source = factory.getProviderDescriptor().createValue();
                        getOrCreateValue(source, "id").setValue("smlSrc");
                        getOrCreateValue(source, "load_all").setValue(true);             
                        
                        final ParameterValueGroup choice = getOrCreateGroup(source, "choice");
                        final ParameterValueGroup omconfig = createGroup(choice, "SMLParameters");
                        getOrCreateValue(omconfig, "sgbdtype").setValue("derby");
                        getOrCreateValue(omconfig, "derbyurl").setValue(url2);                
                        
                        lst.add(new AbstractMap.SimpleImmutableEntry<>("smlSrc",source));                   
                    }

                }catch(Exception ex){
                    throw new RuntimeException(ex.getLocalizedMessage(),ex);
                }
                
                return lst;
             }

             @Override
            public List<Configurator.ProviderInformation> getProviderInformations() throws ConfigurationException {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };

        DataProviders.getInstance().setConfigurator(config);
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
        if (!styleJar.exists()) {
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
