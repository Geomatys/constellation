/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.constellation.provider.sld;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.constellation.provider.StyleProviderService;
import org.constellation.provider.configuration.ProviderConfig;
import org.constellation.provider.configuration.ProviderSource;
import org.xml.sax.SAXException;

import static org.constellation.provider.sld.SLDProvider.*;


/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class SLDProviderService implements StyleProviderService{

    /**
     * Default logger.
     */
    private static final Logger LOGGER = Logger.getLogger(SLDProviderService.class.getName());
    private static final String NAME = "sld";

    private static final Collection<SLDProvider> PROVIDERS = new ArrayList<SLDProvider>();
    private static final Collection<SLDProvider> IMMUTABLE = Collections.unmodifiableCollection(PROVIDERS);

    private static File CONFIG_FILE = null;

    @Override
    public Collection<SLDProvider> getProviders() {
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
            throw new IllegalStateException("The SLD provider service has already been initialize");
        }

        SLDProviderService.CONFIG_FILE = file;

        ProviderConfig config = null;
        try {
            config = ProviderConfig.read(file);
        } catch (ParserConfigurationException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        if(config == null) return;

        for (final ProviderSource ps : config.sources) {
            try {
                SLDProvider provider = new SLDProvider(ps);
                PROVIDERS.add(provider);
                LOGGER.log(Level.INFO, "[PROVIDER]> SLD provider created : " + provider.getSource().parameters.get(KEY_FOLDER_PATH));
            } catch (IllegalArgumentException ex) {
                LOGGER.log(Level.WARNING, "Invalide SLD provider config", ex);
            }
        }

    }

}
