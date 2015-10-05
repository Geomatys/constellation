/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.gui.Mock;

import org.constellation.ServiceDef;
import org.constellation.configuration.Layer;
import org.constellation.configuration.LayerList;
import org.constellation.gui.service.MapManager;

import java.util.ArrayList;

/**
 * Just WMS Mock
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 *
 */
public class MapManagerMock extends MapManager {

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
