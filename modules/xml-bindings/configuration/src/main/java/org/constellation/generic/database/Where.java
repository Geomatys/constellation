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

package org.constellation.generic.database;

import java.util.HashMap;
import java.util.Map.Entry;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.geotoolkit.util.Utilities;


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

    /**
     * Constrcutor used by JAXB.
     */
    public Where() {

    }

    /**
     * Simple constructor with a single value.
     * @param value
     */
    public Where(String value) {
        this.value = value;
    }

    /**
     * clone a where object.
     *
     * @param original the where object to clone.
     */
    public Where(Where original) {
        if (original != null) {
            this.group    = original.group;
            this.operator = original.operator;
            this.value    = original.value;
        }
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

    /**
     * Replace the specified variable in the WHERE text by the submitted value, with or without quote.
     * The variable must be precede by a "&"
     *
     * exemple: if where.value="table1.field1=&var1 AND table1.field2 = table2.field1"
     * calling replaceVariable("var1", "xxx", false) will modify where.value to :
     * "table1.field1=xxx AND table1.field2 = table2.field1"
     *
     * calling replaceVariable("var1", "xxx", false) will modify where.value to :
     * "table1.field1='xxx' AND table1.field2 = table2.field1"
     *
     * @param varName
     * @param varValue
     * @param withQuote
     */
    public void replaceVariable(String varName, String varValue, boolean withQuote) {
        if (varValue != null) {
            if (withQuote) {
                value = value.replaceAll('&' + varName, "'" + varValue + "'");
            } else {
                value = value.replaceAll('&' + varName, varValue);
            }
        }
    }



    /**
     * same mecanism as replaceVariable(String varName, String varValue, boolean withQuote)
     * but all the varName/varValue are contained in a map.
     * 
     * @param parameters a map of varName/varValue.
     */
    public void replaceVariable(HashMap<String, String> parameters) {
        for (Entry<String, String> entry : parameters.entrySet()) {
            value = value.replaceAll('&' + entry.getKey(), entry.getValue());
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("[WHERE]:").append('\n');
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

    /**
     * Verify if this entry is identical to the specified object.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof Where) {
            final Where that = (Where) object;

            return Utilities.equals(this.operator, that.operator) &&
                   Utilities.equals(this.group, that.group) &&
                   Utilities.equals(this.value, that.value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (this.operator != null ? this.operator.hashCode() : 0);
        hash = 47 * hash + (this.group != null ? this.group.hashCode() : 0);
        hash = 47 * hash + (this.value != null ? this.value.hashCode() : 0);
        return hash;
    }
}
