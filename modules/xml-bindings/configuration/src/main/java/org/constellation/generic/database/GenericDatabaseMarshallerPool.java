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

package org.constellation.generic.database;

import org.apache.sis.xml.MarshallerPool;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public final class GenericDatabaseMarshallerPool {

    private static final JAXBContext ctx;
    private static final MarshallerPool instance;
    static {
        try {
            ctx = JAXBContext.newInstance(
                    "org.constellation.configuration:" +
                            "org.constellation.dto:" +
                            "org.constellation.generic.database:" +
                            "org.geotoolkit.ogc.xml.v110:" +
                            "org.apache.sis.internal.jaxb.geometry:" +
                            "org.geotoolkit.gml.xml.v311");
            instance = new MarshallerPool(ctx, null);
        } catch (JAXBException ex) {
            throw new AssertionError(ex); // Should never happen, unless we have a configuration error.
        }
    }
    private GenericDatabaseMarshallerPool() {}

    public static MarshallerPool getInstance() {
        return instance;
    }
    public static JAXBContext getContext() {
        return ctx;
    }
}
