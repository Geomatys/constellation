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
package net.seagis.coverage.model;

import java.util.Comparator;
import net.seagis.catalog.NumberedElement;
import net.seagis.catalog.CatalogException;
import net.seagis.coverage.catalog.Layer;
import net.seagis.coverage.catalog.GridCoverage;


/**
 * A descriptor of a phenomenon. In this package, a descriptor is a combinaison of:
 * <p>
 * <ul>
 *   <li>a {@linkplain Layer layer} of data (often from remote sensing);</li>
 *   <li>an {@linkplain Operation operation} applied on the data (for example a sobel operator)
 *       for computing gradient magnitudes);</li>
 *   <li>a {@linkplain RegionOfInterest region of interest} relative to a given position;</li>
 *   <li>a theorical {@linkplain Distribution distribution}.</li>
 * </ul>
 * <p>
 * Descriptors may be used as explanatory variables in {@linkplain LinearModel linear models}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
public interface Descriptor extends NumberedElement {
    /**
     * A comparator for sorting descriptors according their
     * {@linkplain RegionOfInterest#getDayOffset temporal offset}.
     */
    Comparator<Descriptor> TIME_ORDER = new Comparator<Descriptor>() {
        public int compare(final Descriptor d1, final Descriptor d2) {
            final double dt1 = d1.getRegionOfInterest().getDayOffset();
            final double dt2 = d2.getRegionOfInterest().getDayOffset();
            if (dt1 < dt2) return -1;
            if (dt1 > dt2) return +1;
            int c = d1.getLayer().getName().compareTo(d2.getLayer().getName());
            if (c != 0) {
                return c;
            }
            return d1.getName().compareTo(d2.getName());
        }
    };

    /**
     * Returns the layer data (often from remote sensing) for this descriptor.
     */
    Layer getLayer();

    /**
     * The band to select in the {@linkplain #getLayer layer}.
     */
    short getBand();

    /**
     * Returns the operation to apply of the data from the {@linkplain #getLayer layer}.
     * If no operation are applied, then this method returns the identity operation.
     */
    Operation getOperation();

    /**
     * Returns the region of interest relative to the locations to be
     * {@linkplain Coverage#evaluate(org.opengis.geometry.DirectPosition,double[]) evaluated}.
     */
    RegionOfInterest getRegionOfInterest();

    /**
     * Returns an approximative statical distribution of the values to be computed by this
     * descriptor. If the distribution is unknown, then this method returns {@code null}.
     */
    Distribution getDistribution();

    /**
     * Returns {@code true} if this descriptor returns 1 for every locations in its
     * {@linkplain #getCoverage coverage}. Such identity descriptor can be used in
     * expressions like <code>y = C0 + C1*x + C2*x² + ...</code> where {@code C0}
     * can be coded as <code>C0&times;identity</code>.
     */
    boolean isIdentity();

    /**
     * Returns a view of this descriptor as a coverage. The coverage can be evaluated at
     * (<var>x</var>,<var>y</var>,<var>z</var>,<var>t</var>) location, using interpolations
     * if needed. This method returns a more elaborated coverage than {@link Layer#getCoverage}:
     * <p>
     * <ul>
     *   <li>An {@linkplain #getOperation operation} may be applied on the coverage (for example
     *       a grandient magnitude calculation)</li>
     *   <li>An {@linkplain #getRegionOfInterest area of interest} may be used.</li>
     *   <li>In case of missing value, the {@linkplain Layer#getFallback fall back layer} is queried.</li>
     *   <li>Data may be evaluated on a remote machine without sending full image over the network.</li>
     * </ul>
     * 
     * @throws CatalogException if the coverage can not be obtained.
     */
    GridCoverage getCoverage() throws CatalogException;
}
