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

package org.constellation.dto;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author Bernard Fabien (Geomatys).
 * @version 0.9
 * @since 0.9
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public final class SimpleValue implements Serializable {

    @XmlElement
    private String value;

    public SimpleValue() {
    }

    public SimpleValue(final String value) {
        this.value = value;
    }

    public SimpleValue(final Boolean value) {
        this.value = value.toString();
    }

    public SimpleValue(final int value) {
        this.value = Integer.toString(value);
    }

    public SimpleValue(final double value) {
        this.value = Double.toString(value);
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public Boolean getAsBoolean() {
        return Boolean.parseBoolean(value);
    }

    public int getAsInt() {
        return Integer.parseInt(value);
    }

    public double getAsDouble() {
        return Double.parseDouble(value);
    }

    @Override
    public String toString() {
        return "[SimpleValue value=" + value + "]";
    }
}
