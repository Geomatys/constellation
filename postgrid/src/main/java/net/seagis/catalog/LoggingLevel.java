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
package net.seagis.catalog;

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
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = 7505485471495575298L;

    /**
     * The base level. Current value is between {@link Level#CONFIG} and {@link Level#INFO}.
     */
    private static final int BASE = 750;

    /**
     * Logging level for any read operations (SQL {@code SELECT} statements).
     */
    public static final Level SELECT = new LoggingLevel("SELECT", BASE);

    /**
     * Logging level for any write operations (SQL {@code INSERT},
     * {@code UPDATE}, {@code DELETE} or {@code CREATE} statements).
     */
    public static final Level UPDATE = new LoggingLevel("UPDATE", BASE + 25);

    /**
     * Constructs a new logging level.
     *
     * @param name  The logging level name (e.g. {@code "SELECT"}.
     * @param value The level value.
     */
    private LoggingLevel(final String name, final int value) {
        super(name, value);
    }
}
