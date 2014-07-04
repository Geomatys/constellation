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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Queries {

    private HashMap<String, String> parameters;

    private Query main;

    private QueryList statique;

    private QueryList queryList;

    /**
     * Empty constructor used by JAXB
     */
    public Queries() {

    }

    /**
     * Build a new Queries, with a main query and single/multi queries.
     * put the SQL query which normally return only one result in single and the other in multi.
     *
     * @param main The main SQL query (normally used to retrieve all the identifiers)
     * @param queryList The SQL queries.
     */
    public Queries(final Query main, final QueryList queryList) {
        this.main       = main;
        this.queryList  = queryList;
    }

    /**
     * Build a new Queries, with a main query and single/multi queries ans static parameters.
     * put the SQL query which normally return only one result in single and the other in multi.
     *
     * @param main The main SQL query (normally used to retrieve all the identifiers)
     * @param queryList The SQL queries.
     * @param parameters a map of varName/varValue.
     */
    public Queries(final Query main, final QueryList queryList, final HashMap<String, String> parameters) {
        this.main       = main;
        this.queryList  = queryList;
        this.parameters = parameters;
    }

    /**
     * Return the mainQuery.
     * @return
     */
    public Query getMain() {
        return main;
    }

    /**
     * Return the SQL queries returning multiple results.
     * @return
     */
    public QueryList getQueryList() {
        return queryList;
    }

    /**
     * Set  the SQL queries returing multiple results.
     * @param single
     */
    public void setQueryList(QueryList multi) {
        this.queryList = multi;
    }

    /**
     * return all the SQL queries (single + multifixed) but NOT the main query.
     * @return A list of SQL Query.
     */
    public List<Query> getAllQueries() {
        final List<Query> queries = new ArrayList<Query>();
        if (queryList != null) {
            queries.addAll(this.queryList.getQuery());
        }
        return queries;
    }

    /**
     * Return the specified query.
     *
     * @param queryName The name of the query we search.
     * @return
     */
    public Query getQueryByName(String queryName) {
        return queryList.getQueryByName(queryName);
    }

    /**
     * @return the parameters
     */
    public HashMap<String, String> getParameters() {
        if (parameters == null) {
            parameters = new HashMap<String, String>();
        }
        return parameters;
    }

    /**
     * @param parameters the parameters to set
     */
    public void setParameters(HashMap<String, String> parameters) {
        this.parameters = parameters;
    }

    /**
     * @return the statique
     */
    public QueryList getStatique() {
        return statique;
    }

    /**
     * @param statique the statique to set
     */
    public void setStatique(QueryList statique) {
        this.statique = statique;
    }


    @Override
    public String toString() {
        final StringBuilder s = new StringBuilder("[Queries]");
        s.append("main: ").append(main).append('\n');
        if (parameters != null) {
            s.append("Parameters:\n");
            for (String paramName : parameters.keySet()) {
                s.append(paramName).append(':').append(parameters.get(paramName)).append('\n');
            }
        }
        s.append("main: ").append(main).append('\n');
        s.append("queryList:").append(queryList).append('\n');
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
        if (object instanceof Queries) {
            final Queries that = (Queries) object;

            return Objects.equals(this.main,       that.main)     &&
                   Objects.equals(this.queryList,  that.queryList) &&
                   Objects.equals(this.statique,   that.statique) &&
                   Objects.equals(this.parameters, that.parameters);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.main != null ? this.main.hashCode() : 0);
        hash = 53 * hash + (this.queryList != null ? this.queryList.hashCode() : 0);
        hash = 53 * hash + (this.statique != null ? this.statique.hashCode() : 0);
        hash = 53 * hash + (this.parameters != null ? this.parameters.hashCode() : 0);
        return hash;
    }
}
