/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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


package org.constellation.wfs.utils;

import java.util.Iterator;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;

import static org.junit.Assert.*;

/**
 *
 * @author Johann Sorel (Geomatys)
 * @module pending
 */
public class GlobalUtils {

    public static void featureEquals(Feature expResult, Feature result) {
        assertEquals(expResult.getIdentifier(), result.getIdentifier());
        assertEquals(expResult.getType(), result.getType());
        assertEquals(expResult+"\n"+result, expResult.getProperties().size(), result.getProperties().size());

        final Iterator<Property> props1 = expResult.getProperties().iterator();
        final Iterator<Property> props2 = result.getProperties().iterator();
        while(props1.hasNext()){
            assertEquals(props1.next(), props2.next());
        }

        //this test alone should be enough, but we want to know more exactly what may
        //go wrong
        assertEquals(expResult, result);
    }

    public static String removeXmlns(String xml) {
        String s = xml;
        s = s.replaceAll("xmlns=\"[^\"]*\" ", "");
        s = s.replaceAll("xmlns=\"[^\"]*\"", "");
        s = s.replaceAll("xmlns:[^=]*=\"[^\"]*\" ", "");
        s = s.replaceAll("xmlns:[^=]*=\"[^\"]*\"", "");
        return s;
    }

}
