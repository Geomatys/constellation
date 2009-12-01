/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;

import org.constellation.provider.AbstractProviderService;
import org.constellation.provider.StyleProviderService;
import org.constellation.provider.configuration.ProviderSource;

import org.geotoolkit.style.MutableStyle;

/**
 *
 * @version $Id$
 *
 * @author Johann Sorel (Geomatys)
 */
public class GO2StyleProviderService extends AbstractProviderService<String,MutableStyle> implements StyleProviderService {

    /**
     * Default logger.
     */
    private static final Logger LOGGER = Logger.getLogger(GO2StyleProviderService.class.getName());
    private static final Collection<GO2StyleProvider> PROVIDERS = new ArrayList<GO2StyleProvider>();
    private static final Collection<GO2StyleProvider> IMMUTABLE = Collections.unmodifiableCollection(PROVIDERS);

    public GO2StyleProviderService() {
        super("go2style");
        PROVIDERS.clear();
        //GO2 Style are hard coded java objects with no property configuration
        PROVIDERS.add(new GO2StyleProvider());
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Collection<GO2StyleProvider> getProviders() {
        return IMMUTABLE;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    protected void disposeProviders() {
    }

    /**
     * {@inheritDoc }
     */
    @Override
    protected void loadProvider(ProviderSource ps) {
    }

}
