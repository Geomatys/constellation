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

import org.constellation.ServiceDef.Specification;
import org.constellation.configuration.LayerContext;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public class WCSBean extends AbstractServiceBean{

    public WCSBean() {
        super(Specification.WCS,
                "/service/wcs.xhtml",
                "/service/wcsConfig.xhtml");
    }

    @Override
    public Object getConfigurationObject() {
        Object candidate = super.getConfigurationObject();

        if(candidate == null){
            candidate = new LayerContext();
        }

        return candidate;
    }

}
