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
package org.constellation.sampling;

import javax.xml.bind.annotation.XmlType;
import org.constellation.catalog.Entry;
import org.opengis.observation.sampling.SamplingFeatureRelation;
import org.opengis.util.GenericName;

/**
 *
 * @version $Id:
 * @author Guilhem Legal
 */
@XmlType(name="SamplingFeatureRelation")
public class SamplingFeatureRelationEntry extends Entry implements SamplingFeatureRelation {
    
    // JAXBISSUE private GenericNameEntry role;
    
    private SamplingFeatureEntry target;
    
    /**
     * Constructeur vide utilisé par JAXB
     */
    private SamplingFeatureRelationEntry() {}
    
    /**
     */
    public SamplingFeatureRelationEntry(String name, SamplingFeatureEntry target) {
        super(name);
        //this.role   = role;
        this.target = target;
    }
    
    /**
     * {@inheritDoc}
     */
    public GenericName getRole(){
        throw new UnsupportedOperationException("Not supported yet.");
        //return role;
    }
    
    /**
     * {@inheritDoc}
     */
    public SamplingFeatureEntry getTarget(){
        return target;
    }
}
