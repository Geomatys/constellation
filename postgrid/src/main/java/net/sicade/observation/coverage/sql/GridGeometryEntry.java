/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
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
package net.sicade.observation.coverage.sql;

import java.awt.Dimension;
import org.opengis.coverage.grid.GridRange;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.resources.Utilities;
import net.sicade.observation.sql.Entry;
import net.sicade.observation.sql.CRS;


/**
 * Implementation of a three-dimensionan grid geometry.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class GridGeometryEntry extends Entry {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = -3529884841649813534L;

    /**
     * The grid range.
     */
    private final GridRange gridRange;

    /**
     * The envelope.
     */
    private final GeneralEnvelope envelope;

    /**
     * The vertical extent, or {@code null}.
     */
    private final double[] verticalOrdinates;

    /**
     * Creates an entry from the given geographic bounding box.
     *
     * @param name The identifier of this grid geometry.
     * @param bbox The envelope in geographic coordinates.
     * @param size The image size.
     * @param verticalOrdinates The vertical ordinate values, or {@code null} if none.
     *        <strong>Note:</strong> This array is not cloned; do not modify after construction.
     */
    protected GridGeometryEntry(final String name, final GeographicBoundingBox bbox,
            final Dimension dimension, final double[] verticalOrdinates)
    {
        super(name, null);
        this.verticalOrdinates = verticalOrdinates;
        final int[] size;
        if (verticalOrdinates != null) {
            if (verticalOrdinates.length > Short.MAX_VALUE) {
                // See 'indexOf' for this limitation.
                throw new IllegalArgumentException();
            }
            envelope = new GeneralEnvelope(CRS.XYZT.getCoordinateReferenceSystem());
            double min = Double.POSITIVE_INFINITY;
            double max = Double.NEGATIVE_INFINITY;
            for (int i=0; i<verticalOrdinates.length; i++) {
                final double z = verticalOrdinates[i];
                if (z < min) min = z;
                if (z > max) max = z;
            }
            if (min < max) {
                envelope.setRange(2, min, max);
            }
            size = new int[4];
            size[2] = verticalOrdinates.length;
            size[3] = 1; // The time
        } else {
            envelope = new GeneralEnvelope(CRS.XYT.getCoordinateReferenceSystem());
            size = new int[3];
            size[2] = 1; // The time
        }
        envelope.setRange(0, bbox.getWestBoundLongitude(), bbox.getEastBoundLongitude());
        envelope.setRange(1, bbox.getSouthBoundLatitude(), bbox.getNorthBoundLatitude());
        size[0] = dimension.width;
        size[1] = dimension.height;
        gridRange = new GeneralGridRange(new int[size.length], size);
    }

    /**
     * Returns the grid range.
     */
    public GridRange getGridRange() {
        return gridRange;
    }

    /**
     * Returns the envelope.
     *
     * @todo Time is not set in this envelope.
     */
    public Envelope getEnvelope() {
        return (Envelope) envelope.clone();
    }

    /**
     * Returns the vertical ordinate values, or {@code null} if none.
     */
    public double[] getVerticalOrdinates() {
        return (verticalOrdinates != null) ? (double[]) verticalOrdinates.clone() : null;
    }

    /**
     * Returns the index of the closest altitude. If this entry contains no altitude, or
     * if the specified <var>z</var> is not a finite number, then this method returns 0.
     */
    final short indexOf(final double z) {
        short index = 0;
        if (!Double.isNaN(z) && !Double.isInfinite(z)) {
            double delta = Double.POSITIVE_INFINITY;
            if (verticalOrdinates != null) {
                for (int i=0; i<verticalOrdinates.length; i++) {
                    final double d = Math.abs(verticalOrdinates[i] - z);
                    if (d < delta) {
                        delta = d;
                        index = (short) i; // Array length has been checked at construction time.
                    }
                }
            }
        }
        return index;
    }

    /**
     * Compares this grid geometry with the specified object for equality.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final GridGeometryEntry that = (GridGeometryEntry) object;
            return Utilities.equals(this.gridRange,         that.gridRange) &&
                   Utilities.equals(this.envelope,          that.envelope)  &&
                   Utilities.equals(this.verticalOrdinates, that.verticalOrdinates);
        }
        return false;
    }
}
