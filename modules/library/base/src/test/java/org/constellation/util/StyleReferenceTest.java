/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
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
 * @author Quentin Boileau (Geomatys)
 */
public class StyleReferenceTest {

    @Test
    public void testStyleReference () {

        Date currentDate = new Date();
        long time = currentDate.getTime();

        StyleReference styleReference = new StyleReference("${providerStyleType|myStyleProvider|myStyle}");
        assertEquals("providerStyleType", styleReference.getDataType());
        assertEquals("myStyleProvider", styleReference.getProviderId());
        assertEquals(DefaultName.valueOf("myStyle"), styleReference.getLayerId());
        assertNull(styleReference.getDataVersion());
        assertNull(styleReference.getServiceSpec());
        assertNull(styleReference.getServiceURL());
        assertNull(styleReference.getServiceId());


        styleReference =new StyleReference("myStyleProvider", "myStyle");
        assertEquals("providerStyleType", styleReference.getDataType());
        assertEquals("myStyleProvider", styleReference.getProviderId());
        assertEquals(DefaultName.valueOf("myStyle"), styleReference.getLayerId());
        assertNull(styleReference.getDataVersion());
        assertNull(styleReference.getServiceSpec());
        assertNull(styleReference.getServiceURL());
        assertNull(styleReference.getServiceId());

    }

    @Test
    public void testDataReferencePatternFail () {

        Date currentDate = new Date();
        long time = currentDate.getTime();
        StyleReference styleReference = null;
        try {
            styleReference = new StyleReference("${providerLayerType|myProvider|mylayer}");
            fail();

            styleReference = new StyleReference("${providerStyleType|myStyleProvider|myStyle|"+time+"}");
            fail();
        } catch (IllegalArgumentException ex) {

        }

    }
}
