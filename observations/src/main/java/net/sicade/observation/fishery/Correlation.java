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
package net.sicade.observation.fishery;

// Sicade dependencies
import net.sicade.coverage.catalog.Element;
import net.sicade.coverage.catalog.Descriptor;

// OpenGIS dependencies
import org.opengis.metadata.citation.Citation;


/**
 * Une corrélation entre des {@linkplain Catch captures} et des {@linkplain Descriptor
 * descripteurs du paysage océanique}.
 * 
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
public interface Correlation extends Element {
    /**
     * Retourne une référence vers le fournisseur des données utilisées pour
     * le calcul de la {@linkplain #getCorrelation corrélation}.
     */
    Citation getProvider();
    
    /**
     * Retourne une référence vers la catégorie corrélée.
     */
    Category getCategory();
    
    /**
     * Retourne une référence vers le descripteur corrélé. 
     */
    Descriptor getDescriptor();
    
    /**
     * Retourne la valeur de la corrélation.
     */
    double getCorrelation();
    
    /**
     * Retourne la propabilité que la {@linkplain #getCorrelation corrélation} ne soit
     * <strong>pas</strong> significative.
     */
    double getPValue();
}
