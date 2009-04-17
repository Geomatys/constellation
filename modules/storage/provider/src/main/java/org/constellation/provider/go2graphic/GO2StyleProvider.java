/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2009, Geomatys
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
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.factory.Hints;
import org.geotools.display2d.ext.vectorfield.VectorFieldSymbolizer;
import org.geotools.style.MutableStyle;
import org.geotools.style.MutableStyleFactory;
import org.opengis.style.Symbolizer;

/**
 *
 * @version $Id$
 *
 * @author Johann Sorel (Geomatys)
 */
public class GO2StyleProvider implements StyleProvider{
    
    private final Map<String,MutableStyle> index = new HashMap<String,MutableStyle>();
    
    
    protected GO2StyleProvider(){
        visit();
    }
    
    public Class<String> getKeyClass() {
        return String.class;
    }

    public Class<MutableStyle> getValueClass() {
        return MutableStyle.class;
    }

    public Set<String> getKeys() {
        return index.keySet();
    }

    public boolean contains(String key) {
        return index.containsKey(key);
    }

    public MutableStyle get(String key) {
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
        MutableStyleFactory sf = (MutableStyleFactory)FactoryFinder.getStyleFactory(
                            new Hints(Hints.STYLE_FACTORY, MutableStyleFactory.class));
        //TODO : find another way to load special styles.
        Symbolizer symbol = new VectorFieldSymbolizer();

        index.put("GO2:VectorField", sf.style(symbol));
    }
    
}
