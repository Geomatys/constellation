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
package org.constellation.provider.postgis;

import java.util.logging.Level;

import org.constellation.provider.AbstractProviderService;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.LayerProvider;
import org.constellation.provider.LayerProviderService;
import org.constellation.provider.configuration.ProviderParameters;

import org.opengis.feature.type.Name;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

import static org.geotoolkit.data.postgis.PostgisNGDataStoreFactory.*;
import static org.geotoolkit.parameter.Parameters.*;
import static org.constellation.provider.configuration.ProviderParameters.*;

/**
 *
 * @version $Id$
 *
 * @author Johann Sorel (Geoamtys)
 */
public class PostGisProviderService extends AbstractProviderService<Name,LayerDetails,LayerProvider> implements LayerProviderService {

    private static final String ERROR_MSG = "[PROVIDER]> Invalid postgis provider config";
    private static final ParameterDescriptorGroup SERVICE_CONFIG_DESCRIPTOR =
            ProviderParameters.createDescriptor(PARAMETERS_DESCRIPTOR);

    public PostGisProviderService(){
        super("postgis");
    }

    @Override
    public ParameterDescriptorGroup getDescriptor() {
        return SERVICE_CONFIG_DESCRIPTOR;
    }

    @Override
    public LayerProvider createProvider(ParameterValueGroup ps) {
        try {
            final PostGisProvider provider = new PostGisProvider(this,ps);
            ps = getOrCreate(PARAMETERS_DESCRIPTOR, ps);
            getLogger().log(Level.INFO, "[PROVIDER]> postgis provider created : {0} > {1}",
                    new Object[]{value(HOST, ps),value(DATABASE, ps)});
            return provider;
        } catch (Exception ex) {
            // we should not catch exception, but here it's better to start all source we can
            // rather than letting a potential exception block the provider proxy
            getLogger().log(Level.SEVERE, ERROR_MSG, ex);
        }
        return null;
    }

}
