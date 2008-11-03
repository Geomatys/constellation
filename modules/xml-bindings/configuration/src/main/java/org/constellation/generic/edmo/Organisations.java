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
package org.constellation.generic.edmo;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Organisations")
public class Organisations {

    private List<Organisation> Organisation;

    public List<Organisation> getOrganisation() {
        if (Organisation == null) {
            Organisation = new ArrayList<Organisation>();
        }
        return Organisation;
    }

    public void setOrganisation(List<Organisation> organisation) {
        this.Organisation = organisation;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Organisations:").append('\n');
        int i = 0;
        if (Organisation != null) {
            for (Organisation org : Organisation) {
                sb.append(i).append(": ").append(org).append('\n');
                i++;
            }
        }
        return sb.toString();
    }
}
