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

package org.constellation.generic.vocabulary;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;
import org.geotools.util.Utilities;

/**
 *
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Keyword {

    @XmlAttribute(name = "SDNIdent")
    private String SDNIdent;
    
    @XmlValue
    private String value;
    
    public Keyword() {
    }
    
    public Keyword(String id, String value) {
        SDNIdent   = id;
        this.value = value;
    }
    
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder("[Keyword]: ").append(getSDNIdent()).append(':').append(getValue());
        return sb.toString();
    }

    public String getSDNIdent() {
        return SDNIdent;
    }

    public void setSDNIdent(String SDNIdent) {
        this.SDNIdent = SDNIdent;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj instanceof Keyword) {
            Keyword that = (Keyword) obj;
            return Utilities.equals(this.SDNIdent, that.SDNIdent) &&
                   Utilities.equals(this.value, that.value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (this.SDNIdent != null ? this.SDNIdent.hashCode() : 0);
        return hash;
    }
}
