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

import org.geotools.resources.Utilities;
import org.geotools.resources.geometry.XDimension2D;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;

/**
 * The request for a layer, including its envelope. To be used in a hash map only.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class LayerRequest {
    /**
     * The layer name.
     */
    private final String layer;

    /**
     * The geographic bounding box. Will be computed when first needed.
     */
    private transient GeographicBoundingBox bbox;

    /**
     * The bounding box, including the CRS.
     */
    private final Envelope envelope;

    /**
     * The size of target image.
     */
    private final GridRange size;

    /**
     * Crestes a {@code LayerRequest} for the given layer name, source envelope and target size.
     */
    public LayerRequest(final String layer, final Envelope envelope, final GridRange size) {
        this.layer    = layer;
        this.envelope = envelope;
        this.size     = size;
    }

    /**
     * Returns the envelope as a geographic bounding box, or {@code null} if none.
     * The returned box is not cloned - <strong>do not modify</strong>.
     */
    public GeographicBoundingBox getGeographicBoundingBox() {
        if (bbox == null) {
            if (envelope == null) {
                return null;
            } else try {
                bbox = new GeographicBoundingBoxImpl(envelope);
            } catch (TransformException exception) {
                // Can't transform. Returns 'null', which is legal and means
                // no geographic bounding box selection (return the full image).
            }
        }
        return bbox;
    }

    /**
     * Returns the desired resolution in geographic coordinates, or {@code null} for best
     * resolution.
     *
     * @todo Revisit if it is really appropriate to assumes that x and y axis are at 0 and 1
     *       index in the grid range.
     */
    public Dimension2D getResolution() {
        if (size != null) {
            final GeographicBoundingBox bbox = getGeographicBoundingBox();
            if (bbox != null) {
                final int width  = size.getLength(0);
                final int height = size.getLength(1);
                return new XDimension2D.Double(
                        (bbox.getEastBoundLongitude() - bbox.getWestBoundLongitude()) / width,
                        (bbox.getNorthBoundLatitude() - bbox.getSouthBoundLatitude()) / height);
            }
        }
        return null;
    }

    /**
     * Returns a hash code value for this object.
     */
    @Override
    public int hashCode() {
        int code = layer.hashCode();
        if (envelope != null) {
            code += 37 * envelope.hashCode();
        }
        if (size != null) {
            code += 31 * size.hashCode();
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
            return Utilities.equals(this.layer,    that.layer)    &&
                   Utilities.equals(this.envelope, that.envelope) &&
                   Utilities.equals(this.size,     that.size);
        }
        return false;
    }

    /**
     * Returns a string representation for debugging purpose.
     */
    @Override
    public String toString() {
        return layer;
    }
}
