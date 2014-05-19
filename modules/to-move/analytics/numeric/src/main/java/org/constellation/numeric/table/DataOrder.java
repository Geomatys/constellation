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
package org.constellation.numeric.table;


/**
 * Ordre des données dans un {@linkplain OrderedVector vecteur}. Les données peuvent être croissantes
 * ou décroissantes, strictement ou pas.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public enum DataOrder {
    /**
     * Indique que les données d'un {@linkplain OrderedVector vecteur} sont strictement croissantes.
     */
    STRICTLY_ASCENDING,

    /**
     * Indique que les données d'un {@linkplain OrderedVector vecteur} sont croissantes.
     * Le vecteur contient quelques données consécutives de même valeur.
     */
    ASCENDING,

    /**
     * Indique que les données d'un {@linkplain OrderedVector vecteur} ont la même valeur.
     */
    FLAT,

    /**
     * Indique que les données d'un {@linkplain OrderedVector vecteur} sont décroissantes.
     * Le vecteur contient quelques données consécutives de même valeur.
     */
    DESCENDING,

    /**
     * Indique que les données d'un {@linkplain OrderedVector vecteur} sont strictement croissantes.
     */
    STRICTLY_DESCENDING,

    /**
     * Indique que les données d'un {@linkplain OrderedVector vecteur} ne sont pas ordonnées.
     */
    UNORDERED
}
