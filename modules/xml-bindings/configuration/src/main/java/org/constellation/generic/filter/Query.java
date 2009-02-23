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

package org.constellation.generic.filter;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "paramsSql",
    "select",
    "nestedSelect",
    "from",
    "where",
    "union",
    "complement",
    "orderby",
    "groupby"
})
@XmlRootElement(name = "query")
public class Query {

    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String name;
    @XmlAttribute
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String option;
    @XmlElement(name = "params_sql")
    private ParamsSql paramsSql;
    @XmlElement(required = true)
    private List<Select> select;
    @XmlElement(name = "nested_select")
    private List<NestedSelect> nestedSelect;
    @XmlElement(required = true)
    private List<From> from;
    private List<Where> where;
    private List<Union> union;
    private List<Complement> complement;
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
     * Gets the value of the paramsSql property.
     */
    public ParamsSql getParamsSql() {
        return paramsSql;
    }

    /**
     * Sets the value of the paramsSql property.
     */
    public void setParamsSql(ParamsSql value) {
        this.paramsSql = value;
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
     * Gets the value of the nestedSelect property.
     */
    public List<NestedSelect> getNestedSelect() {
        if (nestedSelect == null) {
            nestedSelect = new ArrayList<NestedSelect>();
        }
        return this.nestedSelect;
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
        for(Where s: getWhere()) {
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
     * Gets the value of the union property.
     */
    public List<Union> getUnion() {
        if (union == null) {
            union = new ArrayList<Union>();
        }
        return this.union;
    }

    /**
     * Gets the value of the complement property.
     */
    public List<Complement> getComplement() {
        if (complement == null) {
            complement = new ArrayList<Complement>();
        }
        return this.complement;
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
     * Gets the value of the groupby property.
     */
    public List<Groupby> getGroupby() {
        if (groupby == null) {
            groupby = new ArrayList<Groupby>();
        }
        return this.groupby;
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
            boolean ORblock = false;
            for (int i = 0; i < where.size(); i++) {
                Where w = where.get(i);
                String block = '(' + w.getvalue() + ')';
                if (i + 1 < where.size()) {
                    if (where.get(i + 1).getGroup().equals(w.getGroup())) {
                        if (ORblock) {
                            sb.append(block).append(" OR ");
                        } else {
                            sb.append('(').append(block).append(" OR ");
                            ORblock = true;
                        }
                    } else {
                        if (ORblock) {
                            sb.append(block).append(") AND ");
                            ORblock = false;
                        } else {
                            sb.append(block).append(" AND ");
                        }
                    }
                } else {
                    sb.append(block);
                }
            }
            
        }
        /*if (union != null) {
            sb.append("UNION ");
            for (Union s : union) {
                sb.append(s).append(" ");
            }
        }
        if (complement != null) {
            sb.append("COMPLEMENT: ");
            for (Complement s : complement) {
                sb.append(s).append(" ");
            }
        }*/
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
        StringBuilder sb = new StringBuilder("[Query]:").append('\n');
        sb.append("name: ").append(name);
        if (option != null) {
            sb.append(" option: ").append(option);
        }
        if (paramsSql != null) {
            sb.append("param SQL: ").append(paramsSql);
        }
        if (select != null) {
            sb.append("SELECT: ");
            for (Select s : select) {
                sb.append(s).append('\n');
            }
        }
        if (nestedSelect != null) {
            sb.append("NESTED SELECT: ");
            for (NestedSelect s : nestedSelect) {
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
        if (union != null) {
            sb.append("UNION: ");
            for (Union s : union) {
                sb.append(s).append('\n');
            }
        }
        if (complement != null) {
            sb.append("COMPLEMENT: ");
            for (Complement s : complement) {
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
}
