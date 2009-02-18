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
package org.constellation.provider.go2graphic;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.constellation.provider.StyleProvider;
import org.geotools.display.container.GridMarkGraphicBuilder;
import org.geotools.map.GraphicBuilder;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class GO2StyleProvider implements StyleProvider{
    
    private final Map<String,GraphicBuilder> index = new HashMap<String,GraphicBuilder>();
    
    
    protected GO2StyleProvider(){
        visit();
    }
    
    public Class<String> getKeyClass() {
        return String.class;
    }

    public Class<Object> getValueClass() {
        return Object.class;
    }

    public Set<String> getKeys() {
        return index.keySet();
    }

    public boolean contains(String key) {
        return index.containsKey(key);
    }

    public GraphicBuilder get(String key) {
        return index.get(key);
    }

    public void reload() {
        index.clear();
        visit();
    }

    public void dispose() {
        index.clear();
    }
    
    private void visit() {
        //TODO : find another way to load special styles.
        index.put("GO2:VectorField", new GridMarkGraphicBuilder());
    }
    
}
