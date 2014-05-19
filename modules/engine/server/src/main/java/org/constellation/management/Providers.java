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

package org.constellation.management;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.constellation.provider.DataProviders;
import org.constellation.provider.StyleProviders;
import org.geotoolkit.feature.DefaultName;
import org.apache.sis.util.logging.Logging;

import org.opengis.feature.type.Name;

/**
 * Providers MBean implementation.
 * 
 * @author Johann Sorel (Geomatys)
 */
public final class Providers implements ProvidersMBean{

    static final Providers INSTANCE = new Providers();
    private static final Logger LOGGER = Logging.getLogger(Providers.class);

    private Providers(){}

    @Override
    public synchronized void reloadLayerProviders() {
        DataProviders.getInstance().reload();
    }

    @Override
    public synchronized void reloadStyleProviders() {
        StyleProviders.getInstance().reload();
    }

    @Override
    public List<String> getLayerList() {
        final List<String> names = new ArrayList<String>();
        try {
            for (Name n : DataProviders.getInstance().getKeys()) {
                names.add(DefaultName.toJCRExtendedForm(n));
            }
            Collections.sort(names);
        } catch (IllegalStateException ex) {
            LOGGER.log(Level.SEVERE, "An error occurs while trying to load the layer providers.",ex);
        }
        return names;
    }

    @Override
    public List<String> getStyleList() {
        final List<String> names = new ArrayList<String>();
        try {
            names.addAll(StyleProviders.getInstance().getKeys());
            Collections.sort(names);
        } catch (IllegalStateException ex) {
            LOGGER.log(Level.SEVERE, "An error occurs while trying to load the style providers.",ex);
        }
        return names;
    }

}
