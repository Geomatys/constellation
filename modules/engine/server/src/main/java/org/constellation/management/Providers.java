/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010, Geomatys
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

package org.constellation.management;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.StyleProviderProxy;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.util.logging.Logging;

import org.opengis.feature.type.Name;

/**
 * Providers MBean implementation.
 * 
 * @author Johann Sorel (Geomatys)
 */
public final class Providers implements ProvidersMBean{

    static final Providers INSTANCE = new Providers();
    public static final String OBJECT_NAME = Providers.class.getPackage().getName()+":type=Manager,name=Providers";
    private static final Logger LOGGER = Logging.getLogger(Providers.class);

    private Providers(){}

    @Override
    public synchronized void reloadLayerProviders() {
        LayerProviderProxy.getInstance().reload();
    }

    @Override
    public synchronized void reloadStyleProviders() {
        StyleProviderProxy.getInstance().reload();
    }

    @Override
    public List<String> getLayerList() {
        final List<String> names = new ArrayList<String>();
        try {
            for (Name n : LayerProviderProxy.getInstance().getKeys()) {
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
            names.addAll(StyleProviderProxy.getInstance().getKeys());
            Collections.sort(names);
        } catch (IllegalStateException ex) {
            LOGGER.log(Level.SEVERE, "An error occurs while trying to load the style providers.",ex);
        }
        return names;
    }

}
