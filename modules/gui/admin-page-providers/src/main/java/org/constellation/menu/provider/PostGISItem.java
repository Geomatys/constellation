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
public class PostGISItem extends AbstractMenuItem{

    public PostGISItem() {
        super(new String[]{
                "/provider/postgis.xhtml",
                "/provider/postgisConfig.xhtml",
                "/provider/postgisLayerConfig.xhtml"},
            "provider.postgis",
            new Path(PROVIDERS_DATA_PATH,"PostGIS", "/provider/postgis.xhtml", null,200)
            );
    }

    @Override
    public boolean isAvailable(final ConstellationServer server) {
        if(server == null) return false;
        final ProvidersReport report = server.providers.listProviders();
        if(report == null) return false;
        return report.getProviderService(PostGISBean.SERVICE_NAME) != null;
    }
    
}
