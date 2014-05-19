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
package org.constellation.numeric;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * Référence bibliographique vers une fonction de <A HREF="http://www.nr.com">Numerical Recipes</A>.
 * L'annotation {@code NR} doit accompagner chaque méthode dont le code, même modifié, est dérivé
 * de <cite>Numerical Recipes</cite>. Ces fonctions sont propriétés intellectuelles de leurs
 * auteurs et ne pourront pas êtres distribuées librement.
 *
 * @author Patricia Derex
 * @version $Id$
 *
 * @see <A HREF="http://www.nr.com">Numerical Recipes</A>
 */
@Documented
@Retention(value = RetentionPolicy.SOURCE)
public @interface NR {
    /**
     * Nom de la fonction (habituellement en C/C++) de <cite>Numerical Recipes</cite>.
     */
    String function();

    /**
     * Chapitre ou section dans laquelle est publiée la fonction.
     */
    String chapter();
}
