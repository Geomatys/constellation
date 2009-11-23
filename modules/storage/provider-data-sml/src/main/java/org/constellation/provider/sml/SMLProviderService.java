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
package org.constellation.provider.sml;

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

import static org.constellation.provider.sml.SMLProvider.*;
/**
 *
 * @version $Id: ShapeFileProviderService.java 1950 2009-11-16 09:59:25Z eclesia $
 *
 * @author Johann Sorel (Geoamtys)
 * @author Guilhem Legal (Geomatys)
 */
public class SMLProviderService extends AbstractProviderService<String,LayerDetails> implements LayerProviderService {

    /**
     * Default logger.
     */
    private static final Logger LOGGER = Logger.getLogger(SMLProviderService.class.getName());
    private static final String NAME = "sensorML";
    private static final String ERROR_MSG = "[PROVIDER]> Invalid sensorML provider config";

    private static final Collection<SMLProvider> PROVIDERS = new ArrayList<SMLProvider>();
    private static final Collection<SMLProvider> IMMUTABLE = Collections.unmodifiableCollection(PROVIDERS);

    @Override
    public Collection<SMLProvider> getProviders() {
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
                SMLProvider provider = new SMLProvider(ps);
                PROVIDERS.add(provider);
                LOGGER.log(Level.INFO, "[PROVIDER]> sensorML provider created : "
                        + provider.getSource().parameters.get(KEY_HOST) + " > "
                        + provider.getSource().parameters.get(KEY_DATABASE));
            } catch (Exception ex) {
                // we should not catch exception, but here it's better to start all source we can
                // rather than letting a potential exception block the provider proxy
                LOGGER.log(Level.SEVERE, ERROR_MSG, ex);
            }
        }

    }

}
