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
package org.constellation.provider.postgrid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.constellation.provider.AbstractProviderService;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.LayerProviderService;
import org.constellation.provider.Provider;
import org.constellation.provider.configuration.ProviderConfig;
import org.constellation.provider.configuration.ProviderSource;

import org.geotoolkit.util.logging.Logging;

import static org.constellation.provider.postgrid.PostGridProvider.*;

/**
 *
 * @version $Id$
 *
 * @author Johann Sorel (Geomatys)
 */
public class PostGridProviderService extends AbstractProviderService<String,LayerDetails> implements LayerProviderService {

    /**
     * Default logger.
     */
    private static final Logger LOGGER = Logging.getLogger(PostGridProviderService.class);
    private static final String ERROR_MSG = "[PROVIDER]> Invalid postgrid provider config";

    private static final Collection<PostGridProvider> PROVIDERS = new ArrayList<PostGridProvider>();
    private static final Collection<PostGridProvider> IMMUTABLE = Collections.unmodifiableCollection(PROVIDERS);

    public PostGridProviderService(){
        super("postgrid");
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Collection<PostGridProvider> getProviders() {
        return IMMUTABLE;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    protected void disposeProvider(Provider provider) {
        if(PROVIDERS.contains(provider)){
            provider.dispose();
            PROVIDERS.remove(provider);
        }else{
            throw new IllegalArgumentException("This provider doesn't belong to this service.");
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    protected void loadProvider(ProviderSource ps){
        try {
            final PostGridProvider provider = new PostGridProvider(ps);
            PROVIDERS.add(provider);
            LOGGER.log(Level.INFO, "[PROVIDER]> postgrid provider created : "
                    + provider.getSource().parameters.get(KEY_DATABASE) + " > "
                    + provider.getSource().parameters.get(KEY_ROOT_DIRECTORY));
        } catch (Exception ex) {
            // we should not catch exception, but here it's better to start all source we can
            // rather than letting a potential exception block the provider proxy
            LOGGER.log(Level.SEVERE, ERROR_MSG, ex);
        }
    }

}
