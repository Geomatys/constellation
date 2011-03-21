/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010, Geomatys
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
package org.constellation.provider.coveragefile;

import java.util.logging.Level;

import org.constellation.provider.AbstractProviderService;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.LayerProvider;
import org.constellation.provider.LayerProviderService;
import org.constellation.provider.configuration.ProviderSource;

import org.opengis.feature.type.Name;

import static org.constellation.provider.coveragefile.CoverageMosaicProvider.*;

/**
 * Service providing mosaic coverage reader.
 * @version $Id$
 *
 * @author Johann Sorel (Geomatys)
 */
public class CoverageMosaicProviderService extends AbstractProviderService
        <Name,LayerDetails,LayerProvider> implements LayerProviderService {

    private static final String ERROR_MSG = "[PROVIDER]> Invalid mosaic coverage provider config";

    public CoverageMosaicProviderService(){
        super("coverage-mosaic");
    }

    @Override
    public LayerProvider createProvider(ProviderSource ps) {
        try {
            final CoverageMosaicProvider provider = new CoverageMosaicProvider(this,ps);
            getLogger().log(Level.INFO, "[PROVIDER]> Mosaic coverage provider created : {0}",
                    provider.getSource().parameters.get(KEY_FOLDER_PATH));
        } catch (Exception ex) {
            // we should not catch exception, but here it's better to start all source we can
            // rather than letting a potential exception block the provider proxy
            getLogger().log(Level.SEVERE, ERROR_MSG, ex);
        }
        return null;
    }

}
