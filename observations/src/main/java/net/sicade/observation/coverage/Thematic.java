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
package net.sicade.observation.coverage;

import net.sicade.observation.Element;
import net.sicade.observation.Phenomenon;


/**
 * Un thème de série(s) d'images. Un thème peut être par exemple la température, où les concentrations
 * en chlorophylle-<var>a</var>. Notez qu'un thème est différent d'une {@linkplain Series série} du
 * fait qu'une même série représente souvent un même thème (par exemple la température) mesuré par le
 * même capteur (par exemple les satellites NOAA) sur la même région géographique. La notion de thème
 * représentée ici est plus générale.
 *
 * @version $Id$
 * @author Antoine Hnawia
 */
public interface Thematic extends Phenomenon {
}
