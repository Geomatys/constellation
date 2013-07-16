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

            styleReference = new StyleReference("${providerStyleType|myStyleProvider|myStyle|time}");
            fail();
        } catch (IllegalArgumentException ex) {

        }

    }
}
