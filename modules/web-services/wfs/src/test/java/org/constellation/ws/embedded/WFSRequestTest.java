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

import org.apache.sis.xml.MarshallerPool;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.admin.DataBusiness;
import org.constellation.admin.ProviderBusiness;
import org.constellation.admin.ServiceBusiness;
import org.constellation.admin.SpringHelper;
import org.constellation.admin.dao.ProviderRecord;
import org.constellation.configuration.LayerContext;
import org.constellation.map.configuration.LayerBusiness;
import org.constellation.provider.DataProviders;
import org.constellation.provider.ProviderFactory;
import org.constellation.test.utils.CstlDOMComparator;
import org.constellation.test.utils.Order;
import org.constellation.test.utils.SpringTestRunner;
import org.constellation.util.Util;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.ogc.xml.v110.FeatureIdType;
import org.geotoolkit.ows.xml.v110.ExceptionReport;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.sampling.xml.v100.SamplingPointType;
import org.geotoolkit.util.FileUtilities;
import org.geotoolkit.util.sql.DerbySqlScriptRunner;
import org.geotoolkit.wfs.xml.ResultTypeType;
import org.geotoolkit.wfs.xml.ValueCollection;
import org.geotoolkit.wfs.xml.v110.FeatureCollectionType;
import org.geotoolkit.wfs.xml.v110.GetFeatureType;
import org.geotoolkit.wfs.xml.v110.InsertResultsType;
import org.geotoolkit.wfs.xml.v110.InsertedFeatureType;
import org.geotoolkit.wfs.xml.v110.QueryType;
import org.geotoolkit.wfs.xml.v110.TransactionResponseType;
import org.geotoolkit.wfs.xml.v110.TransactionSummaryType;
import org.geotoolkit.wfs.xml.v110.WFSCapabilitiesType;
import org.geotoolkit.wfs.xml.v200.DescribeStoredQueriesResponseType;
import org.geotoolkit.wfs.xml.v200.DescribeStoredQueriesType;
import org.geotoolkit.wfs.xml.v200.GetPropertyValueType;
import org.geotoolkit.wfs.xml.v200.ListStoredQueriesResponseType;
import org.geotoolkit.wfs.xml.v200.ListStoredQueriesType;
import org.geotoolkit.wfs.xml.v200.MemberPropertyType;
import org.geotoolkit.wfs.xml.v200.ValueCollectionType;
import org.geotoolkit.xsd.xml.v2001.Schema;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opengis.parameter.ParameterValueGroup;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.constellation.provider.configuration.ProviderParameters.SOURCE_ID_DESCRIPTOR;
import static org.constellation.provider.configuration.ProviderParameters.SOURCE_LOADALL_DESCRIPTOR;
import static org.constellation.provider.configuration.ProviderParameters.getOrCreate;
import static org.constellation.provider.featurestore.FeatureStoreProviderService.SOURCE_CONFIG_DESCRIPTOR;
import org.constellation.test.utils.TestDatabaseHandler;
import static org.geotoolkit.data.AbstractFeatureStoreFactory.NAMESPACE;
import static org.geotoolkit.db.AbstractJDBCFeatureStoreFactory.DATABASE;
import static org.geotoolkit.db.AbstractJDBCFeatureStoreFactory.HOST;
import static org.geotoolkit.db.AbstractJDBCFeatureStoreFactory.PASSWORD;
import static org.geotoolkit.db.AbstractJDBCFeatureStoreFactory.SCHEMA;
import static org.geotoolkit.db.AbstractJDBCFeatureStoreFactory.USER;
import static org.geotoolkit.parameter.ParametersExt.createGroup;
import static org.geotoolkit.parameter.ParametersExt.getOrCreateGroup;
import static org.geotoolkit.parameter.ParametersExt.getOrCreateValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNoException;

// Constellation dependencies
// Geotoolkit dependencies
// JUnit dependencies
// GeoAPI dependencies

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@RunWith(SpringTestRunner.class)
@ContextConfiguration("classpath:/cstl/spring/test-derby.xml")
public class WFSRequestTest extends AbstractGrizzlyServer implements ApplicationContextAware {

    protected ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Inject
    private ServiceBusiness serviceBusiness;
    
    @Inject
    protected LayerBusiness layerBusiness;
    
    @Inject
    protected ProviderBusiness providerBusiness;
    
    @Inject
    protected DataBusiness dataBusiness;
    
    private static final String WFS_GETCAPABILITIES_URL_NO_SERV = "request=GetCapabilities&version=1.1.0";
    private static final String WFS_GETCAPABILITIES_URL_NO_SERV2 = "request=GetCapabilities&version=2.0.0";
    
    private static final String WFS_GETCAPABILITIES_URL_NO_VERS = "request=GetCapabilities&service=WFS";
    
    private static final String WFS_GETCAPABILITIES_URL = "request=GetCapabilities&version=1.1.0&service=WFS";
    
    private static final String WFS_GETCAPABILITIES_URL_AV = "request=GetCapabilities&acceptversions=10.0.0,2.0.0,1.1.0&service=WFS";
    
    private static final String WFS_GETCAPABILITIES_ERROR_URL = "request=GetCapabilities&version=1.3.0&service=WFS";

    private static final String WFS_GETFEATURE_URL = "request=getFeature&service=WFS&version=1.1.0&"
            + "typename=sa:SamplingPoint&namespace=xmlns(sa=http://www.opengis.net/sampling/1.0)&"
            + "filter=%3Cogc:Filter%20xmlns:ogc=%22http://www.opengis.net/ogc%22%20xmlns:gml=%22http://www.opengis.net/gml%22%3E"
            + "%3Cogc:PropertyIsEqualTo%3E"
            + "%3Cogc:PropertyName%3Egml:name%3C/ogc:PropertyName%3E"
            + "%3Cogc:Literal%3E10972X0137-PONT%3C/ogc:Literal%3E"
            + "%3C/ogc:PropertyIsEqualTo%3E"
            + "%3C/ogc:Filter%3E";
    
    private static final String WFS_GETFEATURE_URL_V2 = "request=getFeature&service=WFS&version=2.0.0&"
            + "typenames=sa:SamplingPoint&namespace=xmlns(sa=http://www.opengis.net/sampling/1.0)&"
            + "filter=%3Cfes:Filter%20xmlns:fes=%22http://www.opengis.net/fes/2.0%22%20xmlns:gml=%22http://www.opengis.net/gml/3.2%22%3E"
            + "%3Cfes:PropertyIsEqualTo%3E"
            + "%3Cfes:ValueReference%3Egml:name%3C/fes:ValueReference%3E"
            + "%3Cfes:Literal%3E10972X0137-PONT%3C/fes:Literal%3E"
            + "%3C/fes:PropertyIsEqualTo%3E"
            + "%3C/fes:Filter%3E";

    private static final String WFS_GETFEATURE_SQ_URL = "typeName=tns:SamplingPoint&startindex=0&count=10&request=GetFeature&service=WFS"
            +                                           "&namespaces=xmlns(xml,http://www.w3.org/XML/1998/namespace),xmlns(tns,http://www.opengis.net/sampling/1.0),xmlns(wfs,http://www.opengis.net/wfs/2.0)"
            +                                           "&storedquery_id=urn:ogc:def:storedQuery:OGC-WFS::GetFeatureByType&version=2.0.0";
    
    private static final String WFS_DESCRIBE_FEATURE_TYPE_URL = "request=DescribeFeatureType&service=WFS&version=1.1.0&outputformat=text%2Fxml%3B+subtype%3Dgml%2F3.1.1";
    private static final String WFS_DESCRIBE_FEATURE_TYPE_URL_V2 = "request=DescribeFeatureType&service=WFS&version=2.0.0&outputformat=text%2Fxml%3B+subtype%3Dgml%2F3.2";

    private static String EPSG_VERSION;

    private static boolean initialized = false;
    
    private static boolean mdweb_active = true;
    
    private static boolean localdb_active = true;
    /**
     * Initialize the list of layers from the defined providers in Constellation's configuration.
     */
    @PostConstruct
    public void initPool() {
        SpringHelper.setApplicationContext(applicationContext);
        if (!initialized) {
        try {
                ConfigurationEngine.setupTestEnvironement("WFSRequestTest");
            
                layerBusiness.removeAll();
                serviceBusiness.deleteAll();
                dataBusiness.deleteAll();
                providerBusiness.removeAll();
                
                final ProviderFactory featfactory = DataProviders.getInstance().getFactory("feature-store");

                // PostGIS datastore
                localdb_active = TestDatabaseHandler.hasLocalDatabase();
                if (localdb_active) {
                    final ParameterValueGroup source = featfactory.getProviderDescriptor().createValue();;
                    source.parameter(SOURCE_LOADALL_DESCRIPTOR.getName().getCode()).setValue(Boolean.TRUE);
                    source.parameter(SOURCE_ID_DESCRIPTOR.getName().getCode()).setValue("postgisSrc");

                    final ParameterValueGroup choice = getOrCreate(SOURCE_CONFIG_DESCRIPTOR,source);
                    final ParameterValueGroup pgconfig = createGroup(choice, "PostgresParameters");
                    pgconfig.parameter(DATABASE.getName().getCode()).setValue("cite-wfs");
                    pgconfig.parameter(HOST.getName().getCode()).setValue("localhost");
                    pgconfig.parameter(SCHEMA.getName().getCode()).setValue("public");
                    pgconfig.parameter(USER.getName().getCode()).setValue("test");
                    pgconfig.parameter(PASSWORD.getName().getCode()).setValue("test");
                    pgconfig.parameter(NAMESPACE.getName().getCode()).setValue("http://cite.opengeospatial.org/gmlsf");
                    choice.values().add(pgconfig);
                    
                    //add a custom sql query layer
                    final ParameterValueGroup layer = getOrCreateGroup(source, "Layer");
                    getOrCreateValue(layer, "name").setValue("CustomSQLQuery");
                    getOrCreateValue(layer, "language").setValue("CUSTOM-SQL");
                    getOrCreateValue(layer, "statement").setValue("SELECT name as nom, \"pointProperty\" as geom FROM \"PrimitiveGeoFeature\" ");


                    providerBusiness.createProvider("postgisSrc", null, ProviderRecord.ProviderType.LAYER, "feature-store", source);

                    dataBusiness.create(new QName("http://cite.opengeospatial.org/gmlsf", "AggregateGeoFeature"), "postgisSrc", "VECTOR", false, true, null, null);
                    dataBusiness.create(new QName("http://cite.opengeospatial.org/gmlsf", "PrimitiveGeoFeature"), "postgisSrc", "VECTOR", false, true, null, null);
                    dataBusiness.create(new QName("http://cite.opengeospatial.org/gmlsf", "EntitéGénérique"),     "postgisSrc", "VECTOR", false, true, null, null);
                    dataBusiness.create(new QName("http://cite.opengeospatial.org/gmlsf", "CustomSQLQuery"),      "postgisSrc", "VECTOR", false, true, null, null);
                    
                }  else {
                    LOGGER.log(Level.WARNING, "-- SOME TEST WILL BE SKIPPED BECAUSE THE LOCAL DATABASE IS MISSING --");
                }
                
                
                final File outputDir = initDataDirectory();
                final ParameterValueGroup sourcef = featfactory.getProviderDescriptor().createValue();
                getOrCreateValue(sourcef, "id").setValue("shapeSrc");
                getOrCreateValue(sourcef, "load_all").setValue(true);

                final ParameterValueGroup choice2 = getOrCreateGroup(sourcef, "choice");
                final ParameterValueGroup shpconfig = createGroup(choice2, "ShapefileParametersFolder");
                getOrCreateValue(shpconfig, "url").setValue(new URL("file:"+outputDir.getAbsolutePath() + "/org/constellation/ws/embedded/wms111/shapefiles"));
                getOrCreateValue(shpconfig, "namespace").setValue("http://www.opengis.net/gml");

                final ParameterValueGroup layer2 = getOrCreateGroup(sourcef, "Layer");
                getOrCreateValue(layer2, "name").setValue("NamedPlaces");
                getOrCreateValue(layer2, "style").setValue("cite_style_NamedPlaces");

                providerBusiness.createProvider("shapeSrc", null, ProviderRecord.ProviderType.LAYER, "feature-store", sourcef);

                dataBusiness.create(new QName("http://www.opengis.net/gml", "BuildingCenters"), "shapeSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://www.opengis.net/gml", "BasicPolygons"),   "shapeSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://www.opengis.net/gml", "Bridges"),         "shapeSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://www.opengis.net/gml", "Streams"),         "shapeSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://www.opengis.net/gml", "Lakes"),           "shapeSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://www.opengis.net/gml", "NamedPlaces"),     "shapeSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://www.opengis.net/gml", "Buildings"),       "shapeSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://www.opengis.net/gml", "RoadSegments"),    "shapeSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://www.opengis.net/gml", "DividedRoutes"),   "shapeSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://www.opengis.net/gml", "Forests"),         "shapeSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://www.opengis.net/gml", "MapNeatline"),     "shapeSrc", "VECTOR", false, true, null, null);
                dataBusiness.create(new QName("http://www.opengis.net/gml", "Ponds"),           "shapeSrc", "VECTOR", false, true, null, null);
                
                final String url = "jdbc:derby:memory:TestWFSRequestOM";
                final DefaultDataSource ds = new DefaultDataSource(url + ";create=true");
                Connection con = ds.getConnection();
                DerbySqlScriptRunner sr = new DerbySqlScriptRunner(con);
                sr.run(Util.getResourceAsStream("org/constellation/om2/structure_observations.sql"));
                sr.run(Util.getResourceAsStream("org/constellation/sql/sos-data-om2.sql"));
                con.close();
                ds.shutdown();

                final ParameterValueGroup sourceOM = featfactory.getProviderDescriptor().createValue();
                getOrCreateValue(sourceOM, "id").setValue("omSrc");
                getOrCreateValue(sourceOM, "load_all").setValue(true);    

                final ParameterValueGroup choiceOM = getOrCreateGroup(sourceOM, "choice");
                final ParameterValueGroup omconfig = createGroup(choiceOM, " SOSDBParameters");
                getOrCreateValue(omconfig, "sgbdtype").setValue("derby");
                getOrCreateValue(omconfig, "derbyurl").setValue(url);
                
                providerBusiness.createProvider("omSrc", null, ProviderRecord.ProviderType.LAYER, "feature-store", sourceOM);
                dataBusiness.create(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint"), "omSrc", "VECTOR", false, true, null, null);
                
                // MDWEB store
                mdweb_active = Util.getResourceAsStream("org/mdweb/sql/v24/metadata/model/mdw_schema_2.4_derby.sql") != null;
                if (mdweb_active) {
                    final String url2 = "jdbc:derby:memory:TestWFSRequestSMl";
                    DefaultDataSource ds2 = new DefaultDataSource(url2 + ";create=true");
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
                    final ParameterValueGroup smlconfig = createGroup(choiceSML, "SMLParameters");
                    getOrCreateValue(smlconfig, "sgbdtype").setValue("derby");
                    getOrCreateValue(smlconfig, "derbyurl").setValue(url2);

                    providerBusiness.createProvider("smlSrc", null, ProviderRecord.ProviderType.LAYER, "feature-store", sourceSML);
                    dataBusiness.create(new QName("http://www.opengis.net/sml/1.0", "System"),         "smlSrc", "VECTOR", false, true, null, null);
                    dataBusiness.create(new QName("http://www.opengis.net/sml/1.0", "Component"),      "smlSrc", "VECTOR", false, true, null, null);
                    dataBusiness.create(new QName("http://www.opengis.net/sml/1.0", "DataSourceType"), "smlSrc", "VECTOR", false, true, null, null);
                    dataBusiness.create(new QName("http://www.opengis.net/sml/1.0", "ProcessModel"),   "smlSrc", "VECTOR", false, true, null, null);
                    dataBusiness.create(new QName("http://www.opengis.net/sml/1.0", "ProcessChain"),   "smlSrc", "VECTOR", false, true, null, null);
                }
                
                DataProviders.getInstance().reload();
                
                final LayerContext config = new LayerContext();
                config.getCustomParameters().put("shiroAccessible", "false");
                config.getCustomParameters().put("transactionSecurized", "false");
                config.getCustomParameters().put("transactionnal", "true");

                serviceBusiness.create("wfs", "default", config, null, null);
                if (localdb_active) {
                    layerBusiness.add("AggregateGeoFeature", "http://cite.opengeospatial.org/gmlsf", "postgisSrc", null, "default", "wfs", null);
                    layerBusiness.add("PrimitiveGeoFeature", "http://cite.opengeospatial.org/gmlsf", "postgisSrc", null, "default", "wfs", null);
                    layerBusiness.add("EntitéGénérique",     "http://cite.opengeospatial.org/gmlsf", "postgisSrc", null, "default", "wfs", null);
                    layerBusiness.add("CustomSQLQuery",      "http://cite.opengeospatial.org/gmlsf", "postgisSrc", null, "default", "WFS", null);
                }
                layerBusiness.add("SamplingPoint",       "http://www.opengis.net/sampling/1.0",  "omSrc",      null, "default", "wfs", null);
                layerBusiness.add("BuildingCenters",     "http://www.opengis.net/gml",       "shapeSrc",   null, "default", "wfs", null);
                layerBusiness.add("BasicPolygons",       "http://www.opengis.net/gml",       "shapeSrc",   null, "default", "wfs", null);
                layerBusiness.add("Bridges",             "http://www.opengis.net/gml",       "shapeSrc",   null, "default", "wfs", null);
                layerBusiness.add("Streams",             "http://www.opengis.net/gml",       "shapeSrc",   null, "default", "wfs", null);
                layerBusiness.add("Lakes",               "http://www.opengis.net/gml",       "shapeSrc",   null, "default", "wfs", null);
                layerBusiness.add("NamedPlaces",         "http://www.opengis.net/gml",       "shapeSrc",   null, "default", "wfs", null);
                layerBusiness.add("Buildings",           "http://www.opengis.net/gml",       "shapeSrc",   null, "default", "wfs", null);
                layerBusiness.add("RoadSegments",        "http://www.opengis.net/gml",       "shapeSrc",   null, "default", "wfs", null);
                layerBusiness.add("DividedRoutes",       "http://www.opengis.net/gml",       "shapeSrc",   null, "default", "wfs", null);
                layerBusiness.add("Forests",             "http://www.opengis.net/gml",       "shapeSrc",   null, "default", "wfs", null);
                layerBusiness.add("MapNeatline",         "http://www.opengis.net/gml",       "shapeSrc",   null, "default", "wfs", null);
                layerBusiness.add("Ponds",               "http://www.opengis.net/gml",       "shapeSrc",   null, "default", "wfs", null);

                serviceBusiness.create("wfs", "test", config, null, null);
                if (localdb_active) {
                    layerBusiness.add("AggregateGeoFeature", "http://cite.opengeospatial.org/gmlsf", "postgisSrc", null, "test", "wfs", null);
                    layerBusiness.add("PrimitiveGeoFeature", "http://cite.opengeospatial.org/gmlsf", "postgisSrc", null, "test", "wfs", null);
                    layerBusiness.add("EntitéGénérique",     "http://cite.opengeospatial.org/gmlsf", "postgisSrc", null, "test", "wfs", null);
                    layerBusiness.add("CustomSQLQuery",      "http://cite.opengeospatial.org/gmlsf", "postgisSrc", null, "test", "WFS", null);
                }
                layerBusiness.add("SamplingPoint",       "http://www.opengis.net/sampling/1.0",  "omSrc",      null, "test", "wfs", null);
                layerBusiness.add("BuildingCenters",     "http://www.opengis.net/gml",       "shapeSrc",   null, "test", "wfs", null);
                layerBusiness.add("BasicPolygons",       "http://www.opengis.net/gml",       "shapeSrc",   null, "test", "wfs", null);
                layerBusiness.add("Bridges",             "http://www.opengis.net/gml",       "shapeSrc",   null, "test", "wfs", null);
                layerBusiness.add("Streams",             "http://www.opengis.net/gml",       "shapeSrc",   null, "test", "wfs", null);
                layerBusiness.add("Lakes",               "http://www.opengis.net/gml",       "shapeSrc",   null, "test", "wfs", null);
                layerBusiness.add("NamedPlaces",         "http://www.opengis.net/gml",       "shapeSrc",   null, "test", "wfs", null);
                layerBusiness.add("Buildings",           "http://www.opengis.net/gml",       "shapeSrc",   null, "test", "wfs", null);
                layerBusiness.add("RoadSegments",        "http://www.opengis.net/gml",       "shapeSrc",   null, "test", "wfs", null);
                layerBusiness.add("DividedRoutes",       "http://www.opengis.net/gml",       "shapeSrc",   null, "test", "wfs", null);
                layerBusiness.add("Forests",             "http://www.opengis.net/gml",       "shapeSrc",   null, "test", "wfs", null);
                layerBusiness.add("MapNeatline",         "http://www.opengis.net/gml",       "shapeSrc",   null, "test", "wfs", null);
                layerBusiness.add("Ponds",               "http://www.opengis.net/gml",       "shapeSrc",   null, "test", "wfs", null);


                final LayerContext config2 = new LayerContext();
                config2.getCustomParameters().put("shiroAccessible", "false");
                config2.getCustomParameters().put("transactionSecurized", "false");
                config2.getCustomParameters().put("transactionnal", "true");

                serviceBusiness.create("wfs", "test1", config, null, null);
                layerBusiness.add("SamplingPoint",       "http://www.opengis.net/sampling/1.0",  "omSrc",      null, "test1", "wfs", null);
                layerBusiness.add("BuildingCenters",     "http://www.opengis.net/gml",       "shapeSrc",   null, "test1", "wfs", null);
                layerBusiness.add("BasicPolygons",       "http://www.opengis.net/gml",       "shapeSrc",   null, "test1", "wfs", null);
                layerBusiness.add("Bridges",             "http://www.opengis.net/gml",       "shapeSrc",   null, "test1", "wfs", null);
                layerBusiness.add("Streams",             "http://www.opengis.net/gml",       "shapeSrc",   null, "test1", "wfs", null);
                layerBusiness.add("Lakes",               "http://www.opengis.net/gml",       "shapeSrc",   null, "test1", "wfs", null);
                layerBusiness.add("NamedPlaces",         "http://www.opengis.net/gml",       "shapeSrc",   null, "test1", "wfs", null);
                layerBusiness.add("Buildings",           "http://www.opengis.net/gml",       "shapeSrc",   null, "test1", "wfs", null);
                layerBusiness.add("RoadSegments",        "http://www.opengis.net/gml",       "shapeSrc",   null, "test1", "wfs", null);
                layerBusiness.add("DividedRoutes",       "http://www.opengis.net/gml",       "shapeSrc",   null, "test1", "wfs", null);
                layerBusiness.add("Forests",             "http://www.opengis.net/gml",       "shapeSrc",   null, "test1", "wfs", null);
                layerBusiness.add("MapNeatline",         "http://www.opengis.net/gml",       "shapeSrc",   null, "test1", "wfs", null);
                layerBusiness.add("Ponds",               "http://www.opengis.net/gml",       "shapeSrc",   null, "test1", "wfs", null);
                if (mdweb_active) {
                    layerBusiness.add("System",              "http://www.opengis.net/sml/1.0",       "smlSrc",     null, "test1", "wfs", null);
                    layerBusiness.add("Component",           "http://www.opengis.net/sml/1.0",       "smlSrc",     null, "test1", "wfs", null);
                    layerBusiness.add("DataSourceType",      "http://www.opengis.net/sml/1.0",       "smlSrc",     null, "test1", "wfs", null);
                    layerBusiness.add("ProcessModel",        "http://www.opengis.net/sml/1.0",       "smlSrc",     null, "test1", "wfs", null);
                    layerBusiness.add("ProcessChain",        "http://www.opengis.net/sml/1.0",       "smlSrc",     null, "test1", "wfs", null);
                }

                initServer(null, null);

                EPSG_VERSION = CRS.getVersion("EPSG").toString();
                pool = new MarshallerPool(JAXBContext.newInstance("org.geotoolkit.wfs.xml.v110"   +
                        ":org.geotoolkit.ogc.xml.v110"  +
                        ":org.geotoolkit.wfs.xml.v200"  +
                        ":org.geotoolkit.gml.xml.v311"  +
                        ":org.geotoolkit.xsd.xml.v2001" +
                        ":org.geotoolkit.sampling.xml.v100" +
                        ":org.apache.sis.internal.jaxb.geometry"), null);
                initialized = true;
            } catch (Exception ex) {
                Logger.getLogger(WFSRequestTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @AfterClass
    public static void shutDown() {
        ConfigurationEngine.shutdownTestEnvironement("WFSRequestTest");
        File f = new File("derby.log");
        if (f.exists()) {
            f.delete();
        }
        finish();
    }

    @Test
    @Order(order=1)
    public void testWFSGetCapabilities() throws Exception {
        waitForStart();

        // Creates a valid GetCapabilities url.
        URL getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?" + WFS_GETCAPABILITIES_URL);
        
        Object obj = unmarshallResponse(getCapsUrl);
        assertTrue(obj instanceof WFSCapabilitiesType);

        WFSCapabilitiesType responseCaps = (WFSCapabilitiesType)obj;
        String currentUrl =  responseCaps.getOperationsMetadata().getOperation("GetCapabilities").getDCP().get(0).getHTTP().getGetOrPost().get(0).getHref();
        assertEquals("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?", currentUrl);

        getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/test?" + WFS_GETCAPABILITIES_URL);
        obj = unmarshallResponse(getCapsUrl);
        assertTrue(obj instanceof WFSCapabilitiesType);

        responseCaps = (WFSCapabilitiesType)obj;
        currentUrl =  responseCaps.getOperationsMetadata().getOperation("GetCapabilities").getDCP().get(0).getHTTP().getGetOrPost().get(0).getHref();
        assertEquals("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/test?", currentUrl);


        getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?" + WFS_GETCAPABILITIES_URL);

        obj = unmarshallResponse(getCapsUrl);
        assertTrue(obj instanceof WFSCapabilitiesType);
        responseCaps = (WFSCapabilitiesType)obj;
        currentUrl =  responseCaps.getOperationsMetadata().getOperation("GetCapabilities").getDCP().get(0).getHTTP().getGetOrPost().get(0).getHref();
        assertEquals("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?", currentUrl);
        
        getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?" + WFS_GETCAPABILITIES_ERROR_URL);
        obj = unmarshallResponse(getCapsUrl);
        assertTrue("unexpected type:" + obj.getClass().getName(), obj instanceof ExceptionReport);
        
        getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?" + WFS_GETCAPABILITIES_URL_AV);
        obj = unmarshallResponse(getCapsUrl);
        assertTrue(obj instanceof org.geotoolkit.wfs.xml.v200.WFSCapabilitiesType);
        
        getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?" + WFS_GETCAPABILITIES_URL_NO_SERV);
        obj = unmarshallResponse(getCapsUrl);
        assertTrue(obj instanceof org.geotoolkit.ows.xml.v100.ExceptionReport);
        org.geotoolkit.ows.xml.v100.ExceptionReport report100 = (org.geotoolkit.ows.xml.v100.ExceptionReport) obj;
        assertEquals("1.0.0", report100.getVersion());
        
        getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?" + WFS_GETCAPABILITIES_URL_NO_SERV2);
        obj = unmarshallResponse(getCapsUrl);
        assertTrue(obj instanceof ExceptionReport);
        ExceptionReport report200 = (ExceptionReport) obj;
        assertEquals("2.0.0", report200.getVersion());
        
        getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?" + WFS_GETCAPABILITIES_URL_NO_VERS);
        obj = unmarshallResponse(getCapsUrl);
        assertTrue(obj instanceof org.geotoolkit.wfs.xml.v200.WFSCapabilitiesType);
    }

    /**
     */
    @Test
    @Order(order=2)
    public void testWFSGetFeaturePOST() throws Exception {

        // Creates a valid GetCapabilities url.
        final URL getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?");

        final List<QueryType> queries = new ArrayList<>();
        queries.add(new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null));
        final GetFeatureType request = new GetFeatureType("WFS", "1.1.0", null, 2, queries, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.1.1");

        // for a POST request
        URLConnection conec = getCapsUrl.openConnection();
        postRequestObject(conec, request);
        Object obj = unmarshallResponse(conec);

        assertTrue("unexpected type: " + obj.getClass().getName() + "\n" + obj, obj instanceof FeatureCollectionType);

    }
    
    @Test
    @Order(order=3)
    public void testWFSGetFeaturePOSTV2() throws Exception {

        // Creates a valid GetCapabilities url.
        final URL getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?");

        final List<org.geotoolkit.wfs.xml.v200.QueryType> queries = new ArrayList<>();
        queries.add(new org.geotoolkit.wfs.xml.v200.QueryType(null, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null));
        final org.geotoolkit.wfs.xml.v200.GetFeatureType request = new org.geotoolkit.wfs.xml.v200.GetFeatureType("WFS", "2.0.0", null, 2, queries, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.2.1");

        // for a POST request
        URLConnection conec = getCapsUrl.openConnection();
        postRequestObject(conec, request);
        Object obj = unmarshallResponse(conec);

        assertTrue("unexpected type: " + obj.getClass().getName() + "\n" + obj, obj instanceof org.geotoolkit.wfs.xml.v200.FeatureCollectionType);

    }

    /**
     */
    @Test
    @Order(order=4)
    public void testWFSGetFeatureGET() throws Exception {
        final URL getfeatsUrl;
        try {
            getfeatsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?" + WFS_GETFEATURE_URL);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        Object obj = unmarshallResponse(getfeatsUrl);

        assertTrue(obj instanceof FeatureCollectionType);

        FeatureCollectionType feat = (FeatureCollectionType) obj;
        assertEquals(1, feat.getFeatureMember().size());

        assertTrue("expected samplingPoint but was:" +  feat.getFeatureMember().get(0),
                feat.getFeatureMember().get(0).getAbstractFeature() instanceof SamplingPointType);
        SamplingPointType sp = (SamplingPointType) feat.getFeatureMember().get(0).getAbstractFeature();

        assertEquals("10972X0137-PONT", sp.getName());
    }

    @Test
    @Order(order=5)
    public void testWFSGetFeatureGET2() throws Exception {
        final URL getfeatsUrl;
        try {
            getfeatsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?" + WFS_GETFEATURE_URL_V2);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        Object obj = unmarshallResponse(getfeatsUrl);

        assertTrue("was:" + obj, obj instanceof org.geotoolkit.wfs.xml.v200.FeatureCollectionType);

        org.geotoolkit.wfs.xml.v200.FeatureCollectionType feat = (org.geotoolkit.wfs.xml.v200.FeatureCollectionType) obj;
        assertEquals(1, feat.getMember().size());
        
        MemberPropertyType member = feat.getMember().get(0);
        
        final JAXBElement element = (JAXBElement) member.getContent().get(0);

        assertTrue("expected samplingPoint but was:" +  element.getValue(), element.getValue() instanceof SamplingPointType);
        SamplingPointType sp = (SamplingPointType) element.getValue();

        // assertEquals("10972X0137-PONT", sp.getName()); TODO name attribute is moved to namespace GML 3.2 so the java binding does not match
    }
    
    @Test
    @Order(order=6)
    public void testWFSGetFeatureGETStoredQuery() throws Exception {
        final URL getfeatsUrl;
        try {
            getfeatsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?" + WFS_GETFEATURE_SQ_URL);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        final URLConnection conec = getfeatsUrl.openConnection();

        String xmlResult    = getStringResponse(conec);
        String xmlExpResult = getStringFromFile("org/constellation/wfs/xml/samplingPointCollection-3v2.xml");

        xmlExpResult = xmlExpResult.replace("EPSG_VERSION", EPSG_VERSION);
        xmlResult    = xmlResult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");
        domCompare(xmlExpResult, xmlResult);
    }

    
    /**
     */
    @Test
    @Order(order=7)
    public void testWFSDescribeFeatureGET() throws Exception {
        URL getfeatsUrl;
        try {
            getfeatsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?" + WFS_DESCRIBE_FEATURE_TYPE_URL);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        Object obj = unmarshallResponse(getfeatsUrl);

        assertTrue(obj instanceof Schema);

        Schema schema = (Schema) obj;
        if (localdb_active) {
            assertEquals(17, schema.getElements().size());
        } else {
            assertEquals(13, schema.getElements().size());
        }

        try {
            getfeatsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/test?" + WFS_DESCRIBE_FEATURE_TYPE_URL_V2);
        } catch (MalformedURLException ex) {
            assumeNoException(ex);
            return;
        }

        obj = unmarshallResponse(getfeatsUrl);

        assertTrue("was:" + obj, obj instanceof Schema);

        schema = (Schema) obj;
        if (localdb_active) {
            assertEquals(17, schema.getElements().size());
        } else {
            assertEquals(13, schema.getElements().size());
        }

    }

    /**
     */
    @Test
    @Order(order=8)
    public void testWFSTransactionInsert() throws Exception {

        // Creates a valid GetCapabilities url.
        final URL getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?");


        // for a POST request
        URLConnection conec = getCapsUrl.openConnection();

        postRequestFile(conec, "org/constellation/xml/Insert-SamplingPoint-1.xml");
        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof TransactionResponseType);

        TransactionResponseType result = (TransactionResponseType) obj;

        TransactionSummaryType sum        = new TransactionSummaryType(2, 0, 0);
        List<InsertedFeatureType> insertedFeatures = new ArrayList<>();
        insertedFeatures.add(new InsertedFeatureType(new FeatureIdType("station-007"), null));
        insertedFeatures.add(new InsertedFeatureType(new FeatureIdType("station-008"), null));
        InsertResultsType insertResult    = new InsertResultsType(insertedFeatures);
        TransactionResponseType ExpResult = new TransactionResponseType(sum, null, insertResult, "1.1.0");

        assertEquals(ExpResult, result);


        /**
         * We verify that the 2 new samplingPoint are inserted
         */
        List<QueryType> queries = new ArrayList<>();
        queries.add(new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null));
        GetFeatureType request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.1.1");

        // for a POST request
        conec = getCapsUrl.openConnection();

        postRequestObject(conec, request);
        String xmlResult    = getStringResponse(conec);
        String xmlExpResult = getStringFromFile("org/constellation/xml/samplingPointCollection-1.xml");

        xmlExpResult = xmlExpResult.replace("EPSG_VERSION", EPSG_VERSION);
        xmlResult    = xmlResult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");
        
        domCompare(xmlExpResult, xmlResult);

        // for a POST request
        conec = getCapsUrl.openConnection();

        postRequestFile(conec, "org/constellation/xml/Insert-SamplingPoint-2.xml");

        // Try to unmarshall something from the response returned by the server.
        obj = unmarshallResponse(conec);

        assertTrue(obj instanceof TransactionResponseType);

        result = (TransactionResponseType) obj;

        sum              = new TransactionSummaryType(2, 0, 0);
        insertedFeatures = new ArrayList<>();
        insertedFeatures.add(new InsertedFeatureType(new FeatureIdType("station-010"), null));
        insertedFeatures.add(new InsertedFeatureType(new FeatureIdType("station-009"), null));
        insertResult    = new InsertResultsType(insertedFeatures);
        ExpResult = new TransactionResponseType(sum, null, insertResult, "1.1.0");

        assertEquals(ExpResult, result);

        /**
         * We verify that the 2 new samplingPoint are inserted
         */
        queries = new ArrayList<>();
        queries.add(new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null));
        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.1.1");

        // for a POST request
        conec = getCapsUrl.openConnection();

        postRequestObject(conec, request);

        // Try to unmarshall something from the response returned by the server.
        xmlResult    = getStringResponse(conec);

        xmlExpResult = getStringFromFile("org/constellation/xml/samplingPointCollection-2.xml");
        xmlExpResult = xmlExpResult.replace("EPSG_VERSION", EPSG_VERSION);
        xmlResult    = xmlResult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");

        domCompare(xmlExpResult, xmlResult);


        // for a POST request
        conec = getCapsUrl.openConnection();

        postRequestFile(conec, "org/constellation/xml/Insert-SamplingPoint-3.xml");

        // Try to unmarshall something from the response returned by the server.
        obj = unmarshallResponse(conec);

        assertTrue(obj instanceof TransactionResponseType);

        result = (TransactionResponseType) obj;

        sum              = new TransactionSummaryType(2, 0, 0);
        insertedFeatures = new ArrayList<>();
        insertedFeatures.add(new InsertedFeatureType(new FeatureIdType("station-012"), null));
        insertedFeatures.add(new InsertedFeatureType(new FeatureIdType("station-011"), null));
        insertResult    = new InsertResultsType(insertedFeatures);
        ExpResult = new TransactionResponseType(sum, null, insertResult, "1.1.0");

        assertEquals(ExpResult, result);

        /**
         * We verify that the 2 new samplingPoint are inserted
         */
        queries = new ArrayList<>();
        queries.add(new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null));
        request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.1.1");

        // for a POST request
        conec = getCapsUrl.openConnection();

        postRequestObject(conec, request);

        // Try to unmarshall something from the response returned by the server.
        xmlResult    = getStringResponse(conec);
        xmlExpResult = getStringFromFile("org/constellation/xml/samplingPointCollection-3.xml");
        xmlExpResult = xmlExpResult.replace("EPSG_VERSION", EPSG_VERSION);
        xmlResult    = xmlResult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");

        domCompare(xmlExpResult, xmlResult);

    }

    @Test
    @Order(order=9)
    public void testWFSTransactionUpdate() throws Exception {

        // Creates a valid GetCapabilities url.
        final URL getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?");

        // for a POST request
        URLConnection conec = getCapsUrl.openConnection();

        postRequestFile(conec, "org/constellation/xml/Update-NamedPlaces-1.xml");

        // Try to unmarshall something from the response returned by the server.
        Object obj = unmarshallResponse(conec);

        assertTrue(obj instanceof TransactionResponseType);

        TransactionResponseType result = (TransactionResponseType) obj;

        TransactionSummaryType sum              = new TransactionSummaryType(0, 1, 0);
        TransactionResponseType ExpResult = new TransactionResponseType(sum, null, null, "1.1.0");

        assertEquals(ExpResult, result);


        /**
         * We verify that the namedPlaces have been changed
         */
        List<QueryType> queries = new ArrayList<>();
        queries.add(new QueryType(null, Arrays.asList(new QName("http://www.opengis.net/gml", "NamedPlaces")), null));
        GetFeatureType request = new GetFeatureType("WFS", "1.1.0", null, Integer.MAX_VALUE, queries, ResultTypeType.RESULTS, "text/xml; subtype=gml/3.1.1");

        // for a POST request
        conec = getCapsUrl.openConnection();

        postRequestObject(conec, request);

        // Try to unmarshall something from the response returned by the server.
        String xmlResult    = getStringResponse(conec);
        String xmlExpResult = getStringFromFile("org/constellation/xml/namedPlacesCollection-1.xml");
        xmlExpResult = xmlExpResult.replace("9090", grizzly.getCurrentPort() + "");
        xmlResult    = xmlResult.replaceAll("timeStamp=\"[^\"]*\" ", "timeStamp=\"\" ");
        
        domCompare(xmlExpResult, xmlResult);
    }
    
    @Test
    @Order(order=10)
    public void testWFSListStoredQueries() throws Exception {

        // Creates a valid GetCapabilities url.
        final URL getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?");

        final ListStoredQueriesType request = new ListStoredQueriesType("WFS", "2.0.0", null);

        // for a POST request
        URLConnection conec = getCapsUrl.openConnection();
        postRequestObject(conec, request);
        Object obj = unmarshallResponse(conec);

        assertTrue("unexpected type: " + obj.getClass().getName() + "\n" + obj, obj instanceof ListStoredQueriesResponseType);

    }
    
    @Test
    @Order(order=11)
    public void testWFSDescribeStoredQueries() throws Exception {

        // Creates a valid GetCapabilities url.
        final URL getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?");

        final DescribeStoredQueriesType request = new DescribeStoredQueriesType("WFS", "2.0.0", null, Arrays.asList("urn:ogc:def:storedQuery:OGC-WFS::GetFeatureById"));

        // for a POST request
        URLConnection conec = getCapsUrl.openConnection();
        postRequestObject(conec, request);
        Object obj = unmarshallResponse(conec);

        assertTrue("unexpected type: " + obj.getClass().getName() + "\n" + obj, obj instanceof DescribeStoredQueriesResponseType);

    }
    
    @Test
    @Order(order=12)
    public void testWFSGetPropertyValue() throws Exception {

        // Creates a valid GetCapabilities url.
        final URL getCapsUrl = new URL("http://localhost:"+ grizzly.getCurrentPort() +"/wfs/default?");

         /**
         * Test 1 : query on typeName samplingPoint with HITS
         */
        org.geotoolkit.wfs.xml.v200.QueryType query = new org.geotoolkit.wfs.xml.v200.QueryType(null, Arrays.asList(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint")), null);
        String valueReference = "sampledFeature";
        GetPropertyValueType request = new GetPropertyValueType("WFS", "2.0.0", null, Integer.MAX_VALUE, query, ResultTypeType.HITS, "text/xml; subtype=gml/3.2.1",valueReference);
        request.setValueReference(valueReference);

         // for a POST request
        URLConnection conec = getCapsUrl.openConnection();
        postRequestObject(conec, request);
        Object result = unmarshallResponse(conec);

        assertTrue("unexpected type: " + result.getClass().getName() + "\n" + result, result instanceof ValueCollectionType);
        
        assertTrue(result instanceof ValueCollection);
        assertEquals(12, ((ValueCollection)result).getNumberReturned());

        /**
         * Test 2 : query on typeName samplingPoint with RESULTS
         */
        request.setResultType(ResultTypeType.RESULTS);
        conec = getCapsUrl.openConnection();
        postRequestObject(conec, request);
        String sresult = getStringResponse(conec);

        String expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.embedded.ValueCollectionOM1.xml"));
        domCompare(expectedResult, sresult);

        /**
         * Test 3 : query on typeName samplingPoint with RESULTS
         */
        valueReference = "position";
        request.setValueReference(valueReference);
        conec = getCapsUrl.openConnection();
        postRequestObject(conec, request);
        sresult = getStringResponse(conec);

        expectedResult = FileUtilities.getStringFromFile(FileUtilities.getFileFromResource("org.constellation.wfs.xml.embedded.ValueCollectionOM2.xml"));
        expectedResult = expectedResult.replace("EPSG_VERSION", EPSG_VERSION);
        domCompare(expectedResult, sresult);

    }

    public static void domCompare(final Object actual, final Object expected) throws Exception {

        final CstlDOMComparator comparator = new CstlDOMComparator(expected, actual);
        comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
        comparator.ignoredAttributes.add("http://www.w3.org/2001/XMLSchema-instance:schemaLocation");
        comparator.compare();
    }
}
