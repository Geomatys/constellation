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
package net.sicade.observation;


/**
 * Classe de base des exceptions pouvant survenir lors d'une requête sur une base de données.
 *
 * @version $Id$
 * @author Remi Eve
 * @author Martin Desruisseaux
 */
public class CatalogException extends Exception {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = 3838293108990270182L;

    /**
     * Nom de la table dans laquelle l'enregistrement était attendu, ou {@code null} si inconnu.
     */
    private String table;

    /**
     * Construit une exception avec le message spécifié.
     */
    public CatalogException(final String message) {
        super(message);
    }

    /**
     * Construit une exception avec le message et le nom de table spécifié.
     *
     * @param message Message décrivant l'erreur.
     * @param table Nom de la table dans laquelle un problème est survenu, ou {@code null} si inconnu.
     */
    public CatalogException(final String message, final String table) {
        super(message);
        this.table = table;
    }

    /** 
     * Construit une exception avec la cause spécifiée.
     * Le message sera déterminée à partir de la cause.
     */
    public CatalogException(final Exception cause) {
        super(cause.getLocalizedMessage(), cause);
    }

    /** 
     * Construit une exception avec la cause spécifiée.
     * Le message sera déterminée à partir de la cause.
     */
    CatalogException(final Exception cause, final String table) {
        this(cause);
        this.table = table;
    }

    /**
     * Retourne le nom de la table dans laquelle un problème est survenu.
     * Peut retourner {@code null} si le nom de la table n'est pas connu.
     */
    public String getTable() {
        if (table != null) {
            table = table.trim();
            if (table.length() == 0) {
                table = null;
            }
        }
        return table;
    }
}
