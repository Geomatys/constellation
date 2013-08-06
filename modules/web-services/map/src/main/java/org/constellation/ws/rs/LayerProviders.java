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

import org.apache.sis.geometry.Envelope2D;
import org.apache.sis.storage.DataStoreException;
import org.apache.sis.util.Static;
import org.constellation.dto.BandDescription;
import org.constellation.dto.CoverageDataDescription;
import org.constellation.dto.DataDescription;
import org.constellation.dto.FeatureDataDescription;
import org.constellation.dto.PropertyDescription;
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
import org.geotoolkit.display2d.service.CanvasDef;
import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.referencing.CRS;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.geometry.Envelope;
import org.opengis.util.FactoryException;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;
import static org.apache.sis.util.ArgumentChecks.ensurePositive;

/**
 * Utility class for layer provider management/configuration.
 *
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class LayerProviders extends Static {

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
                return getFeatureDataDescription((FeatureLayerDetails) layer);
            } else {
                return getCoverageDataDescription(layer);
            }
        } catch (DataStoreException ex) {
            throw new CstlServiceException("An error occurred while trying get/open datastore for provider with id \"" + providerId + "\".");
        } catch (IOException ex) {
            throw new CstlServiceException("An error occurred while trying get data for provider with id \"" + providerId + "\".");
        }
    }

    public static BufferedImage portray(final Object context) throws CstlServiceException {
        ensureNonNull("context", context);

        // Get the provider.
        final LayerProvider provider = getProvider(null);

        try {
            // Build portrayal inputs from context.
            final Envelope envelope = new Envelope2D(CRS.decode(null), 0.0, 0.0, 0.0, 0.0);

            final CanvasDef canvasDef = new CanvasDef(new Dimension(0, 0), null);

            return null;
        } catch (FactoryException ex) {
            throw new CstlServiceException("");
        }
    }

    /**
     * Returns all the values of a layer feature collection filtered on the specified
     * property.
     *
     * @param providerId the layer provider id
     * @param layerName  the layer name
     * @param property   the property name
     * @return the values
     * @throws CstlServiceException if failed to extracts the attribute values for any reason
     */
    public static Object[] getPropertyValues(final String providerId, final String layerName, final String property) throws CstlServiceException {
        ensureNonNull("providerId", providerId);
        ensureNonNull("layerName",  layerName);
        ensureNonNull("property", property);

        // Get the layer.
        final LayerDetails layer = getLayer(providerId, layerName);

        // Try to extract attribute values.
        if (layer instanceof FeatureLayerDetails) {

            // Open session.
            final Session session = ((FeatureLayerDetails) layer).getStore().createSession(false);

            // Get feature collection.
            final QueryBuilder qb = new QueryBuilder();
            qb.setProperties(new String[]{property});
            qb.setTypeName(layer.getName());
            final FeatureCollection collection = session.getFeatureCollection(qb.buildQuery());

            // Visit collection.
            final Object[] values = new Object[collection.size()];
            int i = 0;
            for (final Feature feature : collection) {
                values[i] = feature.getProperty(property).getValue();
                i++;
            }

            return values;
        }

        // Not a feature layer.
        throw new CstlServiceException("The layer named \"" + layerName + "\" for provider with id \"" + providerId + "\" is not a feature layer.");
    }

    /**
     * Returns all the values of a coverage layer band filtered on the specified band
     * index.
     *
     * @param providerId the layer provider id
     * @param layerName  the layer name
     * @param bandIndex  the band index
     * @return the values
     * @throws CstlServiceException if failed to extracts the band values for any reason
     */
    public static double[] getBandValues(final String providerId, final String layerName, final int bandIndex) throws CstlServiceException {
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


    /**************************************************************************
     *                            Private methods                             *
     **************************************************************************/

    /**
     * Gets a {@link LayerProvider} instance from its ID.
     *
     * @param providerId the layer provider id
     * @return the {@link LayerProvider} instance
     * @throws CstlServiceException if the provider does not exists
     */
    private static LayerProvider getProvider(final String providerId) throws CstlServiceException {
        final LayerProvider provider = LayerProviderProxy.getInstance().getProvider(providerId);
        if (provider == null) {
            throw new CstlServiceException("No layer provider for id \"" + providerId + "\".");
        }
        return provider;
    }

    /**
     * Gets a {@link LayerDetails} instance from its layer provider and its name.
     *
     * @param provider the layer provider
     * @param layerName  the layer name
     * @return the {@link LayerDetails} instance
     * @throws CstlServiceException if the layer does not exists
     */
    private static LayerDetails getLayer(final LayerProvider provider, final String layerName) throws CstlServiceException {
        final Name name = new DefaultName(ProviderParameters.getNamespace(provider), layerName);
        final LayerDetails layer = provider.get(name);
        if (layer == null) {
            throw new CstlServiceException("No layer named \"" + layerName + "\" in provider with id \"" + provider.getId() + "\".");
        }
        return layer;
    }

    /**
     * Gets a {@link LayerDetails} instance from its layer provider ID and its name.
     *
     * @param providerId the layer provider id
     * @param layerName  the layer name
     * @return the {@link LayerDetails} instance
     * @throws CstlServiceException if the provider or the layer does not exists
     */
    private static LayerDetails getLayer(final String providerId, final String layerName) throws CstlServiceException {
        return getLayer(getProvider(providerId), layerName);
    }

    /**
     * Gives a {@link CoverageDataDescription} instance describing the coverage layer
     * data source.
     *
     * @param layer the layer to visit
     * @return the {@link CoverageDataDescription} instance
     * @throws IOException if an error occurred while trying to read the coverage
     * @throws DataStoreException if an error occurred during coverage store operations
     */
    private static CoverageDataDescription getCoverageDataDescription(final LayerDetails layer) throws IOException, DataStoreException {
        final CoverageDataDescription description = new CoverageDataDescription();

        // Acquire coverage data.
        final GridCoverage2D coverage = layer.getCoverage(null, null, null, null);

        // Bands description.
        final GridSampleDimension[] dims = coverage.getSampleDimensions();
        for (final GridSampleDimension dim : dims) {
            description.getBands().add(new BandDescription(
                    dim.getMinimumValue(),
                    dim.getMaximumValue(),
                    dim.getNoDataValues()));
        }

        // Geographic extent description.
        final Envelope envelope = coverage.getEnvelope();
        fillGeographicDescription(envelope, description);

        return description;
    }

    /**
     * Gives a {@link CoverageDataDescription} instance describing the coverage layer
     * data source.
     *
     * @param layer the layer to visit
     * @return the {@link FeatureDataDescription} instance
     * @throws DataStoreException if an error occurred during feature store operations
     */
    private static FeatureDataDescription getFeatureDataDescription(final FeatureLayerDetails layer) throws DataStoreException {
        final FeatureDataDescription description = new FeatureDataDescription();

        // Acquire data feature type.
        final FeatureStore store = layer.getStore();
        final FeatureType featureType = store.getFeatureType(layer.getName());

        // Feature attributes description.
        final PropertyDescriptor geometryDesc = featureType.getGeometryDescriptor();
        description.setGeometryProperty(new PropertyDescription(
                geometryDesc.getName().getNamespaceURI(),
                geometryDesc.getName().getLocalPart(),
                geometryDesc.getType().getBinding()));
        for (final PropertyDescriptor desc : featureType.getDescriptors()) {
            description.getProperties().add(new PropertyDescription(
                    desc.getName().getNamespaceURI(),
                    desc.getName().getLocalPart(),
                    desc.getType().getBinding()));
        }

        // Geographic extent description.
        final QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.setTypeName(layer.getName());
        final Envelope envelope = store.getEnvelope(queryBuilder.buildQuery());
        fillGeographicDescription(envelope, description);

        return description;
    }

    /**
     * Fills the geographical field of a {@link DataDescription} instance according the
     * specified {@link Envelope}.
     *
     * @param envelope    the envelope to visit
     * @param description the data description to update
     */
    private static void fillGeographicDescription(Envelope envelope, final DataDescription description) {
        double[] lower, upper;
        try {
            envelope = CRS.transform(envelope, CRS.decode("CRS:84"));
            lower = envelope.getLowerCorner().getCoordinate();
            upper = envelope.getUpperCorner().getCoordinate();
        } catch (Exception ignore) {
            lower = new double[]{-180, -90};
            upper = new double[]{ 180,  90};
        }
        description.setBoundingBox(new double[]{lower[0], lower[1], upper[0], upper[1]});
    }
}