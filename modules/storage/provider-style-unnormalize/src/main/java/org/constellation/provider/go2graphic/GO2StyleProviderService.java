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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.constellation.provider.StyleProvider;
import org.constellation.provider.StyleProviderService;
import org.constellation.provider.configuration.ProviderConfig;

/**
 *
 * @version $Id$
 *
 * @author Johann Sorel (Geomatys)
 */
public class GO2StyleProviderService implements StyleProviderService {

    /**
     * Default logger.
     */
    private static final Logger LOGGER = Logger.getLogger(GO2StyleProviderService.class.getName());
    private static final String NAME = "go2style";

    private static final Collection<GO2StyleProvider> PROVIDERS = new ArrayList<GO2StyleProvider>();
    private static final Collection<GO2StyleProvider> IMMUTABLE = Collections.unmodifiableCollection(PROVIDERS);

    private static File CONFIG_FILE = null;

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
    public String getName() {
        return NAME;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void init(File file) {
        if(file == null){
            throw new NullPointerException("Configuration file can not be null");
        }

        if(CONFIG_FILE != null){
            throw new IllegalStateException("The GO2 style provider service has already been initialize");
        }

        init((ProviderConfig)null);
        final StringBuilder sb = new StringBuilder("[PROVIDER]> GO2 style provider created : ");
        for (StyleProvider sp : getProviders()) {
            for (String key : sp.getKeys()) {
                sb.append(key).append(' ');
            }
        }
        LOGGER.log(Level.INFO, sb.toString());
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void init(ProviderConfig config) {
        PROVIDERS.clear();
        //GO2 Style are hard coded java objects with no property configuration
        PROVIDERS.add(new GO2StyleProvider());
    }

}
