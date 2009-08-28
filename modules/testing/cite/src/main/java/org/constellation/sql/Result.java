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
package org.constellation.sql;

import java.util.Date;


/**
 * Representation of a record of the {@code Results} table.
 *
 * @version $Id$
 * @author Cédric Briançon (Geomatys)
 *
 * @since 0.4
 */
public final class Result {
    /**
     * The date of execution.
     */
    private final Date date;

    /**
     * The identifier of the test.
     */
    private final String id;

    /**
     * The directory where logs are stored.
     */
    private final String directory;

    /**
     * Defined whether the test has passed or not.
     */
    private final boolean passed;

    public Result(final Date date, final String id, final String directory, final boolean passed) {
        this.date = date;
        this.id = id;
        this.directory = directory;
        this.passed = passed;
    }

    /**
     * Returns the date of test.
     */
    public Date getDate() {
        return date;
    }

    /**
     * Returns the identifier of the test.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the directory where logs are stored.
     */
    public String getDirectory() {
        return directory;
    }

    /**
     * {@code True} if the test has passed. {@code False} otherwise.
     */
    public boolean isPassed() {
        return passed;
    }

    @Override
    public String toString() {
        return "Result["+ ResultsDatabase.DATE_FORMAT.format(date) +","+ id +","+ directory +","+ passed +"]";
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 31 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 31 * hash + (this.directory != null ? this.directory.hashCode() : 0);
        hash = 31 * hash + (this.passed ? 1 : 0);
        hash = 31 * hash + (this.date != null ? this.date.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Result other = (Result) obj;
        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
            return false;
        }
        if ((this.directory == null) ? (other.directory != null) : !this.directory.equals(other.directory)) {
            return false;
        }
        if (this.passed != other.passed) {
            return false;
        }
        if ((this.date == null) ? (other.date != null) : !this.date.equals(other.date)) {
            return false;
        }
        return true;
    }
}
