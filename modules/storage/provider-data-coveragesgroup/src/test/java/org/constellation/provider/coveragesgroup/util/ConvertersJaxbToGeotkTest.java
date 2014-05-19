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
