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
package net.seagis.wms.v111;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "format"
})
@XmlRootElement(name = "Exception")
public class Exception {

    @XmlElement(name = "Format", required = true)
    private List<String> format = new ArrayList<String>();

    /**
     * An empty constructor used by JAXB.
     */
     Exception() {
     }

    /**
     * Build a new Contact person primary object.
     */
    public Exception(final String... formats) {
        for (final String element : formats) {
            this.format.add(element);
        }
    }
    /**
     * Gets the value of the format property.
     * 
     */
    public List<String> getFormat() {
        return Collections.unmodifiableList(format);
    }

}
