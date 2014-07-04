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

import org.constellation.configuration.ConfigurationException;
import org.constellation.process.AbstractCstlProcess;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviders;
import org.constellation.provider.Provider;
import org.constellation.provider.StyleProvider;
import org.constellation.provider.StyleProviders;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.opengis.parameter.ParameterValueGroup;

import static org.constellation.process.provider.DeleteProviderDescriptor.DELETE_DATA;
import static org.constellation.process.provider.DeleteProviderDescriptor.PROVIDER_ID;
import static org.geotoolkit.parameter.Parameters.value;

/**
 * Remove a provider from constellation.
 *
 * @author Quentin Boileau (Geomatys).
 * @author Fabien Bernard (Geomatys).
 */
public final class DeleteProvider extends AbstractCstlProcess{

    public DeleteProvider(final ProcessDescriptor desc, final ParameterValueGroup parameter) {
        super(desc, parameter);
    }

    /**
     * @throws ProcessException if the provider can't be found
     */
    @Override
    protected void execute() throws ProcessException {
        final String providerID  = value(PROVIDER_ID, inputParameters); // required
        final Boolean deleteData = value(DELETE_DATA, inputParameters); // optional

        // Retrieve or not the provider instance.
        Provider provider = DataProviders.getInstance().getProvider(providerID);
        if (provider == null) {
            provider = StyleProviders.getInstance().getProvider(providerID);
        }
        if (provider == null) {
            throw new ProcessException("Unable to delete the provider with id \"" + providerID + "\". Not found.", this, null);
        }

        // Remove provider from its registry.
        if (provider instanceof DataProvider) {
            if (deleteData != null && deleteData) {
                provider.removeAll();
            }
            try {
                DataProviders.getInstance().removeProvider((DataProvider) provider);
            } catch (ConfigurationException ex) {
                throw new ProcessException("Failed to delete provider : " + providerID+"  "+ex.getMessage(), this, ex);
            }
        } else {
            try {
                StyleProviders.getInstance().removeProvider((StyleProvider) provider);
            } catch (ConfigurationException ex) {
                throw new ProcessException("Failed to delete provider : " + providerID+"  "+ex.getMessage(), this, ex);
            }
        }
    }

}
