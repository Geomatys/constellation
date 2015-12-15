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

import java.util.Iterator;
import java.util.logging.Level;
import org.constellation.configuration.ConfigurationException;
import org.constellation.configuration.Instance;
import org.constellation.configuration.ProcessContext;
import org.constellation.configuration.ProcessFactory;
import org.constellation.dto.SimpleValue;
import org.constellation.ogc.configuration.OGCConfigurer;
import org.constellation.wps.utils.WPSUtils;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessFinder;
import org.geotoolkit.process.ProcessingRegistry;

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
    }
    
    @Override
    public Instance getInstance(final String spec, final String identifier) throws ConfigurationException {
        final Instance instance = super.getInstance(spec, identifier);
        try {
            instance.setLayersNumber(getProcessCount(identifier));
        } catch (ConfigurationException ex) {
            LOGGER.log(Level.WARNING, "Error while getting process count on WPS instance:" + identifier, ex);
        }
        return instance;
    }
    
    public int getProcessCount(String id) throws ConfigurationException {
        final ProcessContext context = (ProcessContext) serviceBusiness.getConfiguration("WPS", id);
        int count = 0;
        if (Boolean.TRUE.equals(context.getProcesses().getLoadAll()) ) {
            for (Iterator<ProcessingRegistry> it = ProcessFinder.getProcessFactories(); it.hasNext();) {
                final ProcessingRegistry processingRegistry = it.next();
                for (ProcessDescriptor descriptor : processingRegistry.getDescriptors()) {
                    if (WPSUtils.isSupportedProcess(descriptor)) {
                        count++;
                    }
                }
            }
        } else {
            for (ProcessFactory pFacto : context.getProcessFactories()) {
                final ProcessingRegistry processingRegistry = ProcessFinder.getProcessFactory(pFacto.getAutorityCode());
                if (pFacto.getLoadAll()) {
                    count = count + processingRegistry.getDescriptors().size();
                } else {
                    count = count + pFacto.getInclude().getProcess().size();
                }
            }
        }
        return count;
    }
    
}
