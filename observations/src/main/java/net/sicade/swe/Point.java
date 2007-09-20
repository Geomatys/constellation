/*
 * Sicade - Syst?mes int?gr?s de connaissances pour l'aide ? la d?cision en environnement
 * (C) 2005, Institut de Recherche pour le D?veloppement
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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.geotools.resources.Utilities;



/**
 * Represente un point designé par un identifiant et une position. 
 *
 * @author Guilhem Legal
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Point {
   
    /**
     * l'indentifier du point.
     */
    private String id;
    
    /**
     * La position du point.
     */
    private Position position;
    
    /**
     * constructeur vide utilisé par jaxb.
     */
    public Point(){}
    
    /** 
     * Crée un nouveau point design� par l'identifiant et la position specifi�  
     */
    public Point(String id, Position position) {
        this.id       = id;
        this.position = position;
    }
    
    public Position getPosition(){
        return position;
    }

    /**
     * Retourne l'identifiant du point.
     */
    public String getId() {
        return id;
    }
    
    /**
     * Retourne un code représentant ce point.
     */
    @Override
    public final int hashCode() {
        return id.hashCode();
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
            final Point that = (Point) object;
            return Utilities.equals(this.id,       that.id) &&
                   Utilities.equals(this.position, that.position) ;
        }
        return false;
    }
}
