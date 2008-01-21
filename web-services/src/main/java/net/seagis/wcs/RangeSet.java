/*
 * Sicade - SystÃ¨mes intÃ©grÃ©s de connaissances pour l'aide Ã  la dÃ©cision en environnement
 * (C) 2005, Institut de Recherche pour le DÃ©veloppement
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

package net.seagis.wcs;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RangeSet")
public class RangeSet {
    
    @XmlElement(name="RangeSet")
    private RangeSetType rangeSet;
    
    /**
     * An empty constructor used by JAXB.
     */
    RangeSet(){
        
    }
    
    /**
     * Build a new RangeSet.
     */
    public RangeSet(RangeSetType rangeSet){
        this.rangeSet = rangeSet;
    }
    
    /**
     * Return the rangeSet property
     */
    public RangeSetType getRangeSet(){
        return rangeSet;
    }

}
