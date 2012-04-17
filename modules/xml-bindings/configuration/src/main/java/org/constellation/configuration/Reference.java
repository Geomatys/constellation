/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010, Geomatys
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

package org.constellation.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;
import org.geotoolkit.util.Utilities;

/**
 *
 * @author Guilhem Legal (Geomatys)
 * @since 0.6
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Reference {

    @XmlAttribute(namespace="http://www.w3.org/1999/xlink")
    private String href;

    @XmlAttribute
    private String authority;

    @XmlValue
    private String value;

    public Reference() {
        
    }

    public Reference(final String href) {
        this.href      = href;
    }

    public Reference(final String authority, final String value) {
        this.authority = authority;
        this.value     = value;
    }
    
    public Reference(final String href, final String authority, final String value) {
        this.href      = href;
        this.authority = authority;
        this.value     = value;
    }

    /**
     * @return the href
     */
    public String getHref() {
        return href;
    }

    /**
     * @param href the href to set
     */
    public void setHref(String href) {
        this.href = href;
    }

    /**
     * @return the authority
     */
    public String getAuthority() {
        return authority;
    }

    /**
     * @param authority the authority to set
     */
    public void setAuthority(String authority) {
        this.authority = authority;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("[Reference]");
        if (authority != null) {
            sb.append("authority=").append(authority).append('\n');
        }
        if (href != null) {
            sb.append("href=").append(href).append('\n');
        }
        if (value != null) {
            sb.append("value=").append(value).append('\n');
        }
        return sb.toString();
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Reference) {
            final Reference that = (Reference) obj;
            return Utilities.equals(this.authority, that.authority) &&
                   Utilities.equals(this.href,      that.href) &&
                   Utilities.equals(this.value,     that.value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (this.href != null ? this.href.hashCode() : 0);
        hash = 97 * hash + (this.authority != null ? this.authority.hashCode() : 0);
        hash = 97 * hash + (this.value != null ? this.value.hashCode() : 0);
        return hash;
    }

}
