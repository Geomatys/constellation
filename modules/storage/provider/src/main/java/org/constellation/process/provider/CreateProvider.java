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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import org.constellation.configuration.ConfigurationException;
import org.constellation.process.AbstractCstlProcess;
import static org.constellation.process.provider.CreateProviderDescriptor.PROVIDER_TYPE;
import static org.constellation.process.provider.CreateProviderDescriptor.SOURCE;
import org.constellation.provider.DataProvider;
import org.constellation.provider.DataProviderFactory;
import org.constellation.provider.DataProviders;
import org.constellation.provider.Provider;
import org.constellation.provider.ProviderFactory;
import org.constellation.provider.StyleProvider;
import org.constellation.provider.StyleProviderFactory;
import org.constellation.provider.StyleProviders;
import org.geotoolkit.feature.type.Name;
import static org.geotoolkit.parameter.Parameters.value;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.opengis.parameter.ParameterValueGroup;

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
