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

package org.constellation.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class DataSourceType {

    private static final List<DataSourceType> VALUES = new ArrayList<>();

    public static final DataSourceType INTERNAL = new DataSourceType("internal");

    public static final DataSourceType FILESYSTEM = new DataSourceType("filesystem");

    public static final DataSourceType NETCDF = new DataSourceType("netcdf");

    public static final DataSourceType MDWEB = new DataSourceType("mdweb");

    public static final DataSourceType POSTGRID = new DataSourceType("postgrid");

    public static final DataSourceType GENERIC = new DataSourceType("generic");

    public static final DataSourceType LUCENE = new DataSourceType("lucene");
    
    public static final DataSourceType OM2 = new DataSourceType("om2");

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
            return Objects.equals(this.name, that.name);
        }
        return false;
    }

    @Override
    public String toString() {
        return name;
    }
}
