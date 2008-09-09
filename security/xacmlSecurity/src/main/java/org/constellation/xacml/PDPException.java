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
package org.constellation.xacml;


/**
 * Exception which comes during the creation or the use of the PDP implementation.
 *
 * @version $Id$
 * @author Cédric Briançon
 */
public class PDPException extends XACMLException {
    /**
     * Creates an exception with no cause and no details message.
     */
    public PDPException() {
        super();
    }

    /**
     * Creates an exception with the specified details message.
     *
     * @param message The detail message.
     */
    public PDPException(final String message) {
        super(message);
    }

    /**
     * Creates an exception with the specified cause and no details message.
     *
     * @param cause The cause for this exception.
     */
    public PDPException(final Exception cause) {
        super(cause);
    }

    /**
     * Creates an exception with the specified details message and cause.
     *
     * @param message The detail message.
     * @param cause The cause for this exception.
     */
    public PDPException(final String message, final Exception cause) {
        super(message, cause);
    }
}
