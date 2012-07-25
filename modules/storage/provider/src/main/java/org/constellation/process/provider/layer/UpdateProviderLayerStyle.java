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
package org.constellation.process.provider.layer;

import java.util.Collection;
import org.constellation.process.AbstractCstlProcess;
import org.constellation.provider.LayerProvider;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.configuration.ProviderParameters;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;

import static org.geotoolkit.parameter.Parameters.*;
import static org.constellation.process.provider.layer.UpdateProviderLayerStyleDescriptor.*;

/**
 * Update a layer from an existing provider.
 * @author Quentin Boileau (Geomatys).
 */
public class UpdateProviderLayerStyle extends AbstractCstlProcess {

     public UpdateProviderLayerStyle(final ProcessDescriptor desc, final ParameterValueGroup parameter) {
        super(desc, parameter);
    }

    /**
     * Update a layer from an existing provider.
     *
     * @throws ProcessException if :
     * - Provider identifier is null/empty or not found in LayerProvider list.
     * - layer name is null/empty or not found in LayerProvider list.
     */
    @Override
    protected void execute() throws ProcessException {

        final String providerId = value(PROVIDER_ID, inputParameters);
        final String layerName = value(LAYER_NAME, inputParameters);
        final ParameterValueGroup updateLayer = value(UPDATE_LAYER, inputParameters);

        if (providerId == null || "".equals(providerId.trim())) {
            throw new ProcessException("Provider identifier can't be null or empty.", this, null);
        }

        if (layerName == null || "".equals(layerName.trim())) {
            throw new ProcessException("Layer name can't be null or empty.", this, null);
        }

        if (updateLayer == null) {
            throw new ProcessException("Layer can't be null.", this, null);
        }

        final Collection<LayerProvider> providers = LayerProviderProxy.getInstance().getProviders();

        boolean providerFound = false;
        boolean updated = false;
        for (final LayerProvider p : providers) {
            if (p.getId().equals(providerId)) {
                for (final GeneralParameterValue param : p.getSource().values()) {
                    if (param instanceof ParameterValueGroup) {
                        final ParameterValueGroup pvg = (ParameterValueGroup)param;
                        if (param.getDescriptor().equals(ProviderParameters.LAYER_DESCRIPTOR)) {
                            final ParameterValue value = pvg.parameter("name");
                            if (value.stringValue().equals(layerName)) {
                                p.getSource().values().remove(pvg);
                                p.getSource().values().add(updateLayer);
                                updated = true;
                                break;
                            }
                        }
                    }
                }
                p.updateSource(p.getSource());
                providerFound = true;
                break;
            }
        }

        if (!providerFound) {
            throw new ProcessException("Provider with id "+providerId+" can't be found.", this, null);
        } else {
            if (!updated) {
                throw new ProcessException("Layer with name "+layerName+" can't be found in provider "+providerId+" layers.", this, null);
            }
        }

    }

}
