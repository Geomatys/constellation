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
import java.util.ServiceLoader;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.constellation.ws.rs.WebService;


/**
 * Main Data provider for styles objects. This class act as a proxy for
 * several SLD folder providers.
 *
 * @version $Id$
 * @author Johann Sorel (Geomatys)
 */
public class StyleProviderProxy implements Provider<String,Object>{

    /**
     * Default logger.
     */
    private static final Logger LOGGER = Logger.getLogger(LayerProviderProxy.class.getName());

    private static final Collection<StyleProviderService> SERVICES = new ArrayList<StyleProviderService>();

    private static String CONFIG_PATH = null;

    private static StyleProviderProxy INSTANCE = null;

    private StyleProviderProxy(){}


    /**
     * {@inheritDoc }
     */
    public Class<String> getKeyClass() {
        return String.class;
    }

    /**
     * {@inheritDoc }
     */
    public Class<Object> getValueClass() {
        return Object.class;
    }

    /**
     * {@inheritDoc }
     */
    public Set<String> getKeys() {

        final Set<String> keys = new HashSet<String>();

        for(StyleProviderService service : SERVICES){
            for(StyleProvider provider : service.getProviders()){
                keys.addAll( provider.getKeys() );
            }
        }

        return keys;
    }

    /**
     * {@inheritDoc }
     */
    public boolean contains(String key) {

        for(StyleProviderService service : SERVICES){
            for(StyleProvider provider : service.getProviders()){
                if(provider.contains(key)) return true;
            }
        }

        return false;
    }

    /**
     * {@inheritDoc }
     */
    public Object get(String key) {

        for(StyleProviderService service : SERVICES){
            for(StyleProvider provider : service.getProviders()){
                Object style = provider.get(key);
                if(style != null) return style;
            }
        }

        return null;
    }

    /**
     * {@inheritDoc }
     */
    public void reload() {

        for(StyleProviderService service : SERVICES){
            for(StyleProvider provider : service.getProviders()){
                provider.reload();
            }
        }

    }

    /**
     * {@inheritDoc }
     */
    public void dispose() {

        for(StyleProviderService service : SERVICES){
            for(StyleProvider provider : service.getProviders()){
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
            throw new IllegalStateException("The style provider proxy has already been initialize");
        }

        CONFIG_PATH = configPath;

        final ServiceLoader<StyleProviderService> loader = ServiceLoader.load(StyleProviderService.class);
        for(final StyleProviderService service : loader){
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

    public synchronized static StyleProviderProxy getInstance(){
        if(INSTANCE == null){
            init(WebService.getSicadeDirectory().getPath() + File.separator);
            INSTANCE = new StyleProviderProxy();
        }
        return INSTANCE;
    }
    
}
