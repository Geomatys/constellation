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
package org.constellation.process.provider.restart;

import java.util.Collection;
import org.constellation.process.AbstractCstlProcess;
import org.constellation.provider.LayerProvider;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.StyleProvider;
import org.constellation.provider.StyleProviderProxy;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.opengis.parameter.ParameterValueGroup;
import static org.geotoolkit.parameter.Parameters.*;

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

        final Collection<LayerProvider> layerProviders = LayerProviderProxy.getInstance().getProviders();
        for (LayerProvider p : layerProviders) {
            if (p.getId().equals(providerId)) {
                p.reload();
                reloaded = true;
                break;
            }
        }

        if (!reloaded) {
            final Collection<StyleProvider> styleProviders = StyleProviderProxy.getInstance().getProviders();
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
