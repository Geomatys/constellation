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

package org.constellation.map.ws.rs;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.namespace.QName;
import org.apache.sis.util.logging.Logging;
import org.constellation.admin.SpringHelper;
import org.constellation.api.ProviderType;
import org.constellation.business.IDataBusiness;
import org.constellation.business.ILayerBusiness;
import org.constellation.business.IProviderBusiness;
import org.constellation.business.IServiceBusiness;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.LayerContext;
import org.constellation.map.ws.QueryContext;
import org.constellation.provider.DataProviders;
import org.constellation.provider.ProviderFactory;
import org.constellation.test.utils.BasicMultiValueMap;
import org.constellation.test.utils.BasicUriInfo;
import org.constellation.test.utils.SpringTestRunner;
import org.constellation.ws.WSEngine;
import org.constellation.ws.Worker;
import org.constellation.ws.embedded.AbstractGrizzlyServer;
import org.constellation.ws.rs.AbstractWebService;
import org.geotoolkit.internal.referencing.CRSUtilities;
import static org.geotoolkit.utility.parameter.ParametersExt.createGroup;
import static org.geotoolkit.utility.parameter.ParametersExt.getOrCreateGroup;
import static org.geotoolkit.utility.parameter.ParametersExt.getOrCreateValue;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.referencing.ReferencingUtilities;
import org.geotoolkit.wms.xml.GetFeatureInfo;
import org.geotoolkit.wms.xml.GetMap;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

/**
 * Testing wms service value parsing.
 * 
 * @author Johann Sorel (Geomatys)
 */
@RunWith(SpringTestRunner.class)
@ContextConfiguration("classpath:/cstl/spring/test-context.xml")
@ActiveProfiles({"standard"})
public class WMSServiceTest implements ApplicationContextAware {

    private static final Logger LOGGER = Logging.getLogger(WMSServiceTest.class);
    
    protected ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Inject
    private IServiceBusiness serviceBusiness;
    
    @Inject
    protected ILayerBusiness layerBusiness;
    
    @Inject
    protected IProviderBusiness providerBusiness;
    
    @Inject
    protected IDataBusiness dataBusiness;
    
    private static final double DELTA = 0.00000001;
    private static WMSService service;
    private final BasicUriInfo info = new BasicUriInfo(null, null);
    private final MultivaluedMap<String,String> queryParameters = new BasicMultiValueMap<>();
    private final MultivaluedMap<String,String> pathParameters = new BasicMultiValueMap<>();

    private static boolean initialized = false;
    
    @BeforeClass
    public static void start() {
        ConfigDirectory.setupTestEnvironement("WMSServiceTest");
    }
    
    @PostConstruct
    public void init() {
        SpringHelper.setApplicationContext(applicationContext);
        if (!initialized) {
            try {

                layerBusiness.removeAll();
                serviceBusiness.deleteAll();
                dataBusiness.deleteAll();
                providerBusiness.removeAll();
                
                // coverage-file datastore
                final File rootDir                  = AbstractGrizzlyServer.initDataDirectory();
                final ProviderFactory covFilefactory = DataProviders.getInstance().getFactory("coverage-store");
                final ParameterValueGroup sourceCF = covFilefactory.getProviderDescriptor().createValue();
                getOrCreateValue(sourceCF, "id").setValue("coverageTestSrc");
                getOrCreateValue(sourceCF, "load_all").setValue(true);
                final ParameterValueGroup choice3 = getOrCreateGroup(sourceCF, "choice");

                final ParameterValueGroup srcCFConfig = getOrCreateGroup(choice3, "FileCoverageStoreParameters");

                getOrCreateValue(srcCFConfig, "path").setValue(new URL("file:" + rootDir.getAbsolutePath() + "/org/constellation/data/SSTMDE200305.png"));
                getOrCreateValue(srcCFConfig, "type").setValue("AUTO");
                getOrCreateValue(srcCFConfig, "namespace").setValue("no namespace");

                providerBusiness.storeProvider("coverageTestSrc", null, ProviderType.LAYER, "coverage-store", sourceCF);
                dataBusiness.create(new QName("SSTMDE200305"), "coverageTestSrc", "COVERAGE", false, true, null, null);
                

                final ProviderFactory ffactory = DataProviders.getInstance().getFactory("feature-store");
                final File outputDir = AbstractGrizzlyServer.initDataDirectory();
                final ParameterValueGroup sourcef = ffactory.getProviderDescriptor().createValue();
                getOrCreateValue(sourcef, "id").setValue("shapeSrc");
                getOrCreateValue(sourcef, "load_all").setValue(true);

                final ParameterValueGroup choice = getOrCreateGroup(sourcef, "choice");
                final ParameterValueGroup shpconfig = createGroup(choice, "ShapefileParametersFolder");
                getOrCreateValue(shpconfig, "url").setValue(new URL("file:"+outputDir.getAbsolutePath() + "/org/constellation/ws/embedded/wms111/shapefiles"));
                getOrCreateValue(shpconfig, "namespace").setValue("http://www.opengis.net/gml");

                final ParameterValueGroup layer = getOrCreateGroup(sourcef, "Layer");
                getOrCreateValue(layer, "name").setValue("NamedPlaces");
                getOrCreateValue(layer, "style").setValue("cite_style_NamedPlaces");

                providerBusiness.storeProvider("shapeSrc", null, ProviderType.LAYER, "feature-store", sourcef);

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

                final LayerContext config = new LayerContext();
                config.getCustomParameters().put("shiroAccessible", "false");

                serviceBusiness.create("wms", "default", config, null);
                layerBusiness.add("SSTMDE200305",                      null,          "coverageTestSrc",        null, "default", "wms", null);
                layerBusiness.add("BuildingCenters",     "http://www.opengis.net/gml",       "shapeSrc",        null, "default", "wms", null);
                layerBusiness.add("BasicPolygons",       "http://www.opengis.net/gml",       "shapeSrc",        null, "default", "wms", null);
                layerBusiness.add("Bridges",             "http://www.opengis.net/gml",       "shapeSrc",        null, "default", "wms", null);
                layerBusiness.add("Streams",             "http://www.opengis.net/gml",       "shapeSrc",        null, "default", "wms", null);
                layerBusiness.add("Lakes",               "http://www.opengis.net/gml",       "shapeSrc",        null, "default", "wms", null);
                layerBusiness.add("NamedPlaces",         "http://www.opengis.net/gml",       "shapeSrc",        null, "default", "wms", null);
                layerBusiness.add("Buildings",           "http://www.opengis.net/gml",       "shapeSrc",        null, "default", "wms", null);
                layerBusiness.add("RoadSegments",        "http://www.opengis.net/gml",       "shapeSrc",        null, "default", "wms", null);
                layerBusiness.add("DividedRoutes",       "http://www.opengis.net/gml",       "shapeSrc",        null, "default", "wms", null);
                layerBusiness.add("Forests",             "http://www.opengis.net/gml",       "shapeSrc",        null, "default", "wms", null);
                layerBusiness.add("MapNeatline",         "http://www.opengis.net/gml",       "shapeSrc",        null, "default", "wms", null);
                layerBusiness.add("Ponds",               "http://www.opengis.net/gml",       "shapeSrc",        null, "default", "wms", null);

                service = new WMSService();
                initialized = true;
            } catch (Exception ex) {
                Logger.getLogger(WMSServiceTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    @AfterClass
    public static void finish() {
        service.destroy();
        ConfigDirectory.shutdownTestEnvironement("WMSServiceTest");
        try {
            SpringHelper.getBean(ILayerBusiness.class).removeAll();
            SpringHelper.getBean(IServiceBusiness.class).deleteAll();
            SpringHelper.getBean(IDataBusiness.class).deleteAll();
            SpringHelper.getBean(IProviderBusiness.class).removeAll();
        } catch (ConfigurationException ex) {
            Logger.getAnonymousLogger().log(Level.WARNING, ex.getMessage());
        }
    }
    
    public void setFields() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException {
        //do not use this in real code, just for testing
        Field privateStringField = AbstractWebService.class.getDeclaredField("uriContext");
        privateStringField.setAccessible(true);
        privateStringField.set(service, info);
        
        info.setPathParameters(pathParameters);
        info.setQueryParameters(queryParameters);
    }

    private GetMap callGetMap() throws IllegalAccessException, IllegalArgumentException,
                                       InvocationTargetException, NoSuchMethodException{
        
        //do not use this in real code, just for testing
        final Worker worker = WSEngine.getInstance("WMS", "default");
        final Method adaptGetMapMethod = WMSService.class.getDeclaredMethod(
                "adaptGetMap", boolean.class, QueryContext.class, Worker.class);
        adaptGetMapMethod.setAccessible(true);
        final GetMap getMap = (GetMap)adaptGetMapMethod.invoke(service, true, new QueryContext(), worker);
        return getMap;
    }

    private GetFeatureInfo callGetFeatureInfo() throws IllegalAccessException, IllegalArgumentException,
                                       InvocationTargetException, NoSuchMethodException{
        //do not use this in real code, just for testing
        final Worker worker = WSEngine.getInstance("WMS", "default");
        final Method adaptGetMapMethod = WMSService.class.getDeclaredMethod(
                "adaptGetFeatureInfo", QueryContext.class, Worker.class);
        adaptGetMapMethod.setAccessible(true);
        final GetFeatureInfo getFI = (GetFeatureInfo)adaptGetMapMethod.invoke(service, new QueryContext(), worker);
        return getFI;
    }


    /**
     * TODO must test :
     * - Errors returned when missing parameters
     * - Version 1.1 and 1.3
     * - Dim_Range value
     */
    @Test
    public void testAdaptGetMap() throws Exception {
        queryParameters.clear();
        pathParameters.clear();
        queryParameters.putSingle("AZIMUTH", "49");
        queryParameters.putSingle("BBOX", "-4000,-150,3200,560");
        queryParameters.putSingle("CRS", "EPSG:3395");
        queryParameters.putSingle("ELEVATION", "156.789");
        queryParameters.putSingle("FORMAT", "image/png");
        queryParameters.putSingle("HEIGHT", "600");
        queryParameters.putSingle("LAYERS", "BlueMarble");
        queryParameters.putSingle("STYLES", "");
        queryParameters.putSingle("TIME", "2007-06-23T14:31:56");
        queryParameters.putSingle("WIDTH", "800");
        queryParameters.putSingle("VERSION", "1.3.0");
        setFields();

        final GetMap parsedQuery = callGetMap();

        //azimuth
        assertEquals(49, parsedQuery.getAzimuth(), DELTA);

        //elevation
        assertEquals(156.789d, parsedQuery.getElevation().doubleValue(), DELTA);

        //time
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT+0"));
        cal.set(Calendar.YEAR, 2007);
        cal.set(Calendar.MONTH, 05);
        cal.set(Calendar.DAY_OF_MONTH, 23);
        cal.set(Calendar.HOUR_OF_DAY, 14);
        cal.set(Calendar.MINUTE, 31);
        cal.set(Calendar.SECOND, 56);
        cal.set(Calendar.MILLISECOND, 0);
        Date time = cal.getTime();
        assertEquals(time, parsedQuery.getTime().get(0));

        //envelope 2D
        Envelope env2D = parsedQuery.getEnvelope2D();
        assertEquals(CRS.decode("EPSG:3395"), env2D.getCoordinateReferenceSystem());
        assertEquals(-4000d, env2D.getMinimum(0),DELTA);
        assertEquals(-150d, env2D.getMinimum(1),DELTA);
        assertEquals(3200d, env2D.getMaximum(0),DELTA);
        assertEquals(560d, env2D.getMaximum(1),DELTA);

        //envelope 4D
        final List<Date> times = parsedQuery.getTime();
        final Date[] dates = new Date[2];
        if (times != null && !times.isEmpty()) {
            dates[0] = times.get(0);
            dates[1] = times.get(times.size()-1);
        }
        Envelope env4D = ReferencingUtilities.combine(parsedQuery.getEnvelope2D(), dates, new Double[]{parsedQuery.getElevation(), parsedQuery.getElevation()});
        CoordinateReferenceSystem crs = env4D.getCoordinateReferenceSystem();
        assertEquals(4, crs.getCoordinateSystem().getDimension());
        CoordinateReferenceSystem crs2D = CRSUtilities.getCRS2D(crs);
        assertEquals(CRS.decode("EPSG:3395"), crs2D);
        assertEquals(-4000d, env4D.getMinimum(0),DELTA);
        assertEquals(-150d, env4D.getMinimum(1),DELTA);
        assertEquals(3200d, env4D.getMaximum(0),DELTA);
        assertEquals(560d, env4D.getMaximum(1),DELTA);
        assertEquals(156.789d, env4D.getMinimum(2), DELTA);
        assertEquals(156.789d, env4D.getMaximum(2), DELTA);
        assertEquals(time.getTime(), env4D.getMinimum(3), DELTA);
        assertEquals(time.getTime(), env4D.getMaximum(3), DELTA);
        

//        TODO
//        getMap.getBackground();
//        getMap.getExceptionFormat();
//        getMap.getFormat();
//        getMap.getLayers();
//        getMap.getRequest();
//        getMap.getService();
//        getMap.getSize();
//        getMap.getSld();
//        getMap.getStyles();
//        getMap.getTime();
//        getMap.getTransparent();
//        getMap.getVersion();

    }

    @Test
    public void testAdaptGetFeatureInfo() throws Exception{
        queryParameters.clear();
        pathParameters.clear();
        queryParameters.putSingle("AZIMUTH", "49");
        queryParameters.putSingle("BBOX", "-4000,-150,3200,560");
        queryParameters.putSingle("CRS", "EPSG:3395");
        queryParameters.putSingle("ELEVATION", "156.789");
        queryParameters.putSingle("FORMAT", "image/png");
        queryParameters.putSingle("HEIGHT", "600");
        queryParameters.putSingle("I", "230");
        queryParameters.putSingle("J", "315");
        queryParameters.putSingle("LAYERS", "BlueMarble");
        queryParameters.putSingle("QUERY_LAYERS", "BlueMarble");
        queryParameters.putSingle("STYLES", "");
        queryParameters.putSingle("TIME", "2007-06-23T14:31:56");
        queryParameters.putSingle("WIDTH", "800");
        queryParameters.putSingle("VERSION", "1.3.0");
        setFields();
        
        final GetFeatureInfo parsedQuery = callGetFeatureInfo();

        //azimuth
        assertEquals(49, parsedQuery.getAzimuth(), DELTA);

        //elevation
        assertEquals(156.789d, parsedQuery.getElevation().doubleValue(), DELTA);

        //time
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT+0"));
        cal.set(Calendar.YEAR, 2007);
        cal.set(Calendar.MONTH, 05);
        cal.set(Calendar.DAY_OF_MONTH, 23);
        cal.set(Calendar.HOUR_OF_DAY, 14);
        cal.set(Calendar.MINUTE, 31);
        cal.set(Calendar.SECOND, 56);
        cal.set(Calendar.MILLISECOND, 0);
        Date time = cal.getTime();
        assertEquals(time, parsedQuery.getTime().get(0));

        //envelope 2D
        Envelope env2D = parsedQuery.getEnvelope2D();
        assertEquals(CRS.decode("EPSG:3395"), env2D.getCoordinateReferenceSystem());
        assertEquals(-4000d, env2D.getMinimum(0),DELTA);
        assertEquals(-150d, env2D.getMinimum(1),DELTA);
        assertEquals(3200d, env2D.getMaximum(0),DELTA);
        assertEquals(560d, env2D.getMaximum(1),DELTA);

        //envelope 4D
        final List<Date> times = parsedQuery.getTime();
        final Date[] dates = new Date[2];
        if (times != null && !times.isEmpty()) {
            dates[0] = times.get(0);
            dates[1] = times.get(times.size()-1);
        }
        Envelope env4D = ReferencingUtilities.combine(parsedQuery.getEnvelope2D(), dates, new Double[]{parsedQuery.getElevation(), parsedQuery.getElevation()});
        CoordinateReferenceSystem crs = env4D.getCoordinateReferenceSystem();
        assertEquals(4, crs.getCoordinateSystem().getDimension());
        CoordinateReferenceSystem crs2D = CRSUtilities.getCRS2D(crs);
        assertEquals(CRS.decode("EPSG:3395"), crs2D);
        assertEquals(-4000d, env4D.getMinimum(0),DELTA);
        assertEquals(-150d, env4D.getMinimum(1),DELTA);
        assertEquals(3200d, env4D.getMaximum(0),DELTA);
        assertEquals(560d, env4D.getMaximum(1),DELTA);
        assertEquals(156.789d, env4D.getMinimum(2), DELTA);
        assertEquals(156.789d, env4D.getMaximum(2), DELTA);
        assertEquals(time.getTime(), env4D.getMinimum(3), DELTA);
        assertEquals(time.getTime(), env4D.getMaximum(3), DELTA);

        //mouse coordinate
        assertEquals(230, parsedQuery.getX());
        assertEquals(315, parsedQuery.getY());

//        TODO
//        getMap.getBackground();
//        getMap.getExceptionFormat();
//        getMap.getFormat();
//        getMap.getLayers();
//        getMap.getRequest();
//        getMap.getService();
//        getMap.getSize();
//        getMap.getSld();
//        getMap.getStyles();
//        getMap.getTime();
//        getMap.getTransparent();
//        getMap.getVersion();

    }


}