/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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
package org.constellation.process.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import javax.xml.bind.JAXBException;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.configuration.LayerContext;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
public abstract class AbstractMapServiceTest extends ServiceProcessTest {


    public AbstractMapServiceTest (final String str, final String serviceName, final Class workerClass) {
        super(str, serviceName, workerClass);
    }

    /** {@inheritDoc} */
    @Override
    protected void createInstance(String identifier) {
        createCustomInstance(identifier, null);
    }

    protected void createInstance(final String identifier, LayerContext context) {
        final LayerContext configuration = context != null ? context : new LayerContext();
        try {
            ConfigurationEngine.createConfiguration(serviceName, identifier, "layerContext.xml", configuration, null);
        } catch (JAXBException | IOException ex) {
            LOGGER.log(Level.SEVERE, "Error while creating instance", ex);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected boolean checkInstanceExist(final String identifier) {
        return ConfigurationEngine.getServiceConfigurationIds(serviceName).contains(identifier);
    }

    /**
     * Create a custom instance.
     * 
     * @param identifier
     * @param context
     */
    protected void createCustomInstance(final String identifier, LayerContext context) {
        try {
            ConfigurationEngine.createConfiguration(serviceName, identifier, "layerContext.xml", context, null);
        }  catch (JAXBException | IOException ex) {
            LOGGER.log(Level.SEVERE, "Error while creating custom instance", ex);
        }
    }

    /**
     * Return the configuration of a service instance.
     * @param identifier
     * @return
     */
    protected  LayerContext getConfig(final String identifier) {
        LayerContext context = null;
        try {
            context = (LayerContext) ConfigurationEngine.getConfiguration(serviceName, identifier, "layerContext.xml");
        } catch (JAXBException | FileNotFoundException ex) {
            LOGGER.log(Level.SEVERE, "Error while getting configuration", ex);
        }
        return context;
    }

}
