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

package org.constellation.menu.service;

import org.constellation.admin.service.ConstellationServer;
import org.constellation.bean.AbstractMenuItem;


/**
 * Add a WMTS service page.
 *
 * @author Johann Sorel (Geomatys)
 */
public class WMTSItem extends AbstractMenuItem{

    public WMTSItem() {
        super(new String[]{
                "/service/wmts.xhtml",
                "/service/wmtsConfig.xhtml"},
            "org.constellation.menu.service.wmts",
            new Path(SERVICES_PATH,"WMTS", "/service/wmts.xhtml", null,100));
    }

    @Override
    public boolean isAvailable(final ConstellationServer server) {
        return serviceAvailable(server, "WMTS");
    }
}
