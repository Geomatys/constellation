/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2010, Geomatys
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
package org.constellation.provider;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

import org.constellation.provider.configuration.ConfigDirectory;
import org.geotoolkit.map.ElevationModel;
import org.geotoolkit.util.FileUtilities;
import org.opengis.feature.type.Name;

/**
 * Main data provider for MapLayer objects. This class act as a proxy for 
 * different kind of data sources, postgrid, shapefile ...
 *
 * @version $Id$
 *
 * @author Johann Sorel (Geomatys)
 */
public class LayerProviderProxy extends AbstractLayerProvider{

    /**
     * Default logger.
     
    private static final Logger LOGGER = Logger.getLogger(LayerProviderProxy.class.getName());*/

    private static final Collection<LayerProviderService> SERVICES = new ArrayList<LayerProviderService>();

    private static String CONFIG_PATH = null;

    private static LayerProviderProxy INSTANCE = null;


    private LayerProviderProxy(){}
    
    /**
     * {@inheritDoc }
     */
    @Override
    public Set<Name> getKeys() {

        final Set<Name> keys = new HashSet<Name>();

        for(LayerProviderService service : SERVICES){
            for(LayerProvider provider : service.getProviders()){
                keys.addAll( provider.getKeys() );
            }
        }

        return keys;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Set<Name> getKeys(String serviceRestrictions) {

        final Set<Name> keys = new HashSet<Name>();

        for(LayerProviderService service : SERVICES) {
            for(LayerProvider provider : service.getProviders()){
                keys.addAll( provider.getKeys(serviceRestrictions) );
            }
        }

        return keys;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean contains(Name key) {

        for(LayerProviderService service : SERVICES){
            for(LayerProvider provider : service.getProviders()){
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

        for(LayerProviderService service : SERVICES){
            for(LayerProvider provider : service.getProviders()){
                final LayerDetails layer = provider.get(key);
                if(layer != null) return layer;
            }
        }

        return null;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public LayerDetails get(Name key, String webService) {

        for(LayerProviderService service : SERVICES){
            for(LayerProvider provider : service.getProviders()){
                final LayerDetails layer = provider.get(key, webService);
                if(layer != null) return layer;
            }
        }

        return null;
    }

    @Override
    public ElevationModel getElevationModel(String name) {

        for(LayerProviderService service : SERVICES){
            for(LayerProvider provider : service.getProviders()){
                final ElevationModel model = provider.getElevationModel(name);
                if(model != null) return model;
            }
        }
        
        return null;
    }

    public Collection<LayerProviderService> getServices() {
        return Collections.unmodifiableCollection(SERVICES);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void reload() {
        loadServices();
    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public void dispose() {

        for(LayerProviderService service : SERVICES){
            for(LayerProvider provider : service.getProviders()){
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

        loadServices();

    }

    public static void loadServices() {
        SERVICES.clear();
        final ServiceLoader<LayerProviderService> loader = ServiceLoader.load(LayerProviderService.class);
        for(final LayerProviderService service : loader){
            final String name = service.getName();
            final String fileName = name + ".xml";
            /*
             * First check that there are config files in the WEB-INF/classes directory
             */
            File configFile = FileUtilities.getFileFromResource(fileName);
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
                File warFile = ConfigDirectory.getWarPackagedConfig("config.xml");
                if(warFile != null){
                    configFile = warFile;
                }
            }


            service.setConfiguration(configFile);

            SERVICES.add(service);
        }
    }


    /**
     * Returns the current instance of {@link LayerProviderProxy}. It will create a new one
     * if it does not already exist.
     */
    public static synchronized LayerProviderProxy getInstance(){
        return getInstance(true);
    }

    /**
     * Returns the current instance of {@link LayerProviderProxy}. If there is no current
     * instance, it will create and return a new one if the parameter {@code createIfNotExists}
     * is {@code true}, or return {@code null} and do nothing if it is {@code false}.
     *
     * @param createIfNotExists {@code True} if we want to create a new instance if not already
     *                          defined, {@code false} if we do not want.
     */
    public static synchronized LayerProviderProxy getInstance(final boolean createIfNotExists){
        if(INSTANCE == null && createIfNotExists){
            init(ConfigDirectory.getConfigDirectory().getPath() + File.separator);
            INSTANCE = new LayerProviderProxy();
        }
        
        return INSTANCE;
    }

}