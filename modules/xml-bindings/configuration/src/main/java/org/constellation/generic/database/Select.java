/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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


package org.constellation.generic.database;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.geotoolkit.util.Utilities;


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

            return Utilities.equals(this.col,   that.col) &&
                   Utilities.equals(this.group, that.group);
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
