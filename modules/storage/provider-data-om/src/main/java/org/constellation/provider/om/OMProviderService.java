/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009-2011, Geomatys
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
package org.constellation.provider.om;

import java.util.logging.Level;

import org.constellation.provider.AbstractProviderService;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.LayerProvider;
import org.constellation.provider.LayerProviderService;
import org.constellation.provider.configuration.ProviderParameters;

import org.opengis.feature.type.Name;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

import static org.geotoolkit.data.om.OMDataStoreFactory.*;
import static org.geotoolkit.parameter.Parameters.*;
import static org.constellation.provider.configuration.ProviderParameters.*;

/**
 *
 * @version $Id: 
 *
 * @author Johann Sorel (Geoamtys)
 * @author Guilhem Legal (Geomatys)
 */
public class OMProviderService extends AbstractProviderService
        <Name,LayerDetails,LayerProvider> implements LayerProviderService {

    private static final String ERROR_MSG = "[PROVIDER]> Invalid observation provider config";
    private static final ParameterDescriptorGroup SERVICE_CONFIG_DESCRIPTOR =
            ProviderParameters.createDescriptor(PARAMETERS_DESCRIPTOR);

    public OMProviderService(){
        super("observation");
    }

    @Override
    public ParameterDescriptorGroup getServiceDescriptor() {
        return SERVICE_CONFIG_DESCRIPTOR;
    }

    @Override
    public ParameterDescriptorGroup getSourceDescriptor() {
        return PARAMETERS_DESCRIPTOR;
    }
    
    @Override
    public LayerProvider createProvider(ParameterValueGroup ps) {
        try {
            final OMProvider provider = new OMProvider(this,ps);
            ps = getOrCreate(PARAMETERS_DESCRIPTOR, ps);
            String msg = "[PROVIDER]> O&M provider created : ";
            final String sgbdType = value(SGBDTYPE, ps);
            if (sgbdType != null && sgbdType.equals("derby")) {
                msg = msg + "java DB: > "
                          + value(DERBYURL, ps);
            } else {
                msg = msg + value(HOST, ps) + " > "
                          + value(DATABASE, ps);
            }
            getLogger().info(msg);
            return provider;
        } catch (Exception ex) {
            // we should not catch exception, but here it's better to start all source we can
            // rather than letting a potential exception block the provider proxy
            getLogger().log(Level.SEVERE, ERROR_MSG, ex);
        }
        return null;
    }

}
