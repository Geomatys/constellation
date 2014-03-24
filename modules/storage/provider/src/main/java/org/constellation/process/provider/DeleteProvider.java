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

import static org.constellation.process.provider.DeleteProviderDescriptor.*;
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
