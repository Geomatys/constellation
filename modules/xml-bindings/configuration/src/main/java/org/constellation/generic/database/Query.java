/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2009, Geomatys
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.geotoolkit.util.Utilities;


/**
 * @author Guilhem Legal (Geomatys)
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "query")
public class Query {

    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String name;
    @XmlAttribute
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String option;
    private HashMap<String, String> parameters;

    private QueryList statique;
    
    @XmlElement(required = true)
    private List<Select> select;
    private List<LeftJoin> leftJoin;
    @XmlElement(required = true)
    private List<From> from;
    private List<Where> where;
    private List<Orderby> orderBy;
    private List<Groupby> groupby;
    
   
    
    private Union union;

    /**
     * Constructor used by JAXB
     */
    public Query() {

    }

    /**
     * Build a SQL query with SELECT and FROM clause.
     *
     * @param name The name of the SQL query.
     * @param select The SELECT clause.
     * @param from The FROM clause.
     */
    public Query(String name, Select select, From from) {
        this.name   = name;
        this.select = Arrays.asList(select);
        this.from   = Arrays.asList(from);
    }

    /**
     * Build a SQL query with SELECT ,FROM and WHERE clause.
     *
     * @param name The name of the SQL query.
     * @param select The SELECT clause.
     * @param from The FROM clause.
     * @param where The WHERE clause.
     */
    public Query(String name, Select select, From from, Where where) {
        this.name   = name;
        this.select = Arrays.asList(select);
        this.from   = Arrays.asList(from);
        this.where  = Arrays.asList(where);
    }

    /**
     *
    * Build a SQL query with SELECT ,FROM, WHERE and ORDERBY clause.
     *
     * @param name The name of the SQL query.
     * @param select The SELECT clause.
     * @param from The FROM clause.
     * @param where The WHERE clause.
     * @param orderBy The ORDERBy clause.
     */
    public Query(String name, Select select, From from, Where where, Orderby orderBy) {
        this.name    = name;
        this.select  = Arrays.asList(select);
        this.from    = Arrays.asList(from);
        this.where   = Arrays.asList(where);
        this.orderBy = Arrays.asList(orderBy);
    }

    /**
     * Gets the value of the name property.
     */
    public String getName() {
        return name;
    }

    /**
     * Return all the name of the variable in a List.
     * @return
     */
    public List<String> getVarNames() {
        final List<String> varNames = new ArrayList<String>();
        if (select != null) {
            for (Select s : select) {
                for (Column col : s.getCol()) {
                    varNames.add(col.getVar());
                }
            }
        }
        return varNames;
    }

    /**
     * Return the name of the first variable in a List.
     * @return
     */
    public String getFirstVarName() {
        if (select != null) {
            for (Select s : select) {
                for (Column col : s.getCol()) {
                    return col.getVar();
                }
            }
        }
        return null;
    }
    
    /**
     * Sets the value of the name property.
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the option property.
     */
    public String getOption() {
        return option;
    }

    /**
     * Sets the value of the option property.
     */
    public void setOption(String value) {
        this.option = value;
    }

    /**
     * Gets the value of the select property.
     */
    public List<Select> getSelect() {
        if (select == null) {
            select = new ArrayList<Select>();
        }
        return select;
    }

    /**
     * Gets the value of the select property for the specified group name.
     */
    public Select getSelect(final String group) {
        for(Select s: getSelect()) {
            if (group != null && group.equals(s.getGroup())) {
                return s;
            }
        }
        return null;
    }

    /**
     * Add a select clause to the query.
     *
     * @param select a SQL Select clause
     */
    public void addSelect(final Select select) {
        this.getSelect().add(select);
    }
    
    /**
     * Gets the value of the from property.
     */
    public List<From> getFrom() {
        if (from == null) {
            from = new ArrayList<From>();
        }
        return this.from;
    }

    /**
     * Gets the value of the select property for the specified group name.
     */
    public From getFrom(final String group) {
        for(From s: getFrom()) {
            if (group != null && group.equals(s.getGroup())) {
                return s;
            }
        }
        return null;
    }

    /**
     * Add a FROM clause to the query.
     *
     * @param select a SQL FROM clause
     */
    public void addFrom(final From from) {
        this.getFrom().add(from);
    }
    
    /**
     * Gets the value of the where property.
     */
    public List<Where> getWhere() {
        if (where == null) {
            where = new ArrayList<Where>();
        }
        return this.where;
    }

    /**
     * Gets the value of the where property for the specified group name.
     */
    public Where getWhere(final String group) {
        for (Where s: getWhere()) {
            if (group != null && group.equals(s.getGroup())) {
                return s;
            }
        }
        return null;
    }

    /**
     * Gets all the value of the where property for the specified group name.
     */
    public List<Where> getAllWhere(final String group) {
        final List<Where> result = new ArrayList<Where>();
        for (Where s: getWhere()) {
            if (group != null && group.equals(s.getGroup())) {
                result.add(s);
            }
        }
        return result;
    }

    /**
     * Add a WHERE clause to the query.
     *
     * @param select a SQL WHERE clause
     */
    public void addWhere(final Where where) {
        this.getWhere().add(where);
    }
    
    /**
    /**
     * Gets the value of the orderby property.
     */
    public List<Orderby> getOrderby() {
        if (orderBy == null) {
            orderBy = new ArrayList<Orderby>();
        }
        return this.orderBy;
    }

    /**
     * Gets the value of the where property for the specified group name.
     */
    public Orderby getOrderby(final String group) {
        for (Orderby o: getOrderby()) {
            if (group != null && group.equals(o.getGroup())) {
                return o;
            }
        }
        return null;
    }

    /**
     * Add a ORDERBY clause to the query.
     *
     * @param select a SQL ORDERBY clause
     */
    public void addOrderby(final Orderby orderby) {
        this.getOrderby().add(orderby);
    }

    
    /**
     * @return the union
     */
    public Union getUnion() {
        return union;
    }

    /**
     * @param union the union to set
     */
    public void setUnion(Union union) {
        this.union = union;
    }

    /**
     * @return the groupby
     */
    public List<Groupby> getGroupby() {
        return groupby;
    }

    /**
     * @param groupby the groupby to set
     */
    public void setGroupby(List<Groupby> groupby) {
        this.groupby = groupby;
    }
    
    /**
     * @return the parameters
     */
    public HashMap<String, String> getParameters() {
        return parameters;
    }

    /**
     * @param parameters the parameters to set
     */
    public void setParameters(final HashMap<String, String> parameters) {
        this.parameters = parameters;
    }
    
    /**
     * @return the leftJoin
     */
    public List<LeftJoin> getLeftJoin() {
        if (leftJoin == null) {
            leftJoin = new ArrayList<LeftJoin>();
        }
        return leftJoin;
    }

    /**
     * @param leftJoin the leftJoin to set
     */
    public void setLeftJoin(final List<LeftJoin> leftJoin) {
        this.leftJoin = leftJoin;
    }
    
    /**
     * Gets the value of the where property for the specified group name.
     */
    public LeftJoin getLeftJoin(final String group) {
        for (LeftJoin s: getLeftJoin()) {
            if (group != null && group.equals(s.getGroup())) {
                return s;
            }
        }
        return null;
    }

    /**
     * Gets all the value of the LeftJoin property for the specified group name.
     */
    public List<LeftJoin> getAllLeftJoin(final String group) {
        final List<LeftJoin> result = new ArrayList<LeftJoin>();
        for (LeftJoin s: getLeftJoin()) {
            if (group != null && group.equals(s.getGroup())) {
                result.add(s);
            }
        }
        return result;
    }

    /**
     * Add a LeftJoin clause to the query.
     *
     * @param select a SQL LeftJoin clause
     */
    public void addLeftJoin(final LeftJoin leftJoin) {
        this.getLeftJoin().add(leftJoin);
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
    
    /**
     * Build the SQL query text by using the different clause contained in this object.
     *
     * @return A SQL query string.
     */
    public String buildSQLQuery() {
        return buildSQLQuery(new HashMap<String, String>());
    }
    /**
     * Return an textual SQL query for a preparedStatement (contains '?').
     * 
     * @param staticParameters A map of varName/varValue to replace in the SQL text.
     * @return
     */
    public String buildSQLQuery(HashMap<String, String> staticParameters) {
        if (staticParameters == null) {
            staticParameters = new HashMap<String, String>();
        }
        StringBuilder mainQuery = new StringBuilder("SELECT ");
        for (Select sel : select) {
            for (Column col : sel.getCol()) {
                String varName        = col.getVar();
                final String varValue = col.getSql();
                if (varName != null && !"*".equals(varValue)) {
                    if (":$".equals(varName)) {
                        varName = "ID";
                    }
                    mainQuery.append(varValue).append(" AS ").append(varName).append(',');
                } else {
                    mainQuery.append(varValue).append(',');
                }
            }
        }
        mainQuery = mainQuery.deleteCharAt(mainQuery.length() - 1);

        if (from != null && from.size() > 0) {
            String sql = "";
            for (From f :from) {
                sql = sql + f.getvalue();
                sql = sql.replace("':$'", "?");
                sql = sql.replace(":$", "?");
            }
            mainQuery.append(" FROM ").append(sql);
        } else {
            throw new IllegalArgumentException("The query :" + name + " is malformed, FROM part missing");
        }

        if (leftJoin != null) {
            mainQuery.append('\n');
            for (LeftJoin s : leftJoin) {
                mainQuery.append("LEFT JOIN ").append(s.getvalue()).append('\n');
            }
            if (leftJoin.size() > 0) {
               mainQuery = mainQuery.delete(mainQuery.length() - 1, mainQuery.length());
            }
        }
        
        final String varBegin = ":${";
        if (where != null) {
            mainQuery.append("\n WHERE ");
            boolean oRblock = false;
            for (int i = 0; i < where.size(); i++) {
                final Where w = where.get(i);

                String sql = w.getvalue();
                while (sql.indexOf(varBegin) != -1 && sql.indexOf('}') != -1) {
                    final String paramName = sql.substring(sql.indexOf(varBegin) + 3, sql.indexOf('}'));
                    final String paramValues = staticParameters.get(paramName);
                    if (paramValues != null) {
                        final String s = sql.substring(sql.indexOf(varBegin), sql.indexOf('}') + 1);
                        sql = sql.replace(s, paramValues);

                    } else {
                        final String s = sql.substring(sql.indexOf(varBegin), sql.indexOf('}') + 1);
                        sql = sql.replace(s, "?");
                    }
                }
                sql        = sql.replace("':$'", "?");
                sql        = sql.replace(":$", "?");
                
                final String block = '(' + sql + ')';
                
                if (i + 1 < where.size()) {
                    if (where.get(i + 1).getGroup().equals(w.getGroup()) && (!"AND".equals(w.getOperator()))) {
                        if (oRblock) {
                            mainQuery.append(block).append(" OR ");
                        } else {
                            mainQuery.append('(').append(block).append(" OR ");
                            oRblock = true;
                        }
                    } else {
                        if (oRblock) {
                            mainQuery.append(block).append(") AND ");
                            oRblock = false;
                        } else {
                            mainQuery.append(block).append(" AND ");
                        }
                    }
                } else {
                    mainQuery.append(block);
                }
            }
            if (oRblock) {
                mainQuery.append(')');
            }
        }
        if (orderBy != null && !orderBy.isEmpty()) {
            mainQuery.append(" ORDER BY ");
            for (Orderby ob : orderBy) {
                String sql = ob.getvalue();
                sql = sql.replace("':$'", "?");
                mainQuery.append(sql).append(" ");
            }
        }
        if (groupby != null && !groupby.isEmpty()) {
            mainQuery.append(" GROUP BY ");
            for (Groupby gb : groupby) {
                String sql = gb.getvalue();
                sql = sql.replace("':$'", "?");
                mainQuery.append(sql).append(" ");
            }
        }

        if (union != null) {
            mainQuery.append(" UNION ").append(union.getQuery().buildSQLQuery());
        }
        return mainQuery.toString();
    }
    
    @Override
    public String toString() {
        final StringBuilder s = new StringBuilder("[Query]");
        if (name != null)
            s.append("name: ").append(name).append('\n');
        if (select != null)
            s.append("select:").append(select).append('\n');
        if (option != null)
            s.append("option: ").append(option).append('\n');
        if (from != null && !from.isEmpty()) {
            int i = 0;
            for (From f: from) {
                s.append(i).append(':').append(f).append('\n');
                i++;
            }
        }
       if ( where != null &&  !where.isEmpty()) {
            int i = 0;
            for (Where f:  where) {
                s.append(i).append(':').append(f).append('\n');
                i++;
            }
        }
        if ( leftJoin != null &&  !leftJoin.isEmpty()) {
            int i = 0;
            for (LeftJoin f:  leftJoin) {
                s.append(i).append(':').append(f).append('\n');
                i++;
            }
        }
        if (orderBy != null && !orderBy.isEmpty()) {
            int i = 0;
            for (Orderby f : orderBy) {
                s.append(i).append(':').append(f).append('\n');
                i++;
            }
        }
        if (groupby != null && !groupby.isEmpty()) {
            int i = 0;
            for (Groupby f : groupby) {
                s.append(i).append(':').append(f).append('\n');
                i++;
            }
        }
        if (statique != null) {
            s.append("Statique: ").append(statique).append('\n');
        }
        if (parameters != null) {
            s.append("Parameters: ");
            for (Entry<String, String> e : parameters.entrySet()) {
                s.append(e.getKey()).append('=').append(e.getValue()).append('\n');
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
        if (object instanceof Query) {
            final Query that = (Query) object;

            return Utilities.equals(this.from,    that.from) &&
                   Utilities.equals(this.name,    that.name) &&
                   Utilities.equals(this.orderBy, that.orderBy) &&
                   Utilities.equals(this.groupby, that.groupby) && 
                   Utilities.equals(this.leftJoin, that.leftJoin) && 
                   Utilities.equals(this.parameters, that.parameters) && 
                   Utilities.equals(this.statique, that.statique) && 
                   Utilities.equals(this.select,  that.select) &&
                   Utilities.equals(this.where,   that.where) &&
                   Utilities.equals(this.option,  that.option);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.select != null ? this.select.hashCode() : 0);
        hash = 29 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 29 * hash + (this.option != null ? this.option.hashCode() : 0);
        hash = 29 * hash + (this.from != null ? this.from.hashCode() : 0);
        hash = 29 * hash + (this.where != null ? this.where.hashCode() : 0);
        hash = 29 * hash + (this.leftJoin != null ? this.leftJoin.hashCode() : 0);
        hash = 29 * hash + (this.statique != null ? this.statique.hashCode() : 0);
        hash = 29 * hash + (this.parameters != null ? this.parameters.hashCode() : 0);
        hash = 29 * hash + (this.orderBy != null ? this.orderBy.hashCode() : 0);
        hash = 29 * hash + (this.groupby != null ? this.groupby.hashCode() : 0);
        return hash;
    }

}
