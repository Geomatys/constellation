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
package net.sicade.coverage.model;

import net.sicade.catalog.Element;


/**
 * Approximative statistical distribution of a {@linkplain Descriptor descriptor}. This distribution
 * is often selected <cite>a-priori</cite> from some theorical knowledge about the underlying data.
 * <p>
 * This interface is used for applying variable changes in order to get a distribution closer
 * to the <A HREF="http://mathworld.wolfram.com/NormalDistribution.html">normal distribution</A>
 * before usage in a {@linkplain LinearModel linear model}.
 * 
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
public interface Distribution extends Element {
    /**
     * A distribution for which {@link #normalize} is an identity operation.
     */
    public static Distribution NORMAL = new DistributionEntry("normale", 1, 0, false);

    /**
     * Applies a variable change, if needed. The returned values should be distributed at least approximatively
     * according a <A HREF="http://mathworld.wolfram.com/NormalDistribution.html">normal distribution</A>.
     *
     * @param  value The value to transform.
     * @return The transformed value. May be identical to {@code value} if this distribution is already normal.
     */
    double normalize(double value);

    /**
     * Returns {@code true} if {@link #normalize} do not perform any transformation.
     */
    boolean isIdentity();
}
