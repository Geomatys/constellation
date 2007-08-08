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

import net.sicade.catalog.Element;


/**
 * A series of coverages sharing common characteristics in a {@linkplain Layer layer}. A layer
 * often regroup all coverages in a single series, but in some cases a layer may contains more
 * than one series. For example a layer of <cite>Sea Surface Temperature</cite> (SST) from Nasa
 * <cite>Pathfinder</cite> can be subdivised in two series:
 * <p>
 * <ul>
 *   <li>Final release of historical data. Those data are often two years old.</li>
 *   <li>More recent but not yet definitive data.</li>
 * </ul>
 * <p>
 * In most cases it is suffisient to work with {@linkplain Layer layer} as a whole without the
 * need to go down to the {@code Series}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface Series extends Element {
    /**
     * Returns the layer which contains this series.
     *
     * @see Layer#getSeries
     */
    Layer getLayer();

    /**
     * Returns the format of all coverages in this series.
     */
    Format getFormat();
}
