/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
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

package org.constellation.menu.provider;

import org.constellation.admin.service.ConstellationServer;
import org.constellation.bean.AbstractMenuItem;
import org.constellation.configuration.ProvidersReport;


/**
 * Add an overview page for providers.
 *
 * @author Johann Sorel (Geomatys)
 */
public class OMItem extends AbstractMenuItem{

    public OMItem() {
        super(
            new String[]{
                "/provider/om.xhtml",
                "/provider/omConfig.xhtml",
                "/provider/omLayerConfig.xhtml"},
            "provider.om",
            new Path(PROVIDERS_DATA_PATH,"O&M", "/provider/om.xhtml", null,500)
            );
    }
    
    @Override
    public boolean isAvailable(final ConstellationServer server) {
        if(server == null) return false;
        final ProvidersReport report = server.providers.listProviders();
        if(report == null) return false;
        return report.getProviderService(OMBean.SERVICE_NAME) != null;
    }

}
