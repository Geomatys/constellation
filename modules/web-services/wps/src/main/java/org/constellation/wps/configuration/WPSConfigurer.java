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

package org.constellation.wps.configuration;

import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.ProcessContext;
import org.constellation.configuration.Processes;
import org.constellation.dto.Service;
import org.constellation.ogc.configuration.OGCConfigurer;

/**
 * {@link org.constellation.configuration.ServiceConfigurer} implementation for WPS service.
 *
 * TODO: implement specific configuration methods
 *
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public class WPSConfigurer extends OGCConfigurer {

    /**
     * Create a new {@link WPSConfigurer} instance.
     */
    protected WPSConfigurer() {
        super(ProcessContext.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createInstance(final String serviceType, final String identifier, final Service metadata, Object configuration) throws ConfigurationException {
        if (configuration == null) {
            configuration = new ProcessContext(new Processes(true));
        }
        super.createInstance(serviceType, identifier, metadata, configuration);
    }
}
