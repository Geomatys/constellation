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
package org.constellation.cite;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.xml.namespace.QName;
import org.apache.sis.geometry.GeneralDirectPosition;
import org.apache.sis.util.logging.Logging;
import org.constellation.business.IDataBusiness;
import org.constellation.business.ILayerBusiness;
import org.constellation.business.IProviderBusiness;
import org.constellation.business.IServiceBusiness;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.admin.SpringHelper;
import org.constellation.api.ProviderType;
import org.constellation.configuration.LayerContext;
import org.constellation.dto.Details;
import org.constellation.provider.DataProviders;
import org.constellation.provider.ProviderFactory;
import static org.constellation.provider.configuration.ProviderParameters.SOURCE_ID_DESCRIPTOR;
import static org.constellation.provider.configuration.ProviderParameters.SOURCE_LOADALL_DESCRIPTOR;
import static org.constellation.provider.configuration.ProviderParameters.getOrCreate;
import static org.constellation.provider.featurestore.FeatureStoreProviderService.SOURCE_CONFIG_DESCRIPTOR;
import org.constellation.test.utils.SpringTestRunner;
import org.constellation.wfs.ws.DefaultWFSWorker;
import org.constellation.wfs.ws.WFSWorker;
import org.constellation.wfs.ws.rs.FeatureCollectionWrapper;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.feature.xml.XmlFeatureWriter;
import org.geotoolkit.feature.xml.jaxp.JAXPStreamFeatureWriter;
import org.geotoolkit.gml.xml.v311.MultiPointType;
import org.geotoolkit.gml.xml.v311.PointPropertyType;
import org.geotoolkit.gml.xml.v311.PointType;
import org.geotoolkit.internal.io.IOUtilities;
import org.geotoolkit.util.FileUtilities;
import org.geotoolkit.ogc.xml.v110.AndType;
import org.geotoolkit.ogc.xml.v110.BBOXType;
import org.geotoolkit.ogc.xml.v110.EqualsType;
import org.geotoolkit.ogc.xml.v110.FilterType;
import org.geotoolkit.ogc.xml.v110.LiteralType;
import org.geotoolkit.ogc.xml.v110.PropertyIsEqualToType;
import org.geotoolkit.ogc.xml.v110.PropertyNameType;
import static org.geotoolkit.utility.parameter.ParametersExt.getOrCreateGroup;
import org.geotoolkit.wfs.xml.ResultTypeType;
import org.geotoolkit.wfs.xml.v110.GetCapabilitiesType;
import org.geotoolkit.wfs.xml.v110.GetFeatureType;
import org.geotoolkit.wfs.xml.v110.QueryType;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opengis.parameter.ParameterValueGroup;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;


/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@RunWith(SpringTestRunner.class)
@ContextConfiguration("classpath:/cstl/spring/test-derby.xml")
@ActiveProfiles({"standard","derby"})
public class WFSCIteWorkerTest implements ApplicationContextAware {

    private static final Logger LOGGER = Logging.getLogger(WFSCIteWorkerTest.class);
    
    protected ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private static WFSWorker worker;

    private XmlFeatureWriter featureWriter;
    
    @Inject
    protected IServiceBusiness serviceBusiness;
    
    @Inject
    protected ILayerBusiness layerBusiness;
    
    @Inject
    protected IProviderBusiness providerBusiness;
    
    @Inject
    protected IDataBusiness dataBusiness;

    private static boolean initialized = false;
    
    @BeforeClass
    public static void initTestDir() {
        ConfigDirectory.setupTestEnvironement("WFSCiteWorkerTest");
    }
    
    @PostConstruct
    public void setUpClass() {
        SpringHelper.setApplicationContext(applicationContext);
        if (!initialized) {
            try {

                layerBusiness.removeAll();
                serviceBusiness.deleteAll();
                dataBusiness.deleteAll();
                providerBusiness.removeAll();

                final File outputDir = initDataDirectory();
                String path;
                if (outputDir.getAbsolutePath().endsWith("org/constellation/ws/embedded/wms111/styles")) {
                    path = outputDir.getAbsolutePath().substring(0, outputDir.getAbsolutePath().indexOf("org/constellation/ws/embedded/wms111/styles"));
                } else {
                    path = outputDir.getAbsolutePath();
                }
                
                final ProviderFactory factory = DataProviders.getInstance().getFactory("feature-store");

                // Defines a GML data provider
                ParameterValueGroup source = factory.getProviderDescriptor().createValue();
                source.parameter(SOURCE_LOADALL_DESCRIPTOR.getName().getCode()).setValue(Boolean.TRUE);
                source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("primGMLSrc");

                ParameterValueGroup choice = getOrCreate(SOURCE_CONFIG_DESCRIPTOR,source);
                ParameterValueGroup pgconfig = getOrCreateGroup(choice, "GMLParameters");
                pgconfig.parameter("identifier").setValue("gml");
                pgconfig.parameter("url").setValue(new URL("file:" + path + "/org/constellation/ws/embedded/wfs110/primitive"));
                pgconfig.parameter("sparse").setValue(Boolean.TRUE);
                pgconfig.parameter("xsd").setValue("file:" + path + "/org/constellation/ws/embedded/wfs110/cite-gmlsf0.xsd");
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
                pgconfig.parameter("url").setValue(new URL("file:" + path + "/org/constellation/ws/embedded/wfs110/entity"));
                pgconfig.parameter("sparse").setValue(Boolean.TRUE);
                pgconfig.parameter("xsd").setValue("file:" + path + "/org/constellation/ws/embedded/wfs110/cite-gmlsf0.xsd");
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
                pgconfig.parameter("url").setValue(new URL("file:" + path + "/org/constellation/ws/embedded/wfs110/aggregate"));
                pgconfig.parameter("sparse").setValue(Boolean.TRUE);
                pgconfig.parameter("xsd").setValue("file:" + path + "/org/constellation/ws/embedded/wfs110/cite-gmlsf0.xsd");
                pgconfig.parameter("xsdtypename").setValue("AggregateGeoFeature");
                pgconfig.parameter("longitudeFirst").setValue(Boolean.TRUE);
                pgconfig.parameter("namespace").setValue("http://cite.opengeospatial.org/gmlsf");
                providerBusiness.storeProvider("aggGMLSrc", null, ProviderType.LAYER, "feature-store", source);
                dataBusiness.create(new QName("http://cite.opengeospatial.org/gmlsf", "AggregateGeoFeature"), "aggGMLSrc", "VECTOR", false, true, null, null);
                

                DataProviders.getInstance().reload();
                final LayerContext config = new LayerContext();
                config.getCustomParameters().put("shiroAccessible", "false");
                config.getCustomParameters().put("transactionSecurized", "false");
                config.getCustomParameters().put("transactionnal", "true");
                
                Details details = new Details("default", "default", null, null, Arrays.asList("1.1.0"), null, null, true, "en");
                         
                serviceBusiness.create("wfs", "default", config, details);
                layerBusiness.add("AggregateGeoFeature", "http://cite.opengeospatial.org/gmlsf", "aggGMLSrc", null, "default", "wfs", null);
                layerBusiness.add("PrimitiveGeoFeature", "http://cite.opengeospatial.org/gmlsf", "primGMLSrc", null, "default", "wfs", null);
                layerBusiness.add("EntitéGénérique",     "http://cite.opengeospatial.org/gmlsf", "entGMLSrc", null, "default", "wfs", null);

                worker = new DefaultWFSWorker("default");
                worker.setLogLevel(Level.FINER);
                initialized = true;
            } catch (Exception ex) {
                Logger.getLogger(WFSCIteWorkerTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        ConfigDirectory.shutdownTestEnvironement("WFSCiteWorkerTest");
    }

    @Before
    public void setUp() throws Exception {
        featureWriter     = new JAXPStreamFeatureWriter();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getCapabilitiesTest() throws Exception {
        worker.getCapabilities(new GetCapabilitiesType("WFS"));
    }
     /**
     * test the feature marshall
     *
     */
    @Test
    public void getFeatureShapeFileTest() throws Exception {

        /**
         * Test 1 : query on typeName aggragateGeofeature
         */

        List<QueryType> queries = new ArrayList<>();
        List<PointPropertyType> points = new ArrayList<>();
        points.add(new PointPropertyType(new PointType(null, new GeneralDirectPosition(29.86, 70.83))));
        points.add(new PointPropertyType(new PointType(null, new GeneralDirectPosition(31.08, 68.87))));
        points.add(new PointPropertyType(new PointType(null, new GeneralDirectPosition(32.19, 71.96))));

        EqualsType equals = new EqualsType("http://cite.opengeospatial.org/gmlsf:multiPointProperty", new MultiPointType("urn:ogc:def:crs:EPSG:4326", points));
        FilterType f = new FilterType(equals);
        queries.add(new QueryType(f, Arrays.asList(new QName("http://cite.opengeospatial.org/gmlsf", "AggregateGeoFeature")), "1.1.0"));
        GetFeatureType request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

        Object result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);

        FeatureCollection collection = ((FeatureCollectionWrapper)result).getFeatureCollection();

        StringWriter writer = new StringWriter();
        featureWriter.write(collection,writer);
        writer.flush();
        String xmlResult = writer.toString();
        assertEquals(1, collection.size());

        /**
         * Test 1 : query on typeName aggragateGeofeature
         */

        queries = new ArrayList<>();
        QueryType query = new QueryType(null, Arrays.asList(new QName("http://cite.opengeospatial.org/gmlsf", "PrimitiveGeoFeature")), "1.1.0");
        queries.add(query);
        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

        result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        
        collection = ((FeatureCollectionWrapper)result).getFeatureCollection();

        writer = new StringWriter();
        featureWriter.write(collection, writer);
        writer.flush();
        xmlResult = writer.toString();
        System.out.println(xmlResult);

        assertEquals(5, collection.size());
        
        /**
         * Test 1 : query on typeName aggragateGeofeature
         */

        queries = new ArrayList<>();
        BBOXType bbox = new BBOXType("http://cite.opengeospatial.org/gmlsf:pointProperty", 30, -12, 60, -6, "urn:ogc:def:crs:EPSG:4326");
        PropertyIsEqualToType propEqual = new PropertyIsEqualToType(new LiteralType("name-f015"), new PropertyNameType("http://www.opengis.net/gml:name"), Boolean.TRUE);
        AndType and = new AndType(bbox, propEqual);
        f = new FilterType(and);
        query = new QueryType(f, Arrays.asList(new QName("http://cite.opengeospatial.org/gmlsf", "PrimitiveGeoFeature")), "1.1.0");
        //query.setSrsName("urn:ogc:def:crs:EPSG:6.11:32629");
        queries.add(query);
        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

        result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        
        collection = ((FeatureCollectionWrapper)result).getFeatureCollection();

        writer = new StringWriter();
        featureWriter.write(collection, writer);
        writer.flush();
        xmlResult = writer.toString();
        System.out.println(xmlResult);

        assertEquals(1, collection.size());
        
        String url = "http://localhost:8180/constellation/WS/wfs/ows11?service=WFS&version=1.1.0&request=GetFeature&typename=sf:PrimitiveGeoFeature&namespace=xmlns%28sf=http://cite.opengeospatial.org/gmlsf%29&filter=%3Cogc:Filter%20xmlns:gml=%22http://www.opengis.net/gml%22%20xmlns:ogc=%22http://www.opengis.net/ogc%22%3E%3Cogc:PropertyIsEqualTo%3E%3Cogc:PropertyName%3E//gml:description%3C/ogc:PropertyName%3E%3Cogc:Literal%3Edescription-f008%3C/ogc:Literal%3E%3C/ogc:PropertyIsEqualTo%3E%3C/ogc:Filter%3E";

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
        }else {
            FileUtilities.deleteDirectory(outputDir);
            outputDir.mkdir();
        }
        IOUtilities.unzip(in, outputDir);
        in.close();
        return outputDir;
    }

    
}
