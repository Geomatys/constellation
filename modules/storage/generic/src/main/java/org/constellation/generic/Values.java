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
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.sis.util.logging.Logging;

/**
 * A container for values retrieved from a database by a generic reader.
 *
 * @author Guilhem Legal (Geomatys)
 */
public class Values {

    private static final Logger LOGGER = Logging.getLogger(Values.class);
    /**
     * A Map of (varName) - (list of values) refreshed at every request.
     */
    private final Map<String, List<Object>> values;

    /**
     * Build a new values container.
     */
    public Values() {
        values = new HashMap<String, List<Object>>();
    }

    /**
     * Build a new values container with the specified values.
     * This constructor is used for test when there is no database to fil the container.
     *
     * @param singleValue   A map of (variable name) - (value)
     * @param multipleValue A map of (variable name) - (list of values)
     */
    public Values(final Map<String, List<Object>> values) {
        if (values != null) {
            this.values = values;
        } else {
            this.values = new HashMap<String, List<Object>>();
        }
    }

    /**
     * return the value for the specified variable name.
     *
     * @param variable
     * @return
     */
    public String getVariable(final String variable) {
        final List<Object> result = values.get(variable);
        if (result != null && result.size() > 0) {
            if (result.size() > 1) {
                LOGGER.log(Level.WARNING, "retrieving single value for variable:{0} but the is multiple values", variable);
            }
            if (result.get(0) != null) {
                return result.get(0).toString();
            } else {
                return null;
            }
        } else if (result == null){
            LOGGER.log(Level.WARNING, "there no such variable in the values:{0}", variable);
        }
        return null;
    }
    
    public Object getTypedVariable(final String variable) {
        final List<Object> result = values.get(variable);
        if (result != null && result.size() > 0) {
            if (result.size() > 1) {
                LOGGER.log(Level.WARNING, "retrieving single value for variable:{0} but the is multiple values", variable);
            }
            return result.get(0);
        } else if (result == null){
            LOGGER.log(Level.WARNING, "there no such variable in the values:{0}", variable);
        }
        return null;
    }

    /**
     * return a list of value for the specified variable name.
     *
     * @param variables
     * @return
     */
    public List<String> getVariables(final String variable) {
        return getVariables(variable, false);
    }
    
    public List<String> getVariables(final String variable, final boolean acceptNull) {
        if (values != null) {
            final List<Object> typedResults = values.get(variable);
            if (typedResults == null) {
                LOGGER.log(Level.WARNING, "there no such variable in the values:{0}", variable);
                return new ArrayList<String>();
            }
            final List<String> result = new ArrayList<String>(); 
            for (Object typedResult : typedResults) {
                if (typedResult != null) {
                    result.add(typedResult.toString());
                } else if (acceptNull){
                    result.add(null);
                }
            }
            return result;
        }
        return null;
    }
    
    public List<Object> getTypedVariables(final String variable) {
        if (values != null) {
            final List<Object> typedResults = values.get(variable);
            if (typedResults == null) {
                LOGGER.log(Level.WARNING, "there no such variable in the values:{0}", variable);
                return new ArrayList<Object>();
            }
            return typedResults;
        }
        return null;
    }

    /**
     * Add a new value to the specified multiple variable.
     *
     * @param varName The name of the variable.
     * @param value   The value to add.
     */
    public void addToValue(final String varName, final Object value) {
        if (values.get(varName) == null) {
           values.put(varName, new ArrayList<Object>());
        }
        values.get(varName).add(value);
    }
    
    /**
     * Add all the map of the specified Values to the current map.
     *
     * @param varName The name of the variable.
     * @param value   The value to add.
     */
    public void mergedValues(final Values value) {
        if (value != null) {
            this.values.putAll(value.values);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("[Values]").append('\n');
        for (Entry<String, List<Object>> entry : values.entrySet()) {
            sb.append(entry.getKey()).append(':').append('\n');
            for (Object val : entry.getValue()) {
                sb.append('\t').append(val).append('\n');
            }
        }
        return sb.toString();
    }
    
}
