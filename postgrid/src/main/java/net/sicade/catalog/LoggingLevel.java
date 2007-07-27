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
package net.sicade.catalog;

import java.util.logging.Level;


/**
 * Logging levels for SQL instructions executed on the catalog database.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see Element#LOGGER
 */
public final class LoggingLevel extends Level {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = 7505485471495575298L;

    /**
     * The base level. Current value is between {@link Level#CONFIG} and {@link Level#INFO}.
     */
    private static final int BASE = 750;

    /**
     * Logging level for SQL {@code SELECT} statements.
     */
    public static final Level SELECT = new LoggingLevel("SELECT", BASE);

    /**
     * Logging level for SQL {@code INSERT} statements.
     */
    public static final Level INSERT = new LoggingLevel("INSERT", BASE + 10);

    /**
     * Logging level for SQL {@code UPDATE} statements.
     */
    public static final Level UPDATE = new LoggingLevel("UPDATE", BASE + 20);

    /**
     * Logging level for SQL {@code DELETE} statements.
     */
    public static final Level DELETE = new LoggingLevel("DELETE", BASE + 30);

    /**
     * Logging level for SQL {@code CREATE} statements.
     */
    public static final Level CREATE = new LoggingLevel("CREATE", BASE + 40);

    /**
     * Construit un nouveau niveau de journalisation.
     *
     * @param name  Le nom du niveau (par exemple {@code "SQL_UPDATE"}.
     * @param value La valeur du niveau.
     */
    private LoggingLevel(final String name, final int value) {
        super(name, value);
    }
}
