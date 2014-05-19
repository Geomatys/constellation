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

// openGis dependencies
import org.opengis.observation.Measurement;


/**
 * Une capture faites lors d'un {@linkplain SeineSet coups de senne} ou par une
 * {@linkplain LongLine palangre}. Un coup de sennes ou une palangre comprendront
 * généralement plusieurs captures, une pour chaque {@linkplain Category catégorie}
 * de poisson pêché.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
public interface Catch extends Measurement {
    /**
     * Retourne la catégorie de poissons capturés.
     */
    Category getObservable();

    /**
     * Retourne le comptage des prises.
     */
    int getCount();
}
