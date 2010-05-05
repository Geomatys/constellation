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

package org.constellation.generic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A container for values retrieved from a database by a generic reader.
 *
 * @author Guilhem Legal (Geomatys)
 */
public class Values {

    /**
     * A Map of (varName) - (list of values) refreshed at every request.
     */
    private final Map<String, List<String>> values;

    /**
     * Build a new values container.
     */
    public Values() {
        values = new HashMap<String, List<String>>();
    }

    /**
     * Build a new values container with the specified values.
     * This constructor is used for test when there is no database to fil the container.
     *
     * @param singleValue   A map of (variable name) - (value)
     * @param multipleValue A map of (variable name) - (list of values)
     */
    public Values(Map<String, List<String>> values) {
        if (values != null) {
            this.values = values;
        } else {
            this.values = new HashMap<String, List<String>>();
        }
    }

    /**
     * return the value for the specified variable name.
     *
     * @param variable
     * @return
     */
    public String getVariable(String variable) {
        final List<String> result = values.get(variable);
        if (result != null && result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    /**
     * return a list of value for the specified variable name.
     *
     * @param variables
     * @return
     */
    public List<String> getVariables(String variable) {
        if (values != null) {
            List<String> result = values.get(variable);
            if (result == null) {
                result = new ArrayList<String>();
            }
            return result;
        }
        return null;
    }

    /**
     * Add a new value to the specified multiple variable.
     *
     * @param varName The name of the variable.
     * @param value   The value to add.
     */
    public void addToValue(String varName, String value) {
        if (values.get(varName) == null) {
           values.put(varName, new ArrayList<String>());
        }
        values.get(varName).add(value);
    }
}
