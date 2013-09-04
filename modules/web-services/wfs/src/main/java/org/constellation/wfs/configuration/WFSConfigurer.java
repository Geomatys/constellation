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

package org.constellation.wfs.configuration;

import org.constellation.ServiceDef.Specification;
import org.constellation.map.configuration.MapConfigurer;

/**
 * {@link org.constellation.configuration.ServiceConfigurer} implementation for WFS service.
 *
 * TODO: implement specific configuration methods
 *
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public class WFSConfigurer extends MapConfigurer {

    /**
     * Create a new {@link WFSConfigurer} instance.
     */
    public WFSConfigurer() {
        super(Specification.WFS);
    }
}
