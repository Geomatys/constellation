/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010, Geomatys
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
package org.constellation.util;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Logging utility methods.
 *
 * @author Cédric Briançon (Geomatys)
 * 
 * @since 0.5
 */
public final class LoggingUtilities {

    private LoggingUtilities() {}

    /**
     * Defines the log level to display in console and files, for the given
     * {@linkplain Logger logger}.
     * Warning: this method will change the logging level for the current logger,
     * but will also change the handler logging level for all parents of the given
     * logger. Consequently other loggers can eventually be displayed if they share
     * the same handler.
     *
     * @param logger   The logger for which the log level will be modified.
     * @param logLevel The minimum level for logs to be displayed.
     */
    public static void setLogLevel(Logger logger, final Level logLevel) {
        logger.setLevel(logLevel);
        while (logger != null) {
            final Handler[] handlers = logger.getHandlers();
            if (handlers != null && handlers.length != 0) {
                for (Handler handler : logger.getHandlers()) {
                    /* Here, we can make a difference between ConsoleHandler and FileHandler
                     * to control either the console or file logging output.
                     * In this implementation we change the level for both console and file
                     * logging.
                     */
                    handler.setLevel(logLevel);
                }
                return;
            }
            logger = logger.getParent();
        }
    }
}
