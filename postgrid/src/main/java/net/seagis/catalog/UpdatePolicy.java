/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
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
package net.seagis.catalog;


/**
 * The policy to apply during a table update if a record already exists for the same
 * primary key.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public enum UpdatePolicy {
    /**
     * The new record is discarted and the existing one is keept unchanged.
     */
    SKIP_EXISTING,

    /**
     * The old record is deleted and the new record is inserted as a replacement.
     */
    REPLACE_EXISTING,

    /**
     * Remove all previous records before to insert new ones.
     */
    CLEAR_BEFORE_UPDATE
}
