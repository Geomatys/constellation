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

import org.constellation.process.AbstractCstlProcess;
import org.constellation.provider.*;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.opengis.parameter.ParameterValueGroup;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.constellation.configuration.ConfigurationException;

import static org.constellation.process.provider.CreateProviderDescriptor.PROVIDER_TYPE;
import static org.constellation.process.provider.CreateProviderDescriptor.SOURCE;
import static org.geotoolkit.parameter.Parameters.value;

/**
 * Create a new provider in constellation.
 * @author Quentin Boileau (Geomatys).
 */
public final class CreateProvider extends AbstractCstlProcess {

    public CreateProvider(final ProcessDescriptor desc, final ParameterValueGroup parameter) {
        super(desc, parameter);
    }

    @Override
    protected void execute() throws ProcessException {
        final String providerType = value(PROVIDER_TYPE, inputParameters);
        final ParameterValueGroup source = value(SOURCE, inputParameters);

        //initialize list of avaible Povider services
        final Map<String, ProviderFactory> services = new HashMap<>();
        final Collection<DataProviderFactory> availableLayerServices = DataProviders.getInstance().getFactories();
        for (DataProviderFactory service: availableLayerServices) {
            services.put(service.getName(), service);
        }
        final Collection<StyleProviderFactory> availableStyleServices = StyleProviders.getInstance().getFactories();
        for (StyleProviderFactory service: availableStyleServices) {
            services.put(service.getName(), service);
        }

        final ProviderFactory service = services.get(providerType);
        if (service != null) {

            //check no other provider with this id exist
            final String id = (String) source.parameter("id").getValue();

            //LayerProvider case
            if (service instanceof DataProviderFactory) {

                final Collection<DataProvider> layerProviders = DataProviders.getInstance().getProviders();
                for (final DataProvider lp : layerProviders) {
                    if (id.equals(lp.getId())) {
                        throw new ProcessException("Provider ID is already used : " + id, this, null);
                    }
                }
                source.parameter("date").setValue(new Date());
                try {
                    DataProviders.getInstance().createProvider(id, (DataProviderFactory) service, source);
                } catch (ConfigurationException ex) {
                    throw new ProcessException("Failed to create provider : " + id+"  "+ex.getMessage(), this, ex);
                }
            }

            //StyleProvider case
            if (service instanceof StyleProviderFactory) {

                final Collection<StyleProvider> styleProviders = StyleProviders.getInstance().getProviders();
                for (final Provider sp : styleProviders) {
                    if (id.equals(sp.getId())) {
                        throw new ProcessException("Provider ID is already used : " + id, this, null);
                    }
                }
                try {
                    StyleProviders.getInstance().createProvider(id, (StyleProviderFactory) service, source);
                } catch (ConfigurationException ex) {
                    throw new ProcessException("Failed to create provider : " + id+"  "+ex.getMessage(), this, ex);
                }
            }
        } else {
            throw new ProcessException("Provider type not found:" + providerType, this, null);
        }
    }

}
