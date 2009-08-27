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
 * Representation of a record of the {@code Suites} table.
 *
 * @version $Id$
 * @author Cédric Briançon (Geomatys)
 *
 * @since 0.4
 */
public final class Suite {
    /**
     * The date of the suite.
     */
    private final Date date;

    /**
     * The service name.
     */
    private final String name;

    /**
     * The service version.
     */
    private final String version;

    public Suite(final Date date, final String name, final String version) {
        this.date = date;
        this.name = name;
        this.version = version;
    }

    /**
     * Returns the date of the suite tests.
     */
    public Date getDate() {
        return date;
    }

    /**
     * Returns the service name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the service version.
     */
    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "Suite["+ date +","+ name +","+ version +"]";
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (this.date != null ? this.date.hashCode() : 0);
        hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 53 * hash + (this.version != null ? this.version.hashCode() : 0);
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
        final Suite other = (Suite) obj;
        if ((this.date == null) ? (other.date != null) : !this.date.equals(other.date)) {
            return false;
        }
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if ((this.version == null) ? (other.version != null) : !this.version.equals(other.version)) {
            return false;
        }
        return true;
    }

}
