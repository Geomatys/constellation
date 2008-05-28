/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2004, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */

package net.seagis.referencing;

import java.util.Collection;
import java.util.Set;
import javax.xml.bind.annotation.XmlType;
import org.opengis.metadata.extent.Extent;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.ReferenceSystem;
import org.opengis.util.GenericName;
import org.opengis.util.InternationalString;


/**
 *
 * @author legal
 */
@XmlType(name = "RS_ReferenceSystem")
public class ReferenceSystemImpl implements ReferenceSystem {
                                
    private ReferenceIdentifier referenceSystemIdentifier;
    
    public ReferenceSystemImpl() {
        
    }
    
    public ReferenceIdentifier getReferenceSystemIdentifier() {
        return referenceSystemIdentifier;
    }
                   
    public void setReferenceSystemIdentifier(ReferenceIdentifier referenceSystemIdentifier) {
       this.referenceSystemIdentifier = referenceSystemIdentifier;
    }

    public Extent getDomainOfValidity() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Extent getValidArea() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public InternationalString getScope() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ReferenceIdentifier getName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<GenericName> getAlias() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Set<ReferenceIdentifier> getIdentifiers() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public InternationalString getRemarks() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String toWKT() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
