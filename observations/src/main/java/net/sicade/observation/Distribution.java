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


/**
 * Distribution statistique approximative des {@linkplain Measurement valeurs mesurées} pour un
 * {@linkplain Observable observable} donné. Cette distribution n'est pas nécessairement calculée
 * à partir des données; il peut s'agir d'un <cite>a-priori</cite>. Cette interface est utilisée
 * pour effectuer des changements de variables avant injection des valeurs dans un
 * {@linkplain net.sicade.observation.coverage.LinearModel modèle linéaire}, de façon à obtenir
 * une distribution des valeurs plus proche de la
 * <A HREF="http://mathworld.wolfram.com/NormalDistribution.html">distribution normale</A>.
 * Le changement de variable peut consister par exemple à calculer le logarithme d'une valeur,
 * afin de transformer une <A HREF="http://mathworld.wolfram.com/LogNormalDistribution.html">
 * distribution log-normale</A> en distribution normale.
 * 
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
public interface Distribution extends Element {
    /**
     * Applique un changement de variable, si nécessaire.
     * Les valeurs retournées devraient avoir (au moins approximativement) une
     * <A HREF="http://mathworld.wolfram.com/NormalDistribution.html">distribution normale</A>.
     * Si cette méthode ne sait pas transformer les valeurs, alors elle doit retourner {@code value}
     * inchangé. Toutefois, le même traitement doit être appliqué à toutes les valeurs (cette méthode
     * ne doit pas transformer certaines valeurs et retourner {@code value} inchangé pour d'autres
     * valeurs).
     *
     * @param  value La valeur à transformer.
     * @return La valeur transformée, ou {@code value} si la distribution des valeurs échantillonées
     *         était déjà normale ou si cette interface ne sait pas transformer les valeurs.
     */
    double normalize(double value);

    /**
     * Retourne {@code true} si {@link #normalize normalize} n'effectue aucune transformation.
     */
    boolean isIdentity();
}
