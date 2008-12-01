/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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
package org.constellation.metadata;

/**
 *
 * @author Guilhem Legal
 */
public enum CSWProfile {

    /**
     * A profile using a unknown database requested through a configuration file.
     */
    GENERIC,
    
    /**
     * A profile using an MDWeb database.
     */
    MDWEB,
    
    /**
     * A profile using a file system instead of a database.
     */
    FILESYSTEM
    
}
