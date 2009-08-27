/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2009, Geomatys
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
package org.constellation.ws.embedded;

import java.util.Date;
import org.constellation.sql.Result;


/**
 *
 * @version $Id$
 * @author Cédric Briançon (Geomatys)
 *
 * @since 0.4
 */
public final class LogParser {
    /**
     * The execution date of the tests.
     */
    private final Date date;

    public LogParser(final Date date) {
        this.date = date;
    }

    /**
     * Parse an output line of the log, and convert it into a {@link Result} record.
     *
     * @param line The line to parse.
     * @return The {@link Result}, or {@code null} if the line is not recognized.
     */
    public Result toResult(String line) {
        line = line.trim();
        if (!line.startsWith("Test ")) {
            return null;
        }
        final String[] lineSplitted = line.split(" ");
        final boolean passed = (lineSplitted[3].equals("Passed")) ? true : false;
        String serviceAndVersion = lineSplitted[2];
        // remove the first and last character (should be parenthesis).
        serviceAndVersion = serviceAndVersion.substring(1, serviceAndVersion.length() - 1);
        final String directory = serviceAndVersion;
        if (serviceAndVersion.contains("/")) {
            serviceAndVersion = serviceAndVersion.substring(0, serviceAndVersion.indexOf("/"));
        }
        final String[] serviceAndVersionArray = serviceAndVersion.split("-");
        return new Result(serviceAndVersionArray[0], serviceAndVersionArray[1],
                          lineSplitted[1], directory, passed, date);
    }
}
