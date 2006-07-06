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
package net.sicade.observation.fishery;

// Sicade dependencies
import net.sicade.observation.Observable;


/**
 * Une {@linkplain Species esp�ces} observ�e � un certain {@linkplain Stage stade de d�veloppement}.
 * 
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
public interface Category extends Observable {
    /**
     * Retourne l'esp�ce observ�e.
     */
    Species getPhenomenon();
    
    /**
     * Retourne le stade de d�veloppement de l'esp�ce observ�.
     */
    Stage getStage();

    /**
     * Retourne la m�thode par laquelle les individus sont captur�s.
     */
    FisheryType getProcedure();
}
