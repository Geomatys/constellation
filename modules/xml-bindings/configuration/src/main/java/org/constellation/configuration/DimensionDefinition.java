/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2013, Geomatys
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
import javax.xml.bind.annotation.XmlElement;


/**
 *
 * @author Cédric Briançon (Geomatys)
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class DimensionDefinition {

    @XmlElement(name="CRS")
    private String crs;

    @XmlElement(name="Lower")
    private String lower;

    @XmlElement(name="Upper")
    private String upper;

    public DimensionDefinition() {
    }

    public String getCrs() {
        return crs;
    }

    public String getLower() {
        return lower;
    }

    public String getUpper() {
        return upper;
    }

    public void setCrs(String crs) {
        this.crs = crs;
    }

    public void setLower(String lower) {
        this.lower = lower;
    }

    public void setUpper(String upper) {
        this.upper = upper;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("[DimensionDefinition]")
          .append("crs:").append(crs).append('\n')
          .append("lower:").append(lower).append('\n')
          .append("upper:").append(upper).append('\n');
        return sb.toString();
    }

}
