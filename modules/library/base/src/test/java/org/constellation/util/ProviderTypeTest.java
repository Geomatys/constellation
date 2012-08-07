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
import org.junit.Test;

/**
 *
 * @author Quentin Boileau (Geomatys)
 */
public class ProviderTypeTest {

    @Test
    public void testProviderReference () {

        ProviderReference providerRef = new ProviderReference("${providerLayerType|myProvider}");
        assertEquals("providerLayerType", providerRef.getProviderType());
        assertEquals("myProvider", providerRef.getProviderId());

        providerRef = new ProviderReference(ProviderReference.PROVIDER_LAYER_TYPE, "newProvider");
        assertEquals("providerLayerType", providerRef.getProviderType());
        assertEquals("newProvider", providerRef.getProviderId());
        assertEquals("${providerLayerType|newProvider}", providerRef.getReference());

        providerRef = new ProviderReference(ProviderReference.PROVIDER_STYLE_TYPE, "newProvider2");
        assertEquals("providerStyleType", providerRef.getProviderType());
        assertEquals("newProvider2", providerRef.getProviderId());
        assertEquals("${providerStyleType|newProvider2}", providerRef.getReference());

    }
}
