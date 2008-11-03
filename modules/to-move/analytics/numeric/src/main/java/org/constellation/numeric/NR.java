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
