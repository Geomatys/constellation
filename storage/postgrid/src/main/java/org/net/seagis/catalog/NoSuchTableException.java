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


/**
 * Throws when a table was not found.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class NoSuchTableException extends CatalogException {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = -4690523618524715517L;

    /**
     * Creates an exception with no cause and no details message.
     */
    public NoSuchTableException() {
        super();
    }

    /**
     * Creates an exception with the specified details message.
     *
     * @param message The detail message.
     */
    public NoSuchTableException(final String message) {
        super(message);
    }

    /**
     * Creates an exception with the specified details message and cause.
     *
     * @param message The detail message.
     * @param cause The cause for this exception.
     */
    public NoSuchTableException(final String message, final Exception cause) {
        super(message, cause);
    }
}
