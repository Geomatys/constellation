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


package org.constellation.generic.database;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * @author Guilhem Legal (Geomatys)
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Select {

    @XmlAttribute
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String group;

    /**
     * A list of Column to add in the select and their alias.
     */
    private List<Column> col;

    /**
     * Empty constrcutor used by JAXB..
     */
    public Select() {
    }

    public Select(final Select select) {
        if (select != null) {
            this.col = new ArrayList<Column>();
            for (Column c : select.getCol()) {
                this.col.add(new Column(c));
            }
            this.group = select.group;
        }
    }

    /**
     * Build a select with a single Column.
     *
     * exemple: new Select("var1", "p.identifier") will give in SQL :
     * SELECT p.identifier as var1
     *
     * @param var the alias of the column
     * @param sql the column itself
     */
    public Select(final String var, final String sql) {
        this.col = new ArrayList<Column>();
        this.col.add(new Column(var, sql));
    }

    /**
     * Build a select with a single Column.
     *
     * @param col the column to add to the select CLAUSE.
     */
    public Select(final Column col) {
        this.col = new ArrayList<Column>();
        this.col.add(col);
    }

    /**
     * Build a select with multiple Column.
     *
     * @param col the columns to add to the select CLAUSE.
     */
    public Select(List<Column> col) {
        this.col = col;
    }

    /**
     * Gets the value of the alias property.
     */
    public List<Column> getCol() {
        if (col == null) {
            col = new ArrayList<Column>();
        }
        return col;
    }

    /**
     * Sets the value of the alias property.
     */
    public void setCol(List<Column> value) {
        this.col = value;
    }

    /**
     * Gets the value of the alias property.
     */
    public void addCol(final Column column) {
        if (column != null) {
            if (col == null) {
                col = new ArrayList<Column>();
            }
            col.add(column);
        }
    }

    /**
     * Gets the value of the alias property.
     */
    public void addCol(final String var, final String sql) {
        if (col == null) {
            col = new ArrayList<Column>();
        }
        col.add(new Column(var, sql));
    }


    /**
     * @return the group
     */
    public String getGroup() {
        return group;
    }

    /**
     * @param group the group to set
     */
    public void setGroup(final String group) {
        this.group = group;
    }


    @Override
    public String toString() {
        final StringBuilder s = new StringBuilder("[Select]");
        if (group != null) {
            s.append("group: ").append(group).append('\n');
        }
        s.append("columns: ").append('\n');
        for (Column c: col) {
            s.append(c.toString()).append('\n');
        }
        return s.toString();
    }

    /**
     * Verify if this entry is identical to the specified object.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof Select) {
            final Select that = (Select) object;

            return Objects.equals(this.col,   that.col) &&
                   Objects.equals(this.group, that.group);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.col != null ? this.col.hashCode() : 0);
        hash = 79 * hash + (this.group != null ? this.group.hashCode() : 0);
        return hash;
    }
}
