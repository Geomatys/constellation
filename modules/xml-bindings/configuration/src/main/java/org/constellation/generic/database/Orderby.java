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
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
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
@XmlRootElement(name = "orderBy")
public class Orderby {

    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String sens;
    @XmlAttribute
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String group;
    @XmlValue
    private String value;

    public Orderby() {

    }

    public Orderby(final Orderby orderBy) {
        if (orderBy != null) {
            this.group  = orderBy.group;
            this.sens   = orderBy.sens;
            this.value  = orderBy.value;
        }
    }

    /**
     * Gets the value of the sens property.
     */
    public String getSens() {
        return sens;
    }

    /**
     * Sets the value of the sens property.
     */
    public void setSens(String value) {
        this.sens = value;
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
        final StringBuilder s = new StringBuilder("[Orderby]");
        if (sens != null)
            s.append("sens: ").append(sens).append('\n');
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
        if (object instanceof Orderby) {
            final Orderby that = (Orderby) object;

            return Objects.equals(this.sens,  that.sens)  &&
                   Objects.equals(this.group, that.group) &&
                   Objects.equals(this.value, that.value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 43 * hash + (this.sens != null ? this.sens.hashCode() : 0);
        hash = 43 * hash + (this.group != null ? this.group.hashCode() : 0);
        hash = 43 * hash + (this.value != null ? this.value.hashCode() : 0);
        return hash;
    }

}
