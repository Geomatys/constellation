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

import java.io.File;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.geotools.util.Utilities;

/**
 *
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "automatic")
public class Automatic {
    
    @XmlTransient
    public static final int DEFAULT     = 0;
    @XmlTransient
    public static final int CSR         = 1;
    @XmlTransient
    public static final int CDI         = 2;
    @XmlTransient
    public static final int EDMED       = 3;
    @XmlTransient
    public static final int MDWEB       = 4;
    @XmlTransient
    public static final int FILESYSTEM  = 5;
            
    private BDD bdd;
    
    @XmlAttribute
    private String format;
    
    private String dataDirectory;
    
    private Queries queries;

    public Queries getQueries() {
        return queries;
    }

    public BDD getBdd() {
        return bdd;
    }

    public String getFormat() {
        return format;
    }
    
    public File getdataDirectory() {
        if (dataDirectory != null)
            return new File(dataDirectory);
        else return null;
    }
    
    public int getType() {
        if (format.equals("cdi"))
            return CDI;
        else if (format.equals("csr"))
            return CSR;
        else if (format.equals("edmed"))
            return EDMED;
        else if (format.equals("mdweb"))
            return MDWEB;
        else if (format.equals("filesystem"))
            return FILESYSTEM;
        else
            return DEFAULT;
    }
    
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("[Automatic]");
        s.append("format: ").append(format).append('\n');
        s.append("BDD:").append(bdd).append('\n');
        s.append("dataDirectory:").append(bdd).append('\n');
        s.append("queries ").append(": ").append(queries).append('\n');
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
        if (object instanceof Automatic) {
            final Automatic that = (Automatic) object;

            return Utilities.equals(this.bdd,   that.bdd)         &&
                   Utilities.equals(this.format  ,   that.format) &&
                   Utilities.equals(this.queries, that.queries);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.bdd != null ? this.bdd.hashCode() : 0);
        hash = 37 * hash + (this.format != null ? this.format.hashCode() : 0);
        hash = 37 * hash + (this.queries != null ? this.queries.hashCode() : 0);
        return hash;
    }

}
