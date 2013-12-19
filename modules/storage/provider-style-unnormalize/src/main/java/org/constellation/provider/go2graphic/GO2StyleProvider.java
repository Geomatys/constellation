/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2010, Geomatys
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
import javax.measure.unit.Unit;

import org.apache.sis.measure.NumberRange;
import org.constellation.provider.AbstractStyleProvider;
import org.geotoolkit.display2d.ext.dimrange.DimRangeSymbolizer;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.MutableStyleFactory;
import org.apache.sis.measure.MeasurementRange;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.style.Symbolizer;

/**
 *
 * @version $Id$
 *
 * @author Johann Sorel (Geomatys)
 */
public class GO2StyleProvider extends AbstractStyleProvider{
    
    private final Map<String,MutableStyle> index = new HashMap<>();
    
    
    protected GO2StyleProvider(final GO2StyleProviderService service,ParameterValueGroup desc){
        super(service,desc);
        visit();
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
    public MutableStyle get(final String key) {
        return index.get(key);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void reload() {
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void dispose() {
    }
    
    private void visit() {
        final MutableStyleFactory sf = (MutableStyleFactory)FactoryFinder.getStyleFactory(
                            new Hints(Hints.STYLE_FACTORY, MutableStyleFactory.class));

// TODO : find another way to load special styles (31/07/13 the class has disappeared).
//        final Symbolizer symbol1 = new VectorFieldSymbolizer();
//        index.put("GO2:VectorField", sf.style(symbol1));
        
        final Symbolizer symbol2 = new DimRangeSymbolizer(new MeasurementRange(NumberRange.create(10, true, 20, true), Unit.ONE));
        index.put("GO2:DimRange", sf.style(symbol2));
    }

    @Override
    public void set(String key, MutableStyle style) {
        throw new UnsupportedOperationException("Not supported. This provider is immutable.");
    }

    @Override
    public void rename(String key, String newName) {
        throw new UnsupportedOperationException("Not supported. This provider is immutable.");
    }

    @Override
    public void remove(String key) {
        throw new UnsupportedOperationException("Not supported. This provider is immutable.");
    }

}
