/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
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
package org.constellation.gui.Mock;

import org.constellation.ServiceDef;
import org.constellation.configuration.Layer;
import org.constellation.configuration.LayerList;
import org.constellation.gui.service.MapManager;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Just WMS Mock
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 *
 */
public class MapManagerMock extends MapManager {

    private static final Logger LOGGER = Logger.getLogger(MapManagerMock.class.getName());


    /**
     *
     * @param serviceName
     * @param specification
     * @return
     */
    @Override
    public LayerList getLayers(String serviceName, final ServiceDef.Specification specification) {
        return new LayerList(new ArrayList<Layer>(0));
    }
}
