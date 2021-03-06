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
package org.constellation.wfs;

import java.io.File;
import java.io.StringWriter;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import org.apache.sis.util.logging.Logging;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.business.IDataBusiness;
import org.constellation.business.ILayerBusiness;
import org.constellation.business.IProviderBusiness;
import org.constellation.business.IServiceBusiness;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.admin.SpringHelper;
import org.constellation.api.ProviderType;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.LayerContext;
import org.constellation.provider.DataProviders;
import org.constellation.provider.FeatureData;
import org.constellation.provider.ProviderFactory;
import org.constellation.test.utils.CstlDOMComparator;
import org.constellation.test.utils.Order;
import org.constellation.test.utils.SpringTestRunner;
import org.constellation.util.QNameComparator;
import org.constellation.util.Util;
import org.constellation.wfs.ws.DefaultWFSWorker;
import org.constellation.wfs.ws.WFSWorker;
import org.constellation.wfs.ws.rs.FeatureCollectionWrapper;
import org.constellation.wfs.ws.rs.ValueCollectionWrapper;
import org.constellation.ws.CstlServiceException;
import org.constellation.ws.embedded.AbstractGrizzlyServer;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.feature.Feature;
import org.geotoolkit.feature.type.FeatureType;
import org.geotoolkit.util.NamesExt;
import org.geotoolkit.feature.xml.XmlFeatureWriter;
import org.geotoolkit.feature.xml.jaxp.JAXPStreamFeatureReader;
import org.geotoolkit.feature.xml.jaxp.JAXPStreamFeatureWriter;
import org.geotoolkit.feature.xml.jaxp.JAXPStreamValueCollectionWriter;
import org.geotoolkit.gml.xml.v321.DirectPositionType;
import org.geotoolkit.gml.xml.v321.EnvelopeType;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.ogc.xml.v200.AndType;
import org.geotoolkit.ogc.xml.v200.BBOXType;
import org.geotoolkit.ogc.xml.v200.ComparisonOpsType;
import org.geotoolkit.ogc.xml.v200.FilterType;
import org.geotoolkit.ogc.xml.v200.LiteralType;
import org.geotoolkit.ogc.xml.v200.LogicOpsType;
import org.geotoolkit.ogc.xml.v200.PropertyIsEqualToType;
import org.geotoolkit.ogc.xml.v200.ResourceIdType;
import org.geotoolkit.ogc.xml.v200.SortByType;
import org.geotoolkit.ogc.xml.v200.SortOrderType;
import org.geotoolkit.ogc.xml.v200.SortPropertyType;
import org.geotoolkit.ogc.xml.v200.SpatialOpsType;
import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_PARAMETER_VALUE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.INVALID_VALUE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.MISSING_PARAMETER_VALUE;
import static org.geotoolkit.ows.xml.OWSExceptionCode.VERSION_NEGOTIATION_FAILED;
import static org.geotoolkit.utility.parameter.ParametersExt.getOrCreateGroup;
import static org.geotoolkit.utility.parameter.ParametersExt.getOrCreateValue;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.util.FileUtilities;
import org.geotoolkit.util.sql.DerbySqlScriptRunner;
import org.geotoolkit.wfs.xml.AllSomeType;
import org.geotoolkit.wfs.xml.CreateStoredQueryResponse;
import org.geotoolkit.wfs.xml.DescribeStoredQueriesResponse;
import org.geotoolkit.wfs.xml.DropStoredQueryResponse;
import org.geotoolkit.wfs.xml.ListStoredQueriesResponse;
import org.geotoolkit.wfs.xml.ResultTypeType;
import org.geotoolkit.wfs.xml.StoredQueries;
import org.geotoolkit.wfs.xml.StoredQueryDescription;
import org.geotoolkit.wfs.xml.TransactionResponse;
import org.geotoolkit.wfs.xml.ValueCollection;
import org.geotoolkit.wfs.xml.WFSCapabilities;
import org.geotoolkit.wfs.xml.WFSMarshallerPool;
import org.geotoolkit.wfs.xml.v200.ActionResultsType;
import org.geotoolkit.wfs.xml.v200.CreateStoredQueryResponseType;
import org.geotoolkit.wfs.xml.v200.CreateStoredQueryType;
import org.geotoolkit.wfs.xml.v200.CreatedOrModifiedFeatureType;
import org.geotoolkit.wfs.xml.v200.DeleteType;
import org.geotoolkit.wfs.xml.v200.DescribeFeatureTypeType;
import org.geotoolkit.wfs.xml.v200.DescribeStoredQueriesResponseType;
import org.geotoolkit.wfs.xml.v200.DescribeStoredQueriesType;
import org.geotoolkit.wfs.xml.v200.DropStoredQueryResponseType;
import org.geotoolkit.wfs.xml.v200.DropStoredQueryType;
import org.geotoolkit.wfs.xml.v200.FeatureCollectionType;
import org.geotoolkit.wfs.xml.v200.GetFeatureType;
import org.geotoolkit.wfs.xml.v200.GetPropertyValueType;
import org.geotoolkit.wfs.xml.v200.InsertType;
import org.geotoolkit.wfs.xml.v200.ListStoredQueriesResponseType;
import org.geotoolkit.wfs.xml.v200.ListStoredQueriesType;
import org.geotoolkit.wfs.xml.v200.ObjectFactory;
import org.geotoolkit.wfs.xml.v200.ParameterExpressionType;
import org.geotoolkit.wfs.xml.v200.ParameterType;
import org.geotoolkit.wfs.xml.v200.PropertyName;
import org.geotoolkit.wfs.xml.v200.PropertyType;
import org.geotoolkit.wfs.xml.v200.QueryExpressionTextType;
import org.geotoolkit.wfs.xml.v200.QueryType;
import org.geotoolkit.wfs.xml.v200.ReplaceType;
import org.geotoolkit.wfs.xml.v200.StoredQueryDescriptionType;
import org.geotoolkit.wfs.xml.v200.StoredQueryListItemType;
import org.geotoolkit.wfs.xml.v200.StoredQueryType;
import org.geotoolkit.wfs.xml.v200.Title;
import org.geotoolkit.wfs.xml.v200.TransactionResponseType;
import org.geotoolkit.wfs.xml.v200.TransactionSummaryType;
import org.geotoolkit.wfs.xml.v200.TransactionType;
import org.geotoolkit.wfs.xml.v200.UpdateActionType;
import org.geotoolkit.wfs.xml.v200.UpdateType;
import org.geotoolkit.wfs.xml.v200.ValueReference;
import org.geotoolkit.xsd.xml.v2001.ComplexType;
import org.geotoolkit.xsd.xml.v2001.Schema;
import org.geotoolkit.xsd.xml.v2001.TopLevelComplexType;
import org.geotoolkit.xsd.xml.v2001.TopLevelElement;
import org.geotoolkit.xsd.xml.v2001.XSDMarshallerPool;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.GenericName;
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
@ContextConfiguration("classpath:/cstl/spring/test-context.xml")
@ActiveProfiles({"standard"})
public class WFS2WorkerTest implements ApplicationContextAware {

    private static final Logger LOGGER = Logging.getLogger("org.constellation.wfs");

    protected ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private static MarshallerPool pool;
    private static WFSWorker worker ;

    @Inject
    private IServiceBusiness serviceBusiness;

    @Inject
    protected ILayerBusiness layerBusiness;

    @Inject
    protected IProviderBusiness providerBusiness;

    @Inject
    protected IDataBusiness dataBusiness;

    private static final DefaultDataSource ds = null;

    private static DefaultDataSource ds2 = null;

    private XmlFeatureWriter featureWriter;

    private XmlFeatureWriter valueWriter;

    private static String EPSG_VERSION;

    private static final ObjectFactory wfsFactory = new ObjectFactory();
    private static final org.geotoolkit.ogc.xml.v200.ObjectFactory ogcFactory = new org.geotoolkit.ogc.xml.v200.ObjectFactory();

    private static final List<QName> alltypes = new ArrayList<>();

    private static boolean initialized = false;

    private static boolean mdweb_active = true;

    @BeforeClass
    public static void initTestDir() {
        ConfigDirectory.setupTestEnvironement("WFS2WorkerTest");
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

                final ProviderFactory featfactory = DataProviders.getInstance().getFactory("feature-store");

                final File outputDir = AbstractGrizzlyServer.initDataDirectory();
                final ParameterValueGroup sourcef = featfactory.getProviderDescriptor().createValue();
                getOrCreateValue(sourcef, "id").setValue("shapeSrc");
                getOrCreateValue(sourcef, "load_all").setValue(true);

                final ParameterValueGroup choice2 = getOrCreateGroup(sourcef, "choice");
                final ParameterValueGroup shpconfig = getOrCreateGroup(choice2, "ShapefileParametersFolder");
                getOrCreateValue(shpconfig, "url").setValue(new URL("file:"+outputDir.getAbsolutePath() + "/org/constellation/ws/embedded/wms111/shapefiles"));
                getOrCreateValue(shpconfig, "namespace").setValue("http://www.opengis.net/gml/3.2");

                final ParameterValueGroup layer = getOrCreateGroup(sourcef, "Layer");
                getOrCreateValue(layer, "name").setValue("NamedPlaces");
                getOrCreateValue(layer, "style").setValue("cite_style_NamedPlaces");

                providerBusiness.storeProvider("shapeSrc", null, ProviderType.LAYER, "feature-store", sourcef);

                dataBusiness.create(new QName("http://www.opengis.net/gml/3.2", "BuildingCenters"), "shapeSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://www.opengis.net/gml/3.2", "BasicPolygons"),   "shapeSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://www.opengis.net/gml/3.2", "Bridges"),         "shapeSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://www.opengis.net/gml/3.2", "Streams"),         "shapeSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://www.opengis.net/gml/3.2", "Lakes"),           "shapeSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://www.opengis.net/gml/3.2", "NamedPlaces"),     "shapeSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://www.opengis.net/gml/3.2", "Buildings"),       "shapeSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://www.opengis.net/gml/3.2", "RoadSegments"),    "shapeSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://www.opengis.net/gml/3.2", "DividedRoutes"),   "shapeSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://www.opengis.net/gml/3.2", "Forests"),         "shapeSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://www.opengis.net/gml/3.2", "MapNeatline"),     "shapeSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://www.opengis.net/gml/3.2", "Ponds"),           "shapeSrc", "VECTOR", false, true, null, null);

                final String url = "jdbc:derby:memory:TestWFS2WorkerOM";
                final DefaultDataSource ds = new DefaultDataSource(url + ";create=true");
                Connection con = ds.getConnection();
                DerbySqlScriptRunner sr = new DerbySqlScriptRunner(con);
                String sql = FileUtilities.getStringFromStream(Util.getResourceAsStream("org/constellation/om2/structure_observations.sql"));
                sql = sql.replace("$SCHEMA", "");
                sr.run(sql);
                sr.run(Util.getResourceAsStream("org/constellation/sql/sos-data-om2.sql"));
                con.close();
                ds.shutdown();

                final ParameterValueGroup sourceOM = featfactory.getProviderDescriptor().createValue();
                getOrCreateValue(sourceOM, "id").setValue("omSrc");
                getOrCreateValue(sourceOM, "load_all").setValue(true);

                final ParameterValueGroup choiceOM = getOrCreateGroup(sourceOM, "choice");
                final ParameterValueGroup omconfig = getOrCreateGroup(choiceOM, " SOSDBParameters");
                getOrCreateValue(omconfig, "sgbdtype").setValue("derby");
                getOrCreateValue(omconfig, "derbyurl").setValue(url);

                providerBusiness.storeProvider("omSrc", null, ProviderType.LAYER, "feature-store", sourceOM);
                dataBusiness.create(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint"), "omSrc", "VECTOR", false, true, null, null);

                // MDWEB store
                mdweb_active = Util.getResourceAsStream("org/mdweb/sql/v24/metadata/model/mdw_schema_2.4_derby.sql") != null;
                if (mdweb_active) {
                    final String url2 = "jdbc:derby:memory:TestWFS2WorkerSMl";
                    ds2 = new DefaultDataSource(url2 + ";create=true");
                    Connection con2 = ds2.getConnection();
                    DerbySqlScriptRunner sr2 = new DerbySqlScriptRunner(con2);
                    sr2.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/model/mdw_schema_2.4_derby.sql"));
                    sr2.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ISO19115.sql"));
                    sr2.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ISO19119.sql"));
                    sr2.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/ISO19108.sql"));
                    sr2.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/data/defaultRecordSets.sql"));
                    sr2.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/users/creation_user.sql"));
                    sr2.run(Util.getResourceAsStream("org/mdweb/sql/v24/metadata/schemas/SensorML.sql"));
                    sr2.run(Util.getResourceAsStream("org/constellation/sql/sml-data.sql"));
                    con.close();

                    final ParameterValueGroup sourceSML = featfactory.getProviderDescriptor().createValue();
                    getOrCreateValue(sourceSML, "id").setValue("smlSrc");
                    getOrCreateValue(sourceSML, "load_all").setValue(true);

                    final ParameterValueGroup choiceSML = getOrCreateGroup(sourceSML, "choice");
                    final ParameterValueGroup smlconfig = getOrCreateGroup(choiceSML, "SMLParameters");
                    getOrCreateValue(smlconfig, "sgbdtype").setValue("derby");
                    getOrCreateValue(smlconfig, "derbyurl").setValue(url2);

                    providerBusiness.storeProvider("smlSrc", null, ProviderType.LAYER, "feature-store", sourceSML);
                    dataBusiness.create(new QName("http://www.opengis.net/sml/1.0", "System"),         "smlSrc", "VECTOR", false, true, null, null);
                    dataBusiness.create(new QName("http://www.opengis.net/sml/1.0", "Component"),      "smlSrc", "VECTOR", false, true, null, null);
                    dataBusiness.create(new QName("http://www.opengis.net/sml/1.0", "DataSourceType"), "smlSrc", "VECTOR", false, true, null, null);
                    dataBusiness.create(new QName("http://www.opengis.net/sml/1.0", "ProcessModel"),   "smlSrc", "VECTOR", false, true, null, null);
                    dataBusiness.create(new QName("http://www.opengis.net/sml/1.0", "ProcessChain"),   "smlSrc", "VECTOR", false, true, null, null);
                }
                initAllTypes();

                DataProviders.getInstance().reload();

                final LayerContext config2 = new LayerContext();
                config2.getCustomParameters().put("shiroAccessible", "false");
                config2.getCustomParameters().put("transactionSecurized", "false");
                config2.getCustomParameters().put("transactionnal", "true");

                serviceBusiness.create("wfs", "test1", config2, null);
                layerBusiness.add("SamplingPoint",       "http://www.opengis.net/sampling/1.0",  "omSrc",      null, "test1", "wfs", null);
                layerBusiness.add("BuildingCenters",     "http://www.opengis.net/gml/3.2",       "shapeSrc",   null, "test1", "wfs", null);
                layerBusiness.add("BasicPolygons",       "http://www.opengis.net/gml/3.2",       "shapeSrc",   null, "test1", "wfs", null);
                layerBusiness.add("Bridges",             "http://www.opengis.net/gml/3.2",       "shapeSrc",   null, "test1", "wfs", null);
                layerBusiness.add("Streams",             "http://www.opengis.net/gml/3.2",       "shapeSrc",   null, "test1", "wfs", null);
                layerBusiness.add("Lakes",               "http://www.opengis.net/gml/3.2",       "shapeSrc",   null, "test1", "wfs", null);
                layerBusiness.add("NamedPlaces",         "http://www.opengis.net/gml/3.2",       "shapeSrc",   null, "test1", "wfs", null);
                layerBusiness.add("Buildings",           "http://www.opengis.net/gml/3.2",       "shapeSrc",   null, "test1", "wfs", null);
                layerBusiness.add("RoadSegments",        "http://www.opengis.net/gml/3.2",       "shapeSrc",   null, "test1", "wfs", null);
                layerBusiness.add("DividedRoutes",       "http://www.opengis.net/gml/3.2",       "shapeSrc",   null, "test1", "wfs", null);
                layerBusiness.add("Forests",             "http://www.opengis.net/gml/3.2",       "shapeSrc",   null, "test1", "wfs", null);
                layerBusiness.add("MapNeatline",         "http://www.opengis.net/gml/3.2",       "shapeSrc",   null, "test1", "wfs", null);
                layerBusiness.add("Ponds",               "http://www.opengis.net/gml/3.2",       "shapeSrc",   null, "test1", "wfs", null);
                if (mdweb_active) {
                    layerBusiness.add("System",              "http://www.opengis.net/sml/1.0",       "smlSrc",     null, "test1", "wfs", null);
                    layerBusiness.add("Component",           "http://www.opengis.net/sml/1.0",       "smlSrc",     null, "test1", "wfs", null);
                    layerBusiness.add("DataSourceType",      "http://www.opengis.net/sml/1.0",       "smlSrc",     null, "test1", "wfs", null);
                    layerBusiness.add("ProcessModel",        "http://www.opengis.net/sml/1.0",       "smlSrc",     null, "test1", "wfs", null);
                    layerBusiness.add("ProcessChain",        "http://www.opengis.net/sml/1.0",       "smlSrc",     null, "test1", "wfs", null);
                }


                pool = WFSMarshallerPool.getInstance();

                final List<StoredQueryDescription> descriptions = new ArrayList<>();
                final ParameterExpressionType param = new ParameterExpressionType("name", "name Parameter", "A parameter on the name of the feature", new QName("http://www.w3.org/2001/XMLSchema", "string", "xs"));
                final List<QName> types = Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint"));
                final PropertyIsEqualToType pis = new PropertyIsEqualToType(new LiteralType("${name}"), "name", true);
                final FilterType filter = new FilterType(pis);
                final QueryType query = new QueryType(filter, types, "2.0.0");
                final QueryExpressionTextType queryEx = new QueryExpressionTextType("urn:ogc:def:queryLanguage:OGC-WFS::WFS_QueryExpression", null, types);
                final ObjectFactory factory = new ObjectFactory();
                queryEx.getContent().add(factory.createQuery(query));
                final StoredQueryDescriptionType des1 = new StoredQueryDescriptionType("nameQuery", "Name query" , "filter on name for samplingPoint", param, queryEx);
                descriptions.add(des1);
                final StoredQueries queries = new StoredQueries(descriptions);
                serviceBusiness.setExtraConfiguration("wfs", "test1", "StoredQueries.xml", queries, pool);

                EPSG_VERSION = CRS.getVersion("EPSG").toString();

                worker = new DefaultWFSWorker("test1");
                worker.setLogLevel(Level.FINER);
                worker.setServiceUrl("http://geomatys.com/constellation/WS/");
                worker.setShiroAccessible(false);
                initialized = true;
            } catch (Exception ex) {
                Logger.getAnonymousLogger().log(Level.SEVERE, "error while initializing test", ex);
            }
        }
    }

    public static void initAllTypes() {

        alltypes.add(new QName("http://www.opengis.net/gml/3.2","BuildingCenters"));
        alltypes.add(new QName("http://www.opengis.net/gml/3.2","BasicPolygons"));
        if (mdweb_active) alltypes.add(new QName("http://www.opengis.net/sml/1.0","System"));
        alltypes.add(new QName("http://www.opengis.net/gml/3.2","Bridges"));
        alltypes.add(new QName("http://www.opengis.net/gml/3.2","Streams"));
        if (mdweb_active) alltypes.add(new QName("http://www.opengis.net/sml/1.0","Component"));
        if (mdweb_active) alltypes.add(new QName("http://www.opengis.net/sml/1.0","DataSourceType"));
        alltypes.add(new QName("http://www.opengis.net/sampling/1.0","SamplingPoint"));
        alltypes.add(new QName("http://www.opengis.net/gml/3.2","Lakes"));
        alltypes.add(new QName("http://www.opengis.net/gml/3.2","NamedPlaces"));
        alltypes.add(new QName("http://www.opengis.net/gml/3.2","Buildings"));
        alltypes.add(new QName("http://www.opengis.net/gml/3.2","RoadSegments"));
        alltypes.add(new QName("http://www.opengis.net/gml/3.2","DividedRoutes"));
        alltypes.add(new QName("http://www.opengis.net/gml/3.2","Forests"));
        alltypes.add(new QName("http://www.opengis.net/gml/3.2","MapNeatline"));
        if (mdweb_active) alltypes.add(new QName("http://www.opengis.net/sml/1.0","ProcessModel"));
        if (mdweb_active) alltypes.add(new QName("http://www.opengis.net/sml/1.0","ProcessChain"));
        alltypes.add(new QName("http://www.opengis.net/gml/3.2","Ponds"));
        Collections.sort(alltypes, new QNameComparator());

    }
    @AfterClass
    public static void tearDownClass() throws Exception {
        try {
            final ILayerBusiness layerBean = SpringHelper.getBean(ILayerBusiness.class);
            if (layerBean != null) {
                layerBean.removeAll();
            }
            final IServiceBusiness service = SpringHelper.getBean(IServiceBusiness.class);
            if (service != null) {
                service.deleteAll();
            }
            final IDataBusiness dataBean = SpringHelper.getBean(IDataBusiness.class);
            if (dataBean != null) {
                dataBean.deleteAll();
            }
            final IProviderBusiness provider = SpringHelper.getBean(IProviderBusiness.class);
            if (provider != null) {
                provider.removeAll();
            }
        } catch (ConfigurationException ex) {
            Logger.getAnonymousLogger().log(Level.WARNING, ex.getMessage());
        }
        ConfigDirectory.shutdownTestEnvironement("WFS2WorkerTest");
        if (ds != null) {
            ds.shutdown();
        }
        if (ds2 != null) {
            ds2.shutdown();
        }

        if (worker != null) {
            worker.destroy();
        }

        File derbyLog = new File("derby.log");
        if (derbyLog.exists()) {
            derbyLog.delete();
        }
    }

    @Before
    public void setUp() throws Exception {
        featureWriter = new JAXPStreamFeatureWriter("3.2.1", "2.0.0", new HashMap<String, String>());
    }

    @After
    public void tearDown() throws Exception {
    }


    /**
     * test the feature marshall
     *
     */
    @Test
    @Order(order=1)
    public void getCapabilitiesTest() throws Exception {
        final Marshaller marshaller = pool.acquireMarshaller();

        org.geotoolkit.ows.xml.v110.AcceptVersionsType acceptVersion = new org.geotoolkit.ows.xml.v110.AcceptVersionsType("2.0.0");
        org.geotoolkit.ows.xml.v110.SectionsType sections       = new org.geotoolkit.ows.xml.v110.SectionsType("featureTypeList");
        org.geotoolkit.wfs.xml.v200.GetCapabilitiesType request = new  org.geotoolkit.wfs.xml.v200.GetCapabilitiesType(acceptVersion, sections, null, null, "WFS");

        WFSCapabilities result = worker.getCapabilities(request);


        StringWriter sw = new StringWriter();
        marshaller.marshal(result, sw);

        if (mdweb_active) {
            domCompare(FileUtilities.getFileFromResource("org.constellation.wfs.xml.WFSCapabilities2-0-0-ftl-mdw.xml"),
                sw.toString());
        } else {
            domCompare(FileUtilities.getFileFromResource("org.constellation.wfs.xml.WFSCapabilities2-0-0-ftl.xml"),
                sw.toString());
        }


        request = new org.geotoolkit.wfs.xml.v200.GetCapabilitiesType();
        request.setAcceptVersions(new org.geotoolkit.ows.xml.v110.AcceptVersionsType("2.0.0"));
        result = worker.getCapabilities(request);

        sw = new StringWriter();
        marshaller.marshal(result, sw);

        if (mdweb_active) {
            domCompare(FileUtilities.getFileFromResource("org.constellation.wfs.xml.WFSCapabilities2-0-0-mdw.xml"),
                    sw.toString());
        } else {
            domCompare(FileUtilities.getFileFromResource("org.constellation.wfs.xml.WFSCapabilities2-0-0.xml"),
                    sw.toString());
        }

        acceptVersion = new org.geotoolkit.ows.xml.v110.AcceptVersionsType("2.3.0");
        request = new  org.geotoolkit.wfs.xml.v200.GetCapabilitiesType(acceptVersion, null, null, null, "WFS");

        try {
            worker.getCapabilities(request);
            fail("Should have raised an error.");
        } catch (CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), VERSION_NEGOTIATION_FAILED);
            assertEquals(ex.getLocator(), "version");
        }

        request = new org.geotoolkit.wfs.xml.v200.GetCapabilitiesType(acceptVersion, null, null, null, "WPS");

        try {
            worker.getCapabilities(request);
            fail("Should have raised an error.");
        } catch (CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "service");
        }

        request = new org.geotoolkit.wfs.xml.v200.GetCapabilitiesType();
        request.setService(null);

        try {
            worker.getCapabilities(request);
            fail("Should have raised an error.");
        } catch (CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), MISSING_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "service");
        }


        acceptVersion = new org.geotoolkit.ows.xml.v110.AcceptVersionsType("2.0.0");
        sections      = new org.geotoolkit.ows.xml.v110.SectionsType("operationsMetadata");
        request       = new  org.geotoolkit.wfs.xml.v200.GetCapabilitiesType(acceptVersion, sections, null, null, "WFS");

        result = worker.getCapabilities(request);


        sw = new StringWriter();
        marshaller.marshal(result, sw);
        domCompare(FileUtilities.getFileFromResource("org.constellation.wfs.xml.WFSCapabilities2-0-0-om.xml"),
                sw.toString());

        acceptVersion = new org.geotoolkit.ows.xml.v110.AcceptVersionsType("2.0.0");
        sections      = new org.geotoolkit.ows.xml.v110.SectionsType("serviceIdentification");
        request       = new  org.geotoolkit.wfs.xml.v200.GetCapabilitiesType(acceptVersion, sections, null, null, "WFS");

        result = worker.getCapabilities(request);


        sw = new StringWriter();
        marshaller.marshal(result, sw);
        domCompare(FileUtilities.getFileFromResource("org.constellation.wfs.xml.WFSCapabilities2-0-0-si.xml"),
                sw.toString());

        acceptVersion = new org.geotoolkit.ows.xml.v110.AcceptVersionsType("2.0.0");
        sections      = new org.geotoolkit.ows.xml.v110.SectionsType("serviceProvider");
        request       = new  org.geotoolkit.wfs.xml.v200.GetCapabilitiesType(acceptVersion, sections, null, null, "WFS");

        result = worker.getCapabilities(request);


        sw = new StringWriter();
        marshaller.marshal(result, sw);
        domCompare(FileUtilities.getFileFromResource("org.constellation.wfs.xml.WFSCapabilities2-0-0-sp.xml"),
                sw.toString());

        acceptVersion = new org.geotoolkit.ows.xml.v110.AcceptVersionsType("10.0.0","2.0.0","1.1.0");
        request       = new  org.geotoolkit.wfs.xml.v200.GetCapabilitiesType(acceptVersion, null, null, null, "WFS");

        result = worker.getCapabilities(request);


        sw = new StringWriter();
        marshaller.marshal(result, sw);
        domCompare(FileUtilities.getFileFromResource("org.constellation.wfs.xml.WFSCapabilities2-0-0.xml"),
                sw.toString());

        pool.recycle(marshaller);
    }

     /**
     * test the Getfeature operations with bad parameter causing exception return
     *
     */
    @Test
    @Order(order=2)
    public void getFeatureErrorTest() throws Exception {
        /**
         * Test 1 : empty query => error
         */
        Integer startIndex = null;
        GetFeatureType request = new GetFeatureType("WFS", "2.0.0", null, startIndex, Integer.MAX_VALUE, null, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.2.1");

        Object result = null;
        try {
            result = worker.getFeature(request);
            fail("Should have raised an error.");
        } catch (CstlServiceException ex) {
            //ok
        }

        /**
         * Test 2 : bad version => error
         */
        request = new GetFeatureType("WFS", "1.2.0", null, startIndex, Integer.MAX_VALUE, null, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.2.1");

        try {
            result = worker.getFeature(request);
            fail("Should have raised an error.");
        } catch (CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "version");
        }
    }

     /**
     * test the feature marshall
     *
     */
    @Test
    @Order(order=3)
    public void getFeatureOMTest() throws Exception {

        /**
         * Test 1 : query on typeName samplingPoint
         */
        List<QueryType> queries = new ArrayList<>();
        queries.add(new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null));
        GetFeatureType request = new GetFeatureType("WFS", "2.0.0", null, null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.2.1");

        Object result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        FeatureCollectionWrapper wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();
        assertEquals("3.2.1", wrapper.getGmlVersion());

        StringWriter writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer, 6);

        String expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.samplingPointCollection-3v2.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);

        String sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");
        domCompare(expectedResult, sresult);

        /**
         * Test 2 : query on typeName samplingPoint whith HITS result type
         */
        queries = new ArrayList<>();
        QueryType query = new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null);
        queries.add(query);
        request = new GetFeatureType("WFS", "2.0.0", null, null, Integer.MAX_VALUE, queries, ResultTypeType.HITS, "text/xml; subtype=gml/3.2.1");

        FeatureCollectionType resultHits = (FeatureCollectionType) worker.getFeature(request);

        assertTrue("results:" + resultHits, resultHits.getNumberReturned() == 6);


        /**
         * Test 3 : query on typeName samplingPoint with propertyName = {gml:name}
         */
        queries = new ArrayList<>();
        query = new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null);
        query.getAbstractProjectionClause().add(wfsFactory.createPropertyName(new PropertyName(new QName("http://www.opengis.net/gml/3.2", "name"))));

        queries.add(query);
        request = new GetFeatureType("WFS", "2.0.0", null, null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.2.1");

        result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();
        assertEquals("3.2.1", wrapper.getGmlVersion());

        writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.samplingPointCollection-5v2.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);

        sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");

        domCompare(expectedResult, sresult);

        /**
         * Test 4 : query on typeName samplingPoint whith a filter name = 10972X0137-PONT
         */
        queries = new ArrayList<>();
        ComparisonOpsType pe = new PropertyIsEqualToType(new LiteralType("10972X0137-PONT"), "name", Boolean.TRUE);
        FilterType filter = new FilterType(pe);
        queries.add(new QueryType(filter, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null));
        request = new GetFeatureType("WFS", "2.0.0", null, null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.2.1");

        result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();
        assertEquals("3.2.1", wrapper.getGmlVersion());

        writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.samplingPointCollection-4v2.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);

        sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");

        domCompare(expectedResult, sresult);

        /**
         * Test 5 : query on typeName samplingPoint whith a filter xpath //gml:name = 10972X0137-PONT
         */
        queries = new ArrayList<>();
        pe = new PropertyIsEqualToType(new LiteralType("10972X0137-PONT"), "//{http://www.opengis.net/gml}name", Boolean.TRUE);
        filter = new FilterType(pe);
        queries.add(new QueryType(filter, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null));
        request = new GetFeatureType("WFS", "2.0.0", null, null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.2.1");

        result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();
        assertEquals("3.2.1", wrapper.getGmlVersion());

        writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.samplingPointCollection-4v2.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);

        sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");

        domCompare(expectedResult, sresult);

        /**
         * Test 6 : query on typeName samplingPoint whith a spatial filter BBOX
         */
        queries = new ArrayList<>();
        SpatialOpsType bbox = new BBOXType("{http://www.opengis.net/sampling/1.0}position", 65300.0, 1731360.0, 65500.0, 1731400.0, "urn:ogc:def:crs:epsg:7.6:27582");
        filter = new FilterType(bbox);
        queries.add(new QueryType(filter, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null));
        request = new GetFeatureType("WFS", "2.0.0", null, null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.2.1");

       result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();
        assertEquals("3.2.1", wrapper.getGmlVersion());

        writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.samplingPointCollection-8v2.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);

        sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");

        domCompare(expectedResult, sresult);

        /**
         * Test 7 : query on typeName samplingPoint whith a spatial filter BBOX () with no namespace
         */
        queries = new ArrayList<>();
        bbox = new BBOXType("position", 65300.0, 1731360.0, 65500.0, 1731400.0, "urn:ogc:def:crs:epsg:7.6:27582");
        filter = new FilterType(bbox);
        queries.add(new QueryType(filter, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null));
        request = new GetFeatureType("WFS", "2.0.0", null, null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.2.1");

        result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();
        assertEquals("3.2.1", wrapper.getGmlVersion());

        writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.samplingPointCollection-8v2.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);

        sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");

        domCompare(expectedResult, sresult);


        /**
         * Test 8 : query on typeName samplingPoint with sort on gml:name
         */

        queries = new ArrayList<>();
        query = new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null);
        query.setAbstractSortingClause(ogcFactory.createSortBy(new SortByType(Arrays.asList(new SortPropertyType("http://www.opengis.net/gml:name", SortOrderType.ASC)))));
        queries.add(query);
        request = new GetFeatureType("WFS", "2.0.0", null, null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.2.1");

        result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();
        assertEquals("3.2.1", wrapper.getGmlVersion());

        writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.samplingPointCollection-6v2.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);

        sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");

        domCompare(expectedResult, sresult);

        /**
         * Test 9 : query on typeName samplingPoint with sort on gml:name
         */
        queries = new ArrayList<>();
        query = new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null);
        query.setAbstractSortingClause(ogcFactory.createSortBy(new SortByType(Arrays.asList(new SortPropertyType("http://www.opengis.net/gml:name", SortOrderType.DESC)))));
        queries.add(query);
        request = new GetFeatureType("WFS", "2.0.0", null, null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.2.1");

        result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();
        assertEquals("3.2.1", wrapper.getGmlVersion());

        writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.samplingPointCollection-7v2.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);

        sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");

        domCompare(expectedResult, sresult);

        /**
         * Test 10 : query on typeName samplingPoint with sort on gml:name and startIndex and maxFeature
         */
        queries = new ArrayList<>();
        query = new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null);
        query.setAbstractSortingClause(ogcFactory.createSortBy(new SortByType(Arrays.asList(new SortPropertyType("http://www.opengis.net/gml:name", SortOrderType.DESC)))));
        queries.add(query);
        request = new GetFeatureType("WFS", "2.0.0", null, null, 2, queries, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.2.1");
        request.setStartIndex(2);
        result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();
        assertEquals("3.2.1", wrapper.getGmlVersion());

        writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.samplingPointCollection-9v2.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);

        sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");

        domCompare(expectedResult, sresult);

        /**
         * Test 11 : query on typeName samplingPoint whith HITS result type
         */
        queries = new ArrayList<>();
        query = new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null);
        queries.add(query);
        request = new GetFeatureType("WFS", "2.0.0", null, null, Integer.MAX_VALUE, queries, ResultTypeType.HITS, "text/xml; subtype=gml/3.2.1");

        resultHits = (FeatureCollectionType) worker.getFeature(request);

        assertTrue(resultHits.getNumberReturned() == 6);


        /**
         * Test 12 : query on typeName samplingPoint whith a filter with unexpected property
         */

        queries = new ArrayList<>();
        pe = new PropertyIsEqualToType(new LiteralType("whatever"), "wrongProperty", Boolean.TRUE);
        filter = new FilterType(pe);
        queries.add(new QueryType(filter, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null));
        request = new GetFeatureType("WFS", "2.0.0", null, null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.2.1");

        try {
            worker.getFeature(request);
            fail("Should have raised an error.");
        } catch (CstlServiceException ex) {
            //ok
        }

        /**
         * Test 13 : query on typeName samplingPoint whith a an unexpected property in propertyNames
         */

        queries = new ArrayList<>();
        query = new QueryType(filter, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null);
        query.getAbstractProjectionClause().add(wfsFactory.createPropertyName(new PropertyName(new QName("wrongProperty"))));
        queries.add(query);
        request = new GetFeatureType("WFS", "2.0.0", null, null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.2.1");

        try {
            worker.getFeature(request);
            fail("Should have raised an error.");
        } catch (CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
        }
    }

     /**
     * test the feature marshall
     *
     */
    @Test
    @Order(order=4)
    public void getPropertyValueOMTest() throws Exception {

        /**
         * Test 1 : query on typeName samplingPoint with HITS
         */
        QueryType query = new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null);
        String valueReference = "sampledFeature";
        GetPropertyValueType request = new GetPropertyValueType("WFS", "2.0.0", null, null, Integer.MAX_VALUE, query, ResultTypeType.HITS, "text/xml; subtype=gml/3.2.1", valueReference);

        Object result = worker.getPropertyValue(request);

        assertTrue(result instanceof ValueCollection);
        assertEquals(6, ((ValueCollection)result).getNumberReturned());

        /**
         * Test 2 : query on typeName samplingPoint with RESULTS
         */
        request.setResultType(ResultTypeType.RESULTS);
        result = worker.getPropertyValue(request);

        assertTrue(result instanceof ValueCollectionWrapper);
        ValueCollectionWrapper wrapper = (ValueCollectionWrapper) result;
        result = wrapper.getFeatureCollection();
        assertEquals("3.2.1", wrapper.getGmlVersion());

        valueWriter   = new JAXPStreamValueCollectionWriter(valueReference);

        StringWriter writer = new StringWriter();
        valueWriter.write((FeatureCollection)result,writer);

        String expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.ValueCollectionOM1.xml"));
        domCompare(expectedResult, writer.toString());

        /**
         * Test 3 : query on typeName samplingPoint with RESULTS
         */
        valueReference = "position";
        request.setValueReference(valueReference);
        result = worker.getPropertyValue(request);

        assertTrue(result instanceof ValueCollectionWrapper);
        wrapper = (ValueCollectionWrapper) result;
        result = wrapper.getFeatureCollection();
        assertEquals("3.2.1", wrapper.getGmlVersion());

        valueWriter   = new JAXPStreamValueCollectionWriter(valueReference);

        writer = new StringWriter();
        valueWriter.write((FeatureCollection)result,writer);

        expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.ValueCollectionOM2.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);
        domCompare(expectedResult, writer.toString());

        /**
         * Test 4 : empty value reference
         */
        valueReference = "";
        request.setValueReference(valueReference);

        boolean exLaunched = false;
        try {
            worker.getPropertyValue(request);
        } catch (CstlServiceException ex) {
            exLaunched = true;
        }

        assertTrue(exLaunched);
    }

    /**
     * test the feature marshall
     *
     */
    @Test
    @Order(order=5)
    public void getPropertyValueSMLTest() throws Exception {

        if (!mdweb_active) return;

        /**
         * Test 1 : query on typeName System with HITS
         */
        QueryType query = new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/sml/1.0", "System")), null);
        String valueReference = "inputs";
        GetPropertyValueType request = new GetPropertyValueType("WFS", "2.0.0", null, null, Integer.MAX_VALUE, query, ResultTypeType.HITS, "text/xml; subtype=gml/3.2.1", valueReference);

        Object result = worker.getPropertyValue(request);

        assertTrue(result instanceof ValueCollection);
        assertEquals(3, ((ValueCollection)result).getNumberReturned());

        /**
         * Test 2 : query on typeName System with RESULTS
         */
        request.setResultType(ResultTypeType.RESULTS);
        result = worker.getPropertyValue(request);

        assertTrue(result instanceof ValueCollectionWrapper);
        ValueCollectionWrapper wrapper = (ValueCollectionWrapper) result;
        result = wrapper.getFeatureCollection();
        assertEquals("3.2.1", wrapper.getGmlVersion());

        valueWriter   = new JAXPStreamValueCollectionWriter(valueReference);

        StringWriter writer = new StringWriter();
        valueWriter.write((FeatureCollection)result,writer);

        String expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.ValueCollectionSML1.xml"));
        domCompare(expectedResult, writer.toString());

        /**
         * Test 3 : query on typeName System with RESULTS
         */
        valueReference = "keywords";
        request.setValueReference(valueReference);
        result = worker.getPropertyValue(request);

        assertTrue(result instanceof ValueCollectionWrapper);
        wrapper = (ValueCollectionWrapper) result;
        result = wrapper.getFeatureCollection();
        assertEquals("3.2.1", wrapper.getGmlVersion());

        valueWriter   = new JAXPStreamValueCollectionWriter(valueReference);

        writer = new StringWriter();
        valueWriter.write((FeatureCollection)result,writer);

        expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.ValueCollectionSML2.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);
        domCompare(expectedResult, writer.toString());
    }

    /**
     * test the feature marshall
     *
     */
    @Test
    @Order(order=6)
    public void getFeatureSMLTest() throws Exception {

        if (!mdweb_active) return;

        /**
         * Test 1 : query on typeName sml:System
         */

        List<QueryType> queries = new ArrayList<>();
        queries.add(new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/sml/1.0", "System")), null));
        GetFeatureType request = new GetFeatureType("WFS", "2.0.0", null, null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.2.1");

        Object result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        FeatureCollectionWrapper wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();
        assertEquals("3.2.1", wrapper.getGmlVersion());

        StringWriter writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        String expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.systemCollection-3v2.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);

        String sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");

        domCompare(expectedResult, sresult);

        /**
         * Test 2 : query on typeName sml:System avec srsName = EPSG:4326
         */

        queries = new ArrayList<>();
        QueryType query = new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/sml/1.0", "System")), null);
        query.setSrsName("EPSG:4326");
        queries.add(query);
        request = new GetFeatureType("WFS", "2.0.0", null, null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.2.1");

        result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();
        assertEquals("3.2.1", wrapper.getGmlVersion());

        writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.systemCollection-3v2.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);

        sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");

        domCompare(expectedResult, sresult);

        /**
         * Test 3 : query on typeName sml:System with propertyName = {sml:keywords, sml:phenomenons}
         */
        queries = new ArrayList<>();
        query   = new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/sml/1.0", "System")), null);
        query.getAbstractProjectionClause().add(wfsFactory.createPropertyName(new PropertyName(new QName("http://www.opengis.net/sml/1.0", "keywords"))));
        query.getAbstractProjectionClause().add(wfsFactory.createPropertyName(new PropertyName(new QName("http://www.opengis.net/sml/1.0", "phenomenons"))));
        queries.add(query);

        request = new GetFeatureType("WFS", "2.0.0", null, null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, null);

        result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();
        assertEquals("3.2.1", wrapper.getGmlVersion());

        writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.systemCollection-2v2.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);

        sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");

        domCompare(expectedResult, sresult);

        /**
         * Test 4 : query on typeName sml:System filter on name = 'Piezometer Test'
         */
        ComparisonOpsType pe1     = new PropertyIsEqualToType(new LiteralType("Piezometer Test"), "name", Boolean.TRUE);
        FilterType filter         = new FilterType(pe1);
        queries = new ArrayList<>();
        query   = new QueryType(filter, Arrays.asList(new QName("http://www.opengis.net/sml/1.0", "System")), null);

        queries.add(query);

        request = new GetFeatureType("WFS", "2.0.0", null, null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, null);

        result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();
        assertEquals("3.2.1", wrapper.getGmlVersion());

        writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.systemCollection-4v2.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);

        sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");

        domCompare(expectedResult, sresult);

        /**
         * Test 5 : same test xpath style
         */
        pe1     = new PropertyIsEqualToType(new LiteralType("Piezometer Test"), "/name", Boolean.TRUE);
        filter         = new FilterType(pe1);
        queries = new ArrayList<>();
        query   = new QueryType(filter, Arrays.asList(new QName("http://www.opengis.net/sml/1.0", "System")), null);

        queries.add(query);

        request = new GetFeatureType("WFS", "2.0.0", null, null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, null);

        result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();
        assertEquals("3.2.1", wrapper.getGmlVersion());

        writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.systemCollection-4v2.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);

        sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");

        domCompare(expectedResult, sresult);

        /**
         * Test 6 : query on typeName sml:System filter on input name = 'height' (xpath style)
         */
        pe1     = new PropertyIsEqualToType(new LiteralType("height"), "/inputs/input/name", Boolean.TRUE);
        filter         = new FilterType(pe1);
        queries = new ArrayList<>();
        query   = new QueryType(filter, Arrays.asList(new QName("http://www.opengis.net/sml/1.0", "System")), null);

        queries.add(query);

        request = new GetFeatureType("WFS", "2.0.0", null, null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, null);

        result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();
        assertEquals("3.2.1", wrapper.getGmlVersion());

        writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.systemCollection-5v2.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);

        sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");

        domCompare(expectedResult, sresult);

        /**
         * Test 7 : query on typeName sml:System with bad xpath NOT WORKING

        pe1     = new PropertyIsEqualToType(new LiteralType("height"), "/inputs/inputation/namein", Boolean.TRUE);
        filter         = new FilterType(pe1);
        queries = new ArrayList<>();
        query   = new QueryType(filter, Arrays.asList(new QName("http://www.opengis.net/sml/1.0", "System")), null);

        queries.add(query);

        request = new GetFeatureType("WFS", "2.0.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, null);

        result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();
        assertEquals("3.2.1", wrapper.getGmlVersion());

        writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.systemCollection-5v2.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);

        domCompare(expectedResult, writer.toString());
         */

        /**
         * Test 8 : query on typeName sml:System filter on input name = 'height' (xpath style) prefixed with featureType name
         */
        pe1     = new PropertyIsEqualToType(new LiteralType("height"), "{http://www.opengis.net/sml/1.0}System/inputs/input/name", Boolean.TRUE);
        filter         = new FilterType(pe1);
        queries = new ArrayList<>();
        query   = new QueryType(filter, Arrays.asList(new QName("http://www.opengis.net/sml/1.0", "System")), null);

        queries.add(query);

        request = new GetFeatureType("WFS", "2.0.0", null, null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, null);

        result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();
        assertEquals("3.2.1", wrapper.getGmlVersion());

        writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.systemCollection-5v2.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);

        sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");

        domCompare(expectedResult, sresult);

         /**
         * Test 8 : query on typeName sml:System aliased as "a" filter on input name = 'height' (xpath style) prefixed with featureType name
         */
        pe1     = new PropertyIsEqualToType(new LiteralType("height"), "a/inputs/input/name", Boolean.TRUE);
        filter         = new FilterType(pe1);
        queries = new ArrayList<>();
        query   = new QueryType(filter, Arrays.asList(new QName("http://www.opengis.net/sml/1.0", "System")), null);
        query.setAliases(Arrays.asList("a"));

        queries.add(query);

        request = new GetFeatureType("WFS", "2.0.0", null, null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, null);

        result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();
        assertEquals("3.2.1", wrapper.getGmlVersion());

        writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.systemCollection-5v2.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);

        sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");

        domCompare(expectedResult, sresult);
    }

     /**
     * test the feature marshall
     *
     */
    @Ignore
    @Order(order=7)
    public void getFeatureSelfJoinTest() throws Exception {

        /**
         * Test 1 : query on typeName sml:System
         */

        ComparisonOpsType pe1     = new PropertyIsEqualToType(new LiteralType("Piezometer Test"), "a/name", Boolean.TRUE);
        PropertyIsEqualToType pe2 = new PropertyIsEqualToType();
        pe2.addValueReference("a/smlref");
        pe2.addValueReference("b/name");
        LogicOpsType le           = new AndType(pe1, pe2);
        FilterType filter         = new FilterType(le);

        List<QueryType> queries = new ArrayList<>();
        QueryType selfJoinQuery = new QueryType(filter, Arrays.asList(new QName("http://www.opengis.net/sml/1.0", "System"), new QName("http://www.opengis.net/sml/1.0", "System")), null);
        selfJoinQuery.getAliases().add("a");
        selfJoinQuery.getAliases().add("b");
        queries.add(selfJoinQuery);
        GetFeatureType request = new GetFeatureType("WFS", "2.0.0", null, null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.2.1");

        Object result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        FeatureCollectionWrapper wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();
        assertEquals("3.2.1", wrapper.getGmlVersion());

        StringWriter writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        String expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.systemCollectionSelfJoin.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);

        domCompare(expectedResult, writer.toString());
    }

    /**
     * test the feature marshall
     *
     */
    @Ignore
    @Order(order=8)
    public void getFeatureJoinTest() throws Exception {

        /**
         * Test 1 : query on typeName sml:System
         */

        ComparisonOpsType pe1     = new PropertyIsEqualToType(new LiteralType("Piezometer Test"), "{http://www.opengis.net/sml/1.0}System/name", Boolean.TRUE);
        PropertyIsEqualToType pe2 = new PropertyIsEqualToType();
        pe2.addValueReference("{http://www.opengis.net/sml/1.0}System/smlref");
        pe2.addValueReference("{http://www.opengis.net/sml/1.0}System/name");
        LogicOpsType le           = new AndType(pe1, pe2);
        FilterType filter         = new FilterType(le);

        List<QueryType> queries = new ArrayList<>();
        QueryType selfJoinQuery = new QueryType(filter, Arrays.asList(new QName("http://www.opengis.net/sml/1.0", "System"), new QName("http://www.opengis.net/sml/1.0", "System")), null);
        selfJoinQuery.getAliases().add("a");
        selfJoinQuery.getAliases().add("b");
        queries.add(selfJoinQuery);
        GetFeatureType request = new GetFeatureType("WFS", "2.0.0", null, null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.2.1");

        Object result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        FeatureCollectionWrapper wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();
        assertEquals("3.2.1", wrapper.getGmlVersion());

        StringWriter writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        String expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.systemCollectionSelfJoin.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);

        domCompare(expectedResult, writer.toString());
    }

    /**
     * test the feature marshall
     *
     */
    @Test
    @Order(order=9)
    public void getFeatureShapeFileTest() throws Exception {

        /**
         * Test 1 : query on typeName bridges
         */

        List<QueryType> queries = new ArrayList<>();
        queries.add(new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/gml/3.2", "Bridges")), null));
        GetFeatureType request = new GetFeatureType("WFS", "2.0.0", null, null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.2.1");

        Object result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        FeatureCollectionWrapper wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();
        assertEquals("3.2.1", wrapper.getGmlVersion());

        StringWriter writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        String sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");

        domCompare(
                FileUtilities.getFileFromResource("org.constellation.wfs.xml.bridgeCollectionv2.xml"),
                sresult);

        /**
         * Test 2 : query on typeName bridges with propertyName = {FID}
         */
        queries = new ArrayList<>();
        QueryType query = new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/gml/3.2", "Bridges")), null);
        query.getAbstractProjectionClause().add(wfsFactory.createPropertyName(new PropertyName(new QName("FID"))));
        queries.add(query);
        request = new GetFeatureType("WFS", "2.0.0", null, null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.2.1");

        result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();
        assertEquals("3.2.1", wrapper.getGmlVersion());

        writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");

        domCompare(
                FileUtilities.getFileFromResource("org.constellation.wfs.xml.bridgeCollection-2v2.xml"),
                sresult);

        /**
         * Test 3 : query on typeName NamedPlaces
         */

        queries = new ArrayList<>();
        queries.add(new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/gml/3.2", "NamedPlaces")), null));
        request = new GetFeatureType("WFS", "2.0.0", null, null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.2.1");

        result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();
        assertEquals("3.2.1", wrapper.getGmlVersion());

        writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");

        domCompare(
                FileUtilities.getFileFromResource("org.constellation.wfs.xml.namedPlacesCollection-1v2.xml"),
                sresult);

        /**
         * Test 4 : query on typeName NamedPlaces with resultType = HITS
         */

        queries = new ArrayList<>();
        queries.add(new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/gml/3.2", "NamedPlaces")), null));
        request = new GetFeatureType("WFS", "2.0.0", null, null, Integer.MAX_VALUE, queries, ResultTypeType.HITS, "text/xml; subtype=gml/3.2.1");

        result = worker.getFeature(request);

        FeatureCollectionType resultHits = (FeatureCollectionType)result;

        assertTrue(resultHits.getNumberReturned() == 2);

        /**
         * Test 5 : query on typeName NamedPlaces with srsName = EPSG:27582
         */

        queries = new ArrayList<>();
        query = new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/gml/3.2", "NamedPlaces")), null);
        query.setSrsName("EPSG:27582");
        queries.add(query);
        request = new GetFeatureType("WFS", "2.0.0", null, null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.2.1");

        result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();
        assertEquals("3.2.1", wrapper.getGmlVersion());

        writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");

        domCompare(
                FileUtilities.getFileFromResource("org.constellation.wfs.xml.namedPlacesCollection-1_reprojv2.xml"),
                sresult);

        /**
         * Test 6 : query on typeName NamedPlaces with DESC sortBy on NAME property (not supported)
         */

        queries = new ArrayList<>();
        query = new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/gml/3.2", "NamedPlaces")), null);
        query.setAbstractSortingClause(ogcFactory.createSortBy(new SortByType(Arrays.asList(new SortPropertyType("NAME", SortOrderType.DESC)))));
        queries.add(query);
        request = new GetFeatureType("WFS", "2.0.0", null, null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.2.1");


        result = worker.getFeature(request);
        assertTrue(result instanceof FeatureCollectionWrapper);
        wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();
        assertEquals("3.2.1", wrapper.getGmlVersion());

        writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");

        domCompare(
                FileUtilities.getFileFromResource("org.constellation.wfs.xml.namedPlacesCollection-5v2.xml"),
                sresult);



        /**
         * Test 7 : query on typeName NamedPlaces with ASC sortBy on NAME property (not supported)
         */
        queries = new ArrayList<>();
        query = new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/gml/3.2", "NamedPlaces")), null);
        query.setAbstractSortingClause(ogcFactory.createSortBy(new SortByType(Arrays.asList(new SortPropertyType("NAME", SortOrderType.ASC)))));
        queries.add(query);
        request = new GetFeatureType("WFS", "2.0.0", null, null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.2.1");

        result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();
        assertEquals("3.2.1", wrapper.getGmlVersion());

        writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");

        domCompare(
                FileUtilities.getFileFromResource("org.constellation.wfs.xml.namedPlacesCollection-1v2.xml"),
                sresult);

    }

    /**
     *
     *
     */
    @Test
    @Order(order=10)
    public void DescribeFeatureTest() throws Exception {
        Unmarshaller unmarshaller = XSDMarshallerPool.getInstance().acquireUnmarshaller();

        /**
         * Test 1 : describe Feature type bridges
         */
        List<QName> typeNames = new ArrayList<>();
        typeNames.add(new QName("http://www.opengis.net/gml/3.2", "Bridges"));
        DescribeFeatureTypeType request = new DescribeFeatureTypeType("WFS", "2.0.0", null, typeNames, "text/xml; subtype=gml/3.2.1");

        Schema result = (Schema) worker.describeFeatureType(request);

        Schema expResult = (Schema) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/wfs/xsd/bridge2.xsd"));
        // fix for equlity on empty list / null list
        for (ComplexType type : expResult.getComplexTypes()) {
            type.getAttributeOrAttributeGroup();
        }
        assertEquals(expResult, result);

        /**
         * Test 2 : describe Feature type Sampling point
         */
        typeNames = new ArrayList<>();
        typeNames.add(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint"));
        request = new DescribeFeatureTypeType("WFS", "2.0.0", null, typeNames, "text/xml; subtype=gml/3.2.1");

        result = (Schema) worker.describeFeatureType(request);

        expResult = (Schema) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/wfs/xsd/sampling2.xsd"));
        // fix for equlity on empty list / null list
        for (ComplexType type : expResult.getComplexTypes()) {
            type.getAttributeOrAttributeGroup();
        }
        assertEquals(expResult, result);

        /**
         * Test 3 : describe Feature type System
         */
        if (mdweb_active) {
            typeNames = new ArrayList<>();
            typeNames.add(new QName("http://www.opengis.net/sml/1.0", "System"));
            request = new DescribeFeatureTypeType("WFS", "2.0.0", null, typeNames, "text/xml; subtype=gml/3.2.1");

            result = (Schema) worker.describeFeatureType(request);

            expResult = (Schema) unmarshaller.unmarshal(Util.getResourceAsStream("org/constellation/wfs/xsd/system2.xsd"));

            assertEquals(expResult.getElements().size(), result.getElements().size());
            for (int i = 0; i < expResult.getElements().size(); i++) {
                TopLevelElement expElem = expResult.getElements().get(i);
                TopLevelElement resElem = result.getElements().get(i);
                assertEquals(expElem, resElem);
            }
            assertEquals(expResult.getComplexTypes().size(), result.getComplexTypes().size());
            for (int i = 0; i < expResult.getComplexTypes().size(); i++) {
                TopLevelComplexType expElem = expResult.getComplexTypes().get(i);
                TopLevelComplexType resElem = result.getComplexTypes().get(i);
                assertEquals(expElem, resElem);
            }
            assertEquals(expResult, result);
        }

        XSDMarshallerPool.getInstance().recycle(unmarshaller);
    }

    /**
     *
     *
     */
    @Test
    @Order(order=11)
    public void TransactionTest() throws Exception {

        /**
         * Test 1 : transaction update for Feature type bridges with a bad inputFormat
         */

        QName typeName = new QName("http://www.opengis.net/gml/3.2", "Bridges");
        List<PropertyType> properties = new ArrayList<>();
        UpdateType update = new UpdateType(null, properties, null, typeName, null);
        update.setInputFormat("bad inputFormat");
        TransactionType request = new TransactionType("WFS", "2.0.0", null, AllSomeType.ALL, update);


        try {
            worker.transaction(request);
            fail("Should have raised an error.");
        } catch (CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "inputFormat");
        }


        /**
         * Test 2 : transaction update for Feature type bridges with a bad property
         */

        typeName = new QName("http://www.opengis.net/gml/3.2", "Bridges");
        properties = new ArrayList<>();
        properties.add(new PropertyType(new ValueReference("whatever", UpdateActionType.REPLACE), "someValue"));
        request = new TransactionType("WFS", "2.0.0", null, AllSomeType.ALL, new UpdateType(null, properties, null, typeName, null));


        try {
            worker.transaction(request);
            fail("Should have raised an error.");
        } catch (CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), INVALID_VALUE);
            assertEquals(ex.getMessage(), "The feature Type {http://www.opengis.net/gml/3.2}Bridges does not has such a property: whatever");
        }


        /**
         * Test 3 : transaction update for Feature type bridges with a bad property in filter
         */

        typeName = new QName("http://www.opengis.net/gml/3.2", "Bridges");
        properties = new ArrayList<>();
        properties.add(new PropertyType(new ValueReference("NAME", UpdateActionType.REPLACE), "someValue"));
        ComparisonOpsType pe     = new PropertyIsEqualToType(new LiteralType("10972X0137-PONT"), "bad", Boolean.TRUE);
        FilterType filter        = new FilterType(pe);
        request = new TransactionType("WFS", "2.0.0", null, AllSomeType.ALL, new UpdateType(null, properties, filter, typeName, null));


        try {
            worker.transaction(request);
            fail("Should have raised an error.");
        } catch (CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getMessage(), "The feature Type {http://www.opengis.net/gml/3.2}Bridges does not has such a property: bad");
        }

        /**
         * Test 4 : transaction update for Feature type NamedPlaces with a property in filter
         */

        typeName = new QName("http://www.opengis.net/gml/3.2", "NamedPlaces");
        properties = new ArrayList<>();
        properties.add(new PropertyType(new ValueReference("FID", UpdateActionType.REPLACE), "999"));
        pe     = new PropertyIsEqualToType(new LiteralType("Ashton"), "NAME", Boolean.TRUE);
        filter = new FilterType(pe);
        request = new TransactionType("WFS", "2.0.0", null, AllSomeType.ALL, new UpdateType(null, properties, filter, typeName, null));


        TransactionResponse result = worker.transaction(request);

        TransactionSummaryType sum = new TransactionSummaryType(0, 1, 0, 0);
        TransactionResponseType ExpResult = new TransactionResponseType(sum, null, null, null, "2.0.0");

        assertEquals(ExpResult, result);

        /**
         * we verify that the feature have been updated
         */
         List<QueryType> queries = new ArrayList<>();
        queries.add(new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/gml/3.2", "NamedPlaces")), null));
        GetFeatureType requestGF = new GetFeatureType("WFS", "2.0.0", null, null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.2.1");

        Object resultGF = worker.getFeature(requestGF);

        assertTrue(resultGF instanceof FeatureCollectionWrapper);
        FeatureCollectionWrapper wrapper = (FeatureCollectionWrapper) resultGF;
        resultGF = wrapper.getFeatureCollection();
        assertEquals("3.2.1", wrapper.getGmlVersion());

        StringWriter writer = new StringWriter();
        featureWriter.write((FeatureCollection)resultGF,writer);

        String sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");

        domCompare(
                FileUtilities.getFileFromResource("org.constellation.wfs.xml.namedPlacesCollection-3v2.xml"),
                sresult);

   }

    @Test
    @Order(order=12)
    public void TransactionReplaceTest() throws Exception {



        /**
         * Test 1 : transaction replace for Feature type NamedPlaces
         */
        final GenericName tName = NamesExt.create("http://www.opengis.net/gml/3.2", "NamedPlaces");
        final FeatureType ft = ((FeatureData) DataProviders.getInstance().get(tName)).getStore().getFeatureType(tName);
        final JAXPStreamFeatureReader fr = new JAXPStreamFeatureReader(ft);
        fr.getProperties().put(JAXPStreamFeatureReader.BINDING_PACKAGE, "GML");
        final Feature feature = (Feature) fr.read(FileUtilities.getFileFromResource("org.constellation.wfs.xml.namedPlaces.xml"));

        PropertyIsEqualToType pe = new PropertyIsEqualToType(new LiteralType("Goose Island"), "NAME", Boolean.TRUE);
        FilterType filter   = new FilterType(pe);
        TransactionType request  = new TransactionType("WFS", "2.0.0", null, AllSomeType.ALL, new ReplaceType(null, filter, feature, "application/gml+xml; version=3.2", null));


        TransactionResponse result = worker.transaction(request);

        TransactionSummaryType sum = new TransactionSummaryType(0, 0, 0, 1);
        final List<CreatedOrModifiedFeatureType> r = new ArrayList<>();
        r.add(new CreatedOrModifiedFeatureType(new ResourceIdType("NamedPlaces.2"), null));
        ActionResultsType replaced = new ActionResultsType(r);
        TransactionResponse ExpResult = new TransactionResponseType(sum, null, null, replaced, "2.0.0");

        assertEquals(ExpResult, result);

        /**
         * we verify that the feature have been updated
         */
        List<QueryType> queries = new ArrayList<>();
        queries.add(new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/gml/3.2", "NamedPlaces")), null));
        GetFeatureType requestGF = new GetFeatureType("WFS", "2.0.0", null, null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.2.1");

        Object resultGF = worker.getFeature(requestGF);

        assertTrue(resultGF instanceof FeatureCollectionWrapper);
        FeatureCollectionWrapper wrapper = (FeatureCollectionWrapper) resultGF;
        resultGF = wrapper.getFeatureCollection();
        assertEquals("3.2.1", wrapper.getGmlVersion());

        StringWriter writer = new StringWriter();
        featureWriter.write((FeatureCollection)resultGF,writer);

        String sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");

        domCompare(
                FileUtilities.getFileFromResource("org.constellation.wfs.xml.namedPlacesCollection-4v2.xml"),
                sresult);

    }

    @Test
    @Order(order=13)
    public void TransactionDeleteTest() throws Exception {



        /**
         * Test 1 : transaction delete for Feature type bridges with a bad property in filter
         */
        QName typeName           = new QName("http://www.opengis.net/gml/3.2", "Bridges");
        PropertyIsEqualToType pe = new PropertyIsEqualToType(new LiteralType("10972X0137-PONT"), "bad", Boolean.TRUE);
        FilterType filter        = new FilterType(pe);
        DeleteType delete        = new DeleteType(filter, null, typeName);
        TransactionType request  = new TransactionType("WFS", "2.0.0", null, AllSomeType.ALL, delete);

        try {
            worker.transaction(request);
            fail("Should have raised an error.");
        } catch (CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getMessage(), "The feature Type {http://www.opengis.net/gml/3.2}Bridges does not has such a property: bad");
        }


        /**
         * Test 2 : transaction delete for Feature type NamedPlaces with a property in filter
         */
        typeName = new QName("http://www.opengis.net/gml/3.2", "NamedPlaces");
        pe       = new PropertyIsEqualToType(new LiteralType("Ashton"), "NAME", Boolean.TRUE);
        filter   = new FilterType(pe);
        delete   = new DeleteType(filter, null, typeName);
        request  = new TransactionType("WFS", "2.0.0", null, AllSomeType.ALL, delete);

        TransactionResponse result = worker.transaction(request);

        TransactionSummaryType sum = new TransactionSummaryType(0, 0, 1, 0);
        TransactionResponseType expresult = new TransactionResponseType(sum, null, null, null,"2.0.0");

        assertEquals(expresult, result);

        /**
         * we verify that the feature have been deleted
         */
        List<QueryType> queries = new ArrayList<>();
        queries.add(new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/gml/3.2", "NamedPlaces")), null));
        GetFeatureType requestGF = new GetFeatureType("WFS", "2.0.0", null, null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.2.1");

        Object resultGF = worker.getFeature(requestGF);

        assertTrue(resultGF instanceof FeatureCollectionWrapper);
        FeatureCollectionWrapper wrapper = (FeatureCollectionWrapper) resultGF;
        resultGF = wrapper.getFeatureCollection();

        StringWriter writer = new StringWriter();
        featureWriter.write((FeatureCollection)resultGF,writer);

        String sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");

        domCompare(
                FileUtilities.getFileFromResource("org.constellation.wfs.xml.namedPlacesCollection-2v2.xml"),
                sresult);
    }
    /**
     *
     *
     */
    @Test
    @Order(order=14)
    public void TransactionInsertTest() throws Exception {

        /**
         * Test 1 : transaction insert for Feature type bridges with a bad inputFormat
         */

        final QName typeName = new QName("http://www.opengis.net/gml/3.2", "Bridges");
        final InsertType insert = new InsertType();
        insert.setInputFormat("bad inputFormat");
        final TransactionType request = new TransactionType("WFS", "2.0.0", null, AllSomeType.ALL, insert);

        try {
            worker.transaction(request);
            fail("Should have raised an error.");
        } catch (CstlServiceException ex) {
            assertEquals(ex.getExceptionCode(), INVALID_PARAMETER_VALUE);
            assertEquals(ex.getLocator(), "inputFormat");
        }
    }

    /**
     *
     *
     */
    @Test
    @Order(order=14)
    public void listStoredQueriesTest() throws Exception {

        final ListStoredQueriesType request = new ListStoredQueriesType("WFS", "2.0.0", null);

        final ListStoredQueriesResponse resultI = worker.listStoredQueries(request);

        assertTrue(resultI instanceof ListStoredQueriesResponseType);
        final ListStoredQueriesResponseType result = (ListStoredQueriesResponseType) resultI;

        final List<StoredQueryListItemType> items = new ArrayList<>();
        items.add(new StoredQueryListItemType("nameQuery", Arrays.asList(new Title("Name query")), Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint"))));
        items.add(new StoredQueryListItemType("urn:ogc:def:query:OGC-WFS::GetFeatureById", Arrays.asList(new Title("Identifier query")), alltypes));
        items.add(new StoredQueryListItemType("urn:ogc:def:storedQuery:OGC-WFS::GetFeatureByType", Arrays.asList(new Title("By type query")), Arrays.asList(new QName(""))));
        final ListStoredQueriesResponseType expResult = new ListStoredQueriesResponseType(items);

        assertEquals(3, result.getStoredQuery().size());
        for (int i = 0; i < result.getStoredQuery().size(); i++) {
            final StoredQueryListItemType expIt = items.get(i);
            final StoredQueryListItemType resIt = result.getStoredQuery().get(i);
            assertEquals(expIt.getReturnFeatureType(), resIt.getReturnFeatureType());
            assertEquals(expIt, resIt);
        }
        assertEquals(expResult, result);

    }

    /**
     *
     *
     */
    @Test
    @Order(order=15)
    public void describeStoredQueriesTest() throws Exception {
        final DescribeStoredQueriesType request = new DescribeStoredQueriesType("WFS", "2.0.0", null, Arrays.asList("nameQuery"));
        final DescribeStoredQueriesResponse resultI = worker.describeStoredQueries(request);

        assertTrue(resultI instanceof DescribeStoredQueriesResponseType);
        final DescribeStoredQueriesResponseType result = (DescribeStoredQueriesResponseType) resultI;

        final List<StoredQueryDescriptionType> descriptions = new ArrayList<>();
        final ParameterExpressionType param = new ParameterExpressionType("name", "name Parameter", "A parameter on the name of the feature", new QName("http://www.w3.org/2001/XMLSchema", "string", "xs"));
        final List<QName> types = Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint"));
        final PropertyIsEqualToType pis = new PropertyIsEqualToType(new LiteralType("${name}"), "name", true);
        final FilterType filter = new FilterType(pis);
        final QueryType query = new QueryType(filter, types, "2.0.0");
        final QueryExpressionTextType queryEx = new QueryExpressionTextType("urn:ogc:def:queryLanguage:OGC-WFS::WFS_QueryExpression", null, types);
        final ObjectFactory factory = new ObjectFactory();
        queryEx.getContent().add(factory.createQuery(query));
        final StoredQueryDescriptionType des1 = new StoredQueryDescriptionType("nameQuery", "Name query" , "filter on name for samplingPoint", param, queryEx);
        descriptions.add(des1);
        final DescribeStoredQueriesResponseType expResult = new DescribeStoredQueriesResponseType(descriptions);

        assertEquals(1, result.getStoredQueryDescription().size());
        assertEquals(expResult.getStoredQueryDescription().get(0).getQueryExpressionText(), result.getStoredQueryDescription().get(0).getQueryExpressionText());
        assertEquals(expResult.getStoredQueryDescription().get(0), result.getStoredQueryDescription().get(0));
        assertEquals(expResult.getStoredQueryDescription(), result.getStoredQueryDescription());
        assertEquals(expResult, result);
    }

    /**
     *
     *
     */
    @Test
    @Order(order=16)
    public void createStoredQueriesTest() throws Exception {
        final List<StoredQueryDescriptionType> desc = new ArrayList<>();

        final ParameterExpressionType param = new ParameterExpressionType("name2", "name Parameter 2 ", "A parameter on the geometry \"the_geom\" of the feature", new QName("http://www.opengis.net/gml/3.2", "AbstractGeometryType", "gml"));
        final List<QName> types = Arrays.asList(new QName("http://www.opengis.net/gml/3.2", "Bridges"));
        final PropertyIsEqualToType pis = new PropertyIsEqualToType(new LiteralType("${geom}"), "the_geom", true);
        final FilterType filter = new FilterType(pis);
        final QueryType query = new QueryType(filter, types, "2.0.0");
        final QueryExpressionTextType queryEx = new QueryExpressionTextType("urn:ogc:def:queryLanguage:OGC-WFS::WFS_QueryExpression", null, types);
        final ObjectFactory factory = new ObjectFactory();
        queryEx.getContent().add(factory.createQuery(query));
        final StoredQueryDescriptionType desc1 = new StoredQueryDescriptionType("geomQuery", "Geom query" , "filter on geom for Bridge", param, queryEx);
        desc.add(desc1);

        final ParameterExpressionType envParam = new ParameterExpressionType("envelope", "envelope parameter", "A parameter on the geometry \"the_geom\" of the feature", new QName("http://www.opengis.net/gml/3.2", "EnvelopeType", "gml"));
        final List<QName> types2 = Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint"));
        final SpatialOpsType bbox = new BBOXType("{http://www.opengis.net/sampling/1.0}position", "${envelope}");
        final FilterType filter2 = new FilterType(bbox);
        final QueryType query2 = new QueryType(filter2, types2, "2.0.0");
        final QueryExpressionTextType queryEx2 = new QueryExpressionTextType("urn:ogc:def:queryLanguage:OGC-WFS::WFS_QueryExpression", null, types2);
        queryEx2.getContent().add(factory.createQuery(query2));
        final StoredQueryDescriptionType desc2 = new StoredQueryDescriptionType("envelopeQuery", "Envelope query" , "BBOX filter on geom for Sampling point", envParam, queryEx2);
        desc.add(desc2);




        final CreateStoredQueryType request = new CreateStoredQueryType("WFS", "2.0.0", null, desc);
        final CreateStoredQueryResponse resultI = worker.createStoredQuery(request);

        assertTrue(resultI instanceof CreateStoredQueryResponseType);
        final CreateStoredQueryResponseType result = (CreateStoredQueryResponseType) resultI;

        final CreateStoredQueryResponseType expResult =  new CreateStoredQueryResponseType("OK");
        assertEquals(expResult, result);

        /**
         * verify that thes queries are well stored
         */
        final ListStoredQueriesType requestlsq = new ListStoredQueriesType("WFS", "2.0.0", null);

        ListStoredQueriesResponse resultlsqI = worker.listStoredQueries(requestlsq);

        assertTrue(resultlsqI instanceof ListStoredQueriesResponseType);
        ListStoredQueriesResponseType resultlsq = (ListStoredQueriesResponseType) resultlsqI;

        final List<StoredQueryListItemType> items = new ArrayList<>();
        items.add(new StoredQueryListItemType("nameQuery",     Arrays.asList(new Title("Name query")),     Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint"))));
        items.add(new StoredQueryListItemType("urn:ogc:def:query:OGC-WFS::GetFeatureById", Arrays.asList(new Title("Identifier query")), alltypes));
        items.add(new StoredQueryListItemType("urn:ogc:def:storedQuery:OGC-WFS::GetFeatureByType", Arrays.asList(new Title("By type query")), Arrays.asList(new QName(""))));
        items.add(new StoredQueryListItemType("geomQuery",     Arrays.asList(new Title("Geom query")),     Arrays.asList(new QName("http://www.opengis.net/gml/3.2", "Bridges"))));
        items.add(new StoredQueryListItemType("envelopeQuery", Arrays.asList(new Title("Envelope query")), Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint"))));
        final ListStoredQueriesResponseType expResultlsq = new ListStoredQueriesResponseType(items);

        assertEquals(5, resultlsq.getStoredQuery().size());
        for (int i = 0; i < resultlsq.getStoredQuery().size(); i++) {
            assertEquals(expResultlsq.getStoredQuery().get(i).getId(), resultlsq.getStoredQuery().get(i).getId());
            assertEquals(expResultlsq.getStoredQuery().get(i).getReturnFeatureType(), resultlsq.getStoredQuery().get(i).getReturnFeatureType());
            assertEquals(expResultlsq.getStoredQuery().get(i).getTitle(), resultlsq.getStoredQuery().get(i).getTitle());
            assertEquals(expResultlsq.getStoredQuery().get(i), resultlsq.getStoredQuery().get(i));
        }
        assertEquals(expResultlsq.getStoredQuery(), resultlsq.getStoredQuery());
        assertEquals(expResultlsq, resultlsq);


        // verify the persistance by restarting the WFS
        worker.destroy();
        worker = new DefaultWFSWorker("test1");
        worker.setLogLevel(Level.FINER);
        worker.setServiceUrl("http://geomatys.com/constellation/WS/");
        worker.setShiroAccessible(false);

        resultlsqI = worker.listStoredQueries(requestlsq);

        assertTrue(resultlsqI instanceof ListStoredQueriesResponseType);
        resultlsq = (ListStoredQueriesResponseType) resultlsqI;


        assertEquals(5, resultlsq.getStoredQuery().size());
        assertEquals(expResultlsq.getStoredQuery(), resultlsq.getStoredQuery());
        assertEquals(expResultlsq, resultlsq);


    }

    @Test
    @Order(order=17)
    public void dropStoredQueriesTest() throws Exception {
        final DropStoredQueryType request = new DropStoredQueryType("WFS", "2.0.0", null, "geomQuery");
        final DropStoredQueryResponse resultI = worker.dropStoredQuery(request);

        assertTrue(resultI instanceof DropStoredQueryResponseType);
        final DropStoredQueryResponseType result = (DropStoredQueryResponseType) resultI;
        final DropStoredQueryResponseType expResult = new DropStoredQueryResponseType("OK");

        assertEquals(expResult, result);


        final ListStoredQueriesType requestlsq = new ListStoredQueriesType("WFS", "2.0.0", null);

        ListStoredQueriesResponse resultlsqI = worker.listStoredQueries(requestlsq);

        assertTrue(resultlsqI instanceof ListStoredQueriesResponseType);
        ListStoredQueriesResponseType resultlsq = (ListStoredQueriesResponseType) resultlsqI;

        final List<StoredQueryListItemType> items = new ArrayList<>();
        items.add(new StoredQueryListItemType("nameQuery", Arrays.asList(new Title("Name query")), Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint"))));
        items.add(new StoredQueryListItemType("urn:ogc:def:query:OGC-WFS::GetFeatureById", Arrays.asList(new Title("Identifier query")), alltypes));
        items.add(new StoredQueryListItemType("urn:ogc:def:storedQuery:OGC-WFS::GetFeatureByType", Arrays.asList(new Title("By type query")), Arrays.asList(new QName(""))));
        items.add(new StoredQueryListItemType("envelopeQuery", Arrays.asList(new Title("Envelope query")), Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint"))));
        final ListStoredQueriesResponseType expResultlsq = new ListStoredQueriesResponseType(items);

        assertEquals(4, resultlsq.getStoredQuery().size());
        for (int i = 0; i < resultlsq.getStoredQuery().size(); i++) {
            assertEquals(expResultlsq.getStoredQuery().get(i).getId(), resultlsq.getStoredQuery().get(i).getId());
            assertEquals(expResultlsq.getStoredQuery().get(i).getReturnFeatureType(), resultlsq.getStoredQuery().get(i).getReturnFeatureType());
            assertEquals(expResultlsq.getStoredQuery().get(i).getTitle(), resultlsq.getStoredQuery().get(i).getTitle());
            assertEquals(expResultlsq.getStoredQuery().get(i), resultlsq.getStoredQuery().get(i));
        }
        assertEquals(expResultlsq.getStoredQuery(), resultlsq.getStoredQuery());
        assertEquals(expResultlsq, resultlsq);


        // verify the persistance by restarting the WFS
        worker.destroy();
        worker = new DefaultWFSWorker("test1");
        worker.setLogLevel(Level.FINER);
        worker.setServiceUrl("http://geomatys.com/constellation/WS/");
        worker.setShiroAccessible(false);

        resultlsqI = worker.listStoredQueries(requestlsq);

        assertTrue(resultlsqI instanceof ListStoredQueriesResponseType);
        resultlsq = (ListStoredQueriesResponseType) resultlsqI;


        assertEquals(4, resultlsq.getStoredQuery().size());
        assertEquals(expResultlsq.getStoredQuery(), resultlsq.getStoredQuery());
        assertEquals(expResultlsq, resultlsq);

    }

    @Test
    @Order(order=18)
    public void getFeatureOMFeatureIdTest() throws Exception {

        /**
         * Test 1 : query on typeName samplingPoint with name parameter
         */
        final FilterType filter = new org.geotoolkit.ogc.xml.v200.FilterType(new org.geotoolkit.ogc.xml.v200.ResourceIdType("station-001"));
        final QueryType query = new QueryType(filter, null, "2.0.0");
        GetFeatureType request = new GetFeatureType("WFS", "2.0.0", null, null, Integer.MAX_VALUE, Arrays.asList(query), ResultTypeType.RESULTS, "text/xml; subtype=gml/3.2.1");

        Object result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        FeatureCollectionWrapper wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();
        assertEquals("3.2.1", wrapper.getGmlVersion());

        StringWriter writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        String expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.samplingPointCollection-2v2.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);

        String sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");

        domCompare(expectedResult, sresult);
    }

    @Test
    @Order(order=19)
    public void getFeatureOMStoredQueriesTest() throws Exception {

        /**
         * Test 1 : query on typeName samplingPoint with name parameter
         */
        Integer startIndex = null;
        GetFeatureType request = new GetFeatureType("WFS", "2.0.0", null, startIndex, Integer.MAX_VALUE, null, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.2.1");
        ObjectFactory factory = new ObjectFactory();
        List<ParameterType> params = new ArrayList<>();
        params.add(new ParameterType("name", "10972X0137-PONT"));
        StoredQueryType query = new StoredQueryType("nameQuery", null, params);
        request.getAbstractQueryExpression().add(factory.createStoredQuery(query));

        Object result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        FeatureCollectionWrapper wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();
        assertEquals("3.2.1", wrapper.getGmlVersion());

        StringWriter writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        String expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.samplingPointCollection-2v2.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);

        String sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");

        domCompare(expectedResult, sresult);

        /**
         * Test 2 : query on typeName samplingPoint with id parameter
         */
        request = new GetFeatureType("WFS", "2.0.0", null, startIndex, Integer.MAX_VALUE, null, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.2.1");
        params = new ArrayList<>();
        params.add(new ParameterType("id", "station-001"));
        query = new StoredQueryType("urn:ogc:def:query:OGC-WFS::GetFeatureById", null, params);
        request.getAbstractQueryExpression().add(factory.createStoredQuery(query));

        result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();
        assertEquals("3.2.1", wrapper.getGmlVersion());

        writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.samplingPointCollection-2v2.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);

        sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");

        domCompare(expectedResult, sresult);

        /**
         * Test 3 : query on typeName samplingPoint with a BBOX parameter
         */
        request = new GetFeatureType("WFS", "2.0.0", null, startIndex, Integer.MAX_VALUE, null, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.2.1");
        params = new ArrayList<>();
        DirectPositionType lower = new DirectPositionType( 65300.0, 1731360.0);
        DirectPositionType upper = new DirectPositionType(65500.0, 1731400.0);
        EnvelopeType env = new EnvelopeType(lower, upper, "urn:ogc:def:crs:epsg:7.6:27582");

        params.add(new ParameterType("envelope", env));
        query = new StoredQueryType("envelopeQuery", null, params);
        request.getAbstractQueryExpression().add(factory.createStoredQuery(query));

        result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();
        assertEquals("3.2.1", wrapper.getGmlVersion());

        writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.samplingPointCollection-8v2.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);

        sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");

        domCompare(expectedResult, sresult);

        /**
         * Test 4 : query with typeName parameter
         */
        request = new GetFeatureType("WFS", "2.0.0", null, startIndex, Integer.MAX_VALUE, null, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.2.1");
        params = new ArrayList<>();
        params.add(new ParameterType("typeName", new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")));
        query = new StoredQueryType("urn:ogc:def:storedQuery:OGC-WFS::GetFeatureByType", null, params);
        request.getAbstractQueryExpression().add(factory.createStoredQuery(query));

        result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();
        assertEquals("3.2.1", wrapper.getGmlVersion());

        writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.samplingPointCollection-3v2.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);

        sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");

        domCompare(expectedResult, sresult);
    }

    @Test
    @Order(order=20)
    public void getFeatureMixedStoredIdentifierQueryTest() throws Exception {
        /**
         * Test 1 : query with id parameter
         */
        Integer startIndex = null;
        GetFeatureType request = new GetFeatureType("WFS", "2.0.0", null, startIndex, Integer.MAX_VALUE, null, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.2.1");
        ObjectFactory factory = new ObjectFactory();
        List<ParameterType> params = new ArrayList<>();
        params.add(new ParameterType("id", "station-001"));
        StoredQueryType query = new StoredQueryType("urn:ogc:def:query:OGC-WFS::GetFeatureById", null, params);
        request.getAbstractQueryExpression().add(factory.createStoredQuery(query));

        Object result = worker.getFeature(request);

        assertTrue(result instanceof FeatureCollectionWrapper);
        FeatureCollectionWrapper wrapper = (FeatureCollectionWrapper) result;
        result = wrapper.getFeatureCollection();
        assertEquals("3.2.1", wrapper.getGmlVersion());

        StringWriter writer = new StringWriter();
        featureWriter.write((FeatureCollection)result,writer);

        String expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.samplingPointCollection-2v2.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);

        String sresult = writer.toString();
        sresult = sresult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");

        domCompare(expectedResult, sresult);

    }

    @Test
    @Order(order=21)
    public void schemaLocationTest() throws Exception {
        List<QueryType> queries = new ArrayList<>();
        queries.add(new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/gml", "NamedPlaces")), null));
        GetFeatureType requestGF = new GetFeatureType("WFS", "2.0.0", null, null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/gml; subtype=gml/3.1.1");

        Object resultGF = worker.getFeature(requestGF);

        assertTrue(resultGF instanceof FeatureCollectionWrapper);
        FeatureCollectionWrapper wrapper = (FeatureCollectionWrapper) resultGF;

        final Map<String, String> expResult = new HashMap<>();
        expResult.put("http://www.opengis.net/gml/3.2", "http://geomatys.com/constellation/WS/wfs/test1?request=DescribeFeatureType&version=2.0.0&service=WFS&namespace=xmlns(ns1=http://www.opengis.net/gml/3.2)&typenames=ns1:NamedPlaces");
        assertEquals(wrapper.getSchemaLocations(), expResult);

    }

    public static void domCompare(final Object actual, final Object expected) throws Exception {

        final CstlDOMComparator comparator = new CstlDOMComparator(expected, actual);
        comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
        comparator.ignoredAttributes.add("http://www.w3.org/2001/XMLSchema-instance:schemaLocation");
        comparator.compare();
    }

}
