/*
 * Sicade - Syst�mes int�gr�s de connaissances pour l'aide � la d�cision en environnement
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
package net.sicade.observation.sql;


/**
 * Type de requ�te ex�cut�e par {@link SingletonTable}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public enum QueryType {
    /**
     * Tous les enregistrements seront list�s. C'est le type de requ�te s�lectionn�
     * lorsque la m�thode {@link SingletonTable#getEntries} est appel�e.
     */
    LIST,

    /**
     * Un enregistrement sera s�lectionn� en fonction de son nom. C'est le type de requ�te
     * s�lectionn� lorsque la m�thode {@link SingletonTable#getEntry(String)} est appel�e.
     */
    SELECT,

    /**
     * Un enregistrement sera s�lectionn� en fonction de son num�ro d'identifiant. C'est le type
     * de requ�te s�lectionn� lorsque la m�thode {@link SingletonTable#getEntry(int)} est appel�e.
     */
    SELECT_BY_IDENTIFIER,

    /**
     * S�lectionne les coordonn�es spatio-temporelles d'un ensemble d'enregistrements. C'est le
     * type de requ�te que peut ex�cuter {@link BoundedSingletonTable#getGeographicBoundingBox}.
     */
    BOUNDING_BOX,

    /**
     * Un enregistrement sera ajout�.
     */
    INSERT
}
