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
import org.geotools.util.Utilities;

/**
 *
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class BDD {

    private String className;
    
    private String connectURL;
    
    private String user;
    
    private String password;

    public String getClassName() {
        return className;
    }

    public String getConnectURL() {
        return connectURL;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
    
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("[BDD]");
        s.append("className: ").append(className).append('\n');
        s.append("connectURL: ").append(connectURL).append('\n');
        s.append("user: ").append(user).append('\n');
        s.append("password: ").append(password).append('\n');
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
        if (object instanceof BDD) {
            final BDD that = (BDD) object;

            return Utilities.equals(this.className,  that.className)  &&
                   Utilities.equals(this.connectURL, that.connectURL) &&
                   Utilities.equals(this.user  ,     that.user)       &&
                   Utilities.equals(this.password,   that.password);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + (this.className != null ? this.className.hashCode() : 0);
        hash = 59 * hash + (this.connectURL != null ? this.connectURL.hashCode() : 0);
        hash = 59 * hash + (this.user != null ? this.user.hashCode() : 0);
        hash = 59 * hash + (this.password != null ? this.password.hashCode() : 0);
        return hash;
    }
}
