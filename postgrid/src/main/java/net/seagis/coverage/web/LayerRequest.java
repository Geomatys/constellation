/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2007, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package net.seagis.coverage.web;

import java.awt.geom.Dimension2D;
import org.opengis.geometry.Envelope;
import org.opengis.coverage.grid.GridRange;
import org.opengis.referencing.operation.TransformException;
import org.opengis.metadata.extent.GeographicBoundingBox;

import org.geotools.util.logging.Logging;
import org.geotools.resources.Utilities;
import org.geotools.resources.geometry.XDimension2D;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;

import net.seagis.coverage.catalog.Layer;
import net.seagis.catalog.CatalogException;


/**
 * The request for a layer, including its envelope. To be used in a hash map only.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class LayerRequest {
    /**
     * Snaps the geographic bounding boxes in order to simulate tile of this size (in pixels).
     * A smaller value will reduce the amount of pixels to load but will increase cache misses.
     * A greater value will reduce the cache misses but increase the amount of data to load and
     * cache.
     */
    private static final int TILE_SIZE = 256;

    /**
     * The global layer. We keep it by reference in order to prevent too early garbage
     * collection, since {@code LayerTable} cache its entries by weak references.
     */
    private final Layer layer;

    /**
     * The geographic bounding box, or {@code null}.
     * <strong>Do not modify</strong>.
     */
    final GeographicBoundingBox bbox;

    /**
     * The resolution, or {@code null}.
     * <strong>Do not modify</strong>.
     */
    final Dimension2D resolution;

    /**
     * Creates a {@code LayerRequest} for the given layer name, source envelope and target size.
     */
    public LayerRequest(final Layer layer, final Envelope envelope, final GridRange size)
            throws CatalogException
    {
        this.layer = layer;
        /*
         * Transforms the envelope from arbitrary CRS to WGS 84.  In case of failure, we will
         * keep the bounding box as null, which is legal and means no geographic bounding box
         * selection (load the full image).
         */
        GeographicBoundingBoxImpl bbox = null;
        if (isValid(envelope)) try {
            bbox = new GeographicBoundingBoxImpl(envelope);
        } catch (TransformException exception) {
            Logging.unexpectedException(WebServiceWorker.LOGGER, WebServiceWorker.class, "getLayer", exception);
        }
        /*
         * "Rounds" the bounding box to something a little bit bigger, and the resolution to
         * something a little bit finer. We do not allows arbitrary values because the layer
         * cache is flushed every time those values change, so we are better to have bigger
         * values than necessary and flush the cache less often. This method conceptually
         * "rounds" the bounding box on a grid.
         */
        Dimension2D resolution = layer.getAverageResolution();
        if (resolution != null) {
            final GeographicBoundingBox global = layer.getGeographicBoundingBox();
            if (bbox == null) {
                bbox = new GeographicBoundingBoxImpl(global);
            }
            double xResolution = resolution.getWidth();
            double yResolution = resolution.getHeight();
            double west  = bbox.getWestBoundLongitude();
            double east  = bbox.getEastBoundLongitude();
            double south = bbox.getSouthBoundLatitude();
            double north = bbox.getNorthBoundLatitude();
            final double xRange = (east  - west);
            final double yRange = (north - south);
            if (global != null) {
                /*
                 * Converts the user's envelope from geographic coordinates to pixel coordinates.
                 * Then, round the pixel coordinates on a grid of TILE_SIZE pixels width. Finally,
                 * converts back to geographic coordinates and make sure that the result still in
                 * the global envelope bounds.
                 */
                if (xResolution > 0 && xResolution < xRange && yResolution > 0 && yResolution < yRange) {
                    final double xOrigin = global.getWestBoundLongitude();
                    final double yOrigin = global.getSouthBoundLatitude();
                    final double xMin    = Math.floor((west  - xOrigin) / (xResolution * TILE_SIZE)) * TILE_SIZE;
                    final double xMax    = Math.ceil ((east  - xOrigin) / (xResolution * TILE_SIZE)) * TILE_SIZE;
                    final double yMin    = Math.floor((south - yOrigin) / (yResolution * TILE_SIZE)) * TILE_SIZE;
                    final double yMax    = Math.ceil ((north - yOrigin) / (yResolution * TILE_SIZE)) * TILE_SIZE;
                    west  = Math.max(xMin, 0) * xResolution + xOrigin;
                    south = Math.max(yMin, 0) * yResolution + yOrigin;
                    east  = Math.min(xMax * xResolution + xOrigin, global.getEastBoundLongitude());
                    north = Math.min(yMax * yResolution + yOrigin, global.getNorthBoundLatitude());
                    bbox.setBounds(west, east, south, north);
                } else {
                    bbox.intersect(global);
                }
            }
            /*
             * Ensures that the resolution is an integer multiple of global resolution.
             * We allows zero, which means best resolution available.
             */
            if (size != null) {
                xResolution *= Math.max(0, Math.floor(xRange / (size.getLength(0) * xResolution)));
                yResolution *= Math.max(0, Math.floor(yRange / (size.getLength(1) * yResolution)));
                resolution = (xResolution != 0 || yResolution != 0) ?
                        new XDimension2D.Double(xResolution, yResolution) : null;
            } else {
                resolution = null;
            }
        }
        if (bbox != null && bbox.isEmpty()) {
            bbox = null;
        }
        // We could call bbox.freeze() here, but it has a cost.
        // We will rather be carefull to not modify this box.
        this.bbox       = bbox;
        this.resolution = resolution;
    }

    /**
     * Returns {@code true} if the given envelope is non-empty and non-infinite.
     */
    private static boolean isValid(final Envelope envelope) {
        if (envelope == null) {
            return false;
        }
        for (int i=envelope.getDimension(); --i>=0;) {
            final double length = envelope.getLength(i);
            if (Double.isInfinite(length) || !(length > 0)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a hash code value for this object.
     */
    @Override
    public int hashCode() {
        int code = layer.getName().hashCode();
        if (bbox != null) {
            code += 37 * bbox.hashCode();
        }
        if (resolution != null) {
            code += 31 * resolution.hashCode();
        }
        return code;
    }

    /**
     * Compares this object with the specified ony for equality.
     */
    @Override
    public boolean equals(final Object object) {
        if (object instanceof LayerRequest) {
            final LayerRequest that = (LayerRequest) object;
            return Utilities.equals(this.layer.getName(), that.layer.getName()) &&
                   Utilities.equals(this.bbox,            that.bbox) &&
                   Utilities.equals(this.resolution,      that.resolution);
        }
        return false;
    }

    /**
     * Returns a string representation for debugging purpose.
     */
    @Override
    public String toString() {
        final String layer = String.valueOf(this.layer);
        if (bbox != null) {
            return layer + " inside " + bbox;
        } else {
            return layer;
        }
    }
}
