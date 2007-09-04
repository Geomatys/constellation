/*
 * Sicade - Systémes intégrés de connaissances pour l'aide à la d�cision en environnement
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

package net.sicade.observation;

import java.sql.Date;
import org.opengis.temporal.TemporalObject;

/**
 * a temporal object  from ISO 19108
 *
 * @author Guilhem Legal
 */
public class TemporalObjectEntry implements TemporalObject{
    
    private Date time;
    
    public TemporalObjectEntry(Date time) {
        this.time = time;
    }
    
}
