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

package org.constellation.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBException;
import org.apache.sis.internal.jaxb.LegacyNamespaces;
import org.apache.sis.metadata.iso.DefaultMetadata;
import org.apache.sis.xml.MarshallerPool;
import static org.geotoolkit.gml.xml.GMLMarshallerPool.createJAXBContext;
import org.geotoolkit.xml.AnchoredMarshallerPool;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class ISOMarshallerPool {
    private static final MarshallerPool instance;
    static {
        try {
            final Map<String, Object> properties = new HashMap<>();
            properties.put(LegacyNamespaces.APPLY_NAMESPACE_REPLACEMENTS, Boolean.FALSE);
            instance = new AnchoredMarshallerPool(createJAXBContext(getAllClassesList()), properties);
        } catch (JAXBException ex) {
            throw new AssertionError(ex); // Should never happen, unless we have a build configuration problem.
        }
    }
    
    private ISOMarshallerPool() {}

    public static MarshallerPool getInstance() {
        return instance;
    }
    
    private static Class[] getAllClassesList() {
        final List<Class> classeList = new ArrayList<>();

        //ISO 19115 class
        classeList.add(DefaultMetadata.class);

        // Inspire classes
        classeList.add(org.geotoolkit.inspire.xml.ObjectFactory.class);

        
        // GML base factory
        classeList.add(org.apache.sis.internal.jaxb.geometry.ObjectFactory.class);
        classeList.add(org.geotoolkit.gml.xml.v311.ObjectFactory.class);
        classeList.add(org.geotoolkit.gml.xml.v321.ObjectFactory.class);

        // vertical CRS
        try {
            Class vcrsClass = Class.forName("org.apache.sis.referencing.crs.DefaultVerticalCRS");
            classeList.add(vcrsClass);
        } catch (ClassNotFoundException ex) {}

        // we add the extensions classes
        classeList.add(org.geotoolkit.service.ServiceIdentificationImpl.class);
      
         return classeList.toArray(new Class[classeList.size()]);
    }
}
