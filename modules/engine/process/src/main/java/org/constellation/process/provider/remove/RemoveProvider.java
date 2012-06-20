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
package org.constellation.process.provider.remove;

import java.util.Collection;
import org.constellation.process.AbstractCstlProcess;
import org.geotoolkit.process.ProcessException;
import org.opengis.parameter.ParameterValueGroup;

import static org.constellation.process.provider.remove.RemoveProviderDescriptor.*;
import org.constellation.provider.*;
import static org.geotoolkit.parameter.Parameters.*;

/**
 * Remove a provider from constellation.
 * @author Quentin Boileau (Geomatys).
 */
public class RemoveProvider extends AbstractCstlProcess{

    public RemoveProvider( final ParameterValueGroup parameter) {
        super(INSTANCE, parameter);
    }

    
    @Override
    protected void execute() throws ProcessException {
        final String providerID = value(PROVIDER_ID, inputParameters);
        
        Provider provider = null;
        final Collection<LayerProvider> layerProviders = LayerProviderProxy.getInstance().getProviders();
        for (final LayerProvider p : layerProviders) {
            if (p.getId().equals(providerID)) {
                provider = p;
            }
        }
       
        final Collection<StyleProvider> styleProviders = StyleProviderProxy.getInstance().getProviders();
        for (final StyleProvider p : styleProviders) {
            if (p.getId().equals(providerID)) {
                provider = p;
            }
        }
        
        if (provider != null) {
            if (provider instanceof LayerProvider) {
                LayerProviderProxy.getInstance().removeProvider((LayerProvider)provider);
            } else if (provider instanceof  StyleProvider) {
                 StyleProviderProxy.getInstance().removeProvider((StyleProvider)provider);
            } else {
                throw new ProcessException("Invalid provider.", this, null);
            }
        } else {
            throw new ProcessException("Provider to remove not found.", this, null);
        }
        
    }

}
