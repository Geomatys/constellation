/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;

import org.constellation.provider.configuration.ProviderConfig;
import org.constellation.provider.configuration.ProviderSource;

import org.xml.sax.SAXException;

/**
 *
 * @version $Id$
 *
 * @author Cédric Briançon (Geomatys)
 * @author Johann Sorel (Geomatys)
 */
public abstract class AbstractProviderService<K, V> implements ProviderService<K, V> {

    private static final Logger LOGGER = Logger.getLogger(AbstractProviderService.class.getName());

    private final String name;
    private ProviderConfig configuration = new ProviderConfig();

    protected AbstractProviderService(String name){
        this.name = name;
    }

    /**
     * Used by setConfiguration to dispose providers before the new ones
     * are loaded.
     */
    protected abstract void disposeProvider(Provider provider);

    /**
     * Used by setConfiguration to load a single source.
     */
    protected abstract void loadProvider(ProviderSource ps);

    /**
     * {@inheritDoc }
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public final void setConfiguration(File file) {
        //unload previous providers
        final Collection<Provider> providers = new ArrayList<Provider>(getProviders());
        for(Provider p : providers){
            disposeProvider(p);
        }

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

        setConfiguration(config);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public final void setConfiguration(ProviderConfig configuration) {
        if(configuration == null){
            throw new IllegalArgumentException("Configuration can not be null");
        }

        //unload previous providers
        final Collection<Provider> providers = new ArrayList<Provider>(getProviders());
        for(Provider p : providers){
            disposeProvider(p);
        }
        
        this.configuration = configuration;

        for (final ProviderSource ps : configuration.sources) {
            loadProvider(ps);
        }

    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ProviderConfig getConfiguration() {
        return configuration;
    }

}
