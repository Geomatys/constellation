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

import org.constellation.bean.AbstractMenuItem;


/**
 * Add a WCS service page.
 *
 * @author Johann Sorel (Geomatys)
 */
public class WCSItem extends AbstractMenuItem {

    public WCSItem() {
        super(new String[]{
                "/service/wcs.xhtml",
                "/service/wcsConfig.xhtml"},
            "service.wcs",
            new Path(SERVICES_PATH,"WCS", "/service/wcs.xhtml", null,400)
            );
    }

}
