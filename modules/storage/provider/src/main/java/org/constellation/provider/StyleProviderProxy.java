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
import org.geotoolkit.factory.FactoryFinder;
import org.geotoolkit.factory.Hints;
import org.geotoolkit.style.MutableStyle;
import org.geotoolkit.style.MutableStyleFactory;
import org.geotoolkit.util.FileUtilities;


/**
 * Main Data provider for styles objects. This class act as a proxy for
 * several SLD folder providers.
 *
 * @version $Id$
 * @author Johann Sorel (Geomatys)
 */
public final class StyleProviderProxy extends AbstractStyleProvider{

    public static final MutableStyleFactory STYLE_FACTORY = (MutableStyleFactory)
            FactoryFinder.getStyleFactory(new Hints(Hints.STYLE_FACTORY, MutableStyleFactory.class));

    public static final RandomStyleFactory STYLE_RANDOM_FACTORY = new RandomStyleFactory();

    private static final Collection<StyleProviderService> SERVICES = new ArrayList<StyleProviderService>();

    private static String configPath = null;

    private static StyleProviderProxy instance = null;

    private StyleProviderProxy(){}


    /**
     * {@inheritDoc }
     */
    @Override
    public Class<String> getKeyClass() {
        return String.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Class<MutableStyle> getValueClass() {
        return MutableStyle.class;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Set<String> getKeys() {

        final Set<String> keys = new HashSet<String>();

        for(StyleProviderService service : SERVICES){
            for(StyleProvider provider : service.getProviders()){
                keys.addAll( provider.getKeys() );
            }
        }

        return keys;
    }

    @Override
    public Set<String> getKeys(String service) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * {@inheritDoc }
     */
    @Override
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
    @Override
    public MutableStyle get(String key) {

        for(StyleProviderService service : SERVICES){
            for(StyleProvider provider : service.getProviders()){
                final MutableStyle style = provider.get(key);
                if(style != null) return style;
            }
        }

        return null;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public MutableStyle get(String key, String serv) {
        for(StyleProviderService service : SERVICES){
            for(StyleProvider provider : service.getProviders()){
                final MutableStyle style = provider.get(key, serv);
                if(style != null) return style;
            }
        }

        return null;
    }

    public Collection<StyleProviderService> getServices() {
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

        for(StyleProviderService service : SERVICES){
            for(StyleProvider provider : service.getProviders()){
                provider.dispose();
            }
        }

        SERVICES.clear();

    }

    public static synchronized void init(final String confPath){
        if(confPath == null){
            throw new NullPointerException("Configuration path can not be null.");
        }

        if(configPath != null){
            throw new IllegalStateException("The style provider proxy has already been initialize");
        }

        configPath = confPath;
        loadServices();
    }

    public static void loadServices() {
        SERVICES.clear();
        final ServiceLoader<StyleProviderService> loader = ServiceLoader.load(StyleProviderService.class);
        for(final StyleProviderService service : loader){
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
                final String path = configPath + fileName;
                configFile = new File(path);
            }
            service.setConfiguration(configFile);

            SERVICES.add(service);
        }
    }


    /**
     * Returns the current instance of {@link StyleProviderProxy}. It will create a new one
     * if it does not already exist.
     */
    public static synchronized StyleProviderProxy getInstance(){
        return getInstance(true);
    }

    /**
     * Returns the current instance of {@link StyleProviderProxy}. If there is no current
     * instance, it will create and return a new one if the parameter {@code createIfNotExists}
     * is {@code true}, or return {@code null} and do nothing if it is {@code false}.
     *
     * @param createIfNotExists {@code True} if we want to create a new instance if not already
     *                          defined, {@code false} if we do not want.
     */
    public static synchronized StyleProviderProxy getInstance(final boolean createIfNotExists){
        if(instance == null && createIfNotExists){
            init(ConfigDirectory.getConfigDirectory().getPath() + File.separator);
            instance = new StyleProviderProxy();
        }

        return instance;
    }

}
