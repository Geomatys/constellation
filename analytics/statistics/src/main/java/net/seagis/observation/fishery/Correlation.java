/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package net.seagis.observation.fishery;

// Sicade dependencies
import net.seagis.catalog.Element;
import net.seagis.coverage.model.Descriptor;

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
