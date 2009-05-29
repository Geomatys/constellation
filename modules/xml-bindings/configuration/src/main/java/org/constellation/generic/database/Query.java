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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.geotoolkit.util.Utilities;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "query")
public class Query {

    @XmlElement(required = true)
    private Select select;
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String name;
    @XmlAttribute
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String option;
    @XmlElement(required = true)
    private List<From> from;
    private List<Where> where;
    private List<Orderby> orderBy;
    private Union union;

    public Query() {

    }

    public Query(String name, Select select, From from) {
        this.name   = name;
        this.select = select;
        this.from   = Arrays.asList(from);
    }

    public Query(String name, Select select, From from, Where where) {
        this.name   = name;
        this.select = select;
        this.from   = Arrays.asList(from);
        this.where  = Arrays.asList(where);
    }

    public Query(String name, Select select, From from, Where where, Orderby orderBy) {
        this.name    = name;
        this.select  = select;
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
    public Select getSelect() {
        return this.select;
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
     * Gets the value of the where property.
     */
    public List<Where> getWhere() {
        if (where == null) {
            where = new ArrayList<Where>();
        }
        return this.where;
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

    public String buildSQLQuery() {
        return buildSQLQuery(new HashMap<String, String>());
    }
    /**
     * Return an textual SQL query for a preparedStatement (contains '?').
     * 
     * @param query
     * @return
     */
    public String buildSQLQuery(HashMap<String, String> staticParameters) {
        if (staticParameters == null) {
            staticParameters = new HashMap<String, String>();
        }
        StringBuilder mainQuery = new StringBuilder("SELECT ");

        for (Column col : select.getCol()) {
            String varName        = col.getVar();
            final String varValue = col.getSql();
            if (varName != null && !varValue.equals("*")) {
                if (varName.equals(":$"))
                    varName = "ID";
                mainQuery.append(varValue).append(" AS ").append(varName).append(',');
            } else {
                mainQuery.append(varValue).append(',');
            }
        }
        mainQuery = mainQuery.deleteCharAt(mainQuery.length() - 1);

        if (from != null && from.size() > 0) {
            mainQuery.append(" FROM ").append(from.get(0).getvalue());
        } else {
            throw new IllegalArgumentException("The query :" + name + " is malformed, FROM part missing");
        }

        if (where != null && where.size() > 0 && where.get(0) != null && !where.get(0).getvalue().equals("")) {
            String sql = where.get(0).getvalue();
            while (sql.indexOf(":${") != -1 && sql.indexOf("}") != -1) {
                String paramName = sql.substring(sql.indexOf(":${") + 3, sql.indexOf("}"));
                String paramValues = staticParameters.get(paramName);
                if (paramValues != null) {
                    String s = sql.substring(sql.indexOf(":${"), sql.indexOf("}") + 1);
                    sql = sql.replace(s, paramValues);
                    
                } else {
                    String s = sql.substring(sql.indexOf(":${"), sql.indexOf("}") + 1);
                    sql = sql.replace(s, "?");
                }
            }
            sql = sql.replace("':$'", "?");
            sql = sql.replace(":$", "?");
            mainQuery.append(" WHERE ").append(sql);
        }
        if (orderBy != null && orderBy.size() > 0 && orderBy.get(0) != null && !orderBy.get(0).getvalue().equals("")) {
            String sql = orderBy.get(0).getvalue();
            sql = sql.replace("':$'", "?");
            mainQuery.append(" ORDER BY ").append(sql);
        }

        if (union != null) {
            mainQuery.append(" UNION ").append(union.getQuery().buildSQLQuery());
        }
        return mainQuery.toString();
    }
    
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("[Query]");
        if (name != null)
            s.append("name: ").append(name).append('\n');
        if (select != null)
            s.append("select:").append(select).append('\n');
        if (option != null)
            s.append("option: ").append(option).append('\n');
        if (from != null && from.size() != 0) {
            int i = 0;
            for (From f: from) {
                s.append(i).append(':').append(f).append('\n');
                i++;
            }
        }
       if ( where != null &&  where.size() != 0) {
            int i = 0;
            for (Where f:  where) {
                s.append(i).append(':').append(f).append('\n');
                i++;
            }
        }
        if (orderBy != null && orderBy.size() != 0) {
            int i = 0;
            for (Orderby f : orderBy) {
                s.append(i).append(':').append(f).append('\n');
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
        if (object instanceof Query) {
            final Query that = (Query) object;

            return Utilities.equals(this.from,    that.from) &&
                   Utilities.equals(this.name,    that.name) &&
                   Utilities.equals(this.orderBy, that.orderBy) &&
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
        hash = 29 * hash + (this.orderBy != null ? this.orderBy.hashCode() : 0);
        return hash;
    }

}
