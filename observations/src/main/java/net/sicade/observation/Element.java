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

import java.util.logging.Logger;


/**
 * Interface de base des éléments ayant un rapport avec les observations. Il s'agit en quelque sorte
 * de l'équivalent de la classe {@link Object}; une interface de base pouvant représenter à peu près
 * n'importe quoi. Les interfaces dérivées utiles comprennent les {@linkplain Observation observations}
 * et les {@linkplain net.sicade.observation.coverage.Series séries d'images} par exemple.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface Element {
    /**
     * Le journal dans lequel enregistrer les événements qui ont rapport avec les données d'observations.
     * Ce journal peut archiver des événements relatifs à certaines {@linkplain LoggingLevel#SELECT
     * consultations} de la base de données, et surtout aux {@linkplain LoggingLevel#UPDATE mises à jour}.
     */
    Logger LOGGER = Logger.getLogger("net.sicade.observation");

    /**
     * Retourne le nom de cet élément. Ce nom peut être dérivé à partir d'une propriété arbitraire
     * de cet élément. Par exemple dans le cas d'une image, il s'agira le plus souvent du nom du
     * fichier (sans son chemin). Le nom retourné par cette méthode devrait être suffisament parlant
     * pour être inséré dans une interface utilisateur (par exemple une liste déroulante).
     */
    String getName();

    /**
     * Retourne des remarques s'appliquant à cette entrée, ou {@code null} s'il n'y en a pas.
     * Ces remarques peuvent être par exemple une courte explication à faire apparaître dans une
     * interface utilisateur comme "<cite>tooltip text</cite>".
     */
    String getRemarks();
}
