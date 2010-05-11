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

package org.constellation.generic.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.constellation.generic.database.Static;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "parameters",
    "statique",
    "select",
    "from",
    "where",
    "orderby",
    "groupby"
})
@XmlRootElement(name = "query", namespace="http://constellation.generic.filter.org")
public class Query {

    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String name;
    @XmlAttribute
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String option;

    private HashMap<String, String> parameters;

    private Static statique;

    @XmlElement(required = true)
    private List<Select> select;
    @XmlElement(required = true)
    private List<From> from;
    private List<Where> where;
    private List<Orderby> orderby;
    private List<Groupby> groupby;

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
    public Select getSelect(String group) {
        for(Select s: getSelect()) {
            if (group != null && group.equals(s.getGroup())) {
                return s;
            }
        }
        return null;
    }

    public void addSelect(Select select) {
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
    public From getFrom(String group) {
        for(From s: getFrom()) {
            if (group != null && group.equals(s.getGroup())) {
                return s;
            }
        }
        return null;
    }

    public void addFrom(From from) {
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
    public Where getWhere(String group) {
        for (Where s: getWhere()) {
            if (group != null && group.equals(s.getGroup())) {
                return s;
            }
        }
        return null;
    }

    public void addWhere(Where where) {
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
    public Orderby getOrderby(String group) {
        for (Orderby o: getOrderby()) {
            if (group != null && group.equals(o.getGroup())) {
                return o;
            }
        }
        return null;
    }

    public void addOrderby(Orderby orderby) {
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
    public void setParameters(HashMap<String, String> parameters) {
        this.parameters = parameters;
    }
    

    public String buildSQLQuery() {
        StringBuilder sb = new StringBuilder();
        if (select != null) {
            sb.append("SELECT ");
            for (Select s : select) {
                sb.append(s.getvalue()).append(" , ");
            }
            if (select.size() > 0)
               sb = sb.delete(sb.length() - 3, sb.length());
        }
        if (from != null) {
            sb.append(" FROM ");
            for (From s : from) {
                sb.append(s.getvalue()).append(" , ");
            }
            if (from.size() > 0)
               sb = sb.delete(sb.length() - 3, sb.length());
        }
        if (where != null) {
            sb.append(" WHERE ");
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
            
        }
        if (orderby != null) {
            sb.append(" ORDER BY ");
            for (Orderby s : orderby) {
                sb.append(s.getvalue()).append(" ");
            }
        }
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
        sb.append("name: ").append(name);
        if (option != null) {
            sb.append(" option: ").append(option);
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
     * @return the statique
     */
    public Static getStatique() {
        return statique;
    }

    /**
     * @param statique the statique to set
     */
    public void setStatique(Static statique) {
        this.statique = statique;
    }
}
