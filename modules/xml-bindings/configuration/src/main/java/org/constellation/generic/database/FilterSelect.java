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
@XmlRootElement(name = "select")
public class FilterSelect {

    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String alias;
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String type;
    @XmlAttribute
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String tooltip;
    @XmlAttribute
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String align;
    @XmlAttribute
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String width;
    @XmlAttribute
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String group;
    @XmlValue
    private String value;

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
     * Gets the value of the type property.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the tooltip property.
     */
    public String getTooltip() {
        return tooltip;
    }

    /**
     * Sets the value of the tooltip property.
     */
    public void setTooltip(String value) {
        this.tooltip = value;
    }

    /**
     * Gets the value of the align property.
     */
    public String getAlign() {
        return align;
    }

    /**
     * Sets the value of the align property.
     */
    public void setAlign(String value) {
        this.align = value;
    }

    /**
     * Gets the value of the width property.
     */
    public String getWidth() {
        return width;
    }

    /**
     * Sets the value of the width property.
     */
    public void setWidth(String value) {
        this.width = value;
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
        final StringBuilder sb = new StringBuilder("[Select]:").append('\n');
        if (value != null) {
            sb.append("value: ").append(value).append('\n');
        }
        if (alias != null) {
            sb.append("alias: ").append(alias).append('\n');
        }
        if (align != null) {
            sb.append("align: ").append(align).append('\n');
        }
        if (group != null) {
            sb.append("group: ").append(group).append('\n');
        }
        if (tooltip != null) {
            sb.append("tooltip: ").append(tooltip).append('\n');
        }
        if (type != null) {
            sb.append("type: ").append(type).append('\n');
        }
        if (width != null) {
            sb.append("width: ").append(width).append('\n');
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
        if (object instanceof FilterSelect) {
            final FilterSelect that = (FilterSelect) object;

            return Utilities.equals(this.alias, that.alias) &&
                   Utilities.equals(this.align, that.align) &&
                   Utilities.equals(this.group, that.group) &&
                   Utilities.equals(this.tooltip, that.tooltip) &&
                   Utilities.equals(this.type, that.type) &&
                   Utilities.equals(this.value, that.value) &&
                   Utilities.equals(this.width, that.width);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + (this.alias != null ? this.alias.hashCode() : 0);
        hash = 79 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 79 * hash + (this.tooltip != null ? this.tooltip.hashCode() : 0);
        hash = 79 * hash + (this.align != null ? this.align.hashCode() : 0);
        hash = 79 * hash + (this.width != null ? this.width.hashCode() : 0);
        hash = 79 * hash + (this.group != null ? this.group.hashCode() : 0);
        hash = 79 * hash + (this.value != null ? this.value.hashCode() : 0);
        return hash;
    }
}
