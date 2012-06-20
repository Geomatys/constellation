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
package org.constellation.process.provider.create;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.constellation.process.AbstractCstlProcess;
import org.geotoolkit.process.ProcessException;
import org.opengis.parameter.ParameterValueGroup;

import static org.constellation.process.provider.create.CreateProviderDescriptor.*;
import org.constellation.provider.*;
import static org.geotoolkit.parameter.Parameters.*;

/**
 * Create a new provider in constellation.
 * @author Quentin Boileau (Geomatys).
 */
public class CreateProvider extends AbstractCstlProcess{

    public CreateProvider( final ParameterValueGroup parameter) {
        super(INSTANCE, parameter);
    }

    
    
    @Override
    protected void execute() throws ProcessException {
        final String serviceName = value(SERVICE_NAME, inputParameters);
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
        
        final ProviderService service = services.get(serviceName);
        if (service != null) {

            //check no other provider with this id exist            
            final String id = (String) source.parameter("id").getValue();

            for (final Provider p : LayerProviderProxy.getInstance().getProviders()) {
                if (id.equals(p.getId())) {
                    throw new ProcessException("Provider ID is already used : " + id, this, null);
                }
            }
            for (final Provider p : StyleProviderProxy.getInstance().getProviders()) {
                if (id.equals(p.getId())) {
                    throw new ProcessException("Provider ID is already used : " + id, this, null);
                }
            }
           
            if (service instanceof LayerProviderService) {
                LayerProviderProxy.getInstance().createProvider((LayerProviderService) service, source);
            } else if (service instanceof StyleProviderService) {
                StyleProviderProxy.getInstance().createProvider((StyleProviderService) service, source);
            }
            
        } else {
            throw new ProcessException("Service name not found.", this, null);
        }
    }

}
