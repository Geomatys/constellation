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
 * Représentation d'un phénomène observé. Cette interface est étendue dans des paquets
 * spécialisés. Par exemple pour les images, les phénomènes observé sont appelés
 * {@linkplain net.sicade.observation.coverage.Thematic thèmes}. Pour les données de pêches,
 * les phénomèmes sont appelés {@linkplain net.sicade.observation.fishery.Species espèces}.
 * <p>
 * La combinaison d'un phénomène avec une {@linkplain Procedure procedure} donne un
 * {@linkplain Observable observable}.
 * 
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
public interface Phenomenon extends Element {    
}
