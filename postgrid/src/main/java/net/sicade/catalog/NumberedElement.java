/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 * (C) 2007, Geomatys
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
package net.sicade.catalog;


/**
 * A catalog elements identified by a {@linkplain #getNumericIdentifier numeric identifier} in
 * addition of the {@linkplain #getName name}. The numeric identifier is redundant with the
 * element name, since the later should also be unique. However numeric identifiers are more
 * efficient than textual names when used as a foreigner key in a large table.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface NumberedElement extends Element {
    /**
     * Returns a unique numeric identifiers for this element.
     */
    int getNumericIdentifier();
}
