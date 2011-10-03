/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2010, Geomatys
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
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.geotoolkit.util.Utilities;

/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "parameters",
    "statique",
    "select",
    "from",
    "leftJoin",
    "where",
    "orderby",
    "groupby"
})
@XmlRootElement(name = "query", namespace="http://constellation.generic.filter.org")
public class FilterQuery {

    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String name;
    @XmlAttribute
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String option;
    @XmlElement(required = true)
    private List<Select> select;
    private List<LeftJoin> leftJoin;
    @XmlElement(required = true)
    private List<From> from;
    private List<Where> where;
    private List<Orderby> orderby;
    private List<Groupby> groupby;

    private HashMap<String, String> parameters;

    private QueryList statique;


    /**
     * Gets the value of the name property.
     */
    public String getName() {
        return name;
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
        return this.select;
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
     * Gets the value of the orderby property.
     */
    public List<Orderby> getOrderby() {
        if (orderby == null) {
            orderby = new ArrayList<Orderby>();
        }
        return this.orderby;
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
     * Gets the value of the groupby property.
     */
    public List<Groupby> getGroupby() {
        if (groupby == null) {
            groupby = new ArrayList<Groupby>();
        }
        return this.groupby;
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
     * Build the concrete SQL text query by combinating all the clause contained in this object.
     * @return An SQL query.
     */
    public String buildSQLQuery() {
        StringBuilder sb = new StringBuilder();
        if (select != null) {
            sb.append("SELECT ");
            for (Select s : select) {
                for (Column col : s.getCol()) {
                    String varName        = col.getVar();
                    final String varValue = col.getSql();
                    if (varName != null) {
                        sb.append(varValue).append(" AS ").append(varName).append(',');
                    } else {
                        sb.append(varValue).append(',');
                    }
                }
                if (!s.getCol().isEmpty()) {
                    sb.deleteCharAt(sb.length() - 1);
                }
            }
        }
        if (from != null) {
            sb.append('\n');
            sb.append(" FROM ");
            for (From s : from) {
                sb.append(s.getvalue()).append(" , ");
            }
            if (from.size() > 0)
               sb = sb.delete(sb.length() - 3, sb.length());
        }
        if (leftJoin != null) {
            sb.append('\n');
            for (LeftJoin s : leftJoin) {
                sb.append("LEFT JOIN ").append(s.getvalue()).append('\n');
            }
            if (leftJoin.size() > 0)
               sb = sb.delete(sb.length() - 1, sb.length());
        }
        if (where != null) {
            sb.append("\n WHERE ");
            boolean oRblock = false;
            for (int i = 0; i < where.size(); i++) {
                final Where w = where.get(i);
                final String block = '(' + w.getvalue() + ')';
                if (i + 1 < where.size()) {
                    if (where.get(i + 1).getGroup().equals(w.getGroup()) && (!"AND".equals(w.getOperator()))) {
                        if (oRblock) {
                            sb.append(block).append(" OR ");
                        } else {
                            sb.append('(').append(block).append(" OR ");
                            oRblock = true;
                        }
                    } else {
                        if (oRblock) {
                            sb.append(block).append(") AND ");
                            oRblock = false;
                        } else {
                            sb.append(block).append(" AND ");
                        }
                    }
                } else {
                    sb.append(block);
                }
            }
            if (oRblock) {
                sb.append(')');
            }
            
        }
        sb.append('\n');
        if (orderby != null) {
            sb.append(" ORDER BY ");
            for (Orderby s : orderby) {
                sb.append(s.getvalue()).append(" ");
            }
        }
        sb.append('\n');
        if (groupby != null) {
            sb.append(" GROUP BY ");
            for (Groupby s : groupby) {
                sb.append(s.getvalue()).append(" ");
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("[Query]:").append('\n');
        sb.append("name: ").append(name).append('\n');
        if (option != null) {
            sb.append(" option: ").append(option);
        }
        if (statique != null) {
            sb.append("Statique: ").append(statique).append('\n');
        }
        if (parameters != null) {
            sb.append("Parameters: ");
            for (Entry<String, String> e : parameters.entrySet()) {
                sb.append(e.getKey()).append('=').append(e.getValue()).append('\n');
            }
        }
        if (select != null) {
            sb.append("SELECT: ");
            for (Select s : select) {
                sb.append(s).append('\n');
            }
        }
        if (from != null) {
            sb.append("FROM: ");
            for (From s : from) {
                sb.append(s).append('\n');
            }
        }
        if (leftJoin != null) {
            sb.append("LEFT JOIN: ");
            for (LeftJoin s : leftJoin) {
                sb.append(s).append('\n');
            }
        }
        if (where != null) {
            sb.append("WHERE: ");
            for (Where s : where) {
                sb.append(s).append('\n');
            }
        }
        if (orderby != null) {
            sb.append("ORDER BY: ");
            for (Orderby s : orderby) {
                sb.append(s).append('\n');
            }
        }
        if (groupby != null) {
            sb.append("GROUP BY: ");
            for (Groupby s : groupby) {
                sb.append(s).append('\n');
            }
        }
        return sb.toString();
    }

    /**
     * Verify if this entry is identical to the specified object.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof FilterQuery) {
            final FilterQuery that = (FilterQuery) object;

            return Utilities.equals(this.from,       that.from) &&
                   Utilities.equals(this.name,       that.name) &&
                   Utilities.equals(this.groupby,    that.groupby) &&
                   Utilities.equals(this.select,     that.select) &&
                   Utilities.equals(this.where,      that.where) &&
                   Utilities.equals(this.leftJoin,   that.leftJoin) &&
                   Utilities.equals(this.option,     that.option) &&
                   Utilities.equals(this.orderby,    that.orderby) &&
                   Utilities.equals(this.parameters, that.parameters) &&
                   Utilities.equals(this.statique,   that.statique);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 37 * hash + (this.option != null ? this.option.hashCode() : 0);
        hash = 37 * hash + (this.parameters != null ? this.parameters.hashCode() : 0);
        hash = 37 * hash + (this.statique != null ? this.statique.hashCode() : 0);
        hash = 37 * hash + (this.select != null ? this.select.hashCode() : 0);
        hash = 37 * hash + (this.from != null ? this.from.hashCode() : 0);
        hash = 37 * hash + (this.where != null ? this.where.hashCode() : 0);
        hash = 37 * hash + (this.leftJoin != null ? this.leftJoin.hashCode() : 0);
        hash = 37 * hash + (this.orderby != null ? this.orderby.hashCode() : 0);
        hash = 37 * hash + (this.groupby != null ? this.groupby.hashCode() : 0);
        return hash;
    }
}
