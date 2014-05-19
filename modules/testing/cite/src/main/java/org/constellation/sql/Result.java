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
     * The assertion tested.
     */
    private final String assertion;

    /**
     * Defined whether the test has passed or not.
     */
    private final boolean passed;

    /**
     * Defined whether the test has passed or not.
     */
    private boolean groupNode;

    public Result(final Date date, final String id, final String directory, final boolean passed, final boolean groupNode,
            final String assertion) {
        this.date        = date;
        this.id          = id;
        this.directory   = directory;
        this.passed      = passed;
        this.assertion   = assertion;
        this.groupNode   = groupNode;
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

    /**
     * @return the assertion
     */
    public String getAssertion() {
        return assertion;
    }

    /**
     * @return the groupNode
     */
    public boolean isGroupNode() {
        return groupNode;
    }

    /**
     * @param groupNode the groupNode to set
     */
    public void setGroupNode(boolean groupNode) {
        this.groupNode = groupNode;
    }

    @Override
    public String toString() {
        return "Result["+ ResultsDatabase.DATE_FORMAT.format(date) +","+ id +","+ directory +","+ passed +","+groupNode+"]";
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
