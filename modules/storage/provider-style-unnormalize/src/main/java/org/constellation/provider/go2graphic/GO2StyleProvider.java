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

import java.awt.Color;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.constellation.provider.StyleProvider;
import org.geotoolkit.display2d.ext.rastermask.RasterMaskSymbolizer;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.display2d.ext.vectorfield.VectorFieldSymbolizer;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.MutableStyleFactory;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
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

    /**
     * {@inheritDoc }
     */
    @Override
    public Class<String> getKeyClass() {
        return String.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Class<MutableStyle> getValueClass() {
        return MutableStyle.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Set<String> getKeys() {
        return index.keySet();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean contains(String key) {
        return index.containsKey(key);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public MutableStyle get(String key) {
        return index.get(key);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void reload() {
        index.clear();
        visit();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void dispose() {
        index.clear();
    }
    
    private void visit() {
        final MutableStyleFactory sf = (MutableStyleFactory)FactoryFinder.getStyleFactory(
                            new Hints(Hints.STYLE_FACTORY, MutableStyleFactory.class));
        final Color TRANSLUCENT = new Color(0f, 0f, 0f, 0f);
        final FilterFactory ff = FactoryFinder.getFilterFactory(null);
        //TODO : find another way to load special styles.
        final Symbolizer symbol = new VectorFieldSymbolizer();
        index.put("GO2:VectorField", sf.style(symbol));

        // Defines intervals for a mask on raster.
        final Map<Expression, List<Symbolizer>> map = new HashMap<Expression, List<Symbolizer>>();
        map.put(null, Collections.singletonList((Symbolizer) sf.polygonSymbolizer(sf.stroke(TRANSLUCENT, 0d), sf.fill(Color.BLUE), null)));
        map.put(ff.literal(20), Collections.singletonList((Symbolizer) sf.polygonSymbolizer(sf.stroke(TRANSLUCENT, 0d), sf.fill(Color.RED), null)));
        map.put(ff.literal(50), Collections.singletonList((Symbolizer) sf.polygonSymbolizer(sf.stroke(TRANSLUCENT, 0d), sf.fill(Color.PINK), null)));
        map.put(ff.literal(75), Collections.singletonList((Symbolizer) sf.polygonSymbolizer(sf.stroke(TRANSLUCENT, 2d), sf.fill(Color.ORANGE), null)));
        map.put(ff.literal(100), Collections.singletonList((Symbolizer) sf.polygonSymbolizer(sf.stroke(TRANSLUCENT, 0d), sf.fill(Color.GRAY), null)));
        map.put(ff.literal(140), Collections.singletonList((Symbolizer) sf.polygonSymbolizer(sf.stroke(TRANSLUCENT, 0d), sf.fill(Color.GREEN), null)));
        map.put(ff.literal(180), Collections.singletonList((Symbolizer) sf.polygonSymbolizer(sf.stroke(TRANSLUCENT, 1d), sf.fill(Color.YELLOW), null)));
        index.put("GO2:RasterMask", sf.style(new RasterMaskSymbolizer(map)));
    }
    
}
