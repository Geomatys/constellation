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

package org.constellation.provider.coveragesgroup.util;

import org.constellation.provider.coveragesgroup.xml.DataReference;
import org.constellation.provider.coveragesgroup.xml.MapLayer;
import org.constellation.provider.coveragesgroup.xml.StyleReference;
import org.geotoolkit.map.MapItem;
import static org.junit.Assert.*;
import org.junit.Test;


/**
 *
 * @author Cédric Briançon (Geomatys)
 */
public class ConvertersJaxbToGeotkTest {

    @Test
    public void testConvertsMapLayer() {
        final StyleReference styleRef = new StyleReference("myStyle");
        final DataReference dataRef =
                new DataReference("${providerLayerType|myProvider|myLayer}");
        final MapLayer mapLayer = new MapLayer(dataRef, styleRef);

        final MapItem item = ConvertersJaxbToGeotk.convertsMapLayer(mapLayer, null, null);
        assertNotNull(item);
    }

    @Test
    public void testConvertsMapLayerWithDataReferenceStyle() {
        final StyleReference styleRef =
                new StyleReference("${providerLayerType|sld|myStyle}");
        final DataReference dataRef =
                new DataReference("${providerLayerType|myProvider|myLayer}");
        final MapLayer mapLayer = new MapLayer(dataRef, styleRef);

        final MapItem item = ConvertersJaxbToGeotk.convertsMapLayer(mapLayer, null, null);
        assertNotNull(item);
    }
}
