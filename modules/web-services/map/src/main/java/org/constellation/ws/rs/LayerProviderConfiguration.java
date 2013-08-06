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
import org.constellation.dto.BandDescription;
import org.constellation.dto.PropertyDescription;
import org.constellation.dto.CoverageDataDescription;
import org.constellation.dto.DataDescription;
import org.constellation.dto.FeatureDataDescription;
import org.constellation.provider.FeatureLayerDetails;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.LayerProvider;
import org.constellation.provider.LayerProviderProxy;
import org.constellation.provider.configuration.ProviderParameters;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.coverage.GridSampleDimension;
import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.data.query.QueryBuilder;
import org.geotoolkit.data.session.Session;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.referencing.CRS;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.util.FactoryException;

import java.io.IOException;
import java.util.Iterator;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import static org.apache.sis.util.ArgumentChecks.ensurePositive;

/**
 * Utility class for layer provider management/configuration.
 *
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class LayerProviderConfiguration {

    /**
     * Gets a {@link LayerDetails} instance from its layer provider ID and its name.
     *
     * @param providerId the layer provider id
     * @param layerName  the layer name
     * @return the {@link LayerDetails} instance
     * @throws CstlServiceException if the provider or the layer does not exists
     */
    public static LayerDetails getLayer(final String providerId, final String layerName) throws CstlServiceException {
        ensureNonNull("providerId", providerId);
        ensureNonNull("layerName",  layerName);

        // Get the provider.
        final LayerProvider provider = LayerProviderProxy.getInstance().getProvider(providerId);
        if (provider == null) {
            throw new CstlServiceException("No layer provider for id \"" + providerId + "\".");
        }

        // Get the layer.
        final Name name = new DefaultName(ProviderParameters.getNamespace(provider), layerName);
        final LayerDetails layer = provider.get(name);
        if (layer == null) {
            throw new CstlServiceException("No layer named \"" + layerName + "\" in provider with id \"" + providerId + "\".");
        }

        // Return the layer.
        return layer;
    }

    /**
     * Gives a {@link DataDescription} instance describing the layer data source.
     * <p>
     * {@link DataDescription} has two distinct implementations:
     * <ui>
     *     <li>{@link FeatureDataDescription} for feature layers.</li>
     *     <li>{@link CoverageDataDescription} for coverage layers.</li>
     * </ui>
     *
     * @param providerId the layer provider id
     * @param layerName  the layer name
     * @return the {@link DataDescription} instance
     * @throws CstlServiceException if failed to extracts the data description for any reason
     */
    public static DataDescription getDataDescription(final String providerId, final String layerName) throws CstlServiceException {
        ensureNonNull("providerId", providerId);
        ensureNonNull("layerName",  layerName);

        // Get the layer.
        final LayerDetails layer = getLayer(providerId, layerName);

        // Try to extract layer data info.
        try {
            if (layer instanceof FeatureLayerDetails) {
                // Feature layer case.
                final FeatureStore store = ((FeatureLayerDetails) layer).getStore();

                final FeatureDataDescription info = new FeatureDataDescription();

                final FeatureType featureType = store.getFeatureType(layer.getName());
                final PropertyDescriptor geometryDesc = featureType.getGeometryDescriptor();
                info.setGeometryProperty(new PropertyDescription(
                        geometryDesc.getName().getNamespaceURI(),
                        geometryDesc.getName().getLocalPart(),
                        geometryDesc.getType().getBinding()));
                for (final PropertyDescriptor desc : featureType.getDescriptors()) {
                    info.getProperties().add(new PropertyDescription(
                            desc.getName().getNamespaceURI(),
                            desc.getName().getLocalPart(),
                            desc.getType().getBinding()));
                }

                final QueryBuilder queryBuilder = new QueryBuilder();
                queryBuilder.setTypeName(layer.getName());
                Envelope envelope = store.getEnvelope(queryBuilder.buildQuery());
                try {
                    envelope = CRS.transform(envelope, CRS.decode("CRS:84"));
                    final DirectPosition lower = envelope.getLowerCorner();
                    final DirectPosition upper = envelope.getUpperCorner();
                    info.setBoundingBox(new double[] {
                            lower.getCoordinate()[0],
                            lower.getCoordinate()[1],
                            upper.getCoordinate()[0],
                            upper.getCoordinate()[1]});
                } catch (FactoryException ignore) {
                } catch (TransformException ignore) {
                }

                return info;
            } else {
                // Coverage layer case.
                final GridCoverage2D coverage = layer.getCoverage(null, null, null, null);

                final CoverageDataDescription info = new CoverageDataDescription();

                final GridSampleDimension[] dims = coverage.getSampleDimensions();
                for (final GridSampleDimension dim : dims) {
                    info.getBands().add(new BandDescription(
                            dim.getMinimumValue(),
                            dim.getMaximumValue(),
                            dim.getNoDataValues()));
                }

                Envelope envelope = coverage.getEnvelope();
                try {
                    envelope = CRS.transform(envelope, CRS.decode("CRS:84"));
                    final DirectPosition lower = envelope.getLowerCorner();
                    final DirectPosition upper = envelope.getUpperCorner();
                    info.setBoundingBox(new double[] {
                            lower.getCoordinate()[0],
                            lower.getCoordinate()[1],
                            upper.getCoordinate()[0],
                            upper.getCoordinate()[1]});
                } catch (FactoryException ignore) {
                } catch (TransformException ignore) {
                }

                return info;
            }
        } catch (DataStoreException ex) {
            throw new CstlServiceException("An error occurred while trying get/open datastore for provider with id \"" + providerId + "\".");
        } catch (IOException ex) {
            throw new CstlServiceException("An error occurred while trying get data info for provider with id \"" + providerId + "\".");
        }
    }

    /**
     *
     * @param providerId the layer provider id
     * @param layerName  the layer name
     * @param property   the attribute name
     * @return the values
     * @throws CstlServiceException if failed to extracts the attribute values for any reason
     */
    public static Object[] getPropertyValues(final String providerId, final String layerName, final String property) throws CstlServiceException {
        ensureNonNull("providerId", providerId);
        ensureNonNull("layerName",  layerName);
        ensureNonNull("property",   property);

        // Get the layer.
        final LayerDetails layer = getLayer(providerId, layerName);

        // Try to extract attribute values.
        if (layer instanceof FeatureLayerDetails) {
            final Session session = ((FeatureLayerDetails) layer).getStore().createSession(false);
            final QueryBuilder qb = new QueryBuilder();
            qb.setProperties(new String[]{property});
            qb.setTypeName(layer.getName());
            final FeatureCollection collection = session.getFeatureCollection(qb.buildQuery());
            final Iterator<Feature> iterator = collection.iterator();

            final Object[] values = new Object[collection.size()];
            int i = 0;
            while (iterator.hasNext()) {
                final Feature feature = iterator.next();
                values[i] = feature.getProperty(property).getValue();
            }
            return values;
        }

        // Not a feature layer.
        throw new CstlServiceException("The layer named \"" + layerName + "\" for provider with id \"" + providerId + "\" is not a feature layer.");
    }

    /**
     *
     * @param providerId the layer provider id
     * @param layerName  the layer name
     * @param bandIndex  the band index
     * @return the values
     * @throws CstlServiceException if failed to extracts the band values for any reason
     */
    public static Number[] getBandValues(final String providerId, final String layerName, final int bandIndex) throws CstlServiceException {
        ensureNonNull("providerId", providerId);
        ensureNonNull("layerName",  layerName);
        ensurePositive("bandIndex", bandIndex);

        // Get the layer.
        final LayerDetails layer = getLayer(providerId, layerName);

        // Try to extract band values.
        if (!(layer instanceof FeatureLayerDetails)) {
            try {
                final GridSampleDimension[] dims = layer.getCoverage(null, null, null, null).getSampleDimensions();
                // TODO
            } catch (DataStoreException ex) {
                throw new CstlServiceException("An error occurred while trying get/open datastore for provider with id \"" + providerId + "\".");
            } catch (IOException ex) {
                throw new CstlServiceException("An error occurred while trying get data info for provider with id \"" + providerId + "\".");
            }
        }

        // Not a coverage layer.
        throw new CstlServiceException("The layer named \"" + layerName + "\" for provider with id \"" + providerId + "\" is not a coverage layer.");
    }
}