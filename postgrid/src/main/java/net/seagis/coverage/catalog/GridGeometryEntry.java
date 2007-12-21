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
package net.seagis.coverage.catalog;

import java.util.Arrays;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

import org.opengis.coverage.grid.GridRange;
import org.opengis.geometry.Envelope;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import org.geotools.referencing.CRS;
import org.geotools.resources.Utilities;
import org.geotools.resources.CRSUtilities;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.resources.geometry.XRectangle2D;

import net.seagis.catalog.Entry;


/**
 * Implementation of a three-dimensional grid geometry. This class assumes that the two first
 * axis are always for the horizontal component of the CRS (no matter if it is (x,y) or (y,x))
 * and that the vertical component, if any, is the third axis. Some of those assumptions are
 * checked by assertions at construction time.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class GridGeometryEntry extends Entry {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = -3529884841649813534L;

    /**
     * The immutable grid range, which may be 2D, 3D or 4D.
     */
    private final GridRange gridRange;

    /**
     * The "grid to CRS" affine transform for the horizontal part. The vertical
     * transform is not included because the {@link #verticalOrdinates} may not
     * be regular.
     */
    private final AffineTransform gridToCRS;

    /**
     * The full envelope, including the vertical and temporal extent if any.
     */
    private final GeneralEnvelope envelope;

    /**
     * Same as the envelope, but in WGS 84 geographic coordinates. This field is read
     * by {@link GridCoverageEntry} only. It should never be modified.
     */
    final XRectangle2D geographicEnvelope;

    /**
     * The vertical ordinates, or {@code null}.
     */
    private final double[] verticalOrdinates;

    /**
     * Creates an entry from the given geographic bounding box.
     * <strong>Note:</strong> This constructor do not clone any of its arguments.
     * Do not modify the arguments after construction.
     *
     * @param name      The identifier of this grid geometry.
     * @param gridToCRS The grid to CRS affine transform.
     * @param gridRange The image dimension. May be 2D or 3D.
     * @param envelope  The spatio-temporal envelope.
     * @param bbox      Same as the envelope, but as a geographic bounding box.
     * @param verticalOrdinates The vertical ordinate values, or {@code null} if none.
     */
    GridGeometryEntry(final String name, final AffineTransform gridToCRS,
                      final GridRange gridRange, final GeneralEnvelope envelope,
                      final GeographicBoundingBox bbox, final double[] verticalOrdinates)
    {
        super(name);
        this.gridToCRS         = gridToCRS;
        this.gridRange         = gridRange;
        this.envelope          = envelope;
        this.verticalOrdinates = verticalOrdinates;
        if (verticalOrdinates != null) {
            if (verticalOrdinates.length > Short.MAX_VALUE) {
                // See 'indexOf' for this limitation.
                throw new IllegalArgumentException();
            }
        }
        // Checks for assumptions - see class javadoc.
        final CoordinateReferenceSystem crs = envelope.getCoordinateReferenceSystem();
        if (crs == null) {
            throw new AssertionError(envelope);
        }
        try {
            assert CRS.getHorizontalCRS(crs) == CRSUtilities.getCRS2D(crs) : crs;
            assert (CRS.getVerticalCRS(crs) == null) == (verticalOrdinates == null) : crs;
        } catch (TransformException e) {
            throw new AssertionError(e);
        }
        geographicEnvelope = XRectangle2D.createFromExtremums(
                bbox.getWestBoundLongitude(), bbox.getSouthBoundLatitude(),
                bbox.getEastBoundLongitude(), bbox.getNorthBoundLatitude());
    }

    /**
     * Returns the affine transform from grid to
     * {@linkplain #getCoordinateReferenceSystem coordinate reference system}.
     */
    public AffineTransform getGridToCRS2D() {
        return (AffineTransform) gridToCRS.clone();
    }

    /**
     * Convenience method returning the two first dimension of the
     * {@linkplain #getGridRange grid range}.
     */
    public Dimension getSize() {
        return new Dimension(gridRange.getLength(0), gridRange.getLength(1));
    }

    /**
     * Convenience method returning the two first dimension of the
     * {@linkplain #getGridRange grid range}.
     */
    public Rectangle getBounds() {
        return new Rectangle(gridRange.getLower (0), gridRange.getLower (1),
                             gridRange.getLength(0), gridRange.getLength(1));
    }

    /**
     * Returns the grid range.
     */
    public GridRange getGridRange() {
        return gridRange;
    }

    /**
     * Returns the coordinate reference system.
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return envelope.getCoordinateReferenceSystem();
    }

    /**
     * Returns the envelope.
     */
    public Envelope getEnvelope() {
        return envelope.clone();
    }

    /**
     * Returns the envelope with altitude restricted to the specified band.
     *
     * @param The band number. Numbering start at 0.
     */
    final Envelope getEnvelope(final int band) {
        final GeneralEnvelope envelope = this.envelope.clone();
        if (verticalOrdinates != null && verticalOrdinates.length > 1) {
            final double z = verticalOrdinates[band];
            final int floor = Math.max(0, band - 1);
            final int ceil  = Math.min(band + 1, verticalOrdinates.length - 1);
            envelope.setRange(2, z - 0.5*Math.abs(verticalOrdinates[floor+1] - verticalOrdinates[floor]),
                                 z + 0.5*Math.abs(verticalOrdinates[ceil] - verticalOrdinates[ceil-1]));
        }
        return envelope;
    }

    /**
     * Returns the vertical ordinate values, or {@code null} if none.
     */
    public double[] getVerticalOrdinates() {
        return (verticalOrdinates != null) ? verticalOrdinates.clone() : null;
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
     * Returns {@code true} if this grid geometry is compatible with the specified ony for a
     * mosaic or pyramid. We do not checks the envelope since it will be TileCollection's job.
     */
    final boolean canMosaic(final GridGeometryEntry that) {
        return Arrays.equals(this.verticalOrdinates, that.verticalOrdinates) &&
            CRS.equalsIgnoreMetadata(this.envelope.getCoordinateReferenceSystem(),
                                     that.envelope.getCoordinateReferenceSystem());
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
            return Utilities.equals(this.gridToCRS,         that.gridToCRS) &&
                   Utilities.equals(this.gridRange,         that.gridRange) &&
                   Utilities.equals(this.envelope,          that.envelope)  &&
                   Arrays   .equals(this.verticalOrdinates, that.verticalOrdinates);
        }
        return false;
    }
}
