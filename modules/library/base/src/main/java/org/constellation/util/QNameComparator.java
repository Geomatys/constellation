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
