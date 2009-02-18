package org.constellation.provider.postgrid;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
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

import static org.constellation.provider.postgrid.PostGridProvider.*;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class PostGridProviderService implements LayerProviderService {

    /**
     * Default logger.
     */
    private static final Logger LOGGER = Logger.getLogger(PostGridProviderService.class.getName());
    private static final String NAME = "postgrid";

    private static final Collection<PostGridProvider> PROVIDERS = new ArrayList<PostGridProvider>();
    private static final Collection<PostGridProvider> IMMUTABLE = Collections.unmodifiableCollection(PROVIDERS);

    private static File CONFIG_FILE = null;

    @Override
    public Collection<PostGridProvider> getProviders() {
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
            throw new IllegalStateException("The postgrid provider service has already been initialize");
        }

        PostGridProviderService.CONFIG_FILE = file;

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
