/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.constellation.ws.rs.WebService;
import org.geotools.map.ElevationModel;

/**
 * Main data provider for MapLayer objects. This class act as a proxy for 
 * different kind of data sources, postgrid, shapefile ...
 * 
 * @author Johann Sorel (Geomatys)
 */
public class LayerProviderProxy implements LayerProvider{

    /**
     * Default logger.
     */
    private static final Logger LOGGER = Logger.getLogger(LayerProviderProxy.class.getName());

    private static final Collection<LayerProviderService> SERVICES = new ArrayList<LayerProviderService>();

    private static String CONFIG_PATH = null;

    private static LayerProviderProxy INSTANCE = null;


    private LayerProviderProxy(){}
    
        
    /**
     * {@inheritDoc }
     */
    public Class<String> getKeyClass() {
        return String.class;
    }

    /**
     * {@inheritDoc }
     */
    public Class<LayerDetails> getValueClass() {
        return LayerDetails.class;
    }

    /**
     * {@inheritDoc }
     */
    public Set<String> getKeys() {

        final Set<String> keys = new HashSet<String>();

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
    public boolean contains(String key) {

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
    public LayerDetails get(String key) {

        for(LayerProviderService service : SERVICES){
            for(LayerProvider provider : service.getProviders()){
                LayerDetails layer = provider.get(key);
                if(layer != null) return layer;
            }
        }

        return null;
    }

    /**
     * {@inheritDoc }
     */
    public List<String> getFavoriteStyles(String layerName) {
        final List<String> styles = new ArrayList<String>();

        for(LayerProviderService service : SERVICES){
            for(LayerProvider provider : service.getProviders()){
                List<String> sts = provider.get(layerName).getFavoriteStyles();
                styles.addAll(sts);
            }
        }

        return styles;
    }

    @Override
    public ElevationModel getElevationModel(String name) {

        for(LayerProviderService service : SERVICES){
            for(LayerProvider provider : service.getProviders()){
                ElevationModel model = provider.getElevationModel(name);
            if(model != null) return model;
            }
        }
        
        return null;
    }

    /**
     * {@inheritDoc }
     */
    public void reload() {

        for(LayerProviderService service : SERVICES){
            for(LayerProvider provider : service.getProviders()){
                provider.reload();
            }
        }

    }
    
    /**
     * {@inheritDoc }
     */
    public void dispose() {

        for(LayerProviderService service : SERVICES){
            for(LayerProvider provider : service.getProviders()){
                provider.dispose();
            }
        }

        SERVICES.clear();

    }

    public synchronized static void init(final String configPath){
        if(configPath == null){
            throw new NullPointerException("Configuration path can not be null.");
        }

        if(CONFIG_PATH != null){
            throw new IllegalStateException("The layer provider proxy has already been initialize");
        }

        CONFIG_PATH = configPath;

        final ServiceLoader<LayerProviderService> loader = ServiceLoader.load(LayerProviderService.class);
        for(final LayerProviderService service : loader){
            final String name = service.getName();
            final String path = CONFIG_PATH + name + ".xml";
            final File configFile = new File(path);
            if(!configFile.exists()){
                try {
                    configFile.createNewFile();
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                    continue;
                }
            }
            service.init(configFile);

            SERVICES.add(service);
        }

    }

    public synchronized static LayerProviderProxy getInstance(){
        if(INSTANCE == null){
            init(WebService.getConfigDirectory().getPath() + File.separator);
            INSTANCE = new LayerProviderProxy();
        }
        
        return INSTANCE;
    }

}