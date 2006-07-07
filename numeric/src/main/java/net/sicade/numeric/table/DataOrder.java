/*
 * Sicade - Syst�mes int�gr�s de connaissances pour l'aide � la d�cision en environnement
 * (C) 2006, Institut de Recherche pour le D�veloppement
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
package net.sicade.numeric.table;


/**
 * Ordre des donn�es dans un {@linkplain OrderedVector vecteur}. Les donn�es peuvent �tre croissantes
 * ou d�croissantes, strictement ou pas.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public enum DataOrder {
    /**
     * Indique que les donn�es d'un {@linkplain OrderedVector vecteur} sont strictement croissantes.
     */
    STRICTLY_ASCENDING,

    /**
     * Indique que les donn�es d'un {@linkplain OrderedVector vecteur} sont croissantes.
     * Le vecteur contient quelques donn�es cons�cutives de m�me valeur.
     */
    ASCENDING,

    /**
     * Indique que les donn�es d'un {@linkplain OrderedVector vecteur} ont la m�me valeur.
     */
    FLAT,

    /**
     * Indique que les donn�es d'un {@linkplain OrderedVector vecteur} sont d�croissantes.
     * Le vecteur contient quelques donn�es cons�cutives de m�me valeur.
     */
    DESCENDING,

    /**
     * Indique que les donn�es d'un {@linkplain OrderedVector vecteur} sont strictement croissantes.
     */
    STRICTLY_DESCENDING,

    /**
     * Indique que les donn�es d'un {@linkplain OrderedVector vecteur} ne sont pas ordonn�es.
     */
    UNORDERED
}
