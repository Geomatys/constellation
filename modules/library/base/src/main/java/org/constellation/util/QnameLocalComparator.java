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
package org.constellation.util;

import java.util.Comparator;
import javax.xml.namespace.QName;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class QnameLocalComparator implements Comparator<QName>{

    @Override
    public int compare(final QName o1, final QName o2) {
        if (o1 != null && o1.getLocalPart() != null && o2 != null) {
            return o1.getLocalPart().compareTo(o2.getLocalPart());
        }
        return -1;
    }
    
}