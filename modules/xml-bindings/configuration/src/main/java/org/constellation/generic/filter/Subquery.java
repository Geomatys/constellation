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
    "subselect",
    "from",
    "where",
    "union",
    "complement",
    "orderby",
    "groupby"
})
@XmlRootElement(name = "subquery")
public class Subquery {

    @XmlAttribute
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String option;
    @XmlElement(required = true)
    private List<Subselect> subselect;
    @XmlElement(required = true)
    private List<From> from;
    private List<Where> where;
    private List<Union> union;
    private List<Complement> complement;
    private List<Orderby> orderby;
    private List<Groupby> groupby;

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
     * Gets the value of the subselect property.
     */
    public List<Subselect> getSubselect() {
        if (subselect == null) {
            subselect = new ArrayList<Subselect>();
        }
        return this.subselect;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[SubQuery]:").append('\n');
        if (option != null) {
            sb.append(" option: ").append(option);
        }
        if (from != null) {
            sb.append("SUBSELECT: ");
            for (From s : from) {
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
