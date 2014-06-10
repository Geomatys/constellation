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

import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.sis.util.logging.Logging;
import org.constellation.configuration.Layer;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.Source;
import org.constellation.map.security.LayerSecurityFilter;
import org.constellation.provider.DataProviders;
import org.geotoolkit.feature.type.Name;
import org.opengis.parameter.ParameterValueGroup;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import org.apache.sis.util.Version;
import org.geotoolkit.sld.MutableStyledLayerDescriptor;
import org.geotoolkit.sld.xml.Specification;
import org.geotoolkit.sld.xml.StyleXmlIO;
import org.opengis.util.FactoryException;

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
    
    /**
     * Verify that all layers are queryable for a {@code GetFeatureInfo}.
     *
     * @param queryLayers A list of requested layer names
     * @param version The version of the WMS service.
     * @return The same list as provided if all layers are queryable.
     *
     * @todo The method {@link Layer#isQueryable} is not valid. It should verify in the
     *       database if a layer is queryable, meaning if a layer is queryable by a
     *       {@code GetFeatureInfo} request. Either rename the {@link Layer#isQueryable}
     *       or create a new one that provides this information.
     */
    public static List<String> areQueryableLayers(final List<String> queryLayers,
                                final Version version)
    {
        /* Do nothing for the moment, waiting for a method in {@link Layer} in order to
         * handle the queryable attribute for a {@link Layer}.
         */

        /*final NamedLayerDP dp = NamedLayerDP.getInstance();
        for (String layerName : queryLayers) {
            final LayerDetails layer = dp.get(layerName);
            if (!layer.isQueryable(Service.WMS)) {
                throw new WMSWebServiceException("Layer "+ layerName +" is not queryable",
                        WMSExceptionCode.LAYER_NOT_QUERYABLE, version);
            }
        }*/
        return queryLayers;
    }

    public static MutableStyledLayerDescriptor toSLD(final String sldBody, final String sldURL,
                                                     final Specification.StyledLayerDescriptor version) throws MalformedURLException {
        final Object src;

        if (sldBody != null && !sldBody.trim().isEmpty()) {
            src = new StringReader(sldBody);
        } else if (sldURL != null && !sldURL.trim().isEmpty()) {
            src = new URL(sldURL);
        } else {
            return null;
        }

        final StyleXmlIO styleIO = new StyleXmlIO();
        try {
            return styleIO.readSLD(src, version);
        } catch (JAXBException ex) {
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
        } catch (FactoryException ex) {
            LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
        }

        return null;
    }
}
