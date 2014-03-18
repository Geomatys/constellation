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

import org.apache.sis.util.logging.Logging;
import org.constellation.configuration.Layer;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.Source;
import org.constellation.map.security.LayerSecurityFilter;
import org.constellation.provider.DataProviders;
import org.opengis.feature.type.Name;
import org.opengis.parameter.ParameterValueGroup;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author Guilhem Legal (Geomatys).
 * @author Fabien Bernard (Geomatys).
 */
public class MapUtilities {

    private static final Logger LOGGER = Logging.getLogger(MapUtilities.class);

    public static List<Layer> getConfigurationLayers(final LayerContext layerContext, final LayerSecurityFilter securityFilter, final String login) {
        if (layerContext == null) {
            return new ArrayList<>();
        }
        final DataProviders namedProxy = DataProviders.getInstance();
        final List<Layer> layers = new ArrayList<>();
        /*
         * For each source declared in the layer context we search for layers informations.
         */
        for (final Source source : layerContext.getLayers()) {
            final String sourceID = source.getId();
            final Set<Name> layerNames = namedProxy.getKeys(sourceID);
            for (final Name layerName : layerNames) {
                final QName qn = new QName(layerName.getNamespaceURI(), layerName.getLocalPart());

                /*
                 * first case : source is in load-all mode
                 */
                if (source.getLoadAll()) {
                    // we look if the layer is excluded and if is allowed
                    if (!source.isExcludedLayer(qn) && (securityFilter == null || securityFilter.allowed(login, layerName))) {
                        Layer layer = source.isIncludedLayer(qn);
                        if (layer == null) {
                            layer = new Layer(qn);
                        }
                        final ParameterValueGroup provider = DataProviders.getInstance().getProvider(sourceID).getSource();
                        layer.setProviderType((String) provider.parameter("providerType").getValue());
                        layer.setProviderID(sourceID);
                        layers.add(layer);
                    }
                /*
                 * second case : we include only the layer in the balise include
                 */
                } else {

                    /*
                     * Get all layer with layer name
                     * NOTE : This case is for get all layer with same name and same source provider
                     * but different data version.
                     */
                    final List<Layer> allLayer = source.allIncludedLayer(qn);
                    if (!allLayer.isEmpty()) {
                        for (Layer layer : allLayer) {
                            layer.setProviderID(sourceID);
                            final ParameterValueGroup provider = DataProviders.getInstance().getProvider(sourceID).getSource();
                            layer.setProviderType((String) provider.parameter("providerType").getValue());
                            layers.add(layer);
                        }
                    }
                }
            }
        }
        return layers;
    }
}
