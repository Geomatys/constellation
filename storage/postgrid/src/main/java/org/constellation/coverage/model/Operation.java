/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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
package org.constellation.coverage.model;

import org.constellation.catalog.Element;
import org.opengis.coverage.Coverage;


/**
 * An operation applied on coverage from a {@linkplain Layer layer}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
public interface Operation extends Element {
    /**
     * The default operation. This is not exactly an identity operation since it applies interpolations
     * on pixel values. But from the point of view of numerical models, we can consider this instance
     * as the identity operation.
     */
    Operation DEFAULT = new OperationEntry();

    /**
     * Returns the prefix to use in composite names. For example the prefix for the gradient
     * magnitude is typically <code>"&nabla;"</code>. It can be applied on abbreviation like
     * {@code "SST"} (<cite>Sea Surface Temperature</cite>) in order to produce a new
     * abbreviation: <code>"&nabla;SST"</code>.
     */
    String getPrefix();

    /**
     * Applies the operation on a coverage.
     *
     * @param  coverage The coverage to apply the operation on.
     * @return A new coverage resulting from the application of this operation on the specified coverage.
     */
    Coverage doOperation(Coverage coverage);




    /**
     * An operation delegating its work to an other instance of {@link Operation}.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    public static class Proxy extends org.constellation.catalog.Proxy implements Operation {
        /**
         * For cross-version compatibility.
         */
        private static final long serialVersionUID = -2285791043646792332L;

        /**
         * The wrapped operation.
         */
        protected final Operation wrapped;

        /**
         * Creates a new operation wrapping the specified one.
         */
        protected Proxy(final Operation wrapped) {
            this.wrapped = wrapped;
        }

        /**
         * Returns the wrapped operation.
         */
        public Operation getBackingElement() {
            return wrapped;
        }

        /**
         * Returns the prefix to use in composite names. The default implementation delegates to
         * the {@linkplain #getBackingElement backing element}.
         */
        public String getPrefix() {
            return wrapped.getPrefix();
        }

        /**
         * Applies the operation. The default implementation delegates to
         * the {@linkplain #getBackingElement backing element}.
         */
        public Coverage doOperation(final Coverage coverage) {
            return wrapped.doOperation(coverage);
        }
    }
}
