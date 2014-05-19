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
