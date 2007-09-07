/*
 * Sicade - Syst�mes int�gr�s de connaissances pour l'aide � la d�cision en environnement
 * (C) 2005, Institut de Recherche pour le D�veloppement
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
package net.sicade.swe;

import org.geotools.resources.Utilities;

/**
 * decrit une position.
 *
 * @author Guilhem Legal
 */
public class Position {
    
    private String srsName;
    private int srsDimension;
    private int value;
    
    /** Creates a new instance of position */
    public Position(String srsName, int srsDimension, int value) {
        this.srsName = srsName;
        this.srsDimension = srsDimension;
        this.value = value;
    }

    public String getSrsName() {
        return srsName;
    }

    public int getSrsDimension() {
        return srsDimension;
    }

    public int getValue() {
        return value;
    }
    
    /**
     * Retourne un code représentant ce phenomene composé.
     */
    @Override
    public final int hashCode() {
        return srsName.hashCode();
    }

    /**
     * Vérifie si cette entré est identique à l'objet spécifié.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final Position that = (Position) object;
            return Utilities.equals(this.srsName,      that.srsName) &&
                   Utilities.equals(this.srsDimension, that.srsDimension) &&
                   Utilities.equals(this.value,        that.value) ;
        }
        return false;
    }
    
}
