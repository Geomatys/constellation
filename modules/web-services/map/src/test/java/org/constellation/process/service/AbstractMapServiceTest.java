/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
            ConfigurationEngine.storeConfiguration(serviceName, identifier, configuration, null);
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
            ConfigurationEngine.storeConfiguration(serviceName, identifier, context, null);
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
            context = (LayerContext) ConfigurationEngine.getConfiguration(serviceName, identifier);
        } catch (JAXBException | FileNotFoundException ex) {
            LOGGER.log(Level.SEVERE, "Error while getting configuration", ex);
        }
        return context;
    }

}
