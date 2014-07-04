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

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Quentin Boileau (Geomatys)
 */
public class ProviderTypeTest {

    @Test
    public void testProviderReference () {

        Date currentDate = new Date();
        long time = currentDate.getTime();
        
        ProviderReference providerRef = new ProviderReference("${providerLayerType|myProvider}");
        assertEquals("providerLayerType", providerRef.getProviderType());
        assertEquals("myProvider", providerRef.getProviderId());
        assertEquals(null, providerRef.getDataVersion());

        //with date
        providerRef = new ProviderReference("${providerLayerType|myProvider|"+time+"}");
        assertEquals("providerLayerType", providerRef.getProviderType());
        assertEquals("myProvider", providerRef.getProviderId());
        assertEquals(currentDate, providerRef.getDataVersion());

        providerRef = new ProviderReference(ProviderReference.PROVIDER_LAYER_TYPE, "newProvider");
        assertEquals("providerLayerType", providerRef.getProviderType());
        assertEquals("newProvider", providerRef.getProviderId());
        assertEquals(null, providerRef.getDataVersion());
        assertEquals("${providerLayerType|newProvider}", providerRef.getReference());
        //with date
        providerRef = new ProviderReference(ProviderReference.PROVIDER_LAYER_TYPE, "newProvider", currentDate);
        assertEquals("providerLayerType", providerRef.getProviderType());
        assertEquals("newProvider", providerRef.getProviderId());
        assertEquals(currentDate, providerRef.getDataVersion());
        assertEquals("${providerLayerType|newProvider|"+time+"}", providerRef.getReference());

        providerRef = new ProviderReference(ProviderReference.PROVIDER_STYLE_TYPE, "newProvider2");
        assertEquals("providerStyleType", providerRef.getProviderType());
        assertEquals("newProvider2", providerRef.getProviderId());
        assertEquals(null, providerRef.getDataVersion());
        assertEquals("${providerStyleType|newProvider2}", providerRef.getReference());

    }
}
