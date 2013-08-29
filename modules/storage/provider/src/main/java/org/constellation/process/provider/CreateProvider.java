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

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.constellation.process.AbstractCstlProcess;
import org.geotoolkit.process.ProcessException;
import org.opengis.parameter.ParameterValueGroup;

import static org.constellation.process.provider.CreateProviderDescriptor.*;
import org.constellation.provider.*;
import static org.geotoolkit.parameter.Parameters.*;
import org.geotoolkit.process.ProcessDescriptor;

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
        final ParameterValueGroup source = (ParameterValueGroup) value(SOURCE, inputParameters);

        //initialize list of avaible Povider services
        final Map<String, ProviderService> services = new HashMap<String, ProviderService>();
        final Collection<LayerProviderService> availableLayerServices = LayerProviderProxy.getInstance().getServices();
        for (LayerProviderService service: availableLayerServices) {
            services.put(service.getName(), service);
        }
        final Collection<StyleProviderService> availableStyleServices = StyleProviderProxy.getInstance().getServices();
        for (StyleProviderService service: availableStyleServices) {
            services.put(service.getName(), service);
        }

        final ProviderService service = services.get(providerType);
        if (service != null) {

            //check no other provider with this id exist
            final String id = (String) source.parameter("id").getValue();

            //LayerProvider case
            if (service instanceof LayerProviderService) {

                final Collection<LayerProvider> layerProviders = LayerProviderProxy.getInstance().getProviders();
                for (final LayerProvider lp : layerProviders) {
                    if (id.equals(lp.getId())) {
                        throw new ProcessException("Provider ID is already used : " + id, this, null);
                    }
                }

                final Date date = new Date();
                final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy X");
                source.parameter("date").setValue(dateFormat.format(date));
                LayerProviderProxy.getInstance().createProvider((LayerProviderService) service, source);
            }

            //StyleProvider case
            if (service instanceof StyleProviderService) {

                final Collection<StyleProvider> styleProviders = StyleProviderProxy.getInstance().getProviders();
                for (final Provider sp : styleProviders) {
                    if (id.equals(sp.getId())) {
                        throw new ProcessException("Provider ID is already used : " + id, this, null);
                    }
                }
                StyleProviderProxy.getInstance().createProvider((StyleProviderService) service, source);
            }

        } else {
            throw new ProcessException("Provider type not found:" + providerType, this, null);
        }
    }

}
