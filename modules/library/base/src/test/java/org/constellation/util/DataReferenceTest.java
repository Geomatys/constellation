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
package org.constellation.util;

import org.geotoolkit.feature.type.DefaultName;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

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
