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
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import org.constellation.configuration.ConfigDirectory;
import org.constellation.provider.configuration.ProviderConfig;
import org.constellation.provider.configuration.ProviderSource;

import org.geotoolkit.map.ElevationModel;

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

    private static final LayerProviderProxy INSTANCE = new LayerProviderProxy();
    //all services
    private static Collection<LayerProviderService> SERVICES = null;

    private LayerProviderProxy(){}
    
    /**
     * {@inheritDoc }
     */
    @Override
    public Set<Name> getKeys() {

        final Set<Name> keys = new HashSet<Name>();

        for(LayerProviderService service : getServices()){
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
    public Set<Name> getKeys(String sourceName) {

        final Set<Name> keys = new HashSet<Name>();

        for(LayerProviderService service : getServices()) {
            for(LayerProvider provider : service.getProviders()){
                ProviderSource ps = provider.getSource();
                if (sourceName.equals(ps.id)) {
                    keys.addAll(provider.getKeys(sourceName) );
                }
            }
        }

        return keys;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean contains(Name key) {

        for(LayerProviderService service : getServices()){
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

        for(LayerProviderService service : getServices()){
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
    public ElevationModel getElevationModel(Name name) {

        for(LayerProviderService service : getServices()){
            for(LayerProvider provider : service.getProviders()){
                final ElevationModel model = provider.getElevationModel(name);
                if(model != null) return model;
            }
        }
        
        return null;
    }

    public synchronized Collection<LayerProviderService> getServices() {
        if(SERVICES != null){
            //services are already loaded
            return SERVICES;
        }

        //configure each service
        final List<LayerProviderService> cache = new ArrayList<LayerProviderService>();
        final ServiceLoader<LayerProviderService> loader = ServiceLoader.load(LayerProviderService.class);
        for(final LayerProviderService service : loader){
            final String name     = service.getName();
            final String fileName = name + ".xml";
            final File configFile = ConfigDirectory.getProviderConfigFile(fileName);
            service.setConfiguration(configFile);
            cache.add(service);
        }

        SERVICES = Collections.unmodifiableCollection(cache);
        return SERVICES;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public synchronized void reload() {
        dispose();
        getServices(); //will load providers
    }
    
    /**
     * {@inheritDoc }
     */
    @Override
    public synchronized void dispose() {
        if(SERVICES == null){
            //services are not loaded
            return;
        }

        //services were loaded, dispose each of them
        for(final LayerProviderService service : SERVICES){
            for(final LayerProvider provider : service.getProviders()){
                provider.dispose();
            }
        }
        SERVICES = null;
    }

    /**
     * Returns the current instance of {@link LayerProviderProxy}.
     */
    public static LayerProviderProxy getInstance(){
        return INSTANCE;
    }

}
