/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2012, Geomatys
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

package org.constellation.json.util;

import org.apache.sis.util.Static;
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.sld.DefaultSLDFactory;
import org.geotoolkit.sld.MutableSLDFactory;
import org.geotoolkit.style.MutableStyleFactory;
import org.opengis.filter.FilterFactory2;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class StyleFactories extends Static {

    /**
     * SLD factory.
     */
    public static final MutableSLDFactory SLDF = new DefaultSLDFactory();

    /**
     * Style elements factory.
     */
    public static final MutableStyleFactory SF = (MutableStyleFactory) FactoryFinder.getStyleFactory(
            new Hints(Hints.STYLE_FACTORY, MutableStyleFactory.class));

    /**
     * Filters factory.
     */
    public static final FilterFactory2 FF = (FilterFactory2) FactoryFinder.getFilterFactory(
            new Hints(Hints.FILTER_FACTORY, FilterFactory2.class));
}
