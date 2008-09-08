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
package net.seagis.catalog;

import java.util.logging.Logger;


/**
 * Base interface for catalog elements.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface Element {
    /**
     * The logger for events related to catalog elements.
     *
     * @see LoggingLevel
     */
    Logger LOGGER = Logger.getLogger("net.seagis.catalog");

    /**
     * Returns the name for this element. It is often (but not always) the primary key value
     * in a database table. The name should be meaningful enough for inclusion in a graphical
     * user interface.
     */
    String getName();

    /**
     * Returns comments applicable to this element, or {@code null} if none. The remarks may
     * be used as "<cite>tooltip text</cite>" in a graphical user interface.
     */
    String getRemarks();
}
