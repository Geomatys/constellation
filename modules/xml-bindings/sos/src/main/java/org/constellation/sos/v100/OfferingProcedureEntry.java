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
package org.constellation.sos.v100;

import org.constellation.catalog.Entry;
import org.constellation.gml.v311.ReferenceEntry;
import org.geotoolkit.util.Utilities;

/**
 *
 * @author legal
 */
public class OfferingProcedureEntry extends Entry {

    /**
     * The offering identifier.
     */
    private String idOffering;
    
    /**
     * the process associated whith this offering.
     */
    private ReferenceEntry component;
    
    /**
     * Build a new link between a procedure and an offering. 
     */
    public OfferingProcedureEntry(String idOffering, ReferenceEntry component) {
        super(component.getHref());
        this.idOffering = idOffering;
        this.component  = component;
    }

    /**
     * Return the offering id.
     */
    public String getIdOffering() {
        return idOffering;
    }

    /**
     * Return the process associated with this offering.
     */
    public ReferenceEntry getComponent() {
        return component;
    }
    
    /**
     * Verifie si cette entree est identique a l'objet specifie.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof OfferingProcedureEntry && super.equals(object)) {
            final OfferingProcedureEntry that = (OfferingProcedureEntry) object;
            return Utilities.equals(this.idOffering, that.idOffering) &&
                   Utilities.equals(this.component,  that.component);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + (this.idOffering != null ? this.idOffering.hashCode() : 0);
        hash = 83 * hash + (this.component != null ? this.component.hashCode() : 0);
        return hash;
    }
}
