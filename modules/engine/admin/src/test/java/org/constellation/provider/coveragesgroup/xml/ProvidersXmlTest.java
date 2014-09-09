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
/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2012, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.provider.coveragesgroup.xml;

import org.apache.sis.internal.jaxb.geometry.ObjectFactory;
import org.apache.sis.test.XMLComparator;
import org.apache.sis.xml.MarshallerPool;
import org.constellation.configuration.GFIParam;
import org.constellation.configuration.GetFeatureInfoCfg;
import org.geotoolkit.se.xml.v110.DescriptionType;
import org.geotoolkit.se.xml.v110.FeatureTypeStyleType;
import org.geotoolkit.sld.xml.v110.StyledLayerDescriptor;
import org.geotoolkit.sld.xml.v110.UserLayer;
import org.geotoolkit.sld.xml.v110.UserStyle;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests on the marshalling/unmarshalling process for a map context.
 *
 * @author Cédric Briançon
 */
public class ProvidersXmlTest {
    private MarshallerPool pool;
    private Unmarshaller unmarshaller;
    private Marshaller marshaller;

    /**
     * What should be the result of the marshalling process, without sld.
     */
    private static final String RESULT_MARSHALLING_WITHOUT_SLD =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"+
            "<MapContext xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:sld=\"http://www.opengis.net/sld\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:se=\"http://www.opengis.net/se\" xmlns:ns7=\"http://www.constellation.org/config\" xmlns:ns8=\"http://www.opengis.net/gml/3.2\">\n"+
            "    <MapItem>\n"+
            "        <MapItem>\n"+
            "            <MapLayer>\n"+
            "                <dataReference>postgis_test:my_otherlayer</dataReference>\n"+
            "                <styleReference>my_otherstyle</styleReference>\n"+
            "            </MapLayer>\n"+
            "            <MapLayer>\n"+
            "                <dataReference>coverage:my_thirdlayer</dataReference>\n"+
            "                <styleReference>my_newstyle</styleReference>\n"+
            "            </MapLayer>\n"+
            "        </MapItem>\n"+
            "        <MapItem/>\n"+
            "        <MapLayer>\n"+
            "            <dataReference>postgis_test:my_layer</dataReference>\n"+
            "            <styleReference>my_style</styleReference>\n"+
            "        </MapLayer>\n"+
            "    </MapItem>\n"+
            "</MapContext>";

    /**
     * What should be the result of the marshalling process, with custom GetFeatureInfo sld.
     */
    private static final String RESULT_MARSHALLING_WITH_GFI =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"+
            "<MapContext xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:sld=\"http://www.opengis.net/sld\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:se=\"http://www.opengis.net/se\" xmlns:ns7=\"http://www.constellation.org/config\" xmlns:ns8=\"http://www.opengis.net/gml/3.2\">\n"+
            "    <MapItem>\n"+
            "        <MapLayer>\n"+
            "            <dataReference>postgis_test:my_layer</dataReference>\n"+
            "            <styleReference>my_style</styleReference>\n"+
            "            <featureInfos>\n"+
            "                <FeatureInfo mimeType=\"image/png\" binding=\"org.some.package.ClassImage\">\n" +
            "                    <ns7:parameters>\n" +
            "                        <ns7:GFIParam key=\"paramKey1\" value=\"paramValue1\"/>\n"+
            "                    </ns7:parameters>\n"+
            "                </FeatureInfo>\n"+
            "            </featureInfos>\n"+
            "        </MapLayer>\n"+
            "    </MapItem>\n"+
            "</MapContext>";

    /**
     * What should be the result of the marshalling process, with sld.
     */
    private static final String RESULT_MARSHALLING_WITH_SLD =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"+
            "<MapContext xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:sld=\"http://www.opengis.net/sld\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:se=\"http://www.opengis.net/se\" xmlns:ns7=\"http://www.constellation.org/config\" xmlns:ns8=\"http://www.opengis.net/gml/3.2\">\n"+
            "    <MapItem>\n"+
            "        <MapItem>\n"+
            "            <MapLayer>\n"+
            "                <dataReference>postgis_test:my_otherlayer</dataReference>\n"+
            "                <style version=\"1.1.0\">\n"+
            "                    <sld:UserLayer>\n"+
            "                        <sld:UserStyle>\n"+
            "                            <se:Description>\n"+
            "                                <se:Title>test_sld</se:Title>\n"+
            "                            </se:Description>\n"+
            "                            <se:FeatureTypeStyle>\n"+
            "                                <se:Name>ft_test</se:Name>\n"+
            "                            </se:FeatureTypeStyle>\n"+
            "                        </sld:UserStyle>\n"+
            "                    </sld:UserLayer>\n"+
            "                </style>\n"+
            "            </MapLayer>\n"+
            "            <MapLayer>\n"+
            "                <dataReference>coverage:my_thirdlayer</dataReference>\n"+
            "                <styleReference>my_newstyle</styleReference>\n"+
            "            </MapLayer>\n"+
            "        </MapItem>\n"+
            "        <MapItem/>\n"+
            "        <MapLayer>\n"+
            "            <dataReference>postgis_test:my_layer</dataReference>\n"+
            "            <styleReference>my_style</styleReference>\n"+
            "        </MapLayer>\n"+
            "    </MapItem>\n"+
            "</MapContext>";

    @Before
    public void setUp() throws JAXBException {
        pool =   new MarshallerPool(JAXBContext.newInstance(MapContext.class, ObjectFactory.class), null);
        unmarshaller = pool.acquireUnmarshaller();
        marshaller   = pool.acquireMarshaller();
    }

    /**
     * Test for the marshalling process of a {@link MapContext}.
     *
     * @throws JAXBException
     */
    @Test
    public void testMarshallingWithoutSLD() throws Exception {
        final List<MapItem> mapLayers2 = new ArrayList<MapItem>();
        mapLayers2.add(new MapLayer(new DataReference("postgis_test:my_otherlayer"), new StyleReference("my_otherstyle")));
        mapLayers2.add(new MapLayer(new DataReference("coverage:my_thirdlayer"), new StyleReference("my_newstyle")));

        final List<MapItem> mapItems = new ArrayList<MapItem>();
        mapItems.add(new MapItem(mapLayers2));

        final MapLayer ml = new MapLayer(new DataReference("postgis_test:my_layer"), new StyleReference("my_style"));
        mapItems.add(new MapItem());
        mapItems.add(ml);
        final MapItem mapItem = new MapItem(mapItems);
        final MapContext mapContext = new MapContext(mapItem);

        final StringWriter sw = new StringWriter();
        marshaller.marshal(mapContext, sw);

        final String result = sw.toString();
        try {
            sw.close();
        } catch (IOException e) {
            fail("Unable to close the writer");
        }
        assertNotNull(result);
        assertFalse(result.isEmpty());

        org.apache.sis.test.XMLComparator comparator = new XMLComparator(RESULT_MARSHALLING_WITHOUT_SLD, result.trim());
        comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
        comparator.compare();
    }

    /**
     * Test for the marshalling process of a {@link MapContext}.
     *
     * @throws JAXBException
     */
    @Test
    public void testMarshallingWithGFI() throws Exception {

        final List<GFIParam> params = new ArrayList<>();
        params.add(new GFIParam("paramKey1", "paramValue1"));

        final GetFeatureInfoCfg gfiParam = new GetFeatureInfoCfg("image/png","org.some.package.ClassImage");
        gfiParam.setGfiParameter(params);

        List<GetFeatureInfoCfg> gfiList = new ArrayList<>();
        gfiList.add(gfiParam);

        final List<MapItem> mapItems = new ArrayList<>();
        final MapLayer ml = new MapLayer(new DataReference("postgis_test:my_layer"), new StyleReference("my_style"));

        ml.setGetFeatureInfoCfgs(gfiList);
        mapItems.add(ml);
        final MapItem mapItem = new MapItem(mapItems);
        final MapContext mapContext = new MapContext(mapItem);

        final StringWriter sw = new StringWriter();
        marshaller.marshal(mapContext, sw);

        final String result = sw.toString();
        try {
            sw.close();
        } catch (IOException e) {
            fail("Unable to close the writer");
        }
        assertNotNull(result);
        assertFalse(result.isEmpty());

        org.apache.sis.test.XMLComparator comparator = new XMLComparator(RESULT_MARSHALLING_WITH_GFI, result.trim());
        comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
        comparator.compare();
    }

    /**
     * Test for the marshalling process of a {@link MapContext}.
     *
     * @throws JAXBException
     */
    @Test
    public void testMarshallingWithSLD() throws Exception {
        final List<MapItem> mapLayers2 = new ArrayList<MapItem>();
        final StyledLayerDescriptor sld = new StyledLayerDescriptor();
        final UserStyle us = new UserStyle();
        final DescriptionType title = new DescriptionType();
        title.setTitle("test_sld");
        us.setDescription(title);
        final FeatureTypeStyleType fts = new FeatureTypeStyleType();
        fts.setName("ft_test");
        us.getFeatureTypeStyleOrCoverageStyleOrOnlineResource().add(fts);
        final UserLayer ul = new UserLayer();
        ul.getUserStyle().add(us);
        sld.getNamedLayerOrUserLayer().add(ul);

        mapLayers2.add(new MapLayer(new DataReference("postgis_test:my_otherlayer"), sld));
        mapLayers2.add(new MapLayer(new DataReference("coverage:my_thirdlayer"), new StyleReference("my_newstyle")));

        final List<MapItem> mapItems = new ArrayList<MapItem>();
        mapItems.add(new MapItem(mapLayers2));

        final MapLayer ml = new MapLayer(new DataReference("postgis_test:my_layer"), new StyleReference("my_style"));
        mapItems.add(new MapItem());
        mapItems.add(ml);
        final MapItem mapItem = new MapItem(mapItems);
        final MapContext mapContext = new MapContext(mapItem);

        final StringWriter sw = new StringWriter();
        marshaller.marshal(mapContext, sw);

        final String result = sw.toString();
        try {
            sw.close();
        } catch (IOException e) {
            fail("Unable to close the writer");
        }
        assertNotNull(result);
        assertFalse(result.isEmpty());

        XMLComparator comparator = new XMLComparator(RESULT_MARSHALLING_WITH_SLD, result.trim());
        comparator.ignoredAttributes.add("http://www.w3.org/2000/xmlns:*");
        comparator.compare();
    }

    /**
     * Test for the unmarshalling process of a string-representation of a {@link MapContext}.
     *
     * @throws JAXBException
     */
    @Test
    public void testUnmarshallingWithoutSLD() throws JAXBException {
        final StringReader sr = new StringReader(RESULT_MARSHALLING_WITHOUT_SLD);
        final Object result = unmarshaller.unmarshal(sr);
        sr.close();
        assertNotNull(result);
        assertTrue(result instanceof MapContext);

        final MapContext mc = (MapContext)result;
        final List<MapItem> mapItems = mc.getMapItem().getMapItems();
        assertNotNull(mapItems);
        assertFalse(mapItems.isEmpty());
        assertEquals(3, mapItems.size());

        final MapLayer ml0 = (MapLayer) mapItems.get(0).getMapItems().get(0);
        assertEquals("postgis_test:my_otherlayer", ml0.getDataReference().getValue());
        assertEquals("my_otherstyle", ml0.getStyleReference().getValue());

        final MapLayer ml1 = (MapLayer) mapItems.get(0).getMapItems().get(1);
        assertEquals("coverage:my_thirdlayer", ml1.getDataReference().getValue());
        assertEquals("my_newstyle", ml1.getStyleReference().getValue());

        assertEquals("postgis_test:my_layer", ((MapLayer) mapItems.get(2)).getDataReference().getValue());
        assertEquals("my_style", ((MapLayer) mapItems.get(2)).getStyleReference().getValue());
    }

    /**
     * Test for the unmarshalling process of a string-representation of a {@link MapContext}.
     *
     * @throws JAXBException
     */
    @Test
    public void testUnmarshallingWithGFI() throws JAXBException {
        final StringReader sr = new StringReader(RESULT_MARSHALLING_WITH_GFI);
        final Object result = unmarshaller.unmarshal(sr);
        sr.close();
        assertNotNull(result);
        assertTrue(result instanceof MapContext);

        final MapContext mc = (MapContext)result;
        final List<MapItem> mapItems = mc.getMapItem().getMapItems();
        assertNotNull(mapItems);
        assertFalse(mapItems.isEmpty());
        assertEquals(1, mapItems.size());

        assertEquals("postgis_test:my_layer", ((MapLayer) mapItems.get(0)).getDataReference().getValue());
        assertEquals("my_style", ((MapLayer) mapItems.get(0)).getStyleReference().getValue());
        assertEquals(1, ((MapLayer) mapItems.get(0)).getGetFeatureInfoCfgs().size());
        List<GetFeatureInfoCfg> gfiCfg = ((MapLayer)mapItems.get(0)).getGetFeatureInfoCfgs();
        assertEquals("image/png", gfiCfg.get(0).getMimeType());
        assertEquals("org.some.package.ClassImage", gfiCfg.get(0).getBinding());
        assertEquals(1, gfiCfg.get(0).getGfiParameter().size());
        assertEquals("paramKey1", gfiCfg.get(0).getGfiParameter().get(0).getKey());
        assertEquals("paramValue1", gfiCfg.get(0).getGfiParameter().get(0).getValue());
    }

    /**
     * Test for the unmarshalling process of a string-representation of a {@link MapContext}.
     *
     * @throws JAXBException
     */
    @Test
    public void testUnmarshallingWithSLD() throws JAXBException {
        final StringReader sr = new StringReader(RESULT_MARSHALLING_WITH_SLD);
        final Object result = unmarshaller.unmarshal(sr);
        sr.close();
        assertNotNull(result);
        assertTrue(result instanceof MapContext);

        final MapContext mc = (MapContext)result;
        final List<MapItem> mapItems = mc.getMapItem().getMapItems();
        assertNotNull(mapItems);
        assertFalse(mapItems.isEmpty());
        assertEquals(3, mapItems.size());

        final MapLayer ml0 = (MapLayer) mapItems.get(0).getMapItems().get(0);
        assertEquals("postgis_test:my_otherlayer", ml0.getDataReference().getValue());
        assertTrue(ml0.getStyle().getNamedLayerOrUserLayer().get(0) instanceof UserLayer);
        final UserLayer ul  = (UserLayer) ml0.getStyle().getNamedLayerOrUserLayer().get(0);
        assertTrue(ul.getUserStyle().get(0).getFeatureTypeStyleOrCoverageStyleOrOnlineResource().get(0) instanceof FeatureTypeStyleType);
        final FeatureTypeStyleType fts = (FeatureTypeStyleType)ul.getUserStyle().get(0).getFeatureTypeStyleOrCoverageStyleOrOnlineResource().get(0);
        assertEquals("ft_test", fts.getName());

        final MapLayer ml1 = (MapLayer) mapItems.get(0).getMapItems().get(1);
        assertEquals("coverage:my_thirdlayer", ml1.getDataReference().getValue());
        assertEquals("my_newstyle", ml1.getStyleReference().getValue());

        assertEquals("postgis_test:my_layer", ((MapLayer) mapItems.get(2)).getDataReference().getValue());
        assertEquals("my_style", ((MapLayer) mapItems.get(2)).getStyleReference().getValue());
    }

    @After
    public void tearDown() {
        if (unmarshaller != null) {
            pool.recycle(unmarshaller);
        }
        if (marshaller != null) {
            pool.recycle(marshaller);
        }
    }
}
