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
import org.geotoolkit.util.Utilities;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Select {

   private List<Column> col;

   public Select() {

   }

   public Select(String var, String sql) {
       this.col = new ArrayList<Column>();
       this.col.add(new Column(var, sql));
   }

   public Select(Column col) {
       this.col = new ArrayList<Column>();
       this.col.add(col);
   }

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
    
    
    @Override
    public String toString() {
        final StringBuilder s = new StringBuilder("[Select]");
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

            return Utilities.equals(this.col, that.col);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.col != null ? this.col.hashCode() : 0);
        return hash;
    }
}
