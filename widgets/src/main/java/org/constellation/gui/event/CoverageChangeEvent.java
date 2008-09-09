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
package org.constellation.gui.event;

import java.util.EventObject;
import org.opengis.coverage.grid.GridCoverage;
import org.constellation.coverage.catalog.CoverageReference;


/**
 * Evénement représentant un changement d'image.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class CoverageChangeEvent extends EventObject {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = 7165162794865321696L;

    /**
     * Description de l'image qui vient de changer.
     */
    private final CoverageReference entry;

    /**
     * Image qui vient de changer, ou {@code null} s'il n'y en a pas.
     */
    private final GridCoverage coverage;

    /**
     * Construit un événement représentant un changement d'images.
     *
     * @param source   Source de cet événement.
     * @param entry    Description de l'image (peut être nulle).
     * @param coverage Nouvelle image (peut être nulle).
     */
    public CoverageChangeEvent(final Object           source,
                               final CoverageReference entry,
                               final GridCoverage   coverage)
    {
        super(source);
        this.entry    = entry;
        this.coverage = coverage;
    }

    /**
     * Retourne la référence décrivant l'image qui vient d'être lue. Si cette référence
     * n'est pas connue ou qu'il n'y en a pas, alors cette méthode retourne {@code null}.
     */
    public CoverageReference getCoverageReference() {
        return entry;
    }

    /**
     * Image qui vient de changer, ou {@code null} s'il n'y en a pas ou qu'elle n'est pas connue.
     */
    public GridCoverage getGridCoverage() {
        return coverage;
    }
}
