/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.constellation.provider;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import org.constellation.provider.configuration.ConfigDirectory;
import org.constellation.util.Util;
import org.opengis.feature.type.Name;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class NamedLayerProviderProxy implements NamedLayerProvider {

    /**
     * Default logger.

    private static final Logger LOGGER = Logger.getLogger(LayerProviderProxy.class.getName());*/

    private static final Collection<NamedLayerProviderService> SERVICES = new ArrayList<NamedLayerProviderService>();

    private static String CONFIG_PATH = null;

    private static NamedLayerProviderProxy INSTANCE = null;


    private NamedLayerProviderProxy(){}


    /**
     * {@inheritDoc }
     */
    @Override
    public Class<Name> getKeyClass() {
        return Name.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Class<LayerDetails> getValueClass() {
        return LayerDetails.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Set<Name> getKeys() {

        final Set<Name> keys = new HashSet<Name>();

        for(NamedLayerProviderService service : SERVICES){
            for(NamedLayerProvider provider : service.getProviders()){
                keys.addAll( provider.getKeys() );
            }
        }

        return keys;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean contains(Name key) {

        for(NamedLayerProviderService service : SERVICES){
            for(NamedLayerProvider provider : service.getProviders()){
                if(provider.contains(key)) return true;
            }
        }

        return false;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public LayerDetails get(Name key) {

        for(NamedLayerProviderService service : SERVICES){
            for(NamedLayerProvider provider : service.getProviders()){
                final LayerDetails layer = provider.get(key);
                if(layer != null) return layer;
            }
        }

        return null;
    }

    /**
     * {@inheritDoc }
     */
    public List<String> getFavoriteStyles(Name layerName) {
        final List<String> styles = new ArrayList<String>();

        for(NamedLayerProviderService service : SERVICES){
            for(NamedLayerProvider provider : service.getProviders()){
                final List<String> sts = provider.get(layerName).getFavoriteStyles();
                styles.addAll(sts);
            }
        }

        return styles;
    }

    public Collection<NamedLayerProviderService> getServices() {
        return Collections.unmodifiableCollection(SERVICES);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void reload() {

        for(NamedLayerProviderService service : SERVICES){
            for(NamedLayerProvider provider : service.getProviders()){
                provider.reload();
            }
        }

    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void dispose() {

        for(NamedLayerProviderService service : SERVICES){
            for(NamedLayerProvider provider : service.getProviders()){
                provider.dispose();
            }
        }

        SERVICES.clear();

    }

    private static synchronized void init(final String configPath){
        if(configPath == null){
            throw new NullPointerException("Configuration path can not be null.");
        }

        if(CONFIG_PATH != null){
            throw new IllegalStateException("The layer provider proxy has already been initialize");
        }

        CONFIG_PATH = configPath;

        final ServiceLoader<NamedLayerProviderService> loader = ServiceLoader.load(NamedLayerProviderService.class);
        for(final NamedLayerProviderService service : loader){
            final String name = service.getName();
            final String fileName = name + ".xml";
            /*
             * First check that there are config files in the WEB-INF/classes directory
             */
            File configFile = Util.getFileFromResource(fileName);
            /*
             * No config file in the resources, then we try with the default config directory.
             */
            if (configFile == null || !configFile.exists()) {
                final String path = CONFIG_PATH + fileName;
                configFile = new File(path);
            }
            /*
             * HACK for ifremer.
             */
            if (!configFile.exists() && name.equals("postgrid")) {
                File warFile = ConfigDirectory.getWarPackagedConfig();
                if(warFile != null){
                    configFile = warFile;
                }
            }


            service.setConfiguration(configFile);

            SERVICES.add(service);
        }

    }

    public static synchronized NamedLayerProviderProxy getInstance(){
        if(INSTANCE == null){
            init(ConfigDirectory.getConfigDirectory().getPath() + File.separator);
            INSTANCE = new NamedLayerProviderProxy();
        }

        return INSTANCE;
    }
}
