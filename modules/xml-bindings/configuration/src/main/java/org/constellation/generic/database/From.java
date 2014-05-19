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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Objects;


/**
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "value"
})
@XmlRootElement(name = "from")
public class From {

    @XmlAttribute
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String alias;
    @XmlAttribute
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String group;
    @XmlValue
    private String value;

    /**
     * Empty constrcutor used by JAXB..
     */
    public From() {

    }

    public From(final From from) {
        if (from != null) {
            this.alias = from.alias;
            this.group = from.group;
            this.value = from.value;
        }
    }


    /**
     * Build a FROM clause with the value.
     * @param value the string to add after the FROM in th SQL request.
     */
    public From(final String value) {
        this.value = value;
    }

    /**
     * Gets the value of the alias property.
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Sets the value of the alias property.
     */
    public void setAlias(String value) {
        this.alias = value;
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

    @Override
    public String toString() {
        final StringBuilder s = new StringBuilder("[From]");
        if (alias != null)
            s.append("alias: ").append(alias).append('\n');
        if (group != null)
            s.append("group:").append(group).append('\n');
        if (value != null)
            s.append("value : ").append(value).append('\n');
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
        if (object instanceof From) {
            final From that = (From) object;

            return Objects.equals(this.alias, that.alias) &&
                   Objects.equals(this.group, that.group) &&
                   Objects.equals(this.value, that.value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + (this.alias != null ? this.alias.hashCode() : 0);
        hash = 37 * hash + (this.group != null ? this.group.hashCode() : 0);
        hash = 37 * hash + (this.value != null ? this.value.hashCode() : 0);
        return hash;
    }

}
