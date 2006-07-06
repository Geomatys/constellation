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
package net.sicade.observation.coverage;

import net.sicade.observation.Element;
import net.sicade.observation.Phenomenon;


/**
 * Un th�me de s�rie(s) d'images. Un th�me peut �tre par exemple la temp�rature, o� les concentrations
 * en chlorophylle-<var>a</var>. Notez qu'un th�me est diff�rent d'une {@linkplain Series s�rie} du
 * fait qu'une m�me s�rie repr�sente souvent un m�me th�me (par exemple la temp�rature) mesur� par le
 * m�me capteur (par exemple les satellites NOAA) sur la m�me r�gion g�ographique. La notion de th�me
 * repr�sent�e ici est plus g�n�rale.
 *
 * @version $Id$
 * @author Antoine Hnawia
 */
public interface Thematic extends Phenomenon {
}
