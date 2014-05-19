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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.Objects;

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

    public QueryList(final QueryList queryList) {
        if (queryList != null) {
            this.query = new ArrayList<Query>();
            for (Query q : queryList.query) {
                this.query.add(new Query(q));
            }
        }
    }

    /**
     * build a query list with only one query.
     *
     * @param query A SQL Query.
     */
    public QueryList(final Query query) {
        this.query = new ArrayList<Query>();
        this.query.add(query);
    }

    /**
     * build a query list with the specified queries.
     * @param query
     */
    public QueryList(final List<Query> query) {
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

            return Objects.equals(this.query, that.query) ;
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
