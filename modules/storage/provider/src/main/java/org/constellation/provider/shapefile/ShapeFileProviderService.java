package org.constellation.provider.shapefile;

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

import static org.constellation.provider.shapefile.ShapeFileProvider.*;

/**
 *
 * @author Johann Sorel (Geoamtys)
 */
public class ShapeFileProviderService implements LayerProviderService {

    /**
     * Default logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ShapeFileProviderService.class.getName());
    private static final String NAME = "shapefile";

    private static final Collection<ShapeFileProvider> PROVIDERS = new ArrayList<ShapeFileProvider>();
    private static final Collection<ShapeFileProvider> IMMUTABLE = Collections.unmodifiableCollection(PROVIDERS);

    private static File CONFIG_FILE = null;

    @Override
    public Collection<ShapeFileProvider> getProviders() {
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
            throw new IllegalStateException("The shapefile provider service has already been initialize");
        }

        ShapeFileProviderService.CONFIG_FILE = file;

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
                ShapeFileProvider provider = new ShapeFileProvider(ps);
                PROVIDERS.add(provider);
                LOGGER.log(Level.INFO, "[PROVIDER]> shapefile provider created : " + provider.getSource().parameters.get(KEY_FOLDER_PATH));
            } catch (IllegalArgumentException ex) {
                LOGGER.log(Level.WARNING, "Invalide shapefile provider config", ex);
            }
        }

    }

}
