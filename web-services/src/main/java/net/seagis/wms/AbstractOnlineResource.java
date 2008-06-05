/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2008 Geomatys
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
package net.seagis.wms;

import javax.xml.bind.annotation.XmlTransient;

/**
 * Abstract class for Online resource object.
 * 
 * @author Guilhem Legal
 */
@XmlTransient
public abstract class AbstractOnlineResource {
    
     /**
     * Gets the value of the href property.
     * 
     */
    public abstract String getHref();
    /**
     * Gets the value of the href property.
     * 
     */
    public abstract void setHref(String href);
}
