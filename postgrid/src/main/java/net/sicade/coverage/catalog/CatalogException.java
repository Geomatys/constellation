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
 * Base class for exceptions that may occur while querying the catalog.
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
     * The database table name where a problem occured, or {@code null} if unknown.
     */
    private String table;

    /**
     * Creates an exception with the specified message.
     *
     * @param message The detail message.
     */
    public CatalogException(final String message) {
        super(message);
    }

    /**
     * Creates an exception with the specified message and table name.
     *
     * @param message The detail message.
     * @param table The database table name where a problem occured, or {@code null} if unknown.
     */
    public CatalogException(final String message, final String table) {
        super(message);
        this.table = table;
    }

    /** 
     * Creates an exception from the specified cause. The details message
     * is copied from the cause.
     *
     * @param cause The cause for this exception.
     */
    public CatalogException(final Exception cause) {
        super(cause.getLocalizedMessage(), cause);
    }

    /** 
     * Creates an exception from the specified cause. The details message
     * is copied from the cause.
     *
     * @param cause The cause for this exception.
     * @param table The database table name where a problem occured, or {@code null} if unknown.
     */
    CatalogException(final Exception cause, final String table) {
        this(cause);
        this.table = table;
    }

    /**
     * Returns the database table name where a problem occured, or {@code null} if unknown.
     *
     * @return The database table name.
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
