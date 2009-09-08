/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.geotoolkit.util.Utilities;

/**
 *
 * @author guilhem
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Column {
    
    private String var;
    
    private String sql;

    public Column() {

    }

    public Column(String var, String sql) {
        this.sql = sql;
        this.var = var;
    }

    public String getVar() {
        return var;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public String getSql() {
        return sql;
    }

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

            return Utilities.equals(this.getSql(), that.getSql()) &&
                   Utilities.equals(this.getVar(), that.getVar());
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
