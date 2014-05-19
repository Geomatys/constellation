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
import java.util.Objects;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Column {

    /**
     * the alias of the column
     */
    private String var;

    /**
     * the sql code in the select
     * (most of the time the name of the column but it can be a function)
     */
    private String sql;

    /**
     * EMpty constructor used by JAXB.
     */
    public Column() {

    }

    public Column(final Column column) {
        if (column != null) {
            this.sql = column.sql;
            this.var = column.var;
        }
    }

    /**
     * Build a new Column.
     *
     * @param var the alias of the column.
     * @param sql the column name (or a function)
     */
    public Column(final String var, final String sql) {
        this.sql = sql;
        this.var = var;
    }

    /**
     * return the alias of the column.
     * @return
     */
    public String getVar() {
        return var;
    }

    /**
     * Set the alias of the column.
     * @param var
     */
    public void setVar(String var) {
        this.var = var;
    }

    /**
     * return the column name (or function)
     * @return
     */
    public String getSql() {
        return sql;
    }

    /**
     * set the column name (or function)
     * @param sql
     */
    public void setSql(String sql) {
        this.sql = sql;
    }

    @Override
    public String toString() {
        final StringBuilder s = new StringBuilder("[Column]");
        if (getVar() != null)
            s.append("var: ").append(getVar()).append('\n');
        if (getSql() != null)
            s.append("sql:").append(getSql()).append('\n');
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
        if (object instanceof Column) {
            final Column that = (Column) object;

            return Objects.equals(this.getSql(), that.getSql()) &&
                   Objects.equals(this.getVar(), that.getVar());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + (this.getVar() != null ? this.getVar().hashCode() : 0);
        hash = 13 * hash + (this.getSql() != null ? this.getSql().hashCode() : 0);
        return hash;
    }
}
