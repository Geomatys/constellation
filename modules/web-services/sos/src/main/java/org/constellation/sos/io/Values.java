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

package org.constellation.sos.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class Values {
    /**
     * A Map of varName - value refreshed at every request.
     */
    protected Map<String, String> singleValue;
    /**
     * * A Map of varName - list of value refreshed at every request.
     */
    protected Map<String, List<String>> multipleValue;

    public Values() {
        singleValue = new HashMap<String, String>();
        multipleValue = new HashMap<String, List<String>>();
    }

    public Values(Map<String, String> singleValue, Map<String, List<String>> multipleValue) {
        this.singleValue   = singleValue;
        this.multipleValue = multipleValue;
    }

    public void addSingleValue(String varName, String value) {
        singleValue.put(varName, value);
    }

    public void createNewMultipleValue(String varName) {
        multipleValue.put(varName, new ArrayList<String>());
    }

    public void addToMultipleValue(String varName, String value) {
        if (multipleValue.get(varName) != null) {
            multipleValue.get(varName).add(value);
        }
    }
}
