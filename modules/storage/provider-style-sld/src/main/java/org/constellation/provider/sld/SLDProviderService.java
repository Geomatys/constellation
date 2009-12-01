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
package org.constellation.provider.sld;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.constellation.provider.AbstractProviderService;
import org.constellation.provider.StyleProviderService;
import org.constellation.provider.configuration.ProviderSource;

import org.geotoolkit.style.MutableStyle;

import static org.constellation.provider.sld.SLDProvider.*;


/**
 *
 * @version $Id$
 *
 * @author Johann Sorel (Geomatys)
 */
public class SLDProviderService extends AbstractProviderService<String,MutableStyle> implements StyleProviderService {

    /**
     * Default logger.
     */
    private static final Logger LOGGER = Logger.getLogger(SLDProviderService.class.getName());
    private static final String ERROR_MSG = "[PROVIDER]> Invalid SLD provider config";

    private static final Collection<SLDProvider> PROVIDERS = new ArrayList<SLDProvider>();
    private static final Collection<SLDProvider> IMMUTABLE = Collections.unmodifiableCollection(PROVIDERS);

    public SLDProviderService(){
        super("sld");
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Collection<SLDProvider> getProviders() {
        return IMMUTABLE;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    protected void disposeProviders() {
        for(final SLDProvider provider : PROVIDERS){
            provider.dispose();
            PROVIDERS.remove(provider);
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    protected void loadProvider(ProviderSource ps) {
        try {
            SLDProvider provider = new SLDProvider(ps);
            PROVIDERS.add(provider);
            LOGGER.log(Level.INFO, "[PROVIDER]> SLD provider created : " + provider.getSource().parameters.get(KEY_FOLDER_PATH));
        } catch (Exception ex) {
            // we should not catch exception, but here it's better to start all source we can
            // rather than letting a potential exception block the provider proxy
            LOGGER.log(Level.SEVERE, ERROR_MSG, ex);
        }
    }

}
