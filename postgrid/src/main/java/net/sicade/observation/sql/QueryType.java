/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
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
 */
package net.sicade.observation.sql;


/**
 * Type de requête exécutée par {@link SingletonTable}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public enum QueryType {
    /**
     * Tous les enregistrements seront listés. C'est le type de requête sélectionné
     * lorsque la méthode {@link SingletonTable#getEntries} est appelée.
     */
    LIST,

    /**
     * Un enregistrement sera sélectionné en fonction de son nom. C'est le type de requête
     * sélectionné lorsque la méthode {@link SingletonTable#getEntry(String)} est appelée.
     */
    SELECT,

    /**
     * Un enregistrement sera sélectionné en fonction de son numéro d'identifiant. C'est le type
     * de requête sélectionné lorsque la méthode {@link SingletonTable#getEntry(int)} est appelée.
     */
    SELECT_BY_IDENTIFIER,

    /**
     * Sélectionne les coordonnées spatio-temporelles d'un ensemble d'enregistrements. C'est le
     * type de requête que peut exécuter {@link BoundedSingletonTable#getGeographicBoundingBox}.
     */
    BOUNDING_BOX,

    /**
     * Un enregistrement sera ajouté.
     */
    INSERT
}
