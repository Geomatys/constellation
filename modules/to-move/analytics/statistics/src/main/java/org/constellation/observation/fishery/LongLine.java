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

// OpenGis dependencies
import org.opengis.observation.sampling.SamplingFeature;


/**
 * Représentation d'une ligne de palangre.
 * 
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
public interface LongLine extends SamplingFeature {
    /**
     * Retourne le nombre d'hameçons.
     */
    int getHookCount();

    /**
     * Retourne la longueur de la palangre.
     */
    float getLineLength();

    /**
     * Retourne la distance au bateau le plus proche du même jour,
     * ou {@link Float#NaN NaN} si elle n'est pas connue.
     */
    float getNearestNeighbor();

    /**
     * Retourne la distance à la côte la plus proche,
     * ou {@link Float#NaN NaN} si elle n'est pas connue.
     */
    float getNearestCoast();
}
