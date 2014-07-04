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

package org.constellation.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

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
