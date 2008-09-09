/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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
