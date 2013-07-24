/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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
package org.constellation.util;

import org.geotoolkit.feature.DefaultName;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Date;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
public class DataReferenceTest {

    @Test
    public void testDataReference () {

        Date currentDate = new Date();
        long time = currentDate.getTime();

        DataReference dataRef = new DataReference("${providerLayerType|myProvider|myLayer}");
        assertEquals("providerLayerType", dataRef.getDataType());
        assertEquals("myProvider", dataRef.getProviderOrServiceId());
        assertEquals(DefaultName.valueOf("myLayer"), dataRef.getLayerId());
        assertNull(dataRef.getDataVersion());
        assertNull(dataRef.getServiceSpec());

        //with date
        dataRef = new DataReference("${providerLayerType|myProvider|myLayer|"+time+"}");
        assertEquals("providerLayerType", dataRef.getDataType());
        assertEquals("myProvider", dataRef.getProviderOrServiceId());
        assertEquals(DefaultName.valueOf("myLayer"), dataRef.getLayerId());
        assertEquals(currentDate, dataRef.getDataVersion());
        assertNull(dataRef.getServiceSpec());

        dataRef = new DataReference("${serviceType|http://serviceURL/|WMS|defaultInstance|myLayer}");
        assertEquals("serviceType", dataRef.getDataType());
        assertEquals("http://serviceURL/", dataRef.getServiceURL());
        assertEquals("WMS", dataRef.getServiceSpec());
        assertEquals("defaultInstance", dataRef.getProviderOrServiceId());
        assertEquals(DefaultName.valueOf("myLayer"), dataRef.getLayerId());
        assertNull(dataRef.getDataVersion());

        //with date
        dataRef = new DataReference("${serviceType|http://serviceURL/|WMS|defaultInstance|myLayer|"+time+"}");
        assertEquals("serviceType", dataRef.getDataType());
        assertEquals("http://serviceURL/", dataRef.getServiceURL());
        assertEquals("WMS", dataRef.getServiceSpec());
        assertEquals("defaultInstance", dataRef.getProviderOrServiceId());
        assertEquals(DefaultName.valueOf("myLayer"), dataRef.getLayerId());
        assertEquals(currentDate, dataRef.getDataVersion());


        dataRef = DataReference.createProviderDataReference(DataReference.PROVIDER_STYLE_TYPE, "myStyleProvider", "myStyle");
        assertEquals("providerStyleType", dataRef.getDataType());
        assertEquals("myStyleProvider", dataRef.getProviderOrServiceId());
        assertEquals(DefaultName.valueOf("myStyle"), dataRef.getLayerId());
        assertNull(dataRef.getDataVersion());
        assertNull(dataRef.getServiceSpec());
        assertEquals("${providerStyleType|myStyleProvider|myStyle}", dataRef.getReference());

        dataRef = DataReference.createProviderDataReference(DataReference.PROVIDER_LAYER_TYPE, "myProvider", "myLayer", currentDate);
        assertEquals("providerLayerType", dataRef.getDataType());
        assertEquals("myProvider", dataRef.getProviderOrServiceId());
        assertEquals(DefaultName.valueOf("myLayer"), dataRef.getLayerId());
        assertEquals(currentDate, dataRef.getDataVersion());
        assertNull(dataRef.getServiceSpec());
        assertEquals("${providerLayerType|myProvider|myLayer|"+time+"}", dataRef.getReference());

        dataRef = DataReference.createServiceDataReference("http://localhost:8080/cstl/WS/WMS/defaultInstance2","WMS", "defaultInstance2", "myLayer10");
        assertEquals("serviceType", dataRef.getDataType());
        assertEquals("http://localhost:8080/cstl/WS/WMS/defaultInstance2", dataRef.getServiceURL());
        assertEquals("WMS", dataRef.getServiceSpec());
        assertEquals("defaultInstance2", dataRef.getProviderOrServiceId());
        assertEquals(DefaultName.valueOf("myLayer10"), dataRef.getLayerId());
        assertEquals("${serviceType|http://localhost:8080/cstl/WS/WMS/defaultInstance2|WMS|defaultInstance2|myLayer10}", dataRef.getReference());

        //with namespace
        dataRef = new DataReference("${providerLayerType|dcns-coastline|{http://geotoolkit.org}isoline}");
        assertEquals("providerLayerType", dataRef.getDataType());
        assertEquals("dcns-coastline", dataRef.getProviderOrServiceId());
        assertEquals(DefaultName.valueOf("{http://geotoolkit.org}isoline"), dataRef.getLayerId());
        assertNull(dataRef.getServiceSpec());

        dataRef = new DataReference("${serviceType|http://localhost:8080/cstl/WS/wfs/default3|WFS|default3|{shp}Countries}");
        assertEquals("serviceType", dataRef.getDataType());
        assertEquals("http://localhost:8080/cstl/WS/wfs/default3", dataRef.getServiceURL());
        assertEquals("WFS", dataRef.getServiceSpec());
        assertEquals("default3", dataRef.getProviderOrServiceId());
        assertEquals(DefaultName.valueOf("{shp}Countries"), dataRef.getLayerId());


    }

    @Test
    public void testDataReferencePatternFail () {
        DataReference dataRef = null;
        try {
            dataRef = new DataReference("providerLayerType|myProvider|myLayer");
            fail();

            dataRef = new DataReference("${providerLayerType|myProvider|myLayer");
            fail();

            dataRef = new DataReference("$providerLayerType|myProvider|myLayer}");
            fail();

            dataRef = new DataReference("${unknowType|myProvider|myLayer}");
            fail();

            dataRef = new DataReference("${providerLayerType|otherParam|myProvider|myLayer}");
            fail();

            dataRef = new DataReference("${providerLayerType|otherParam|myProvider|myLayer}");
            fail();
        } catch (IllegalArgumentException ex) {

        }

    }
}
