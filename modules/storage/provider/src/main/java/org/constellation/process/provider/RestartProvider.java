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
package org.constellation.process.provider;

import org.constellation.process.AbstractCstlProcess;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviders;
import org.constellation.provider.StyleProvider;
import org.constellation.provider.StyleProviders;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.opengis.parameter.ParameterValueGroup;

import java.util.Collection;

import static org.geotoolkit.parameter.Parameters.value;

/**
 *
 * @author Quentin Boileau (Geomatys)
 */
public class RestartProvider extends AbstractCstlProcess {

    public RestartProvider(final ProcessDescriptor desc, final ParameterValueGroup parameter) {
        super(desc, parameter);
    }

    @Override
    protected void execute() throws ProcessException {

        final String providerId = value(RestartProviderDescriptor.PROVIDER_ID, inputParameters);

        if (providerId == null || providerId.trim().isEmpty()) {
            throw new ProcessException("Provider ID can't be null or empty.", this, null);
        }

        boolean reloaded = false;

        final Collection<DataProvider> layerProviders = DataProviders.getInstance().getProviders();
        for (DataProvider p : layerProviders) {
            if (p.getId().equals(providerId)) {
                p.reload();
                reloaded = true;
                break;
            }
        }

        if (!reloaded) {
            final Collection<StyleProvider> styleProviders = StyleProviders.getInstance().getProviders();
            for (StyleProvider p : styleProviders) {
                if (p.getId().equals(providerId)) {
                    p.reload();
                    reloaded = true;
                    break;
                }
            }
        }

        if (!reloaded) {
            throw new ProcessException("Provider ID not found for ID : "+providerId, this, null);
        }
    }
}
