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
package org.constellation.sql;


/**
 * Representation of a record of the {@code TestsDescriptions} table.
 *
 * @author Cédric Briançon (Geomatys)
 *
 * @since 0.6
 */
public final class TestsDescription {
    /**
     * The test id.
     */
    private final String id;

    /**
     * The test description.
     */
    private String description;

    /**
     * The test assertion.
     */
    private String assertion;

    public TestsDescription(final String id) {
        this.id = id;
    }

    public TestsDescription(final String id, final String description, final String assertion) {
        this.id = id;
        this.description = description;
        this.assertion = assertion;
    }

    /**
     * Returns the id of the suite tests.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the test description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the test assertion.
     */
    public String getAssertion() {
        return assertion;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 53 * hash + (this.description != null ? this.description.hashCode() : 0);
        hash = 53 * hash + (this.assertion != null ? this.assertion.hashCode() : 0);
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
        final TestsDescription other = (TestsDescription) obj;
        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
            return false;
        }
        if ((this.description == null) ? (other.description != null) : !this.description.equals(other.description)) {
            return false;
        }
        if ((this.assertion == null) ? (other.assertion != null) : !this.assertion.equals(other.assertion)) {
            return false;
        }
        return true;
    }

}
