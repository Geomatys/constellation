/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package net.seagis.sos;

import net.seagis.catalog.Entry;
import net.seagis.swe.PhenomenonEntry;

/**
 *
 * @author Guilhem legal
 */
public class OfferingPhenomenonEntry extends Entry {
    
    /**
     * The offering identifier.
     */
    private String idOffering;
    
    /**
     * The phenomenon associed to this offering (reference).
     */
    private PhenomenonEntry component;
    
    /**
     * Build a new link between a procedure and an offering. 
     */
    public OfferingPhenomenonEntry(String idOffering, PhenomenonEntry component) {
        super(component.getId());
        this.idOffering = idOffering;
        this.component  = component;
    }

    /**
     * Return the phenomenon id.
     */
    public String getIdOffering() {
        return idOffering;
    }

    /**
     * Retourne le phénomène associé.
     */
    public PhenomenonEntry getComponent() {
        return component;
    }

}
