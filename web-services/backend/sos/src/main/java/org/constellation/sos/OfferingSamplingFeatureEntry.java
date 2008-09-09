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
package org.constellation.sos;

import org.constellation.catalog.Entry;
import org.constellation.gml.v311.ReferenceEntry;

/**
 *
 * @author Guilhem Legal
 */
public class OfferingSamplingFeatureEntry extends Entry {

    /**
     * The offering identifier.
     */
    private String idOffering;
    
    /**
     * La station associe a cet offering.
     */
    private ReferenceEntry component;
    
    /**
     * Cree une nouveau lien entre une Station et un offering. 
     */
    public OfferingSamplingFeatureEntry(String idOffering, ReferenceEntry component) {
        super(component.getId());
        this.idOffering = idOffering;
        this.component  = component;
    }

    /**
     * Retourne l'id de l'offering.
     */
    public String getIdOffering() {
        return idOffering;
    }

    /**
     * Retourne le process associe a cet offering.
     */
    public ReferenceEntry getComponent() {
        return component;
    }
    
}
