/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.observation.fishery;

// Constellation dependencies

import org.constellation.catalog.Element;
import org.constellation.coverage.model.Descriptor;
import org.opengis.metadata.citation.Citation;

// OpenGIS dependencies


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
