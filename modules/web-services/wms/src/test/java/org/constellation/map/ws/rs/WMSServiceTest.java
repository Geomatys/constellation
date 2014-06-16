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

import java.lang.reflect.InvocationTargetException;
import javax.ws.rs.core.MultivaluedMap;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.admin.ServiceBusiness;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.LayerContext;
import org.constellation.map.configuration.LayerBusiness;

import org.constellation.map.ws.QueryContext;
import org.constellation.ws.rs.WebService;
import org.constellation.test.utils.BasicMultiValueMap;
import org.constellation.test.utils.BasicUriInfo;
import org.constellation.test.utils.TestRunner;
import org.constellation.ws.WSEngine;
import org.constellation.ws.Worker;

import org.geotoolkit.referencing.CRS;
import org.geotoolkit.internal.referencing.CRSUtilities;
import org.geotoolkit.referencing.ReferencingUtilities;
import org.geotoolkit.wms.xml.GetMap;
import org.geotoolkit.wms.xml.GetFeatureInfo;
import org.junit.AfterClass;

import org.opengis.geometry.Envelope;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;

/**
 * Testing wms service value parsing.
 * 
 * @author Johann Sorel (Geomatys)
 */
@RunWith(TestRunner.class)
public class WMSServiceTest {

    @Inject
    private ServiceBusiness serviceBusiness;
    
    @Inject
    protected LayerBusiness layerBusiness;
    
    private static final double DELTA = 0.00000001;
    private static WMSService service;
    private final BasicUriInfo info = new BasicUriInfo(null, null);
    private final MultivaluedMap<String,String> queryParameters = new BasicMultiValueMap<>();
    private final MultivaluedMap<String,String> pathParameters = new BasicMultiValueMap<>();

    @PostConstruct
    public void init() {
        try {
            ConfigurationEngine.setupTestEnvironement("WMSServiceTest");
            
            final LayerContext config = new LayerContext();
            config.getCustomParameters().put("shiroAccessible", "false");
            
            serviceBusiness.create("WMS", "default", config, null);
            layerBusiness.add("SST_tests",            null,                                  "coverageTestSrc", null, "default", "WMS");
            layerBusiness.add("BuildingCenters",     "http://www.opengis.net/gml/3.2",       "shapeSrc",        null, "default", "WMS");
            layerBusiness.add("BasicPolygons",       "http://www.opengis.net/gml/3.2",       "shapeSrc",        null, "default", "WMS");
            layerBusiness.add("Bridges",             "http://www.opengis.net/gml/3.2",       "shapeSrc",        null, "default", "WMS");
            layerBusiness.add("Streams",             "http://www.opengis.net/gml/3.2",       "shapeSrc",        null, "default", "WMS");
            layerBusiness.add("Lakes",               "http://www.opengis.net/gml/3.2",       "shapeSrc",        null, "default", "WMS");
            layerBusiness.add("NamedPlaces",         "http://www.opengis.net/gml/3.2",       "shapeSrc",        null, "default", "WMS");
            layerBusiness.add("Buildings",           "http://www.opengis.net/gml/3.2",       "shapeSrc",        null, "default", "WMS");
            layerBusiness.add("RoadSegments",        "http://www.opengis.net/gml/3.2",       "shapeSrc",        null, "default", "WMS");
            layerBusiness.add("DividedRoutes",       "http://www.opengis.net/gml/3.2",       "shapeSrc",        null, "default", "WMS");
            layerBusiness.add("Forests",             "http://www.opengis.net/gml/3.2",       "shapeSrc",        null, "default", "WMS");
            layerBusiness.add("MapNeatline",         "http://www.opengis.net/gml/3.2",       "shapeSrc",        null, "default", "WMS");
            layerBusiness.add("Ponds",               "http://www.opengis.net/gml/3.2",       "shapeSrc",        null, "default", "WMS");
            
            service = new WMSService();
        } catch (ConfigurationException ex) {
            Logger.getLogger(WMSServiceTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @AfterClass
    public static void finish() {
        service.destroy();
        ConfigurationEngine.shutdownTestEnvironement("WMSServiceTest");
    }
    
    public WMSServiceTest() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException {
        //do not use this in real code, just for testing
        Field privateStringField = WebService.class.getDeclaredField("uriContext");
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
        assertEquals(time, parsedQuery.getTime());

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
    public void testAdaptGetFeatureInfo() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException,
                   InvocationTargetException, NoSuchAuthorityCodeException, FactoryException, TransformException{
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
        assertEquals(time, parsedQuery.getTime());

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