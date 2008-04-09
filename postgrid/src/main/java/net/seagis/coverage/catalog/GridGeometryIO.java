/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2008, Geomatys
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
import org.opengis.coverage.grid.GridRange;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridGeometry2D;


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
     * The origin or the region to be read.
     */
    private final int x,y;

    /**
     * Subsampling along <var>x</var> and <var>y</var> axis.
     */
    public final short sx, sy;

    /**
     * Creates a new grid geometry.
     */
    public GridGeometryIO(final Rectangle        sourceRegion,
                          final Dimension         subsampling,
                          final MathTransform       gridToCRS,
                          final CoordinateReferenceSystem crs)
    {
        super(getGridRange(gridToCRS.getSourceDimensions(), sourceRegion, subsampling),
                PixelInCell.CELL_CORNER, gridToCRS, crs, null);
        x  = sourceRegion.x;
        y  = sourceRegion.y;
        sx = (short) subsampling.width;
        sy = (short) subsampling.height;
        // Checks for overflow.
        if (sx != subsampling.width || sy != subsampling.height) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Workaround for RFE #4093999 ("Relax constraint on placement of this()/super()
     * call in constructors").
     */
    @SuppressWarnings("fallthrough")
    private static GridRange getGridRange(final int dimension,
            final Rectangle sourceRegion, final Dimension subsampling)
    {
        final int[] lower = new int[dimension];
        final int[] upper = new int[dimension];
        switch (dimension) {
            default: Arrays.fill(upper, 2, dimension, 1);                 // Fall through
            case 2:  upper[1] = sourceRegion.height / subsampling.width;  // Fall through
            case 1:  upper[0] = sourceRegion.width  / subsampling.height; // Fall through
            case 0:  break;
        }
        return new GeneralGridRange(lower, upper);
    }

    /**
     * Returns the source region to read.
     */
    public Rectangle getSourceRegion() {
        return new Rectangle(x, y,
                gridRange.getLength(gridDimensionX) * sx,
                gridRange.getLength(gridDimensionY) * sy);
    }
}
