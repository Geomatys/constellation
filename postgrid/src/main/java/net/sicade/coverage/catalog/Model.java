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
import net.sicade.catalog.Element;
import org.opengis.coverage.Coverage;


/**
 * Base interface for numerical models, which may or may not be {@linkplain LinearModel linear}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface Model extends Element {
    /**
     * Returns the layer which represents the results of this numerical model.
     */
    Layer getTarget();

    /**
     * Returns all descriptors used as input for this numerical model. The descriptor at
     * index <var>i</var> in this list determines the value at the same index <var>i</var>
     * in the {@code values} array given to the {@link #normalize} and {@link #evaluate}
     * methods, as in the following pseudo-code:
     *
     * <blockquote><pre>
     * values[i] = descriptors.get(i).getCoverage().evaluate(position);
     * </pre></blockquote>
     */
    List<Descriptor> getDescriptors();

    /**
     * {@linkplain Distribution#normalize Normalize} all values in the specified array. It is user
     * responsability to invoke this method exactly once before invoking {@link #evaluate evaluate}.
     * The normalization is performed in-place, i.e. the normalized values replace the specified
     * values.
     */
    void normalize(double[] values);

    /**
     * Computes a {@linkplain #getTarget target} value from the given {@linkplain #getDescriptors
     * descriptor} values.
     */
    double evaluate(double[] values);

    /**
     * Returns a view of this numerical model as a coverage.
     *
     * @throws CatalogException if the coverage can not be created.
     */
    Coverage asCoverage() throws CatalogException;
}
