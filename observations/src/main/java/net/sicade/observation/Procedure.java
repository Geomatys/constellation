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
package net.sicade.observation;


/**
 * Repr�sentation d'une proc�dure utilis�e pour observer un {@linkplain Phenomenon ph�nom�ne}.
 * Cette interface est �tendue dans des paquets sp�cialis�s. Par exemple pour les images, les
 * proc�dures utilis�es sont des {@linkplain net.sicade.observation.coverage.Operation op�rations}.
 * Pour les donn�es de p�ches, les proc�dures utilis�es sont des
 * {@linkplain net.sicade.observation.fishery.FisheryType types de p�che}.
 * <p>
 * La combinaison d'un {@linkplain Phenomenon ph�nom�ne} avec une procedure donne un
 * {@linkplain Observable observable}.
 *
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
public interface Procedure extends Element {
}
