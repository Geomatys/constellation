package org.constellation.provider.postgis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.constellation.provider.LayerProviderService;
import org.constellation.provider.configuration.ProviderConfig;
import org.constellation.provider.configuration.ProviderSource;
import org.xml.sax.SAXException;

import static org.constellation.provider.postgis.PostGisProvider.*;

/**
 *
 * @author Johann Sorel (Geoamtys)
 */
public class PostGisProviderService implements LayerProviderService {

    /**
     * Default logger.
     */
    private static final Logger LOGGER = Logger.getLogger(PostGisProviderService.class.getName());
    private static final String NAME = "postgis";

    private static final Collection<PostGisProvider> PROVIDERS = new ArrayList<PostGisProvider>();
    private static final Collection<PostGisProvider> IMMUTABLE = Collections.unmodifiableCollection(PROVIDERS);

    private static File CONFIG_FILE = null;

    @Override
    public Collection<PostGisProvider> getProviders() {
        return IMMUTABLE;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public synchronized void init(File file) {
        if(file == null){
            throw new NullPointerException("Configuration file can not be null");
        }

        if(CONFIG_FILE != null){
            throw new IllegalStateException("The postgis provider service has already been initialize");
        }

        PostGisProviderService.CONFIG_FILE = file;

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
                PostGisProvider provider = new PostGisProvider(ps);
                PROVIDERS.add(provider);
                LOGGER.log(Level.INFO, "[PROVIDER]> postgis provider created : " 
                        + provider.getSource().parameters.get(KEY_HOST) + " > "
                        + provider.getSource().parameters.get(KEY_DATABASE));
            } catch (IllegalArgumentException ex) {
                LOGGER.log(Level.WARNING, "Invalide postgis provider config", ex);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Invalide postgis provider config", ex);
            }
        }

    }

}
