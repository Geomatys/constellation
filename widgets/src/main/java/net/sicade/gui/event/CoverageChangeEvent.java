/*
 * Sicade - Syst�mes int�gr�s de connaissances pour l'aide � la d�cision en environnement
 * (C) 2005, Institut de Recherche pour le D�veloppement
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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sicade.gui.event;

import java.util.EventObject;
import org.opengis.coverage.grid.GridCoverage;
import net.sicade.observation.coverage.CoverageReference;


/**
 * Ev�nement repr�sentant un changement d'image.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class CoverageChangeEvent extends EventObject {
    /**
     * Pour compatibilit�s entre les enregistrements binaires de diff�rentes versions.
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
     * Construit un �v�nement repr�sentant un changement d'images.
     *
     * @param source   Source de cet �v�nement.
     * @param entry    Description de l'image (peut �tre nulle).
     * @param coverage Nouvelle image (peut �tre nulle).
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
     * Retourne la r�f�rence d�crivant l'image qui vient d'�tre lue. Si cette r�f�rence
     * n'est pas connue ou qu'il n'y en a pas, alors cette m�thode retourne {@code null}.
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
