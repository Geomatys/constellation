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

import java.util.Collection;
import org.constellation.process.AbstractCstlProcess;
import org.geotoolkit.process.ProcessException;
import org.opengis.parameter.ParameterValueGroup;

import static org.constellation.process.provider.GetConfigProviderDescriptor.*;
import org.constellation.provider.*;
import static org.geotoolkit.parameter.Parameters.*;
import org.geotoolkit.process.ProcessDescriptor;

/**
 * Remove a provider from constellation. Throw an ProcessException if Provider is not found.
 *
 * @author Quentin Boileau (Geomatys).
 */
public final class GetConfigProvider extends AbstractCstlProcess {

    public GetConfigProvider( final ProcessDescriptor desc, final ParameterValueGroup parameter) {
        super(desc, parameter);
    }


    @Override
    protected void execute() throws ProcessException {
        final String providerID = value(PROVIDER_ID, inputParameters);

        ParameterValueGroup config = null;

        Collection<? extends Provider> providers = DataProviders.getInstance().getProviders();
        for (final Provider p : providers) {
            if (p.getId().equals(providerID)) {
                config = p.getSource();
                break;
            }
        }

        if (config == null) {
            providers = StyleProviders.getInstance().getProviders();
            for (final Provider p : providers) {
                if (p.getId().equals(providerID)) {
                    config = p.getSource();
                    break;
                }
            }
        }

        if (config == null) {
            throw new ProcessException("Provider not found.", this, null);
        }

        getOrCreate(CONFIG, outputParameters).setValue(config);
    }

}
