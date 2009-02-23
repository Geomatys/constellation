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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "value"
})
@XmlRootElement(name = "where")
public class Where {

    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String operator;
    @XmlAttribute
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String group;
    @XmlValue
    private String value;

    public Where() {

    }

    public Where(Where original) {
        this.group    = original.group;
        this.operator = original.operator;
        this.value    = original.value;
    }

    /**
     * Gets the value of the operator property.
     */
    public String getOperator() {
        return operator;
    }

    /**
     * Sets the value of the operator property.
     */
    public void setOperator(String value) {
        this.operator = value;
    }

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
     * Gets the value of the value property.
     */
    public String getvalue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     */
    public void setvalue(String value) {
        this.value = value;
    }

    public void replaceVariable(String varName, String varValue, boolean withQuote) {
        if (withQuote)
            value = value.replaceAll('&' + varName, "'" + varValue + "'");
        else
            value = value.replaceAll('&' + varName, varValue);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[WHERE]:").append('\n');
        if (value != null) {
            sb.append("value: ").append(value).append('\n');
        }
        if (group != null) {
            sb.append("group: ").append(group).append('\n');
        }
        if (operator != null) {
            sb.append("operator: ").append(operator).append('\n');
        }
        return sb.toString();
     }
}
