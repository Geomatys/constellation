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

import java.io.IOException;
import java.sql.SQLException;
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

import static org.constellation.provider.postgrid.PostGridProvider.*;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class PostGridProviderService extends AbstractProviderService<String,LayerDetails> implements LayerProviderService {

    /**
     * Default logger.
     */
    private static final Logger LOGGER = Logger.getLogger(PostGridProviderService.class.getName());
    private static final String NAME = "postgrid";

    private static final Collection<PostGridProvider> PROVIDERS = new ArrayList<PostGridProvider>();
    private static final Collection<PostGridProvider> IMMUTABLE = Collections.unmodifiableCollection(PROVIDERS);

    @Override
    public Collection<PostGridProvider> getProviders() {
        return IMMUTABLE;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void init(ProviderConfig provConf) {
        PROVIDERS.clear();

        for (final ProviderSource ps : provConf.sources) {
            try {
                PostGridProvider provider = new PostGridProvider(ps);
                PROVIDERS.add(provider);
                LOGGER.log(Level.INFO, "[PROVIDER]> postgrid provider created : "
                        + provider.getSource().parameters.get(KEY_DATABASE) + " > "
                        + provider.getSource().parameters.get(KEY_ROOT_DIRECTORY));
            } catch (IllegalArgumentException ex) {
                LOGGER.log(Level.WARNING, "[PROVIDER]> Invalide postgrid provider config", ex);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "[PROVIDER]> Invalide postgrid provider config", ex);
            } catch (SQLException ex) {
                LOGGER.log(Level.WARNING, "[PROVIDER]> Invalide postgrid provider config", ex);
            }
        }
    }

}
