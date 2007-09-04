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

package net.sicade.observation;



/**
 * Represente un point designé par un identifiant et une position. 
 *
 * @author Guilhem Legal
 */
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
     * Cr�e un nouveau point design� par l'identifiant et la position specifi�  
     */
    public Point(String id, Position position) {
        this.id       = id;
        this.position = position;
    }
    
    public Position getPosition(){
        return position;
    }
}
