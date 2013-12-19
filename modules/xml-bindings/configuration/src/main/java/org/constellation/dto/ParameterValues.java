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

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Legal Guilhem (Geomatys).
 * @version 0.9
 * @since 0.9
 */
@XmlRootElement
public class ParameterValues {

    private HashMap<String,String> values = new HashMap<>();

    public ParameterValues() {

    }

    public ParameterValues(final Map<String,String> values) {
        this.values = new HashMap<>(values);
    }

    public String get(final String key) {
        if (values != null) {
            return values.get(key);
        }
        return null;
    }
    /**
     * @return the values
     */
    public HashMap<String,String> getValues() {
        return values;
    }

    public Boolean getAsBoolean(final String paramName) {
        for (String key : values.keySet()) {
            if (key.equalsIgnoreCase(paramName)) {
                return Boolean.parseBoolean(values.get(key));
            }
        }
        return false;
    }

    /**
     * @param values the values to set
     */
    public void setValues(HashMap<String,String> values) {
        this.values = values;
    }
}
