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
package net.sicade.coverage.catalog;

import java.util.List;
import net.sicade.catalog.CatalogException;

import org.opengis.coverage.Coverage;
import org.opengis.util.InternationalString;
import org.opengis.geometry.DirectPosition;


/**
 * A coverage in which values are located on a grid. This interface provides a {@link #snap}
 * method returning the location of the nearest point on the grid.
 *
 * @author Martin Desruisseaux
 * @version $Id$
 */
public interface GridCoverage extends Coverage {
    /**
     * Returns the name of this coverage.
     */
    InternationalString getName();

    /**
     * Locates the voxel containing the specified coordinate, and returns the location of its center.
     * This method can be see as "rounding" the direct position to the nearest grid point.
     * <p>
     * Invoking <code>{@linkplain #evaluate(DirectPosition,double[]) evaluate}(snap(position))</code>
     * should returns a value without interpolation.
     *
     * @param  position The position to align on the grid.
     * @reutrn The nearest grid point.
     * @throws CatalogException if an error occured while querying the database.
     */
    DirectPosition snap(DirectPosition position) throws CatalogException;

    /**
     * Returns the coverages used as building blocks by the {@code evaluate} methods. Some
     * 3 or 4 dimensional grid coverages are implemented as a stack of other coverages with
     * less dimensions (typically a stack of two-dimensional coverages). This method returns
     * those coverages, if any.
     *
     * @param  The position where to evaluate a value.
     * @return The coverages used for evaluating a value at the specified position.
     * @throws CatalogException if an error occured while querying the database.
     */
    List<Coverage> coveragesAt(DirectPosition position) throws CatalogException;
}
