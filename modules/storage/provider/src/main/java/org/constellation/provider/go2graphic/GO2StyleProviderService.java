/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.constellation.provider.go2graphic;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.constellation.provider.StyleProviderService;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class GO2StyleProviderService implements StyleProviderService{

    /**
     * Default logger.
     */
    private static final Logger LOGGER = Logger.getLogger(GO2StyleProviderService.class.getName());
    private static final String NAME = "go2style";

    private static final Collection<GO2StyleProvider> PROVIDERS = new ArrayList<GO2StyleProvider>();
    private static final Collection<GO2StyleProvider> IMMUTABLE = Collections.unmodifiableCollection(PROVIDERS);

    private static File CONFIG_FILE = null;

    @Override
    public Collection<GO2StyleProvider> getProviders() {
        return IMMUTABLE;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void init(File file) {
        if(file == null){
            throw new NullPointerException("Configuration file can not be null");
        }

        if(CONFIG_FILE != null){
            throw new IllegalStateException("The GO2 style provider service has already been initialize");
        }

        //GO2 Style are hard coded java objects with no property configuration
        PROVIDERS.add(new GO2StyleProvider());

        LOGGER.log(Level.INFO, "[PROVIDER]> GO2 style provider created : " + "GO2:VectorField");

    }

}
