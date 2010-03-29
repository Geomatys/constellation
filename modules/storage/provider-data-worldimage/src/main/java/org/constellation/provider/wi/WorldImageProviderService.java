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
package org.constellation.provider.wi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.constellation.provider.AbstractProviderService;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.LayerProviderService;
import org.constellation.provider.Provider;
import org.constellation.provider.configuration.ProviderSource;
import org.geotoolkit.image.io.plugin.WorldFileImageReader;
import org.geotoolkit.image.io.plugin.WorldFileImageWriter;
import org.geotoolkit.image.jai.Registry;

import org.geotoolkit.util.logging.Logging;
import org.opengis.feature.type.Name;

import static org.constellation.provider.wi.WorldImageProvider.*;

/**
 * Service providing world image files coverag reader.
 * @version $Id$
 *
 * @author Johann Sorel (Geomatys)
 */
public class WorldImageProviderService extends AbstractProviderService<Name,LayerDetails> implements LayerProviderService {

    /**
     * Default logger.
     */
    private static final Logger LOGGER = Logging.getLogger(WorldImageProviderService.class);
    private static final String ERROR_MSG = "[PROVIDER]> Invalid world image provider config";

    private static final Collection<WorldImageProvider> PROVIDERS = new ArrayList<WorldImageProvider>();
    private static final Collection<WorldImageProvider> IMMUTABLE = Collections.unmodifiableCollection(PROVIDERS);

    static {
        Registry.setDefaultCodecPreferences();
        WorldFileImageReader.Spi.registerDefaults(null);
        WorldFileImageWriter.Spi.registerDefaults(null);
    }

    public WorldImageProviderService(){
        super("worldimage");
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Collection<WorldImageProvider> getProviders() {
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
            final WorldImageProvider provider = new WorldImageProvider(ps);
            PROVIDERS.add(provider);
            LOGGER.log(Level.INFO, "[PROVIDER]> world image provider created : "
                    + provider.getSource().parameters.get(KEY_FOLDER_PATH));
        } catch (Exception ex) {
            // we should not catch exception, but here it's better to start all source we can
            // rather than letting a potential exception block the provider proxy
            LOGGER.log(Level.SEVERE, ERROR_MSG, ex);
        }
    }

}
