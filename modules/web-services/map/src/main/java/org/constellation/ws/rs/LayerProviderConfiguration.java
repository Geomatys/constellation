/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2012, Geomatys
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

import org.apache.sis.storage.DataStoreException;
import org.constellation.configuration.AcknowlegementType;
import org.constellation.dto.AttributeInfo;
import org.constellation.dto.BandInfo;
import org.constellation.dto.CoverageDataInfo;
import org.constellation.dto.DataInfo;
import org.constellation.dto.FeatureDataInfo;
import org.constellation.provider.FeatureLayerDetails;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.LayerProvider;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.configuration.ProviderParameters;
import org.geotoolkit.coverage.GridSampleDimension;
import org.geotoolkit.feature.DefaultName;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;

import java.io.IOException;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;

/**
 * Utility class for layer provider management/configuration.
 *
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class LayerProviderConfiguration {

    /**
     * Gives a {@link DataInfo} object describing the layer data source.
     * <p>
     * {@link DataInfo} has two distinct implementations:
     * <ui>
     *     <li>{@link FeatureDataInfo} for feature layers.</li>
     *     <li>{@link CoverageDataInfo} for coverage layers.</li>
     * </ui>
     *
     * @param providerId the layer provider id
     * @param layerName  the layer name
     * @return the {@link DataInfo} or an {@link AcknowlegementType} on error
     */
    public static Object getLayerDataInfo(final String providerId, final String layerName) {
        ensureNonNull("providerId", providerId);
        ensureNonNull("layerName", layerName);

        // Get the provider.
        final LayerProvider provider = LayerProviderProxy.getInstance().getProvider(providerId);
        if (provider == null) {
            return new AcknowlegementType("Failure", "No layer provider for id \"" + providerId + "\".");
        }

        // Get the layer.
        final Name name = new DefaultName(ProviderParameters.getNamespace(provider), layerName);
        final LayerDetails layer = LayerProviderProxy.getInstance().get(name, providerId);
        if (layer == null) {
            return new AcknowlegementType("Failure", "No layer named \"" + layerName + "\" in provider with id \"" + providerId + "\".");
        }

        // Try to extract layer data info.
        try {
            if (layer instanceof FeatureLayerDetails) {
                // Feature layer case.
                final FeatureType featureType = ((FeatureLayerDetails) layer).getStore().getFeatureType(layer.getName());
                final FeatureDataInfo info = new FeatureDataInfo();
                for (final PropertyDescriptor desc : featureType.getDescriptors()) {
                    info.getAttributes().add(new AttributeInfo(
                            desc.getName().getNamespaceURI(),
                            desc.getName().getLocalPart(),
                            desc.getType().getBinding()));
                }
                return info;
            } else {
                // Coverage layer case.
                final GridSampleDimension[] dims = layer.getCoverage(null, null, null, null).getSampleDimensions();
                final CoverageDataInfo info = new CoverageDataInfo();
                for (final GridSampleDimension dim : dims) {
                    info.getBands().add(new BandInfo(
                            dim.getMinimumValue(),
                            dim.getMaximumValue()));
                }
                return info;
            }
        } catch (DataStoreException ex) {
            return new AcknowlegementType("Failure", "An error occurred while trying get/open datastore for provider with id: " + providerId);
        } catch (IOException ex) {
            return new AcknowlegementType("Failure", "An error occurred while trying get data info for provider with id: " + providerId);
        }
    }
}
