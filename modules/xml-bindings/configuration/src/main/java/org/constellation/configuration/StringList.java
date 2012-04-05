/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.geotoolkit.util.Utilities;

/**
 *
 * @author Johann Sorel (Geomatys)
 * @since 0.8
 */
@XmlRootElement(name="StringList")
@XmlAccessorType(XmlAccessType.FIELD)
public class StringList {

    @XmlElement(name="Entry")
    private Collection<String> list;
    
    public StringList() {
        
    }
    
    public StringList(final Collection<String> list) {
        this.list = list;
    }

    public Collection<String> getList() {
        if(list == null){
            list = new ArrayList<String>();
        }
        return list;
    }

    public void setList(final Collection<String> list) {
        this.list = list;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("[StringList]:\n");
        if (list != null) {
            for (String s : list) {
                sb.append(s).append(",");
            }
        }
        return sb.toString();
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof StringList) {
            final StringList that = (StringList) obj;
            return Utilities.equals(this.list, that.list);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + (this.list != null ? this.list.hashCode() : 0);
        return hash;
    }
}
