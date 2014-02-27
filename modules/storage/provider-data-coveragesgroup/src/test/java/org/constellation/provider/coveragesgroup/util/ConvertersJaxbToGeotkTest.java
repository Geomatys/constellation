
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
