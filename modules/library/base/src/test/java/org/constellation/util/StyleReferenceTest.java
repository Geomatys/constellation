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

import org.geotoolkit.util.NamesExt;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

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
        assertEquals(NamesExt.valueOf("myStyle"), styleReference.getLayerId());
        assertNull(styleReference.getDataVersion());
        assertNull(styleReference.getServiceSpec());
        assertNull(styleReference.getServiceURL());
        assertNull(styleReference.getServiceId());


        styleReference =new StyleReference("myStyleProvider", "myStyle");
        assertEquals("providerStyleType", styleReference.getDataType());
        assertEquals("myStyleProvider", styleReference.getProviderId());
        assertEquals(NamesExt.valueOf("myStyle"), styleReference.getLayerId());
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
