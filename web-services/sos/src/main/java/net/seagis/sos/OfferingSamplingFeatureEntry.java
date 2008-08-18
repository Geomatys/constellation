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
import net.seagis.gml.v311.ReferenceEntry;

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
