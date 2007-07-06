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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sicade.observation.coverage.sql;

import net.sicade.observation.Procedure;          // Pour javadoc
import net.sicade.observation.coverage.Thematic;  // Pour javadoc
import net.sicade.observation.coverage.Series;    // Pour javadoc
import net.sicade.observation.coverage.Format;    // Pour javadoc
import org.opengis.coverage.SampleDimension;      // Pour javadoc
import org.geotools.coverage.Category;            // Pour javadoc


/**
 * Indique la profondeur de l'arborescence attendue de {@link SeriesTree#getTree}. Les arborescences
 * peuvent contenir des chemins de la forme "{@linkplain Thematic thématique}/{@linkplain Procedure
 * procédure}/{@linkplain Series série}/{@linkplain Format format}/{@linkplain SampleDimension
 * bande}/{@linkplain Category catégorie}".
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public enum TreeDepth {
    /**
     * Indique que l'arborescence ne doit pas aller plus loin que les {@linkplain Thematic thèmes}.
     */
    THEMATIC(SeriesTree.THEMATIC),

    /**
     * Indique que l'arborescence ne doit pas aller plus loin que les {@linkplain Procedure procédures}.
     */
    PROCEDURE(SeriesTree.PROCEDURE),

    /**
     * Indique que l'arborescence ne doit pas aller plus loin que les {@linkplain Series séries}.
     */
    SERIES(SeriesTree.SERIES),

    /**
     * Indique que l'arborescence ne doit pas aller plus loin que les sous-séries (après les séries).
     */
    SUBSERIES(SeriesTree.SUBSERIES),

    /**
     * Indique que l'arborescence ne doit pas aller plus loin que les {@linkplain Format formats}.
     */
    FORMAT(SeriesTree.FORMAT),

    /**
     * Indique que l'arborescence doit aller jusqu'aux catégories (après les formats).
     */
    CATEGORY(SeriesTree.FORMAT + 2);

    /**
     * La profondeur de l'arborescence, comptée comme le nombre de noeuds à traverser jusqu'à la
     * feuille (feuille inclue).
     */
    final int rank;

    /**
     * Construit une nouvelle énumération avec la profondeur d'arborescence spécifiée. Cette
     * profondeur est comptée comme le nombre de noeuds à traverser jusqu'à la feuille
     * (feuille inclue).
     */
    private TreeDepth(final int rank) {
        this.rank = rank;
    }
}
