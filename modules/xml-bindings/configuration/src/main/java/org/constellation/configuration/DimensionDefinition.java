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
