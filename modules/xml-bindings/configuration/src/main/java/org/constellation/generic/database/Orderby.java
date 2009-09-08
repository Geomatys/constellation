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

            return Utilities.equals(this.sens,  that.sens)  &&
                   Utilities.equals(this.group, that.group) &&
                   Utilities.equals(this.value, that.value);
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
