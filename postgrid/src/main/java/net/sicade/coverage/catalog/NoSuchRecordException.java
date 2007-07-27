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
package net.sicade.coverage.catalog;


/**
 * Indique qu'un enregistrement requis n'a pas été trouvé dans la base de données.
 * Cette exception contient le nom de la table dans laquelle l'enregistrement était attendu.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class NoSuchRecordException extends CatalogException {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = -3105861955682823122L;

    /**
     * Construit une exception signalant qu'un enregistrement n'a pas été trouvé.
     *
     * @param message Message décrivant l'erreur.
     * @param table Nom de la table dans laquelle l'enregistrement était attendu, ou {@code null} si inconnu.
     */
    public NoSuchRecordException(final String message, final String table) {
        super(message, table);
    }
}
