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

import java.awt.Dimension;
import org.opengis.geometry.Envelope;
import org.geotools.resources.Utilities;

/**
 * The request for a layer, including its envelope.
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
     * The bounding box, including the CRS.
     */
    private final Envelope envelope;

    /**
     * The size of target image.
     */
    private final Dimension size;

    /**
     * Crestes a {@code LayerRequest} for the given layer name, source envelope and target size.
     */
    public LayerRequest(final String layer, final Envelope envelope, final Dimension size) {
        this.layer    = layer;
        this.envelope = envelope;
        this.size     = size;
    }

    /**
     * Returns a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return layer.hashCode() + 37*envelope.hashCode() + 31*size.hashCode();
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
