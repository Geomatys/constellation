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
package net.sicade.observation;

import java.util.Date;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import net.sicade.util.DateRange;


/**
 * Un élément à une certaine position spatio-temporelle. Cette position est généralement ponctuelle,
 * mais il peut s'agir aussi d'une trajectoire. Par exemple il peut s'agir de la position d'une
 * pêche à la senne, ou la forme geométrique représentant la disposition d'une ligne de palangre.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface LocatedElement extends Element {
    /**
     * Retourne une chaîne de caractères décrivant la position de cet élément.
     * Cette chaîne de caractères peut être utilisée comme un identifiant plus
     * détaillé que le {@linkplain #getName nom} à des fins d'interfaces utilisateurs.
     *
     * @throws CatalogException si l'interrogation du catalogue a échoué.
     */
    String getLocation() throws CatalogException;

    /**
     * Retourne une coordonnée représentative de cet élément, en degrés de longitude et de latitude.
     * Cette méthode peut retourner {@code null} si aucune coordonnées représentative n'est trouvée.
     *
     * @throws CatalogException si l'interrogation du catalogue a échoué.
     */
    Point2D getCoordinate() throws CatalogException;

    /**
     * Retourne une date représentative de cet élément. Dans le cas des observations qui
     * s'étendent sur une certaine période de temps, ça pourrait être par exemple la date
     * du milieu. Cette méthode peut retourner {@code null} si aucune date n'est associée
     * à cet élément.
     *
     * @throws CatalogException si l'interrogation du catalogue a échoué.
     */
    Date getTime() throws CatalogException;

    /**
     * Retourne la plage de temps de cet élément. Les composantes de la plage retournée seront du
     * type {@link Date}. Cette méthode peut retourner {@code null} si aucune plage de temps n'est
     * associé à cet élément.
     *
     * @throws CatalogException si l'interrogation du catalogue a échoué.
     */
    DateRange getTimeRange() throws CatalogException;

    /**
     * Retourne la forme géométrique reliant toutes les positions visitées par cet élément,
     * dans leur ordre chronologique.
     *
     * @throws CatalogException si l'interrogation du catalogue a échoué.
     */
    Shape getPath() throws CatalogException;

    /**
     * Vérifie si cet élément intercepte le rectangle spécifié.
     * La réponse retournée par cette méthode n'est qu'à titre indicatif.
     *
     * @throws CatalogException si l'interrogation du catalogue a échoué.
     */
    boolean intersects(final Rectangle2D rect) throws CatalogException;
}
