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
package net.sicade.observation.coverage.sql;

import net.sicade.observation.Procedure;          // Pour javadoc
import net.sicade.observation.coverage.Thematic;  // Pour javadoc
import net.sicade.observation.coverage.Series;    // Pour javadoc
import net.sicade.observation.coverage.Format;    // Pour javadoc
import org.opengis.coverage.SampleDimension;      // Pour javadoc
import org.geotools.coverage.Category;            // Pour javadoc


/**
 * Indique la profondeur de l'arborescence attendue de {@link SeriesTree#getTree}. Les arborescences
 * peuvent contenir des chemins de la forme "{@linkplain Thematic th�matique}/{@linkplain Procedure
 * proc�dure}/{@linkplain Series s�rie}/{@linkplain Format format}/{@linkplain SampleDimension
 * bande}/{@linkplain Category cat�gorie}".
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public enum TreeDepth {
    /**
     * Indique que l'arborescence ne doit pas aller plus loin que les {@linkplain Thematic th�mes}.
     */
    THEMATIC(SeriesTree.THEMATIC),

    /**
     * Indique que l'arborescence ne doit pas aller plus loin que les {@linkplain Procedure proc�dures}.
     */
    PROCEDURE(SeriesTree.PROCEDURE),

    /**
     * Indique que l'arborescence ne doit pas aller plus loin que les {@linkplain Series s�ries}.
     */
    SERIES(SeriesTree.SERIES),

    /**
     * Indique que l'arborescence ne doit pas aller plus loin que les sous-s�ries (apr�s les s�ries).
     */
    SUBSERIES(SeriesTree.SUBSERIES),

    /**
     * Indique que l'arborescence ne doit pas aller plus loin que les {@linkplain Format formats}.
     */
    FORMAT(SeriesTree.FORMAT),

    /**
     * Indique que l'arborescence doit aller jusqu'aux cat�gories (apr�s les formats).
     */
    CATEGORY(SeriesTree.FORMAT + 2);

    /**
     * La profondeur de l'arborescence, compt�e comme le nombre de noeuds � traverser jusqu'� la
     * feuille (feuille inclue).
     */
    final int rank;

    /**
     * Construit une nouvelle �num�ration avec la profondeur d'arborescence sp�cifi�e. Cette
     * profondeur est compt�e comme le nombre de noeuds � traverser jusqu'� la feuille
     * (feuille inclue).
     */
    private TreeDepth(final int rank) {
        this.rank = rank;
    }
}
