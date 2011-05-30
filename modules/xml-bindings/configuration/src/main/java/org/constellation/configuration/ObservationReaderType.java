/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2009, Geomatys
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

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public enum ObservationReaderType {
    
    GENERIC("generic"),

    DEFAULT("default"),

    FILESYSTEM("filesystem");
    
    private final String name;
    
    private ObservationReaderType(final String name) {
        this.name = name;
    }
    
    public static ObservationReaderType fromName(final String name) {
        for (ObservationReaderType o : ObservationReaderType.values()) {
            if (o.getName().equals(name)) {
                return o;
            }
        }
        return null;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
}
