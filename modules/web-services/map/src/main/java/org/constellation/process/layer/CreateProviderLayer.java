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
package org.constellation.process.layer;

import java.util.Collection;
import org.constellation.process.AbstractCstlProcess;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.opengis.parameter.ParameterValueGroup;
import static org.geotoolkit.parameter.Parameters.*;
import static org.constellation.process.layer.CreateProviderLayerDescriptor.*;
import org.constellation.provider.LayerProvider;
import org.constellation.provider.LayerProviderProxy;

/**
 * Add a layer to an existing provider.
 *
 * @author Quentin Boileau (Geomatys).
 */
public class CreateProviderLayer extends AbstractCstlProcess {

    public CreateProviderLayer(final ProcessDescriptor desc, final ParameterValueGroup parameter) {
        super(desc, parameter);
    }

    /**
     * Add a layer to an existing provider.
     * @throws ProcessException if :
     * - Provider identifier is null/empty or not found in LayerProvider list.
     * - layer is null.
     */
    @Override
    protected void execute() throws ProcessException {

        String providerId = value(PROVIDER_ID, inputParameters);
        final ParameterValueGroup layer = value(LAYER, inputParameters);

        if (providerId == null || "".equals(providerId.trim())) {
            throw new ProcessException("Provider identifier can't be null or empty.", this, null);
        }

        if (layer != null) {
            final Collection<LayerProvider> providers = LayerProviderProxy.getInstance().getProviders();

            boolean found = false;
            for (final LayerProvider p : providers) {
                if (p.getId().equals(providerId)) {
                    p.getSource().values().add(layer);
                    p.updateSource(p.getSource());
                    found = true;
                    break;
                }
            }

            if (!found) {
                throw new ProcessException("Provider with id "+providerId+" not found.", this, null);
            }

        } else {
            throw new ProcessException("Layer can't be null.", this, null);
        }
    }
}
