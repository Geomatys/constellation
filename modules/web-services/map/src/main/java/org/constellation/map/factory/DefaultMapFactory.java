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
package org.constellation.map.factory;

import org.constellation.configuration.DataSourceType;
import org.constellation.ws.LayerSecurityFilter;
import org.constellation.map.security.NoLayerSecurityFilter;
import org.constellation.ws.MapFactory;

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
