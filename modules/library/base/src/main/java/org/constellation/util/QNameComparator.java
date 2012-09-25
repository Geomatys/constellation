/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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
public class QNameComparator implements Comparator<QName>{

    @Override
    public int compare(final QName o1, final QName o2) {
        if (o1 != null && o2 != null) {
            if (o1.getNamespaceURI() != null && o2.getNamespaceURI() != null) {
                if (o1.getNamespaceURI().equals(o2.getNamespaceURI())) {
                    return o1.getLocalPart().compareTo(o2.getLocalPart());
                } else {
                    return o1.getNamespaceURI().compareTo(o2.getNamespaceURI());
                }
            }
            return o1.getLocalPart().compareTo(o2.getLocalPart());
        }
        return -1;
    }

}
