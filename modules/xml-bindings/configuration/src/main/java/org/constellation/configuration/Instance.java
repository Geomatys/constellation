/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2011, Geomatys
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
import javax.xml.bind.annotation.XmlRootElement;
import org.geotoolkit.util.Utilities;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@XmlRootElement(name ="Instance")
@XmlAccessorType(XmlAccessType.FIELD)
public class Instance {

    @XmlAttribute
    private String name;

    @XmlAttribute
    private ServiceStatus status;

    public Instance() {

    }

    public Instance(final String name, final ServiceStatus status) {
        this.name   = name;
        this.status = status;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the status
     */
    public ServiceStatus getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Instance) {
            final Instance that = (Instance) obj;
            return Utilities.equals(this.name, that.name) &&
                   Utilities.equals(this.status, that.status);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 73 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 73 * hash + (this.status != null ? this.status.hashCode() : 0);
        return hash;
    }

    

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("[Instance]\n");
        if (name != null) {
            sb.append("name:").append(name).append('\n');
        }
        if (status != null) {
            sb.append("status:").append(status).append('\n');
        }
        return sb.toString();
    }
}
