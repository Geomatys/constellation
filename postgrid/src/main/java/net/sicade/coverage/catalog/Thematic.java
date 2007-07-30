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
 * A thematic (or <cite>phenomenon</cite>) for a set of {@linkplain Layer layers}. Examples are
 * are <cite>Sea Surface Temperature</cite> (SST) or <cite>Chlorophylle-a concentration</cite>.
 * This is slightly more generic than a {@linkplain Layer layer}, since the later is often about
 * a phenomenon measured by a given sensor, for example SST measured by NOAA.
 *
 * @version $Id$
 * @author Antoine Hnawia
 */
public interface Thematic extends Element {
}
