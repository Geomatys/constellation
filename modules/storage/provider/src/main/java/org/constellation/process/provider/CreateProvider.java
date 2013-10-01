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

import org.constellation.admin.AdminDatabase;
import org.constellation.admin.AdminSession;
import org.constellation.configuration.ProviderRecord.ProviderType;
import org.constellation.process.AbstractCstlProcess;
import org.constellation.provider.*;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.opengis.parameter.ParameterValueGroup;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import static org.constellation.process.provider.CreateProviderDescriptor.OWNER;
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
        final String owner = value(OWNER, inputParameters);
        final ParameterValueGroup source = value(SOURCE, inputParameters);

        //initialize list of avaible Povider services
        final Map<String, ProviderService> services = new HashMap<>();
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
                source.parameter("date").setValue(new Date());
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

            // Register provider into administration database.
            AdminSession session = null;
            try {
                session = AdminDatabase.createSession();
                if (service instanceof LayerProviderService) {
                    session.writeProvider(id, ProviderType.LAYER, providerType, owner);
                } else {
                    session.writeProvider(id, ProviderType.STYLE, providerType, owner);
                }
            } catch (SQLException ex) {
                LOGGER.log(Level.WARNING, "An error occurred while updating administration database after creating the provider with id \"" + id + "\".", ex);
            } finally {
                if (session != null) session.close();
            }

        } else {
            throw new ProcessException("Provider type not found:" + providerType, this, null);
        }
    }

}
