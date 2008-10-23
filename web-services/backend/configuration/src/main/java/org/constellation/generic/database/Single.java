/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.constellation.generic.database;

import java.util.ArrayList;
import java.util.List;
import org.geotools.util.Utilities;

/**
 *
 * @author guilhem
 */
public class Single {

    private List<Query> query;

    public List<Query> getQuery() {
        if (query == null)
            query = new ArrayList<Query>();
        return query;
    }
    
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("[Single]");
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
        if (object instanceof Single) {
            final Single that = (Single) object;

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
