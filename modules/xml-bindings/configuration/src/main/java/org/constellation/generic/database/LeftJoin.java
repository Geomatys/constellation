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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Objects;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @Since 0.8
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "value"
})
public class LeftJoin {

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
    public LeftJoin() {

    }

    /**
     * Simple constructor with a single value.
     * @param value
     */
    public LeftJoin(String value) {
        this.value = value;
    }

    /**
     * clone a LeftJoin object.
     *
     * @param original the where object to clone.
     */
    public LeftJoin(LeftJoin original) {
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
        final StringBuilder sb = new StringBuilder("[LeftJoin]:").append('\n');
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
        if (object instanceof LeftJoin) {
            final LeftJoin that = (LeftJoin) object;

            return Objects.equals(this.operator, that.operator) &&
                   Objects.equals(this.group, that.group) &&
                   Objects.equals(this.value, that.value);
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
