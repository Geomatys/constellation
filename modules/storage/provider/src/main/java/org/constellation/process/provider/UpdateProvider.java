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
import org.constellation.provider.DataProviders;
import org.constellation.provider.Provider;
import org.constellation.provider.StyleProviders;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.opengis.parameter.ParameterValueGroup;

import java.util.Collection;

import static org.constellation.process.provider.UpdateProviderDescriptor.PROVIDER_ID;
import static org.constellation.process.provider.UpdateProviderDescriptor.SOURCE;
import static org.geotoolkit.parameter.Parameters.value;

/**
 * Update a provider from constellation.
 * @author Quentin Boileau (Geomatys).
 */
public class UpdateProvider extends AbstractCstlProcess{

    public UpdateProvider(final ProcessDescriptor desc, final ParameterValueGroup parameter) {
        super(desc, parameter);
    }

       @Override
    protected void execute() throws ProcessException {
        final String providerID = value(PROVIDER_ID, inputParameters);
        final ParameterValueGroup source = (ParameterValueGroup) value(SOURCE, inputParameters);

        boolean updated = false;

        Collection<? extends Provider> providers = DataProviders.getInstance().getProviders();
        for (final Provider p : providers) {
            if (providerID.equals(p.getId())) {
                p.updateSource(source);
                updated = true;
                break;
            }
        }
        if (!updated) {
            providers = StyleProviders.getInstance().getProviders();
            for (final Provider p : providers) {
                if (providerID.equals(p.getId())) {
                    p.updateSource(source);
                    updated = true;
                    break;
                }
            }
        }

        if (!updated) {
            throw new ProcessException("Provider ID not found.", this, null);
        }
    }

}
