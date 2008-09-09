/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
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

import net.seagis.resources.i18n.ResourceKeys;
import net.seagis.resources.i18n.Resources;


/**
 * Thrown when an inconsistency has been found during an update. This exception occurs for example
 * during an {@code INSERT}, {@code DELETE} or {@code UPDATE} statement if we expected a change in
 * only one row but more rows were affected.
 * <p>
 * When such an inconsistent update occurs, the updated {@linkplain Table table} will typically
 * revert to the previous state through a call to {@link java.sql.Connection#rollback()}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class IllegalUpdateException extends CatalogException {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = 4475051674927844277L;

    /**
     * Creates an exception with no cause and no details message.
     */
    public IllegalUpdateException() {
        super();
    }

    /**
     * Creates an exception with a detail messages created from the specified number
     * of rows that were changed.
     *
     * @param count The unexpected number of rows changed.
     */
    public IllegalUpdateException(final int count) {
        this(Resources.format(ResourceKeys.ERROR_UNEXPECTED_UPDATE_$1, count));
    }

    /**
     * Creates an exception with the specified details message.
     *
     * @param message The detail message.
     */
    public IllegalUpdateException(final String message) {
        super(message);
    }

    /**
     * Creates an exception with the specified cause and no details message.
     *
     * @param cause The cause for this exception.
     */
    public IllegalUpdateException(final Exception cause) {
        super(cause);
    }
}
