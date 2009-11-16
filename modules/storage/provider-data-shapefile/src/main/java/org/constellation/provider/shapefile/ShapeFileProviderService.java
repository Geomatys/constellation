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
package org.constellation.provider.shapefile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.constellation.provider.AbstractProviderService;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.LayerProviderService;
import org.constellation.provider.configuration.ProviderConfig;
import org.constellation.provider.configuration.ProviderSource;

import static org.constellation.provider.shapefile.ShapeFileProvider.*;

/**
 *
 * @version $Id$
 *
 * @author Johann Sorel (Geoamtys)
 */
public class ShapeFileProviderService extends AbstractProviderService<String,LayerDetails> implements LayerProviderService {

    /**
     * Default logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ShapeFileProviderService.class.getName());
    private static final String NAME = "shapefile";
    private static final String ERROR_MSG = "[PROVIDER]> Invalid shapefile provider config";

    private static final Collection<ShapeFileProvider> PROVIDERS = new ArrayList<ShapeFileProvider>();
    private static final Collection<ShapeFileProvider> IMMUTABLE = Collections.unmodifiableCollection(PROVIDERS);

    @Override
    public Collection<ShapeFileProvider> getProviders() {
        return IMMUTABLE;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void init(ProviderConfig config) {
        PROVIDERS.clear();
        for (final ProviderSource ps : config.sources) {
            try {
                ShapeFileProvider provider = new ShapeFileProvider(ps);
                PROVIDERS.add(provider);
                LOGGER.log(Level.INFO, "[PROVIDER]> shapefile provider created : " + provider.getSource().parameters.get(KEY_FOLDER_PATH));
            } catch (Exception ex) {
                // we should not catch exception, but here it's better to start all source we can
                // rather than letting a potential exception block the provider proxy
                LOGGER.log(Level.SEVERE, ERROR_MSG, ex);
            }
        }

    }

}
