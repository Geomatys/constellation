/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.ws.embedded;

import org.constellation.sql.Result;

import java.util.Date;


/**
 * Parse the log lines gotten from the execution of the script {@code log.sh [session]},
 * for the date specified.
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
            serviceAndVersion = serviceAndVersion.substring(0, serviceAndVersion.indexOf('/'));
        }
        return new Result(date, lineSplitted[1], directory, passed, false, null);
    }
}
