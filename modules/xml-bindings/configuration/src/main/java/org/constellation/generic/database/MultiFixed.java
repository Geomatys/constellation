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
import org.geotools.util.Utilities;

/**
 *
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class MultiFixed {

    private List<Query> query;

    public List<Query> getQuery() {
        if (query == null)
            query = new ArrayList<Query>();
        return query;
    }
    
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("[MultiFixed]");
        if (query != null && query.size() != 0) {
            int i = 0;
            for (Query q: query) {
                s.append(i).append(": ").append(q).append('\n');
                i++;
            }
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
        if (object instanceof MultiFixed) {
            final MultiFixed that = (MultiFixed) object;

            return Utilities.equals(this.query, that.query) ;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + (this.query != null ? this.query.hashCode() : 0);
        return hash;
    }
}
