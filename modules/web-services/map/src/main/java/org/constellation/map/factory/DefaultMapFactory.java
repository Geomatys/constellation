/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
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
package org.constellation.map.factory;

import org.constellation.configuration.DataSourceType;
import org.constellation.map.security.LayerSecurityFilter;
import org.constellation.map.security.NoLayerSecurityFilter;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class DefaultMapFactory implements MapFactory {

    @Override
    public LayerSecurityFilter getSecurityFilter() {
        return new NoLayerSecurityFilter();
    }

    @Override
    public boolean factoryMatchType(final DataSourceType type) {
        if (type == null || type.getName().isEmpty()) {
            return true;
        }
        return false;
    }

}
