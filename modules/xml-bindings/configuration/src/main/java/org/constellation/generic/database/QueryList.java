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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.geotoolkit.util.Utilities;

/**
 *
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class QueryList {

    /**
     * A list of SQL query.
     */
    private List<Query> query;

    /**
     * constructor used by JAXB.
     */
    public QueryList() {

    }

    /**
     * build a query list with only one query.
     *
     * @param query A SQL Query.
     */
    public QueryList(Query query) {
        this.query = new ArrayList<Query>();
        this.query.add(query);
    }

    /**
     * build a query list with the specified queries.
     * @param query
     */
    public QueryList(List<Query> query) {
        this.query = query;
    }

    /**
     * Return the list of query.
     * @return
     */
    public List<Query> getQuery() {
        if (query == null)
            query = new ArrayList<Query>();
        return query;
    }

    /**
     * Return the query named "name" of {@code null}
     * if there is no such query.
     * @param name
     * @return
     */
    public Query getQueryByName(String name) {
        if (query != null) {
            for (Query q : query) {
                if (name.equals(q.getName())) {
                    return q;
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        final StringBuilder s = new StringBuilder("[QueryList]");
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
        if (object instanceof QueryList) {
            final QueryList that = (QueryList) object;

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
