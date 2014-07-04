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
package org.constellation.ws.rs;

import org.constellation.ServiceDef.Specification;
import org.constellation.map.configuration.MapConfigurer;
import org.constellation.provider.DataProviders;
import org.constellation.provider.StyleProviders;
import org.constellation.ws.Worker;

/**
 * A Super class for WMS, WMTS, WFS and WCS web-service.
 * The point is to remove the hard-coded dependency to JAI.
 *
 * @author Guilhem Legal (Geomatys)
 * @author Cédric Briançon (Geomatys)
 * @author Benjamin Garcia (Geomatys)
 *
 * @version 0.9
 * @since 0.5
 */
public abstract class GridWebService<W extends Worker> extends OGCWebService<W> {

    public GridWebService(final Specification specification) {
        super(specification);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void specificRestart(String identifier) {
        LOGGER.info("reloading provider");
        // clear style and layer caches.
        StyleProviders.getInstance().dispose();
        DataProviders.getInstance().dispose();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<? extends MapConfigurer> getConfigurerClass() {
        return MapConfigurer.class;
    }
}
