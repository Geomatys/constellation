/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.constellation.provider.AbstractProviderService;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.LayerProviderService;
import org.constellation.provider.Provider;
import org.constellation.provider.configuration.ProviderSource;

import org.opengis.feature.type.Name;
import static org.constellation.provider.om.OMProvider.*;
/**
 *
 * @version $Id: 
 *
 * @author Johann Sorel (Geoamtys)
 * @author Guilhem Legal (Geomatys)
 */
public class OMProviderService extends AbstractProviderService<Name,LayerDetails> implements LayerProviderService {

    /**
     * Default logger.
     */
    private static final Logger LOGGER = Logger.getLogger(OMProviderService.class.getName());
    private static final String ERROR_MSG = "[PROVIDER]> Invalid observation provider config";

    private static final Collection<OMProvider> PROVIDERS = new ArrayList<OMProvider>();
    private static final Collection<OMProvider> IMMUTABLE = Collections.unmodifiableCollection(PROVIDERS);

    public OMProviderService(){
        super("observation");
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Collection<OMProvider> getProviders() {
        return IMMUTABLE;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    protected void disposeProvider(Provider provider) {
        if(PROVIDERS.contains(provider)){
            provider.dispose();
            PROVIDERS.remove(provider);
        }else{
            throw new IllegalArgumentException("This provider doesn't belong to this service.");
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    protected void loadProvider(ProviderSource ps) {
        try {
            OMProvider provider = new OMProvider(ps);
            PROVIDERS.add(provider);
            String msg = "[PROVIDER]> O&M provider created : ";
            String SGBDType = provider.getSource().parameters.get(KEY_SGBDTYPE);
            if (SGBDType != null && SGBDType.equals("derby")) {
                msg = msg + "java DB: > "
                          + provider.getSource().parameters.get(KEY_DERBYURL);
            } else {
                msg = msg + provider.getSource().parameters.get(KEY_HOST) + " > "
                          + provider.getSource().parameters.get(KEY_DATABASE);
            }
            LOGGER.info(msg);
        } catch (Exception ex) {
            // we should not catch exception, but here it's better to start all source we can
            // rather than letting a potential exception block the provider proxy
            LOGGER.log(Level.SEVERE, ERROR_MSG, ex);
        }
    }

}
