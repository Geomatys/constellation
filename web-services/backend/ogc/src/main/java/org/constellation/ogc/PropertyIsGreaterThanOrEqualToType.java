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
package org.constellation.ogc;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import org.opengis.filter.PropertyIsGreaterThanOrEqualTo;

/**
 *
 * @author guilhem
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PropertyIsGreaterThanOrEqualTo")
public class PropertyIsGreaterThanOrEqualToType extends BinaryComparisonOpType implements PropertyIsGreaterThanOrEqualTo {
    
    /**
     * Empty constructor used by JAXB
     */
    PropertyIsGreaterThanOrEqualToType() {
        
    }
    
    /**
     * Build a new Binary comparison operator
     */
    public PropertyIsGreaterThanOrEqualToType(LiteralType literal, PropertyNameType propertyName, Boolean matchCase) {
        super(literal, propertyName, matchCase);
    }

}
