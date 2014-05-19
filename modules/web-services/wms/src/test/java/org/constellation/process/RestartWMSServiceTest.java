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
package org.constellation.process;

import java.io.IOException;
import java.util.logging.Level;
import javax.xml.bind.JAXBException;
import org.constellation.admin.ConfigurationEngine;
import org.constellation.configuration.LayerContext;
import org.constellation.map.ws.DefaultWMSWorker;
import org.constellation.process.service.RestartServiceTest;

/**
 *
 * @author Quentin Boileau (Geomatys).
 */
public class RestartWMSServiceTest extends RestartServiceTest {

    public RestartWMSServiceTest() {
        super("WMS", DefaultWMSWorker.class);
    }

    /** {@inheritDoc} */
    @Override
    protected void createInstance(final String identifier) {
        try {
            final LayerContext configuration = new LayerContext();
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
}
