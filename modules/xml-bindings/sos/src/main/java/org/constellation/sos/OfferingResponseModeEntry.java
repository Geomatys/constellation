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

/**
 *
 * @author legal
 */
public class OfferingResponseModeEntry extends Entry{

    /**
     * L'identifiant de l'offering.
     */
    private String idOffering;
    
    /**
     * Le mode de reponse associe a cet offering.
     */
    private ResponseModeType mode;
    
    /**
     * Cree une nouveau lien entre une procedure et un offering. 
     */
    public OfferingResponseModeEntry(String idOffering, ResponseModeType mode) {
        super(mode.value());
        this.idOffering = idOffering;
        this.mode  = mode;
    }

    /**
     * Retourne l'id de l'offering
     */
    public String getIdOffering() {
        return idOffering;
    }

    /**
     * Retourne le mode de reponse associe.
     */
    public ResponseModeType getMode() {
        return mode;
    }

}
