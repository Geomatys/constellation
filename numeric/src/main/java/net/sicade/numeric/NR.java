/*
 * Sicade - Syst�mes int�gr�s de connaissances
 *          pour l'aide � la d�cision en environnement
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
package net.sicade.numeric;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * R�f�rence bibliographique vers une fonction de <A HREF="http://www.nr.com">Numerical Recipes</A>.
 * L'annotation {@code NR} doit accompagner chaque m�thode dont le code, m�me modifi�, est d�riv�
 * de <cite>Numerical Recipes</cite>. Ces fonctions sont propri�t�s intellectuelles de leurs
 * auteurs et ne pourront pas �tres distribu�es librement.
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
     * Chapitre ou section dans laquelle est publi�e la fonction.
     */
    String chapter();
}
