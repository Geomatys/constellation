/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009-2010, Geomatys
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
package org.constellation.provider.sml;

import java.util.logging.Level;

import org.constellation.provider.AbstractProviderService;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.LayerProvider;
import org.constellation.provider.LayerProviderService;
import org.constellation.provider.configuration.ProviderSource;

import org.opengis.feature.type.Name;
import static org.constellation.provider.sml.SMLProvider.*;
/**
 *
 * @version $Id: 
 *
 * @author Johann Sorel (Geoamtys)
 * @author Guilhem Legal (Geomatys)
 */
public class SMLProviderService extends AbstractProviderService<Name,LayerDetails,LayerProvider> implements LayerProviderService {

    private static final String ERROR_MSG = "[PROVIDER]> Invalid sensorML provider config";

    public SMLProviderService(){
        super("sensorML");
    }

    @Override
    public LayerProvider createProvider(ProviderSource ps) {
        try {
            final SMLProvider provider = new SMLProvider(ps);
            String msg = "[PROVIDER]> sensorML provider created :";
            final String sgbdType = provider.getSource().parameters.get(KEY_SGBDTYPE);
            if (sgbdType != null && sgbdType.equals("derby")) {
                msg = msg + "java DB: > "
                          + provider.getSource().parameters.get(KEY_DERBYURL);
            } else {
                msg = msg + provider.getSource().parameters.get(KEY_HOST) + " > "
                          + provider.getSource().parameters.get(KEY_DATABASE);
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
