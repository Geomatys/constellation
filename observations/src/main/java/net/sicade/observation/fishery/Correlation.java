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
import net.sicade.observation.Element;
import net.sicade.observation.coverage.Descriptor;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;


/**
 * Une corr�lation entre des {@linkplain Catch captures} et des {@linkplain Descriptor
 * descripteurs du paysage oc�anique}.
 * 
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
public interface Correlation extends Element {
    /**
     * Retourne une r�f�rence vers le fournisseur des donn�es utilis�es pour
     * le calcul de la {@linkplain #getCorrelation corr�lation}.
     */
    Citation getProvider();
    
    /**
     * Retourne une r�f�rence vers la cat�gorie corr�l�e.
     */
    Category getCategory();
    
    /**
     * Retourne une r�f�rence vers le descripteur corr�l�. 
     */
    Descriptor getDescriptor();
    
    /**
     * Retourne la valeur de la corr�lation.
     */
    double getCorrelation();
    
    /**
     * Retourne la propabilit� que la {@linkplain #getCorrelation corr�lation} ne soit
     * <strong>pas</strong> significative.
     */
    double getPValue();
}
