/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2010, Geomatys
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

package org.constellation.generic.database;

import javax.xml.bind.JAXBException;
import org.geotoolkit.xml.MarshallerPool;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public final class GenericDatabaseMarshallerPool {

    private static final MarshallerPool instance;
    static {
        try {
            instance = new MarshallerPool("org.constellation.configuration:"       +
                                          "org.constellation.dto:"                 +
                                          "org.constellation.generic.database:"    +
                                          "org.geotoolkit.ogc.xml.v110:"           +
                                          "org.geotoolkit.internal.jaxb.geometry:" +
                                          "org.geotoolkit.gml.xml.v311");
        } catch (JAXBException ex) {
            throw new AssertionError(ex); // Should never happen, unless we have a configuration error.
        }
    }
    private GenericDatabaseMarshallerPool() {}

    public static MarshallerPool getInstance() {
        return instance;
    }
}
