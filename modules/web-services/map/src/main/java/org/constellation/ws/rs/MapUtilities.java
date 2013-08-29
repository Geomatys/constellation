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

package org.constellation.ws.rs;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;
import org.constellation.configuration.Layer;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.Source;
import org.constellation.map.security.LayerSecurityFilter;
import org.constellation.provider.LayerProviderProxy;
import org.opengis.feature.type.Name;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class MapUtilities {

    public static List<Layer> getConfigurationLayers(final LayerContext layerContext, final LayerSecurityFilter securityFilter, final String login) {
        if (layerContext == null) {
            return new ArrayList<>();
        }
        final LayerProviderProxy namedProxy  = LayerProviderProxy.getInstance();
        final List<Layer> layers = new ArrayList<>();
        /*
         * For each source declared in the layer context we search for layers informations.
         */
        for (final Source source : layerContext.getLayers()) {
            final String sourceID = source.getId();
            final Set<Name> layerNames = namedProxy.getKeys(sourceID);
                for(final Name layerName : layerNames) {
                final QName qn = new QName(layerName.getNamespaceURI(), layerName.getLocalPart());
                /*
                 * first case : source is in load-all mode
                 */
                if (source.getLoadAll()) {
                    // we look if the layer is excluded
                    if (source.isExcludedLayer(qn)) {
                        continue;
                    // we look for detailled informations in the include sections
                    } else {
                        if (securityFilter.allowed(login, layerName)) {
                            Layer layer = source.isIncludedLayer(qn);
                            if (layer == null) {
                                layer = new Layer(qn);
                            }
                            layer.setProviderID(sourceID);
                            layers.add(layer);
                        }
                    }
                /*
                 * second case : we include only the layer in the balise include
                 */
                } else {
                    Layer layer = source.isIncludedLayer(qn);
                    if (layer != null && securityFilter.allowed(login, layerName)) {
                        layer.setProviderID(sourceID);
                        layers.add(layer);
                    }
                }
            }
        }
        return layers;
    }
}
