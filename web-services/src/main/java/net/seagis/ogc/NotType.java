/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
 * (C) 2007, Geomatys
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

package net.seagis.ogc;

import org.opengis.filter.Filter;
import org.opengis.filter.Not;

/**
 *
 * @author guilhem
 */
public class NotType extends UnaryLogicOpType implements Not {

    /**
     * An empty constructor used by JAXB
     */
     NotType() {
         
     }
     
     /**
      * Build a new Unary logic operator of type NOT 
      */
     public NotType(Object operators) {
         super(operators);
         
     }

}
