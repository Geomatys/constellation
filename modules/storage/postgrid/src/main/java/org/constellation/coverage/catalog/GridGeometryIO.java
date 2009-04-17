/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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
package org.constellation.coverage.catalog;

import java.util.Arrays;
import java.awt.Dimension;
import java.awt.Rectangle;
import org.opengis.coverage.grid.GridRange;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import org.geotoolkit.util.converter.Classes;
import org.geotoolkit.referencing.operation.matrix.MatrixFactory;
import org.geotoolkit.referencing.operation.transform.ProjectiveTransform;
import org.geotoolkit.referencing.operation.transform.ConcatenatedTransform;
import org.geotoolkit.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotoolkit.coverage.grid.InvalidGridGeometryException;


/**
 * A grid geometry with information about the source region to be read and
 * the subsampling to apply.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class GridGeometryIO extends GridGeometry2D {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = -3557138213154730285L;

    /**
     * Whatever default grid range computation should be performed on transform
     * relative to pixel center or relative to pixel corner.  The former is OGC
     * convention while the later is Java convention.
     */
    static final PixelInCell PIXEL_IN_CELL = PixelInCell.CELL_CORNER;

    /**
     * The origin or the region to be read.
     */
    private final int x,y;

    /**
     * Subsampling along <var>x</var> and <var>y</var> axis.
     */
    public final short sx, sy;

    /**
     * Creates a new grid geometry.
     *
     * @todo Consider rounding the matrix coefficients when close to an integer.
     *       See the BlueMarble test case in MosaicTest.
     */
    public GridGeometryIO(final Rectangle        sourceRegion,
                          final Dimension         subsampling,
                          final Matrix              gridToCRS,
                          final CoordinateReferenceSystem crs)
    {
        this(sourceRegion, subsampling, ProjectiveTransform.create(gridToCRS), crs);
    }

    /**
     * Creates a new grid geometry.
     */
    private GridGeometryIO(final Rectangle        sourceRegion,
                           final Dimension         subsampling,
                           final MathTransform       gridToCRS,
                           final CoordinateReferenceSystem crs)
    {
        super(createGridRange(gridToCRS.getSourceDimensions(), sourceRegion, subsampling),
                PIXEL_IN_CELL, gridToCRS, crs, null);
        x  = sourceRegion.x;
        y  = sourceRegion.y;
        sx = (short) subsampling.width;
        sy = (short) subsampling.height;
        if (sx != subsampling.width || sy != subsampling.height) {  // Checks for overflow.
            throw new IllegalArgumentException();
        }
        if (gridDimensionX != 0 || gridDimensionY != 1) {  // getGridRange(...) is assuming that.
            throw new InvalidGridGeometryException();
        }
    }

    /**
     * Workaround for RFE #4093999 ("Relax constraint on placement of this()/super()
     * call in constructors").
     */
    @SuppressWarnings("fallthrough")
    private static GridRange createGridRange(final int dimension,
            final Rectangle sourceRegion, final Dimension subsampling)
    {
        final int[] lower = new int[dimension];
        final int[] upper = new int[dimension];
        switch (dimension) {
            default: Arrays.fill(upper, 2, dimension, 1);                 // Fall through
            case 2:  upper[1] = sourceRegion.height / subsampling.height; // Fall through
            case 1:  upper[0] = sourceRegion.width  / subsampling.width;  // Fall through
            case 0:  break;
        }
        return new GeneralGridRange(lower, upper);
    }

    /**
     * Returns the source region to read.
     */
    public Rectangle getSourceRegion() {
        return new Rectangle(x, y,
                gridRange.getSpan(gridDimensionX) * sx,
                gridRange.getSpan(gridDimensionY) * sy);
    }

    /**
     * Returns the subsampling to use when reading the source region.
     */
    public Dimension getSubsampling() {
        return new Dimension(sx, sy);
    }

    /**
     * Returns a new grid geometry with a {@link #gridToCRS} transform adjusted for the
     * given subsampling. If the given subsampling is identical to the one in the current
     * object, then {@code this} is returned.
     * <p>
     * This method is necessary when an image has been read from the mosaic image reader,
     * which may use a different subsampling than the selected one.
     */
    public GridGeometry2D scaleForSubsampling(final Dimension subsampling) {
        if (subsampling.width == sx && subsampling.height == sy) {
            return this;
        }
        final Matrix matrix = MatrixFactory.create(gridToCRS.getTargetDimensions() + 1,
                                                   gridToCRS.getSourceDimensions() + 1);
        matrix.setElement(gridDimensionX, gridDimensionX, subsampling.getWidth()  / sx);
        matrix.setElement(gridDimensionY, gridDimensionY, subsampling.getHeight() / sy);
        MathTransform gridToCRS = ProjectiveTransform.create(matrix);
        gridToCRS = ConcatenatedTransform.create(gridToCRS, this.gridToCRS);
        return new GridGeometry2D(null, PIXEL_IN_CELL, gridToCRS, getCoordinateReferenceSystem(), null);
    }

    /**
     * Returns a string representation for debugging purpose.
     */
    @Override
    public String toString() {
        String sourceRegion = getSourceRegion().toString();
        sourceRegion = sourceRegion.substring(sourceRegion.indexOf('['));
        return Classes.getShortClassName(this) + "[sourceRegion" + sourceRegion +
                ", subsampling[" + sx + ',' + sy + "], " + gridRange + ", " + gridToCRS + ']';
    }
}
