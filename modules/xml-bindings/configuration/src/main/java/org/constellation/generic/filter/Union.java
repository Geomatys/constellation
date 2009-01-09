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
    "subquery"
})
@XmlRootElement(name = "union")
public class Union {

    @XmlAttribute
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String group;
    @XmlElement(required = true)
    private Subquery subquery;

    /**
     * Gets the value of the group property.
     */
    public String getGroup() {
        return group;
    }

    /**
     * Sets the value of the group property.
     */
    public void setGroup(String value) {
        this.group = value;
    }

    /**
     * Gets the value of the subquery property.
     */
    public Subquery getSubquery() {
        return subquery;
    }

    /**
     * Sets the value of the subquery property.
     */
    public void setSubquery(Subquery value) {
        this.subquery = value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[UNION]:").append('\n');
        if (subquery != null) {
            sb.append("subQuery: ").append(subquery).append('\n');
        }
        if (group != null) {
            sb.append("group: ").append(group).append('\n');
        }
        return sb.toString();
     }

}
