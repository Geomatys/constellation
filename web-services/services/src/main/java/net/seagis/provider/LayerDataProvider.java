
package net.seagis.provider;

import java.util.List;
import org.geotools.map.MapLayer;
import org.geotools.style.MutableStyle;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public interface LayerDataProvider<K,V> extends DataProvider<K,V>{

    MapLayer get(String layerName, MutableStyle style);
    
    List<String> getFavoriteStyles(String layerName);
    
    
}
