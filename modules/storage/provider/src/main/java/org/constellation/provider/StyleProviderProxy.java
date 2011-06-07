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
package org.constellation.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.MutableStyleFactory;


/**
 * Main Data provider for styles objects. This class act as a proxy for
 * several SLD folder providers.
 *
 * @version $Id$
 * @author Johann Sorel (Geomatys)
 */
public final class StyleProviderProxy extends AbstractProviderProxy
        <String,MutableStyle,StyleProvider,StyleProviderService> implements StyleProvider{

    public static final MutableStyleFactory STYLE_FACTORY = (MutableStyleFactory)
            FactoryFinder.getStyleFactory(new Hints(Hints.STYLE_FACTORY, MutableStyleFactory.class));

    public static final RandomStyleFactory STYLE_RANDOM_FACTORY = new RandomStyleFactory();

    private static final Collection<StyleProviderService> SERVICES;
    static {
        final List<StyleProviderService> cache = new ArrayList<StyleProviderService>();
        final ServiceLoader<StyleProviderService> loader = ServiceLoader.load(StyleProviderService.class);
        for(final StyleProviderService service : loader){
            cache.add(service);
        }
        SERVICES = Collections.unmodifiableCollection(cache);
    }

    private static final StyleProviderProxy INSTANCE = new StyleProviderProxy();

    private StyleProviderProxy(){}

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

    @Override
    public MutableStyle getByIdentifier(String key) {
        return get(key);
    }

    @Override
    public Collection<StyleProviderService> getServices() {
        return SERVICES;
    }

    /**
     * @return null, this provider does not have a service.
     */
    @Override
    public ProviderService<String, MutableStyle, Provider<String, MutableStyle>> getService() {
        return null;
    }

    /**
     * Returns the current instance of {@link StyleProviderProxy}.
     */
    public static StyleProviderProxy getInstance(){
        return INSTANCE;
    }

    @Override
    public void set(String key, MutableStyle style) {
        throw new UnsupportedOperationException("Not supported. Proxy class is immutable.");
    }

    @Override
    public void rename(String key, String newName) {
        throw new UnsupportedOperationException("Not supported yet. Proxy class is immutable.");
    }

    @Override
    public void remove(String key) {
        throw new UnsupportedOperationException("Not supported yet. Proxy class is immutable.");
    }

}
