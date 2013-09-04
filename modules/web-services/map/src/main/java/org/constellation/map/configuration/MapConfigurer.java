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

package org.constellation.map.configuration;

import org.constellation.ServiceDef.Specification;
import org.constellation.configuration.Layer;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.NoSuchInstanceException;
import org.constellation.dto.AddLayer;
import org.constellation.ogc.configuration.OGCConfigurer;
import org.constellation.process.service.AddLayerToMapServiceDescriptor;
import org.constellation.provider.LayerProvider;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.configuration.ProviderParameters;
import org.constellation.util.DataReference;
import org.constellation.ws.rs.MapUtilities;
import org.geotoolkit.process.ProcessDescriptor;
import org.geotoolkit.process.ProcessException;
import org.opengis.parameter.ParameterValueGroup;

import java.util.List;

/**
 * {@link org.constellation.configuration.ServiceConfigurer} base for "map" services.
 *
 * @author Fabien Bernard (Geomatys).
 * @author Benjamin Garcia (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public class MapConfigurer extends OGCConfigurer {

    /**
     * Create a new {@link MapConfigurer} instance.
     *
     * @param specification  the target service specification
     */
    public MapConfigurer(final Specification specification) {
        super(specification, LayerContext.class, "layerContext.xml");
    }

    /**
     * Adds a new layer to a "map" service instance.
     *
     * @param addLayerData the layer to be added
     * @throws NoSuchInstanceException if the service with specified identifier does not exist
     * @throws ProcessException if the process used to perform action has failed
     */
    public void addLayer(final AddLayer addLayerData) throws NoSuchInstanceException, ProcessException {
        this.ensureExistingInstance(addLayerData.getServiceId());

        final LayerProvider provider = LayerProviderProxy.getInstance().getProvider(addLayerData.getProviderId());
        final String namespace = ProviderParameters.getNamespace(provider);
        final String layerId = "{" + namespace + "}" + addLayerData.getLayerId();

        // Set layer and style provider reference.
        final DataReference layerProviderReference = DataReference.createProviderDataReference(DataReference.PROVIDER_LAYER_TYPE, addLayerData.getProviderId(), layerId);
        final DataReference styleProviderReference = DataReference.createProviderDataReference(DataReference.PROVIDER_STYLE_TYPE, addLayerData.getStyleProviderId(), addLayerData.getStyleId());

        // Build descriptor.
        final ProcessDescriptor descriptor = getProcessDescriptor("service.add_layer");
        final ParameterValueGroup inputs = descriptor.getInputDescriptor().createValue();
        inputs.parameter(AddLayerToMapServiceDescriptor.LAYER_REF_PARAM_NAME).setValue(layerProviderReference);
        inputs.parameter(AddLayerToMapServiceDescriptor.LAYER_ALIAS_PARAM_NAME).setValue(addLayerData.getLayerAlias());
        inputs.parameter(AddLayerToMapServiceDescriptor.LAYER_STYLE_PARAM_NAME).setValue(styleProviderReference);
        inputs.parameter(AddLayerToMapServiceDescriptor.SERVICE_TYPE_PARAM_NAME).setValue(addLayerData.getServiceType());
        inputs.parameter(AddLayerToMapServiceDescriptor.SERVICE_INSTANCE_PARAM_NAME).setValue(addLayerData.getServiceId());

        // Call process.
        descriptor.createProcess(inputs).call();
    }

    /**
     * Extracts and returns the list of {@link Layer}s available on a "map" service.
     *
     * @param identifier the service identifier
     * @return the {@link Layer} list
     * @throws NoSuchInstanceException if the service with specified identifier does not exist
     * @throws ProcessException if the process used to perform action has failed
     */
    public List<Layer> getLayers(final String identifier) throws NoSuchInstanceException, ProcessException {
        this.ensureExistingInstance(identifier);

        // Extracts the layer list from service configuration.
        final LayerContext layerContext = (LayerContext) this.getInstanceConfiguration(identifier);
        return MapUtilities.getConfigurationLayers(layerContext, null, null);
    }
}
