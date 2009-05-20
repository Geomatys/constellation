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

import java.util.Map;
import java.util.HashMap;
import java.sql.SQLException;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;

import org.opengis.coverage.grid.GridEnvelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.SingleCRS;
import org.opengis.referencing.crs.VerticalCRS;
import org.opengis.referencing.crs.TemporalCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.Matrix;

import org.geotoolkit.util.Utilities;
import org.geotoolkit.referencing.CRS;
import org.geotoolkit.util.XArrays;
import org.geotoolkit.coverage.grid.GeneralGridEnvelope;
import org.geotoolkit.coverage.grid.GeneralGridGeometry;
import org.geotoolkit.referencing.AbstractIdentifiedObject;
import org.geotoolkit.referencing.crs.DefaultGeographicCRS;
import org.geotoolkit.referencing.operation.matrix.MatrixFactory;
import org.geotoolkit.referencing.operation.transform.ProjectiveTransform;


/**
 * The horizontal, vertical and temporal components of a {@link GridGeometryEntry} CRS.
 * The {@linkplain CoordinateReferenceSystem coordinate reference systems} are built from
 * the SRID declared in the {@code "GridGeometries"} table, linked to the values declared
 * in the PostGIS {@code "spatial_ref_sys"} table.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class SpatialRefSysEntry {
    /**
     * The horizontal and vertical SRID declared in the database.
     */
    protected final int horizontalSRID, verticalSRID;

    /**
     * The horizontal CRS.
     *
     * @see #horizontalSRID
     */
    private SingleCRS horizontalCRS;

    /**
     * The vertical CRS, or {@code null} if none.
     *
     * @see #verticalSRID
     */
    private VerticalCRS verticalCRS;

    /**
     * The temporal CRS, or {@code null} if none.
     */
    private final TemporalCRS temporalCRS;

    /**
     * The {@link #crs} without the temporal component.
     */
    private CoordinateReferenceSystem timelessCRS;

    /**
     * The coordinate reference system made of the combinaison of all the above.
     */
    private CoordinateReferenceSystem crs;

    /**
     * The transform to the geographic CRS. Will be created when first needed.
     */
    private MathTransform2D toGeographicCRS;

    /**
     * Constructs a new entry for the given SRID.
     *
     * @param horizontalSRID The SRID of the horizontal CRS, or {@code 0} if none.
     * @param verticalSRID   The SRID of the vertical CRS, or {@code 0} if none.
     * @param temporalCRS    The temporal CRS, or {@code null} if none.
     */
    SpatialRefSysEntry(final int horizontalSRID, final int verticalSRID, final TemporalCRS temporalCRS) {
        this.horizontalSRID = horizontalSRID;
        this.verticalSRID   = verticalSRID;
        this.temporalCRS    = temporalCRS;
    }

    /**
     * Creates the horizontal and vertical CRS from the factory bundled in the given table.
     *
     * @param  table The table to use for creating a CRS from a SRID.
     * @throws SQLException if an error occured while reading the database.
     * @throws FactoryException if an error occured while creating the CRS.
     * @throws ClassCastException if the CRS is not of expected type.
     */
    final void createSingleCRS(final GridGeometryTable table, final boolean horizontal, final boolean vertical)
            throws SQLException, FactoryException, ClassCastException
    {
        if (horizontal && horizontalSRID != 0) {
            horizontalCRS = (SingleCRS) table.getSpatialReferenceSystem(horizontalSRID);
        }
        if (vertical && verticalSRID != 0) {
            verticalCRS = (VerticalCRS) table.getSpatialReferenceSystem(verticalSRID);
        }
    }

    /**
     * Creates the compound CRS from the single CRS created by {@link #createSingleCRS}.
     *
     * @param  factory The factory to use for creating the compound CRS.
     * @throws FactoryException if an error occured while creating the CRS.
     */
    final void createCompoundCRS(final CRSFactory factory) throws FactoryException {
        int count = 0;
        SingleCRS[] elements = new SingleCRS[3];
        if (horizontalCRS != null) elements[count++] = horizontalCRS;
        if (verticalCRS   != null) elements[count++] = verticalCRS;
        if (temporalCRS   != null) elements[count++] = temporalCRS;
        if (count == 0) {
            return;
        }
        crs = elements[0];
        if (count == 1) {
            if (crs != temporalCRS) {
                timelessCRS = crs;
            }
            return;
        }
        elements = XArrays.resize(elements, count);
        Map<String,?> properties = AbstractIdentifiedObject.getProperties(crs);
        if (verticalCRS != null) {
            String name = crs.getName().getCode();
            name = name + " + " + verticalCRS.getName().getCode();
            final Map<String,Object> copy = new HashMap<String,Object>(properties);
            copy.put(CoordinateReferenceSystem.NAME_KEY, name);
            properties = copy;
        }
        crs = factory.createCompoundCRS(properties, elements);
        if (temporalCRS == null) {
            timelessCRS = crs;
        } else {
            if (--count == 1) {
                timelessCRS = elements[0];
            } else {
                elements = XArrays.resize(elements, count);
                timelessCRS = factory.createCompoundCRS(properties, elements);
            }
        }
        assert CRS.getHorizontalCRS(crs) == CRS.getHorizontalCRS(timelessCRS);
        assert CRS.getVerticalCRS  (crs) == CRS.getVerticalCRS  (timelessCRS);
    }

    /**
     * Returns the coordinate reference system, which may be up to 4-dimensional.
     *
     * @param time {@code true} if the CRS should include the time component,
     *        or {@code false} for a spatial-only CRS.
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem(final boolean time) {
        return time ? crs : timelessCRS;
    }

    /**
     * Returns a grid geometry for the given horizontal size and transform, and the given vertical
     * ordinate values. The coefficients for the vertical axis assume that the vertical ordinates
     * are evenly spaced. This is not always true; a special processing will be performed later.
     * The time dimension, if any, is left to the identity transform.
     */
    public GeneralGridGeometry getGridGeometry(final Dimension size,
            final AffineTransform transform, final double[] altitudes)
    {
        /*
         * Creates the "grid to CRS" transform as a matrix. The coefficients for the vertical
         * axis assume that the vertical ordinates are evenly spaced. This is not always true;
         * a special processing will be performed by {@link GridCoverageEntry} for computing a
         * more accurate value.
         */
        final int dim = crs.getCoordinateSystem().getDimension();
        final int[] lower = new int[dim];
        final int[] upper = new int[dim];
        final Matrix gridToCRS = MatrixFactory.create(dim + 1);
        int verticalDim = 0;
        if (horizontalCRS != null) {
            copy(transform, gridToCRS);
            verticalDim = horizontalCRS.getCoordinateSystem().getDimension();
        }
        if (verticalCRS != null && altitudes != null) {
            int n = altitudes.length;
            if (n != 0) {
                upper[verticalDim] = n;
                final double offset = altitudes[0];
                final double scale = (--n == 0) ? 0 : (altitudes[n] - offset) / n;
                gridToCRS.setElement(verticalDim, verticalDim, scale); // May be negative.
                gridToCRS.setElement(verticalDim, dim, offset);
            }
        }
        upper[0] = size.width;
        upper[1] = size.height;
        final GridEnvelope gridRange = new GeneralGridEnvelope(lower, upper, false);
        return new GeneralGridGeometry(gridRange, GridGeometryIO.PIXEL_IN_CELL,
                ProjectiveTransform.create(gridToCRS), crs);
    }

    /**
     * Copies the affine transform coefficients into the two first dimensions of the affine
     * transform represented by the target matrix.
     */
    static void copy(final AffineTransform source, final Matrix target) {
        final int dim = target.getNumCol() - 1;
        target.setElement(0, 0,   source.getScaleX());
        target.setElement(1, 1,   source.getScaleY());
        target.setElement(0, 1,   source.getShearX());
        target.setElement(1, 0,   source.getShearY());
        target.setElement(0, dim, source.getTranslateX());
        target.setElement(1, dim, source.getTranslateY());
    }

    /**
     * Returns the transform to the geographic CRS.
     */
    public MathTransform2D getHorizontalToGeographicCRS() throws FactoryException {
        // No need to synhronize - this is not a big deal if the transform is searched twice.
        if (toGeographicCRS == null) {
            toGeographicCRS = (MathTransform2D) CRS.findMathTransform(horizontalCRS, DefaultGeographicCRS.WGS84);
        }
        return toGeographicCRS;
    }

    /**
     * Returns the dimension for the <var>z</var> axis.
     */
    final int zDimension() {
        return (verticalCRS == null) ? -1 : (horizontalCRS == null) ? 0 :
                horizontalCRS.getCoordinateSystem().getDimension();
    }

    /**
     * Returns a hash code value for this entry.  The value must be determined only from the
     * arguments given at construction time, i.e. it must be unchanged by call to any method
     * in this class.
     */
    @Override
    public int hashCode() {
        // 100003 is a prime number assumed large enough for avoiding overlapping between SRID.
        int code = horizontalSRID + 100003*verticalSRID;
        if (temporalCRS != null) {
            code ^= temporalCRS.hashCode();
        }
        return code;
    }

    /**
     * Compares this entry with the specified object for equality. The comparaison must include
     * only the arguments given at construction time.
     */
    @Override
    public boolean equals(final Object object) {
        if (object instanceof SpatialRefSysEntry) {
            final SpatialRefSysEntry that = (SpatialRefSysEntry) object;
            return this.horizontalSRID == that.horizontalSRID &&
                   this.verticalSRID   == that.verticalSRID   &&
                   Utilities.equals(this.temporalCRS, that.temporalCRS);
        }
        return false;
    }

    /**
     * Returns a string representation for debugging purpose.
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[h=" + horizontalSRID + ", v=" + verticalSRID + ']';
    }
}
