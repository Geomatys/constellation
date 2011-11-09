/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2009, Geomatys
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
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlValue;

import org.geotoolkit.util.Utilities;

/**
 *
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class DataSourceType {

    private static final List<DataSourceType> VALUES = new ArrayList<DataSourceType>();
    
    public static final DataSourceType FILESYSTEM = new DataSourceType("filesystem");
    
    public static final DataSourceType NETCDF = new DataSourceType("netcdf");

    public static final DataSourceType MDWEB = new DataSourceType("mdweb");

    public static final DataSourceType POSTGRID = new DataSourceType("postgrid");

    public static final DataSourceType GENERIC = new DataSourceType("generic");
    
    public static final DataSourceType LUCENE = new DataSourceType("lucene");

    public static final DataSourceType NONE = new DataSourceType("none");

    @XmlValue
    private final String name;
    
    public DataSourceType() {
        this.name = null;
    }
    
    public DataSourceType(final String name) {
        this.name = name;
        if (!VALUES.contains(this)) {
            VALUES.add(this);
        }
    }
    
    public static DataSourceType fromName(final String name) {
        for (DataSourceType o : VALUES) {
            if (o.getName().equalsIgnoreCase(name)) {
                return o;
            }
        }
        return null;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof DataSourceType) {
            DataSourceType that = (DataSourceType)obj;
            return Utilities.equals(this.name, that.name);
        }
        return false;
    }
    
    public String toString() {
        return name;
    }
}
