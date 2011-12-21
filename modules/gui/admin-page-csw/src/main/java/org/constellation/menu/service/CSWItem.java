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
 * Add a CSW service page.
 *
 * @author Johann Sorel (Geomatys)
 */
public class CSWItem extends AbstractMenuItem {

    public CSWItem() {
        super(new String[]{
                "/service/csw.xhtml",
                "/service/cswConfig.xhtml"},
            "service.csw",
            new Path(SERVICES_PATH,"CSW", "/service/csw.xhtml",null,600)
            );
    }
    
    @Override
    public boolean isAvailable(final ConstellationServer server) {
        return serviceAvailable(server, "CSW");
    }

}
