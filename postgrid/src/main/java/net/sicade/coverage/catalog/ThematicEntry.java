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

import net.sicade.catalog.Entry;


/**
 * Implementation of a {@linkplain Thematic thematic} entry in the database.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class ThematicEntry extends Entry implements Thematic {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = 5494927909479524177L;

    /**
     * Construit un nouveau thème du nom spécifié.
     *
     * @param name Le nom du phénomène.
     */
    protected ThematicEntry(final String name) {
        super(name);
    }

    /**
     * Construit un nouveau thème du nom spécifié.
     *
     * @param name    Le nom du phénomène.
     * @param remarks Remarques s'appliquant à ce phénomène, ou {@code null}.
     */
    protected ThematicEntry(final String name, final String remarks) {
        super(name, remarks);
    }
}
