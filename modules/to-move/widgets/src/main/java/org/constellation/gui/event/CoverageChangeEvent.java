/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.gui.event;

import org.constellation.coverage.catalog.CoverageReference;
import org.opengis.coverage.grid.GridCoverage;

import java.util.EventObject;


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
