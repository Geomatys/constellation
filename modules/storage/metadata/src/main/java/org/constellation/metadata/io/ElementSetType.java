/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
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

package org.constellation.metadata.io;

/**
 * This class is a copy of the enum in csw xml binding in order to use it without havinf to import all the dependencies of this module.
 * 
 * @author Guilhem Legal
 */
public enum ElementSetType {

    BRIEF("brief"),
    SUMMARY("summary"),
    FULL("full");
    private final String value;

    ElementSetType(final String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ElementSetType fromValue(final String v) {
        for (ElementSetType c: ElementSetType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
