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
import java.awt.Shape;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

import org.opengis.coverage.grid.GridRange;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.operation.MathTransform2D;

import org.geotools.referencing.CRS;
import org.geotools.resources.Utilities;
import org.geotools.resources.CRSUtilities;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;
import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.geotools.referencing.crs.DefaultGeographicCRS;

import net.seagis.catalog.Entry;


/**
 * Implementation of a three-dimensional grid geometry. This class assumes that the two first
 * axis are always for the horizontal component of the CRS (no matter if it is (x,y) or (y,x))
 * and that the vertical component, if any, is the third axis. Some of those assumptions are
 * checked by assertions at construction time.
 * <p>
 * This implementation allows direct access to the field for convenience and efficiency, but
 * those fields should never be modified. We allow this unsafe practice because this class
 * is not public.
 *
 * @todo Actually current implementation may contains a 4D grid range and envelope, with a time
 *       axis. But the information provided in the time axis is invalid. We need to clarify the
 *       role of this class regarding time, and possibly make it purely spatial 3D.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Sam Hiatt
 */
final class GridGeometryEntry extends Entry {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = -3529884841649813534L;

    /**
     * The immutable grid range, which may be 2D, 3D or 4D.
     */
    protected final GridRange gridRange;

    /**
     * The "grid to CRS" affine transform for the horizontal part. The vertical
     * transform is not included because the {@link #verticalOrdinates} may not
     * be regular.
     */
    protected final AffineTransform2D gridToCRS;

    /**
     * The full envelope, including the vertical and temporal extent if any. The coordinate
     * reference system is the one declared in the {@link GridCoverageTable} for that entry.
     * Should not be modified after construction.
     */
    private final GeneralEnvelope envelope;

    /**
     * A shape describing the coverage outline in WGS 84 geographic coordinates. This is the
     * value computed by GeoTools, not the PostGIS object declared in the database, in order
     * to make sure that coordinate transformations are applied using the same algorithm (the
     * GeoTools one as opposed to the Proj4 algorithms used by PostGIS). This is necessary in
     * case the {@code "spatial_ref_sys"} table content is inconsistent with the EPSG database
     * used by GeoTools.
     */
    private final Shape geographicBoundingShape;

    /**
     * The horizontal and vertical SRID declared in the database.
     * Stored for informative purpose, but not used by this entry.
     */
    protected final int horizontalSRID, verticalSRID;

    /**
     * The vertical ordinates, or {@code null}.
     */
    private final double[] verticalOrdinates;

    /**
     * Creates an entry from the given grid range and <cite>grid to CRS</cite> transform.
     * <strong>Note:</strong> This constructor do not clone any of its arguments.
     * Do not modify the arguments after construction.
     *
     * @param name      The identifier of this grid geometry.
     * @param gridToCRS The grid to CRS affine transform.
     * @param gridRange The image dimension. May be 2D or 3D.
     * @param envelope  The spatio-temporal envelope.
     * @param verticalOrdinates The vertical ordinate values, or {@code null} if none.
     */
    GridGeometryEntry(final String            name,
                      final AffineTransform2D gridToCRS,
                      final GridRange         gridRange,
                      final GeneralEnvelope   envelope,
                      final int               horizontalSRID,
                      final int               verticalSRID,
                      final double[]          verticalOrdinates)
            throws FactoryException, TransformException
    {
        super(name);
        this.gridToCRS         = gridToCRS;
        this.gridRange         = gridRange;
        this.envelope          = envelope;
        this.horizontalSRID    = horizontalSRID;
        this.verticalSRID      = verticalSRID;
        this.verticalOrdinates = verticalOrdinates;
        if (verticalOrdinates != null) {
            if (verticalOrdinates.length > Short.MAX_VALUE) {
                throw new IllegalArgumentException(); // See 'indexOf' for this limitation.
            }
            assert gridRange.getLength(2) == verticalOrdinates.length : gridRange;
        }
        CoordinateReferenceSystem crs = envelope.getCoordinateReferenceSystem();
        /*
         * Checks for assumptions - see class javadoc.
         */
        if (crs == null) {
            throw new AssertionError(envelope);
        }
        try {
            assert CRS.getHorizontalCRS(crs) == CRSUtilities.getCRS2D(crs) : crs;
            assert (CRS.getVerticalCRS(crs) == null) == (verticalOrdinates == null) : crs;
        } catch (TransformException e) {
            throw new AssertionError(e);
        }
        /*
         * Computes the coverage geographic shape.
         */
        crs = CRS.getHorizontalCRS(crs);
        final MathTransform2D tr = (MathTransform2D) CRS.findMathTransform(crs, DefaultGeographicCRS.WGS84);
        geographicBoundingShape = tr.createTransformedShape(getShape());
    }

    /**
     * Returns {@code true} if the geographic bounding box described by this entry is empty.
     */
    public boolean isEmpty() {
        RectangularShape bounds;
        if (geographicBoundingShape instanceof RectangularShape) {
            bounds = (RectangularShape) geographicBoundingShape;
        } else {
            bounds = geographicBoundingShape.getBounds2D();
        }
        return bounds.isEmpty();
    }

    /**
     * Returns a copy of the geographic bounding box. This copy can be freely modified.
     */
    public GeographicBoundingBoxImpl getGeographicBoundingBox() {
        Rectangle2D bounds;
        if (geographicBoundingShape instanceof Rectangle2D) {
            bounds = (Rectangle2D) geographicBoundingShape;
        } else {
            bounds = geographicBoundingShape.getBounds2D();
        }
        return new GeographicBoundingBoxImpl(bounds);
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
     * Returns the coverage shape in coverage CRS (not geographic CRS). The returned shape is likely
     * (but not garanteed) to be an instance of {@link Rectangle2D}. It can be freely modified.
     */
    public Shape getShape() {
        Shape shape = new Rectangle2D.Double(
                gridRange.getLower (0), gridRange.getLower (1),
                gridRange.getLength(0), gridRange.getLength(1));
        shape = AffineTransform2D.transform(gridToCRS, shape, true);
        return shape;
    }

    /**
     * Returns the coordinate reference system.
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return envelope.getCoordinateReferenceSystem();
    }

    /**
     * Returns the a copy of the envelope.
     */
    public GeneralEnvelope getEnvelope() {
        return envelope.clone();
    }

    /**
     * Returns the envelope with altitude restricted to the specified band. Altitudes are stored
     * in the database as an array. This method replace the altitude range, which was previously
     * from the minimum to the maximum value declared in the array, to the value at one specific
     * element of the altitude array.
     *
     * @param The band number, in the range 0 inclusive to
     *        <code>{@linkplain #getVerticalOrdinates}.length</code> exclusive.
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
     * Returns the vertical ordinate values, or {@code null} if none. If non-null,
     * then the array length must be equals to the {@code gridRange.getLength(2)}.
     */
    public double[] getVerticalOrdinates() {
        if (verticalOrdinates != null) {
            assert gridRange.getLength(2) == verticalOrdinates.length : gridRange;
            return verticalOrdinates.clone();
        }
        return null;
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
     * Returns {@code true} if the specified entry has the same envelope than this entry,
     * regardless the grid size.
     */
    final boolean sameEnvelope(final GridGeometryEntry that) {
        return Utilities.equals(this.envelope,          that.envelope) &&
                  Arrays.equals(this.verticalOrdinates, that.verticalOrdinates);
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
                      Arrays.equals(this.verticalOrdinates, that.verticalOrdinates);
        }
        return false;
    }
}
